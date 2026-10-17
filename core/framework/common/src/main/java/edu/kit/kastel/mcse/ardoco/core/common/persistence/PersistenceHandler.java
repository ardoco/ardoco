/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.util.Collection;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.SimpleText;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

/**
 * Interface defining persistence operations.
 * Implemented by the neo4j-schema module.
 * <p>
 * Write-only by design: nothing reads Neo4j during a run (memory is authoritative). Reads needed outside write
 * paths (e.g. the neo4j-schema module's own facade tests, or matching existing nodes by id before creating an
 * edge inside a write path) stay on the concrete {@code Neo4jPersistenceHandler}/service classes, not here.
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
     * Saves the preprocessed text for the given identifier.
     *
     * @param text       the preprocessed text to save
     * @param identifier the identifier for the preprocessed text which can also be used to load it later
     */
    void savePreprocessedText(Text text, String identifier);

    /**
     * Saves a collection of tracelinks
     *
     * @param traceLinks the trace links to save
     * @return true if the trace links were successfully saved, false otherwise
     */
    boolean saveTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks);

    /**
     * Saves a collection of inconsistencies.
     *
     * @param inconsistencies the inconsistencies to save
     *
     * @return true if the inconsistencies were successfully saved, false otherwise
     */
    boolean addInconsistencies(Collection<? extends Inconsistency> inconsistencies);

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
     * Batched form of {@link #saveNounMapping}: upserts every mapping (and its word/reference/phrase links) in one
     * round trip instead of one per mapping. Used by the stage-level write-behind ({@code PersistenceBridge::withDeferredWrites}).
     *
     * @param nounMappings the mappings to persist; a no-op if empty
     */
    void saveNounMappings(Collection<NounMapping> nounMappings);

    /**
     * Deletes a persisted noun mapping by its stable ardoco id.
     *
     * @param ardocoId the mapping id
     */
    void deleteNounMapping(String ardocoId);

    /**
     * Saves (upserts) a recommended instance. Dual-write: does not replace in-memory RecommendationState.
     *
     * @param recommendedInstance the recommended instance
     * @param metamodel           the metamodel bucket this instance belongs to
     */
    void saveRecommendedInstance(RecommendedInstance recommendedInstance, Metamodel metamodel);

    /**
     * Batched form of {@link #saveRecommendedInstance}: upserts every instance (all for the same metamodel) in one
     * round trip instead of one per instance. Used by the stage-level write-behind ({@code PersistenceBridge::withDeferredWrites}).
     *
     * @param recommendedInstances the instances to persist; a no-op if empty
     * @param metamodel            the metamodel bucket the instances belong to
     */
    void saveRecommendedInstances(Collection<RecommendedInstance> recommendedInstances, Metamodel metamodel);

    /**
     * Saves a thin {@link SimpleText} (raw text + ordered lines).
     */
    void saveSimpleText(SimpleText simpleText, String identifier);

    /**
     * Saves project metadata (name + raw input text) on a Project root node.
     */
    void saveProjectMetadata(String projectName, String inputText);

    /**
     * Dual-writes a named architecture entity (and its occurrences) for a metamodel.
     *
     * @param entity    the entity
     * @param metamodel metamodel bucket
     * @param unlinked  true if stored in the unlinked set
     */
    void saveNamedArchitectureEntity(NamedArchitectureEntity entity, Metamodel metamodel, boolean unlinked);

    /**
     * Batched form of {@link #saveNamedArchitectureEntity}: dual-writes every entity (all for the same metamodel and
     * linked/unlinked bucket) in one transaction instead of one per entity. Used by the stage-level write-behind
     * ({@code PersistenceBridge::withDeferredWrites}).
     *
     * @param entities  the entities to save; a no-op if empty
     * @param metamodel metamodel bucket
     * @param unlinked  true if stored in the unlinked set
     */
    void saveNamedArchitectureEntities(Collection<NamedArchitectureEntity> entities, Metamodel metamodel, boolean unlinked);

    /**
     * Exports the entire graph as a lossless JSON dump: every node (id, labels, properties) and every relationship
     * (id, type, start/end node id, properties) currently stored. Used to keep a Neo4j-mode run's knowledge graph on
     * disk once the run ends; the live database itself is wiped after every run (interim format until the final
     * knowledge-graph format is researched).
     *
     * @return the JSON dump as a string
     */
    String exportGraphAsJson();
}
