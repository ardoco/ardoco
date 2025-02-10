package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.PathExtractor;
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
        String path = PathExtractor.extractPath(ctx);
        String fileName = PathExtractor.extractNameFromPath(ctx);
        String packageName = getPackageName(path, fileName);
        Python3ModuleElement module = new Python3ModuleElement(fileName, path, packageName);
        modules.add(module);
        super.visitFile_input(ctx);
        return modules;
    }    

    private String getPackageName(String path, String fileName) {
        String packagePath = path.substring(0, path.lastIndexOf(fileName));

        // base package is source directory of the file
        String basePackageName = this.dirPath.substring(0, this.dirPath.lastIndexOf('/', this.dirPath.lastIndexOf('/') - 1));

        // build package name relative to the base package
        String packageName = packagePath.substring(basePackageName.length() + 1, packagePath.length() - 1);

        return packageName;
    }
}
