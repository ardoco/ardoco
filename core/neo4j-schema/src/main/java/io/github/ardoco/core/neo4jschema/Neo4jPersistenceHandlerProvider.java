/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.neo4j.core.Neo4jClient;

import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandler;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandlerProvider;

/**
 * {@link PersistenceHandlerProvider} registered via {@code META-INF/services} so {@code PersistenceBridge} can
 * bootstrap Neo4j persistence via {@link java.util.ServiceLoader} without a compile-time dependency from the runner
 * modules to this one (this module already depends on {@code pipeline-tlr}/{@code pipeline-id}; a dependency the
 * other way would be a cycle).
 *
 * <h2>Spring context lifecycle</h2>
 * Starting a Spring context costs seconds, so one is started on first use and reused across calls as long as the
 * connection properties (uri/username/password) do not change; if they do, the old context is closed and a new one
 * started. A JVM shutdown hook closes the context so it never outlives the process. All of this is synchronized: two
 * concurrent callers never start two contexts.
 *
 * <p>Note: starting this context also activates {@link Neo4jBridgeActivator} inside it (it sets
 * {@code ardoco.persistence.neo4j.enabled=true}), which calls {@code PersistenceBridge.setHandler(...)} itself with
 * the same handler bean this class returns. That duplicate registration is harmless (same bean, same effect).
 */
public final class Neo4jPersistenceHandlerProvider implements PersistenceHandlerProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jPersistenceHandlerProvider.class);

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static ConfigurableApplicationContext context;
    private static ImmutableSortedMap<String, String> activeProperties;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(Neo4jPersistenceHandlerProvider::closeContext, "neo4j-persistence-context-shutdown"));
    }

    @Override
    public PersistenceHandler createHandler(ImmutableSortedMap<String, String> connectionProperties) {
        LOCK.lock();
        try {
            ConfigurableApplicationContext ctx = getOrStartContext(connectionProperties);
            verifyConnectivity(ctx, connectionProperties);
            return ctx.getBean(Neo4jPersistenceHandler.class);
        } finally {
            LOCK.unlock();
        }
    }

    private ConfigurableApplicationContext getOrStartContext(ImmutableSortedMap<String, String> properties) {
        if (context != null && context.isActive() && properties.equals(activeProperties)) {
            return context;
        }
        closeContext();

        String uri = properties.get(PROPERTY_URI);
        String username = properties.get(PROPERTY_USERNAME);
        String password = properties.get(PROPERTY_PASSWORD);
        LOGGER.info("Starting the Neo4j persistence context (uri={}, username={})", uri, username);

        // SpringApplicationBuilder.properties(...) only sets "default properties" (Spring Boot's lowest-precedence
        // source), which this module's own classpath application.properties would silently win over (it defines the
        // same keys with its own, fixed defaults). An initializer that adds a property source with addFirst() runs
        // before bean creation and outranks every other source, so these properties reliably reach the Neo4j driver
        // regardless of what application.properties says.
        Map<String, Object> connectionProperties = new LinkedHashMap<>();
        connectionProperties.put("spring.neo4j.uri", uri);
        connectionProperties.put("spring.neo4j.authentication.username", username);
        connectionProperties.put("spring.neo4j.authentication.password", password);
        connectionProperties.put("ardoco.persistence.neo4j.enabled", "true");
        connectionProperties.put("spring.neo4j.pool.metrics-enabled", "false");

        ConfigurableApplicationContext started = new SpringApplicationBuilder(Neo4jPersistenceContext.class).web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .initializers(applicationContext -> applicationContext.getEnvironment()
                        .getPropertySources()
                        .addFirst(new MapPropertySource("persistenceBridgeConnectionProperties", connectionProperties)))
                .run();
        context = started;
        activeProperties = properties;
        return started;
    }

    /**
     * Verification attempts before giving up; see the retry note below. 5 attempts, 1200 ms apart (~4.8 s of
     * retrying) comfortably clears Neo4j's default {@code dbms.security.auth_lock_time} (5 s) if the very first
     * attempt tripped it.
     */
    private static final int VERIFY_CONNECTIVITY_ATTEMPTS = 5;
    private static final long VERIFY_CONNECTIVITY_RETRY_DELAY_MILLIS = 1200;

    private void verifyConnectivity(ConfigurableApplicationContext ctx, ImmutableSortedMap<String, String> properties) {
        Neo4jClient client = ctx.getBean(Neo4jClient.class);
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= VERIFY_CONNECTIVITY_ATTEMPTS; attempt++) {
            try {
                client.query("RETURN 1").run();
                return;
            } catch (RuntimeException ex) {
                lastFailure = ex;
                // A freshly started database (e.g. a Testcontainers instance right after its wait strategy passes)
                // can briefly reject the very first connection while its auth subsystem finishes initializing. A
                // couple of short retries absorb that without weakening fail-fast: a truly dead database still
                // aborts within about a second.
                if (attempt < VERIFY_CONNECTIVITY_ATTEMPTS) {
                    sleepBriefly();
                }
            }
        }
        // Neo4jInitializer swallows its own index-creation failures; without this explicit check, a dead database
        // would otherwise look like a successfully bootstrapped handler.
        closeContext();
        throw new IllegalStateException(
                "Could not connect to Neo4j at " + properties.get(PROPERTY_URI) + " as " + properties.get(PROPERTY_USERNAME) + ": "
                        + lastFailure.getMessage(),
                lastFailure);
    }

    private static void sleepBriefly() {
        try {
            Thread.sleep(VERIFY_CONNECTIVITY_RETRY_DELAY_MILLIS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static void closeContext() {
        LOCK.lock();
        try {
            if (context != null) {
                try {
                    context.close();
                } catch (RuntimeException ex) {
                    LOGGER.warn("Failed to close the previous Neo4j persistence context cleanly: {}", ex.toString());
                }
                context = null;
                activeProperties = null;
            }
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Test-only: force-closes a context started by this provider and forgets the active properties, so a test does
     * not leak a Spring context (or its beans' registration with {@code PersistenceBridge}) into the next one.
     */
    public static void resetForTesting() {
        closeContext();
    }
}
