/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.TextInconsistency;
import edu.kit.kastel.mcse.ardoco.core.common.JsonHandling;
import edu.kit.kastel.mcse.ardoco.id.execution.runner.InconsistencyDetection;

class InconsistencyGraphTargetTest extends AbstractPersistenceTest {

    @Test
    @DisplayName("TeaStore InconsistencyDetection, storage=neo4j: every text inconsistency hangs on Sentence n-1 for its 1-based sentence n")
    void textInconsistenciesAttachToTheirSentence() throws Exception {
        deleteExportedGraph();
        var runner = new InconsistencyDetection(projectName);
        runner.setUp(inputText, inputModelArchitecture, ModelFormat.PCM, getInconsistencyConfigsWithStorage(true), outputDir);
        testRunnerAssertions(runner);

        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        List<Integer> inMemorySentenceNumbers = new ArrayList<>(result.getAllTextInconsistencies().collect(TextInconsistency::getSentenceNumber).toList());
        Assertions.assertEquals(24, inMemorySentenceNumbers.size());

        JsonNode root = JsonHandling.createObjectMapper().readTree(new File(outputDir, "knowledgeGraph_" + projectName + ".json"));
        Map<String, JsonNode> nodesById = new HashMap<>();
        for (JsonNode node : root.get("nodes")) {
            nodesById.put(node.get("id").asText(), node);
        }
        List<String> wrongTargets = new ArrayList<>();
        List<Integer> graphSentenceNumbers = new ArrayList<>();
        for (JsonNode relationship : root.get("relationships")) {
            if (!"HAS_INCONSISTENCY".equals(relationship.get("type").asText())) {
                continue;
            }
            JsonNode source = nodesById.get(relationship.get("startNodeId").asText());
            JsonNode target = nodesById.get(relationship.get("endNodeId").asText());
            if (!hasLabel(source, "Sentence") || !hasLabel(target, "TextInconsistency")) {
                continue;
            }
            int sentenceIndex = source.get("properties").get("sentenceNumber").asInt();
            int inconsistencySentence = target.get("properties").get("sentenceNumber").asInt();
            graphSentenceNumbers.add(inconsistencySentence);
            if (sentenceIndex != inconsistencySentence - 1) {
                wrongTargets.add(target.get("properties").get("name").asText() + ": inconsistency sentence " + inconsistencySentence + " on Sentence "
                        + sentenceIndex);
            }
        }

        Assertions.assertEquals(List.of(), wrongTargets, "every edge must go to Sentence n-1");
        Assertions.assertEquals(24, graphSentenceNumbers.size(), "exactly 24 HAS_INCONSISTENCY edges from a Sentence");
        inMemorySentenceNumbers.sort(Integer::compare);
        graphSentenceNumbers.sort(Integer::compare);
        Assertions.assertEquals(inMemorySentenceNumbers, graphSentenceNumbers, "the graph carries exactly the in-memory sentence numbers");
    }

    private static boolean hasLabel(JsonNode node, String label) {
        for (JsonNode nodeLabel : node.get("labels")) {
            if (label.equals(nodeLabel.asText())) {
                return true;
            }
        }
        return false;
    }
}
