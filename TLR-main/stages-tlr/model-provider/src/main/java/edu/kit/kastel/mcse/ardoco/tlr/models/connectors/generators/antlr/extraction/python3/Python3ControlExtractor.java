package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.python3.Python3Parser;
import generated.antlr.python3.Python3ParserBaseVisitor;

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
        String path = PathExtractor.extractPath(ctx);
        Parent parent = new Python3ParentExtractor().getParent(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        ControlElement controlElement = new ControlElement(name, path, parent);
        controlElement.setStartLine(startLine);
        controlElement.setEndLine(endLine);
        controls.add(controlElement);
        return controls;
    }
    
}
