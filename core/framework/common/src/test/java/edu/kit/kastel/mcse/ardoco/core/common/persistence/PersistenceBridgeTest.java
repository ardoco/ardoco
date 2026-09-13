/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.util.Collection;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.jgit.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.RecommendationModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

/**
 * Unit tests for {@link PersistenceBridge} configuration and fault isolation.
 */
class PersistenceBridgeTest {

    @AfterEach
    void resetFlags() {
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
        @Nullable
        public Model loadModel(Metamodel metamodel) {
            throw fail();
        }

        @Override
        public ImmutableSortedSet<Metamodel> getStoredMetamodels() {
            throw fail();
        }

        @Override
        public void savePreprocessedText(Text text, String identifier) {
            throw fail();
        }

        @Override
        @Nullable
        public Text loadPreprocessedText(String identifier) {
            throw fail();
        }

        @Override
        public boolean hasPreprocessedText(String identifier) {
            throw fail();
        }

        @Override
        public boolean saveTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
            throw fail();
        }

        @Override
        public Collection<ArchitectureCodeTraceLink> loadArchitectureCodeTraceLinks() {
            throw fail();
        }

        @Override
        public Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> loadTransitiveTraceLinks() {
            throw fail();
        }

        @Override
        public Collection<SentenceModelTraceLink> loadSentenceModelTraceLinks() {
            throw fail();
        }

        @Override
        public boolean addInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
            throw fail();
        }

        @Override
        public Collection<? extends Inconsistency> getInconsistencies() {
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
        public void deleteNounMapping(String ardocoId) {
            throw fail();
        }

        @Override
        public void saveRecommendedInstance(RecommendedInstance recommendedInstance, Metamodel metamodel) {
            throw fail();
        }

        @Override
        public boolean hasNounMappings() {
            throw fail();
        }

        @Override
        public Collection<NounMapping> loadNounMappings(Text annotatedText) {
            throw fail();
        }

        @Override
        public boolean hasRecommendedInstances() {
            throw fail();
        }

        @Override
        public Collection<RecommendedInstance> loadRecommendedInstances(Metamodel metamodel, SortedMap<String, NounMapping> nounMappingsById) {
            throw fail();
        }

        @Override
        public boolean hasRecommendationModelTraceLinks() {
            throw fail();
        }

        @Override
        public Collection<RecommendationModelTraceLink> loadRecommendationModelTraceLinks(SortedMap<String, RecommendedInstance> recommendedInstancesById,
                SortedMap<String, ArchitectureItem> architectureItemsById) {
            throw fail();
        }

        @Override
        public Collection<RecommendationModelTraceLink> loadRecommendationCodeTraceLinks(SortedMap<String, RecommendedInstance> recommendedInstancesById,
                SortedMap<String, edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem> codeItemsById) {
            throw fail();
        }

        @Override
        public void saveSimpleText(edu.kit.kastel.mcse.ardoco.core.api.text.SimpleText simpleText, String identifier) {
            throw fail();
        }

        @Override
        public boolean hasSimpleText(String identifier) {
            throw fail();
        }

        @Override
        public edu.kit.kastel.mcse.ardoco.core.api.text.SimpleText loadSimpleText(String identifier) {
            throw fail();
        }

        @Override
        public void saveProjectMetadata(String projectName, String inputText) {
            throw fail();
        }

        @Override
        public boolean hasProjectMetadata(String projectName) {
            throw fail();
        }

        @Override
        public String loadProjectInputText(String projectName) {
            throw fail();
        }

        @Override
        public String loadSoleProjectName() {
            throw fail();
        }

        @Override
        public void saveNamedArchitectureEntity(edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity entity,
                Metamodel metamodel, boolean unlinked) {
            throw fail();
        }

        @Override
        public boolean hasNerNamedArchitectureEntities() {
            throw fail();
        }

        @Override
        public Collection<edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity> loadNamedArchitectureEntities(
                Metamodel metamodel, boolean unlinkedOnly) {
            throw fail();
        }

        @Override
        public Collection<edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityToModelTraceLink> loadNerTraceLinks(
                Metamodel metamodel,
                SortedMap<String, edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence> occurrencesById,
                SortedMap<String, ModelEntity> modelEntitiesById) {
            throw fail();
        }
    }
}
