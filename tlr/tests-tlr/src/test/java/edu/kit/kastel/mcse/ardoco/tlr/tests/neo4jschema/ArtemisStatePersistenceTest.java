/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeFile;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityToModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisConnectionStatesImpl;
import io.github.ardoco.core.neo4jschema.Neo4jPersistenceHandler;

/**
 * Checks that the ArTEMiS connection states store their named entities, their unlinked entities and their trace links in neo4j storage mode, for an
 * architecture target and two code targets (classes and code files), and that they write nothing in memory mode. The states are filled the way the
 * NER step and the connection step fill them, each step in its own deferred-write scope.
 */
class ArtemisStatePersistenceTest extends AbstractPersistenceTest implements Claimant {

    private static final ArtemisTarget COMPONENT_TARGET = new ArtemisTarget(Metamodel.ARCHITECTURE_WITH_COMPONENTS, NamedEntityType.COMPONENT);
    private static final ArtemisTarget CLASS_TARGET = new ArtemisTarget(Metamodel.CODE_WITH_COMPILATION_UNITS, NamedEntityType.CLASS);
    private static final ArtemisTarget DATAFILE_TARGET = new ArtemisTarget(Metamodel.CODE_WITH_COMPILATION_UNITS, NamedEntityType.DATAFILE);

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Test
    @DisplayName("Neo4j mode: named entities, unlinked flags, occurrences and NER_ARCHITECTURE/NER_CODE links of three targets are stored")
    void statesStoreEntitiesAndLinks() {
        ArchitectureComponent component = new ArchitectureComponent("AuthService", "id_as_comp", new TreeSet<>(), new TreeSet<>(), new TreeSet<>(), "Service");
        CodeModelWithCompilationUnits codeModel = createCodeModel();
        ClassUnit classUnit = (ClassUnit) codeModel.createCodeModelDto().codeItemRepository().getCodeItem("id_as_cls");
        CodeFile codeFile = codeModel.getCodeFiles().getFirst();

        Fixture fixture = new Fixture();
        try {
            PersistenceBridge.setHandler(persistenceHandler);
            PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));
            persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS, new ArchitectureModelWithComponentsAndInterfaces(List.<ArchitectureItem>of(
                    component)));
            persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS, codeModel);

            fillStates(fixture, component, classUnit, codeFile);
        } finally {
            PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "memory"));
            PersistenceBridge.setHandler(null);
        }

        Assertions.assertEquals(6L, count("MATCH (n:NamedArchitectureEntity) RETURN count(n) AS c", Map.of()));
        assertEntity(COMPONENT_TARGET, fixture.componentEntity, false);
        assertEntity(COMPONENT_TARGET, fixture.componentUnlinked, true);
        assertEntity(CLASS_TARGET, fixture.classEntity, false);
        assertEntity(CLASS_TARGET, fixture.classUnlinked, true);
        assertEntity(DATAFILE_TARGET, fixture.datafileEntity, false);
        assertEntity(DATAFILE_TARGET, fixture.datafileUnlinked, true);

        Assertions.assertEquals(1L, countLinks("NER_ARCHITECTURE"));
        Assertions.assertEquals(2L, countLinks("NER_CODE"));
        Assertions.assertEquals(0.91, linkConfidence("NER_ARCHITECTURE", component.getId()));
        Assertions.assertEquals(0.82, linkConfidence("NER_CODE", classUnit.getId()));
        Assertions.assertEquals(0.73, linkConfidence("NER_CODE", codeFile.getId()));
    }

    @Test
    @DisplayName("Memory mode: the same state calls write no named entity and no NER link")
    void memoryModeWritesNothing() {
        ArchitectureComponent component = new ArchitectureComponent("AuthService", "id_as_comp", new TreeSet<>(), new TreeSet<>(), new TreeSet<>(), "Service");
        CodeModelWithCompilationUnits codeModel = createCodeModel();
        ClassUnit classUnit = (ClassUnit) codeModel.createCodeModelDto().codeItemRepository().getCodeItem("id_as_cls");
        CodeFile codeFile = codeModel.getCodeFiles().getFirst();

        try {
            PersistenceBridge.setHandler(persistenceHandler);
            PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "memory"));
            fillStates(new Fixture(), component, classUnit, codeFile);
        } finally {
            PersistenceBridge.setHandler(null);
        }

        Assertions.assertEquals(0L, count("MATCH (n:NamedArchitectureEntity) RETURN count(n) AS c", Map.of()));
        Assertions.assertEquals(0L, count("MATCH (n:NamedArchitectureEntityOccurrence) RETURN count(n) AS c", Map.of()));
        Assertions.assertEquals(0L, count("MATCH ()-[r:TRACES_TO]->() RETURN count(r) AS c", Map.of()));
    }

    private void fillStates(Fixture fixture, ArchitectureComponent component, ClassUnit classUnit, CodeFile codeFile) {
        ArtemisConnectionStatesImpl states = ArtemisConnectionStatesImpl.build(List.of(COMPONENT_TARGET, CLASS_TARGET, DATAFILE_TARGET));
        ArtemisConnectionState componentState = states.getState(COMPONENT_TARGET);
        ArtemisConnectionState classState = states.getState(CLASS_TARGET);
        ArtemisConnectionState datafileState = states.getState(DATAFILE_TARGET);

        PersistenceBridge.withDeferredWrites("ArtemisNer", () -> {
            componentState.addNamedEntities(Lists.mutable.with(fixture.componentEntity, fixture.componentUnlinked));
            classState.addNamedEntities(Lists.mutable.with(fixture.classEntity, fixture.classUnlinked));
            datafileState.addNamedEntities(Lists.mutable.with(fixture.datafileEntity, fixture.datafileUnlinked));
            return null;
        });
        PersistenceBridge.withDeferredWrites("ArtemisConnectionGenerator", () -> {
            componentState.addTraceLinks(Lists.mutable.<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>>with(
                    new NamedArchitectureEntityToModelTraceLink(fixture.componentEntity.getOccurrences().getFirst(), component, this, 0.91)));
            componentState.addUnlinkedNamedEntities(Lists.mutable.with(fixture.componentUnlinked));
            classState.addTraceLinks(Lists.mutable.<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>>with(new NamedArchitectureEntityToModelTraceLink(
                    fixture.classEntity.getOccurrences().getFirst(), classUnit, this, 0.82)));
            classState.addUnlinkedNamedEntities(Lists.mutable.with(fixture.classUnlinked));
            datafileState.addTraceLinks(Lists.mutable.<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>>with(
                    new NamedArchitectureEntityToModelTraceLink(fixture.datafileEntity.getOccurrences().getFirst(), codeFile, this, 0.73)));
            datafileState.addUnlinkedNamedEntities(Lists.mutable.with(fixture.datafileUnlinked));
            return null;
        });
    }

    private void assertEntity(ArtemisTarget target, NamedArchitectureEntity entity, boolean unlinked) {
        String key = PersistenceBridge.namedArchitectureEntityKey(target, entity);
        Map<String, Object> row = neo4jClient.query("""
                MATCH (n:NamedArchitectureEntity {ardocoId: $id})
                OPTIONAL MATCH (n)-[:HAS_OCCURRENCE]->(o:NamedArchitectureEntityOccurrence)
                RETURN n.metamodel AS metamodel, n.entityType AS entityType, n.unlinked AS unlinked, collect(o.ardocoId) AS occurrences
                """).bindAll(Map.of("id", key)).fetch().one().orElseThrow(() -> new AssertionError("no NamedArchitectureEntity node for " + key));
        Assertions.assertEquals(target.metamodel().name(), row.get("metamodel"), key);
        Assertions.assertEquals(target.entityType().name(), row.get("entityType"), key);
        Assertions.assertEquals(unlinked, row.get("unlinked"), key);
        Assertions.assertEquals(List.of(entity.getOccurrences().getFirst().getId()), row.get("occurrences"), key);
    }

    private long countLinks(String type) {
        return count("MATCH (:NamedArchitectureEntityOccurrence)-[r:TRACES_TO]->() WHERE r.traceLinkType = $type RETURN count(r) AS c", Map.of("type", type));
    }

    private double linkConfidence(String type, String targetId) {
        return neo4jClient.query("""
                MATCH (:NamedArchitectureEntityOccurrence)-[r:TRACES_TO]->(t:Traceable {ardocoId: $id})
                WHERE r.traceLinkType = $type
                RETURN r.confidence AS c
                """).bindAll(Map.of("id", targetId, "type", type)).fetch().one().map(row -> ((Number) row.get("c")).doubleValue()).orElse(-1.0);
    }

    private long count(String query, Map<String, Object> parameters) {
        return neo4jClient.query(query).bindAll(parameters).fetch().one().map(row -> ((Number) row.get("c")).longValue()).orElse(-1L);
    }

    private static CodeModelWithCompilationUnits createCodeModel() {
        CodeItemRepository repository = new CodeItemRepository();
        CodeCompilationUnit compilationUnit = new CodeCompilationUnit("id_as_cu", repository, "AuthController", new TreeSet<>(), List.of("src", "auth"), "java",
                ProgrammingLanguage.JAVA);
        ClassUnit classUnit = new ClassUnit("id_as_cls", repository, "AuthController", new TreeSet<>());
        compilationUnit.addContent(classUnit);
        classUnit.setCompilationUnit(compilationUnit);
        List<CodeFile> codeFiles = List.of(new CodeFile("README", List.of("docs"), "md", null));
        return new CodeModelWithCompilationUnits(new CodeModel.CodeModelDto("artemis_state_code_model", repository, List.of("id_as_cu"), codeFiles));
    }

    private static final class Fixture {
        private final NamedArchitectureEntity componentEntity = entity("AuthService", 1);
        private final NamedArchitectureEntity componentUnlinked = entity("Logger", 4);
        private final NamedArchitectureEntity classEntity = entity("AuthController", 2);
        private final NamedArchitectureEntity classUnlinked = entity("Cache", 5);
        private final NamedArchitectureEntity datafileEntity = entity("README.md", 3);
        private final NamedArchitectureEntity datafileUnlinked = entity("config.yml", 6);

        private static NamedArchitectureEntity entity(String name, int sentenceNumber) {
            return new NamedArchitectureEntity(name, new TreeSet<>(), List.of(new NamedArchitectureEntityOccurrence(name, sentenceNumber)));
        }
    }
}
