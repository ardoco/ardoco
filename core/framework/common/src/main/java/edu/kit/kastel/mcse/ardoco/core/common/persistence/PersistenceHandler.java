package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;

import java.util.SortedSet;

/**
 * Interface defining persistence operations.
 * Implemented by the neo4j-schema module.
 */
public interface PersistenceHandler {
    void saveModel(Metamodel metamodel, Model model);
    Model loadModel(Metamodel metamodel);
    SortedSet<Metamodel> getStoredMetamodels();

    void savePreprocessedText(Text text, String identifier);
    Text loadPreprocessedText(String identifier);
    boolean hasPreprocessedText(String identifier);

}
