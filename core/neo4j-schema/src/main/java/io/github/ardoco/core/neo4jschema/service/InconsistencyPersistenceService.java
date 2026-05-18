/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import io.github.ardoco.core.neo4jschema.repository.TraceableNodeRepository;
import io.github.ardoco.core.neo4jschema.repository.documentation.SentenceNodeRepository;
import io.github.ardoco.core.neo4jschema.repository.inconsistencies.InconsistencyNodeRepository;

@Service
public class InconsistencyPersistenceService implements InconsistencyNodeVisitor<Inconsistency> {

    private static final Logger logger = LoggerFactory.getLogger(InconsistencyPersistenceService.class);

    private final TraceableNodeRepository traceableNodeRepository;
    private final SentenceNodeRepository sentenceNodeRepository;
    private final InconsistencyNodeRepository inconsistencyRepository;

    private final ArchitectureModelMapper archMapper;
    private final CodeModelMapper codeMapper;

    public InconsistencyPersistenceService(SentenceNodeRepository sentenceNodeRepository, ArchitectureModelMapper archMapper, CodeModelMapper codeMapper,
            TraceableNodeRepository traceableNodeRepository, InconsistencyNodeRepository inconsistencyRepository) {
        this.archMapper = archMapper;
        this.codeMapper = codeMapper;
        this.traceableNodeRepository = traceableNodeRepository;
        this.sentenceNodeRepository = sentenceNodeRepository;
        this.inconsistencyRepository = inconsistencyRepository;
    }

    /**
     * Adds a collection of inconsistencies to the database. For each inconsistency, it checks if an equivalent inconsistency already exists to prevent
     * duplicates.
     *
     * @param inconsistencies The collection of inconsistencies to be added to the database. This collection can contain both model-based and text-based
     *                        inconsistencies.
     * @return true if the inconsistencies were successfully added (or already exist)
     */
    @Transactional
    public boolean addInconsistencies(Collection<? extends Inconsistency> inconsistencies) {

        for (Inconsistency inconsistency : inconsistencies) {
            String reason = inconsistency.getReason();

            if (inconsistency instanceof ModelInconsistency mi) {
                String uid = mi.getModelInstanceUid();
                if (!inconsistencyRepository.existsModelInconsistency(uid, reason)) {
                    this.traceableNodeRepository.findByArdocoId(uid).ifPresentOrElse(parent -> {
                        inconsistencyRepository.saveModelInconsistency(uid, uid, reason);
                    }, () -> logger.warn("No TraceableNode found in Neo4j for Ardoco ID: {}. Skipping inconsistency with reason: {}", uid, reason));
                }

            } else if (inconsistency instanceof TextInconsistency ti) {
                int num = ti.getSentenceNumber();
                String type = ti.getType();

                if (!inconsistencyRepository.existsTextInconsistency(num, reason, type)) {
                    this.sentenceNodeRepository.findBySentenceNumber(num).ifPresentOrElse(parent -> {
                        String name = (ti instanceof TextEntityAbsentFromModelInconsistency team) ? team.name() : "unknown";
                        double conf = (ti instanceof TextEntityAbsentFromModelInconsistency team) ? team.confidence() : -1.0;
                        inconsistencyRepository.saveTextInconsistency(num, name, conf, reason, type);
                    }, () -> logger.warn("No TraceableNode found in Neo4j for SentenceNumber: {}. Skipping inconsistency with reason: {}", num, reason));
                }
            } else {
                logger.warn("Unknown inconsistency type encountered: {}. Skipping this inconsistency with reason: {}", inconsistency.getClass().getName(),
                        reason);
            }
        }

        return true;
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
                sentenceNumbers.add(ti.getSentenceNumber());
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
            throw new IllegalStateException("Orphaned Inconsistency: " + node.getId());
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
