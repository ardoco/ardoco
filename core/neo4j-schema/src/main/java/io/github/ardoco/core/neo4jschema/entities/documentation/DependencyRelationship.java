/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.documentation;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

/**
 * Represents a relationship between two Word Nodes
 */
@RelationshipProperties
public class DependencyRelationship {

    @Id
    @GeneratedValue
    private String id;

    private String dependencyType; // Stores DependencyTag.name()

    @TargetNode
    private WordNode targetWord;

    public DependencyRelationship(String dependencyType, WordNode targetWord) {
        this.dependencyType = dependencyType;
        this.targetWord = targetWord;
    }

    public DependencyRelationship() {
    }

    public String getDependencyType() {
        return dependencyType;
    }

    public WordNode getTargetWord() {
        return targetWord;
    }
}
