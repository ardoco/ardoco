/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.architectureModel;

import org.springframework.data.neo4j.core.schema.Node;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ArchitectureType;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

@Node("ArchitectureItem")
public abstract class ArchitectureItemNode extends TraceableNode {

    protected String name;

    public ArchitectureItemNode(String name, String ardocoId) {
        super(ardocoId);
        this.name = name;
    }

    protected ArchitectureItemNode() {
    }

    public String getName() {
        return name;
    }

    public String getArdocoId() {
        return ardocoId;
    }

    @Override
    public ArchitectureType getModelType() {
        return ArchitectureType.ARCHITECTURE;
    }
}
