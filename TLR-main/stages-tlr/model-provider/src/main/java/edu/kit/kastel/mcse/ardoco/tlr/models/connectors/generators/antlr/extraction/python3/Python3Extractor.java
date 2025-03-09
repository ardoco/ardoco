package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.Python3ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.Python3ModelMapper;
import generated.antlr.python3.Python3Lexer;

public class Python3Extractor extends ANTLRExtractor {

    public Python3Extractor(CodeItemRepository repository, String path) {
        super(repository, path, ProgrammingLanguage.PYTHON3);
        Python3ElementManager elementManager = new Python3ElementManager();
        this.elementManager = elementManager;
        this.mapper = new Python3ModelMapper(repository, elementManager);
        this.elementExtractor = new Python3ElementExtractor(elementManager);
        this.commentExtractor = new Python3CommentExtractor(elementManager);
    }

    @Override
    protected List<Path> getFiles() {
        Path dir = Path.of(path);
        List<Path> pythonFiles = new ArrayList<>();
        try {
            Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".py"))
                    .forEach(pythonFiles::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pythonFiles;
    }

    @Override 
    protected CommonTokenStream buildTokens(Path file) throws IOException{
        CharStream stream = CharStreams.fromPath(file);
        Python3Lexer lexer = new Python3Lexer(stream);
        return new CommonTokenStream(lexer);
    }

    // For testing purposes
    public Python3ElementManager getElementManager() {
        return (Python3ElementManager) elementExtractor.getElements();
    }

}
