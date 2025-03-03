package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction;

import org.antlr.v4.runtime.ParserRuleContext;

public final class PathExtractor {

    public static String extractPath(ParserRuleContext ctx) {
        return ctx.getStart().getInputStream().getSourceName();
    }

    public static String extractNameFromPath(ParserRuleContext ctx) {
        String path = extractPath(ctx);
        String normalizedPath = path.replace("\\", "/");
        String name = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1, normalizedPath.lastIndexOf("."));
        return name;
    }


    
}
