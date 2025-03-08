package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParserBaseVisitor;

public class JavaCompilationUnitExtractor extends JavaParserBaseVisitor<BasicElement> {

    @Override
    public BasicElement visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        Parent parent;
        String path = PathExtractor.extractPath(ctx);
        String name = PathExtractor.extractNameFromPath(ctx);

        if (ctx.packageDeclaration() != null) {
            String packageName = ctx.packageDeclaration().qualifiedName().getText();
            String packagePath = path.substring(0, path.lastIndexOf("/") + 1);
            parent = new Parent(packageName, packagePath, BasicType.PACKAGE);
            return new BasicElement(name, path, parent);
        }
        return new BasicElement(name, path);

    }

}
