/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.architectureModel;

import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("ArchitectureComponent")
public class ArchitectureComponentNode extends ArchitectureItemNode implements Comparable<ArchitectureComponentNode> {

    private String type;

    @Relationship(type = "HAS_SUBCOMPONENT", direction = Relationship.Direction.OUTGOING)
    private SortedSet<ArchitectureComponentNode> subcomponents = new TreeSet<>();

    @Relationship(type = "PROVIDES_INTERFACE", direction = Relationship.Direction.OUTGOING)
    private SortedSet<ArchitectureInterfaceNode> providedInterfaces = new TreeSet<>();

    @Relationship(type = "REQUIRES_INTERFACE", direction = Relationship.Direction.OUTGOING)
    private SortedSet<ArchitectureInterfaceNode> requiredInterfaces = new TreeSet<>();

    public ArchitectureComponentNode(String name, String type, String ardocoId) {
        super(name, ardocoId);
        this.type = type;
    }

    protected ArchitectureComponentNode() {
    }

    public void addSubcomponent(ArchitectureComponentNode component) {
        this.subcomponents.add(component);
    }

    public void addProvidedInterface(ArchitectureInterfaceNode iface) {
        this.providedInterfaces.add(iface);
    }

    public void addRequiredInterface(ArchitectureInterfaceNode iface) {
        this.requiredInterfaces.add(iface);
    }

    public String getType() {
        return type;
    }

    public SortedSet<ArchitectureComponentNode> getSubcomponents() {
        return subcomponents;
    }

    public SortedSet<ArchitectureInterfaceNode> getProvidedInterfaces() {
        return providedInterfaces;
    }

    public SortedSet<ArchitectureInterfaceNode> getRequiredInterfaces() {
        return requiredInterfaces;
    }

    @Override
    public int compareTo(ArchitectureComponentNode o) {
        if (this == o)
            return 0;
        if (this.ardocoId != null && o.ardocoId != null) {
            return this.ardocoId.compareTo(o.ardocoId);
        }
        if (this.id != null && o.id != null) {
            return this.id.compareTo(o.id);
        }
        if (this.name != null && o.name != null) {
            return this.name.compareTo(o.name);
        }
        return 0;
    }
}
