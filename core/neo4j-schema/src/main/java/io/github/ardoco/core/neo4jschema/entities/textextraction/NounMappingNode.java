/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.textextraction;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import io.github.ardoco.core.neo4jschema.entities.documentation.PhraseNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.WordNode;

/**
 * Graph representation of an ARDoCo {@code NounMapping} (TextState).
 * {@code ardocoId} is application-assigned so merges can upsert the same node.
 */
@Node("NounMapping")
public class NounMappingNode {

    @Id
    private String ardocoId;

    private String reference;
    private String kind;
    private double probability;
    private boolean isCompound;
    private List<String> surfaceForms = new ArrayList<>();
    private double nameProbability;
    private double typeProbability;

    @Relationship(type = "MAPS_WORD", direction = Relationship.Direction.OUTGOING)
    private List<WordNode> mappedWords = new ArrayList<>();

    @Relationship(type = "HAS_REFERENCE_WORD", direction = Relationship.Direction.OUTGOING)
    private List<WordNode> referenceWords = new ArrayList<>();

    @Relationship(type = "IN_PHRASE", direction = Relationship.Direction.OUTGOING)
    private List<PhraseNode> phrases = new ArrayList<>();

    public NounMappingNode() {
    }

    public NounMappingNode(String ardocoId) {
        this.ardocoId = ardocoId;
    }

    public String getArdocoId() {
        return ardocoId;
    }

    public String getReference() {
        return reference;
    }

    public String getKind() {
        return kind;
    }

    public double getProbability() {
        return probability;
    }

    public boolean isCompound() {
        return isCompound;
    }

    public List<String> getSurfaceForms() {
        return surfaceForms;
    }

    public double getNameProbability() {
        return nameProbability;
    }

    public double getTypeProbability() {
        return typeProbability;
    }

    public List<WordNode> getMappedWords() {
        return mappedWords;
    }

    public List<WordNode> getReferenceWords() {
        return referenceWords;
    }

    public List<PhraseNode> getPhrases() {
        return phrases;
    }
}
