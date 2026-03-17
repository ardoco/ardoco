/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.codeModel;

import org.springframework.data.neo4j.core.schema.Node;

@Node("ClassUnit")
public class ClassUnitNode extends DatatypeNode {
    public ClassUnitNode(String name, String ardocoId) {
        super(name, ardocoId);
    }

    protected ClassUnitNode() {
    }
}
