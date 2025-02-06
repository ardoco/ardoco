package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.JavaParser;
import generated.antlr.JavaParserBaseVisitor;

public class JavaControlExtractor extends JavaParserBaseVisitor<List<ControlElement>> {
    private final List<ControlElement> controls = new ArrayList<>(); 

        @Override 
        public List<ControlElement> visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
            super.visitCompilationUnit(ctx);
            return controls;
        }
        
        @Override
        public List<ControlElement> visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
            String name = ctx.identifier().getText();
            Parent parent = JavaParentExtractor.getParent(ctx);
            String path = PathExtractor.extractPath(ctx);
            int fromLine = ctx.getStart().getLine();
            int toLine = ctx.getStop().getLine();
            
            ControlElement controlElement = new ControlElement(name, path, parent);
            controlElement.setFromLine(fromLine);
            controlElement.setToLine(toLine);
            controls.add(controlElement);
            return controls;
        }    
}