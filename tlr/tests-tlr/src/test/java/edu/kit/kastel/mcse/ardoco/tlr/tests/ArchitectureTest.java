/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.tests;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;

/**
 * Runs the shared {@link edu.kit.kastel.mcse.ardoco.core.tests.architecture.ArchitectureTest} against the full TLR classpath (it has to be executed in this
 * module so that all stages are on the classpath).
 * <p>
 * Two groups of classes are deliberately excluded from the analysis:
 * <ul>
 * <li>The Neo4j persistence module ({@code io.github.ardoco.core.neo4jschema}). It is a cross-cutting persistence/adapter layer that intentionally depends on
 * domain types from every stage (models, trace links, inconsistencies, recommendations), so it does not fit the layered-architecture rules that apply to the
 * pipeline stages.</li>
 * <li>The shared execution test base classes ({@code RunnerBaseTest} / {@code CodeRunnerBaseTest}). They are pulled in via the {@code pipeline-core} test-jar and
 * use test-only conveniences ({@code System.getenv}, {@code Stream.forEach}) that the production architecture rules forbid.</li>
 * </ul>
 */
@AnalyzeClasses(packages = { "edu.kit.kastel.mcse.ardoco.core", "edu.kit.kastel.mcse.ardoco.tlr" }, importOptions = ArchitectureTest.ExcludePersistenceAndSharedTestBases.class)
public class ArchitectureTest extends edu.kit.kastel.mcse.ardoco.core.tests.architecture.ArchitectureTest {
    // Has to be executed in this module

    /**
     * Excludes the Neo4j persistence module and the shared execution test base classes from the architecture analysis.
     * {@code RunnerBaseTest} as a substring also matches {@code CodeRunnerBaseTest}.
     */
    static final class ExcludePersistenceAndSharedTestBases implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return !location.contains("neo4jschema") && !location.contains("RunnerBaseTest");
        }
    }
}
