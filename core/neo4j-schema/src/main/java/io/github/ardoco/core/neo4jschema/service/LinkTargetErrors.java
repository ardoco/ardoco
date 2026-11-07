/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;

final class LinkTargetErrors {

    private static final int MAX_EXAMPLES = 5;

    private final String kind;
    private final SortedMap<String, long[]> countsByRelation = new TreeMap<>();
    private final SortedMap<String, List<String>> examplesByRelation = new TreeMap<>();

    LinkTargetErrors(String kind) {
        this.kind = kind;
    }

    void missing(String relation, long count, String example) {
        add(relation, 0, count, example);
    }

    void ambiguous(String relation, String example) {
        add(relation, 1, 1, example);
    }

    void recordEdge(String relation, Object sourceId, Object targetId, long sourceMatches, long targetMatches) {
        String example = sourceId + " -> " + targetId + " (source " + describe(sourceMatches) + ", target " + describe(targetMatches) + ")";
        if (sourceMatches == 0 || targetMatches == 0) {
            missing(relation, 1, example);
        } else {
            ambiguous(relation, example);
        }
    }

    void recordEdges(String relation, List<Map<String, Object>> unresolvedRows) {
        for (Map<String, Object> row : unresolvedRows) {
            recordEdge(relation, row.get("sourceId"), row.get("targetId"), ((Number) row.get("sourceMatches")).longValue(), ((Number) row.get("targetMatches"))
                    .longValue());
        }
    }

    boolean hasErrors() {
        return !countsByRelation.isEmpty();
    }

    void throwIfAny(Logger logger) {
        if (!hasErrors()) {
            return;
        }
        String message = message();
        logger.error(message);
        throw new MissingLinkTargetException(message);
    }

    private void add(String relation, int slot, long count, String example) {
        countsByRelation.computeIfAbsent(relation, ignored -> new long[2])[slot] += count;
        List<String> examples = examplesByRelation.computeIfAbsent(relation, ignored -> new ArrayList<>());
        if (examples.size() < MAX_EXAMPLES) {
            examples.add(example);
        }
    }

    private String message() {
        long total = 0;
        List<String> perRelation = new ArrayList<>();
        List<String> examples = new ArrayList<>();
        for (var entry : countsByRelation.entrySet()) {
            long missingCount = entry.getValue()[0];
            long ambiguousCount = entry.getValue()[1];
            total += missingCount + ambiguousCount;
            perRelation.add(entry.getKey() + ": " + missingCount + " missing, " + ambiguousCount + " ambiguous");
            for (String example : examplesByRelation.get(entry.getKey())) {
                examples.add(entry.getKey() + ": " + example);
            }
        }
        return kind + ": " + total + " link target(s) not resolved - " + String.join(", ", perRelation) + "; examples: " + examples;
    }

    private static String describe(long matches) {
        if (matches == 0) {
            return "missing";
        }
        return matches == 1 ? "found" : matches + " matches";
    }
}
