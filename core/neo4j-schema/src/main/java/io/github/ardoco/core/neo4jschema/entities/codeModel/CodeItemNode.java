/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.codeModel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ArchitectureType;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

@Node("CodeItem")
public class CodeItemNode extends TraceableNode implements Comparable<CodeItemNode> {

    private String name;

    /**
     * The id of the parent {@code CodeModule}, if any. Stored explicitly (rather than derived from the containment hierarchy) because the domain model represents
     * parent links and content lists independently: an item can appear in another item's content list while its own parent id is {@code null}. Deriving the parent
     * from containment would fabricate parent links that were not present in the original model and break round-trip equality.
     */
    private String parentModuleId;

    /**
     * The ordered ids of this item's content, stored verbatim as a node property.
     *
     * <p>This duplicates the {@code CONTAINS_CODE_ITEM} relationship below on purpose: the relationship is kept for graph queries/visualization, but Neo4j
     * relationships are unordered whereas the domain content list is order-sensitive ({@code CodeModule}/{@code ClassUnit#equals} compare the content lists in
     * order). The verbatim id list here is the source of truth used to rebuild the domain content in its original order.
     */
    private List<String> contentIds;

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

    public String getParentModuleId() {
        return parentModuleId;
    }

    public void setParentModuleId(String parentModuleId) {
        this.parentModuleId = parentModuleId;
    }

    public List<String> getContentIds() {
        return contentIds;
    }

    public void setContentIds(List<String> contentIds) {
        this.contentIds = contentIds;
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
