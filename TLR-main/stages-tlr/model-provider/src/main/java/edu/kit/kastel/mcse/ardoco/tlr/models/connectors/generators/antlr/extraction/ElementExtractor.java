package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction;

import org.antlr.v4.runtime.CommonTokenStream;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.management.ElementManager;

public interface ElementExtractor {

    public void extract(CommonTokenStream tokens);

    public ElementManager getElements(); 
}
