/* Licensed under MIT 2024-2026. */
package edu.kit.kastel.mcse.ardoco.id.tests;

import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses(packages = { "edu.kit.kastel.mcse.ardoco.core", "edu.kit.kastel.mcse.ardoco.tlr", "edu.kit.kastel.mcse.ardoco.id" })
public class DeterministicArdocoTest extends edu.kit.kastel.mcse.ardoco.core.tests.architecture.DeterministicArdocoTest {
    // Has to be executed in this module
}
