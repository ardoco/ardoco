package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaLexer;

public class ANTLRExtractor extends CodeExtractor {
    private CodeModel extractedModel;
    SortedSet <CodeItem> modelContent;

    public ANTLRExtractor(CodeItemRepository codeItemRepository, String path) {
        super(codeItemRepository, path);
        extractedModel = null;
    }

    @Override
    public synchronized CodeModel extractModel() {
        if (extractedModel == null) {
            Path directoryPath = Path.of(path);
            SortedSet<CodeItem> modelContent = parseDirectory(directoryPath);
            extractedModel = new CodeModel(codeItemRepository, modelContent);
        }
        return this.extractedModel;
    }

    private SortedSet<CodeItem> parseDirectory(Path dir) {
        final SortedSet<CodeItem> items = new TreeSet<>();
        String[] javaFiles = getEntries(dir, ".java");

        for (String filePath : javaFiles) {
            try {
                Path file = Path.of(filePath);
                CodeItem item = parseFile(file, dir);
                items.add(item);
            } catch (IOException e) {
                throw new IllegalStateException("Error while parsing file: " + filePath, e);
            }
        }
        return items;
    }

    private CodeItem parseFile(Path file, Path dir) throws IOException {
        CharStream input = CharStreams.fromFileName(file.toString());
        
        JavaLexer lexer = new JavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        
        ParseTree tree = parser.compilationUnit();

        //JavaVisitorManager visitor = new JavaVisitorManager(codeItemRepository, dir, file);
       // visitor.visit(tree);

        //return visitor.getCodeItem();
        return null;
    }

    private static String[] getEntries(Path dir, String suffix) {
        try (Stream<Path> paths = Files.walk(dir)) {
            return paths.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(suffix))
                    .map(Path::toAbsolutePath)
                    .map(Path::normalize)
                    .map(Path::toString)
                    .toArray(String[]::new);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading directory: " + dir, e);
        }
    }
}
