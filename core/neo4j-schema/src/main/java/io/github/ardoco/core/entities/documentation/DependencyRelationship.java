package io.github.ardoco.core.entities.documentation;

import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class DependencyRelationship {

    @RelationshipId
    private Long id;

    private String dependencyType; // Stores DependencyTag.name()

    @TargetNode
    private WordNode targetWord;

    public DependencyRelationship(String dependencyType, WordNode targetWord) {
        this.dependencyType = dependencyType;
        this.targetWord = targetWord;
    }

    public DependencyRelationship() {}

    public String getDependencyType() { return dependencyType; }
    public WordNode getTargetWord() { return targetWord; }
}
