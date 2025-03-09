package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction;

import java.nio.file.Path;

import org.antlr.v4.runtime.CommonTokenStream;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.management.ElementManager;

public interface ElementExtractor {

    public void extract(Path file);

    public CommonTokenStream getTokens();

    public ElementManager getElements(); 
}
