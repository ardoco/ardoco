/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;

public final class PersistenceTestSupport {

    private PersistenceTestSupport() {
    }

    public static Collection<Metamodel> activeMetamodels(DataRepository dataRepository) {
        return dataRepository.getData(ModelStates.ID, ModelStates.class).orElseThrow().getMetamodels();
    }

    public static ExportedGraph readExportedGraph(File outputDirectory, String projectName) {
        File file = new File(outputDirectory, "knowledgeGraph_" + projectName + ".json");
        if (!file.isFile()) {
            throw new IllegalStateException("Exported knowledge graph not found: " + file.getAbsolutePath());
        }
        try {
            return ExportedGraph.read(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public record ExportedGraph(long nodeCount, long relationshipCount, List<JsonNode> nodes, List<JsonNode> relationships) {

        public static ExportedGraph read(File file) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(file);
            List<JsonNode> nodes = new ArrayList<>();
            root.get("nodes").forEach(nodes::add);
            List<JsonNode> relationships = new ArrayList<>();
            root.get("relationships").forEach(relationships::add);
            return new ExportedGraph(root.get("nodeCount").asLong(), root.get("relationshipCount").asLong(), nodes, relationships);
        }

        private Map<String, JsonNode> nodesById() {
            Map<String, JsonNode> byId = new HashMap<>();
            for (JsonNode node : nodes) {
                byId.put(node.get("id").asText(), node);
            }
            return byId;
        }

        public long countNodesWithLabel(String label) {
            return nodes.stream().filter(node -> node.get("labels").toString().contains("\"" + label + "\"")).count();
        }

        public long countRelationshipsWithType(String type) {
            return relationships.stream().filter(rel -> type.equals(rel.get("type").asText())).count();
        }

        public long countTraceLinksOfType(String traceLinkType) {
            return relationships.stream()
                    .filter(rel -> "TRACES_TO".equals(rel.get("type").asText()))
                    .filter(rel -> traceLinkType.equals(rel.get("properties").get("traceLinkType").asText()))
                    .count();
        }

        public long countNodesWithLabelWithoutOutgoing(String label, Set<String> relationshipTypes) {
            Set<String> withOutgoing = new HashSet<>();
            for (JsonNode rel : relationships) {
                if (relationshipTypes.contains(rel.get("type").asText())) {
                    withOutgoing.add(rel.get("startNodeId").asText());
                }
            }
            return nodes.stream()
                    .filter(node -> node.get("labels").toString().contains("\"" + label + "\""))
                    .filter(node -> !withOutgoing.contains(node.get("id").asText()))
                    .count();
        }

        public long countDanglingRelationships() {
            Map<String, JsonNode> byId = nodesById();
            return relationships.stream()
                    .filter(rel -> !byId.containsKey(rel.get("startNodeId").asText()) || !byId.containsKey(rel.get("endNodeId").asText()))
                    .count();
        }

        public long countDuplicateTraceLinks() {
            Set<String> seen = new HashSet<>();
            long duplicates = 0;
            for (JsonNode rel : relationships) {
                if (!"TRACES_TO".equals(rel.get("type").asText())) {
                    continue;
                }
                String key = rel.get("startNodeId").asText() + "|" + rel.get("endNodeId").asText() + "|" + rel.get("properties").get("traceLinkType").asText();
                if (!seen.add(key)) {
                    duplicates++;
                }
            }
            return duplicates;
        }

        public long countDuplicateTraceableArdocoIds() {
            Map<String, Integer> counts = new HashMap<>();
            for (JsonNode node : nodes) {
                JsonNode ardocoId = node.get("properties").get("ardocoId");
                if (ardocoId == null) {
                    continue;
                }
                counts.merge(ardocoId.asText(), 1, Integer::sum);
            }
            return counts.values().stream().filter(count -> count > 1).count();
        }

        public Set<String> traceLinkKeys() {
            Map<String, JsonNode> byId = nodesById();
            Set<String> keys = new HashSet<>();
            for (JsonNode rel : relationships) {
                if (!"TRACES_TO".equals(rel.get("type").asText())) {
                    continue;
                }
                JsonNode source = byId.get(rel.get("startNodeId").asText());
                JsonNode target = byId.get(rel.get("endNodeId").asText());
                if (source == null || target == null) {
                    continue;
                }
                JsonNode sourceArdocoId = source.get("properties").get("ardocoId");
                JsonNode targetArdocoId = target.get("properties").get("ardocoId");
                if (sourceArdocoId == null || targetArdocoId == null) {
                    continue;
                }
                String type = rel.get("properties").get("traceLinkType").asText();
                keys.add(sourceArdocoId.asText() + "|" + targetArdocoId.asText() + "|" + type);
            }
            return keys;
        }
    }
}
