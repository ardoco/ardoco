package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ElementExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14ParserBaseVisitor;
import generated.antlr.cpp.CPP14Parser.FunctionBodyContext;
import generated.antlr.cpp.CPP14Parser.TranslationUnitContext;

public class CppElementExtractor extends CPP14ParserBaseVisitor<Void> implements ElementExtractor {
    private final CppElementManager elementManager;

    public CppElementExtractor() {
        this.elementManager = new CppElementManager();
    }

    public CppElementExtractor(CppElementManager elementManager) {
        this.elementManager = elementManager;
    }

    @Override
    public CppElementManager getElements() {
        return elementManager;
    }

    @Override
    public void extract(CommonTokenStream tokens) {
        TranslationUnitContext ctx = buildContext(tokens);

        visitTranslationUnit(ctx);
        addFile(ctx);
    }

    private TranslationUnitContext buildContext(CommonTokenStream tokenStream) {
        CPP14Parser parser = new CPP14Parser(tokenStream);
        return parser.translationUnit();
    }

    @Override
    public Void visitTranslationUnit(CPP14Parser.TranslationUnitContext ctx) {
        if (ctx.declarationseq() != null) {
            for (CPP14Parser.DeclarationContext declaration : ctx.declarationseq().declaration()) {
                visitDeclaration(declaration);
            }
        }
        super.visitTranslationUnit(ctx);
        return null;
    }

    @Override
    public Void visitDeclaration(CPP14Parser.DeclarationContext ctx) {
        if (ctx.functionDefinition() != null) {
            visitFunctionDefinition(ctx.functionDefinition());
        }
        if (ctx.namespaceDefinition() != null) {
            visitNamespaceDefinition(ctx.namespaceDefinition());
        }
        if (ctx.blockDeclaration() != null) {
            visitBlockDeclaration(ctx.blockDeclaration());
        }

        return null;
    }

    @Override
    public Void visitNamespaceDefinition(CPP14Parser.NamespaceDefinitionContext ctx) {
        if (ctx.declarationseq() != null) {
            extractNamespace(ctx);

            for (CPP14Parser.DeclarationContext declaration : ctx.declarationseq().declaration()) {
                visitDeclaration(declaration);
            }
        }
        return null;
    }

    @Override
    public Void visitBlockDeclaration(CPP14Parser.BlockDeclarationContext ctx) {
        if (ctx.simpleDeclaration() != null) {
            visitSimpleDeclaration(ctx.simpleDeclaration());
        }
        return null;
    }

    @Override
    public Void visitFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {
        if (ctx.declarator() == null) {
            return null;
        }
        if (ctx.functionBody() != null) {
            visitFunctionBody(ctx.functionBody());
        }
        String name = ctx.declarator().getText();
        Parent parent = new CppParentExtractor().getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addFunction(name, path, parent, startLine, endLine);
        return null;
    }

    @Override
    public Void visitFunctionBody(FunctionBodyContext ctx) {
        if (ctx.compoundStatement() != null && ctx.compoundStatement().statementSeq() != null
                && ctx.compoundStatement().statementSeq().statement() != null) {
            for (CPP14Parser.StatementContext statement : ctx.compoundStatement().statementSeq().statement()) {
                if (statement.declarationStatement() != null) {
                    visitBlockDeclaration(statement.declarationStatement().blockDeclaration());
                }
            }
        }
        return null;
    }

    @Override
    public Void visitSimpleDeclaration(CPP14Parser.SimpleDeclarationContext ctx) {
        if (ctx.declSpecifierSeq() == null) {
            return null;
        }

        for (CPP14Parser.DeclSpecifierContext declSeq : ctx.declSpecifierSeq().declSpecifier()) {
            if (declSeq != null && declSeq.typeSpecifier().classSpecifier() != null) {
                visitClassSpecifier(declSeq.typeSpecifier().classSpecifier());
            }
            // extractVariablesFromClass(declSeq.typeSpecifier().classSpecifier());
        }

        if (ctx.initDeclaratorList() != null) {
            extractVariableElement(ctx);
        }
        return null;
    }

    @Override
    public Void visitClassSpecifier(CPP14Parser.ClassSpecifierContext ctx) {
        extractClass(ctx);
        extractVariablesFromClass(ctx);
        return null;
    }

    private void extractNamespace(CPP14Parser.NamespaceDefinitionContext ctx) {
        String name = "anonymous";
        if (ctx.Identifier() != null) {
            name = ctx.Identifier().getText();
        }
        String path = PathExtractor.extractPath(ctx);
        Parent parent = new CppParentExtractor().getParent(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();
        addNamespace(name, path, parent, startLine, endLine);
    }

    private void extractClass(CPP14Parser.ClassSpecifierContext ctx) {
        String name = ctx.classHead().classHeadName().getText();
        String path = PathExtractor.extractPath(ctx);
        Parent parent = new CppParentExtractor().getParent(ctx);
        List<String> inherits = getInherits(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addClass(name, path, parent, inherits, startLine, endLine);
    }

    private void extractVariablesFromClass(CPP14Parser.ClassSpecifierContext ctx) {
        if (ctx.memberSpecification() == null) {
            return;
        }
        for (CPP14Parser.MemberdeclarationContext memberCtx : ctx.memberSpecification().memberdeclaration()) {
            if (memberCtx.functionDefinition() != null) {
                visitFunctionDefinition(memberCtx.functionDefinition());
                continue;
            }

            if (memberCtx.declSpecifierSeq() != null) {
                for (CPP14Parser.DeclSpecifierContext declSpec : memberCtx.declSpecifierSeq().declSpecifier()) {
                    if (declSpec.typeSpecifier() != null && declSpec.typeSpecifier().classSpecifier() != null) {
                        // Recursive call to extract inner class members**
                        extractVariablesFromClass(declSpec.typeSpecifier().classSpecifier());
                    }
                }
            }

            if (memberCtx.memberDeclaratorList() != null && memberCtx.declSpecifierSeq() != null) {
                extractVariableElement(memberCtx);
            }
        }
    }

    private void extractVariableElement(CPP14Parser.MemberdeclarationContext ctx) {
        String variableType = ctx.declSpecifierSeq().getText();
        Parent parent = new CppParentExtractor().getParent(ctx);
        List<String> varNames = extractVariableNames(ctx.memberDeclaratorList());
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addVariables(varNames, path, variableType, parent, startLine, endLine);
    }

    private void extractVariableElement(CPP14Parser.SimpleDeclarationContext ctx) {
        String variableType = ctx.declSpecifierSeq().getText();
        Parent parent = new CppParentExtractor().getParent(ctx);
        List<String> varNames = extractVariableNames(ctx.initDeclaratorList());
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addVariables(varNames, path, variableType, parent, startLine, endLine);
    }

    private List<String> extractVariableNames(CPP14Parser.MemberDeclaratorListContext ctx) {
        List<String> varNames = new ArrayList<>();
        for (CPP14Parser.MemberDeclaratorContext memberDec : ctx.memberDeclarator()) {
            // Skip if it is a function or Constructor
            if (memberDec.declarator().pointerDeclarator().noPointerDeclarator() != null && memberDec.declarator()
                    .pointerDeclarator().noPointerDeclarator().parametersAndQualifiers() != null) {
                continue;
            }
            varNames.add(memberDec.declarator().getText());
        }
        return varNames;
    }

    private List<String> extractVariableNames(CPP14Parser.InitDeclaratorListContext ctx) {
        List<String> varNames = new ArrayList<>();
        for (CPP14Parser.InitDeclaratorContext initDec : ctx.initDeclarator()) {
            // Skip if it is a function or Constructor
            if (initDec.declarator().pointerDeclarator().noPointerDeclarator() != null && initDec.declarator()
                    .pointerDeclarator().noPointerDeclarator().parametersAndQualifiers() != null) {
                continue;
            }
            varNames.add(initDec.declarator().getText());
        }
        return varNames;
    }

    private List<String> getInherits(CPP14Parser.ClassSpecifierContext ctx) {
        List<String> inherits = new ArrayList<>();
        if (ctx.classHead().baseClause() != null) {
            for (CPP14Parser.BaseSpecifierContext base : ctx.classHead().baseClause().baseSpecifierList()
                    .baseSpecifier()) {
                String baseClass = base.getText();

                if (base.accessSpecifier() != null) {
                    String accessSpecifier = base.accessSpecifier().getText();
                    baseClass = baseClass.replace(accessSpecifier, "").trim();
                }
                inherits.add(baseClass);
            }
        }
        return inherits;
    }

    private void addVariables(List<String> varNames, String path, String variableType, Parent parent,
            int startLine, int endLine) {
        for (String varName : varNames) {
            addVariableElement(varName, path, variableType, parent, startLine, endLine);
        }
    }

    private void addVariableElement(String varName, String path, String variableType, Parent parent,
            int startLine, int endLine) {
        VariableElement variable = new VariableElement(varName, path, variableType, parent, startLine, endLine);
        elementManager.addVariable(variable);
    }

    private void addFunction(String name, String path, Parent parent, int startLine, int endLine) {
        Element function = new Element(name, path, parent, startLine, endLine);
        elementManager.addFunction(function);
    }

    private void addClass(String name, String path, Parent parent, List<String> inherits, int startLine, int endLine) {
        ClassElement cppClassElement = new ClassElement(name, path, parent, startLine, endLine, inherits);
        elementManager.addClass(cppClassElement);
    }

    private void addNamespace(String name, String path, Parent parent, int startLine, int endLine) {
        Element namespace = new Element(name, path, parent, startLine, endLine);
        elementManager.addNamespace(namespace);
    }

    private void addFile(TranslationUnitContext ctx) {
        String name = PathExtractor.extractNameFromPath(ctx);
        String path = PathExtractor.extractPath(ctx);
        Element file = new Element(name, path);
        elementManager.addFile(file);
    }
}
