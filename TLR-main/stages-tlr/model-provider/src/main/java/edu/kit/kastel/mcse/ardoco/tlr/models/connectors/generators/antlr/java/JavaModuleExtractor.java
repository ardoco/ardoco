package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParser;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParserBaseVisitor;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModule;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ComputationalObject;

public class JavaModuleExtractor extends JavaParserBaseVisitor<CodeItem> {
    private CodeItemRepository codeItemRepository;

    public JavaModuleExtractor(CodeItemRepository codeItemRepository) {
        this.codeItemRepository = codeItemRepository;
    }

    @Override
    public CodeItem visitModuleDeclaration(JavaParser.ModuleDeclarationContext ctx) {
        String moduleName = ctx.qualifiedName().getText();
        SortedSet<CodeItem> content = extractContent(ctx.moduleBody());
        return new CodeModule(codeItemRepository, moduleName, content);
    }

    private SortedSet<CodeItem> extractContent(JavaParser.ModuleBodyContext ctx) {
        SortedSet<CodeItem> content = new TreeSet<>();
        
        if (ctx.moduleDirective() != null) {
            for (JavaParser.ModuleDirectiveContext moduleDirectiveContext : ctx.moduleDirective()) {
                CodeItem directive = visitModuleDirective(moduleDirectiveContext);
                if (directive != null) {
                    content.add(directive);
                }
            }
        }
        return content;
    }

    @Override
    public CodeItem visitModuleDirective(JavaParser.ModuleDirectiveContext ctx) {
        if (ctx.REQUIRES() != null) {
            return visitRequiresModifier(ctx);
        }
        if (ctx.EXPORTS() != null) {
            return visitExportsModifier(ctx);
        }
        if (ctx.OPENS() != null) {
            return visitOpensModifier(ctx);
        }
        if (ctx.USES() != null) {
            return visitUsesModifier(ctx);
        }
        if (ctx.PROVIDES() != null) {
            return visitProvidesModifier(ctx);
        }
        return null;
    }

    private CodeItem visitRequiresModifier(JavaParser.ModuleDirectiveContext ctx) {
        String requiredModule = extractModuleNames(ctx, "requires");
        return new ComputationalObject(codeItemRepository, requiredModule);
    }

    private CodeItem visitExportsModifier(JavaParser.ModuleDirectiveContext ctx) {
        String exportedPackage = extractModuleNames(ctx, "exports");
        return new ComputationalObject(codeItemRepository, exportedPackage);
    }

    private CodeItem visitOpensModifier(JavaParser.ModuleDirectiveContext ctx) {
        String openedPackage = extractModuleNames(ctx, "opens");
        return new ComputationalObject(codeItemRepository, openedPackage);
    }

    private CodeItem visitUsesModifier(JavaParser.ModuleDirectiveContext ctx) {
        String usedService = extractModuleNames(ctx, "uses");
        return new ComputationalObject(codeItemRepository, usedService);
    }

    private CodeItem visitProvidesModifier(JavaParser.ModuleDirectiveContext ctx) {
        String providedService = extractModuleNames(ctx, "provides");
        return new ComputationalObject(codeItemRepository, providedService);
    }

    private String extractModuleNames(JavaParser.ModuleDirectiveContext ctx, String directive) {
        String extractedNames = directive + " ";
        for (JavaParser.QualifiedNameContext names : ctx.qualifiedName()) {
            extractedNames += names.getText() + " ";
        }
        extractedNames = extractedNames.substring(0, extractedNames.length() - 1);
        return extractedNames;
    }
    
}
