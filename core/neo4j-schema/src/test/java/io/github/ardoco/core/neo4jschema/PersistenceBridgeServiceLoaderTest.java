/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.Map;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.testcontainers.containers.Neo4jContainer;

import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandler;

/**
 * Verifies the production bootstrap path (B1 of revised Phase 1 step 1): {@link PersistenceBridge} finding and using
 * {@link Neo4jPersistenceHandlerProvider} through {@link java.util.ServiceLoader}, with no test-provided Spring
 * context. Every other test in this module wires its own {@code @SpringBootTest} context and so never exercises this
 * path; here {@link PersistenceBridge} starts (and owns) the context itself, exactly as a runner would.
 */
class PersistenceBridgeServiceLoaderTest {

    private static Neo4jContainer<?> container;

    @BeforeAll
    static void startContainer() {
        container = new Neo4jContainer<>("neo4j:5").withReuse(true);
        container.start();
    }

    @AfterAll
    static void stopContainer() {
        Neo4jPersistenceHandlerProvider.resetForTesting();
        if (container != null) {
            container.stop();
        }
    }

    @AfterEach
    void resetBridge() {
        PersistenceBridge.setHandler(null);
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.empty());
        Neo4jPersistenceHandlerProvider.resetForTesting();
    }

    @Test
    @DisplayName("storage=neo4j bootstraps a Neo4jPersistenceHandler via ServiceLoader, connected to this container (not e.g. a live Neo4j Desktop)")
    void bootstrapsHandlerViaServiceLoader() {
        PersistenceBridge.setHandler(null);

        PersistenceBridge.getInstance().applyConfiguration(configFor(container.getBoltUrl(), "neo4j", container.getAdminPassword()));

        Assertions.assertTrue(PersistenceBridge.isNeo4jStorage());
        PersistenceHandler handler = PersistenceBridge.getHandler();
        Assertions.assertInstanceOf(Neo4jPersistenceHandler.class, handler);

        // A property-precedence bug once caused the provider to silently connect to a live Neo4j Desktop instance
        // instead of this container (see the session notes). Prove the handler is actually talking to THIS
        // container: write a distinctive marker through the handler, then read it back via a completely separate
        // direct driver connection to the container's own (randomly assigned) port.
        String marker = "service-loader-smoke-test-marker-" + System.nanoTime();
        handler.saveProjectMetadata(marker, "x");
        try (Driver directDriver = GraphDatabase.driver(container.getBoltUrl(), AuthTokens.basic("neo4j", container.getAdminPassword()))) {
            try (Session session = directDriver.session()) {
                long count = session.run("MATCH (p:Project {projectName: $name}) RETURN count(p) AS c", Map.of("name", marker)).single().get("c").asLong();
                Assertions.assertEquals(1, count, "the handler must be writing to this Testcontainers instance, not e.g. a live Neo4j Desktop");
            }
        }
    }

    @Test
    @DisplayName("storage=neo4j with a bad connection aborts with a clear message, never containing the password")
    void badConnectionAborts() {
        PersistenceBridge.setHandler(null);

        ImmutableSortedMap<String, String> configWithBadUri = configFor("bolt://127.0.0.1:1", "neo4j", "wrong-password");

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> PersistenceBridge.getInstance()
                .applyConfiguration(configWithBadUri));
        Assertions.assertTrue(exception.getMessage().contains("bolt://127.0.0.1:1"), "message should name the attempted URI");
        Assertions.assertFalse(exception.getMessage().contains("wrong-password"), "the password must never appear in an error message");
    }

    @Test
    @DisplayName("A run configured for a second database after a first one writes to the second database")
    void changedConnectionSettingsRebootstrapTheHandler() {
        try (Neo4jContainer<?> second = new Neo4jContainer<>("neo4j:5")) {
            second.start();
            PersistenceBridge.setHandler(null);
            PersistenceBridge.getInstance().applyConfiguration(configFor(container.getBoltUrl(), "neo4j", container.getAdminPassword()));
            PersistenceBridge.getInstance().applyConfiguration(configFor(second.getBoltUrl(), "neo4j", second.getAdminPassword()));

            String marker = "rebootstrap-marker-" + System.nanoTime();
            PersistenceBridge.getHandler().saveProjectMetadata(marker, "x");

            Assertions.assertEquals(1, countProjects(second.getBoltUrl(), second.getAdminPassword(), marker), "the marker must be in the second database");
            Assertions.assertEquals(0, countProjects(container.getBoltUrl(), container.getAdminPassword(), marker),
                    "the marker must not be in the first " + "database");
        }
    }

    @Test
    @DisplayName("An unreachable database configured twice fails both times")
    void unreachableDatabaseFailsEveryTime() {
        PersistenceBridge.setHandler(null);
        ImmutableSortedMap<String, String> configWithBadUri = configFor("bolt://127.0.0.1:1", "neo4j", "wrong-password");

        Assertions.assertThrows(IllegalStateException.class, () -> PersistenceBridge.getInstance().applyConfiguration(configWithBadUri));
        Assertions.assertThrows(IllegalStateException.class, () -> PersistenceBridge.getInstance().applyConfiguration(configWithBadUri));
    }

    private static long countProjects(String boltUrl, String password, String projectName) {
        try (Driver directDriver = GraphDatabase.driver(boltUrl, AuthTokens.basic("neo4j", password)); Session session = directDriver.session()) {
            return session.run("MATCH (p:Project {projectName: $name}) RETURN count(p) AS c", Map.of("name", projectName)).single().get("c").asLong();
        }
    }

    private static ImmutableSortedMap<String, String> configFor(String uri, String username, String password) {
        return SortedMaps.immutable.with(//
                "PersistenceBridge::storage", "neo4j", //
                "PersistenceBridge::neo4jUri", uri, //
                "PersistenceBridge::neo4jUsername", username, //
                "PersistenceBridge::neo4jPassword", password);
    }
}
