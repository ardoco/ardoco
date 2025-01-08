package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.JavaParser;
import generated.antlr.JavaParserBaseVisitor;

public class JavaControlExtractor extends JavaParserBaseVisitor<ControlElement> {
    
        @Override
        public ControlElement visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
            String name = ctx.identifier().getText();
            Parent parent = JavaParentExtractor.getParent(ctx);
            return new ControlElement(name, parent);
        }    
}