/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationProject;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Arcotl;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Ardocode;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Transarc;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.PersistenceTestSupport;

@EnabledIfSystemProperty(named = "ardoco.tests.memoryVsNeo4j", matches = "true")
class MemoryVsNeo4jConsistencyTest extends AbstractPersistenceTest {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MemoryVsNeo4jConsistencyTest.class);
    private static final String CASES_FILTER_PROPERTY = "ardoco.tests.memoryVsNeo4j.cases";
    private static final String CASE_ROOT = "target/memory-vs-neo4j/";
    private static final String INPUTS_ROOT = "target/memory-vs-neo4j/inputs/";
    private static final String[] TRACE_LINK_TYPES = { "SENTENCE_ARCHITECTURE", "SENTENCE_CODE", "ARCHITECTURE_CODE", "RECOMMENDATION_ARCHITECTURE",
            "RECOMMENDATION_CODE" };

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void verify(Case testCase) throws Exception {
        clearNeo4jGraph();
        File caseDir = new File(CASE_ROOT + testCase.name());
        File memoryDir = new File(caseDir, "memory");
        File neo4jDir = new File(caseDir, "neo4j");
        deleteRecursively(caseDir.toPath());
        Files.createDirectories(memoryDir.toPath());
        Files.createDirectories(neo4jDir.toPath());

        ArdocoRunner memoryRunner = testCase.runnerFactory().apply(getConfigsWithMemoryStorage(), memoryDir);
        testRunnerAssertions(memoryRunner);
        ArdocoResult memoryResult = memoryRunner.run();
        Assertions.assertNotNull(memoryResult, testCase.name() + ": memory run must produce a result");
        Map<String, String> memoryFiles = readOutputFiles(memoryDir);

        Logger persistenceLogger = (Logger) LoggerFactory.getLogger("io.github.ardoco.core.neo4jschema");
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        persistenceLogger.addAppender(appender);
        ArdocoResult neo4jResult;
        DataRepository dataRepository;
        try {
            ArdocoRunner neo4jRunner = testCase.runnerFactory().apply(getConfigsWithNeo4jStorage(), neo4jDir);
            testRunnerAssertions(neo4jRunner);
            dataRepository = neo4jRunner.getArdoco().getDataRepository();
            neo4jResult = neo4jRunner.run();
        } finally {
            persistenceLogger.detachAppender(appender);
        }
        Assertions.assertNotNull(neo4jResult, testCase.name() + ": neo4j run must produce a result");
        Map<String, String> neo4jFiles = readOutputFiles(neo4jDir);

        List<String> warningsAndErrors = appender.list.stream()
                .filter(event -> event.getLevel().isGreaterOrEqual(Level.WARN))
                .map(event -> event.getLevel() + " " + event.getFormattedMessage())
                .toList();
        Assertions.assertTrue(warningsAndErrors.isEmpty(), testCase.name() + ": unexpected persistence WARN/ERROR events: " + warningsAndErrors);

        Assertions.assertEquals(memoryFiles.keySet(), neo4jFiles.keySet(), testCase.name() + ": output file sets must match between runs");
        for (var entry : memoryFiles.entrySet()) {
            Assertions.assertEquals(normalize(entry.getValue()), normalize(neo4jFiles.get(entry.getKey())), testCase.name() + ": " + entry
                    .getKey() + " must match between the memory and neo4j runs");
        }

        File exportedJsonFile = new File(neo4jDir, "knowledgeGraph_" + testCase.project() + ".json");
        Assertions.assertTrue(exportedJsonFile.isFile(), testCase.name() + ": exported knowledge graph JSON must exist");
        PersistenceTestSupport.ExportedGraph graph = PersistenceTestSupport.ExportedGraph.read(exportedJsonFile);

        Assertions.assertEquals(1L, graph.countNodesWithLabel("RunMetadata"), testCase.name() + ": exactly one RunMetadata node is expected");
        Assertions.assertEquals(0L, graph.countDanglingRelationships(), testCase.name() + ": no relationship may reference a missing node");
        Assertions.assertEquals(0L, graph.countDuplicateTraceLinks(), testCase.name() + ": no duplicate (source, target, type) TRACES_TO edge is expected");
        Assertions.assertEquals(0L, graph.countDuplicateTraceableArdocoIds(), testCase.name() + ": no duplicate Traceable ardocoId is expected");
        for (String forbiddenLabel : testCase.forbiddenLabels()) {
            Assertions.assertEquals(0L, graph.countNodesWithLabel(forbiddenLabel), testCase
                    .name() + ": label " + forbiddenLabel + " must not appear for this pipeline");
        }
        for (String requiredLabel : testCase.requiredLabels()) {
            Assertions.assertTrue(graph.countNodesWithLabel(requiredLabel) > 0, testCase
                    .name() + ": label " + requiredLabel + " must appear for this pipeline");
        }

        Set<String> expectedKeys = collectExpectedTraceLinks(dataRepository);
        Set<String> actualKeys = graph.traceLinkKeys();
        StringBuilder sizesLog = new StringBuilder(testCase.name());
        sizesLog.append(": persistence WARN/ERROR events=").append(warningsAndErrors.size());
        sizesLog.append(" expected total=").append(expectedKeys.size()).append(" actual total=").append(actualKeys.size());
        for (String type : TRACE_LINK_TYPES) {
            sizesLog.append(" ")
                    .append(type)
                    .append(" expected=")
                    .append(countByType(expectedKeys, type))
                    .append(" actual=")
                    .append(countByType(actualKeys, type));
        }
        logger.info(sizesLog.toString());
        Set<String> missingFromGraph = new HashSet<>(expectedKeys);
        missingFromGraph.removeAll(actualKeys);
        Set<String> unexpectedInGraph = new HashSet<>(actualKeys);
        unexpectedInGraph.removeAll(expectedKeys);
        Assertions.assertEquals(Set.of(), missingFromGraph, testCase.name() + ": in-memory trace links missing from the exported graph");
        Assertions.assertEquals(Set.of(), unexpectedInGraph, testCase.name() + ": exported graph trace links with no matching in-memory link");

        for (var fileCheck : testCase.fileRowChecksByType().entrySet()) {
            String fileName = fileCheck.getKey();
            String type = fileCheck.getValue();
            String content = neo4jFiles.get(fileName);
            Assertions.assertNotNull(content, testCase.name() + ": expected " + fileName + " to exist for this file row check");
            long rows = dataRows(content);
            long edges = countByType(actualKeys, type);
            Assertions.assertEquals(edges, rows, testCase.name() + ": " + fileName + " row count must match the " + type + " edge count");
        }

        if (testCase.fixedTraceLinkCounts() != null) {
            for (var fixedEntry : testCase.fixedTraceLinkCounts().entrySet()) {
                Assertions.assertEquals((long) fixedEntry.getValue(), countByType(actualKeys, fixedEntry.getKey()), testCase
                        .name() + ": known-reference " + fixedEntry.getKey() + " count must hold");
            }
        }

        if (testCase.fixedCensus() != null) {
            Assertions.assertEquals(testCase.fixedCensus().nodeCount(), graph.nodeCount(), testCase.name() + ": known-reference node count must hold");
            Assertions.assertEquals(testCase.fixedCensus().relationshipCount(), graph.relationshipCount(), testCase
                    .name() + ": known-reference relationship count must hold");
            Assertions.assertEquals(testCase.fixedCensus().codeItemCount(), graph.countNodesWithLabel("CodeItem"), testCase
                    .name() + ": known-reference CodeItem count must hold");
        }

        Long remainingNodes = neo4jClient.query("MATCH (n) RETURN count(n) AS c")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get("c").asLong())
                .one()
                .orElse(-1L);
        Assertions.assertEquals(0L, remainingNodes, testCase.name() + ": the database must be empty after the neo4j run");
    }

    static Stream<Case> cases() {
        String filterProperty = System.getProperty(CASES_FILTER_PROPERTY, "");
        Set<String> filter = filterProperty.isBlank() ?
                Set.of() :
                Set.of(filterProperty.split(",")).stream().map(String::trim).collect(java.util.stream.Collectors.toSet());

        List<Case> all = new ArrayList<>();
        all.add(teaStoreCase("transarc", MemoryVsNeo4jConsistencyTest::transarc, Set.of(), transarcRequiredLabels(), transarcFileChecks(), Map.of(
                "SENTENCE_ARCHITECTURE", 20, "SENTENCE_CODE", 100, "ARCHITECTURE_CODE", 155, "RECOMMENDATION_CODE", 25, "RECOMMENDATION_ARCHITECTURE", 7),
                new FixedCensus(5714, 19277, 3819)));
        all.add(teaStoreCase("swattr", MemoryVsNeo4jConsistencyTest::swattr, codeLabels(), swattrRequiredLabels(), swattrFileChecks(), null, null));
        all.add(teaStoreCase("ardocode", MemoryVsNeo4jConsistencyTest::ardocode, architectureLabels(), ardocodeRequiredLabels(), Map.of(), null, null));
        all.add(teaStoreCase("arcotl", MemoryVsNeo4jConsistencyTest::arcotl, textLabels(), arcotlRequiredLabels(), arcotlFileChecks(), null, null));

        all.add(evaluationProjectCase(EvaluationProject.JABREF, "transarc", MemoryVsNeo4jConsistencyTest::transarc, Set.of(), transarcRequiredLabels(),
                transarcFileChecks(), Map.of("SENTENCE_ARCHITECTURE", 20, "SENTENCE_CODE", 31, "ARCHITECTURE_CODE", 1957, "RECOMMENDATION_CODE", 18,
                        "RECOMMENDATION_ARCHITECTURE", 5), new FixedCensus(28648, 71377, 27966)));
        all.add(evaluationProjectCase(EvaluationProject.JABREF, "swattr", MemoryVsNeo4jConsistencyTest::swattr, codeLabels(), swattrRequiredLabels(),
                swattrFileChecks(), null, null));
        all.add(evaluationProjectCase(EvaluationProject.JABREF, "ardocode", MemoryVsNeo4jConsistencyTest::ardocode, architectureLabels(),
                ardocodeRequiredLabels(), Map.of(), null, null));
        all.add(evaluationProjectCase(EvaluationProject.JABREF, "arcotl", MemoryVsNeo4jConsistencyTest::arcotl, textLabels(), arcotlRequiredLabels(),
                arcotlFileChecks(), null, null));

        all.add(evaluationProjectCase(EvaluationProject.TEAMMATES, "transarc", MemoryVsNeo4jConsistencyTest::transarc, Set.of(), transarcRequiredLabels(),
                transarcFileChecks(), null, null));
        all.add(evaluationProjectCase(EvaluationProject.MEDIASTORE, "transarc", MemoryVsNeo4jConsistencyTest::transarc, Set.of(), transarcRequiredLabels(),
                transarcFileChecks(), null, null));
        all.add(evaluationProjectCase(EvaluationProject.BIGBLUEBUTTON, "transarc", MemoryVsNeo4jConsistencyTest::transarc, Set.of(), transarcRequiredLabels(),
                transarcFileChecks(), null, null));

        if (filter.isEmpty()) {
            return all.stream();
        }
        return all.stream().filter(c -> filter.contains(c.name()));
    }

    private static Case teaStoreCase(String pipelineName, PipelineFactory factory, Set<String> forbiddenLabels, Set<String> requiredLabels,
            Map<String, String> fileRowChecksByType, Map<String, Integer> fixedTraceLinkCounts, FixedCensus fixedCensus) {
        File text = copyClasspathResource("/teastore.txt", "teastore.txt");
        File architecture = copyClasspathResource("/teastore.repository", "teastore.repository");
        File code = copyClasspathResource("/code/teastore/codeModel.acm", "teastore-codeModel.acm");
        return new Case("teastore", pipelineName, (configs, outputDir) -> factory.build("teastore", text, architecture, code, configs, outputDir),
                forbiddenLabels, requiredLabels, fileRowChecksByType, fixedTraceLinkCounts, fixedCensus);
    }

    private static File copyClasspathResource(String classpathName, String targetFileName) {
        try {
            File directory = new File(INPUTS_ROOT);
            Files.createDirectories(directory.toPath());
            File file = new File(directory, targetFileName);
            try (InputStream in = MemoryVsNeo4jConsistencyTest.class.getResourceAsStream(classpathName)) {
                Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("Could not copy classpath resource " + classpathName, e);
        }
    }

    private static Case evaluationProjectCase(EvaluationProject project, String pipelineName, PipelineFactory factory, Set<String> forbiddenLabels,
            Set<String> requiredLabels, Map<String, String> fileRowChecksByType, Map<String, Integer> fixedTraceLinkCounts, FixedCensus fixedCensus) {
        String projectName = project.name().toLowerCase();
        File text = project.getTextFile();
        File architecture = project.getArchitectureModel(ModelFormat.PCM);
        File code = project.getCodeModelFromResources();
        return new Case(projectName, pipelineName, (configs, outputDir) -> factory.build(projectName, text, architecture, code, configs, outputDir),
                forbiddenLabels, requiredLabels, fileRowChecksByType, fixedTraceLinkCounts, fixedCensus);
    }

    private static ArdocoRunner transarc(String projectName, File text, File architecture, File code, ImmutableSortedMap<String, String> configs,
            File outputDir) {
        var runner = new Transarc(projectName);
        runner.setUp(text, new ArchitectureConfiguration(architecture, ModelFormat.PCM), new CodeConfiguration(code,
                CodeConfiguration.CodeConfigurationType.ACM_FILE), configs, outputDir);
        return runner;
    }

    private static ArdocoRunner swattr(String projectName, File text, File architecture, File code, ImmutableSortedMap<String, String> configs,
            File outputDir) {
        var runner = new Swattr(projectName);
        runner.setUp(text, new ArchitectureConfiguration(architecture, ModelFormat.PCM), configs, outputDir);
        return runner;
    }

    private static ArdocoRunner ardocode(String projectName, File text, File architecture, File code, ImmutableSortedMap<String, String> configs,
            File outputDir) {
        var runner = new Ardocode(projectName);
        runner.setUp(text, new CodeConfiguration(code, CodeConfiguration.CodeConfigurationType.ACM_FILE), configs, outputDir);
        return runner;
    }

    private static ArdocoRunner arcotl(String projectName, File text, File architecture, File code, ImmutableSortedMap<String, String> configs,
            File outputDir) {
        var runner = new Arcotl(projectName);
        runner.setUp(new ArchitectureConfiguration(architecture, ModelFormat.PCM), new CodeConfiguration(code,
                CodeConfiguration.CodeConfigurationType.ACM_FILE), configs, outputDir);
        return runner;
    }

    private static Set<String> codeLabels() {
        return Set.of("CodeItem", "CodePackage", "CodeModule", "CodeAssembly", "ControlElement", "CodeCompilationUnit", "ClassUnit", "InterfaceUnit");
    }

    private static Set<String> architectureLabels() {
        return Set.of("ArchitectureComponent", "ArchitectureInterface", "ArchitectureMethod", "ArchitectureItem");
    }

    private static Set<String> textLabels() {
        return Set.of("Text", "Sentence", "Word", "Phrase", "NounMapping", "RecommendedInstance");
    }

    private static Set<String> transarcRequiredLabels() {
        return Set.of("Text", "Sentence", "Word", "Phrase", "NounMapping", "RecommendedInstance", "ArchitectureItem", "CodeItem", "RunMetadata", "Project");
    }

    private static Set<String> swattrRequiredLabels() {
        return Set.of("Text", "Sentence", "Word", "Phrase", "NounMapping", "RecommendedInstance", "ArchitectureItem", "RunMetadata", "Project");
    }

    private static Set<String> ardocodeRequiredLabels() {
        return Set.of("Text", "Sentence", "Word", "Phrase", "NounMapping", "RecommendedInstance", "CodeItem", "RunMetadata", "Project");
    }

    private static Set<String> arcotlRequiredLabels() {
        return Set.of("ArchitectureItem", "CodeItem", "RunMetadata");
    }

    private static Map<String, String> transarcFileChecks() {
        return Map.of("sadSamTlr_%s.csv", "SENTENCE_ARCHITECTURE", "samCodeTlr_%s.csv", "ARCHITECTURE_CODE");
    }

    private static Map<String, String> swattrFileChecks() {
        return Map.of("sadSamTlr_%s.csv", "SENTENCE_ARCHITECTURE");
    }

    private static Map<String, String> arcotlFileChecks() {
        return Map.of("samCodeTlr_%s.csv", "ARCHITECTURE_CODE");
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }

    private static Map<String, String> readOutputFiles(File directory) throws IOException {
        Map<String, String> files = new LinkedHashMap<>();
        File[] children = directory.listFiles();
        if (children == null) {
            return files;
        }
        for (File child : children) {
            if (child.isFile() && !child.getName().endsWith(".json") && !child.getName().equals("wallMs.txt")) {
                files.put(child.getName(), Files.readString(child.toPath(), StandardCharsets.UTF_8));
            }
        }
        return files;
    }

    private static String normalize(String content) {
        if (content == null) {
            return null;
        }
        return content.lines().filter(line -> !line.matches("^\\d{4}-\\d{2}-\\d{2}.*")).sorted().collect(java.util.stream.Collectors.joining("\n"));
    }

    private static long dataRows(String content) {
        return content.lines().skip(1).filter(line -> !line.isBlank()).count();
    }

    private static Set<String> collectExpectedTraceLinks(DataRepository dataRepository) {
        Set<String> keys = new HashSet<>();
        var connectionStatesOptional = dataRepository.getData(ConnectionStates.ID, ConnectionStates.class);
        if (connectionStatesOptional.isPresent()) {
            ConnectionStates connectionStates = connectionStatesOptional.get();
            for (Metamodel metamodel : PersistenceTestSupport.activeMetamodels(dataRepository)) {
                var connectionState = connectionStates.getConnectionState(metamodel);
                if (connectionState == null) {
                    continue;
                }
                String sentenceType = metamodel.isArchitectureModel() ? "SENTENCE_ARCHITECTURE" : "SENTENCE_CODE";
                for (TraceLink<SentenceEntity, ModelEntity> link : connectionState.getTraceLinks()) {
                    keys.add(key(sentenceKey(link.getFirstEndpoint()), modelKey(link.getSecondEndpoint()), sentenceType));
                }
                String recommendationType = metamodel.isArchitectureModel() ? "RECOMMENDATION_ARCHITECTURE" : "RECOMMENDATION_CODE";
                for (TraceLink<RecommendedInstance, ModelEntity> link : connectionState.getInstanceLinks()) {
                    keys.add(key(link.getFirstEndpoint().getId(), modelKey(link.getSecondEndpoint()), recommendationType));
                }
            }
        }
        var codeTraceabilityOptional = dataRepository.getData(CodeTraceabilityState.ID, CodeTraceabilityState.class);
        if (codeTraceabilityOptional.isPresent()) {
            CodeTraceabilityState codeTraceabilityState = codeTraceabilityOptional.get();
            for (TraceLink<? extends ArchitectureEntity, ? extends ModelEntity> link : codeTraceabilityState.getSamCodeTraceLinks()) {
                keys.add(key(modelKey(link.getFirstEndpoint()), modelKey(link.getSecondEndpoint()), "ARCHITECTURE_CODE"));
            }
            for (TraceLink<SentenceEntity, ? extends ModelEntity> link : codeTraceabilityState.getSadCodeTraceLinks()) {
                if (link instanceof TransitiveTraceLink<?, ?> transitive) {
                    @SuppressWarnings("unchecked") TraceLink<SentenceEntity, ? extends ModelEntity> firstLeg = (TraceLink<SentenceEntity, ? extends ModelEntity>) transitive
                            .getFirstTraceLink();
                    @SuppressWarnings("unchecked") TraceLink<? extends ModelEntity, ? extends ModelEntity> secondLeg = (TraceLink<? extends ModelEntity, ? extends ModelEntity>) transitive
                            .getSecondTraceLink();
                    keys.add(key(sentenceKey(firstLeg.getFirstEndpoint()), modelKey(firstLeg.getSecondEndpoint()), "SENTENCE_ARCHITECTURE"));
                    keys.add(key(modelKey(secondLeg.getFirstEndpoint()), modelKey(secondLeg.getSecondEndpoint()), "ARCHITECTURE_CODE"));
                } else {
                    keys.add(key(sentenceKey(link.getFirstEndpoint()), modelKey(link.getSecondEndpoint()), "SENTENCE_CODE"));
                }
            }
        }
        return keys;
    }

    private static String key(String sourceId, String targetId, String type) {
        return sourceId + "|" + targetId + "|" + type;
    }

    private static long countByType(Set<String> keys, String type) {
        return keys.stream().filter(k -> k.endsWith("|" + type)).count();
    }

    private static String sentenceKey(SentenceEntity sentenceEntity) {
        var sentence = sentenceEntity.getSentence();
        return String.valueOf(sentence.getSentenceNumber()) + sentence.getText().hashCode();
    }

    private static String modelKey(ModelEntity modelEntity) {
        return modelEntity.getId();
    }

    @FunctionalInterface
    private interface PipelineFactory {
        ArdocoRunner build(String projectName, File text, File architecture, File code, ImmutableSortedMap<String, String> configs, File outputDir);
    }

    private record FixedCensus(long nodeCount, long relationshipCount, long codeItemCount) {
    }

    private record Case(String project, String pipelineName, BiFunction<ImmutableSortedMap<String, String>, File, ArdocoRunner> runnerFactory,
                        Set<String> forbiddenLabels, Set<String> requiredLabels, Map<String, String> fileRowChecksRaw,
                        Map<String, Integer> fixedTraceLinkCounts, FixedCensus fixedCensus) {

        String name() {
            return project + "-" + pipelineName;
        }

        Map<String, String> fileRowChecksByType() {
            Map<String, String> resolved = new LinkedHashMap<>();
            for (var entry : fileRowChecksRaw.entrySet()) {
                resolved.put(String.format(entry.getKey(), project), entry.getValue());
            }
            return resolved;
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
