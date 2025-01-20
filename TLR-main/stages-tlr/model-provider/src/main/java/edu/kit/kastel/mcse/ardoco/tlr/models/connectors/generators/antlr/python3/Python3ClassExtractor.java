package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.Python3Parser;

import generated.antlr.Python3ParserBaseVisitor;

public class Python3ClassExtractor extends Python3ParserBaseVisitor<List<Python3ClassElement>> {
    private final List<Python3ClassElement> classes = new ArrayList<>();
    private final String fileName;

    public Python3ClassExtractor(String filePath) {
        String potentialFileName = filePath;
        if (potentialFileName.contains("/")) {
            potentialFileName = potentialFileName.substring(potentialFileName.lastIndexOf("/") + 1, potentialFileName.length());
        } 
        if (potentialFileName.contains(".")) {
            potentialFileName = potentialFileName.substring(0, potentialFileName.lastIndexOf("."));
        }
        this.fileName = potentialFileName;
    }



    @Override
    public List<Python3ClassElement> visitFile_input(Python3Parser.File_inputContext ctx) {
        super.visitFile_input(ctx);
        return classes;
    }

    @Override
    public List<Python3ClassElement> visitClassdef(Python3Parser.ClassdefContext ctx) {
        String name = ctx.name().getText();
        Parent parent = Python3ParentExtractor.getParent(ctx, this.fileName);
        List<String> childClassOf = getParentClasses(ctx);
        Python3ClassElement Python3ClassElement = new Python3ClassElement(name, parent, childClassOf);
        classes.add(Python3ClassElement);
        super.visitClassdef(ctx);
        return classes;
    }

    private List<String> getParentClasses(Python3Parser.ClassdefContext ctx) {
        List<String> parentClasses = new ArrayList<>();
        if (ctx.arglist() != null) {
            for (Python3Parser.ArgumentContext arg : ctx.arglist().argument()) {
                parentClasses.add(arg.getText());
            }
        }
        return parentClasses;

    }
    
}
