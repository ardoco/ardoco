package io.github.ardoco.core.entities.documentation;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.ArrayList;
import java.util.List;

@Node("Text")
public class TextNode {

    @Id @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;


    @Relationship(type = "HAS_SENTENCE", direction = Relationship.Direction.OUTGOING)
    private List<SentenceNode> sentences = new ArrayList<>();

    private String ardocoId;

    public TextNode(String ardocoId) {
        this.ardocoId = ardocoId;
    }

    public TextNode() {}

    public void addSentence(SentenceNode sentence) {
        this.sentences.add(sentence);
    }


    public String getId() {
        return id;
    }

    public String getArdocoId() {
        return ardocoId;
    }

    public void setArdocoId(String id) {
        this.ardocoId = id;
    }

    public List<SentenceNode> getSentences() {
        return sentences;
    }

    public void setSentences(List<SentenceNode> sentences) {
        this.sentences = sentences;
    }
}
