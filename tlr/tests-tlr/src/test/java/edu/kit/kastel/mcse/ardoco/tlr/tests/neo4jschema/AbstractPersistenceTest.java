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

import edu.kit.kastel.mcse.ardoco.core.execution.CodeRunnerBaseTest;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import io.github.ardoco.core.neo4jschema.Neo4jPersistenceHandler;

/**
 * Base class for TLR persistence integration tests against a local Neo4j instance.
 *
 * <h2>Local setup (Neo4j Desktop)</h2>
 * <ul>
 *   <li>Start the Desktop DBMS before running tests (status must be RUNNING).</li>
 *   <li>Bolt URI uses {@code 127.0.0.1} (avoids Windows {@code localhost} → IPv6 quirks).</li>
 *   <li>Keep credentials in sync with Desktop / repo {@code .env}.</li>
 *   <li>Stop any Docker Neo4j on ports 7474/7687 to avoid conflicts.</li>
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

    /**
     * Used by {@link #clearNeo4jAfterTest()} to wipe the graph after each test.
     * Data remains available only until that teardown — i.e. through any in-test pause.
     */
    @Autowired
    private Neo4jPersistenceHandler neo4jPersistenceHandler;

    @Autowired
    private Neo4jClient neo4jClient;

    /**
     * Wires Spring Data Neo4j to the local Desktop instance.
     * Prefer {@code 127.0.0.1} over {@code localhost} on Windows.
     */
    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", () -> "bolt://127.0.0.1:7687");
        registry.add("spring.neo4j.authentication.username", () -> NEO4J_USER);
        registry.add("spring.neo4j.authentication.password", () -> NEO4J_PASSWORD);
    }

    private static StanfordCoreNLP pipeline;

    @BeforeEach
    void setUp() {
        System.out.println("----------------------------------------------------------");
        System.out.println("neo4j browser: " + NEO4J_BROWSER);
        System.out.println("username:      " + NEO4J_USER);
        System.out.println("password:      " + NEO4J_PASSWORD);
        System.out.println("Connect URL:   bolt://127.0.0.1:7687");
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
        System.out.println(">>> @AfterEach: clearing all Neo4j test data (MATCH (n) DETACH DELETE n)...");
        clearNeo4jGraph();
        System.out.println(">>> Neo4j graph cleared.");
    }

    protected void clearNeo4jGraph() {
        neo4jPersistenceHandler.deleteAllData();
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
        System.out.println(">>> Open Browser NOW: " + NEO4J_BROWSER);
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
        boolean persistTextState = Boolean.parseBoolean(System.getProperty("ardoco.neo4j.persistTextState", "false")) || persistRecommendations;
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
        boolean persistTextState = Boolean.parseBoolean(System.getProperty("ardoco.neo4j.persistTextState", "false")) || persistRecommendations;
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
