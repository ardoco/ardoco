/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.codeModel;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Datatype")
public abstract class DatatypeNode extends CodeItemNode {

    @Relationship(type = "EXTENDS", direction = Relationship.Direction.OUTGOING)
    private Set<DatatypeNode> extendedTypes = new HashSet<>();

    @Relationship(type = "IMPLEMENTS", direction = Relationship.Direction.OUTGOING)
    private Set<DatatypeNode> implementedTypes = new HashSet<>();

    protected DatatypeNode(String name, String ardocoId) {
        super(name, ardocoId);
    }

    protected DatatypeNode() {
    }

    public void addExtendedType(DatatypeNode type) {
        extendedTypes.add(type);
    }

    public void addImplementedType(DatatypeNode type) {
        implementedTypes.add(type);
    }

    public Set<DatatypeNode> getExtendedTypes() {
        return extendedTypes;
    }

    public Set<DatatypeNode> getImplementedTypes() {
        return implementedTypes;
    }
}
