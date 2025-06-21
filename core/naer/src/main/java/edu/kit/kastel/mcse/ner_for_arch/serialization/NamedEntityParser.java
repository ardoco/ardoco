package edu.kit.kastel.mcse.ner_for_arch.serialization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ner_for_arch.model.SoftwareArchitectureDocumentation;

import java.io.IOException;
import java.util.Set;

/**
 * Utility class for converting JSON representations of named entities into Java objects.
 */
public class NamedEntityParser {

    private NamedEntityParser() {
        // utility class -> prevent instantiation
    }

    /**
     * Deserializes a JSON array representing named entities into a set of {@link NamedEntity} objects.
     *
     * @param json the JSON string to deserialize; must represent a JSON array of named entity objects
     * @param sad  the software architecture documentation associated with the entities
     * @return a set of deserialized {@link NamedEntity} objects
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
}
