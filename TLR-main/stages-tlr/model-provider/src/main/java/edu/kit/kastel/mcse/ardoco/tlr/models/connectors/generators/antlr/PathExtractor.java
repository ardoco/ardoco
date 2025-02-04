package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr;

import org.antlr.v4.runtime.ParserRuleContext;

public final class PathExtractor {

    public static String extractRawPath(ParserRuleContext ctx) {
        return ctx.getStart().getInputStream().getSourceName();
    }
    
    public static String extractPath(ParserRuleContext ctx) {
        String path = extractRawPath(ctx);    
        String cleansedPath = path.substring(0, path.lastIndexOf("/") + 1);
        return cleansedPath;
    } 

    public static String extractNameFromPath(ParserRuleContext ctx) {
        String path = extractRawPath(ctx);
        String name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
        return name;
    }


    
}
