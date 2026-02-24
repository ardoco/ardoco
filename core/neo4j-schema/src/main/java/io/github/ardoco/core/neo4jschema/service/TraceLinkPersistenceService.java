/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.ardoco.core.neo4jschema.repository.architectureModel.ArchitectureModelRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureItemNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeItemNode;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceLinkRelationship;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceLinkType;
import io.github.ardoco.core.neo4jschema.repository.architectureModel.ArchitectureItemRepository;
import io.github.ardoco.core.neo4jschema.repository.codeModel.CodeItemRepository;
import io.github.ardoco.core.neo4jschema.repository.tracelink.TraceLinkRepository;
import io.github.ardoco.core.neo4jschema.service.architectureModel.ArchitectureModelMapper;
import io.github.ardoco.core.neo4jschema.service.codeModel.CodeModelMapper;

@Service
public class TraceLinkPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(TraceLinkPersistenceService.class);

    private final ArchitectureItemRepository archRepo;
    private final CodeItemRepository codeRepo;
    private final TraceLinkRepository traceLinkRepo;

    private final ArchitectureModelMapper archMapper;
    private final CodeModelMapper codeMapper;

    ArchitectureModelRepository archModelRepo;

    public TraceLinkPersistenceService(ArchitectureItemRepository archRepo, ArchitectureModelRepository archModelRepo, CodeItemRepository codeRepo, TraceLinkRepository traceLinkRepo,
            ArchitectureModelMapper archMapper, CodeModelMapper codeMapper) {
        this.archRepo = archRepo;
        this.codeRepo = codeRepo;
        this.traceLinkRepo = traceLinkRepo;
        this.archMapper = archMapper;
        this.codeMapper = codeMapper;
        this.archModelRepo = archModelRepo;
    }

    @Transactional
    public void saveTraceLinks(ArchitectureCodeTraceLink traceLink) {
        saveGenericLink(traceLink);
    }

    public boolean saveAllTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
        saveGenericLinks(traceLinks);
//        for (TraceLink<?, ?> link : traceLinks) {
//            saveGenericLink(link);
//        }
        return true; // TODO: Implement proper error handling and return false if any save operation fails
    }

    private void saveGenericLinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
        // Collect all unique IDs that need to be queried
        Set<String> archIds = new HashSet<>();
        Set<String> codeIds = new HashSet<>();

        for (TraceLink<?, ?> link : traceLinks) {
            if (link instanceof ArchitectureCodeTraceLink actl) {
                archIds.add(((ArchitectureItem) actl.getFirstEndpoint()).getId());
                codeIds.add(((CodeItem) actl.getSecondEndpoint()).getId());
            }
        }

        //Batch fetch all nodes in two queries
        Map<String, ArchitectureItemNode> archMap = archRepo.findAllByArdocoIdIn(archIds)
                .stream().collect(Collectors.toMap(ArchitectureItemNode::getArdocoId, n -> n, (n1, n2) -> n1));

        Map<String, CodeItemNode> codeMap = codeRepo.findAllByArdocoIdIn(codeIds)
                .stream().collect(Collectors.toMap(CodeItemNode::getArdocoId, n -> n, (n1, n2) -> n1));

        // Process links in-memory
        List<ArchitectureItemNode> updatedNodes = new ArrayList<>();
        for (TraceLink<?, ?> link : traceLinks) {
            if (link instanceof ArchitectureCodeTraceLink actl) {
                ArchitectureItemNode archNode = archMap.get(((ArchitectureItem) actl.getFirstEndpoint()).getId());
                CodeItemNode codeNode = codeMap.get(((CodeItem) actl.getSecondEndpoint()).getId());

                if (archNode != null && codeNode != null) {
                    // Use your existing Relationship class
                    TraceLinkRelationship rel = new TraceLinkRelationship(codeNode, null, TraceLinkType.ARCHITECTURE_CODE);
                    archNode.addTraceLink(rel);
                    updatedNodes.add(archNode);
                }
            }
        }

        // Batch Save to avoid "Inferred Type" error
        if (!updatedNodes.isEmpty()) {
            archRepo.saveAll(updatedNodes);
        }
    }

    private void saveGenericLink(TraceLink<?, ?> link) {
        if (link instanceof ArchitectureCodeTraceLink) {
            ArchitectureItem architectureItem = (ArchitectureItem) link.getFirstEndpoint();
            CodeItem codeItem = (CodeItem) link.getSecondEndpoint();
            // var archNodeOpt = archRepo.findByArdocoId(architectureItem.getId());
            var archNodes = archRepo.findByArdocoId(architectureItem.getId());
            if (archNodes.size() > 1) {
                logger.error("Found {} duplicate nodes for ID: {}. Printing details...",
                        archNodes.size(), architectureItem.getId());
                for (var node : archNodes) {
                    logger.error("Node internal ID: {} | Property value: {}",
                            node.getId(), node.getArdocoId());
                }
            } else {
                logger.info("Found {} node(s) for ID: {}", archNodes.size(), architectureItem.getId());
            }

            var codeNodeOpt = codeRepo.findByArdocoId(codeItem.getId());
//            if (codeNodeOpt.isPresent()) {
//                CodeItemNode codeNode = codeNodeOpt.get();
//                logger.info("Found CodeItemNode with ID: {} and name: {}", codeNode.getArdocoId(), codeNode.getName());
//            } else {
//                logger.warn("No CodeItemNode found for ID: {}", codeItem.getId());
//            }

//            if (archNodeOpt.isPresent() && codeNodeOpt.isPresent()) {
//                ArchitectureItemNode archNode = archNodeOpt.get();
            ArchitectureItemNode archNode = (ArchitectureItemNode) archNodes.getFirst();
            if (codeNodeOpt.isPresent()) {
                CodeItemNode codeNode = codeNodeOpt.get();

                Double confidence = null;
                // if (link instanceof ConfidentTraceLink ctl) { confidence = ctl.getProbability(); }

                TraceLinkRelationship rel = new TraceLinkRelationship(codeNode, confidence, TraceLinkType.ARCHITECTURE_CODE);
                archNode.addTraceLink(rel);
                archRepo.save(archNode);
            } else {
                logger.warn("Skipping TraceLink save: Endpoint not found in DB.");
            }
        }
    }

    @Transactional(readOnly = true)
    public Set<ArchitectureCodeTraceLink> loadAllArchitectureCodeTraceLinks() {
        Set<ArchitectureCodeTraceLink> links = new HashSet<>();
        List<ArchitectureItemNode> nodes = traceLinkRepo.findAllWithTraceLinks();

        for (ArchitectureItemNode archNode : nodes) {
            ArchitectureItem archItem = archMapper.mapItem(archNode);

            for (TraceLinkRelationship rel : archNode.getTraceLinks()) {
                if (TraceLinkType.ARCHITECTURE_CODE.equals(rel.getTraceLinkType())) {
                    CodeItem codeItem = codeMapper.mapItem(rel.getTargetCodeItem());

                    ArchitectureCodeTraceLink link = new ArchitectureCodeTraceLink(archItem, codeItem);
                    links.add(link);
                }
            }
        }
        logger.info("Loaded {} architecture code trace links.", links.size());
        return links;
    }
}
