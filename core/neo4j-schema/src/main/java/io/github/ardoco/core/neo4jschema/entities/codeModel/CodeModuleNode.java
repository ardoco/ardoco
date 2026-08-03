/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.codeModel;

import org.springframework.data.neo4j.core.schema.Node;

@Node("CodeModule")
public class CodeModuleNode extends CodeItemNode {
    public CodeModuleNode(String name, String ardocoId) {
        super(name, ardocoId);
    }

    protected CodeModuleNode() {
    }
}
