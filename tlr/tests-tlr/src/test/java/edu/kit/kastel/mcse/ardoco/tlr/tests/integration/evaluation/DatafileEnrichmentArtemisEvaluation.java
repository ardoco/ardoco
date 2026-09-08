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

public class DatafileEnrichmentArtemisEvaluation extends DatafileArtemisEvaluation {

    private final int enrichmentNumber;

    public DatafileEnrichmentArtemisEvaluation(ArtemisEvaluationProject project, LargeLanguageModel llmForNer, int enrichmentNumber) {
        super(project, llmForNer);
        this.enrichmentNumber = enrichmentNumber;
    }

    protected File getDocumentationFile() {
        return new File(Objects.requireNonNull(this.getClass()
                .getResource("/datafile-artemis-text-enrichments/" + project.getName().toLowerCase() + "/enrichment-" + enrichmentNumber + ".txt")).getFile());
    }

    @Override
    public SingleClassificationResult<String> calculateEvaluationResults(ArdocoResult result, List<Pair<Integer, String>> goldStandard,
            ArtemisNerStrategy strategy) {
        goldStandard.addAll(getExpectedEnrichmentTraceLinks());
        return super.calculateEvaluationResults(result, goldStandard, strategy);
    }

    private List<Pair<Integer, String>> getExpectedEnrichmentTraceLinks() {
        File file = EvaluationHelper.loadFileFromResources(
                "/datafile-artemis-text-enrichments/" + project.getName().toLowerCase() + "/goldstandard-enrichment-" + enrichmentNumber + ".csv");

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
