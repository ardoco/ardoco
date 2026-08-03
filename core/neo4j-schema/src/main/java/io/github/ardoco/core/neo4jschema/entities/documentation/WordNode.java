/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.documentation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("Word")
public class WordNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    private int position;
    private String text;
    private String lemma;
    private String posTag;

    @Relationship(type = "NEXT_WORD", direction = Relationship.Direction.OUTGOING)
    private WordNode nextWord;

    @Relationship(type = "DEPENDENCY", direction = Relationship.Direction.OUTGOING)
    private List<DependencyRelationship> dependencies = new ArrayList<>();

    public WordNode(int position, String text, String lemma, String posTag) {
        this.position = position;
        this.text = text;
        this.lemma = lemma;
        this.posTag = posTag;
    }

    public WordNode() {
    }

    public void addDependency(String type, WordNode target) {
        this.dependencies.add(new DependencyRelationship(type, target));
    }

    public void setNextWord(WordNode nextWord) {
        this.nextWord = nextWord;
    }

    public String getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

    public String getLemma() {
        return lemma;
    }

    public String getPosTag() {
        return posTag;
    }

    public WordNode getNextWord() {
        return nextWord;
    }

    public List<DependencyRelationship> getDependencies() {
        return dependencies;
    }
}
