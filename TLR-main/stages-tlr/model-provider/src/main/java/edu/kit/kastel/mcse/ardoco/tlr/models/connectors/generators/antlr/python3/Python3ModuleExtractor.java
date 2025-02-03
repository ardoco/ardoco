package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3ModuleElement;
import generated.antlr.Python3Parser;
import generated.antlr.Python3ParserBaseVisitor;

public class Python3ModuleExtractor extends Python3ParserBaseVisitor<List<Python3ModuleElement>> {
    private final String dirPath;
    private final List<Python3ModuleElement> modules;

    public Python3ModuleExtractor(String dirPath) {
        this.dirPath = dirPath;
        this.modules = new ArrayList<>();
    }

    public List<Python3ModuleElement> visitFile_input(Python3Parser.File_inputContext ctx) {
        Token token = ctx.getStart();
        String filePath = token.getTokenSource().getSourceName();
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.'));
        String packagePath = filePath.substring(0, filePath.lastIndexOf('/') + 1);
        String packageName = getPackageName(filePath, packagePath);
        Python3ModuleElement module = new Python3ModuleElement(fileName, packagePath, packageName);
        modules.add(module);
        super.visitFile_input(ctx);
        return modules;
    }    

    private String getPackageName(String filePath, String packagePath) {
        // base package is source directory of the file
        String basePackagePath = this.dirPath.substring(0, this.dirPath.lastIndexOf('/', this.dirPath.lastIndexOf('/') - 1));
        // return package name relative to the base package
        return packagePath.substring(basePackagePath.length() + 1, packagePath.length() - 1);
    }
}
