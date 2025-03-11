package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

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
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.CppModelMapper;
import generated.antlr.cpp.CPP14Lexer;

public class CppExtractor extends ANTLRExtractor {

    public CppExtractor(CodeItemRepository repository, String path) {
        super(repository, path, ProgrammingLanguage.CPP);
        CppElementManager elementManager = new CppElementManager();
        setMapper(new CppModelMapper(repository, elementManager));
        setElementExtractor(new CppElementExtractor(elementManager));
        setCommentExtractor(new CppCommentExtractor(elementManager));
    }

    @Override
    protected List<Path> getFiles() {
        Path dir = Path.of(path);
        List<Path> cppFiles = new ArrayList<>();
        try {
            Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".cpp") || f.toString().endsWith(".h"))
                    .forEach(cppFiles::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cppFiles;
    }

    @Override 
    protected CommonTokenStream buildTokens(Path file) throws IOException{
        CharStream stream = CharStreams.fromPath(file);
        CPP14Lexer lexer = new CPP14Lexer(stream);
        return new CommonTokenStream(lexer);
    }
}
