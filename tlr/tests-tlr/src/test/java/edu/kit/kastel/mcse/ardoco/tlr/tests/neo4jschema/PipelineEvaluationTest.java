//package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4j;
//
//import edu.kit.kastel.mcse.ardoco.tlr.tests.approach.ArDoCodeEvaluationProject;
//import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.ArdocodeEvaluation;
//import edu.kit.kastel.mcse.ardoco.tlr.tests.neo4j;
//
//
//import org.junit.jupiter.api.Test;
//
//
//@Testcontainers
//@SpringBootTest
//@Transactional
//public class PipelineEvaluationTest {
//
//    @Autowired
//    private CodePersistenceService persistenceService;
//
//    @Autowired
//    private CodeModelRepository codeModelRepository;
//
//    @Container
//    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.15.0")
//            .withRandomPassword();
//
//    @DynamicPropertySource
//    static void neo4jProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.neo4j.uri", edu.kit.kastel.mcse.ardoco.tlr.tests.neo4j::getBoltUrl);
//        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
//        registry.add("spring.neo4j.authentication.password", edu.kit.kastel.mcse.ardoco.tlr.tests.neo4j::getAdminPassword);
//    }
//
//    @Test
//    void evaluateArdocodeWithPersistence() {
//        ArdocodeEvaluation eval= new ArdocodeEvaluation(ArDoCodeEvaluationProject.BIGBLUEBUTTON);
//        eval.runTraceLinkEvaluation();
//    }
//
//
//}
