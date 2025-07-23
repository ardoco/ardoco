package edu.kit.kastel.mcse.ardoco.ner_for_arch.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.ner_for_arch.model.SoftwareArchitectureDocumentation;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Custom deserializer for converting a JSON array into a set of {@link NamedEntity} instances.
 * <p>
 * This deserializer ensures that each {@link NamedEntity} instance is linked to the provided {@link SoftwareArchitectureDocumentation} as its source text.
 * </p>
 */
public class NamedEntityDeserializer extends JsonDeserializer<Set<NamedEntity>> {

    private final SoftwareArchitectureDocumentation softwareArchitectureDocumentation;

    /**
     * Creates a deserializer that associates deserialized {@link NamedEntity} instances with the given software architecture documentation (SAD).
     *
     * @param softwareArchitectureDocumentation the SAD which is to be linked to each deserialized entity
     */
    public NamedEntityDeserializer(SoftwareArchitectureDocumentation softwareArchitectureDocumentation) {
        this.softwareArchitectureDocumentation = softwareArchitectureDocumentation;
    }

    /**
     * Deserializes a JSON array of named entities, setting their source text reference.
     *
     * @param p    the JSON parser
     * @param ctxt the deserialization context
     * @return a set of {@link NamedEntity} instances with source text set
     * @throws IOException if an error occurs during deserialization
     */
    @Override
    public Set<NamedEntity> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        List<NamedEntity> entities = mapper.readValue(p, new TypeReference<>() {
        });
        for (NamedEntity entity : entities) {
            entity.setSourceText(softwareArchitectureDocumentation);
        }
        return new HashSet<>(entities);
    }
}
