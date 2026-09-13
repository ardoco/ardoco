/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.documentation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Thin Neo4j representation of {@code SimpleText} (raw text + ordered lines), used by NER/Artemis paths.
 */
@Node("SimpleText")
public class SimpleTextNode {

    @Id
    private String identifier;

    private String text;

    private List<String> lines = new ArrayList<>();

    public SimpleTextNode() {
    }

    public SimpleTextNode(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines != null ? lines : new ArrayList<>();
    }
}
