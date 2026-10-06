/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;

/**
 * Unit tests for the write scope of {@link PersistenceBridge#withDeferredWrites} (noun-mapping merges).
 */
class PersistenceBridgeDeferredWritesTest {

    private final List<String> calls = new ArrayList<>();

    @BeforeEach
    void setUp() {
        PersistenceBridge.setHandler(recordingHandler(this.calls));
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
        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", usePersistence, "PersistenceBridge::persistTextState",
                        persistTextState, "PersistenceBridge::persistRecommendations", "false", "PersistenceBridge::persistNerConnection", "false"));
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

    private static PersistenceHandler recordingHandler(List<String> calls) {
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
}
