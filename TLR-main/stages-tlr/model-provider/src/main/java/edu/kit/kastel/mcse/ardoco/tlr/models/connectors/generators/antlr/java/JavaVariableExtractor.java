package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import generated.antlr.JavaParser;
import generated.antlr.JavaParserBaseVisitor;

public class JavaVariableExtractor extends JavaParserBaseVisitor<VariableElement> {

    @Override
    public VariableElement visitVariableDeclarator(JavaParser.VariableDeclaratorContext ctx) {
        String variableName = ctx.variableDeclaratorId().identifier().getText();
        String variableType = ctx.variableInitializer().getText();
        Parent parent = JavaParentExtractor.getParent(ctx);
        return new VariableElement(variableName, variableType, parent);
    }
}