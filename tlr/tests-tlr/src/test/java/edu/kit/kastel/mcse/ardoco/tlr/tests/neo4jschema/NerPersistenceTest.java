/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.kit.kastel.mcse.ardoco.core.api.SimplePreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeFile;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityToModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.PlainSimpleText;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.UmlExtractor;
import io.github.ardoco.core.neo4jschema.Neo4jPersistenceHandler;
import io.github.ardoco.core.neo4jschema.mapper.NounMappingMapper;

/**
 * Unit-style Neo4j Desktop tests for NER dual-write / load APIs (Phase 3).
 * Full Artemis LLM pipeline tests are optional and flag-gated separately.
 */
class NerPersistenceTest extends AbstractPersistenceTest {

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Test
    @DisplayName("NER entity save and load round-trip")
    void testNerEntityRoundTrip() {
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance()
                .applyConfiguration(org.eclipse.collections.api.factory.SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));

        var occurrence = new NamedArchitectureEntityOccurrence("WebUI", 2);
        var entity = new NamedArchitectureEntity("WebUI", new TreeSet<>(List.of("UI")), List.of(occurrence));
        Metamodel mm = Metamodel.ARCHITECTURE_WITH_COMPONENTS;
        ArtemisTarget target = new ArtemisTarget(mm, NamedEntityType.COMPONENT);

        persistenceHandler.saveNamedArchitectureEntity(entity, target, false);
        assertThat(persistenceHandler.hasNerNamedArchitectureEntities()).isTrue();

        var loaded = persistenceHandler.loadNamedArchitectureEntities(mm, false);
        assertThat(loaded).hasSize(1);
        NamedArchitectureEntity restored = loaded.iterator().next();
        assertThat(restored).isEqualTo(entity);
        assertThat(restored.getName()).isEqualTo("WebUI");
        assertThat(restored.getOccurrences()).hasSize(1);
        assertThat(restored.getOccurrences().get(0).getSentenceNumber()).isEqualTo(2);

        persistenceHandler.saveSimpleText(new PlainSimpleText("line1\nline2", List.of("line1", "line2")), SimplePreprocessingData.ID);
        assertThat(persistenceHandler.hasSimpleText(SimplePreprocessingData.ID)).isTrue();

        pauseForNeo4jInspection();
    }

    @Test
    @DisplayName("NER_ARCHITECTURE trace link save and load round-trip")
    void testNerArchitectureTraceLinkRoundTrip() {
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance()
                .applyConfiguration(org.eclipse.collections.api.factory.SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));

        Metamodel mm = Metamodel.ARCHITECTURE_WITH_COMPONENTS;
        UmlExtractor extractor = new UmlExtractor(this.inputModelArchitectureUml, mm);
        ArchitectureModel architectureModel = extractor.extractModel();
        persistenceHandler.saveModel(mm, architectureModel);
        ArchitectureComponent component = (ArchitectureComponent) architectureModel.getEndpoints().get(0);

        var occurrence = new NamedArchitectureEntityOccurrence("WebUI", 2);
        var entity = new NamedArchitectureEntity("WebUI", new TreeSet<>(List.of("UI")), List.of(occurrence));
        persistenceHandler.saveNamedArchitectureEntity(entity, new ArtemisTarget(mm, NamedEntityType.COMPONENT), false);

        var link = new NamedArchitectureEntityToModelTraceLink(occurrence, component);
        assertThat(persistenceHandler.saveTraceLinks(List.of(link))).isTrue();

        long stored = countNerArchitectureLinks();
        assertThat(stored).isEqualTo(1L);

        SortedMap<String, NamedArchitectureEntityOccurrence> occurrencesById = new TreeMap<>();
        occurrencesById.put(occurrence.getId(), occurrence);
        SortedMap<String, ModelEntity> modelEntitiesById = new TreeMap<>();
        modelEntitiesById.put(component.getId(), component);

        var loaded = persistenceHandler.loadNerTraceLinks(mm, occurrencesById, modelEntitiesById);
        assertThat(loaded).hasSize(1);
        assertThat(loaded.iterator().next().getSecondEndpoint().getId()).isEqualTo(component.getId());

        pauseForNeo4jInspection();
    }

    @Test
    @DisplayName("NER_ARCHITECTURE link confidence round-trips through the batched save path")
    void testNerArchitectureTraceLinkConfidenceRoundTrip() {
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance()
                .applyConfiguration(org.eclipse.collections.api.factory.SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));

        Metamodel mm = Metamodel.ARCHITECTURE_WITH_COMPONENTS;
        UmlExtractor extractor = new UmlExtractor(this.inputModelArchitectureUml, mm);
        ArchitectureModel architectureModel = extractor.extractModel();
        persistenceHandler.saveModel(mm, architectureModel);
        ArchitectureComponent component = (ArchitectureComponent) architectureModel.getEndpoints().get(0);

        var occurrence = new NamedArchitectureEntityOccurrence("WebUI", 2);
        var entity = new NamedArchitectureEntity("WebUI", new TreeSet<>(List.of("UI")), List.of(occurrence));
        persistenceHandler.saveNamedArchitectureEntities(List.of(entity), new ArtemisTarget(mm, NamedEntityType.COMPONENT), false);

        double confidence = 0.73;
        var link = new NamedArchitectureEntityToModelTraceLink(occurrence, component, NounMappingMapper.RESTORE_CLAIMANT, confidence);
        assertThat(persistenceHandler.saveTraceLinks(List.of(link))).isTrue();

        double storedConfidence = neo4jClient.query("""
                MATCH (:NamedArchitectureEntityOccurrence)-[r:TRACES_TO]->(:Traceable)
                WHERE r.traceLinkType = 'NER_ARCHITECTURE'
                RETURN r.confidence AS c
                """).fetch().one().map(row -> ((Number) row.get("c")).doubleValue()).orElse(-1.0);
        assertThat(storedConfidence).isEqualTo(confidence);

        SortedMap<String, NamedArchitectureEntityOccurrence> occurrencesById = new TreeMap<>();
        occurrencesById.put(occurrence.getId(), occurrence);
        SortedMap<String, ModelEntity> modelEntitiesById = new TreeMap<>();
        modelEntitiesById.put(component.getId(), component);

        var loaded = persistenceHandler.loadNerTraceLinks(mm, occurrencesById, modelEntitiesById);
        assertThat(loaded).hasSize(1);
        assertThat(loaded.iterator().next().getConfidence()).isEqualTo(confidence);

        pauseForNeo4jInspection();
    }

    @Test
    @DisplayName("Unlinked NamedArchitectureEntity is stored as unlinked, has no NER_ARCHITECTURE edge, and loads back as unlinked")
    void testUnlinkedNerEntityRoundTrip() {
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance()
                .applyConfiguration(org.eclipse.collections.api.factory.SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));

        Metamodel mm = Metamodel.ARCHITECTURE_WITH_COMPONENTS;
        ArtemisTarget target = new ArtemisTarget(mm, NamedEntityType.COMPONENT);
        var occurrence = new NamedArchitectureEntityOccurrence("Logger", 5);
        var entity = new NamedArchitectureEntity("Logger", new TreeSet<>(List.of("Log")), List.of(occurrence));

        persistenceHandler.saveNamedArchitectureEntities(List.of(entity), target, true);

        Boolean storedUnlinked = neo4jClient.query("MATCH (n:NamedArchitectureEntity {ardocoId: $id}) RETURN n.unlinked AS unlinked")
                .bindAll(Map.of("id", PersistenceBridge.namedArchitectureEntityKey(target, entity)))
                .fetch()
                .one()
                .map(row -> (Boolean) row.get("unlinked"))
                .orElse(null);
        assertThat(storedUnlinked).isTrue();

        String storedEntityType = neo4jClient.query("MATCH (n:NamedArchitectureEntity {ardocoId: $id}) RETURN n.entityType AS entityType")
                .bindAll(Map.of("id", "ARCHITECTURE_WITH_COMPONENTS:COMPONENT:Logger"))
                .fetch()
                .one()
                .map(row -> (String) row.get("entityType"))
                .orElse(null);
        assertThat(storedEntityType).isEqualTo("COMPONENT");

        long nerEdges = countNerArchitectureLinks();
        assertThat(nerEdges).isZero();

        var loadedUnlinked = persistenceHandler.loadNamedArchitectureEntities(mm, true);
        assertThat(loadedUnlinked).hasSize(1);
        assertThat(loadedUnlinked.iterator().next().getName()).isEqualTo("Logger");

        var loadedLinked = persistenceHandler.loadNamedArchitectureEntities(mm, false);
        assertThat(loadedLinked).isEmpty();

        pauseForNeo4jInspection();
    }

    @Test
    @DisplayName("NER_CODE trace links to a class and to a code file are stored with their confidence and load back")
    void testNerCodeTraceLinkRoundTrip() {
        Metamodel mm = Metamodel.CODE_WITH_COMPILATION_UNITS;
        CodeItemRepository repository = new CodeItemRepository();
        CodeCompilationUnit compilationUnit = new CodeCompilationUnit("id_ner_cu", repository, "AuthController", new TreeSet<>(), List.of("src"), "java",
                ProgrammingLanguage.JAVA);
        ClassUnit classUnit = new ClassUnit("id_ner_cls", repository, "AuthController", new TreeSet<>());
        compilationUnit.addContent(classUnit);
        classUnit.setCompilationUnit(compilationUnit);
        CodeFile codeFile = new CodeFile("README", List.of("docs"), "md", null);
        persistenceHandler.saveModel(mm, new CodeModelWithCompilationUnits(new CodeModel.CodeModelDto("ner_code_model", repository, List.of("id_ner_cu"), List
                .of(codeFile))));

        var classOccurrence = new NamedArchitectureEntityOccurrence("AuthController", 1);
        var fileOccurrence = new NamedArchitectureEntityOccurrence("README.md", 2);
        persistenceHandler.saveNamedArchitectureEntity(new NamedArchitectureEntity("AuthController", new TreeSet<>(), List.of(classOccurrence)),
                new ArtemisTarget(mm, NamedEntityType.CLASS), false);
        persistenceHandler.saveNamedArchitectureEntity(new NamedArchitectureEntity("README.md", new TreeSet<>(), List.of(fileOccurrence)), new ArtemisTarget(mm,
                NamedEntityType.DATAFILE), false);
        assertThat(persistenceHandler.saveTraceLinks(List.of(new NamedArchitectureEntityToModelTraceLink(classOccurrence, classUnit,
                NounMappingMapper.RESTORE_CLAIMANT, 0.6), new NamedArchitectureEntityToModelTraceLink(fileOccurrence, codeFile,
                        NounMappingMapper.RESTORE_CLAIMANT, 0.7)))).isTrue();

        long nerCodeEdges = neo4jClient.query("""
                MATCH (:NamedArchitectureEntityOccurrence)-[r:TRACES_TO]->(:Traceable)
                WHERE r.traceLinkType = 'NER_CODE'
                RETURN count(r) AS c
                """).fetch().one().map(row -> ((Number) row.get("c")).longValue()).orElse(0L);
        assertThat(nerCodeEdges).isEqualTo(2L);

        SortedMap<String, NamedArchitectureEntityOccurrence> occurrencesById = new TreeMap<>();
        occurrencesById.put(classOccurrence.getId(), classOccurrence);
        occurrencesById.put(fileOccurrence.getId(), fileOccurrence);
        SortedMap<String, ModelEntity> modelEntitiesById = new TreeMap<>();
        modelEntitiesById.put(classUnit.getId(), classUnit);
        modelEntitiesById.put(codeFile.getId(), codeFile);

        var loaded = persistenceHandler.loadNerTraceLinks(mm, occurrencesById, modelEntitiesById);
        assertThat(loaded).hasSize(2);
        Map<String, Double> confidenceByTarget = new TreeMap<>();
        for (var link : loaded) {
            confidenceByTarget.put(link.getSecondEndpoint().getId(), link.getConfidence());
        }
        assertThat(confidenceByTarget).isEqualTo(Map.of(classUnit.getId(), 0.6, codeFile.getId(), 0.7));
    }

    private long countNerArchitectureLinks() {
        return neo4jClient.query("""
                MATCH (:NamedArchitectureEntityOccurrence)-[r:TRACES_TO]->(:Traceable)
                WHERE r.traceLinkType = 'NER_ARCHITECTURE'
                RETURN count(r) AS c
                """).fetch().one().map(row -> ((Number) row.get("c")).longValue()).orElse(0L);
    }
}
