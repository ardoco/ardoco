package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;

public abstract class ParentExtractor {

    public ElementIdentifier getParent(ParserRuleContext ctx) {
        ParserRuleContext parentCtx = ctx.getParent();
        boolean foundValidParent = false;
        while (parentCtx != null && !foundValidParent) {
            String path = PathExtractor.extractPath(parentCtx);

            foundValidParent = isValidParent(parentCtx, path);
            if (foundValidParent) {
                return buildParent(parentCtx, path);
            } else {
                parentCtx = parentCtx.getParent(); // Continue traversing upwards
            }
        }
        return null;
    }

    protected abstract boolean isValidParent(ParserRuleContext parentCtx, String path);

    protected abstract ElementIdentifier buildParent(ParserRuleContext parentCtx, String path);

}
