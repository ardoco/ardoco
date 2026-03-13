/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.codeModel;

import java.util.ArrayList;
import java.util.List;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ArchitectureType;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("CodeItem")
public class CodeItemNode extends TraceableNode implements Comparable<CodeItemNode> {

//    @Id
//    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
//    protected String id;

    private String name;
//    private String ardocoId; // The domain ID

    // Generic containment relationship for all subtypes (Package->Class, Class->Method, etc.)
    @Relationship(type = "CONTAINS_CODE_ITEM", direction = Relationship.Direction.OUTGOING)
    private List<CodeItemNode> content = new ArrayList<>();

    public CodeItemNode(String name, String ardocoId) {
        super(ardocoId);
        this.name = name;
//        this.ardocoId = ardocoId;
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
