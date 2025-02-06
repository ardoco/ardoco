package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr;

import org.antlr.v4.runtime.ParserRuleContext;

public final class PathExtractor {

    public static String extractPath(ParserRuleContext ctx) {
        return ctx.getStart().getInputStream().getSourceName();
    }

    public static String extractNameFromPath(ParserRuleContext ctx) {
        String path = extractPath(ctx);
        String name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
        return name;
    }


    
}
