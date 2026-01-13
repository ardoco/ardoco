package io.github.ardoco.core.entities.architectureModel;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.Objects;

@Node("ArchitectureMethod")
public class ArchitectureMethodNode implements Comparable<ArchitectureMethodNode> {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    private String name;
    private String ardocoId;

    protected ArchitectureMethodNode() {}

    public ArchitectureMethodNode(String name, String ardocoId) {
        this.name = name;
        this.ardocoId = ardocoId;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getArdocoId() {
        return ardocoId;
    }

    @Override
    public int compareTo(ArchitectureMethodNode o) {
        if (this == o) return 0;
        if (this.id == null || o.id == null) {
            if (this.name == null && o.name == null) return 0;
            if (this.name == null) return -1;
            if (o.name == null) return 1;
            return this.name.compareTo(o.name);
        }
        return this.id.compareTo(o.id);
    }
}
