/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.ner;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

/**
 * Graph representation of a NER {@code NamedArchitectureEntity}.
 */
@Node("NamedArchitectureEntity")
public class NamedArchitectureEntityNode {

    @Id
    private String ardocoId;

    private String name;
    private String metamodel;
    private boolean unlinked;
    private List<String> alternativeNames = new ArrayList<>();

    @Relationship(type = "HAS_OCCURRENCE", direction = Relationship.Direction.OUTGOING)
    private List<NamedArchitectureEntityOccurrenceNode> occurrences = new ArrayList<>();

    public NamedArchitectureEntityNode() {
    }

    public NamedArchitectureEntityNode(String ardocoId) {
        this.ardocoId = ardocoId;
    }

    public String getArdocoId() {
        return ardocoId;
    }

    public void setArdocoId(String ardocoId) {
        this.ardocoId = ardocoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetamodel() {
        return metamodel;
    }

    public void setMetamodel(String metamodel) {
        this.metamodel = metamodel;
    }

    public boolean isUnlinked() {
        return unlinked;
    }

    public void setUnlinked(boolean unlinked) {
        this.unlinked = unlinked;
    }

    public List<String> getAlternativeNames() {
        return alternativeNames;
    }

    public void setAlternativeNames(List<String> alternativeNames) {
        this.alternativeNames = alternativeNames != null ? alternativeNames : new ArrayList<>();
    }

    public List<NamedArchitectureEntityOccurrenceNode> getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(List<NamedArchitectureEntityOccurrenceNode> occurrences) {
        this.occurrences = occurrences != null ? occurrences : new ArrayList<>();
    }
}
