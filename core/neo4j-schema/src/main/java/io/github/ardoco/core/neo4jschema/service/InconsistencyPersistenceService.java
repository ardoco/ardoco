/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ModelInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.TextInconsistency;
import edu.kit.kastel.mcse.ardoco.id.types.ModelEntityAbsentFromTextInconsistency;
import edu.kit.kastel.mcse.ardoco.id.types.TextEntityAbsentFromModelInconsistency;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jTextInconsistency;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureItemNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeItemNode;
import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ArchitectureType;
import io.github.ardoco.core.neo4jschema.entities.inconsistencies.InconsistencyNodeVisitor;
import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ModelInconsistencyNode;
import io.github.ardoco.core.neo4jschema.entities.inconsistencies.TextInconsistencyNode;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;
import io.github.ardoco.core.neo4jschema.mapper.ArchitectureModelMapper;
import io.github.ardoco.core.neo4jschema.mapper.CodeModelMapper;
import io.github.ardoco.core.neo4jschema.repository.inconsistencies.InconsistencyNodeRepository;

@Service
public class InconsistencyPersistenceService implements InconsistencyNodeVisitor<Inconsistency> {

    private static final Logger logger = LoggerFactory.getLogger(InconsistencyPersistenceService.class);

    private static final int WRITE_CHUNK_SIZE = 2000;

    private static final String TEXT_INCONSISTENCY_QUERY = """
            UNWIND $rows AS row
            OPTIONAL MATCH (s:Sentence {sentenceNumber: row.sentenceIndex}) USING INDEX SEEK s:Sentence(sentenceNumber)
            WHERE EXISTS { MATCH (:Text {ardocoId: $textId})-[:HAS_SENTENCE]->(s) }
            WITH row, collect(s) AS targets
            FOREACH (s IN CASE WHEN size(targets) = 1 THEN targets ELSE [] END |
                MERGE (s)-[:HAS_INCONSISTENCY]->(i:TextInconsistency:Inconsistency {reason: row.reason, type: row.type, confidence: row.confidence})
                ON CREATE SET i.id = randomUUID(), i.name = row.name, i.sentenceNumber = row.sentenceNumber)
            WITH row, size(targets) AS matches
            WHERE matches <> 1
            RETURN row.sentenceNumber AS sentenceNumber, row.reason AS reason, matches
            """;

    private static final String MODEL_INCONSISTENCY_QUERY = """
            UNWIND $rows AS row
            OPTIONAL MATCH (t:Traceable {ardocoId: row.uid}) USING INDEX SEEK t:Traceable(ardocoId)
            WHERE t:ArchitectureItem OR t:CodeItem
            WITH row, collect(t) AS targets
            FOREACH (t IN CASE WHEN size(targets) = 1 THEN targets ELSE [] END |
                MERGE (t)-[:HAS_INCONSISTENCY]->(i:ModelInconsistency:Inconsistency {reason: row.reason})
                ON CREATE SET i.id = randomUUID(), i.modelArdocoId = row.uid, i.type = "ModelEntityAbsentFromText")
            WITH row, size(targets) AS matches
            WHERE matches <> 1
            RETURN row.uid AS uid, row.reason AS reason, matches
            """;

    private final InconsistencyNodeRepository inconsistencyRepository;
    private final Neo4jClient neo4jClient;

    private final ArchitectureModelMapper archMapper;
    private final CodeModelMapper codeMapper;

    public InconsistencyPersistenceService(ArchitectureModelMapper archMapper, CodeModelMapper codeMapper, InconsistencyNodeRepository inconsistencyRepository,
            Neo4jClient neo4jClient) {
        this.archMapper = archMapper;
        this.codeMapper = codeMapper;
        this.inconsistencyRepository = inconsistencyRepository;
        this.neo4jClient = neo4jClient;
    }

    /**
     * Adds a collection of inconsistencies to the database in one batched {@code UNWIND} query per inconsistency kind (chunks of at most
     * {@value #WRITE_CHUNK_SIZE} rows); no entity is loaded. A text inconsistency carries the 1-based sentence number of the output file and is attached
     * to the {@code Sentence} with {@code sentenceNumber = n - 1} of the run's text ({@code Text {ardocoId: PreprocessingData.ID}}); the inconsistency
     * node keeps the 1-based number. A model inconsistency is attached to the architecture or code item with its uid. {@code MERGE} on (sentence, reason,
     * type, exact confidence) for a text inconsistency and on (parent, reason) for a model inconsistency keeps the write idempotent. A missing or ambiguous
     * parent, or an unknown inconsistency type, is logged as one ERROR and thrown as a
     * {@link MissingLinkTargetException} after the last chunk; the call runs in one transaction, so the exception rolls back all of its rows.
     *
     * @param inconsistencies The collection of inconsistencies to be added to the database. This collection can contain both model-based and text-based
     *                        inconsistencies.
     * @return true if every inconsistency was attached
     */
    @Transactional
    public boolean addInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
        Map<String, Map<String, Object>> textRows = new LinkedHashMap<>();
        Map<String, Map<String, Object>> modelRows = new LinkedHashMap<>();
        LinkTargetErrors errors = new LinkTargetErrors("Inconsistency");

        for (Inconsistency inconsistency : inconsistencies) {
            String reason = inconsistency.getReason();
            if (inconsistency instanceof ModelInconsistency mi) {
                String uid = mi.getModelInstanceUid();
                modelRows.put(uid + "\n" + reason, Map.of("uid", uid, "reason", reason));
            } else if (inconsistency instanceof TextInconsistency ti) {
                int sentenceNumber = ti.getSentenceNumber();
                String type = ti.getType();
                double confidence = ti instanceof TextEntityAbsentFromModelInconsistency team ? team.confidence() : -1.0;
                Map<String, Object> row = new HashMap<>();
                row.put("sentenceNumber", sentenceNumber);
                row.put("sentenceIndex", sentenceNumber - 1);
                row.put("name", ti instanceof TextEntityAbsentFromModelInconsistency team ? team.name() : "unknown");
                row.put("confidence", confidence);
                row.put("reason", reason);
                row.put("type", type);
                textRows.put(sentenceNumber + "\n" + reason + "\n" + type + "\n" + confidence, row);
            } else {
                errors.missing("unknown inconsistency type", 1, inconsistency.getClass().getName());
            }
        }

        for (List<Map<String, Object>> chunk : chunk(new ArrayList<>(textRows.values()))) {
            for (Map<String, Object> unresolved : neo4jClient.query(TEXT_INCONSISTENCY_QUERY)
                    .bind(PreprocessingData.ID)
                    .to("textId")
                    .bind(chunk)
                    .to("rows")
                    .fetch()
                    .all()) {
                errors.recordEdge("HAS_INCONSISTENCY (Sentence)", "sentence " + unresolved.get("sentenceNumber"), unresolved.get("reason"), ((Number) unresolved
                        .get("matches")).longValue(), 1);
            }
        }
        for (List<Map<String, Object>> chunk : chunk(new ArrayList<>(modelRows.values()))) {
            for (Map<String, Object> unresolved : neo4jClient.query(MODEL_INCONSISTENCY_QUERY).bind(chunk).to("rows").fetch().all()) {
                errors.recordEdge("HAS_INCONSISTENCY (model item)", unresolved.get("uid"), unresolved.get("reason"), ((Number) unresolved.get("matches"))
                        .longValue(), 1);
            }
        }
        errors.throwIfAny(logger);
        return true;
    }

    private static List<List<Map<String, Object>>> chunk(List<Map<String, Object>> rows) {
        List<List<Map<String, Object>>> chunks = new ArrayList<>();
        for (int start = 0; start < rows.size(); start += WRITE_CHUNK_SIZE) {
            chunks.add(rows.subList(start, Math.min(start + WRITE_CHUNK_SIZE, rows.size())));
        }
        return chunks;
    }

    /**
     * Retrieves all inconsistencies from the database and maps them to their corresponding domain objects.
     *
     * @return A collection of Inconsistency objects representing all inconsistencies currently stored in the database. This collection can contain both
     *         model-based and text-based inconsistencies.
     */
    @Transactional(readOnly = true)
    public Collection<? extends Inconsistency> getInconsistencies() {
        return inconsistencyRepository.findAllWithRelationships()
                .stream()
                .map(node -> node.accept(this)) // Use Visitor pattern to map each database node
                .filter(java.util.Objects::nonNull) // orphaned nodes are skipped (see visit(ModelInconsistencyNode))
                .toList();
    }

    /**
     * Deletes a collection of inconsistencies from the database.
     *
     * @param inconsistencies The collection of inconsistencies to be deleted from the database. This collection can contain both model-based and text-based
     *                        inconsistencies.
     */
    @Transactional
    public void deleteInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
        List<String> modelIds = new ArrayList<>();
        List<Integer> sentenceNumbers = new ArrayList<>();

        for (Inconsistency inc : inconsistencies) {
            if (inc instanceof ModelInconsistency mi) {
                modelIds.add(mi.getModelInstanceUid());
            } else if (inc instanceof TextInconsistency ti) {
                sentenceNumbers.add(ti.getSentenceNumber() - 1);
            }
        }

        if (!modelIds.isEmpty()) {
            inconsistencyRepository.deleteByModelUids(modelIds);
        }
        if (!sentenceNumbers.isEmpty()) {
            inconsistencyRepository.deleteBySentenceNumbers(sentenceNumbers);
        }
        logger.info("Deleted {} inconsistencies ({} model-based, {} text-based).", inconsistencies.size(), modelIds.size(), sentenceNumbers.size());
    }

    /**
     * Deletes all inconsistencies that are associated with architecture items.
     */
    @Transactional
    public void deleteInconsistenciesOfArchitectureItems() {
        inconsistencyRepository.deleteInconsistenciesForArchitectureItems();
    }

    /**
     * Deletes all inconsistencies that are associated with code items.
     */
    @Transactional
    public void deleteInconsistenciesOfCodeItems() {
        inconsistencyRepository.deleteInconsistenciesForCodeItems();
    }

    /**
     * Deletes all inconsistencies that are associated with sentences in the text.
     */
    @Transactional
    public void deleteTextInconsistencies() {
        inconsistencyRepository.deleteTextInconsistencies();
    }

    /**
     * Deletes all inconsistencies from the database, regardless of their type or association. Use with caution, as this will remove all inconsistency data.
     */
    @Transactional
    public void deleteAllInconsistencies() {
        inconsistencyRepository.deleteAll();
    }

    @Override
    public Inconsistency visit(ModelInconsistencyNode node) {
        TraceableNode modelNode = node.getTraceableNode();
        if (modelNode == null) {
            // Can only come from data written before model deletes removed attached inconsistencies; skip instead of failing the whole load.
            logger.warn("Skipping orphaned model inconsistency {} (its model element no longer exists)", node.getId());
            return null;
        }

        ModelEntity modelEntity = (modelNode.getModelType() == ArchitectureType.ARCHITECTURE) ?
                archMapper.mapItem((ArchitectureItemNode) modelNode) :
                codeMapper.toDomain((CodeItemNode) modelNode);

        return new ModelEntityAbsentFromTextInconsistency(modelEntity);
    }

    @Override
    public Inconsistency visit(TextInconsistencyNode node) {
        return new Neo4jTextInconsistency(node.getName(), node.getSentenceNumber(), node.getConfidence());
    }

}
