package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CompilationUnitElement;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParserBaseVisitor;

public class JavaCompilationUnitExtractor extends JavaParserBaseVisitor<CompilationUnitElement> {

    @Override
    public CompilationUnitElement visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        String packageName = "";
        if (ctx.packageDeclaration() != null) {
            packageName = ctx.packageDeclaration().qualifiedName().getText();
        }
        String path = PathExtractor.extractPath(ctx);
        String name = PathExtractor.extractNameFromPath(ctx);
        return new CompilationUnitElement(name, path, packageName);
    }
    
}
