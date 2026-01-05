package io.github.ardoco.core.entities.codeModel;

import org.springframework.data.neo4j.core.schema.Node;

@Node("ClassUnit")
public class ClassUnitNode extends CodeItemNode {
    public ClassUnitNode(String name, String ardocoId) {
        super(name, ardocoId);
    }
    protected ClassUnitNode() {}
}
