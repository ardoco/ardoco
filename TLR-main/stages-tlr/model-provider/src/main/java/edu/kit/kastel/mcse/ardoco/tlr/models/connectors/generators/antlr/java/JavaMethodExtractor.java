package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParser;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParserBaseVisitor;

public class JavaMethodExtractor extends JavaParserBaseVisitor<CodeItem> {
    
        private CodeItemRepository codeItemRepository;
    
        public JavaMethodExtractor(CodeItemRepository codeItemRepository) {
            this.codeItemRepository = codeItemRepository;
        }
    
        @Override
        public CodeItem visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
            String name = ctx.identifier().getText();
            return new ControlElement(codeItemRepository, name);
        }
    
}
