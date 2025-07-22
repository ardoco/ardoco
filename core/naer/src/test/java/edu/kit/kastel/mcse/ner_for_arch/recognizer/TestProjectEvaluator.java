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

/**
 * The TestProjectEvaluator class is responsible for evaluating component recognition within test projects.
 * It compares recognized components against ground-truth components to assess matching performance using precision, recall, and F1-score metrics.
 */
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

    // We do this because sometimes the LLM returns names like "gui component" but we use names like "gui" in the goldstandards
    private static String cleanComponentName(String input) {
        // Remove the word "component" (case-insensitive) and trim extra whitespace
        return input.replaceAll("(?i)\\bcomponent\\b", "").replaceAll("\\s+", " ").trim();
    }

    /**
     * Evaluates the specified test project or all test projects depending on the input.
     * If the provided project is {@code ComponentRecognitionParameterizedTest.TestProject.ALL},
     * evaluates all test projects. Otherwise, evaluates the specified single test project.
     *
     * @param project the test project to evaluate. If {@code ALL}, all test projects will be evaluated.
     */
    public void evaluate(ComponentRecognitionParameterizedTest.TestProject project) {
        if (project == ComponentRecognitionParameterizedTest.TestProject.ALL) {
            evaluateAll();
        } else {
            evaluateSingle(project);
        }
    }

    /**
     * Evaluates all test projects located in the evaluation resources directory.
     * For each project directory, it invokes the evaluation process and verifies
     * the outputs against the expected results. The method ensures that all project
     * evaluations complete without errors, and logs any issues encountered.
     * <p>
     * Throws an assertion failure if any test project evaluations fail, providing
     * the count of failed projects in the error message.
     */
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

    /**
     * Evaluates a single test project
     *
     * @param project the test project to evaluate, represented as an instance of {@link ComponentRecognitionParameterizedTest.TestProject}.
     */
    private void evaluateSingle(ComponentRecognitionParameterizedTest.TestProject project) {
        Path evalResourcesPath = getEvaluationResourcesPath();
        Path projectDir = evalResourcesPath.resolve(project.name().toLowerCase());
        assertTrue(Files.exists(projectDir), "Test project directory does not exist: " + projectDir);

        assertDoesNotThrow(() -> evaluateProjectInDirectory(projectDir));
    }

    /**
     * Retrieves the path to the evaluation resources directory.
     * Ensures that the specified directory exists and can be converted into a {@link Path}.
     * If the directory is unavailable or inaccessible, an assertion error is thrown.
     *
     * @return the {@link Path} to the evaluation resources directory
     */
    private Path getEvaluationResourcesPath() {
        URL evalResourcesUrl = this.getClass().getClassLoader().getResource("evaluation_resources");
        assertNotNull(evalResourcesUrl, "Evaluation resources not found");
        return assertDoesNotThrow(() -> Paths.get(evalResourcesUrl.toURI()));
    }

    /**
     * Evaluates the project located in the specified directory by processing the necessary files,
     * calling the component recognition, and comparing the results with the goldstandard data.
     *
     * @param dir the directory containing the project files to be evaluated
     */
    private void evaluateProjectInDirectory(Path dir) {
        logger.info("Evaluating project: {}", dir.getFileName());

        Path goldstandardFile = findGoldstandardFile(dir);
        Path sadFile = findSadFile(dir);

        NamedEntityRecognizer.Builder builder = new NamedEntityRecognizer.Builder(sadFile).chatModel(model);
        if (prompt != null) {
            builder = builder.prompt(prompt);
        }
        NamedEntityRecognizer recognizer = builder.build();

        Set<NamedEntity> components = useGoldstandardComponentNames ? recognizer.recognize(GoldstandardParser.getPossibleComponents(dir)) : recognizer.recognize();
        Set<NamedEntity> groundTruth = assertDoesNotThrow(() -> GoldstandardParser.parse(goldstandardFile));
        //System.out.println("recognized:\n" + components);
        //System.out.println("-----------------------------------------------");
        //System.out.println("groundTruth:\n" + groundTruth);

        matchAndLogResults(components, groundTruth);
        logger.info("-----------------------------------------------");
    }

    /**
     * Searches for and retrieves the path of a goldstandard file named "goldstandard_NER.csv"
     * within the "goldstandards" subdirectory of the specified directory.
     * Throws a runtime exception if no such file is found.
     *
     * @param dir the root directory to search for the goldstandard file
     * @return the {@link Path} to the first matching goldstandard file
     */
    private Path findGoldstandardFile(Path dir) {
        return assertDoesNotThrow(() ->
                Files.list(dir.resolve("goldstandards"))
                        .filter(p -> p.getFileName().toString().contains("goldstandard_NER.csv"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No goldstandard file in " + dir))
        );
    }

    /**
     * Searches for and retrieves the path to a specific SAD file within a given directory.
     * The method first identifies a subdirectory under the specified directory whose name
     * contains the substring "text_". Then, it looks for a file in that subdirectory
     * whose name ends with "_1SentPerLine.txt". If no such file or directory is found,
     * a runtime exception is thrown.
     *
     * @param dir the root directory to search for the SAD file
     * @return the {@link Path} to the found SAD file
     */
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

    /**
     * Matches the given components with the ground truth, evaluates the matching results,
     * and logs classification metrics such as precision, recall, and F1-score.
     *
     * @param components  the set of recognized components to be evaluated
     * @param groundTruth the set of goldstandard components to compare against
     */
    private void matchAndLogResults(Set<NamedEntity> components, Set<NamedEntity> groundTruth) {
        matchComponentNames(groundTruth, components);

        Set<SimpleComponentOccurrence> componentsOccurrences = SimpleComponentOccurrence.fromComponents(components);
        Set<SimpleComponentOccurrence> groundTruthOccurrences = SimpleComponentOccurrence.fromComponents(groundTruth);

        //System.out.println("componentsOccurrences: " + componentsOccurrences);
        //System.out.println("groundTruthOccurrences: " + groundTruthOccurrences);

        ClassificationMetricsCalculator calculator = ClassificationMetricsCalculator.getInstance();
        SingleClassificationResult<SimpleComponentOccurrence> result = calculator.calculateMetrics(componentsOccurrences, groundTruthOccurrences, null);

        //logger.info("Precision = {}; Recall = {}; F1-Score = {}", result.getPrecision(), result.getRecall(), result.getF1());
        //System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||");
        result.prettyPrint();
        //System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||");
        System.out.println("false positives (= zu viel): " + result.getFalsePositives().stream().sorted().toList());
        System.out.println("false negatives (= fehlt): " + result.getFalseNegatives().stream().sorted().toList());
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
            componentNamePool.add(cleanComponentName(component.getName()));
            for (NamedEntity groundTruthComponent : groundTruth) {
                if (foundEquivalentComponent) {
                    break; //because we assume that there is only one possible match to be found
                }
                Set<String> groundTruthComponentNamePool = new HashSet<>(groundTruthComponent.getAlternativeNames());
                groundTruthComponentNamePool.add(cleanComponentName(groundTruthComponent.getName()));
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
