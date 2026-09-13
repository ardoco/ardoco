/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityToModelTraceLink;
import io.github.ardoco.core.neo4jschema.entities.ner.NamedArchitectureEntityNode;
import io.github.ardoco.core.neo4jschema.entities.ner.NamedArchitectureEntityOccurrenceNode;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceLinkType;
import io.github.ardoco.core.neo4jschema.repository.ner.NamedArchitectureEntityOccurrenceRepository;
import io.github.ardoco.core.neo4jschema.repository.ner.NamedArchitectureEntityRepository;

@Service
public class NerPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(NerPersistenceService.class);

    private final NamedArchitectureEntityRepository entityRepository;
    private final NamedArchitectureEntityOccurrenceRepository occurrenceRepository;
    private final Neo4jClient neo4jClient;

    public NerPersistenceService(NamedArchitectureEntityRepository entityRepository, NamedArchitectureEntityOccurrenceRepository occurrenceRepository,
            Neo4jClient neo4jClient) {
        this.entityRepository = entityRepository;
        this.occurrenceRepository = occurrenceRepository;
        this.neo4jClient = neo4jClient;
    }

    @Transactional
    public void saveNamedArchitectureEntity(NamedArchitectureEntity entity, Metamodel metamodel, boolean unlinked) {
        NamedArchitectureEntityNode node = entityRepository.findById(entity.getId()).orElseGet(() -> new NamedArchitectureEntityNode(entity.getId()));
        node.setName(entity.getName());
        node.setMetamodel(metamodel.name());
        node.setUnlinked(unlinked);
        node.setAlternativeNames(new ArrayList<>(entity.getAlternativeNames()));

        List<NamedArchitectureEntityOccurrenceNode> occurrenceNodes = new ArrayList<>();
        for (NamedArchitectureEntityOccurrence occurrence : entity.getOccurrences()) {
            NamedArchitectureEntityOccurrenceNode occNode = occurrenceRepository.findById(occurrence.getId())
                    .orElseGet(() -> new NamedArchitectureEntityOccurrenceNode(occurrence.getId()));
            occNode.setName(occurrence.getName());
            occNode.setSentenceNumber(occurrence.getSentenceNumber());
            occurrenceNodes.add(occurrenceRepository.save(occNode));
        }
        node.setOccurrences(occurrenceNodes);
        entityRepository.save(node);
        logger.debug("Saved NamedArchitectureEntity {} ({})", entity.getId(), entity.getName());
    }

    @Transactional(readOnly = true)
    public boolean hasNamedArchitectureEntities() {
        return entityRepository.count() > 0;
    }

    @Transactional(readOnly = true)
    public Collection<NamedArchitectureEntity> loadNamedArchitectureEntities(Metamodel metamodel, boolean unlinkedOnly) {
        List<NamedArchitectureEntityNode> nodes = unlinkedOnly
                ? entityRepository.findByMetamodelAndUnlinked(metamodel.name(), true)
                : entityRepository.findByMetamodelAndUnlinked(metamodel.name(), false);
        if (!unlinkedOnly) {
            // Also include unlinked when loading "all linked" is false: for hydrate we load both sets separately.
            // When unlinkedOnly is false, load linked entities only (unlinked=false).
        }
        List<NamedArchitectureEntity> result = new ArrayList<>();
        for (NamedArchitectureEntityNode node : nodes) {
            result.add(toDomain(node));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Collection<NamedArchitectureEntity> loadAllNamedArchitectureEntities(Metamodel metamodel) {
        return entityRepository.findByMetamodel(metamodel.name()).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Collection<NamedArchitectureEntityToModelTraceLink> loadNerTraceLinks(Metamodel metamodel,
            SortedMap<String, NamedArchitectureEntityOccurrence> occurrencesById, SortedMap<String, ModelEntity> modelEntitiesById) {
        List<NamedArchitectureEntityToModelTraceLink> links = new ArrayList<>();
        var rows = neo4jClient.query("""
                MATCH (o:NamedArchitectureEntityOccurrence)-[r:TRACES_TO]->(t:Traceable)
                WHERE r.traceLinkType = $type
                  AND EXISTS {
                    MATCH (e:NamedArchitectureEntity)-[:HAS_OCCURRENCE]->(o)
                    WHERE e.metamodel = $metamodel
                  }
                RETURN o.ardocoId AS occId, t.ardocoId AS targetId, coalesce(r.confidence, -1.0) AS confidence
                """)
                .bind(TraceLinkType.NER_ARCHITECTURE.name())
                .to("type")
                .bind(metamodel.name())
                .to("metamodel")
                .fetch()
                .all();
        for (var row : rows) {
            String occId = (String) row.get("occId");
            String targetId = (String) row.get("targetId");
            NamedArchitectureEntityOccurrence occurrence = occurrencesById.get(occId);
            ModelEntity modelEntity = modelEntitiesById.get(targetId);
            if (occurrence != null && modelEntity != null) {
                links.add(new NamedArchitectureEntityToModelTraceLink(occurrence, modelEntity));
            }
        }
        logger.info("Loaded {} NER trace links for {}", links.size(), metamodel);
        return links;
    }

    private NamedArchitectureEntity toDomain(NamedArchitectureEntityNode node) {
        List<NamedArchitectureEntityOccurrence> occurrences = new ArrayList<>();
        if (node.getOccurrences() != null) {
            for (NamedArchitectureEntityOccurrenceNode occ : node.getOccurrences()) {
                occurrences.add(new NamedArchitectureEntityOccurrence(occ.getArdocoId(), occ.getName(), occ.getSentenceNumber()));
            }
        }
        return new NamedArchitectureEntity(node.getArdocoId(), node.getName(), new TreeSet<>(node.getAlternativeNames()), occurrences);
    }
}
