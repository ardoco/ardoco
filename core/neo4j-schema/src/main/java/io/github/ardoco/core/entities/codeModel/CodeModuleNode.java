package io.github.ardoco.core.entities.codeModel;

import org.springframework.data.neo4j.core.schema.Node;

@Node("CodeModule")
public class CodeModuleNode extends CodeItemNode {
    public CodeModuleNode(String name, String ardocoId) { super(name, ardocoId); }
    protected CodeModuleNode() {}
}
