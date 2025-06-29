package edu.kit.kastel.mcse.ner_for_arch.serialization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntityReferenceType;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntityType;
import edu.kit.kastel.mcse.ner_for_arch.model.SoftwareArchitectureDocumentation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for parsing named entities from various input formats.
 */
public class NamedEntityParser {

    private NamedEntityParser() {
        // utility class -> prevent instantiation
    }

    /**
     * Deserializes a JSON array representing named entities into a set of {@link NamedEntity} instances.
     *
     * @param json the JSON string to deserialize; must represent a JSON array of named entity instances
     * @param sad  the software architecture documentation associated with the entities
     * @return a set of deserialized {@link NamedEntity} instances
     * @throws IOException if deserialization fails (e.g. malformed JSON)
     */
    public static Set<NamedEntity> fromJson(String json, SoftwareArchitectureDocumentation sad) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Set.class, new NamedEntityDeserializer(sad));
        mapper.registerModule(module);
        return mapper.readValue(json, new TypeReference<>() {
        });
    }

    /**
     * Parses a string representation of named entities into a set of {@link NamedEntity} instances.
     * The input string must follow the specific format created by prompts using the type {@link edu.kit.kastel.mcse.ner_for_arch.recognizer.PromptType#STRUCTURED_TEXT_OUTPUT_PROMPT}.
     *
     * @param str                               the string (structured text format of named entities)
     * @param softwareArchitectureDocumentation the software architecture documentation associated with the named entities
     * @return a set of parsed {@link NamedEntity} instances
     * @throws IOException if the input string is invalid or cannot be parsed
     */
    public static Set<NamedEntity> fromString(String str, SoftwareArchitectureDocumentation softwareArchitectureDocumentation) throws IOException {
        System.out.println("Parsing: \n" + str);
        Map<String, NamedEntity> entityMap = new HashMap<>();
        String[] lines = str.split("\\R");

        boolean parsingAlternativeNames = false;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            if ((line.trim()).equalsIgnoreCase("Alternative names:")) {
                parsingAlternativeNames = true;
                continue;
            }

            if (!parsingAlternativeNames) {
                // Parse entity occurrence: <name>, <lineNumber>, <referenceType>
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    throw new IOException("Invalid entity occurrence format: " + line);
                }

                String name = parts[0].trim();
                int lineNumber = Integer.parseInt(parts[1].trim());
                NamedEntityReferenceType referenceType = NamedEntityReferenceType.valueOf(parts[2].trim());

                NamedEntity entity = entityMap.get(name);
                if (entity == null) {
                    entity = new NamedEntity(name, NamedEntityType.COMPONENT); //TODO das dann auch anpassen
                    entity.setSourceText(softwareArchitectureDocumentation);
                    entityMap.put(name, entity);
                }
                entity.addOccurrence(lineNumber, referenceType);

            } else {
                // Parse alternative names: <componentName>: <alt1>, <alt2>, ...
                String[] parts = line.split(":");
                if (parts.length != 2) {
                    throw new IOException("Invalid alternative names format: " + line);
                }

                String name = parts[0].trim();
                String alternativesStr = parts[1].trim();
                if (alternativesStr.equalsIgnoreCase("None")) {
                    continue;
                }

                NamedEntity entity = entityMap.get(name);
                if (entity != null) {
                    String[] alternatives = alternativesStr.split(",");
                    for (String alt : alternatives) {
                        entity.addAlternativeName(alt.trim());
                    }
                } else {
                    throw new IOException("Alternative names for unknown component: " + name);
                }


            }
        }

        return new HashSet<>(entityMap.values());
    }
}
