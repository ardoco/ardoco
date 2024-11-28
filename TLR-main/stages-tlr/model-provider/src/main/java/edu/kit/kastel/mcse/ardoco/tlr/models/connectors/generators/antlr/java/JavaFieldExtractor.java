package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParser;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParserBaseVisitor;

public class JavaFieldExtractor extends JavaParserBaseVisitor<CodeItem> {
    private CodeItemRepository codeItemRepository;

    public JavaFieldExtractor(CodeItemRepository codeItemRepository) {
        this.codeItemRepository = codeItemRepository;
    }

    @Override
    public CodeItem visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        String fieldType = ctx.typeType().getText();
        SortedSet<CodeItem> vars = new TreeSet<>();
        for (JavaParser.VariableDeclaratorContext var : ctx.variableDeclarators().variableDeclarator()) {
            vars.add(new JavaVariableExtractor(codeItemRepository).visitVariableDeclarator(var));
        }
        return new ClassUnit(codeItemRepository, fieldType, vars);
    }
    
}
