package io.github.ardoco.core.neo4jschema;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;

public abstract class AbstractNeo4jTest {
    static final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:5")
            .withReuse(true);

    static {
        neo4jContainer.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
    }
}
