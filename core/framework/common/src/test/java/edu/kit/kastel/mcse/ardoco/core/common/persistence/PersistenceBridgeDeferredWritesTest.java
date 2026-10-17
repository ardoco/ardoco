/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.RecommendationModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * Unit tests for the write-behind scope of {@link PersistenceBridge#withDeferredWrites}: originally noun-mapping merges (Fix 2a), now the
 * stage-level write-behind for all five dual-write kinds (noun mappings, recommended instances, NER entities, trace links, inconsistencies), each
 * batched into one call per scope, plus noun-mapping deletions.
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
    @DisplayName("In a merge scope, noun-mapping saves and deletions are both deferred; the surviving id is upserted, the obsolete id deleted last")
    void mergeScopeKeepsSurvivingIdAndDeletesObsoleteLast() {
        PersistenceBridge.withDeferredWrites(() -> {
            PersistenceBridge.deleteNounMapping("first");   // removeNounMapping(first, merged)
            PersistenceBridge.deleteNounMapping("second");  // removeNounMapping(second, merged)
            Assertions.assertTrue(this.calls.isEmpty(), "Deletions must be deferred inside the scope");
            PersistenceBridge.saveNounMapping(nounMapping("first")); // addNounMapping(merged), merged keeps the id of first
            Assertions.assertTrue(this.calls.isEmpty(), "Noun-mapping saves must also be deferred inside the scope, batched with the rest");
            return null;
        });
        Assertions.assertEquals(List.of("saveNounMappings:[first]", "deleteNounMapping:second"), this.calls);
    }

    @Test
    @DisplayName("Chained merges in one scope: all pending saves flush as one batch; an id merged away later is still deleted")
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
        // One batched save for both surviving ids (A and C), not one call per save; B and the re-deleted A are deleted last.
        Assertions.assertEquals(List.of("saveNounMappings:[A, C]", "deleteNounMapping:B", "deleteNounMapping:A"), this.calls);
    }

    @Test
    @DisplayName("Recommended-instance saves are deferred to the end of the scope and batched into one call")
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
        Assertions.assertEquals(List.of("saveRecommendedInstances:[ri-1]", "deleteNounMapping:old"), this.calls);
    }

    @Test
    @DisplayName("If a deferred batch fails, obsolete noun mappings are not deleted (no link is lost)")
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
        // The NounMapping batch succeeds independently of the RecommendedInstance batch; only the latter fails.
        Assertions.assertEquals(List.of("saveNounMappings:[first]", "saveRecommendedInstances:[ri-2]"), this.calls,
                "The obsolete noun mapping 'second' must not be deleted after the failed batch");
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
    @DisplayName("Collected writes are discarded, not flushed, when the scope body throws, and the exception propagates")
    void scopeDiscardsPendingWritesOnException() {
        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, () -> PersistenceBridge.withDeferredWrites(() -> {
            PersistenceBridge.saveNounMapping(nounMapping("y"));
            PersistenceBridge.deleteNounMapping("z");
            throw new IllegalStateException("pipeline failure");
        }));
        Assertions.assertEquals("pipeline failure", thrown.getMessage());
        Assertions.assertTrue(this.calls.isEmpty(),
                "A failed stage discards its pending writes instead of flushing them; the run gets wiped on failure anyway");
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

    @Test
    @DisplayName("A stage scope flushes every kind in the fixed order: NounMappings, RecommendedInstances, NER entities, trace links, "
            + "inconsistencies, deletions last - trace links and inconsistencies match their endpoints by id, so those must already exist")
    void stageFlushOrderCoversAllKinds() {
        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true", "PersistenceBridge::persistTextState", "true",
                        "PersistenceBridge::persistRecommendations", "true", "PersistenceBridge::persistNerConnection", "true"));

        RecommendedInstance ri = new StubRecommendedInstance("ri-order");
        NamedArchitectureEntity entity = new NamedArchitectureEntity("entity-order", new TreeSet<>(), List.of());
        TraceLink<?, ?> traceLink = stubTraceLink("order");
        Inconsistency inconsistency = stubInconsistency("order");

        PersistenceBridge.withDeferredWrites("stage-order", () -> {
            PersistenceBridge.deleteNounMapping("obsolete");
            PersistenceBridge.saveNounMapping(nounMapping("order"));
            PersistenceBridge.saveRecommendedInstance(ri, Metamodel.ARCHITECTURE_WITH_COMPONENTS);
            PersistenceBridge.saveNamedArchitectureEntity(entity, Metamodel.ARCHITECTURE_WITH_COMPONENTS, false);
            PersistenceBridge.saveTraceLinks(List.of(traceLink));
            PersistenceBridge.addInconsistencies(List.of(inconsistency));
            Assertions.assertTrue(this.calls.isEmpty(), "Every kind must be deferred inside the scope");
            return null;
        });

        Assertions.assertEquals(List.of("saveNounMappings:[order]", "saveRecommendedInstances:[ri-order]", "saveNamedArchitectureEntities:1",
                "saveTraceLinks:1", "addInconsistencies:1", "deleteNounMapping:obsolete"), this.calls);
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

    private static TraceLink<?, ?> stubTraceLink(String id) {
        RecommendedInstance ri = new StubRecommendedInstance("ri-" + id);
        ArchitectureComponent target = new ArchitectureComponent("Target-" + id, "target-" + id, new TreeSet<>(), new TreeSet<>(), new TreeSet<>(),
                "Service");
        return new RecommendationModelTraceLink(ri, target);
    }

    private static Inconsistency stubInconsistency(String reason) {
        return new Inconsistency() {
            @Override
            public String getReason() {
                return reason;
            }

            @Override
            public String getType() {
                return "stub";
            }

            @Override
            public ImmutableCollection<String[]> toFileOutput() {
                return Lists.immutable.empty();
            }
        };
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
            case "saveNounMappings":
                calls.add("saveNounMappings:" + idsOf((Collection<?>) args[0], item -> ((NounMapping) item).getArdocoId()));
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
            case "saveRecommendedInstances":
                calls.add("saveRecommendedInstances:" + idsOf((Collection<?>) args[0], item -> ((RecommendedInstance) item).getId()));
                if (failRecommendedInstanceSaves.getAsBoolean()) {
                    throw new IllegalStateException("simulated Neo4j failure");
                }
                return null;
            case "saveNamedArchitectureEntities":
                calls.add("saveNamedArchitectureEntities:" + ((Collection<?>) args[0]).size());
                return null;
            case "saveTraceLinks":
                calls.add("saveTraceLinks:" + ((Collection<?>) args[0]).size());
                return true;
            case "addInconsistencies":
                calls.add("addInconsistencies:" + ((Collection<?>) args[0]).size());
                return true;
            default:
                calls.add(method.getName());
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static String idsOf(Collection<?> items, java.util.function.Function<Object, String> idExtractor) {
        List<String> ids = new ArrayList<>();
        for (Object item : items) {
            ids.add(idExtractor.apply(item));
        }
        return ids.toString();
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
