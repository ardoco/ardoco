package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParser;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParserBaseVisitor;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;

import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.InterfaceUnit;


public class JavaTypeExtractor extends JavaParserBaseVisitor<CodeItem> {

    private CodeItemRepository codeItemRepository;

    public JavaTypeExtractor(CodeItemRepository codeItemRepository) {
        this.codeItemRepository = codeItemRepository;
    }

    @Override 
    public CodeItem visitTypeDeclaration(JavaParser.TypeDeclarationContext ctx) {
        if (ctx.classDeclaration() != null) {
            return visitClassDeclaration(ctx.classDeclaration());
        } 
        if (ctx.interfaceDeclaration() != null) {
            return visitInterfaceDeclaration(ctx.interfaceDeclaration());
        } 
        if (ctx.enumDeclaration() != null) {
        }
        if (ctx.annotationTypeDeclaration() != null) {
        } 
        if (ctx.recordDeclaration() != null) {
        }
        return null;
    }

    @Override
    public CodeItem visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        String name = ctx.identifier().getText();
        SortedSet<? extends CodeItem> content = extractContent(ctx);
        return new ClassUnit(codeItemRepository, name, content);
    }

    @Override
    public CodeItem visitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
        String name = ctx.identifier().getText();
        SortedSet<? extends CodeItem> content = extractContent(ctx);
        return new InterfaceUnit(codeItemRepository, name, content);
    }

    private SortedSet<? extends CodeItem> extractContent(JavaParser.ClassDeclarationContext ctx) {
        SortedSet<CodeItem> content = new TreeSet<>();
        for (JavaParser.ClassBodyDeclarationContext classBodyDeclarationContext : ctx.classBody().classBodyDeclaration()) {
            CodeItem codeItem = new JavaMemberExtractor(codeItemRepository).visitClassBodyDeclaration(classBodyDeclarationContext);
            if (codeItem != null) {
                content.add(codeItem);
            }
        }
        return content;
    }

    private SortedSet<? extends CodeItem> extractContent(JavaParser.InterfaceDeclarationContext ctx) {
        SortedSet<CodeItem> content = new TreeSet<>();
        for (JavaParser.InterfaceBodyDeclarationContext interfaceBodyDeclarationContext : ctx.interfaceBody().interfaceBodyDeclaration()) {
            CodeItem codeItem = new JavaMemberExtractor(codeItemRepository).visitInterfaceBodyDeclaration(interfaceBodyDeclarationContext);
            if (codeItem != null) {
                content.add(codeItem);
            }
        }
        return content;
    }
    
}
