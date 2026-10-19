/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnitsAndPackages;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NerConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.text.PlainSimpleText;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.CodeTraceabilityStateImpl;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.TraceLinkCombiner;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.NerConnectionStatesImpl;
import io.github.ardoco.core.neo4jschema.Neo4jPersistenceHandler;

class ArtemisTraceLinkCombinerPersistenceTest extends AbstractPersistenceTest implements Claimant {

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Test
    @DisplayName("TraceLinkCombiner's ArTEMiS transitive links persist with distinct legs deduplicated by MERGE")
    void artemisCombinerTransitiveLinksArePersisted() {
        var simpleText = new PlainSimpleText("Line zero.\n\nLine one.\nLine two.", List.of("Line zero.", "Line one.", "Line two."));
        persistenceHandler.saveSimpleText(simpleText, "combiner-test");

        ArchitectureComponent archComponent = new ArchitectureComponent("AuthService", "id_comp_auth", new TreeSet<>(), new TreeSet<>(), new TreeSet<>(),
                "Service");
        CodeItemRepository codeRepo = new CodeItemRepository();
        CodePackage pkg = new CodePackage("id_pkg_auth", codeRepo, "edu.kit.auth");
        CodeCompilationUnit codeUnit = new CodeCompilationUnit("id_cu_auth", codeRepo, "AuthService", new TreeSet<>(), List.of("edu", "kit", "auth"), "java",
                ProgrammingLanguage.JAVA);
        pkg.addContent(codeUnit);
        codeUnit.setParent(pkg);
        codeRepo.init();

        var archModel = new ArchitectureModelWithComponentsAndInterfaces(List.<ArchitectureItem>of(archComponent));
        var codeModel = new CodeModelWithCompilationUnitsAndPackages("combiner_test_code_model", codeRepo, new TreeSet<>(List.<CodeItem>of(pkg)));

        DataRepository dataRepository = new DataRepository();
        try {
            PersistenceBridge.setHandler(persistenceHandler);
            PersistenceBridge.usePersistenceStatic = true;

            ModelStates modelStates = new ModelStates();
            modelStates.addModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS, archModel);
            modelStates.addModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);
            dataRepository.addData(ModelStates.ID, modelStates);

            CodeTraceabilityStateImpl codeTraceabilityState = new CodeTraceabilityStateImpl();
            codeTraceabilityState.addSamCodeTraceLinks(List.of(new ArchitectureCodeTraceLink(archComponent, codeUnit)));
            dataRepository.addData(CodeTraceabilityState.ID, codeTraceabilityState);

            NerConnectionStatesImpl nerConnectionStates = NerConnectionStatesImpl.build(new Metamodel[] { Metamodel.ARCHITECTURE_WITH_COMPONENTS });
            var nerConnectionState = nerConnectionStates.getNerConnectionState(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
            nerConnectionState.addToLinks(new NamedArchitectureEntityOccurrence("AuthService", 1), archComponent, this, 0.9);
            nerConnectionState.addToLinks(new NamedArchitectureEntityOccurrence("AuthService", 2), archComponent, this, 0.8);
            dataRepository.addData(NerConnectionStates.ID, nerConnectionStates);

            new TraceLinkCombiner(dataRepository).process();

            var transitiveLinks = codeTraceabilityState.getSadCodeTraceLinks();
            Assertions.assertEquals(2, transitiveLinks.size(), "Two sentences name the same architecture component, so two transitive links are expected");
            for (TraceLink<SentenceEntity, ?> link : transitiveLinks) {
                var sentence = link.getFirstEndpoint().getSentence();
                Assertions.assertEquals(String.valueOf(sentence.getSentenceNumber()), sentence.getText(),
                        "TraceLinkCombiner's ArTEMiS placeholder sentence text must still equal its own sentence number");
            }
        } finally {
            PersistenceBridge.usePersistenceStatic = false;
            PersistenceBridge.setHandler(null);
        }

        long sentenceArchitectureEdges = countTraceLinksOfType("SENTENCE_ARCHITECTURE");
        long architectureCodeEdges = countTraceLinksOfType("ARCHITECTURE_CODE");

        Assertions.assertEquals(2L, sentenceArchitectureEdges, "Two distinct sentences each get their own SENTENCE_ARCHITECTURE edge");
        Assertions.assertEquals(1L, architectureCodeEdges, "Both transitive links share the same architecture-code leg, so MERGE must deduplicate it to one edge");
    }

    private long countTraceLinksOfType(String traceLinkType) {
        return neo4jClient.query("MATCH ()-[r:TRACES_TO]->() WHERE r.traceLinkType = $type RETURN count(r) AS c")
                .bindAll(Map.of("type", traceLinkType))
                .fetch()
                .one()
                .map(row -> ((Number) row.get("c")).longValue())
                .orElse(0L);
    }
}
