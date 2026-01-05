package io.github.ardoco.core.entities.codeModel;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.HashSet;
import java.util.Set;

@Node("Datatype")
public abstract class DatatypeNode extends CodeItemNode {

    @Relationship(type = "EXTENDS", direction = Relationship.Direction.OUTGOING)
    private Set<DatatypeNode> extendedTypes = new HashSet<>();

    @Relationship(type = "IMPLEMENTS", direction = Relationship.Direction.OUTGOING)
    private Set<DatatypeNode> implementedTypes = new HashSet<>();

    protected DatatypeNode(String name, String ardocoId) { super(name, ardocoId); }
    protected DatatypeNode() {}

    public void addExtendedType(DatatypeNode type) { extendedTypes.add(type); }
    public void addImplementedType(DatatypeNode type) { implementedTypes.add(type); }

    public Set<DatatypeNode> getExtendedTypes() { return extendedTypes; }
    public Set<DatatypeNode> getImplementedTypes() { return implementedTypes; }
}
