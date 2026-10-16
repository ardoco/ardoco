/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;

import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.execution.CodeRunnerBaseTest;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import io.github.ardoco.core.neo4jschema.Neo4jPersistenceHandler;

/**
 * Base class for TLR persistence integration tests.
 *
 * <h2>Which Neo4j the tests use</h2>
 * Selected with {@code -Dardoco.neo4j.testDatabase=container|local}:
 * <ul>
 *   <li>{@code container} (default): an isolated, throw-away Neo4j started via Testcontainers (Docker must be running). The tests never touch
 *       a local database, so a graph kept in Neo4j Desktop is safe.</li>
 *   <li>{@code local}: Neo4j Desktop at {@code bolt://127.0.0.1:7687} (user {@value #NEO4J_USER}, password {@value #NEO4J_PASSWORD}), for
 *       inspecting the graph in Neo4j Browser during a pause. To protect a kept graph, a test refuses to start if this database is not empty,
 *       unless {@code -Dardoco.neo4j.allowWipe=true} is set; a test that refused to start never clears the database.</li>
 * </ul>
 *
 * <h2>Data lifetime: pause then clear</h2>
 * Pipeline data is <em>not</em> left in Neo4j permanently.
 * <ol>
 *   <li>During the test, {@link #pauseForNeo4jInspection()} may sleep so you can open
 *       Neo4j Browser ({@value #NEO4J_BROWSER}) and inspect the graph.</li>
 *   <li>After the test method returns, {@link #clearNeo4jAfterTest()} runs
 *       ({@code @AfterEach}) and deletes all nodes/relationships.</li>
 * </ol>
 *
 * <h2>Pausing under Maven Surefire</h2>
 * {@code System.in.read()} does <em>not</em> work under Surefire (stdin is not interactive).
 * Use {@link #pauseForNeo4jInspection()} with {@code -Dardoco.neo4j.pauseSeconds=N} instead.
 *
 * @see TraceLinkPersistenceTest
 * @see TextStateRecommendationPersistenceTest
 */
@SpringBootTest(classes = io.github.ardoco.core.neo4jschema.Main.class, properties = { "spring.data.neo4j.repositories.type=imperative",
        "ardoco.persistence.neo4j.enabled=true", "spring.neo4j.pool.metrics-enabled=false" })
public abstract class AbstractPersistenceTest extends CodeRunnerBaseTest {

    /** Neo4j Browser (HTTP) — open this in a browser to inspect the graph during a pause. */
    protected static final String NEO4J_BROWSER = "http://localhost:7474";

    /**
     * Display / docs Bolt URI. The Spring connection below uses {@code 127.0.0.1}
     * explicitly for reliability on Windows.
     */
    protected static final String NEO4J_BOLT = "bolt://localhost:7687";

    protected static final String NEO4J_USER = "neo4j";
    protected static final String NEO4J_PASSWORD = "password123";

    /**
     * System property that enables a timed pause after pipeline runs.
     * Default {@code 0} = no pause (CI / normal local runs stay fast).
     */
    protected static final String PAUSE_SECONDS_PROPERTY = "ardoco.neo4j.pauseSeconds";

    /** System property selecting the test database: {@code container} (default) or {@code local}. */
    protected static final String TEST_DATABASE_PROPERTY = "ardoco.neo4j.testDatabase";

    /** System property that allows tests to clear a non-empty local database. */
    protected static final String ALLOW_WIPE_PROPERTY = "ardoco.neo4j.allowWipe";

    private static final boolean USE_LOCAL_NEO4J = "local".equalsIgnoreCase(System.getProperty(TEST_DATABASE_PROPERTY, "container"));

    /** Isolated Neo4j for the default mode; {@code null} when the local database is used. Same image as the neo4j-schema tests. */
    private static final Neo4jContainer<?> NEO4J_CONTAINER = USE_LOCAL_NEO4J ? null : startContainer();

    /**
     * Used by {@link #clearNeo4jAfterTest()} to wipe the graph after each test.
     * Data remains available only until that teardown — i.e. through any in-test pause.
     */
    @Autowired
    private Neo4jPersistenceHandler neo4jPersistenceHandler;

    @Autowired
    protected Neo4jClient neo4jClient;

    /**
     * Wires Spring Data Neo4j to the selected test database: the Testcontainers instance (default) or the local Desktop instance. For the local
     * instance, {@code 127.0.0.1} is used instead of {@code localhost} (Windows IPv6 quirks).
     */
    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        if (USE_LOCAL_NEO4J) {
            registry.add("spring.neo4j.uri", () -> "bolt://127.0.0.1:7687");
            registry.add("spring.neo4j.authentication.username", () -> NEO4J_USER);
            registry.add("spring.neo4j.authentication.password", () -> NEO4J_PASSWORD);
        } else {
            registry.add("spring.neo4j.uri", NEO4J_CONTAINER::getBoltUrl);
            registry.add("spring.neo4j.authentication.username", () -> "neo4j");
            registry.add("spring.neo4j.authentication.password", NEO4J_CONTAINER::getAdminPassword);
        }
    }

    private static Neo4jContainer<?> startContainer() {
        Neo4jContainer<?> container = new Neo4jContainer<>("neo4j:5").withReuse(true);
        container.start();
        return container;
    }

    /** {@return the Neo4j Browser URL of the database the tests use} */
    protected static String neo4jBrowserUrl() {
        return USE_LOCAL_NEO4J ? NEO4J_BROWSER : NEO4J_CONTAINER.getHttpUrl();
    }

    /** {@return the password of the database the tests use} */
    protected static String neo4jPassword() {
        return USE_LOCAL_NEO4J ? NEO4J_PASSWORD : NEO4J_CONTAINER.getAdminPassword();
    }

    /** {@return the Bolt URL of the database the tests use} */
    protected static String neo4jBoltUrl() {
        return USE_LOCAL_NEO4J ? "bolt://127.0.0.1:7687" : NEO4J_CONTAINER.getBoltUrl();
    }

    private static StanfordCoreNLP pipeline;

    /** True once this test may clear the database (container mode, or a local database that was empty or explicitly allowed). */
    private boolean graphOwnedByTest = false;

    @BeforeEach
    void setUp() {
        if (USE_LOCAL_NEO4J && !Boolean.getBoolean(ALLOW_WIPE_PROPERTY)) {
            long existingNodes = countAllNodes();
            if (existingNodes > 0) {
                throw new IllegalStateException("Local Neo4j at bolt://127.0.0.1:7687 already contains " + existingNodes
                        + " nodes. The tests would delete them. Use the default -D" + TEST_DATABASE_PROPERTY + "=container, empty the database, or set -D"
                        + ALLOW_WIPE_PROPERTY + "=true.");
            }
        }
        graphOwnedByTest = true;
        PersistenceBridge.setFailOnPersistenceError(true);
        System.out.println("----------------------------------------------------------");
        System.out.println("test database: " + (USE_LOCAL_NEO4J ? "local Neo4j Desktop" : "Testcontainers (isolated)"));
        System.out.println("neo4j browser: " + neo4jBrowserUrl());
        System.out.println("username:      " + NEO4J_USER);
        System.out.println("password:      " + neo4jPassword());
        System.out.println("Connect URL:   " + neo4jBoltUrl());
        System.out.println("Pause (s):     -D" + PAUSE_SECONDS_PROPERTY + "=<seconds> (0 = off)");
        System.out.println("Teardown:      graph cleared in @AfterEach after the test");
        System.out.println("----------------------------------------------------------");
    }

    /**
     * Deletes all Neo4j nodes and relationships after each test method.
     *
     * <p>JUnit order: test body (including {@link #pauseForNeo4jInspection()}) finishes
     * first, then this runs. So Browser inspection during the pause still sees data;
     * once the pause ends and assertions complete, the DB is emptied and nothing from
     * the test run is left behind.
     */
    @AfterEach
    void clearNeo4jAfterTest() {
        try {
            if (!graphOwnedByTest) {
                System.out.println(">>> @AfterEach: test did not start; database left untouched.");
                return;
            }
            System.out.println(">>> @AfterEach: clearing all Neo4j test data (MATCH (n) DETACH DELETE n)...");
            clearNeo4jGraph();
            System.out.println(">>> Neo4j graph cleared.");
        } finally {
            PersistenceBridge.setFailOnPersistenceError(false);
        }
    }

    protected void clearNeo4jGraph() {
        if (!graphOwnedByTest) {
            throw new IllegalStateException("Refusing to clear a database this test does not own");
        }
        neo4jPersistenceHandler.deleteAllData();
    }

    private long countAllNodes() {
        return neo4jClient.query("MATCH (n) RETURN count(n) AS c")
                .fetch()
                .one()
                .map(row -> ((Number) row.get("c")).longValue())
                .orElse(0L);
    }

    /**
     * Optional timed pause so you can inspect Neo4j Browser while the JVM is still up
     * and <em>before</em> {@link #clearNeo4jAfterTest()} wipes the graph.
     *
     * <p><b>Why not {@code System.in.read()}?</b> Maven Surefire does not attach an
     * interactive console; stdin typically hits EOF immediately, so Enter-to-continue
     * never blocks.
     *
     * <p><b>Usage (PowerShell — quote each {@code -D} argument):</b>
     * <pre>{@code
     * mvn -pl tlr/tests-tlr -am test `
     *   "-Dtest=TraceLinkPersistenceTest#testSwattrPipelineWithNeo4j" `
     *   "-Dsurefire.failIfNoSpecifiedTests=false" `
     *   "-Dflatten.skip=true" `
     *   "-Dardoco.neo4j.pauseSeconds=300"
     * }</pre>
     * That example pauses for 5 minutes after the pipeline writes to Neo4j.
     * When the pause ends, the test finishes and {@code @AfterEach} clears the DB.
     * Omit the property (or set it to {@code 0}) for a normal non-pausing run
     * (data is still cleared immediately after the test).
     *
     * <p>Alternative: run the test from the IDE and set a breakpoint after {@code runner.run()}.
     */
    protected static void pauseForNeo4jInspection() {
        long seconds;
        try {
            seconds = Long.parseLong(System.getProperty(PAUSE_SECONDS_PROPERTY, "0"));
        } catch (NumberFormatException ex) {
            System.out.println(">>> Invalid " + PAUSE_SECONDS_PROPERTY + "; expected an integer. Skipping pause.");
            return;
        }

        if (seconds <= 0) {
            return;
        }

        System.out.println(">>> Pipeline data is in Neo4j until this test ends.");
        System.out.println(">>> Open Browser NOW: " + neo4jBrowserUrl());
        System.out.println(">>> Pausing " + seconds + "s ( -D" + PAUSE_SECONDS_PROPERTY + " ).");
        System.out.println(">>> After the pause, @AfterEach will DELETE all graph data.");

        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(">>> Pause interrupted; continuing test (graph will still be cleared).");
        }

        System.out.println(">>> Pause finished; continuing assertions, then teardown clear.");
    }

    protected ImmutableSortedMap<String, String> getConfigsWithPersistence(boolean enabled) {
        boolean persistRecommendations = Boolean.parseBoolean(System.getProperty("ardoco.neo4j.persistRecommendations", "false"));
        boolean persistTextState = Boolean.parseBoolean(System.getProperty("ardoco.neo4j.persistTextState", "false"));
        return getConfigsWithPersistence(enabled, persistTextState, persistRecommendations);
    }

    protected ImmutableSortedMap<String, String> getConfigsWithPersistence(boolean enabled, boolean persistTextState, boolean persistRecommendations) {
        ImmutableSortedMap<String, String> configs = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        MutableSortedMap<String, String> additionalConfigs = SortedMaps.mutable.empty();
        additionalConfigs.putAll(configs.toSortedMap());
        putPersistenceFlags(additionalConfigs, enabled, persistTextState, persistRecommendations);
        return additionalConfigs.toImmutable();
    }

    protected ImmutableSortedMap<String, String> getInconsistencyConfigsWithPersistence(boolean enabled) throws Exception {
        var resource = getClass().getResource("/teastore/inconsistencyConfig.txt");
        if (resource == null) {
            throw new FileNotFoundException("Could not find config in resources");
        }
        boolean persistRecommendations = Boolean.parseBoolean(System.getProperty("ardoco.neo4j.persistRecommendations", "false"));
        boolean persistTextState = Boolean.parseBoolean(System.getProperty("ardoco.neo4j.persistTextState", "false"));
        ImmutableSortedMap<String, String> configs = ConfigurationHelper.loadAdditionalConfigs(new File(resource.toURI()));
        MutableSortedMap<String, String> additionalConfigs = configs.toSortedMap();
        putPersistenceFlags(additionalConfigs, enabled, persistTextState, persistRecommendations);
        return additionalConfigs.toImmutable();
    }

    private static void putPersistenceFlags(MutableSortedMap<String, String> additionalConfigs, boolean enabled, boolean persistTextState,
            boolean persistRecommendations) {
        additionalConfigs.put("PersistenceBridge::usePersistence", String.valueOf(enabled));
        additionalConfigs.put("PersistenceBridge::persistTextState", String.valueOf(persistTextState));
        additionalConfigs.put("PersistenceBridge::persistRecommendations", String.valueOf(persistRecommendations));
        boolean persistNer = Boolean.parseBoolean(System.getProperty("ardoco.neo4j.persistNerConnection", "false"));
        additionalConfigs.put("PersistenceBridge::persistNerConnection", String.valueOf(persistNer));
    }

    protected ImmutableSortedMap<String, String> getConfigsWithPersistence(boolean enabled, boolean persistTextState, boolean persistRecommendations,
            boolean persistNerConnection) {
        ImmutableSortedMap<String, String> configs = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        MutableSortedMap<String, String> map = SortedMaps.mutable.empty();
        map.putAll(configs.toSortedMap());
        map.put("PersistenceBridge::usePersistence", String.valueOf(enabled));
        map.put("PersistenceBridge::persistTextState", String.valueOf(persistTextState));
        map.put("PersistenceBridge::persistRecommendations", String.valueOf(persistRecommendations));
        map.put("PersistenceBridge::persistNerConnection", String.valueOf(persistNerConnection));
        return map.toImmutable();
    }

    /**
     * Architecture/code model items must carry {@code :Traceable} so link writes can use {@code traceable_id_idx}.
     */
    protected long countModelItemsMissingTraceableLabel() {
        return neo4jClient.query("""
                MATCH (n)
                WHERE (n:ArchitectureItem OR n:CodeItem) AND NOT n:Traceable
                RETURN count(n) AS c
                """)
                .fetch()
                .one()
                .map(row -> ((Number) row.get("c")).longValue())
                .orElse(0L);
    }

    protected long countNodesWithLabel(String label) {
        return neo4jClient.query("MATCH (n:" + label + ") RETURN count(n) AS c")
                .fetch()
                .one()
                .map(row -> ((Number) row.get("c")).longValue())
                .orElse(0L);
    }

    protected long countRelationshipsWithType(String relationshipType) {
        return neo4jClient.query("MATCH ()-[r:" + relationshipType + "]->() RETURN count(r) AS c")
                .fetch()
                .one()
                .map(row -> ((Number) row.get("c")).longValue())
                .orElse(0L);
    }

    protected StanfordCoreNLP getNLP() {
        if (pipeline == null) {
            java.util.Properties props = new java.util.Properties();
            props.setProperty("ner.useSUTime", "false");
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse");
            pipeline = new StanfordCoreNLP(props);
        }
        return pipeline;
    }
}
