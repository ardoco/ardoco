package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.management.JavaElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.CommentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaModelMapper;

public class JavaExtractor extends ANTLRExtractor {

    public JavaExtractor(CodeItemRepository repository, String path) {
        super(repository, path, ProgrammingLanguage.JAVA);
        JavaElementManager elementManager = new JavaElementManager();
        this.elementManager = elementManager;
        this.mapper = new JavaModelMapper(repository, elementManager);
        this.elementExtractor = new JavaElementExtractor(elementManager);
    }

    @Override
    protected List<Path> getFiles() {
        Path dir = Path.of(path);
        List<Path> javaFiles = new ArrayList<>();
        try {
            Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".java"))
                    .forEach(javaFiles::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return javaFiles;
    }

    @Override
    protected CommentExtractor createCommentExtractor(CommonTokenStream tokens, String path) {
        return new JavaCommentExtractor(tokens, path);
    }

    // For testing purposes
    public JavaElementManager getElementManager() {
        return (JavaElementManager) elementExtractor.getElements();
    }
}
