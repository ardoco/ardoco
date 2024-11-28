package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.Datatype;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParser;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParserBaseVisitor;

public class JavaVariableExtractor extends JavaParserBaseVisitor<CodeItem> {

    private CodeItemRepository codeItemRepository;

    public JavaVariableExtractor(CodeItemRepository codeItemRepository) {
        this.codeItemRepository = codeItemRepository;
    }

    @Override
    public CodeItem visitVariableDeclarator(JavaParser.VariableDeclaratorContext ctx) {
        String variableName = ctx.variableDeclaratorId().identifier().getText();
        return new Datatype(codeItemRepository, variableName);
    }

}