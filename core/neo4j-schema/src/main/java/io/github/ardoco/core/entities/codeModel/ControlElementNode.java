package io.github.ardoco.core.entities.codeModel;

import org.springframework.data.neo4j.core.schema.Node;

@Node("ControlElement")
public class ControlElementNode extends CodeItemNode {
    public ControlElementNode(String name, String ardocoId) { super(name, ardocoId); }
    protected ControlElementNode() {}
}
