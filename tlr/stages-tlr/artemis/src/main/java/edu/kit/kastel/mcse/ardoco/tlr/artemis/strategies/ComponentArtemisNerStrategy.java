package edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.Prompt;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.TwoPartPrompt;

public class ComponentArtemisNerStrategy implements ArtemisNerStrategy {
    @Override
    public Prompt createPrompt(DataRepository dataRepository) {
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
                   - thumbnail — "Each item includes a thumbnail."
                   - suggestion — "Suggestions are generated..."
                   - document — "Uploads include a JSON document."
                   - interaction — "Each interaction is stored separately."
                   - slideshow — "Uploaded slideshows go through conversion..."
                
                
                   Include only when wrapped in named software components that perform active, modular responsibilities (if explicitly named and described).
                
                3. DO include technical subsystems described with proper software roles, and clearly scoped:
                   - (Web) server — if described as a component implementing client-server communication or event dispatching
                   - (Web) client — if described as rendering or subscribing to events/data
                   - Media Server / MS — as a media streaming component implementing SFU/MCU
                
                4. Do not include:
                   - Package, class, or namespace names (e.g., common.util, x.y.z)
                   - Interfaces (unless directly implemented and deployed)
                   - General use of technologies or third-party tools like "React" or "Spring" unless internally wrapped as system components
                
                5. A component is valid only if both conditions hold:
                    a) The entity is presented as a distinct architectural unit with a defined boundary.
                    b) The entity participates in the system architecture as an independently described service, application, server, client, subsystem, engine, pipeline, processor, gateway, adapter, or similarly scoped runtime unit.
                
                Possessing technical responsibilities alone is not sufficient.
                
                6. Reverse pronoun references are allowed only when strongly tied to a previously named component across adjacent lines.
                   Do not infer vague or implied components through generic phrases like:
                   - it handles the process
                   - this system
                   - the module
                
                7. A sentence may be assigned to a component only if the sentence itself contains:
                    - the component name,
                    - a known alias/abbreviation of the component,
                    - or a clear and unambiguous pronoun/reference to that component.
                
                    Do not extend component descriptions across subsequent sentences that lack such a reference, even if they appear immediately after a component description.
                
                8. Do not create implied components from action nouns (e.g., "validation", "authentication", "routing") unless these are mentioned as named, distinct architectural elements.
                
                9. If an external technology (e.g., PostgreSQL, Apache Kafka, etc.) is used in a custom component (e.g., our PostgresSyncService, or KafkaEventPublisher), include that named component — not the technology itself.
                
                10. Exclude implementation-level and organizational constructs.
                    An entity is only a component if the text presents it as a distinct architectural unit rather than an internal element of another unit.
                
                11. Respect architectural containment.
                    If an entity is described as existing within, belonging to, or operating as part of another named component, do not extract it unless it is itself described as a separate architectural unit.
                    Prefer higher-level architectural units over their internal implementation elements.
                
                12. APIs, technologies, and products are not components by default.
                    References to APIs, frameworks, databases, protocols, middleware, web servers, application servers, or third-party products should only be extracted when the text explicitly models them as architectural units of the analyzed system.
                
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

        taskPrompt += getPossibleEntities(dataRepository);
        return new TwoPartPrompt(taskPrompt, formattingPrompt);
    }

    private StringBuilder getPossibleEntities(DataRepository dataRepository) {
        Map<NamedEntityType, Set<String>> possibleEntities = new EnumMap<>(NamedEntityType.class);
        possibleEntities.put(NamedEntityType.COMPONENT, new TreeSet<>());

        var modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        var model = modelStatesData.getModel(getMetamodel());
        for (var endpoint : model.getEndpoints()) {
            String endpointName = endpoint.getName();
            possibleEntities.get(NamedEntityType.COMPONENT).add(Objects.requireNonNull(endpointName));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n\nAs support, here is a list of entities that could be mentioned in the text:\n");
        for (Map.Entry<NamedEntityType, Set<String>> entry : possibleEntities.entrySet()) {
            NamedEntityType type = entry.getKey();
            Set<String> names = entry.getValue();
            if (names.isEmpty()) {
                continue;
            }
            sb.append(type.toString().toLowerCase()).append(" entities: ");
            sb.append(String.join(", ", names));
            sb.append("\n");
        }
        sb.append("\n");

        return sb;
    }

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.ARCHITECTURE_WITH_COMPONENTS;
    }

    @Override
    public NamedEntityType getNamedEntityType() {
        return NamedEntityType.COMPONENT;
    }
}
