/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.junit.jupiter.api.AfterEach;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.CodeRunnerBaseTest;

@Testcontainers
@SpringBootTest(classes = io.github.ardoco.core.neo4jschema.Main.class, properties = {
        "spring.data.neo4j.repositories.type=imperative",
        "spring.neo4j.pool.metrics-enabled=false"
})
public abstract class AbstractPersistenceTest extends CodeRunnerBaseTest {

    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5").withRandomPassword();

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
    }

    private static StanfordCoreNLP pipeline;


    @AfterEach
    void clearDatabase() {
        try (var driver = GraphDatabase.driver(neo4j.getBoltUrl(),
                AuthTokens.basic("neo4j", neo4j.getAdminPassword()));
                Session session = driver.session()) {

            session.run("MATCH (n) DETACH DELETE n");
        }
    }

    /**
     * Common helper to build the configuration map with the persistence toggle.
     */
    protected ImmutableSortedMap<String, String> getConfigsWithPersistence(boolean enabled) {
        ImmutableSortedMap<String, String> configs = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        MutableSortedMap<String, String> additionalConfigs = SortedMaps.mutable.empty();
        additionalConfigs.putAll(configs.toSortedMap());
        additionalConfigs.put("PersistenceBridge::usePersistence", String.valueOf(enabled));
        return additionalConfigs.toImmutable();
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
