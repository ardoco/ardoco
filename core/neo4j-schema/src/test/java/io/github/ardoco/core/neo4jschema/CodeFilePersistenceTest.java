/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeFile;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityToModelTraceLink;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;

/**
 * Stores the code files of a code model as {@code CodeFile:Traceable} nodes: properties, the {@code HAS_CODE_FILE} and {@code FILE_OF} relationships,
 * re-saving and deleting the model, the restore with the original ids, and a trace link whose target is a code file.
 */
@SpringBootTest(classes = io.github.ardoco.core.neo4jschema.Main.class)
class CodeFilePersistenceTest extends AbstractNeo4jTest {

    private static final Metamodel METAMODEL = Metamodel.CODE_WITH_COMPILATION_UNITS;
    private static final String MODEL_ID = "code_file_test_model";
    private static final String COMPILATION_UNIT_ID = "id_cf_cu";

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void clearDatabase() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    @DisplayName("Code files become CodeFile:Traceable nodes with HAS_CODE_FILE and FILE_OF; the code items stay the same")
    void codeFilesAreStoredAsNodes() {
        persistenceHandler.saveModel(METAMODEL, createModel(false));
        long codeItemsWithoutFiles = count("MATCH (n:CodeItem) RETURN count(n) AS c");
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();

        CodeModelWithCompilationUnits model = createModel(true);
        List<CodeFile> codeFiles = model.getCodeFiles();
        persistenceHandler.saveModel(METAMODEL, model);

        Assertions.assertEquals(2L, count("MATCH (f:CodeFile:Traceable) RETURN count(f) AS c"));
        Assertions.assertEquals(0L, count("MATCH (f:CodeFile:CodeItem) RETURN count(f) AS c"));
        Assertions.assertEquals(codeItemsWithoutFiles, count("MATCH (n:CodeItem) RETURN count(n) AS c"));

        Map<String, Object> first = properties(codeFiles.get(0).getId());
        Assertions.assertEquals(new TreeSet<>(List.of("ardocoId", "name", "pathElements", "extension", "compilationUnitId")), new TreeSet<>(first.keySet()));
        Assertions.assertEquals("AuthService", first.get("name"));
        Assertions.assertEquals(List.of("src", "auth"), first.get("pathElements"));
        Assertions.assertEquals("java", first.get("extension"));
        Assertions.assertEquals(COMPILATION_UNIT_ID, first.get("compilationUnitId"));

        Map<String, Object> second = properties(codeFiles.get(1).getId());
        Assertions.assertEquals(new TreeSet<>(List.of("ardocoId", "name", "pathElements", "extension")), new TreeSet<>(second.keySet()));
        Assertions.assertEquals("README", second.get("name"));
        Assertions.assertEquals(List.of("docs"), second.get("pathElements"));
        Assertions.assertEquals("md", second.get("extension"));

        Assertions.assertEquals(2L, count("MATCH (:CodeModel)-[r:HAS_CODE_FILE]->(:CodeFile) RETURN count(r) AS c"));
        Assertions.assertEquals(0L, index(codeFiles.get(0).getId()));
        Assertions.assertEquals(1L, index(codeFiles.get(1).getId()));
        Assertions.assertEquals(1L, count(
                "MATCH (f:CodeFile)-[r:FILE_OF]->(:CodeCompilationUnit {ardocoId: '" + COMPILATION_UNIT_ID + "'}) RETURN count(r) AS c"));
        Assertions.assertEquals(1L, count("MATCH ()-[r:FILE_OF]->() RETURN count(r) AS c"));
    }

    @Test
    @DisplayName("Saving the model again keeps one node per code file; deleting the model removes them")
    void resaveAndDelete() {
        CodeModelWithCompilationUnits model = createModel(true);
        persistenceHandler.saveModel(METAMODEL, model);
        persistenceHandler.saveModel(METAMODEL, model);

        Assertions.assertEquals(2L, count("MATCH (f:CodeFile) RETURN count(f) AS c"));
        Assertions.assertEquals(2L, count("MATCH ()-[r:HAS_CODE_FILE]->() RETURN count(r) AS c"));
        Assertions.assertEquals(1L, count("MATCH ()-[r:FILE_OF]->() RETURN count(r) AS c"));

        persistenceHandler.deleteModel(METAMODEL);

        Assertions.assertEquals(0L, count("MATCH (f:CodeFile) RETURN count(f) AS c"));
    }

    @Test
    @DisplayName("The loaded model has the same code files in the same order and with the original ids")
    void restoreKeepsCodeFilesAndIds() {
        CodeModelWithCompilationUnits model = createModel(true);
        persistenceHandler.saveModel(METAMODEL, model);

        CodeModel loaded = (CodeModel) persistenceHandler.loadModel(METAMODEL);

        Assertions.assertNotNull(loaded);
        Assertions.assertEquals(model.getCodeFiles(), loaded.getCodeFiles());
        List<String> originalIds = new ArrayList<>();
        for (CodeFile codeFile : model.getCodeFiles()) {
            originalIds.add(codeFile.getId());
        }
        List<String> loadedIds = new ArrayList<>();
        for (CodeFile codeFile : loaded.getCodeFiles()) {
            loadedIds.add(codeFile.getId());
        }
        Assertions.assertEquals(originalIds, loadedIds);
    }

    @Test
    @DisplayName("A trace link whose target is a code file ends at the CodeFile node")
    void traceLinkToCodeFile() {
        CodeModelWithCompilationUnits model = createModel(true);
        persistenceHandler.saveModel(METAMODEL, model);
        CodeFile codeFile = model.getCodeFiles().get(1);

        var occurrence = new NamedArchitectureEntityOccurrence("README.md", 3);
        var entity = new NamedArchitectureEntity("README.md", new TreeSet<>(), List.of(occurrence));
        persistenceHandler.saveNamedArchitectureEntity(entity, new ArtemisTarget(METAMODEL, NamedEntityType.DATAFILE), false);

        Assertions.assertTrue(persistenceHandler.saveTraceLinks(List.of(new NamedArchitectureEntityToModelTraceLink(occurrence, codeFile))));

        Assertions.assertEquals(1L, count("MATCH (:NamedArchitectureEntityOccurrence)-[r:TRACES_TO]->(:CodeFile {ardocoId: '" + codeFile
                .getId() + "'}) RETURN count(r) AS c"));
        Assertions.assertEquals(1L, count("MATCH ()-[r:TRACES_TO]->() RETURN count(r) AS c"));
    }

    @Test
    @DisplayName("The loaded code files follow the HAS_CODE_FILE index, not the order in which the nodes and relationships were created")
    void restoreFollowsTheStoredIndex() {
        CodeFile alpha = new CodeFile("Alpha", List.of("src"), "java", null);
        CodeFile beta = new CodeFile("Beta", List.of("src"), "java", null);
        CodeFile gamma = new CodeFile("Gamma", List.of("src"), "java", null);
        persistenceHandler.saveModel(METAMODEL, createModel(List.of(beta, gamma, alpha)));
        setIndex(alpha.getId(), 0);
        setIndex(beta.getId(), 1);
        setIndex(gamma.getId(), 2);

        CodeModel loaded = (CodeModel) persistenceHandler.loadModel(METAMODEL);

        Assertions.assertNotNull(loaded);
        List<String> loadedIds = new ArrayList<>();
        for (CodeFile codeFile : loaded.getCodeFiles()) {
            loadedIds.add(codeFile.getId());
        }
        Assertions.assertEquals(List.of(alpha.getId(), beta.getId(), gamma.getId()), loadedIds);
    }

    @Test
    @DisplayName("A trace link to a code file ends at the CodeFile node even if a node of another Traceable kind has the same ardocoId")
    void traceLinkToCodeFileWithSharedId() {
        CodeModelWithCompilationUnits model = createModel(true);
        persistenceHandler.saveModel(METAMODEL, model);
        CodeFile codeFile = model.getCodeFiles().get(1);
        neo4jClient.query("CREATE (:ArchitectureComponent:ArchitectureItem:Traceable {ardocoId: $id, name: 'Collision'})")
                .bind(codeFile.getId())
                .to("id")
                .run();

        var occurrence = new NamedArchitectureEntityOccurrence("README.md", 3);
        var entity = new NamedArchitectureEntity("README.md", new TreeSet<>(), List.of(occurrence));
        persistenceHandler.saveNamedArchitectureEntity(entity, new ArtemisTarget(METAMODEL, NamedEntityType.DATAFILE), false);

        Assertions.assertTrue(persistenceHandler.saveTraceLinks(List.of(new NamedArchitectureEntityToModelTraceLink(occurrence, codeFile))));

        Assertions.assertEquals(1L, count("MATCH (:NamedArchitectureEntityOccurrence)-[r:TRACES_TO]->(:CodeFile {ardocoId: '" + codeFile
                .getId() + "'}) RETURN count(r) AS c"));
        Assertions.assertEquals(0L, count("MATCH ()-[r:TRACES_TO]->(:ArchitectureComponent) RETURN count(r) AS c"));
        Assertions.assertEquals(1L, count("MATCH ()-[r:TRACES_TO]->() RETURN count(r) AS c"));
    }

    private static CodeModelWithCompilationUnits createModel(List<CodeFile> codeFiles) {
        CodeItemRepository repository = new CodeItemRepository();
        CodeCompilationUnit compilationUnit = new CodeCompilationUnit(COMPILATION_UNIT_ID, repository, "AuthService", new TreeSet<>(), List.of("src", "auth"),
                "java", ProgrammingLanguage.JAVA);
        ClassUnit classUnit = new ClassUnit("id_cf_cls", repository, "AuthService", new TreeSet<>());
        compilationUnit.addContent(classUnit);
        classUnit.setCompilationUnit(compilationUnit);
        return new CodeModelWithCompilationUnits(new CodeModel.CodeModelDto(MODEL_ID, repository, List.of(COMPILATION_UNIT_ID), codeFiles));
    }

    private void setIndex(String codeFileId, long index) {
        neo4jClient.query("MATCH (:CodeModel)-[r:HAS_CODE_FILE]->(:CodeFile {ardocoId: $id}) SET r.index = $index")
                .bind(codeFileId)
                .to("id")
                .bind(index)
                .to("index")
                .run();
    }

    private static CodeModelWithCompilationUnits createModel(boolean withCodeFiles) {
        CodeItemRepository repository = new CodeItemRepository();
        CodeCompilationUnit compilationUnit = new CodeCompilationUnit(COMPILATION_UNIT_ID, repository, "AuthService", new TreeSet<>(), List.of("src", "auth"),
                "java", ProgrammingLanguage.JAVA);
        ClassUnit classUnit = new ClassUnit("id_cf_cls", repository, "AuthService", new TreeSet<>());
        compilationUnit.addContent(classUnit);
        classUnit.setCompilationUnit(compilationUnit);
        List<CodeFile> codeFiles = withCodeFiles ?
                List.of(new CodeFile("AuthService", List.of("src", "auth"), "java", COMPILATION_UNIT_ID), new CodeFile("README", List.of("docs"), "md", null)) :
                List.of();
        return new CodeModelWithCompilationUnits(new CodeModel.CodeModelDto(MODEL_ID, repository, List.of(COMPILATION_UNIT_ID), codeFiles));
    }

    private long count(String query) {
        return neo4jClient.query(query).fetchAs(Long.class).one().orElseThrow();
    }

    private long index(String codeFileId) {
        return neo4jClient.query("MATCH (:CodeModel)-[r:HAS_CODE_FILE]->(:CodeFile {ardocoId: $id}) RETURN r.index AS i")
                .bind(codeFileId)
                .to("id")
                .fetchAs(Long.class)
                .one()
                .orElseThrow();
    }

    private Map<String, Object> properties(String codeFileId) {
        return neo4jClient.query("MATCH (f:CodeFile {ardocoId: $id}) RETURN properties(f) AS p")
                .bind(codeFileId)
                .to("id")
                .fetchAs(Map.class)
                .mappedBy((typeSystem, record) -> record.get("p").asMap())
                .one()
                .orElseThrow();
    }
}
