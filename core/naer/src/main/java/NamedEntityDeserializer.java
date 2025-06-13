import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NamedEntityDeserializer extends JsonDeserializer<Set<NamedEntity>> {

    private final SoftwareArchitectureDocumentation softwareArchitectureDocumentation;

    public NamedEntityDeserializer(SoftwareArchitectureDocumentation softwareArchitectureDocumentation) {
        this.softwareArchitectureDocumentation = softwareArchitectureDocumentation;
    }

    @Override
    public Set<NamedEntity> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        List<NamedEntity> entities = mapper.readValue(p, new TypeReference<>() {});
        for (NamedEntity entity : entities) {
            entity.setSourceText(softwareArchitectureDocumentation);
        }
        return new HashSet<>(entities);
    }
}
