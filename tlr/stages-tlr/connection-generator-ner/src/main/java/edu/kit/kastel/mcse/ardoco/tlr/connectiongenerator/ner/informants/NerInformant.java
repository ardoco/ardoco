/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.model.SoftwareArchitectureDocumentation;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.NamedEntityRecognizer;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.TwoPartPrompt;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.NerConnectionStatesImpl;

@Deterministic("Currently not fully deterministic due to NAER.")
public class NerInformant extends Informant {

    public NerInformant(DataRepository dataRepository) {
        super(NerInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        var nerConnectionStates = DataRepositoryHelper.getNerConnectionStates(dataRepository).asPipelineStepData(NerConnectionStatesImpl.class).orElseThrow();

        var text = DataRepositoryHelper.getSimpleText(dataRepository);
        SoftwareArchitectureDocumentation sad = new SoftwareArchitectureDocumentation(text.getText());

        var llmSettings = nerConnectionStates.getLlmSettings();
        var chatModel = llmSettings.createChatModel();
        var prompt = getPrompt();
        var namedEntityRecognizer = new NamedEntityRecognizer.Builder().chatModel(chatModel).prompt(prompt).build();

        var modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        for (var metamodel : modelStatesData.getMetamodels()) {
            var possibleEntities = getPossibleEntities(metamodel);
            var namedArchitectureEntities = recognizeNamedArchitectureEntities(namedEntityRecognizer, sad, possibleEntities);

            var nerConnectionState = nerConnectionStates.getNerConnectionState(metamodel);
            nerConnectionState.addNamedEntities(namedArchitectureEntities);
        }
    }

    private static Set<NamedArchitectureEntity> recognizeNamedArchitectureEntities(NamedEntityRecognizer namedEntityRecognizer,
            SoftwareArchitectureDocumentation sad, Map<NamedEntityType, Set<String>> possibleEntities) {
        // TODO This is not deterministic .. as the hashset has a random order. This should be fixed in NAER.
        Set<NamedEntity> namedEntities = new LinkedHashSet<>(namedEntityRecognizer.recognize(sad, possibleEntities));
        return transformNamedEntitiesToNamedArchitectureEntities(namedEntities);
    }

    @NotNull
    private static Set<NamedArchitectureEntity> transformNamedEntitiesToNamedArchitectureEntities(Set<NamedEntity> namedEntities) {
        SortedSet<NamedArchitectureEntity> namedArchitectureEntities = new TreeSet<>();
        for (var namedEntity : namedEntities) {
            var name = namedEntity.getName();
            var alternativeNames = new TreeSet<>(namedEntity.getAlternativeNames());
            var occurrences = namedEntity.getOccurrenceLines();

            MutableList<NamedArchitectureEntityOccurrence> namedArchitectureEntityOccurrences = getNamedArchitectureEntityOccurrences(name, occurrences);

            var namedArchitectureEntity = new NamedArchitectureEntity(name, alternativeNames, namedArchitectureEntityOccurrences);
            namedArchitectureEntities.add(namedArchitectureEntity);
        }
        return namedArchitectureEntities;
    }

    private static MutableList<NamedArchitectureEntityOccurrence> getNamedArchitectureEntityOccurrences(String name, Set<Integer> occurrences) {
        MutableList<NamedArchitectureEntityOccurrence> namedArchitectureEntityOccurrences = Lists.mutable.of();
        for (var occurrence : occurrences) {
            var namedArchitectureEntityOccurrence = new NamedArchitectureEntityOccurrence(name, occurrence);
            namedArchitectureEntityOccurrences.add(namedArchitectureEntityOccurrence);
        }
        return namedArchitectureEntityOccurrences;
    }

    private Map<NamedEntityType, Set<String>> getPossibleEntities(Metamodel metamodel) {
        Map<NamedEntityType, Set<String>> possibleEntities = new EnumMap<>(NamedEntityType.class);
        for (var type : NamedEntityType.values()) {
            possibleEntities.put(type, new TreeSet<>());
        }

        var modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        var model = modelStatesData.getModel(metamodel);
        for (var endpoint : model.getEndpoints()) {
            String endpointName = endpoint.getName();
            NamedEntityType namedEntityType = getNamedEntityType(endpoint);
            possibleEntities.get(namedEntityType).add(endpointName);
        }

        return possibleEntities;
    }

    @NotNull
    private static NamedEntityType getNamedEntityType(ModelEntity endpoint) {
        NamedEntityType namedEntityType = NamedEntityType.COMPONENT;
        var type = endpoint.getType();
        if (type.isPresent()) {
            String typeName = type.get().toLowerCase();
            if (typeName.contains("interface")) {
                namedEntityType = NamedEntityType.INTERFACE;
            }
        }
        return namedEntityType;
    }

    private TwoPartPrompt getPrompt() {
        String taskPrompt = """
                In the following text, identify all architecturally relevant components that are explicitly named.
                
                For each component, provide:
                - The primary name (as it appears in the text)
                - All alternative names or abbreviations found in the text (case-insensitive match)
                - All complete lines where the component is mentioned.
                
                Rules:
                - Only include actual architecturally relevant components (e.g., modules, services, subsystems, layers)
                - Do not include: interfaces, external libraries, frameworks, or technologies unless they are implemented in this architecture as components
                - Include all indirect references to components as well.
                  For example, if a sentence says “Component X handles requests.”, and the following sentence says “It interacts with Component Y.”, then both sentences must be included for Component X, because “It” indirectly refers to Component X.
                - Package names are not architecturally relevant. For example, the package "common.util" should not be found as architecturally relevant.
                
                Return your findings in a clear, unambiguous, structured text format so that a follow-up transformation into JSON is easy.
                """;
        String formattingPrompt = """
                Given the last answer (see below), for each component, return a JSON object containing:
                - "name": the primary name of the component (use the most descriptive name).
                - "type": "COMPONENT"
                - "alternativeNames": a list of alternative or ambiguous names, if applicable.
                - "occurrences": a list of lines where the component appears or is referenced.
                
                Output should be a JSON array (and nothing else!), like:
                [
                    {
                        "name": "...",
                        "type": "COMPONENT",
                        "alternativeNames": [...],
                        "occurrences": [...]
                    },
                    ...
                ]
                
                Example:
                [
                    {
                        "name": "AuthenticationService",
                        "type": "COMPONENT",
                        "alternativeNames": ["service"],
                        "occurrences": ["The AuthenticationService handles login requests.", "It forwards valid credentials to the UserDatabase.", "The service logs each attempt."]
                    },
                    {
                        "name": "UserDatabase",
                        "type": "COMPONENT",
                        "alternativeNames": ["DB"],
                        "occurrences": ["It forwards valid credentials to the UserDatabase.", "The DB then validates the credentials."]
                    }
                ]
                """;
        return new TwoPartPrompt(taskPrompt, formattingPrompt);
    }
}
