/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CodeProjectTest {
    @EnumSource(CodeProject.class)
    @ParameterizedTest(name = "{0}")
    void testFiles(CodeProject project) {
        Assertions.assertNotNull(project);
        Assertions.assertNotNull(project.getCodeRepository());
        Assertions.assertNotNull(project.getCommitHash());
        Assertions.assertNotNull(project.getCodeLocation(true));
        Assertions.assertNotNull(project.getExpectedResultsForSamCode());
        Assertions.assertNotNull(project.getExpectedResultsForSadSamCode());
        Assertions.assertNotNull(project.getSamCodeGoldStandard());
        Assertions.assertNotNull(project.getSadCodeGoldStandard());
    }
}
