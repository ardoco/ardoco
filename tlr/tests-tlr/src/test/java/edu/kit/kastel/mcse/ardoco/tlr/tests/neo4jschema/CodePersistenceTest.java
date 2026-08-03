/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import static edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.CodeModelEqualityHelper.assertCodeModelsEqual;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.CodeExtractor;
import io.github.ardoco.core.neo4jschema.service.CodePersistenceService;

@Transactional
public class CodePersistenceTest extends AbstractPersistenceTest {

    @Autowired
    private CodePersistenceService persistenceService;

    @Test
    @DisplayName("Should persist and restore a Code Model")
    void testSaveAndLoadCodeModel() {
        // --- VISUALIZATION BLOCK ---
        System.out.println("----------------------------------------------------------");
        System.out.println("neo4j browser: " + neo4j.getHttpUrl()); // e.g., http://localhost:32789
        System.out.println("password:      " + neo4j.getAdminPassword());
        System.out.println("Connect URL:   " + neo4j.getBoltUrl());
        System.out.println("----------------------------------------------------------");

        File codeFile = codeConfiguration.code();

        CodeModel extractedModel = CodeExtractor.readInCodeModel(codeFile, Metamodel.CODE_WITH_COMPILATION_UNITS);
        persistenceService.saveCodeModel(extractedModel);
        CodeModel loadedModel = persistenceService.loadCodeModel(Metamodel.CODE_WITH_COMPILATION_UNITS).get();
        assertCodeModelsEqual(extractedModel, loadedModel);
    }
}
