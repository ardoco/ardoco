/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.util.Collection;
import java.util.SortedSet;

import org.eclipse.jgit.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
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
     * @param metamodel the metamodel (this will be used to identify the model when loading it later)
     * @param model     the model to save
     */
    void saveModel(Metamodel metamodel, Model model);

    /**
     * Loads the model for the given metamodel from persistence.
     *
     * @param metamodel the metamodel which should be loaded
     * @return the loaded model or null if no model for the given metamodel is stored
     */
    @Nullable
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
     * @param text       the preprocessed text to save
     * @param identifier the identifier for the preprocessed text which can also be used to load it later
     */
    void savePreprocessedText(Text text, String identifier);

    /**
     * Loads the preprocessed text for the given identifier from persistence.
     *
     * @param identifier the identifier for the preprocessed text
     * @return the loaded preprocessed text
     */
    @Nullable
    Text loadPreprocessedText(String identifier);

    /**
     * Checks if a preprocessed text for the given identifier is stored in persistence.
     *
     * @param identifier the identifier for the preprocessed text
     * @return true if a preprocessed text for the given identifier is stored, false otherwise
     */
    boolean hasPreprocessedText(String identifier);

    /**
     * Saves a collection of tracelinks
     *
     * @param traceLinks the trace links to save
     * @return true if the trace links were successfully saved, false otherwise
     */
    boolean saveTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks);

    /**
     * Loads the specific Architecture-Code links from the database.
     *
     * @return the loaded Architecture-Code tracelinks
     */
    Collection<ArchitectureCodeTraceLink> loadArchitectureCodeTraceLinks();

    /**
     * Loads a collection of transitive tracelinks between sentences and code model entities.
     * As well as direct links between sentences and Code Model entities
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
     * Saves a collection of inconsistencies.
     *
     * @param inconsistencies the inconsistencies to save
     *
     * @return true if the inconsistencies were successfully saved, false otherwise
     */
    boolean addInconsistencies(Collection<? extends Inconsistency> inconsistencies);

    /**
     * Retrieves all inconsistencies from persistence.
     *
     * @return a collection of all inconsistencies
     */
    Collection<? extends Inconsistency> getInconsistencies();

    /**
     * Removes all data associated with a specific metamodel.
     * 
     * @param metamodel the metamodel to clear
     */
    void deleteModel(Metamodel metamodel);

    /**
     * Deletes a specific preprocessed text.
     * 
     * @param identifier the identifier of the text to remove
     */
    void deletePreprocessedText(String identifier);

    /**
     * Deletes the given inconsistencies from persistence.
     *
     * @param inconsistencies the inconsistencies to delete
     */
    void deleteInconsistencies(Collection<? extends Inconsistency> inconsistencies);

    /**
     * Removes all stored inconsistencies.
     */
    void deleteAllInconsistencies();

    /**
     * Wipes the entire persistence storage (useful for fresh starts/tests).
     */
    void deleteAllData();

    /**
     * Saves (upserts) a TextState noun mapping. Write-only: does not replace in-memory TextState.
     *
     * @param nounMapping the mapping to persist
     */
    void saveNounMapping(NounMapping nounMapping);

    /**
     * Deletes a persisted noun mapping by its stable ardoco id.
     *
     * @param ardocoId the mapping id
     */
    void deleteNounMapping(String ardocoId);

    /**
     * Saves (upserts) a recommended instance. Write-only: does not replace in-memory RecommendationState.
     *
     * @param recommendedInstance the recommended instance
     * @param metamodel           the metamodel bucket this instance belongs to
     */
    void saveRecommendedInstance(RecommendedInstance recommendedInstance, Metamodel metamodel);
}
