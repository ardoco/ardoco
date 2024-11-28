package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParser;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParserBaseVisitor;

public class JavaPackageExtractor extends JavaParserBaseVisitor<CodeItem> {
    private CodeItemRepository codeItemRepository;

    public JavaPackageExtractor(CodeItemRepository codeItemRepository) {
        this.codeItemRepository = codeItemRepository;
    }

    @Override
    public CodeItem visitPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        String packageName = ctx.qualifiedName().getText();
        return new CodePackage(codeItemRepository, packageName);
    }
    
}
