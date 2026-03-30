package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.github.ardoco.core.neo4jschema.repository.inconsistencies.TextInconsistencyNodeRepository;

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
import io.github.ardoco.core.neo4jschema.entities.inconsistencies.InconsistencyNode;
import io.github.ardoco.core.neo4jschema.entities.inconsistencies.InconsistencyNodeVisitor;
import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ModelInconsistencyNode;
import io.github.ardoco.core.neo4jschema.entities.inconsistencies.TextInconsistencyNode;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;
import io.github.ardoco.core.neo4jschema.repository.TraceableNodeRepository;
import io.github.ardoco.core.neo4jschema.repository.documentation.SentenceNodeRepository;
import io.github.ardoco.core.neo4jschema.repository.inconsistencies.InconsistencyNodeRepository;
import io.github.ardoco.core.neo4jschema.mapper.ArchitectureModelMapper;
import io.github.ardoco.core.neo4jschema.mapper.CodeModelMapper;

@Service
public class InconsistencyPersistenceService implements InconsistencyNodeVisitor<Inconsistency> {

    private static final Logger logger = LoggerFactory.getLogger(InconsistencyPersistenceService.class);

    private final TraceableNodeRepository traceableNodeRepository;
    private final SentenceNodeRepository sentenceNodeRepository;
    private final InconsistencyNodeRepository inconsistencyRepository;
    private final TextInconsistencyNodeRepository textInconsistencyRepository;

    private final ArchitectureModelMapper archMapper;
    private final CodeModelMapper codeMapper;

    public InconsistencyPersistenceService(SentenceNodeRepository sentenceNodeRepository, ArchitectureModelMapper archMapper, CodeModelMapper codeMapper,
            TraceableNodeRepository traceableNodeRepository, InconsistencyNodeRepository inconsistencyRepository,
            TextInconsistencyNodeRepository textInconsistencyRepository) {

        this.archMapper = archMapper;
        this.codeMapper = codeMapper;
        this.traceableNodeRepository = traceableNodeRepository;
        this.sentenceNodeRepository = sentenceNodeRepository;
        this.inconsistencyRepository = inconsistencyRepository;
        this.textInconsistencyRepository = textInconsistencyRepository;
    }
    @Transactional
    public boolean addInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
        if (inconsistencies == null || inconsistencies.isEmpty()) {
            return false;
        }

        List<InconsistencyNode> nodesToSave = new ArrayList<>();

        for (Inconsistency inconsistency : inconsistencies) {
            String reason = inconsistency.getReason();

            if (inconsistency instanceof ModelInconsistency mi) {
                String uid = mi.getModelInstanceUid();
                if (!inconsistencyRepository.existsModelInconsistency(uid, reason)) {
                    this.traceableNodeRepository.findByArdocoId(uid).ifPresentOrElse(parent -> {
                        ModelInconsistencyNode modelNode = new ModelInconsistencyNode(uid, reason);
                        modelNode.setTraceableNode(parent);
                        nodesToSave.add(modelNode);
                    }, () -> logger.warn("No TraceableNode found in Neo4j for Ardoco ID: {}. Skipping inconsistency with reason: {}", uid, reason));
                }

            } else if (inconsistency instanceof TextInconsistency ti) {
                // Keep your debug print
                System.out.println("Text inconsistency: " + ti.getSentenceNumber() + " - " + reason);

                int num = ti.getSentenceNumber();
                String type = ti.getType();

                if (!inconsistencyRepository.existsTextInconsistency(num, reason, type)) {

                    this.sentenceNodeRepository.findBySentenceNumber(num).ifPresentOrElse(parent -> {
                        // Keep your debug print
                        System.out.println("Found TraceableNode for sentence number: " + num);

                        String name = (ti instanceof TextEntityAbsentFromModelInconsistency team) ? team.name() : "unknown";
                        double conf = (ti instanceof TextEntityAbsentFromModelInconsistency team) ? team.confidence() : -1.0;
                        var textNode = new TextInconsistencyNode(name, num, conf, reason, type);
                        textNode.setTraceableNode(parent);
                        nodesToSave.add(textNode);
                    }, () -> logger.warn("No TraceableNode found in Neo4j for SentenceNumber: {}. Skipping inconsistency with reason: {}", num, reason));
                }
            } else {
                logger.warn("Unknown inconsistency type encountered: {}. Skipping this inconsistency with reason: {}",
                        inconsistency.getClass().getName(), reason);
            }
        }

        if (!nodesToSave.isEmpty()) {
            logger.info("About to save inconsistencys");
            inconsistencyRepository.saveAll(nodesToSave);
            logger.info("Successfully persisted {} new unique inconsistencies.", nodesToSave.size());
        }

        return true;
    }

    @Transactional(readOnly = true)
    public Collection<? extends Inconsistency> getInconsistencies() {
        return inconsistencyRepository.findAllWithRelationships().stream().map(node -> node.accept(this)) // Use Visitor pattern to map each database node
                .toList();
    }

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
    public void deleteInconsistenciesOfArchitectureItems() {
        inconsistencyRepository.deleteInconsistenciesForArchitectureItems();
    }

    /**
     * Deletes all inconsistencies that are associated with code items.
     */
    public void deleteInconsistenciesOfCodeItems() {
        inconsistencyRepository.deleteInconsistenciesForCodeItems();
    }

    public void deleteTextInconsistencies() {
        textInconsistencyRepository.deleteAllTextInconsistencies();
    }

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
