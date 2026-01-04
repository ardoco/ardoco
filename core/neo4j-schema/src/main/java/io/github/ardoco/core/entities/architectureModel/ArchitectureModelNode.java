package io.github.ardoco.core.entities.architectureModel;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.ArrayList;
import java.util.List;

@Node("ArchitectureModel")
public class ArchitectureModelNode {

    @Id @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    private String modelId;

    private String metamodel;

    @Relationship(type = "HAS_COMPONENT", direction = Relationship.Direction.OUTGOING)
    private List<ArchitectureComponentNode> components = new ArrayList<>();

    @Relationship(type = "HAS_INTERFACE", direction = Relationship.Direction.OUTGOING)
    private List<ArchitectureInterfaceNode> interfaces = new ArrayList<>();

    public ArchitectureModelNode(String modelId, String metamodel) {
        this.metamodel = metamodel;
        this.modelId = modelId;
    }

    protected ArchitectureModelNode() {}

    public void addComponent(ArchitectureComponentNode component) {
        this.components.add(component);
    }

    public void addInterface(ArchitectureInterfaceNode iface) {
        this.interfaces.add(iface);
    }


    public String getModelId() {
        return modelId;
    }

    public List<ArchitectureComponentNode> getComponents() {
        return components;
    }

    public List<ArchitectureInterfaceNode> getInterfaces() {
        return interfaces;
    }

    public String getMetamodel() {
        return metamodel;
    }

}
