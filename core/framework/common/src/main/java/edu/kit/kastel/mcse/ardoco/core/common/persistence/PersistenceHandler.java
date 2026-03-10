/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.util.Collection;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

/**
 * Interface defining persistence operations.
 * Implemented by the neo4j-schema module.
 */
public interface PersistenceHandler {

    /**
     * Saves the given model for the given metamodel.
     *
     * @param metamodel the metamodel
     * @param model     the model to save
     */
    void saveModel(Metamodel metamodel, Model model);

    /**
     * Loads the model for the given metamodel from persistence.
     *
     * @param metamodel the metamodel
     * @return the loaded model
     */
    Model loadModel(Metamodel metamodel);

    /**
     * Retrieves the set of metamodels for which a model is stored in persistence.
     *
     * @return the set of metamodels with stored models
     */
    SortedSet<Metamodel> getStoredMetamodels();

    /**
     * Saves the preprocessed text for the given identifier.
     *
     * @param text      the preprocessed text to save
     * @param identifier the identifier for the preprocessed text which can also be used to load it later
     */
    void savePreprocessedText(Text text, String identifier);

    /**
     * Loads the preprocessed text for the given identifier from persistence.
     *
     * @param identifier the identifier for the preprocessed text
     * @return the loaded preprocessed text
     */
    Text loadPreprocessedText(String identifier);

    /**
     * Checks if a preprocessed text for the given identifier is stored in persistence.
     *
     * @param identifier the identifier for the preprocessed text
     * @return true if a preprocessed text for the given identifier is stored, false otherwise
     */
    boolean hasPreprocessedText(String identifier);

    /**
     * Saves a collection of generic trace links.
     * The implementation should filter for supported types (e.g. ArchitectureCodeTraceLink).
     *
     * @param traceLinks the trace links to save
     */
    boolean saveSamCodeTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks);

    /**
     * Loads the specific Architecture-Code links from the database.
     *
     * @return the loaded Architecture-Code tracelinks
     */
    Collection<ArchitectureCodeTraceLink> loadSamCodeTraceLinks();

    /**
     * Saves a collection of transitive tracelinks between sentences and model entities.
     *
     * @param traceLinks the trace links to save
     * @return true if the trace links were successfully saved, false otherwise
     */
    boolean saveTransitiveTraceLinks(Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks);

    /**
     * Loads a collection of transitive tracelinks between sentences and model entities.
     *
     * @return the loaded transitive tracelinks
     */
    Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> loadTransitiveTraceLinks();

    /**
     * Loads a collection of tracelinks between sentences and model entities.
     *
     * @return the loaded trace links
     */
    Collection<SentenceModelTraceLink> loadSentenceModelTraceLinks();

    /**
     * Saves a collection of tracelinks between sentences and model entities.
     *
     * @param traceLinks the trace links to save
     * @return true if the trace links were successfully saved, false otherwise
     */
    boolean saveSentenceModelTraceLinks(Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks);
}
