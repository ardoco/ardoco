/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.codeModel;

import java.util.ArrayList;
import java.util.List;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ArchitectureType;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("CodeItem")
public class CodeItemNode extends TraceableNode implements Comparable<CodeItemNode> {

    private String name;

    @Relationship(type = "CONTAINS_CODE_ITEM", direction = Relationship.Direction.OUTGOING)
    private List<CodeItemNode> content = new ArrayList<>();

    public CodeItemNode(String name, String ardocoId) {
        super(ardocoId);
        this.name = name;
    }

    protected CodeItemNode() {
    }

    @Override
    public ArchitectureType getModelType() {
        return ArchitectureType.CODE;
    }

    public void addContent(CodeItemNode child) {
        this.content.add(child);
    }

    public String getName() {
        return name;
    }

    public String getArdocoId() {
        return ardocoId;
    }

    public List<CodeItemNode> getContent() {
        return content;
    }

    @Override
    public int compareTo(CodeItemNode o) {
        if (this.ardocoId != null && o.ardocoId != null)
            return this.ardocoId.compareTo(o.ardocoId);
        return this.name.compareTo(o.name);
    }
}
