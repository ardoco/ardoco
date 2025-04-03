/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.models.generators.uml;

import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.UmlExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.generators.ArchitectureExtractorTest;

class UmlExtractorTest extends ArchitectureExtractorTest {

    @Test
    void extractorTest() {
        UmlExtractor umlExtractor = new UmlExtractor("src/test/resources/mediastore/architecture/uml/ms.uml");
        ArchitectureModel model = umlExtractor.extractModel();
        checkModel(model);
    }
}
