package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.Python3Parser;

import generated.antlr.Python3ParserBaseVisitor;

public class Python3ClassExtractor extends Python3ParserBaseVisitor<List<Python3ClassElement>> {
    private final List<Python3ClassElement> classes = new ArrayList<>();

    @Override
    public List<Python3ClassElement> visitFile_input(Python3Parser.File_inputContext ctx) {
        super.visitFile_input(ctx);
        return classes;
    }

    @Override
    public List<Python3ClassElement> visitClassdef(Python3Parser.ClassdefContext ctx) {
        String name = ctx.name().getText();
        String path = PathExtractor.extractPath(ctx);
        List<String> childClassOf = getParentClasses(ctx);
        Parent parent = Python3ParentExtractor.getParent(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        Python3ClassElement python3ClassElement = new Python3ClassElement(name, path, parent, childClassOf);
        python3ClassElement.setStartLine(startLine);
        python3ClassElement.setEndLine(endLine);
        classes.add(python3ClassElement);
        traverseInnerClasses(ctx);
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

    private void traverseInnerClasses(Python3Parser.ClassdefContext ctx) {
        if (ctx.children != null) {
            for (var child : ctx.children) {
                if (child instanceof Python3Parser.BlockContext) {
                    visitBlock(ctx.block());
                }
            }
        }
    }

    @Override
    public List<Python3ClassElement> visitBlock(Python3Parser.BlockContext ctx) {
        if (ctx.children != null) {
            for (var child : ctx.children) {
                if (child instanceof Python3Parser.StmtContext && ((Python3Parser.StmtContext) child).compound_stmt() != null) {
                    visitCompound_stmt(((Python3Parser.StmtContext) child).compound_stmt());

                }
            }
        }
        return classes;
    }

    @Override
    public List<Python3ClassElement> visitCompound_stmt(Python3Parser.Compound_stmtContext ctx) {
        if (ctx.children != null) {
            for (var child : ctx.children) {
                if (child instanceof Python3Parser.ClassdefContext) {
                    visitClassdef((Python3Parser.ClassdefContext) child);
                } else if (child instanceof Python3Parser.DecoratedContext) {
                    visitDecorated((Python3Parser.DecoratedContext) child);
                }
            }
        }
        return classes;
    }

    @Override
    public List<Python3ClassElement> visitDecorated(Python3Parser.DecoratedContext ctx) {
        if (ctx.children != null) {
            for (var child : ctx.children) {
                if (child instanceof Python3Parser.ClassdefContext) {
                    visitClassdef((Python3Parser.ClassdefContext) child);
                }
            }
        }
        return classes;
    }
    
}
