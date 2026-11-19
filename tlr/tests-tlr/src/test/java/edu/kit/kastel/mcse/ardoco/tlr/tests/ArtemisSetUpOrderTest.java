/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandler;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Artemis;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

/**
 * The Artemis runner must apply its persistence configuration before it records the persistence settings for the run-time check in
 * {@code Ardoco.run()}. Uses a stub handler and no NER strategy, so neither a Neo4j database nor a language model is involved.
 */
class ArtemisSetUpOrderTest {

    @AfterEach
    void resetPersistence() {
        PersistenceBridge.setHandler(null);
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.empty());
    }

    @Test
    @DisplayName("An Artemis runner set up with storage=neo4j after a memory configuration passes the settings check and reaches the database clean")
    void neo4jSetUpAfterMemoryReachesTheDatabaseClean(@TempDir File directory) throws IOException {
        PersistenceBridge.setHandler(sentinelHandler());
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.empty());
        Assertions.assertFalse(PersistenceBridge.isNeo4jStorage());

        File inputText = new File(directory, "input.txt");
        Files.writeString(inputText.toPath(), "The client calls the server.");
        File outputDirectory = new File(directory, "out");
        Assertions.assertTrue(outputDirectory.mkdirs());

        MutableSortedMap<String, String> configs = SortedMaps.mutable.empty();
        configs.put("PersistenceBridge::storage", "neo4j");
        configs.put("PersistenceBridge::neo4jUri", "bolt://127.0.0.1:1");
        configs.put("PersistenceBridge::neo4jUsername", "stub");
        configs.put("PersistenceBridge::neo4jPassword", "stub");

        Artemis runner = new Artemis("artemis-set-up-order");
        runner.setUp(inputText, null, null, configs.toImmutable(), outputDirectory, LargeLanguageModel.GPT_4_O, List.of());
        Assertions.assertTrue(PersistenceBridge.isNeo4jStorage());

        Assertions.assertThrows(SentinelException.class, runner::run);
    }

    private static PersistenceHandler sentinelHandler() {
        return (PersistenceHandler) Proxy.newProxyInstance(PersistenceHandler.class.getClassLoader(), new Class<?>[] { PersistenceHandler.class }, (proxy,
                method, args) -> {
            if ("deleteAllData".equals(method.getName())) {
                throw new SentinelException();
            }
            return method.getReturnType() == boolean.class ? Boolean.FALSE : null;
        });
    }

    private static final class SentinelException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        SentinelException() {
            super("deleteAllData reached");
        }
    }
}
