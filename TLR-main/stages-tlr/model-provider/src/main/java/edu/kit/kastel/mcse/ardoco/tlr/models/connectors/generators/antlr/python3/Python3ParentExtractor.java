package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.Python3Parser;

public final class Python3ParentExtractor {
    public static Parent getParent(ParserRuleContext ctx, String fileName) {
        ParserRuleContext parentCtx = ctx.getParent();
        while (parentCtx != null) {
            if (parentCtx instanceof Python3Parser.ClassdefContext) {
                Python3Parser.ClassdefContext classCtx = (Python3Parser.ClassdefContext) parentCtx;
                return buildParentFromClassContext(classCtx, BasicType.CLASS);
            } else if (parentCtx instanceof Python3Parser.FuncdefContext) {
                Python3Parser.FuncdefContext funcCtx = (Python3Parser.FuncdefContext) parentCtx;
                return buildParentFromFuncContext(funcCtx, BasicType.CONTROL);
            } else if (parentCtx instanceof Python3Parser.File_inputContext) {
                return buildParentFromModuleContext(fileName, BasicType.MODULE);
            } else {
                parentCtx = parentCtx.getParent();
            }
        }
        return null;
    }

    private static Parent buildParentFromClassContext(Python3Parser.ClassdefContext ctx, BasicType type) {
        String name = ctx.name().getText();
        return new Parent(name, type);
    }

    private static Parent buildParentFromFuncContext(Python3Parser.FuncdefContext ctx, BasicType type) {
        String name = ctx.name().getText();
        return new Parent(name, type);
    }

    private static Parent buildParentFromModuleContext(String fileName, BasicType type) {
        return new Parent(fileName, type);
    }
}
