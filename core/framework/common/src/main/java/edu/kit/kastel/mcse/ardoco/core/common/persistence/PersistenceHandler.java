package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

import java.util.Collection;
import java.util.Set;
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

    /**
     * Saves a collection of generic trace links.
     * The implementation should filter for supported types (e.g. ArchitectureCodeTraceLink).
     */
    void saveSamCodeTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks);

    /**
     * Loads the specific Architecture-Code links from the database.
     */
    Collection<ArchitectureCodeTraceLink> loadSamCodeTraceLinks();

}
