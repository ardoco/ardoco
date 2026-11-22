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
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
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
 * Saves the same named entity twice through the ArTEMiS connection state in one deferred-write scope, once as linked and once as unlinked, the
 * way the NER step can, and then links its occurrence to a code item in a second scope. The graph must hold one entity node (the later save wins,
 * so it is unlinked), one occurrence node with one {@code HAS_OCCURRENCE}, and the {@code NER_CODE} link.
 */
class ArtemisNamedEntityResaveTest extends AbstractPersistenceTest implements Claimant {

    private static final ArtemisTarget CLASS_TARGET = new ArtemisTarget(Metamodel.CODE_WITH_COMPILATION_UNITS, NamedEntityType.CLASS);

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Test
    @DisplayName("One entity saved as linked and as unlinked in one scope, then linked to a code item: one entity, one occurrence, one link")
    void entitySavedTwiceKeepsOneOccurrence() {
        CodeModelWithCompilationUnits codeModel = createCodeModel();
        ClassUnit classUnit = (ClassUnit) codeModel.createCodeModelDto().codeItemRepository().getCodeItem("id_nr_cls");
        NamedArchitectureEntityOccurrence occurrence = new NamedArchitectureEntityOccurrence("OrderController", 2);
        NamedArchitectureEntity entity = new NamedArchitectureEntity("OrderController", new TreeSet<>(), List.of(occurrence));

        try {
            PersistenceBridge.setHandler(persistenceHandler);
            PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));
            persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS, codeModel);
            ArtemisConnectionState state = ArtemisConnectionStatesImpl.build(List.of(CLASS_TARGET)).getState(CLASS_TARGET);

            PersistenceBridge.withDeferredWrites("ArtemisNer", () -> {
                state.addNamedEntities(Lists.mutable.with(entity));
                state.addUnlinkedNamedEntities(Lists.mutable.with(entity));
                return null;
            });
            PersistenceBridge.withDeferredWrites("ArtemisConnectionGenerator", () -> {
                state.addTraceLinks(Lists.mutable.<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>>with(new NamedArchitectureEntityToModelTraceLink(
                        occurrence, classUnit, this, 0.8)));
                return null;
            });
        } finally {
            PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "memory"));
            PersistenceBridge.setHandler(null);
        }

        String key = PersistenceBridge.namedArchitectureEntityKey(CLASS_TARGET, entity);
        Assertions.assertEquals(1L, count("MATCH (n:NamedArchitectureEntity) RETURN count(n) AS c", Map.of()));
        Assertions.assertEquals(1L, count("MATCH (n:NamedArchitectureEntity {ardocoId: $id}) WHERE n.unlinked = true RETURN count(n) AS c", Map.of("id", key)));
        Assertions.assertEquals(1L, count("MATCH (o:NamedArchitectureEntityOccurrence) RETURN count(o) AS c", Map.of()));
        Assertions.assertEquals(1L, count(
                "MATCH (:NamedArchitectureEntity {ardocoId: $id})-[r:HAS_OCCURRENCE]->(:NamedArchitectureEntityOccurrence {ardocoId: $occ}) RETURN count(r) AS c",
                Map.of("id", key, "occ", occurrence.getId())));
        Assertions.assertEquals(1L, count("MATCH ()-[r:HAS_OCCURRENCE]->() RETURN count(r) AS c", Map.of()));
        Assertions.assertEquals(1L, count("""
                MATCH (:NamedArchitectureEntityOccurrence {ardocoId: $occ})-[r:TRACES_TO {traceLinkType: 'NER_CODE'}]->(:ClassUnit {ardocoId: $target})
                RETURN count(r) AS c
                """, Map.of("occ", occurrence.getId(), "target", classUnit.getId())));
        Assertions.assertEquals(1L, count("MATCH ()-[r:TRACES_TO]->() RETURN count(r) AS c", Map.of()));
    }

    private long count(String query, Map<String, Object> parameters) {
        return neo4jClient.query(query).bindAll(parameters).fetch().one().map(row -> ((Number) row.get("c")).longValue()).orElse(-1L);
    }

    private static CodeModelWithCompilationUnits createCodeModel() {
        CodeItemRepository repository = new CodeItemRepository();
        CodeCompilationUnit compilationUnit = new CodeCompilationUnit("id_nr_cu", repository, "OrderController", new TreeSet<>(), List.of("src", "order"),
                "java", ProgrammingLanguage.JAVA);
        ClassUnit classUnit = new ClassUnit("id_nr_cls", repository, "OrderController", new TreeSet<>());
        compilationUnit.addContent(classUnit);
        classUnit.setCompilationUnit(compilationUnit);
        return new CodeModelWithCompilationUnits(new CodeModel.CodeModelDto("artemis_resave_code_model", repository, List.of("id_nr_cu"), List.of()));
    }
}
