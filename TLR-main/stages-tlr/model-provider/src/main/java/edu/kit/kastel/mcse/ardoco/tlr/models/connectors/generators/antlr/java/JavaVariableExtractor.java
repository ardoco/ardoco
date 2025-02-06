package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import generated.antlr.JavaParser;
import generated.antlr.JavaParserBaseVisitor;

public class JavaVariableExtractor extends JavaParserBaseVisitor<List<VariableElement>> {
    private final List<VariableElement> variables = new ArrayList<>();

    @Override 
    public List<VariableElement> visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        super.visitCompilationUnit(ctx);
        return variables;
    }

    @Override 
    public List<VariableElement> visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        String variableType = ctx.typeType().getText();
        Parent parent = JavaParentExtractor.getParent(ctx);
        List<String> varNames = extractVariableNames(ctx.variableDeclarators().variableDeclarator());
        String path = PathExtractor.extractPath(ctx);
        int fromLine = ctx.getStart().getLine();
        int toLine = ctx.getStop().getLine();
        parseToVariablesList(varNames, path, variableType, parent, fromLine, toLine);
        return variables;
    }

    @Override 
    public List<VariableElement> visitLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
        String variableType = ctx.typeType().getText();
        Parent parent = JavaParentExtractor.getParent(ctx);
        List<String> varNames = extractVariableNames(ctx.variableDeclarators().variableDeclarator());
        String path = PathExtractor.extractPath(ctx);
        int fromLine = ctx.getStart().getLine();
        int toLine = ctx.getStop().getLine();
        
        parseToVariablesList(varNames, path, variableType, parent, fromLine, toLine);
        return variables;
    }

    private List<String> extractVariableNames(List<JavaParser.VariableDeclaratorContext> variableDeclarators) {
        List<String> variableNames = new ArrayList<>();
        for (JavaParser.VariableDeclaratorContext variableDeclarator : variableDeclarators) {
            String name = variableDeclarator.variableDeclaratorId().identifier().getText();
            variableNames.add(name);
        }
        return variableNames;
    }

    private void parseToVariablesList(List<String> varNames, String path, String variableType, Parent parent, int fromLine, int toLine) {
        for (String variableName : varNames) {
            VariableElement variable = new VariableElement(variableName, path, variableType, parent);
            variable.setFromLine(fromLine);
            variable.setToLine(toLine);
            variables.add(variable);
        }
    }




}