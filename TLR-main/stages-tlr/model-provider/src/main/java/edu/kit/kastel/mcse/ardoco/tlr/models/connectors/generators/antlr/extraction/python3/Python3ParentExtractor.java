package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ParentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.python3.Python3Parser;

public final class Python3ParentExtractor extends ParentExtractor {

    @Override
    protected boolean isValidParent(ParserRuleContext parentCtx, String path) {
        return parentCtx instanceof Python3Parser.ClassdefContext
                || parentCtx instanceof Python3Parser.FuncdefContext
                || parentCtx instanceof Python3Parser.File_inputContext;
    }

    @Override
    protected ElementIdentifier buildParent(ParserRuleContext parentCtx, String path) {
        if (parentCtx instanceof Python3Parser.ClassdefContext) {
            return buildParentFromClassContext((Python3Parser.ClassdefContext) parentCtx, path, Type.CLASS);
        } else if (parentCtx instanceof Python3Parser.FuncdefContext) {
            return buildParentFromFuncContext((Python3Parser.FuncdefContext) parentCtx, path, Type.FUNCTION);
        } else if (parentCtx instanceof Python3Parser.File_inputContext) {
            return buildParentFromModuleContext((Python3Parser.File_inputContext) parentCtx, path, Type.MODULE);
        }
        return null;
    }

    private static ElementIdentifier buildParentFromClassContext(Python3Parser.ClassdefContext ctx, String path, Type type) {
        String name = ctx.name().getText();
        return new ElementIdentifier(name, path, type);
    }

    private static ElementIdentifier buildParentFromFuncContext(Python3Parser.FuncdefContext ctx, String path, Type type) {
        String name = ctx.name().getText();
        return new ElementIdentifier(name, path, type);
    }

    private static ElementIdentifier buildParentFromModuleContext(Python3Parser.File_inputContext ctx, String path,
            Type type) {
        String name = PathExtractor.extractNameFromPath(ctx);
        return new ElementIdentifier(name, path, type);
    }
}
