package io.github.ardoco.core.entities.codeModel;

import org.springframework.data.neo4j.core.schema.Node;

@Node("CodeModule")
public abstract class CodeModuleNode extends CodeItemNode {
    protected CodeModuleNode(String name, String ardocoId) { super(name, ardocoId); }
    protected CodeModuleNode() {}
}
