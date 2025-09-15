/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.models.antlr4.cpp;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("java:S100")
public abstract class CPP14ParserBase extends Parser {
    private static final Logger logger = LoggerFactory.getLogger(CPP14ParserBase.class);

    protected CPP14ParserBase(TokenStream input) {
        super(input);
    }

    protected boolean IsPureSpecifierAllowed() {
        try {
            ParserRuleContext memberDeclarator = this._ctx; // memberDeclarator
            ParseTree declarator = memberDeclarator.getChild(0).getChild(0);
            ParseTree declaratorChild = declarator.getChild(0);
            ParseTree parametersAndQualifiers = declaratorChild.getChild(1);
            if (parametersAndQualifiers == null)
                return false;
            return (parametersAndQualifiers instanceof CPP14Parser.ParametersAndQualifiersContext);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }
}
