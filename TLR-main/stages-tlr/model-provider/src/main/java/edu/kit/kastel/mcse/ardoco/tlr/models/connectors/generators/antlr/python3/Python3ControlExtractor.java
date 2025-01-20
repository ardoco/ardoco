package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.Python3Parser;
import generated.antlr.Python3ParserBaseVisitor;

public class Python3ControlExtractor extends Python3ParserBaseVisitor<List<ControlElement>> {
    private final List<ControlElement> controls = new ArrayList<>();

    @Override
    public List<ControlElement> visitFile_input(Python3Parser.File_inputContext ctx) {
        super.visitFile_input(ctx);
        return controls;
    }

    @Override
    public List<ControlElement> visitFuncdef(Python3Parser.FuncdefContext ctx) {
        String name = ctx.name().getText();
        Parent parent = Python3ParentExtractor.getParent(ctx);
        ControlElement controlElement = new ControlElement(name, parent);
        controls.add(controlElement);
        return controls;
    }
    
}
