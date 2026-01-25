package io.github.ardoco.core.neo4jschema.service;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceLinkRelationship;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureItemNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeItemNode;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceLinkType;
import io.github.ardoco.core.neo4jschema.repository.architectureModel.ArchitectureItemRepository;

import io.github.ardoco.core.neo4jschema.repository.codeModel.CodeItemRepository;

import io.github.ardoco.core.neo4jschema.repository.tracelink.TraceLinkRepository;

import io.github.ardoco.core.neo4jschema.service.architectureModel.ArchitectureModelMapper;

import io.github.ardoco.core.neo4jschema.service.codeModel.CodeModelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TraceLinkPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(TraceLinkPersistenceService.class);

    private final ArchitectureItemRepository archRepo;
    private final CodeItemRepository codeRepo;
    private final TraceLinkRepository traceLinkRepo;

    private final ArchitectureModelMapper archMapper;
    private final CodeModelMapper codeMapper;

    public TraceLinkPersistenceService(ArchitectureItemRepository archRepo, CodeItemRepository codeRepo,
            TraceLinkRepository traceLinkRepo, ArchitectureModelMapper archMapper,
            CodeModelMapper codeMapper) {
        this.archRepo = archRepo;
        this.codeRepo = codeRepo;
        this.traceLinkRepo = traceLinkRepo;
        this.archMapper = archMapper;
        this.codeMapper = codeMapper;
    }

    @Transactional
    public void saveTraceLinks(ArchitectureCodeTraceLink traceLink) {
        saveGenericLink(traceLink);
    }

    public void saveAllTraceLinks(Collection<? extends TraceLink<?,?>> traceLinks) {
        for (TraceLink<?,?> link : traceLinks) {
            saveGenericLink(link);
        }
    }


    private void saveGenericLink(TraceLink<?,?> link) {
        if (link instanceof ArchitectureCodeTraceLink) {
            ArchitectureItem architectureItem = (ArchitectureItem) link.getFirstEndpoint();
            CodeItem codeItem = (CodeItem) link.getSecondEndpoint();
            var archNodeOpt = archRepo.findByArdocoId(architectureItem.getId());
            var codeNodeOpt = codeRepo.findByArdocoId(codeItem.getId());

            if (archNodeOpt.isPresent() && codeNodeOpt.isPresent()) {
                ArchitectureItemNode archNode = archNodeOpt.get();
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
        return links;
    }
}
