/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.codemodel;

import org.springframework.data.neo4j.core.schema.Node;

@Node("CodePackage")
public class CodePackageNode extends CodeItemNode {
    public CodePackageNode(String name, String ardocoId) {
        super(name, ardocoId);
    }

    protected CodePackageNode() {
    }
}
