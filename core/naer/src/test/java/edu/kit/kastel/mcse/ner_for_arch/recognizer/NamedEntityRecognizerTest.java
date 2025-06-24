package edu.kit.kastel.mcse.ner_for_arch.recognizer;

import dev.langchain4j.model.chat.ChatModel;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ner_for_arch.util.ChatModelFactory;
import edu.kit.kastel.mcse.ner_for_arch.util.ModelProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NamedEntityRecognizerTest {
    private final Logger logger = LoggerFactory.getLogger(NamedEntityRecognizerTest.class);

    @Test
    @Disabled("Nur bei Bedarf aktivieren")
    void evaluateComponentRecognitionVDL() {
        logger.info("Evaluating component recognition (using VDL model) ...");

        ChatModel model = ChatModelFactory.withProvider(ModelProvider.VDL).build();

        evaluateAllTestProjects(model);
    }

    private void evaluateAllTestProjects(ChatModel model) {
        URL evalResourcesUrl = this.getClass().getClassLoader().getResource("evaluation_resources");
        assertNotNull(evalResourcesUrl);
        Path evalResourcesPath = assertDoesNotThrow(() -> Paths.get(evalResourcesUrl.toURI()));
        Stream<Path> testProjectPaths = assertDoesNotThrow(() -> Files.list(evalResourcesPath));

        testProjectPaths.filter(Files::isDirectory).forEach(dir -> {
            assertDoesNotThrow(() -> {
                //search goldstandard file
                Path goldstandardFilePath = assertDoesNotThrow(() ->
                        Files.list(dir.resolve("goldstandards"))
                                .filter(p -> p.getFileName().toString().contains("goldstandard_NER.csv"))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No goldstandard file"))
                );

                //search SAD file
                Path sadDirPath = assertDoesNotThrow(() ->
                        Files.list(dir)
                                .filter(Files::isDirectory)
                                .filter(p -> p.getFileName().toString().contains("text_"))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No SAD subdir in " + dir))
                );
                Path sadFilePath = assertDoesNotThrow(() ->
                        Files.list(sadDirPath)
                                .filter(p -> p.getFileName().toString().endsWith("_1SentPerLine.txt"))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No SAD file in " + sadDirPath))
                );

                evaluateSingleTestProject(dir, model, sadFilePath, goldstandardFilePath);
            });
        });
    }

    private void evaluateSingleTestProject(Path dir, ChatModel model, Path sadFilePath, Path goldstandardFilePath) {
        logger.info("Test project \"{}\":", dir.getFileName());

        NamedEntityRecognizer componentRecognizer = new NamedEntityRecognizer.Builder(sadFilePath).chatModel(model).build();
        Set<NamedEntity> components = componentRecognizer.recognize();

        Set<NamedEntity> groundTruth = assertDoesNotThrow(() -> GoldstandardParser.parse(goldstandardFilePath));

        /*System.out.println(components);
        System.out.println("-----------------------------------------------");
        System.out.println(groundTruth);
        System.out.println("-----------------------------------------------");*/

        matchComponentNames(groundTruth, components);
        Set<SimpleComponentOccurrence> componentsOccurrences = SimpleComponentOccurrence.fromComponents(components);
        Set<SimpleComponentOccurrence> groundTruthOccurrences = SimpleComponentOccurrence.fromComponents(groundTruth);

        //use ArDoCo ClassificationMetricsCalculator to calculate metrics
        ClassificationMetricsCalculator calculator = ClassificationMetricsCalculator.getInstance();
        SingleClassificationResult<SimpleComponentOccurrence> result = calculator.calculateMetrics(componentsOccurrences, groundTruthOccurrences, null);
        result.prettyPrint();
        logger.info("Recognized Components: {}", components);
        logger.info("--------------------------------------------------------------------------------------------------");
    }

    //todo javadoc Erklärung: "findet matching components und changed bei beiden den name auf den matchingName (damit metric calc es checkt)"
    private void matchComponentNames(Set<NamedEntity> groundTruth, Set<NamedEntity> recognizedComponents) {
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
                        break; //because we assume that there is only one possible match to be found //todo what if not? => vllt wenn ein parameter aktiviert ist weiter laufen lassen und bei multiple matches ein warning ausgeben
                    }
                }
            }
        }
    }


    @Test
    @Disabled("Nur bei Bedarf aktivieren. ACHTUNG KANN TEUER WERDEN!")
    public void evaluateComponentRecognitionOpenAI() {
        logger.info("Evaluating component recognition with OpenAI model...");

        ChatModel model = ChatModelFactory.withProvider(ModelProvider.OPEN_AI).build();

        evaluateAllTestProjects(model);
    }
}