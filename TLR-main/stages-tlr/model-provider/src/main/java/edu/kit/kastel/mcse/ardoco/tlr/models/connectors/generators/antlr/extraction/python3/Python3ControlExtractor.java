package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.python3.Python3Parser;
import generated.antlr.python3.Python3ParserBaseVisitor;

public class Python3ControlExtractor extends Python3ParserBaseVisitor<List<BasicElement>> {
    private final List<BasicElement> controls = new ArrayList<>();

    @Override
    public List<BasicElement> visitFile_input(Python3Parser.File_inputContext ctx) {
        super.visitFile_input(ctx);
        return controls;
    }

    @Override
    public List<BasicElement> visitFuncdef(Python3Parser.FuncdefContext ctx) {
        String name = ctx.name().getText();
        String path = PathExtractor.extractPath(ctx);
        Parent parent = new Python3ParentExtractor().getParent(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        BasicElement BasicElement = new BasicElement(name, path, parent);
        BasicElement.setStartLine(startLine);
        BasicElement.setEndLine(endLine);
        controls.add(BasicElement);
        return controls;
    }

}
