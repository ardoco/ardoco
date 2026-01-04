package io.github.ardoco.core.entities.architectureModel;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.SortedSet;
import java.util.TreeSet;

@Node("ArchitectureInterface")
public class ArchitectureInterfaceNode implements Comparable<ArchitectureInterfaceNode> {

    @Id @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    private String name;
    private String ardocoId;
    private String type;

    @Relationship(type = "HAS_METHOD", direction = Relationship.Direction.OUTGOING)
    private SortedSet<ArchitectureMethodNode> methodSignatures = new TreeSet<>();

    public ArchitectureInterfaceNode(String name, String type, String ardocoId) {
        this.name = name;
        this.type = type;
        this.ardocoId = ardocoId;
    }

    protected ArchitectureInterfaceNode() {}

    public void addMethodSignature(ArchitectureMethodNode methodNode) {
        this.methodSignatures.add(methodNode);
    }

    public String getName() { return name; }
    public String getArdocoId() { return ardocoId; }
    public String getId() { return id; }
    public String getType() { return type; }
    public SortedSet<ArchitectureMethodNode> getMethodSignatures() { return methodSignatures; }

    @Override
    public int compareTo(ArchitectureInterfaceNode o) {
        if (this == o) return 0;

        // Prefer ardocoId (stable domain ID)
        if (this.ardocoId != null && o.ardocoId != null) {
            return this.ardocoId.compareTo(o.ardocoId);
        }

        // Fallback to internal ID
        if (this.id != null && o.id != null) {
            return this.id.compareTo(o.id);
        }

        // Fallback to Name
        if (this.name != null && o.name != null) {
            return this.name.compareTo(o.name);
        }
        return 0;
    }
}
