/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.codeModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Datatype")
public abstract class DatatypeNode extends CodeItemNode {

    /**
     * The id of the compilation unit that owns this datatype, if any. Stored explicitly (see the rationale on {@link CodeItemNode#getParentModuleId()}) so it is
     * preserved verbatim on a save/load round trip rather than being re-derived from the containment hierarchy.
     */
    private String compilationUnitId;

    /**
     * The id of the parent datatype, if any. Stored explicitly for the same reason as {@link #compilationUnitId}.
     */
    private String parentDatatypeId;

    /**
     * The raw extended/implemented/referenced datatype id lists, stored verbatim as node properties.
     *
     * <p>These duplicate the information in the {@code EXTENDS}/{@code IMPLEMENTS}/{@code REFERENCES_DATATYPE} relationships below on purpose: the relationships
     * are kept for graph queries/visualization, but they can only ever point to datatypes that actually exist as nodes and they carry no ordering or
     * {@code null}-vs-empty information. The domain model, however, treats these id lists verbatim (order-sensitive, and a {@code null} list is distinct from an
     * empty one), so the raw lists are persisted here to allow a lossless round trip.
     */
    private List<String> extendedDataTypesIds;
    private List<String> implementedDataTypesIds;
    private List<String> datatypeReferencesIds;

    @Relationship(type = "EXTENDS", direction = Relationship.Direction.OUTGOING)
    private Set<DatatypeNode> extendedTypes = new HashSet<>();

    @Relationship(type = "IMPLEMENTS", direction = Relationship.Direction.OUTGOING)
    private Set<DatatypeNode> implementedTypes = new HashSet<>();

    @Relationship(type = "REFERENCES_DATATYPE", direction = Relationship.Direction.OUTGOING)
    private Set<DatatypeNode> referencedDatatypes = new HashSet<>();

    protected DatatypeNode(String name, String ardocoId) {
        super(name, ardocoId);
    }

    protected DatatypeNode() {
    }

    public void addExtendedType(DatatypeNode type) {
        extendedTypes.add(type);
    }

    public void addImplementedType(DatatypeNode type) {
        implementedTypes.add(type);
    }

    public void addReferencedDatatype(DatatypeNode type) {
        referencedDatatypes.add(type);
    }

    public Set<DatatypeNode> getExtendedTypes() {
        return extendedTypes;
    }

    public Set<DatatypeNode> getImplementedTypes() {
        return implementedTypes;
    }

    public Set<DatatypeNode> getReferencedDatatypes() {
        return referencedDatatypes;
    }

    public String getCompilationUnitId() {
        return compilationUnitId;
    }

    public void setCompilationUnitId(String compilationUnitId) {
        this.compilationUnitId = compilationUnitId;
    }

    public String getParentDatatypeId() {
        return parentDatatypeId;
    }

    public void setParentDatatypeId(String parentDatatypeId) {
        this.parentDatatypeId = parentDatatypeId;
    }

    public List<String> getExtendedDataTypesIds() {
        return extendedDataTypesIds;
    }

    public void setExtendedDataTypesIds(List<String> extendedDataTypesIds) {
        this.extendedDataTypesIds = extendedDataTypesIds;
    }

    public List<String> getImplementedDataTypesIds() {
        return implementedDataTypesIds;
    }

    public void setImplementedDataTypesIds(List<String> implementedDataTypesIds) {
        this.implementedDataTypesIds = implementedDataTypesIds;
    }

    public List<String> getDatatypeReferencesIds() {
        return datatypeReferencesIds;
    }

    public void setDatatypeReferencesIds(List<String> datatypeReferencesIds) {
        this.datatypeReferencesIds = datatypeReferencesIds;
    }
}
