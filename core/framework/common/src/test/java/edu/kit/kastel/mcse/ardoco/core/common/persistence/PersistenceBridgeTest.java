/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.util.Collection;
import java.util.List;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

class PersistenceBridgeTest {

    @AfterEach
    void resetStorage() {
        PersistenceBridge.setHandler(null);
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.empty());
    }

    @Test
    @DisplayName("storage=neo4j enables writes and exposes the registered handler")
    void storageNeo4jEnablesWrites() {
        ThrowingPersistenceHandler throwingHandler = new ThrowingPersistenceHandler();
        PersistenceBridge.setHandler(throwingHandler);

        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));

        Assertions.assertEquals(PersistenceStorage.NEO4J, PersistenceBridge.getStorage());
        Assertions.assertTrue(PersistenceBridge.isNeo4jStorage());
        Assertions.assertSame(throwingHandler, PersistenceBridge.getHandler());
    }

    @Test
    @DisplayName("storage resets to memory when applyConfiguration omits the key, instead of keeping the previous run's value")
    void storageResetsToMemoryWhenAbsent() {
        PersistenceBridge.setHandler(new ThrowingPersistenceHandler());
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));
        Assertions.assertTrue(PersistenceBridge.isNeo4jStorage());

        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.empty());

        Assertions.assertEquals(PersistenceStorage.MEMORY, PersistenceBridge.getStorage());
        Assertions.assertFalse(PersistenceBridge.isNeo4jStorage());
        Assertions.assertNull(PersistenceBridge.getHandler());
    }

    @Test
    @DisplayName("storage=memory never calls the handler")
    void storageMemoryWritesNothing() {
        PersistenceBridge.setHandler(new ThrowingPersistenceHandler());
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "memory"));

        Assertions.assertDoesNotThrow(() -> PersistenceBridge.saveNounMapping(null));
        Assertions.assertDoesNotThrow(() -> PersistenceBridge.deleteNounMapping("id"));
        Assertions.assertDoesNotThrow(() -> PersistenceBridge.saveRecommendedInstance(null, Metamodel.ARCHITECTURE_WITH_COMPONENTS));
        Assertions.assertDoesNotThrow(() -> PersistenceBridge.saveNamedArchitectureEntity(null, Metamodel.ARCHITECTURE_WITH_COMPONENTS, false));
        Assertions.assertDoesNotThrow(() -> PersistenceBridge.saveTraceLinks(List.of(new FakeTraceLink())));
    }

    @Test
    @DisplayName("in storage=neo4j a failing write propagates instead of being swallowed")
    void writeFailurePropagatesInNeo4jStorage() {
        PersistenceBridge.setHandler(new ThrowingPersistenceHandler());
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> PersistenceBridge.saveNounMapping(null));
        Assertions.assertEquals("simulated Neo4j down", exception.getMessage());
    }

    @Test
    @DisplayName("in storage=neo4j a missing handler is an error, never a silently skipped write")
    void missingHandlerInNeo4jStorageIsAnError() {
        PersistenceBridge.setHandler(new ThrowingPersistenceHandler());
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));
        PersistenceBridge.setHandler(null);

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> PersistenceBridge.saveNounMapping(null));
        Assertions.assertTrue(exception.getMessage().contains("PersistenceBridge::storage=neo4j"));
    }

    @Test
    @DisplayName("storage=neo4j without a registered PersistenceHandlerProvider aborts with a clear message")
    void storageNeo4jWithoutProviderAborts() {
        PersistenceBridge.setHandler(null);

        var config = SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j");
        PersistenceBridge bridge = PersistenceBridge.getInstance();
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> bridge.applyConfiguration(config));
        Assertions.assertTrue(exception.getMessage().contains("PersistenceHandlerProvider"));
        Assertions.assertTrue(exception.getMessage().contains("ServiceLoader"));
        Assertions.assertFalse(PersistenceBridge.isNeo4jStorage());
    }

    @Test
    @DisplayName("an invalid storage value is rejected with a clear error instead of silently defaulting")
    void invalidStorageValueIsRejected() {
        var config = SortedMaps.immutable.with("PersistenceBridge::storage", "NEO4J");
        PersistenceBridge bridge = PersistenceBridge.getInstance();
        Assertions.assertThrows(IllegalArgumentException.class, () -> bridge.applyConfiguration(config));
    }

    static List<String> legacyKeys() {
        return PersistenceBridge.LEGACY_KEYS;
    }

    @ParameterizedTest
    @MethodSource("legacyKeys")
    @DisplayName("a removed per-kind persistence flag is rejected with a message naming the key and the storage setting")
    void legacyKeysAreRejected(String legacyKey) {
        PersistenceBridge.setHandler(new ThrowingPersistenceHandler());
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));

        var config = SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j", legacyKey, "true");
        PersistenceBridge bridge = PersistenceBridge.getInstance();
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> bridge.applyConfiguration(config));
        Assertions.assertTrue(exception.getMessage().contains(legacyKey));
        Assertions.assertTrue(exception.getMessage().contains("PersistenceBridge::storage=memory|neo4j"));
        Assertions.assertFalse(PersistenceBridge.isNeo4jStorage(), "a rejected configuration must not leave the previous neo4j mode active");
    }

    private static final class FakeTraceLink extends TraceLink<Entity, Entity> {
        private FakeTraceLink() {
            super(null, null);
        }
    }

    private static final class ThrowingPersistenceHandler implements PersistenceHandler {
        private RuntimeException fail() {
            return new RuntimeException("simulated Neo4j down");
        }

        @Override
        public void saveModel(Metamodel metamodel, Model model) {
            throw fail();
        }

        @Override
        public void savePreprocessedText(Text text, String identifier) {
            throw fail();
        }

        @Override
        public boolean saveTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
            throw fail();
        }

        @Override
        public boolean addInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
            throw fail();
        }

        @Override
        public void deleteModel(Metamodel metamodel) {
            throw fail();
        }

        @Override
        public void deletePreprocessedText(String identifier) {
            throw fail();
        }

        @Override
        public void deleteInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
            throw fail();
        }

        @Override
        public void deleteAllInconsistencies() {
            throw fail();
        }

        @Override
        public void deleteAllData() {
            throw fail();
        }

        @Override
        public void saveNounMapping(NounMapping nounMapping) {
            throw fail();
        }

        @Override
        public void saveNounMappings(Collection<NounMapping> nounMappings) {
            throw fail();
        }

        @Override
        public void deleteNounMapping(String ardocoId) {
            throw fail();
        }

        @Override
        public void saveRecommendedInstance(RecommendedInstance recommendedInstance, Metamodel metamodel) {
            throw fail();
        }

        @Override
        public void saveRecommendedInstances(Collection<RecommendedInstance> recommendedInstances, Metamodel metamodel) {
            throw fail();
        }

        @Override
        public void saveSimpleText(edu.kit.kastel.mcse.ardoco.core.api.text.SimpleText simpleText, String identifier) {
            throw fail();
        }

        @Override
        public void saveProjectMetadata(String projectName, String inputText) {
            throw fail();
        }

        @Override
        public void saveNamedArchitectureEntity(edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity entity,
                Metamodel metamodel, boolean unlinked) {
            throw fail();
        }

        @Override
        public void saveNamedArchitectureEntities(
                Collection<edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity> entities, Metamodel metamodel,
                boolean unlinked) {
            throw fail();
        }

        @Override
        public void saveRunMetadataStart(String projectName, String startTimestamp, String configurationJson) {
            throw fail();
        }

        @Override
        public void saveRunMetadataFinish(String projectName, String finishTimestamp) {
            throw fail();
        }

        @Override
        public String exportGraphAsJson() {
            throw fail();
        }
    }
}
