/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.architectureModel;

import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("ArchitectureInterface")
public class ArchitectureInterfaceNode extends ArchitectureItemNode implements Comparable<ArchitectureInterfaceNode> {

    private String type;

    @Relationship(type = "HAS_METHOD", direction = Relationship.Direction.OUTGOING)
    private SortedSet<ArchitectureMethodNode> methodSignatures = new TreeSet<>();

    public ArchitectureInterfaceNode(String name, String type, String ardocoId) {
        super(name, ardocoId);
        this.type = type;
    }

    protected ArchitectureInterfaceNode() {
    }

    public void addMethodSignature(ArchitectureMethodNode methodNode) {
        this.methodSignatures.add(methodNode);
    }

    public String getType() {
        return type;
    }

    public SortedSet<ArchitectureMethodNode> getMethodSignatures() {
        return methodSignatures;
    }

    @Override
    public int compareTo(ArchitectureInterfaceNode o) {
        if (this == o)
            return 0;

        if (this.ardocoId != null && o.ardocoId != null) {
            return this.ardocoId.compareTo(o.ardocoId);
        }

        if (this.name != null && o.name != null) {
            return this.name.compareTo(o.name);
        }
        return 0;
    }
}
