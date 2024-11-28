package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.CodeExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaParser;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.generated.sources.JavaLexer;

public class JavaExtractor extends CodeExtractor {

    public JavaExtractor(CodeItemRepository codeItemRepository, String path) {
        super(codeItemRepository, path);
    }

    @Override
    public CodeModel extractModel() {

        try{
            CharStream input = CharStreams.fromFileName(path);

            JavaLexer lexer = new JavaLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            JavaParser parser = new JavaParser(tokens);
            ParseTree tree = parser.compilationUnit();

            CodeItem codeItem = null;
            if (tree instanceof JavaParser.CompilationUnitContext ctx) {
                if (ctx.moduleDeclaration() != null) {
                    JavaModuleExtractor moduleExtractor = new JavaModuleExtractor(codeItemRepository);
                    codeItem = moduleExtractor.visitModuleDeclaration(ctx.moduleDeclaration());
                } else {
                    JavaCompilationUnitExtractor compilationUnitExtractor = new JavaCompilationUnitExtractor(codeItemRepository);
                    codeItem = compilationUnitExtractor.visitCompilationUnit(ctx);
                }
            }
            return wrapInModel(codeItem);

            } catch (IOException e) {
            e.printStackTrace();
            return null;
            }
    }

    private CodeModel wrapInModel(CodeItem codeItem) {
        SortedSet<CodeItem> codeItems = new TreeSet<>();
        codeItems.add(codeItem);
        CodeModel model = new CodeModel(codeItemRepository, codeItems);
        return model;
    }
}
