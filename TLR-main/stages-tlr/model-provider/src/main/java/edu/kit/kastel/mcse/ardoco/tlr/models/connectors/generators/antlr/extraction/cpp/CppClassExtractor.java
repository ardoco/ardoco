package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14ParserBaseVisitor;

public class CppClassExtractor extends CPP14ParserBaseVisitor<List<ClassElement>> {
    private final List<ClassElement> classes = new ArrayList<>();

    @Override
    public List<ClassElement> visitTranslationUnit(CPP14Parser.TranslationUnitContext ctx) {
        if (ctx.declarationseq() != null) {
            for (CPP14Parser.DeclarationContext declaration : ctx.declarationseq().declaration()) {
                visitDeclaration(declaration);
            }
        }
        return classes;
    }

    @Override
    public List<ClassElement> visitDeclaration(CPP14Parser.DeclarationContext ctx) {
        if (ctx.namespaceDefinition() != null) {
            visitNamespaceDefinition(ctx.namespaceDefinition());
        }
        if (ctx.blockDeclaration() != null) {
            visitBlockDeclaration(ctx.blockDeclaration());
        }
        return classes;
    }

    @Override
    public List<ClassElement> visitNamespaceDefinition(CPP14Parser.NamespaceDefinitionContext ctx) {
        if (ctx.declarationseq() != null) {
            for (CPP14Parser.DeclarationContext declaration : ctx.declarationseq().declaration()) {
                visitDeclaration(declaration);
            }
        }
        return classes;
    }

    @Override
    public List<ClassElement> visitBlockDeclaration(CPP14Parser.BlockDeclarationContext ctx) {
        if (ctx.simpleDeclaration() != null) {
            visitSimpleDeclaration(ctx.simpleDeclaration());
        }
        return classes;
    }

    @Override
    public List<ClassElement> visitSimpleDeclaration(CPP14Parser.SimpleDeclarationContext ctx) {
        if (ctx.declSpecifierSeq() != null) {
            for (CPP14Parser.DeclSpecifierContext declSpecifier : ctx.declSpecifierSeq().declSpecifier()) {
                if (declSpecifier.typeSpecifier() != null && declSpecifier.typeSpecifier().classSpecifier() != null) {
                    visitClassSpecifier(declSpecifier.typeSpecifier().classSpecifier());
                }
            }
        }
        return classes;
    }

    @Override
    public List<ClassElement> visitClassSpecifier(CPP14Parser.ClassSpecifierContext ctx) {
        String name = ctx.classHead().classHeadName().getText();
        String path = PathExtractor.extractPath(ctx);
        Parent parent = new CppParentExtractor().getParent(ctx);
        List<String> inherits = getInherits(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        ClassElement cppClassElement = new ClassElement(name, path, parent, inherits);
        cppClassElement.setStartLine(startLine);
        cppClassElement.setEndLine(endLine);
        classes.add(cppClassElement);
        return classes;
    }

    private List<String> getInherits(CPP14Parser.ClassSpecifierContext ctx) {
        List<String> inherits = new ArrayList<>();
        if (ctx.classHead().baseClause() != null) {
            for (CPP14Parser.BaseSpecifierContext baseSpecifier : ctx.classHead().baseClause().baseSpecifierList()
                    .baseSpecifier()) {
                String baseClass = baseSpecifier.getText();

                if (baseSpecifier.accessSpecifier() != null) {
                    String accessSpecifier = baseSpecifier.accessSpecifier().getText();
                    baseClass = baseClass.replace(accessSpecifier, "").trim();
                }
                inherits.add(baseClass);
            }
        }
        return inherits;
    }

}
