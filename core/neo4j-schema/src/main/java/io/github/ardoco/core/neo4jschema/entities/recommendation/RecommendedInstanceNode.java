/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.recommendation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import io.github.ardoco.core.neo4jschema.entities.textextraction.NounMappingNode;

/**
 * Graph representation of an ARDoCo {@code RecommendedInstance}.
 * {@code ardocoId} is the entity id assigned in {@code RecommendedInstanceImpl}.
 */
@Node("RecommendedInstance")
public class RecommendedInstanceNode {

    @Id
    private String ardocoId;

    private String name;
    private String type;
    private double probability;
    private String metamodel;

    @Relationship(type = "HAS_NAME_MAPPING", direction = Relationship.Direction.OUTGOING)
    private List<NounMappingNode> nameMappings = new ArrayList<>();

    @Relationship(type = "HAS_TYPE_MAPPING", direction = Relationship.Direction.OUTGOING)
    private List<NounMappingNode> typeMappings = new ArrayList<>();

    public RecommendedInstanceNode() {
    }

    public RecommendedInstanceNode(String ardocoId) {
        this.ardocoId = ardocoId;
    }

    public String getArdocoId() {
        return ardocoId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getProbability() {
        return probability;
    }

    public String getMetamodel() {
        return metamodel;
    }

    public List<NounMappingNode> getNameMappings() {
        return nameMappings;
    }

    public List<NounMappingNode> getTypeMappings() {
        return typeMappings;
    }
}
