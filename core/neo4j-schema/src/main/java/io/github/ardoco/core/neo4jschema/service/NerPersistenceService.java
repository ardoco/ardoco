/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import io.github.ardoco.core.neo4jschema.repository.ner.NamedArchitectureEntityRepository;

@Service
public class NerPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(NerPersistenceService.class);

    private static final int WRITE_CHUNK_SIZE = 2000;

    private static final String SAVE_ENTITIES_QUERY = """
            UNWIND $rows AS row
            MERGE (n:NamedArchitectureEntity {ardocoId: row.key})
            SET n.name = row.name,
                n.metamodel = row.metamodel,
                n.entityType = row.entityType,
                n.unlinked = row.unlinked,
                n.alternativeNames = row.alternativeNames
            WITH n, row
            OPTIONAL MATCH (n)-[old:HAS_OCCURRENCE]->(o:NamedArchitectureEntityOccurrence)
            WHERE NOT o.ardocoId IN [occurrence IN row.occurrences | occurrence.ardocoId]
            WITH n, row, collect(old) AS stale
            FOREACH (r IN stale | DELETE r)
            FOREACH (occurrence IN row.occurrences |
                MERGE (o:NamedArchitectureEntityOccurrence:Traceable {ardocoId: occurrence.ardocoId})
                SET o.name = occurrence.name,
                    o.sentenceNumber = occurrence.sentenceNumber
                MERGE (n)-[:HAS_OCCURRENCE]->(o))
            """;

    private final NamedArchitectureEntityRepository entityRepository;
    private final Neo4jClient neo4jClient;

    public NerPersistenceService(NamedArchitectureEntityRepository entityRepository, Neo4jClient neo4jClient) {
        this.entityRepository = entityRepository;
        this.neo4jClient = neo4jClient;
    }

    /**
     * Upserts the node of a named architecture entity, keyed by {@link PersistenceBridge#namedArchitectureEntityKey}, with its occurrences. The
     * node keeps the metamodel and the linked/unlinked flag of the last save and gets the target's entity type as {@code entityType}. Same write as
     * {@link #saveNamedArchitectureEntities} for a single entity.
     *
     * @param entity   the entity to save
     * @param target   the ArTEMiS target the entity belongs to
     * @param unlinked true if stored in the unlinked set
     */
    @Transactional
    public void saveNamedArchitectureEntity(NamedArchitectureEntity entity, ArtemisTarget target, boolean unlinked) {
        saveNamedArchitectureEntities(List.of(entity), target, unlinked);
    }

    /**
     * Upserts the nodes of named architecture entities (all for the same target and linked/unlinked bucket) with Cypher in {@code UNWIND} chunks
     * of at most {@value #WRITE_CHUNK_SIZE} entities. Each entity node is merged by its key ({@link PersistenceBridge#namedArchitectureEntityKey})
     * and gets {@code name}, {@code metamodel}, {@code entityType}, {@code unlinked} and {@code alternativeNames} of this save, so the later save
     * wins. Each occurrence is merged as a {@code NamedArchitectureEntityOccurrence:Traceable} node by its id with {@code name} and
     * {@code sentenceNumber}, linked by one {@code HAS_OCCURRENCE}; {@code HAS_OCCURRENCE} relationships to occurrences the entity no longer has are
     * removed. Nothing else is read or written: in particular the occurrences' {@code TRACES_TO} relationships and their targets stay as they are.
     * Spring Data's {@code save} is not used because it loads an occurrence together with its trace-link targets and writes them back; a target
     * without its own node class (a code file) was then written as a second occurrence node with the target's id.
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
        List<Map<String, Object>> rows = new ArrayList<>();
        for (NamedArchitectureEntity entity : entities) {
            List<Map<String, Object>> occurrences = new ArrayList<>();
            for (NamedArchitectureEntityOccurrence occurrence : entity.getOccurrences()) {
                Map<String, Object> occurrenceRow = new LinkedHashMap<>();
                occurrenceRow.put("ardocoId", occurrence.getId());
                occurrenceRow.put("name", occurrence.getName());
                occurrenceRow.put("sentenceNumber", occurrence.getSentenceNumber());
                occurrences.add(occurrenceRow);
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("key", PersistenceBridge.namedArchitectureEntityKey(target, entity));
            row.put("name", entity.getName());
            row.put("metamodel", target.metamodel().name());
            row.put("entityType", target.entityType().name());
            row.put("unlinked", unlinked);
            row.put("alternativeNames", new ArrayList<>(entity.getAlternativeNames()));
            row.put("occurrences", occurrences);
            rows.add(row);
        }
        for (int start = 0; start < rows.size(); start += WRITE_CHUNK_SIZE) {
            neo4jClient.query(SAVE_ENTITIES_QUERY).bind(rows.subList(start, Math.min(start + WRITE_CHUNK_SIZE, rows.size()))).to("rows").run();
        }
        logger.debug("Saved {} NamedArchitectureEntities ({}, unlinked={})", entities.size(), target.getId(), unlinked);
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
