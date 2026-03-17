/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.codeModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("CodeModel")
public class CodeModelNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    private String modelId;
    private String metamodel; // Stores the specific class type (e.g., "CODE_WITH_COMPILATION_UNITS")

    @Relationship(type = "CONTAINS_CODE_ROOT", direction = Relationship.Direction.OUTGOING)
    private List<CodeItemNode> content = new ArrayList<>();

    @Relationship(type = "HAS_REPOSITORY_ITEM", direction = Relationship.Direction.OUTGOING)
    private Set<CodeItemNode> allRepositoryItems = new HashSet<>();

    public CodeModelNode(String modelId, String metamodel) {
        this.modelId = modelId;
        this.metamodel = metamodel;
    }

    protected CodeModelNode() {
    }

    public void addContent(CodeItemNode item) {
        this.content.add(item);
    }

    public void addRepositoryItem(CodeItemNode item) {
        this.allRepositoryItems.add(item);
    }

    public Set<CodeItemNode> getAllRepositoryItems() {
        return allRepositoryItems;
    }

    public String getModelId() {
        return modelId;
    }

    public String getMetamodel() {
        return metamodel;
    }

    public List<CodeItemNode> getContent() {
        return content;
    }
}
