/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandler;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;

/**
 * A Neo4j run whose storage mode is switched to memory while the run is in progress (for example by another runner set up in between) has no
 * persistence handler left for its export and its wipe. The run must then fail with a clear {@link IllegalStateException} instead of a
 * {@link NullPointerException}.
 */
class ArdocoStorageChangeDuringRunTest {

    private static final String EXPECTED_MESSAGE = "PersistenceBridge::storage is memory, not neo4j, so no PersistenceHandler is available; the persistence settings were changed while a Neo4j run was in progress";

    private final List<String> handlerCalls = new ArrayList<>();

    @AfterEach
    void resetStorage() {
        PersistenceBridge.setHandler(null);
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.empty());
    }

    @Test
    @DisplayName("Storage switched to memory during a successful Neo4j run: the export fails with an IllegalStateException")
    void exportAfterStorageChangeFailsWithIllegalStateException() {
        Ardoco ardoco = neo4jRunWithStorageChangingStep(null);

        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, ardoco::run);

        Assertions.assertEquals(EXPECTED_MESSAGE, thrown.getMessage());
        Assertions.assertFalse(this.handlerCalls.contains("exportGraphAsJson"), this.handlerCalls::toString);
    }

    @Test
    @DisplayName("Storage switched to memory during a failing Neo4j run: the step's failure is rethrown and the wipe logs an IllegalStateException")
    void wipeAfterStorageChangeLogsIllegalStateException() {
        RuntimeException stepFailure = new RuntimeException("step failed");
        Ardoco ardoco = neo4jRunWithStorageChangingStep(stepFailure);

        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
        RuntimeException thrown;
        try {
            thrown = Assertions.assertThrows(RuntimeException.class, ardoco::run);
        } finally {
            System.setErr(originalErr);
        }
        String log = errBuffer.toString(StandardCharsets.UTF_8);

        Assertions.assertSame(stepFailure, thrown);
        Assertions.assertTrue(log.contains("Failed to wipe Neo4j after the aborted run"), log);
        Assertions.assertTrue(log.contains(IllegalStateException.class.getName() + ": " + EXPECTED_MESSAGE), log);
        Assertions.assertFalse(log.contains(NullPointerException.class.getName()), log);
    }

    private Ardoco neo4jRunWithStorageChangingStep(RuntimeException failure) {
        PersistenceBridge.setHandler(recordingHandler());
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));
        Ardoco ardoco = new Ardoco("storage-change");
        ardoco.addPipelineStep(new AbstractPipelineStep("SwitchStorageToMemory", ardoco.getDataRepository()) {
            @Override
            protected void process() {
                PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.empty());
                if (failure != null) {
                    throw failure;
                }
            }

            @Override
            protected void before() {
            }

            @Override
            protected void after() {
            }

            @Override
            protected void delegateApplyConfigurationToInternalObjects(ImmutableSortedMap<String, String> additionalConfiguration) {
            }
        });
        return ardoco;
    }

    private PersistenceHandler recordingHandler() {
        return (PersistenceHandler) Proxy.newProxyInstance(PersistenceHandler.class.getClassLoader(), new Class<?>[] { PersistenceHandler.class }, (proxy,
                method, args) -> {
            this.handlerCalls.add(method.getName());
            if (method.getReturnType() == String.class) {
                return "{}";
            }
            if (method.getReturnType() == boolean.class) {
                return false;
            }
            return null;
        });
    }
}
