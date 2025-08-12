/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

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
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

@Deterministic("Currently not fully deterministic due to NAER.")
public class NerInformant extends Informant {

    private final LargeLanguageModel llm;

    public NerInformant(DataRepository dataRepository, LargeLanguageModel llm) {
        super(NerInformant.class.getSimpleName(), dataRepository);
        this.llm = Objects.requireNonNull(llm);
    }

    @Override
    public void process() {
        var nerConnectionStates = DataRepositoryHelper.getNerConnectionStates(dataRepository).asPipelineStepData(NerConnectionStatesImpl.class).orElseThrow();

        var text = DataRepositoryHelper.getSimpleText(dataRepository);
        SoftwareArchitectureDocumentation sad = new SoftwareArchitectureDocumentation(text.getText());

        var chatModel = llm.create();

        var modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        for (var metamodel : modelStatesData.getMetamodels()) {
            if (!metamodel.isArchitectureModel()) {
                continue;
            }
            var prompt = getPrompt();
            var namedEntityRecognizer = new NamedEntityRecognizer.Builder().chatModel(chatModel).prompt(prompt).build();
            var possibleEntities = getPossibleEntities(metamodel);
            var namedArchitectureEntities = recognizeNamedArchitectureEntities(namedEntityRecognizer, sad, possibleEntities);

            var nerConnectionState = nerConnectionStates.getNerConnectionState(metamodel);
            nerConnectionState.addNamedEntities(namedArchitectureEntities);
        }
    }

    private static Set<NamedArchitectureEntity> recognizeNamedArchitectureEntities(NamedEntityRecognizer namedEntityRecognizer,
            SoftwareArchitectureDocumentation sad, Map<NamedEntityType, Set<String>> possibleEntities) {
        // TODO This is not fully deterministic .. as the hashset has a random order. This should be fixed in NAER.
        var namedEntities = namedEntityRecognizer.recognize(sad, possibleEntities);

        MutableList<NamedEntity> namedEntitiesList = Lists.mutable.ofAll(namedEntities);
        // Sort the named entities by their name to ensure a deterministic order
        namedEntitiesList.sortThisBy(NamedEntity::getName);
        return transformNamedEntitiesToNamedArchitectureEntities(namedEntitiesList);
    }

    private static Set<NamedArchitectureEntity> transformNamedEntitiesToNamedArchitectureEntities(MutableList<NamedEntity> namedEntities) {
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
                Identify all architecturally relevant software components that are explicitly named in the following text.

                For each identified component, provide:
                - The primary name (as it appears in the text)
                - All alternative names or abbreviations used for the same component in the text (case-insensitive)
                - All full lines where the component is mentioned (directly or via clear context)

                Rules for identifying components:

                1. Only include explicit modular software components with distinct technical responsibilities. These may include:
                   - services (e.g., UserService)
                   - APIs (e.g., PaymentAPI)
                   - adapters, handlers, managers, routers, engines
                   - infrastructure components (e.g., Media Server, Presentation Conversion Pipeline)
                   - client-side or server-side subsystems (e.g., electron client, backend server)

                2. Exclude domain-level entities, even if capitalized — such as business data objects, file types, or general functionalities — unless used as part of a named technical unit.
                   Do not include non-technical concepts even if mentioned with verbs like "convert", "generate", or "store" — these are often subject-side actions unless framed as components.

                   Examples of domain terms (do not include):
                   - image — "Each item includes an image."
                   - recommendation — "Recommendations are generated..."
                   - file — "Uploads include a JSON file."
                   - session — "Each session is stored separately."
                   - presentation — "Uploaded presentations go through conversion..."


                   Include only when wrapped in named software components that perform active, modular responsibilities (if explicitly named and described).

                3. DO include technical subsystems described with proper software roles, and clearly scoped:
                   - (Web) server — if described as a component implementing client-server communication or event dispatching
                   - (Web) client — if described as rendering or subscribing to events/data
                   - Media Server / MS — as a media streaming component implementing SFU/MCU

                4. Do not include:
                   - Package, class, or namespace names (e.g., common.util, x.y.z)
                   - Interfaces (unless directly implemented and deployed)
                   - General use of technologies or third-party tools like "React" or "Spring" unless internally wrapped as system components

                5. A component is valid if:
                   a) Its name includes a functional suffix or architecture-relevant term (Service, Client, Engine, Manager, Adapter, Server, Router, Converter, etc.)
                   OR
                   b) The text clearly describes it as implementing a technical function within the system (e.g., routing requests, synchronizing state, managing media streams)

                6. Reverse pronoun references are allowed only when strongly tied to a previously named component across adjacent lines.
                   Do not infer vague or implied components through generic phrases like:
                   - it handles the process
                   - this system
                   - the module

                7. Do not create implied components from action nouns (e.g., "conversion", "delivery", "recommendation") unless these are mentioned as named, distinct architectural elements.

                8. If an external technology (e.g., MongoDB, Redis, etc.) is used in a custom component (e.g., our RedisPublisher, or MongoSyncService), include that named component — not the technology itself.


                Return the results in a clearly structured, unambiguous plain-text format that enables straightforward conversion to JSON (e.g., using key-value sections per component).
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
