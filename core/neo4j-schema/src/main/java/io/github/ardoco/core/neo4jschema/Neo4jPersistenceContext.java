/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lean Spring Boot application used by {@link Neo4jPersistenceHandlerProvider} to bootstrap Neo4j persistence
 * outside of tests (the production {@link edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandlerProvider} path).
 *
 * <p>Unlike {@link Main}, which scans all of {@code edu.kit.kastel} (convenient for
 * {@code @SpringBootTest(classes = Main.class)} in other modules), this context is scoped to this module's own
 * package: every bean it needs ({@link Neo4jPersistenceHandler}, the persistence services, the repositories,
 * {@link Neo4jBridgeActivator}, {@link Neo4jInitializer}) lives under {@code io.github.ardoco.core.neo4jschema}, and
 * nothing outside it has Spring beans (checked when this was introduced). Scoping narrowly keeps the context cheap to
 * start and avoids pulling in unrelated classes from the wider ArDoCo codebase.
 *
 * @see Neo4jPersistenceHandlerProvider
 */
@SpringBootApplication(scanBasePackages = "io.github.ardoco.core.neo4jschema")
public class Neo4jPersistenceContext {
}
