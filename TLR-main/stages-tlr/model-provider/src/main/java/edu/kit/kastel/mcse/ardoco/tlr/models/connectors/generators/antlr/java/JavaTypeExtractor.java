package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import generated.antlr.JavaParser;
import generated.antlr.JavaParserBaseVisitor;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.Datatype;

import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.InterfaceUnit;


public class JavaTypeExtractor extends JavaParserBaseVisitor<Datatype> {

    private CodeItemRepository codeItemRepository;

    public JavaTypeExtractor(CodeItemRepository codeItemRepository) {
        this.codeItemRepository = codeItemRepository;
    }

    @Override 
    public Datatype visitTypeDeclaration(JavaParser.TypeDeclarationContext ctx) {
        if (ctx.classDeclaration() != null) {
            return visitClassDeclaration(ctx.classDeclaration());
        } 
        if (ctx.interfaceDeclaration() != null) {
            return visitInterfaceDeclaration(ctx.interfaceDeclaration());
        } 
        if (ctx.enumDeclaration() != null) {
            // Do nothing
        }
        if (ctx.annotationTypeDeclaration() != null) {
            //Do nothing
        } 
        if (ctx.recordDeclaration() != null) {
            //Do nothing
        }
        return null;
    }

    @Override
    public Datatype visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        String name = ctx.identifier().getText();
        SortedSet<? extends CodeItem> content = extractContent(ctx);
        return new ClassUnit(codeItemRepository, name, content);
    }

    @Override
    public Datatype visitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
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
