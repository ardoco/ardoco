package edu.kit.kastel.mcse.ner_for_arch.recognizer;


import dev.langchain4j.model.chat.ChatModel;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TestProjectEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(TestProjectEvaluator.class);
    private final ChatModel model;
    private final boolean useGoldstandardComponentNames;
    private Prompt prompt;

    public TestProjectEvaluator(ChatModel model, Prompt prompt, boolean useGoldstandardComponentNames) {
        this.model = model;
        this.prompt = prompt;
        this.useGoldstandardComponentNames = useGoldstandardComponentNames;
    }

    public void evaluate(ComponentRecognitionParameterizedTest.TestProject project) {
        if (project == ComponentRecognitionParameterizedTest.TestProject.ALL) {
            evaluateAll();
        } else {
            evaluateSingle(project);
        }
    }

    private void evaluateAll() {
        Path evalResourcesPath = getEvaluationResourcesPath();
        Stream<Path> testProjectDirs = assertDoesNotThrow(() -> Files.list(evalResourcesPath));
        int[] errorCounter = {0};

        testProjectDirs
                .filter(Files::isDirectory)
                .forEach(dir -> {
                    try {
                        evaluateProjectInDirectory(dir);
                    } catch (Exception e) {
                        errorCounter[0]++;
                        logger.error("Evaluation failed for project: {}", dir.getFileName());
                        logger.info("-----------------------------------------------");
                    }
                });

        assertEquals(0, errorCounter[0], "There were errors in " + errorCounter[0] + " test project(s) during evaluation. Please check the log for details.");
    }


    private void evaluateSingle(ComponentRecognitionParameterizedTest.TestProject project) {
        Path evalResourcesPath = getEvaluationResourcesPath();
        Path projectDir = evalResourcesPath.resolve(project.name().toLowerCase());
        assertTrue(Files.exists(projectDir), "Test project directory does not exist: " + projectDir);

        assertDoesNotThrow(() -> evaluateProjectInDirectory(projectDir));
    }

    private Path getEvaluationResourcesPath() {
        URL evalResourcesUrl = this.getClass().getClassLoader().getResource("evaluation_resources");
        assertNotNull(evalResourcesUrl, "Evaluation resources not found");
        return assertDoesNotThrow(() -> Paths.get(evalResourcesUrl.toURI()));
    }

    private void evaluateProjectInDirectory(Path dir) {
        logger.info("Evaluating project: {}", dir.getFileName());

        Path goldstandardFile = findGoldstandardFile(dir);
        Path sadFile = findSadFile(dir);

        NamedEntityRecognizer.Builder builder = new NamedEntityRecognizer.Builder(sadFile).chatModel(model);
        if (prompt != null) {
            if (useGoldstandardComponentNames) { //add goldstandard component names if wanted:
                //TODO: probably add sth like: + ".\nUse the names of this list as main name of the recognized components." (to improve effect of this information)
                String componentSupportList = "\nAs support, here is the complete list of all architecturally relevant components mentioned in the text:\n" + GoldstandardParser.getComponentNames(dir);
                prompt = new Prompt(prompt.first() + componentSupportList, prompt.second(), prompt.type());
            }

            builder = builder.prompt(prompt);
        }
        NamedEntityRecognizer recognizer = builder.build();

        Set<NamedEntity> components = recognizer.recognize();
        Set<NamedEntity> groundTruth = assertDoesNotThrow(() -> GoldstandardParser.parse(goldstandardFile));
        //System.out.println("recognized:\n" + components);
        //System.out.println("-----------------------------------------------");
        //System.out.println("groundTruth:\n" + groundTruth);

        matchAndLogResults(components, groundTruth);
        logger.info("-----------------------------------------------");
    }

    private Path findGoldstandardFile(Path dir) {
        return assertDoesNotThrow(() ->
                Files.list(dir.resolve("goldstandards"))
                        .filter(p -> p.getFileName().toString().contains("goldstandard_NER.csv"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No goldstandard file in " + dir))
        );
    }

    private Path findSadFile(Path dir) {
        Path sadDir = assertDoesNotThrow(() ->
                Files.list(dir)
                        .filter(Files::isDirectory)
                        .filter(p -> p.getFileName().toString().contains("text_"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No SAD directory in " + dir))
        );

        return assertDoesNotThrow(() ->
                Files.list(sadDir)
                        .filter(p -> p.getFileName().toString().endsWith("_1SentPerLine.txt"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No SAD file in " + sadDir))
        );
    }

    private void matchAndLogResults(Set<NamedEntity> components, Set<NamedEntity> groundTruth) {
        matchComponentNames(groundTruth, components);

        Set<SimpleComponentOccurrence> componentsOccurrences = SimpleComponentOccurrence.fromComponents(components);
        Set<SimpleComponentOccurrence> groundTruthOccurrences = SimpleComponentOccurrence.fromComponents(groundTruth);

        ClassificationMetricsCalculator calculator = ClassificationMetricsCalculator.getInstance();
        SingleClassificationResult<SimpleComponentOccurrence> result = calculator.calculateMetrics(componentsOccurrences, groundTruthOccurrences, null);

        //logger.info("Precision = {}; Recall = {}; F1-Score = {}", result.getPrecision(), result.getRecall(), result.getF1());
        result.prettyPrint();
    }

    /**
     * Matches components between the ground truth and recognized components based on their names.
     * If a name match is found, the names of both components are unified to the matching name.
     * This alignment facilitates metrics calculation by the {@link ClassificationMetricsCalculator} treating them as equivalent.
     *
     * @param groundTruth          the set of goldstandard components
     * @param recognizedComponents the set of recognized components
     */
    private void matchComponentNames(Set<NamedEntity> groundTruth, Set<NamedEntity> recognizedComponents) {
        for (NamedEntity component : groundTruth) {
            component.makeAllNamesLowerCase();
        }
        for (NamedEntity component : recognizedComponents) {
            component.makeAllNamesLowerCase();
        }

        for (NamedEntity component : recognizedComponents) {
            //one of the possible names of the recognized component needs to match one of the possible names of a ground-truth-component for them to be "equivalent":
            boolean foundEquivalentComponent = false;
            Set<String> componentNamePool = new HashSet<>(component.getAlternativeNames());
            componentNamePool.add(component.getName());
            for (NamedEntity groundTruthComponent : groundTruth) {
                if (foundEquivalentComponent) {
                    break; //because we assume that there is only one possible match to be found
                }
                Set<String> groundTruthComponentNamePool = new HashSet<>(groundTruthComponent.getAlternativeNames());
                groundTruthComponentNamePool.add(groundTruthComponent.getName());
                for (String componentName : componentNamePool) {
                    if (groundTruthComponentNamePool.contains(componentName)) {
                        foundEquivalentComponent = true;
                        //in this case: componentName is the name that joins/matches both components
                        component.changeName(componentName);
                        groundTruthComponent.changeName(componentName);
                        //System.out.println("MATCH: " + componentName);
                        break; //because we assume that there is only one possible match to be found
                    }
                }
            }
        }
    }
}
