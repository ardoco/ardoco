/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.architectureModel;

import org.springframework.data.neo4j.core.schema.Node;

@Node("ArchitectureMethod")
public class ArchitectureMethodNode extends ArchitectureItemNode implements Comparable<ArchitectureMethodNode> {

    protected ArchitectureMethodNode() {
    }

    public ArchitectureMethodNode(String name, String ardocoId) {
        super(name, ardocoId);
    }

    @Override
    public int compareTo(ArchitectureMethodNode o) {
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
