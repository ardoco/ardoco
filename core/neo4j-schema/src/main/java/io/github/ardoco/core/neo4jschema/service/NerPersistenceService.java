/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityToModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import io.github.ardoco.core.neo4jschema.entities.ner.NamedArchitectureEntityNode;
import io.github.ardoco.core.neo4jschema.entities.ner.NamedArchitectureEntityOccurrenceNode;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceLinkType;
import io.github.ardoco.core.neo4jschema.mapper.NounMappingMapper;
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

    /**
     * Upserts the node of a named architecture entity, keyed by {@link PersistenceBridge#namedArchitectureEntityKey}, with its occurrences. The
     * node keeps the metamodel and the linked/unlinked flag of the last save and gets the target's entity type as {@code entityType}.
     *
     * @param entity   the entity to save
     * @param target   the ArTEMiS target the entity belongs to
     * @param unlinked true if stored in the unlinked set
     */
    @Transactional
    public void saveNamedArchitectureEntity(NamedArchitectureEntity entity, ArtemisTarget target, boolean unlinked) {
        String key = PersistenceBridge.namedArchitectureEntityKey(target, entity);
        NamedArchitectureEntityNode node = entityRepository.findById(key).orElseGet(() -> new NamedArchitectureEntityNode(key));
        node.setName(entity.getName());
        node.setMetamodel(target.metamodel().name());
        node.setEntityType(target.entityType().name());
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
        logger.debug("Saved NamedArchitectureEntity {} ({})", key, entity.getName());
    }

    /**
     * Batched form of {@link #saveNamedArchitectureEntity}: dual-writes every entity (all for the same target and linked/unlinked bucket) in one
     * transaction instead of one per entity. Used by the stage-level write-behind ({@code PersistenceBridge::withDeferredWrites}). NER entities
     * carry a nested occurrence subgraph, so this still upserts per entity internally; the batching is at the transaction/round-trip level, not a
     * single {@code UNWIND} query like the NounMapping/RecommendedInstance batches.
     *
     * @param entities the entities to save; a no-op if empty
     * @param target   the ArTEMiS target (metamodel and entity type) of the bucket
     * @param unlinked true if stored in the unlinked set
     */
    @Transactional
    public void saveNamedArchitectureEntities(Collection<NamedArchitectureEntity> entities, ArtemisTarget target, boolean unlinked) {
        if (entities.isEmpty()) {
            return;
        }
        for (NamedArchitectureEntity entity : entities) {
            saveNamedArchitectureEntity(entity, target, unlinked);
        }
        logger.debug("Saved {} NamedArchitectureEntities in one batch ({}, unlinked={})", entities.size(), target.getId(), unlinked);
    }

    @Transactional(readOnly = true)
    public boolean hasNamedArchitectureEntities() {
        return entityRepository.count() > 0;
    }

    @Transactional(readOnly = true)
    public Collection<NamedArchitectureEntity> loadNamedArchitectureEntities(Metamodel metamodel, boolean unlinkedOnly) {
        List<NamedArchitectureEntityNode> nodes = unlinkedOnly ?
                entityRepository.findByMetamodelAndUnlinked(metamodel.name(), true) :
                entityRepository.findByMetamodelAndUnlinked(metamodel.name(), false);
        List<NamedArchitectureEntity> result = new ArrayList<>();
        for (NamedArchitectureEntityNode node : nodes) {
            result.add(toDomain(node));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Collection<NamedArchitectureEntity> loadAllNamedArchitectureEntities(Metamodel metamodel) {
        return entityRepository.findByMetamodel(metamodel.name()).stream().map(this::toDomain).toList();
    }

    /**
     * Loads the named-entity trace links ({@code NER_ARCHITECTURE} and {@code NER_CODE}) whose occurrence belongs to a named entity of the given
     * metamodel. Links whose occurrence or target is not in the given maps are skipped.
     *
     * @param metamodel         the metamodel of the named entities
     * @param occurrencesById   the occurrences, by id
     * @param modelEntitiesById the possible targets (architecture items, code items, code files), by id
     * @return the loaded trace links
     */
    @Transactional(readOnly = true)
    public Collection<NamedArchitectureEntityToModelTraceLink> loadNerTraceLinks(Metamodel metamodel,
            SortedMap<String, NamedArchitectureEntityOccurrence> occurrencesById, SortedMap<String, ModelEntity> modelEntitiesById) {
        List<NamedArchitectureEntityToModelTraceLink> links = new ArrayList<>();
        var rows = neo4jClient.query("""
                MATCH (o:NamedArchitectureEntityOccurrence)-[r:TRACES_TO]->(t:Traceable)
                WHERE r.traceLinkType IN $types
                  AND EXISTS {
                    MATCH (e:NamedArchitectureEntity)-[:HAS_OCCURRENCE]->(o)
                    WHERE e.metamodel = $metamodel
                  }
                RETURN o.ardocoId AS occId, t.ardocoId AS targetId, coalesce(r.confidence, -1.0) AS confidence
                """)
                .bind(List.of(TraceLinkType.NER_ARCHITECTURE.name(), TraceLinkType.NER_CODE.name()))
                .to("types")
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
                double confidence = row.get("confidence") instanceof Number number ? number.doubleValue() : -1.0;
                links.add(confidence >= 0 ?
                        new NamedArchitectureEntityToModelTraceLink(occurrence, modelEntity, NounMappingMapper.RESTORE_CLAIMANT, confidence) :
                        new NamedArchitectureEntityToModelTraceLink(occurrence, modelEntity));
            }
        }
        logger.info("Loaded {} NER trace links for {}", links.size(), metamodel);
        return links;
    }

    private NamedArchitectureEntity toDomain(NamedArchitectureEntityNode node) {
        List<NamedArchitectureEntityOccurrence> occurrences = new ArrayList<>();
        if (node.getOccurrences() != null) {
            for (NamedArchitectureEntityOccurrenceNode occ : node.getOccurrences()) {
                occurrences.add(new NamedArchitectureEntityOccurrence(occ.getName(), occ.getSentenceNumber()));
            }
        }
        return new NamedArchitectureEntity(node.getName(), new TreeSet<>(node.getAlternativeNames()), occurrences);
    }
}
