package io.github.ardoco.core.entities.codeModel;

import org.springframework.data.neo4j.core.schema.Node;

@Node("InterfaceUnit")
public class InterfaceUnitNode extends CodeItemNode {
    public InterfaceUnitNode(String name, String ardocoId) {
        super(name, ardocoId);
    }
    protected InterfaceUnitNode() {}
}
