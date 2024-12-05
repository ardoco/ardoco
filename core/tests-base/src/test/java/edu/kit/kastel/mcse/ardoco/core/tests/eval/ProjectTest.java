/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ProjectTest {

    @EnumSource(Project.class)
    @ParameterizedTest(name = "{0}")
    void testFiles(Project project) {
        Assertions.assertNotNull(project.getModelFile());
        Assertions.assertNotNull(project.getTextFile());
        Assertions.assertNotNull(project.getTlrGoldStandard());
        Assertions.assertNotNull(project.getMissingTextForModelElementGoldStandard());
        Assertions.assertNotNull(project.getExpectedTraceLinkResults());
        Assertions.assertNotNull(project.getExpectedInconsistencyResults());
    }
}
