/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnitsAndPackages;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;

@SpringBootTest(classes = Main.class)
class ModelIdCollisionPersistenceTest extends AbstractNeo4jTest {

    private static final String SHARED_ID = "acm000001337jsd";

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void setUp() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @AfterEach
    void tearDown() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    @DisplayName("An ArchitectureMethod and a ClassUnit with the same id stay apart: model edges and the trace link reach only the labelled node")
    void sharedIdDoesNotFanOut() {
        ArchitectureMethod method = new ArchitectureMethod(SHARED_ID, "doWork");
        ArchitectureInterface architectureInterface = new ArchitectureInterface("IWork", "collision_iface", new TreeSet<>(List.of(method)));
        ArchitectureComponent component = new ArchitectureComponent("Worker", "collision_comp", new TreeSet<>(), new TreeSet<>(List.of(
                architectureInterface)), new TreeSet<>(), "Component");
        var architectureModel = new ArchitectureModelWithComponentsAndInterfaces(List.<ArchitectureItem>of(component, architectureInterface));

        CodeItemRepository repository = new CodeItemRepository();
        CodePackage codePackage = new CodePackage("collision_pkg", repository, "edu.kit.collision");
        CodeCompilationUnit compilationUnit = new CodeCompilationUnit("collision_cu", repository, "Worker", new TreeSet<>(), List.of("edu", "kit",
                "collision"), "java", ProgrammingLanguage.JAVA);
        ClassUnit classUnit = new ClassUnit(SHARED_ID, repository, "Worker", new TreeSet<>());
        codePackage.addContent(compilationUnit);
        compilationUnit.setParent(codePackage);
        compilationUnit.addContent(classUnit);
        classUnit.setCompilationUnit(compilationUnit);
        repository.init();
        SortedSet<CodeItem> roots = new TreeSet<>(List.of(codePackage));
        var codeModel = new CodeModelWithCompilationUnitsAndPackages("collision_code_model", repository, roots);

        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, architectureModel);
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);
        persistenceHandler.saveTraceLinks(List.of(new ArchitectureCodeTraceLink(method, classUnit)));

        Assertions.assertEquals(1L, count("MATCH (:ArchitectureMethod {ardocoId: $id}) RETURN count(*) AS c"), "one ArchitectureMethod node");
        Assertions.assertEquals(1L, count("MATCH (:ClassUnit {ardocoId: $id}) RETURN count(*) AS c"), "one ClassUnit node");
        Assertions.assertEquals(1L, count("MATCH ()-[r:TRACES_TO]->() RETURN count(r) AS c"), "exactly one TRACES_TO edge");
        Assertions.assertEquals(1L, count("MATCH (:ArchitectureMethod {ardocoId: $id})-[r:TRACES_TO]->(:ClassUnit {ardocoId: $id}) RETURN count(r) AS c"),
                "the TRACES_TO edge goes from the method to the class");
        Assertions.assertEquals(0L, count("MATCH ()-[:HAS_METHOD]->(n:CodeItem) RETURN count(n) AS c"), "HAS_METHOD must not reach a code item");
        Assertions.assertEquals(0L, count("MATCH (:CodeModel)-[]->(n:ArchitectureItem) RETURN count(n) AS c"), "code model edges must not reach an "
                + "architecture item");
        Assertions.assertEquals(0L, count("MATCH (:CodeItem)-[]->(n:ArchitectureItem) RETURN count(n) AS c"), "code edges must not reach an architecture item");
    }

    private long count(String query) {
        return neo4jClient.query(query).bind(SHARED_ID).to("id").fetchAs(Long.class).mappedBy((typeSystem, record) -> record.get("c").asLong()).one().orElse(
                0L);
    }
}
