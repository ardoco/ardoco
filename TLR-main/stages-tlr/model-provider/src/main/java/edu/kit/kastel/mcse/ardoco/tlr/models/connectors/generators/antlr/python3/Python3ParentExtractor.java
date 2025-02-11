package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.python3.Python3Parser;

public final class Python3ParentExtractor {
    public static Parent getParent(ParserRuleContext ctx) {
        ParserRuleContext parentCtx = ctx.getParent();
        while (parentCtx != null) {
            String path = PathExtractor.extractPath(parentCtx);
            if (parentCtx instanceof Python3Parser.ClassdefContext) {
                Python3Parser.ClassdefContext classCtx = (Python3Parser.ClassdefContext) parentCtx;
                return buildParentFromClassContext(classCtx, path, BasicType.CLASS);
            } else if (parentCtx instanceof Python3Parser.FuncdefContext) {
                Python3Parser.FuncdefContext funcCtx = (Python3Parser.FuncdefContext) parentCtx;
                return buildParentFromFuncContext(funcCtx, path, BasicType.CONTROL);
            } else if (parentCtx instanceof Python3Parser.File_inputContext) {
                Python3Parser.File_inputContext moduleCtx = (Python3Parser.File_inputContext) parentCtx;
                return buildParentFromModuleContext(moduleCtx, path, BasicType.MODULE);
            } else {
                parentCtx = parentCtx.getParent();
            }
        }
        return null;
    }

    private static Parent buildParentFromClassContext(Python3Parser.ClassdefContext ctx, String path, BasicType type) {
        String name = ctx.name().getText();
        return new Parent(name, path, type);
    }

    private static Parent buildParentFromFuncContext(Python3Parser.FuncdefContext ctx, String path, BasicType type) {
        String name = ctx.name().getText();
        return new Parent(name, path, type);
    }

    private static Parent buildParentFromModuleContext(Python3Parser.File_inputContext ctx, String path, BasicType type) {
        String name = PathExtractor.extractNameFromPath(ctx);
        return new Parent(name, path, type);
    }
}
