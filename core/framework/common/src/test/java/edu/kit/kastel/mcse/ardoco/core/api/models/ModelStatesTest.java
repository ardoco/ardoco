/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.SimpleText;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandler;

/**
 * Unit tests for {@link ModelStates#addModel}'s persistence de-duplication by model object identity.
 */
class ModelStatesTest {

    @AfterEach
    void resetFlags() {
        PersistenceBridge.setHandler(null);
        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "false", "PersistenceBridge::persistTextState", "false",
                        "PersistenceBridge::persistRecommendations", "false", "PersistenceBridge::persistNerConnection", "false"));
    }

    @Test
    @DisplayName("addModel registers the same model object under two metamodel keys but persists it only once")
    void addModelPersistsSameModelObjectOnlyOnce() {
        AtomicInteger saveCount = new AtomicInteger();
        PersistenceBridge.setHandler(new CountingPersistenceHandler(saveCount));
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true"));

        ArchitectureModelWithComponentsAndInterfaces model = new ArchitectureModelWithComponentsAndInterfaces(new ArrayList<>());

        ModelStates modelStates = new ModelStates();
        // Mirrors ModelProviderInformant.process(), which registers the same extracted model object under
        // extractedModel.getMetamodel() and extractor.getMetamodel() - the same value for e.g. PcmExtractor.
        modelStates.addModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS, model);
        modelStates.addModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS, model);

        Assertions.assertEquals(1, saveCount.get(), "The same model object must be persisted only once per run");
        Assertions.assertSame(model, modelStates.getModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS), "In-memory registration must still happen on every call");
    }

    @Test
    @DisplayName("addModel persists two distinct model objects separately")
    void addModelPersistsDistinctModelsSeparately() {
        AtomicInteger saveCount = new AtomicInteger();
        PersistenceBridge.setHandler(new CountingPersistenceHandler(saveCount));
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true"));

        ModelStates modelStates = new ModelStates();
        modelStates.addModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS, new ArchitectureModelWithComponentsAndInterfaces(new ArrayList<>()));
        modelStates.addModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, new ArchitectureModelWithComponentsAndInterfaces(new ArrayList<>()));

        Assertions.assertEquals(2, saveCount.get(), "Two distinct model objects must each be persisted");
    }

    @Test
    @DisplayName("addModel persists two distinct model objects that happen to share an id")
    void addModelPersistsDistinctModelObjectsWithSameId() {
        AtomicInteger saveCount = new AtomicInteger();
        PersistenceBridge.setHandler(new CountingPersistenceHandler(saveCount));
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true"));

        // IdentifierProvider.reset() runs before every extraction (see ModelProviderInformant.process()), so two
        // distinct models extracted in the same run (e.g. a PCM architecture model and a code model read from a
        // directory) can legitimately share a Model.getId() value - de-duplication must not key on that id.
        String sharedId = "shared-id";
        ArchitectureModelWithComponentsAndInterfaces first = new ArchitectureModelWithComponentsAndInterfaces(sharedId, new ArrayList<>());
        ArchitectureModelWithComponentsAndInterfaces second = new ArchitectureModelWithComponentsAndInterfaces(sharedId, new ArrayList<>());

        ModelStates modelStates = new ModelStates();
        modelStates.addModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS, first);
        modelStates.addModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, second);

        Assertions.assertEquals(2, saveCount.get(), "Two distinct model objects sharing an id must both be persisted");
    }

    private static final class CountingPersistenceHandler implements PersistenceHandler {
        private final AtomicInteger saveCount;

        private CountingPersistenceHandler(AtomicInteger saveCount) {
            this.saveCount = saveCount;
        }

        @Override
        public void saveModel(Metamodel metamodel, Model model) {
            saveCount.incrementAndGet();
        }

        @Override
        public void savePreprocessedText(Text text, String identifier) {
            // no-op
        }

        @Override
        public boolean saveTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
            return true;
        }

        @Override
        public boolean addInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
            return true;
        }

        @Override
        public void deleteModel(Metamodel metamodel) {
            // no-op
        }

        @Override
        public void deletePreprocessedText(String identifier) {
            // no-op
        }

        @Override
        public void deleteInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
            // no-op
        }

        @Override
        public void deleteAllInconsistencies() {
            // no-op
        }

        @Override
        public void deleteAllData() {
            // no-op
        }

        @Override
        public void saveNounMapping(NounMapping nounMapping) {
            // no-op
        }

        @Override
        public void saveNounMappings(Collection<NounMapping> nounMappings) {
            // no-op
        }

        @Override
        public void deleteNounMapping(String ardocoId) {
            // no-op
        }

        @Override
        public void saveRecommendedInstance(RecommendedInstance recommendedInstance, Metamodel metamodel) {
            // no-op
        }

        @Override
        public void saveRecommendedInstances(Collection<RecommendedInstance> recommendedInstances, Metamodel metamodel) {
            // no-op
        }

        @Override
        public void saveSimpleText(SimpleText simpleText, String identifier) {
            // no-op
        }

        @Override
        public void saveProjectMetadata(String projectName, String inputText) {
            // no-op
        }

        @Override
        public void saveNamedArchitectureEntity(NamedArchitectureEntity entity, Metamodel metamodel, boolean unlinked) {
            // no-op
        }

        @Override
        public void saveNamedArchitectureEntities(Collection<NamedArchitectureEntity> entities, Metamodel metamodel, boolean unlinked) {
            // no-op
        }

        @Override
        public String exportGraphAsJson() {
            return "{}";
        }
    }
}
