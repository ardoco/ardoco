/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * Unit tests for the write scope of {@link PersistenceBridge#withDeferredWrites} (noun-mapping merges).
 */
class PersistenceBridgeDeferredWritesTest {

    private final List<String> calls = new ArrayList<>();
    private boolean failRecommendedInstanceSaves = false;

    @BeforeEach
    void setUp() {
        PersistenceBridge.setHandler(recordingHandler(this.calls, () -> this.failRecommendedInstanceSaves));
        configure("true", "true");
    }

    @AfterEach
    void tearDown() {
        PersistenceBridge.setHandler(null);
        configure("false", "false");
    }

    @Test
    @DisplayName("Outside a scope, noun-mapping deletions run immediately")
    void deletionRunsImmediatelyOutsideScope() {
        PersistenceBridge.deleteNounMapping("a");
        Assertions.assertEquals(List.of("deleteNounMapping:a"), this.calls);
    }

    @Test
    @DisplayName("In a merge scope, the surviving id is upserted, not deleted; the obsolete id is deleted last")
    void mergeScopeKeepsSurvivingIdAndDeletesObsoleteLast() {
        PersistenceBridge.withDeferredWrites(() -> {
            PersistenceBridge.deleteNounMapping("first");   // removeNounMapping(first, merged)
            PersistenceBridge.deleteNounMapping("second");  // removeNounMapping(second, merged)
            Assertions.assertTrue(this.calls.isEmpty(), "Deletions must be deferred inside the scope");
            PersistenceBridge.saveNounMapping(nounMapping("first")); // addNounMapping(merged), merged keeps the id of first
            Assertions.assertEquals(List.of("saveNounMapping:first"), this.calls, "Noun-mapping saves must run immediately");
            return null;
        });
        Assertions.assertEquals(List.of("saveNounMapping:first", "deleteNounMapping:second"), this.calls);
    }

    @Test
    @DisplayName("Chained merges in one scope: an id saved earlier and merged away later is still deleted")
    void chainedMergesDeleteIdsMergedAwayLater() {
        PersistenceBridge.withDeferredWrites(() -> {
            // merge 1: A + B -> A
            PersistenceBridge.deleteNounMapping("A");
            PersistenceBridge.deleteNounMapping("B");
            PersistenceBridge.saveNounMapping(nounMapping("A"));
            // merge 2: A + C -> C (C is older than A)
            PersistenceBridge.deleteNounMapping("A");
            PersistenceBridge.deleteNounMapping("C");
            PersistenceBridge.saveNounMapping(nounMapping("C"));
            return null;
        });
        Assertions.assertEquals(List.of("saveNounMapping:A", "saveNounMapping:C", "deleteNounMapping:B", "deleteNounMapping:A"), this.calls);
    }

    @Test
    @DisplayName("Recommended-instance saves are deferred to the end of the scope and run once per instance")
    void recommendedInstanceSavesAreDeferredAndDeduplicated() {
        configure("true", "true", "true");
        RecommendedInstance ri = new StubRecommendedInstance("ri-1");
        PersistenceBridge.withDeferredWrites(() -> {
            PersistenceBridge.saveRecommendedInstance(ri, Metamodel.ARCHITECTURE_WITH_COMPONENTS);
            PersistenceBridge.deleteNounMapping("old");
            PersistenceBridge.saveRecommendedInstance(ri, Metamodel.ARCHITECTURE_WITH_COMPONENTS);
            Assertions.assertTrue(this.calls.isEmpty(), "Recommended-instance saves must be deferred inside the scope");
            return null;
        });
        Assertions.assertEquals(List.of("saveRecommendedInstance:ri-1", "deleteNounMapping:old"), this.calls);
    }

    @Test
    @DisplayName("If a deferred save fails, obsolete noun mappings are not deleted (no link is lost)")
    void failedDeferredSaveSkipsDeletions() {
        configure("true", "true", "true");
        this.failRecommendedInstanceSaves = true;
        RecommendedInstance ri = new StubRecommendedInstance("ri-2");
        PersistenceBridge.withDeferredWrites(() -> {
            PersistenceBridge.deleteNounMapping("first");
            PersistenceBridge.deleteNounMapping("second");
            PersistenceBridge.saveNounMapping(nounMapping("first"));
            PersistenceBridge.saveRecommendedInstance(ri, Metamodel.ARCHITECTURE_WITH_COMPONENTS);
            return null;
        });
        Assertions.assertEquals(List.of("saveNounMapping:first", "saveRecommendedInstance:ri-2"), this.calls,
                "The obsolete noun mapping 'second' must not be deleted after the failed save");
    }

    @Test
    @DisplayName("Nested scopes join the outer scope and flush only at its end")
    void nestedScopesJoinOuterScope() {
        PersistenceBridge.withDeferredWrites(() -> {
            PersistenceBridge.withDeferredWrites(() -> {
                PersistenceBridge.deleteNounMapping("x");
                return null;
            });
            Assertions.assertTrue(this.calls.isEmpty(), "Inner scope must not flush");
            return null;
        });
        Assertions.assertEquals(List.of("deleteNounMapping:x"), this.calls);
    }

    @Test
    @DisplayName("Collected writes are flushed when the scope body throws, and the exception propagates")
    void scopeFlushesOnException() {
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, () -> PersistenceBridge.withDeferredWrites(() -> {
            PersistenceBridge.deleteNounMapping("y");
            throw new IllegalStateException("pipeline failure");
        }));
        Assertions.assertEquals("pipeline failure", thrown.getMessage());
        Assertions.assertEquals(List.of("deleteNounMapping:y"), this.calls);
    }

    @Test
    @DisplayName("Without TextState persistence nothing is written, inside or outside a scope")
    void nothingIsWrittenWhenTextStatePersistenceIsOff() {
        configure("true", "false");
        PersistenceBridge.deleteNounMapping("a");
        PersistenceBridge.withDeferredWrites(() -> {
            PersistenceBridge.saveNounMapping(nounMapping("b"));
            PersistenceBridge.deleteNounMapping("c");
            return null;
        });
        Assertions.assertTrue(this.calls.isEmpty());
    }

    private static void configure(String usePersistence, String persistTextState) {
        configure(usePersistence, persistTextState, "false");
    }

    private static void configure(String usePersistence, String persistTextState, String persistRecommendations) {
        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", usePersistence, "PersistenceBridge::persistTextState",
                        persistTextState, "PersistenceBridge::persistRecommendations", persistRecommendations, "PersistenceBridge::persistNerConnection",
                        "false"));
    }

    private static NounMapping nounMapping(String ardocoId) {
        return (NounMapping) Proxy.newProxyInstance(NounMapping.class.getClassLoader(), new Class<?>[] { NounMapping.class }, (proxy, method, args) -> {
            switch (method.getName()) {
            case "getArdocoId":
                return ardocoId;
            case "toString":
                return "NounMapping[" + ardocoId + "]";
            case "hashCode":
                return System.identityHashCode(proxy);
            case "equals":
                return proxy == args[0];
            default:
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static PersistenceHandler recordingHandler(List<String> calls, java.util.function.BooleanSupplier failRecommendedInstanceSaves) {
        return (PersistenceHandler) Proxy.newProxyInstance(PersistenceHandler.class.getClassLoader(), new Class<?>[] { PersistenceHandler.class }, (proxy,
                method, args) -> {
            switch (method.getName()) {
            case "toString":
                return "RecordingPersistenceHandler";
            case "hashCode":
                return System.identityHashCode(proxy);
            case "equals":
                return proxy == args[0];
            case "saveNounMapping":
                calls.add("saveNounMapping:" + ((NounMapping) args[0]).getArdocoId());
                return null;
            case "deleteNounMapping":
                calls.add("deleteNounMapping:" + args[0]);
                return null;
            case "saveRecommendedInstance":
                calls.add("saveRecommendedInstance:" + ((RecommendedInstance) args[0]).getId());
                if (failRecommendedInstanceSaves.getAsBoolean()) {
                    throw new IllegalStateException("simulated Neo4j failure");
                }
                return null;
            default:
                calls.add(method.getName());
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == double.class) {
            return 0.0d;
        }
        if (type == float.class) {
            return 0.0f;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }

    /** Minimal {@link RecommendedInstance} for the bridge tests; only the id is used. */
    private static final class StubRecommendedInstance extends RecommendedInstance {
        private static final long serialVersionUID = 1L;

        StubRecommendedInstance(String id) {
            super("stub", id);
        }

        @Override
        public ImmutableList<NounMapping> getNameMappings() {
            return Lists.immutable.empty();
        }

        @Override
        public ImmutableList<NounMapping> getTypeMappings() {
            return Lists.immutable.empty();
        }

        @Override
        public double getProbability() {
            return 0;
        }

        @Override
        public void addProbability(Claimant claimant, double probability) {
            // not needed
        }

        @Override
        public void addMappings(ImmutableList<NounMapping> nameMapping, ImmutableList<NounMapping> typeMapping) {
            // not needed
        }

        @Override
        public void addName(NounMapping nameMapping) {
            // not needed
        }

        @Override
        public void addType(NounMapping typeMapping) {
            // not needed
        }

        @Override
        public String getType() {
            return "stub";
        }

        @Override
        public String getName() {
            return "stub";
        }

        @Override
        public void onNounMappingDeletion(NounMapping nounMapping, NounMapping replacement) {
            // not needed
        }

        @Override
        public void setMetamodel(Metamodel metamodel) {
            // not needed
        }
    }
}
