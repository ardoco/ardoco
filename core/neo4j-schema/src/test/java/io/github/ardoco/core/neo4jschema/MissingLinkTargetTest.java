/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedSets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnitsAndPackages;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.RecommendedInstanceImpl;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.NounMappingImpl;
import io.github.ardoco.core.neo4jschema.service.MissingLinkTargetException;
import io.github.ardoco.core.neo4jschema.util.models.ArchitectureModelFactory;
import io.github.ardoco.core.neo4jschema.util.models.TextFactory;

@SpringBootTest(classes = Main.class)
class MissingLinkTargetTest extends AbstractNeo4jTest {

    private static final Claimant TEST_CLAIMANT = new Claimant() {
    };

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void setUp() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @AfterEach
    void tearDown() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    @DisplayName("NounMapping whose words and phrases do not exist: one ERROR, MissingLinkTargetException")
    void nounMappingWithMissingWords() {
        NounMapping nounMapping = unsavedNounMapping();

        String error = assertOneErrorAndException(() -> persistenceHandler.saveNounMappings(List.of(nounMapping)));

        Assertions.assertTrue(error.contains("NounMapping"), error);
        Assertions.assertTrue(error.contains(nounMapping.getArdocoId()), error);
    }

    @Test
    @DisplayName("RecommendedInstance whose noun mapping does not exist: one ERROR, MissingLinkTargetException")
    void recommendedInstanceWithMissingNounMapping() {
        RecommendedInstance recommendedInstance = new RecommendedInstanceImpl("Ghost", "component", TEST_CLAIMANT, 0.5, Lists.immutable.with(
                unsavedNounMapping()), Lists.immutable.empty());

        String error = assertOneErrorAndException(() -> persistenceHandler.saveRecommendedInstances(List.of(recommendedInstance),
                Metamodel.ARCHITECTURE_WITH_COMPONENTS));

        Assertions.assertTrue(error.contains("RecommendedInstance"), error);
        Assertions.assertTrue(error.contains(recommendedInstance.getId()), error);
    }

    @Test
    @DisplayName("Code model whose EXTENDS target does not exist: one ERROR, MissingLinkTargetException")
    void codeModelWithMissingExtendsTarget() {
        CodeItemRepository repository = new CodeItemRepository();
        CodePackage codePackage = new CodePackage("missing_target_pkg", repository, "edu.kit.missing");
        CodeCompilationUnit compilationUnit = new CodeCompilationUnit("missing_target_cu", repository, "Missing", new TreeSet<>(), List.of("edu", "kit",
                "missing"), "java", ProgrammingLanguage.JAVA);
        ClassUnit classUnit = new ClassUnit("missing_target_cls", repository, "Missing", new TreeSet<>());
        codePackage.addContent(compilationUnit);
        compilationUnit.setParent(codePackage);
        compilationUnit.addContent(classUnit);
        classUnit.setCompilationUnit(compilationUnit);
        repository.init();
        classUnit.setExtendedDataTypesIds(List.of("ghost-code-id"));
        SortedSet<CodeItem> roots = new TreeSet<>(List.of(codePackage));
        var codeModel = new CodeModelWithCompilationUnitsAndPackages("missing_target_code_model", repository, roots);

        String error = assertOneErrorAndException(() -> persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel));

        Assertions.assertTrue(error.contains("CodeModel"), error);
        Assertions.assertTrue(error.contains("EXTENDS"), error);
        Assertions.assertTrue(error.contains("ghost-code-id"), error);
    }

    @Test
    @DisplayName("Architecture model whose item id also belongs to a foreign architecture node: one ERROR (ambiguous), MissingLinkTargetException")
    void architectureModelWithAmbiguousItem() {
        neo4jClient.query("CREATE (:ArchitectureComponent:ArchitectureItem:Traceable {ardocoId: 'id_comp_auth', name: 'Foreign'})").run();

        String error = assertOneErrorAndException(() -> persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES,
                ArchitectureModelFactory.createArchitectureModel()));

        Assertions.assertTrue(error.contains("ArchitectureModel"), error);
        Assertions.assertTrue(error.contains("id_comp_auth"), error);
        Assertions.assertTrue(error.contains("ambiguous"), error);
    }

    private static NounMapping unsavedNounMapping() {
        Text text = TextFactory.createComplexText("unsaved_noun_mapping_text");
        Word word = text.getSentences().get(0).getWords().get(1);
        return new NounMappingImpl(SortedSets.immutable.with(word), MappingKind.NAME, TEST_CLAIMANT, 1.0, Lists.immutable.with(word), Lists.immutable.with(word
                .getText()));
    }

    private static String assertOneErrorAndException(Executable action) {
        Logger logger = (Logger) LoggerFactory.getLogger("io.github.ardoco.core.neo4jschema");
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        MissingLinkTargetException thrown;
        try {
            thrown = Assertions.assertThrows(MissingLinkTargetException.class, action);
        } finally {
            logger.detachAppender(appender);
        }
        List<String> errors = appender.list.stream().filter(event -> event.getLevel() == Level.ERROR).map(ILoggingEvent::getFormattedMessage).toList();
        Assertions.assertEquals(1, errors.size(), errors.toString());
        Assertions.assertEquals(errors.getFirst(), thrown.getMessage());
        return errors.getFirst();
    }
}
