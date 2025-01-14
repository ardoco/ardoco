package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CompilationUnitElement;
import generated.antlr.JavaParser;
import generated.antlr.JavaParserBaseVisitor;

public class JavaCompilationUnitExtractor extends JavaParserBaseVisitor<CompilationUnitElement> {
    private final String path;

    public JavaCompilationUnitExtractor(String path) {
        this.path = path;
    }

    @Override
    public CompilationUnitElement visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        String packageName = "";
        if (ctx.packageDeclaration() != null) {
            packageName = ctx.packageDeclaration().qualifiedName().getText();
        }
        String name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
        String cleansedPath = path.substring(0, path.lastIndexOf("/") + 1);
        return new CompilationUnitElement(name, cleansedPath, packageName);
    }
    
}
