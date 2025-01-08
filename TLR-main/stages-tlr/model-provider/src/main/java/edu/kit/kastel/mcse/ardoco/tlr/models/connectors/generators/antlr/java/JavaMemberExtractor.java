package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import generated.antlr.JavaParser;
import generated.antlr.JavaParserBaseVisitor;

public class JavaMemberExtractor extends JavaParserBaseVisitor<CodeItem> {
    private CodeItemRepository codeItemRepository;

    
    public JavaMemberExtractor(CodeItemRepository codeItemRepository) {
        this.codeItemRepository = codeItemRepository;
    }

    @Override
    public CodeItem visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
        if (ctx.memberDeclaration() != null) {
            return visitMemberDeclaration(ctx.memberDeclaration());
        }
        return null;
    }

    @Override
    public CodeItem visitMemberDeclaration(JavaParser.MemberDeclarationContext ctx) {
        if (ctx.methodDeclaration() != null) {
            return new JavaMethodExtractor(codeItemRepository).visitMethodDeclaration(ctx.methodDeclaration());
        }
        if (ctx.fieldDeclaration() != null) {
            return new JavaFieldExtractor(codeItemRepository).visitFieldDeclaration(ctx.fieldDeclaration());
        }
        return null;
    }
}

