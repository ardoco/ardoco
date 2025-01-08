package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import generated.antlr.JavaParserBaseVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import generated.antlr.JavaParser;

public class JavaCompilationUnitExtractor extends JavaParserBaseVisitor<CodeItem> {
    private CodeItemRepository codeItemRepository;

    public JavaCompilationUnitExtractor(CodeItemRepository codeItemRepository) {
        this.codeItemRepository = codeItemRepository;
    }
    @Override
    public CodeItem visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        String name = extractFileName(ctx);
        SortedSet<? extends CodeItem> content = extractContent(ctx);
        List<String> pathElements = extractPathElements(ctx);
        String extension = "java";
        ProgrammingLanguage language = ProgrammingLanguage.JAVA;
        return new CodeCompilationUnit(codeItemRepository, name, content, pathElements, extension, language);
    }

    private String extractFileName(JavaParser.CompilationUnitContext ctx) {
        String fileName = "UnnamedCompilationUnit";
        if (ctx.moduleDeclaration() != null) {
            fileName = ctx.moduleDeclaration().qualifiedName().getText();
        } else if (ctx.packageDeclaration() != null) {
            fileName = ctx.packageDeclaration().qualifiedName().getText();
        }
        return fileName;
    }

    private SortedSet<? extends CodeItem> extractContent(JavaParser.CompilationUnitContext ctx) {
        SortedSet<CodeItem> content = new TreeSet<>();
        for (JavaParser.TypeDeclarationContext typeDeclarationContext : ctx.typeDeclaration()) {
            CodeItem codeItem = new JavaTypeExtractor(codeItemRepository).visitTypeDeclaration(typeDeclarationContext);
            if (codeItem != null) {
                content.add(codeItem);
            }
        }
        return content;
    }

    private List<String> extractPathElements(JavaParser.CompilationUnitContext ctx) {
        List<String> pathElements = new ArrayList<>();
        if(ctx.packageDeclaration() != null) {
            String packageName = ctx.packageDeclaration().qualifiedName().getText();
            pathElements.addAll(List.of(packageName.split("\\.")));
        }
        return pathElements;
    }
}
