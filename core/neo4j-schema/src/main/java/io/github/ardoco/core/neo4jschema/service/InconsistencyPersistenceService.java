package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
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
import io.github.ardoco.core.neo4jschema.service.architectureModel.ArchitectureModelMapper;
import io.github.ardoco.core.neo4jschema.service.codeModel.CodeModelMapper;

@Service
public class InconsistencyPersistenceService implements InconsistencyNodeVisitor<Inconsistency> {

    private static final Logger logger = LoggerFactory.getLogger(InconsistencyPersistenceService.class);

    private final TraceableNodeRepository traceableNodeRepository;
    private final SentenceNodeRepository sentenceNodeRepository;
    private final InconsistencyNodeRepository inconsistencyRepository;

    private final ArchitectureModelMapper archMapper;
    private final CodeModelMapper codeMapper;

    private final Neo4jClient neo4jClient;

    public InconsistencyPersistenceService(SentenceNodeRepository sentenceNodeRepository, ArchitectureModelMapper archMapper, CodeModelMapper codeMapper,
            Neo4jClient neo4jClient, TraceableNodeRepository traceableNodeRepository, InconsistencyNodeRepository inconsistencyRepository) {

        this.neo4jClient = neo4jClient;
        this.archMapper = archMapper;
        this.codeMapper = codeMapper;
        this.traceableNodeRepository = traceableNodeRepository;
        this.sentenceNodeRepository = sentenceNodeRepository;
        this.inconsistencyRepository = inconsistencyRepository;
    }

    @Override
    public Inconsistency visit(ModelInconsistencyNode node) {
        TraceableNode modelNode = node.getTraceableNode();

        if (modelNode == null) {
            logger.error("InconsistencyNode with ID {} is missing its TraceableNode link!", node.getId());
            throw new IllegalStateException("Database integrity error: Inconsistency node is not linked to any TraceableNode.");
        }

        ModelEntity modelEntity = mapModelNodeToModel(modelNode);
        return new ModelEntityAbsentFromTextInconsistency(modelEntity);
    }

    @Override
    public Inconsistency visit(TextInconsistencyNode node) {
        return new Neo4jTextInconsistency(node.getName(), node.getSentenceNumber(), node.getConfidence());
    }

    public Collection<? extends Inconsistency> getInconsistencies() {
        List<InconsistencyNode> nodes = inconsistencyRepository.findAllWithRelationships();

        if (nodes.isEmpty()) {
            logger.info("No inconsistencies found in the database.");
            return List.of();
        }

        return nodes.stream().map(node -> node.accept(this)) // Use Visitor pattern to map each database node
                .toList();
    }

    @Transactional
    public boolean addInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
        List<TraceableNode> nodesToSave = new ArrayList<>();

        for (Inconsistency inconsistency : inconsistencies) {
            if (inconsistency instanceof ModelInconsistency modelInconsistency) {
                String ardocoId = modelInconsistency.getModelInstanceUid();
                TraceableNode modelNode = this.traceableNodeRepository.findByArdocoId(ardocoId)
                        .orElseThrow(() -> new IllegalArgumentException("No TraceableNode found in Neo4j database for Ardoco ID: " + ardocoId));
                if (!modelNode.getModelType().isModel()) {
                    throw new IllegalArgumentException(
                            "TraceableNode with Ardoco ID: " + ardocoId + " is not a model node, but has model type: " + modelNode.getModelType());
                }
                ModelInconsistencyNode modelInconsistencyNode = new ModelInconsistencyNode(modelInconsistency.getModelInstanceUid(),
                        modelInconsistency.getReason());
                modelInconsistencyNode.setTraceableNode(modelNode);
                modelNode.addInconsistency(modelInconsistencyNode);
                nodesToSave.add(modelNode);
            } else if (inconsistency instanceof TextInconsistency textInconsistency) {
                int sentenceNumber = textInconsistency.getSentenceNumber();
                TraceableNode modelNode = this.sentenceNodeRepository.findBySentenceNumber(sentenceNumber)
                        .orElseThrow(() -> new IllegalArgumentException("No TraceableNode found in Neo4j database for SentenceNumber: " + sentenceNumber));

                String name = "unknown";
                double confidence = 0.0;

                if (inconsistency instanceof TextEntityAbsentFromModelInconsistency teamInconsistency) {
                    name = teamInconsistency.name();
                    confidence = teamInconsistency.confidence();
                }

                TextInconsistencyNode modelInconsistencyNode = new TextInconsistencyNode(name, sentenceNumber, confidence, textInconsistency.getReason(),
                        textInconsistency.getType());
                modelInconsistencyNode.setTraceableNode(modelNode);
                modelNode.addInconsistency(modelInconsistencyNode);
                nodesToSave.add(modelNode);
            }
        }

        // Single Batch Save
        traceableNodeRepository.saveAll(nodesToSave);
        return true;
    }

    private ModelEntity mapModelNodeToModel(TraceableNode modelNode) {
        if (modelNode.getModelType() == ArchitectureType.ARCHITECTURE) {
            ArchitectureItemNode architectureItemNode = (ArchitectureItemNode) modelNode;
            return archMapper.mapItem(architectureItemNode);
        } else if (modelNode.getModelType() == ArchitectureType.CODE) {
            CodeItemNode codeNode = (CodeItemNode) modelNode;
            return codeMapper.mapItem(codeNode);
        }
        throw new IllegalArgumentException("Unknown model node type for mapping: " + modelNode.getClass().getName());
    }

}
