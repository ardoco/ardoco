package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.management.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.CommentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.CppModelMapper;

public class CppExtractor extends ANTLRExtractor {

    public CppExtractor(CodeItemRepository repository, String path) {
        super(repository, path, ProgrammingLanguage.CPP);
        CppElementManager elementManager = new CppElementManager();
        this.elementManager = elementManager;
        this.mapper = new CppModelMapper(repository, elementManager);
        this.elementExtractor = new CppElementExtractor(elementManager);
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
    protected CommentExtractor createCommentExtractor(CommonTokenStream tokens, String path) {
        return new CppCommentExtractor(tokens, path);
    }

    // For testing purposes
    public CppElementManager getElementManager() {
        return (CppElementManager) elementExtractor.getElements();
    }

}
