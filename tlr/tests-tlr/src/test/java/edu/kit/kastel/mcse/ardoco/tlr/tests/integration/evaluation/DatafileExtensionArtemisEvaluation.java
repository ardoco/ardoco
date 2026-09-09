package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationHelper;
import edu.kit.kastel.mcse.ardoco.metrics.result.SingleClassificationResult;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;
import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArtemisEvaluationProject;

public class DatafileExtensionArtemisEvaluation extends DatafileArtemisEvaluation {

    private final int extensionNumber;

    public DatafileExtensionArtemisEvaluation(ArtemisEvaluationProject project, LargeLanguageModel llmForNer, int extensionNumber) {
        super(project, llmForNer);
        this.extensionNumber = extensionNumber;
    }

    protected File getDocumentationFile() {
        return new File(Objects.requireNonNull(this.getClass()
                .getResource("/datafile-artemis-text-extensions/" + project.getName().toLowerCase() + "/extension-" + extensionNumber + ".txt")).getFile());
    }

    @Override
    public SingleClassificationResult<String> calculateEvaluationResults(ArdocoResult result, List<Pair<Integer, String>> goldStandard,
            ArtemisNerStrategy strategy) {
        goldStandard.addAll(getExpectedExtendedTraceLinks());
        return super.calculateEvaluationResults(result, goldStandard, strategy);
    }

    private List<Pair<Integer, String>> getExpectedExtendedTraceLinks() {
        File file = EvaluationHelper.loadFileFromResources(
                "/datafile-artemis-text-extensions/" + project.getName().toLowerCase() + "/goldstandard-extension-" + extensionNumber + ".csv");

        List<String> goldLinks;
        try {
            goldLinks = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        goldLinks.removeFirst(); // remove header
        goldLinks.removeIf(String::isBlank);

        List<Pair<Integer, String>> expectedLinks = new ArrayList<>();
        for (String line : goldLinks) {
            String[] parts = line.split(",");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid gold standard format: " + line);
            }
            int sentenceId = Integer.parseInt(parts[0].trim());
            String modelElementId = parts[1].trim();
            expectedLinks.add(new Pair<>(sentenceId, modelElementId));
        }

        return expectedLinks;
    }

}
