package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14ParserBaseVisitor;

public class CppVariableExtractor extends CPP14ParserBaseVisitor<List<VariableElement>> {
    List<VariableElement> variables = new ArrayList<>();

    @Override
    public List<VariableElement> visitTranslationUnit(CPP14Parser.TranslationUnitContext ctx) {
        super.visitTranslationUnit(ctx);
        return variables;
    }

    @Override
    public List<VariableElement> visitSimpleDeclaration(CPP14Parser.SimpleDeclarationContext ctx) {
        if (isAssignment(ctx)) {
            return variables;
        }

        for (CPP14Parser.DeclSpecifierContext declSeq : ctx.declSpecifierSeq().declSpecifier()) {
            if (declSeq.typeSpecifier().classSpecifier() != null) {
                return extractVariablesFromClass(declSeq.typeSpecifier().classSpecifier());
            }
        }


        if (ctx.initDeclaratorList() != null) {
            return extractNormalVariableElement(ctx);
        }
        
        return variables;
    }

    private List<VariableElement> extractVariablesFromClass(CPP14Parser.ClassSpecifierContext ctx) {
        if (ctx.memberSpecification() == null) {
            return variables;
        }
        for (CPP14Parser.MemberdeclarationContext memberCtx : ctx.memberSpecification().memberdeclaration()) {
            if (memberCtx.functionDefinition() != null) {
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
                String variableType = memberCtx.declSpecifierSeq().getText();
                Parent parent = new CppParentExtractor().getParent(memberCtx);
                List<String> varNames = extractVariableNames(memberCtx.memberDeclaratorList());
                String path = PathExtractor.extractPath(ctx);
                int startLine = memberCtx.getStart().getLine();
                int endLine = memberCtx.getStop().getLine();

                parseToVariablesList(varNames, path, variableType, parent, startLine, endLine);
            }
        }
        return variables;
    }
    private List<VariableElement> extractNormalVariableElement(CPP14Parser.SimpleDeclarationContext ctx) {
        String variableType = ctx.declSpecifierSeq().getText();
        Parent parent = new CppParentExtractor().getParent(ctx);
        List<String> varNames = extractVariableNames(ctx.initDeclaratorList());
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();
        parseToVariablesList(varNames, path, variableType, parent, startLine, endLine);
        return variables;
    }

    private List<String> extractVariableNames(CPP14Parser.InitDeclaratorListContext ctx) {
        List<String> varNames = new ArrayList<>();
        for (CPP14Parser.InitDeclaratorContext initDec : ctx.initDeclarator()) {
            // Skip if it is a function or Constructor
            if (initDec.declarator().pointerDeclarator().noPointerDeclarator() != null && initDec.declarator().pointerDeclarator().noPointerDeclarator().parametersAndQualifiers() != null) {
                continue;
            }
            varNames.add(initDec.declarator().getText());
        }
        return varNames;
    }

    private List<String> extractVariableNames(CPP14Parser.MemberDeclaratorListContext ctx) {
        List<String> varNames = new ArrayList<>();
        for (CPP14Parser.MemberDeclaratorContext memberDec : ctx.memberDeclarator()) {
            // Skip if it is a function or Constructor
            if (memberDec.declarator().pointerDeclarator().noPointerDeclarator() != null && memberDec.declarator().pointerDeclarator().noPointerDeclarator().parametersAndQualifiers() != null) {
                continue;
            }
            varNames.add(memberDec.declarator().getText());
        }
        return varNames;
    }

    private void parseToVariablesList(List<String> varNames, String path, String variableType, Parent parent, int startLine, int endLine) {
        for (String varName : varNames) {
            VariableElement variable = createVariableElement(varName, path, variableType, parent, startLine, endLine);
            variables.add(variable);
        }
    }

    private VariableElement createVariableElement(String varName, String path, String variableType, Parent parent, int startLine, int endLine) {
        VariableElement variable = new VariableElement(varName, path, variableType, parent);
        variable.setStartLine(startLine);
        variable.setEndLine(endLine);
        return variable;
    }

    private boolean isAssignment(CPP14Parser.SimpleDeclarationContext ctx) {
        return ctx.declSpecifierSeq() == null;
    }
}
