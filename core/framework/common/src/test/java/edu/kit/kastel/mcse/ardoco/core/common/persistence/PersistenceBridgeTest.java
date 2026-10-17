/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

/**
 * Unit tests for {@link PersistenceBridge} configuration and fault isolation.
 */
class PersistenceBridgeTest {

    @AfterEach
    void resetFlags() {
        PersistenceBridge.setFailOnPersistenceError(false);
        PersistenceBridge.setHandler(null);
        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "false", "PersistenceBridge::persistTextState", "false",
                        "PersistenceBridge::persistRecommendations", "false", "PersistenceBridge::persistNerConnection", "false"));
    }

    @Test
    @DisplayName("persistRecommendations without persistTextState auto-enables TextState persistence")
    void persistRecommendationsImpliesPersistTextState() {
        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true", "PersistenceBridge::persistTextState", "false",
                        "PersistenceBridge::persistRecommendations", "true"));

        Assertions.assertTrue(PersistenceBridge.persistTextStateStatic);
        Assertions.assertTrue(PersistenceBridge.persistRecommendationsStatic);
    }

    @Test
    @DisplayName("persistTextState alone does not enable recommendations")
    void persistTextStateDoesNotEnableRecommendations() {
        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true", "PersistenceBridge::persistTextState", "true",
                        "PersistenceBridge::persistRecommendations", "false"));

        Assertions.assertTrue(PersistenceBridge.persistTextStateStatic);
        Assertions.assertFalse(PersistenceBridge.persistRecommendationsStatic);
    }

    @Test
    @DisplayName("runQuietly swallows Neo4j RuntimeExceptions and continues")
    void runQuietlyIsolatesFailures() {
        AtomicBoolean ran = new AtomicBoolean(false);
        Assertions.assertDoesNotThrow(() -> PersistenceBridge.runQuietly("test-op", () -> {
            ran.set(true);
            throw new RuntimeException("simulated Neo4j down");
        }));
        Assertions.assertTrue(ran.get());
    }

    @Test
    @DisplayName("callQuietly rethrows when fail-on-persistence-error is enabled")
    void callQuietlyRethrowsInStrictMode() {
        PersistenceBridge.setFailOnPersistenceError(true);
        Assertions.assertThrows(RuntimeException.class, () -> PersistenceBridge.callQuietly("test-op", () -> {
            throw new RuntimeException("simulated Neo4j down");
        }, "fallback"));
    }

    @Test
    @DisplayName("callQuietly returns fallback when Neo4j throws")
    void callQuietlyReturnsFallback() {
        String result = PersistenceBridge.callQuietly("test-op", () -> {
            throw new RuntimeException("simulated Neo4j down");
        }, "fallback");
        Assertions.assertEquals("fallback", result);
    }

    @Test
    @DisplayName("failing handler does not abort dual-write style saveNounMapping call site")
    void failingHandlerDoesNotAbortDualWrite() {
        PersistenceBridge.setHandler(new ThrowingPersistenceHandler());
        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true", "PersistenceBridge::persistTextState", "true",
                        "PersistenceBridge::persistRecommendations", "false"));

        Assertions.assertDoesNotThrow(() -> PersistenceBridge.runQuietly("saveNounMapping", () -> PersistenceBridge.getHandler().saveNounMapping(null)));
    }

    @Test
    @DisplayName("storage=neo4j forces all write flags on and fail-fast, even if an explicit flag was set to false")
    void storageNeo4jDerivesAllFlags() {
        PersistenceBridge.setHandler(new ThrowingPersistenceHandler()); // handler already registered: bootstrap via ServiceLoader is skipped

        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j", "PersistenceBridge::persistNerConnection", "false"));

        Assertions.assertEquals(PersistenceStorage.NEO4J, PersistenceBridge.storageStatic);
        Assertions.assertTrue(PersistenceBridge.usePersistenceStatic);
        Assertions.assertTrue(PersistenceBridge.persistTextStateStatic);
        Assertions.assertTrue(PersistenceBridge.persistRecommendationsStatic);
        Assertions.assertTrue(PersistenceBridge.persistNerConnectionStatic, "storage=neo4j overrides an explicit persistNerConnection=false");
        Assertions.assertTrue(PersistenceBridge.shouldFailOnPersistenceError(), "storage=neo4j implies fail-fast without an explicit setFailOnPersistenceError");
    }

    @Test
    @DisplayName("storage=memory (default) leaves the individual flags exactly as configured")
    void storageMemoryLeavesExplicitFlagsAlone() {
        PersistenceBridge.setHandler(new ThrowingPersistenceHandler());

        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true", "PersistenceBridge::persistTextState", "true"));

        Assertions.assertEquals(PersistenceStorage.MEMORY, PersistenceBridge.storageStatic);
        Assertions.assertTrue(PersistenceBridge.usePersistenceStatic);
        Assertions.assertTrue(PersistenceBridge.persistTextStateStatic);
        Assertions.assertFalse(PersistenceBridge.persistNerConnectionStatic);
        Assertions.assertFalse(PersistenceBridge.shouldFailOnPersistenceError(), "memory storage must not derive fail-fast");
    }

    @Test
    @DisplayName("every flag resets to its default when applyConfiguration omits its key, instead of keeping the previous run's value")
    void flagsResetWhenKeysAreAbsent() {
        PersistenceBridge.setHandler(new ThrowingPersistenceHandler());
        var allOn = org.eclipse.collections.api.factory.SortedMaps.mutable.<String, String>empty();
        allOn.put("PersistenceBridge::storage", "neo4j");
        allOn.put("PersistenceBridge::usePersistence", "true");
        allOn.put("PersistenceBridge::persistTextState", "true");
        allOn.put("PersistenceBridge::persistRecommendations", "true");
        allOn.put("PersistenceBridge::persistNerConnection", "true");
        PersistenceBridge.getInstance().applyConfiguration(allOn.toImmutable());
        Assertions.assertTrue(PersistenceBridge.usePersistenceStatic, "sanity check: flags are on before the empty reconfiguration");

        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.empty());

        Assertions.assertEquals(PersistenceStorage.MEMORY, PersistenceBridge.storageStatic);
        Assertions.assertFalse(PersistenceBridge.usePersistenceStatic);
        Assertions.assertFalse(PersistenceBridge.persistTextStateStatic);
        Assertions.assertFalse(PersistenceBridge.persistRecommendationsStatic);
        Assertions.assertFalse(PersistenceBridge.persistNerConnectionStatic);
        Assertions.assertFalse(PersistenceBridge.shouldFailOnPersistenceError(), "the derived fail-fast value must not leak into the next run either");
    }

    @Test
    @DisplayName("the explicit fail-on-persistence-error override is independent of storage and survives storage=memory")
    void explicitFailOnPersistenceErrorIsIndependentOfStorage() {
        PersistenceBridge.setFailOnPersistenceError(true);

        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "memory"));

        Assertions.assertTrue(PersistenceBridge.shouldFailOnPersistenceError(), "the explicit override must not be reset by applyConfiguration");
    }

    @Test
    @DisplayName("storage=neo4j without a registered PersistenceHandlerProvider aborts with a clear message")
    void storageNeo4jWithoutProviderAborts() {
        PersistenceBridge.setHandler(null); // no handler pre-registered, and this module has no ServiceLoader provider on its classpath

        var config = SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j");
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> PersistenceBridge.getInstance().applyConfiguration(config));
        Assertions.assertTrue(exception.getMessage().contains("PersistenceHandlerProvider"));
        Assertions.assertTrue(exception.getMessage().contains("ServiceLoader"));
    }

    @Test
    @DisplayName("an invalid storage value is rejected with a clear error instead of silently defaulting")
    void invalidStorageValueIsRejected() {
        var config = SortedMaps.immutable.with("PersistenceBridge::storage", "NEO4J"); // wrong case: values are lower-case, see PersistenceStorage
        Assertions.assertThrows(IllegalArgumentException.class, () -> PersistenceBridge.getInstance().applyConfiguration(config));
    }

    /** Minimal handler that always fails — used to simulate Neo4j outage. */
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
        public String exportGraphAsJson() {
            throw fail();
        }
    }
}
