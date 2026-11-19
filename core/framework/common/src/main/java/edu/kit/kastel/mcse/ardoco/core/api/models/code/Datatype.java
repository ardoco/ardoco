/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents a datatype in the code model. Can be a class or interface and serves as a base for {@link ClassUnit} and {@link InterfaceUnit}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ //
        @JsonSubTypes.Type(value = ClassUnit.class, name = "ClassUnit"),//
        @JsonSubTypes.Type(value = InterfaceUnit.class, name = "InterfaceUnit") //
})
@JsonTypeName("Datatype")
public sealed class Datatype extends CodeItem permits ClassUnit, InterfaceUnit {

    @Serial
    private static final long serialVersionUID = -1925023806648753973L;

    @JsonProperty
    private String compilationUnitId;
    @JsonProperty
    private String parentDatatypeId;
    @JsonProperty
    private int startLine = -1;
    @JsonProperty
    private int endLine = -1;
    @JsonProperty
    private List<String> extendedDataTypesIds;
    @JsonProperty
    private List<String> implementedDataTypesIds;
    @JsonProperty
    private List<String> datatypeReferencesIds;
    @JsonProperty
    private List<String> content;

    Datatype() {
        // Jackson
    }

    /**
     * Creates a new datatype with the specified name.
     *
     * @param codeItemRepository the code item repository
     * @param name               the name of the datatype
     * @param content            the content of the datatype
     */
    public Datatype(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, name);
        this.extendedDataTypesIds = new ArrayList<>();
        this.implementedDataTypesIds = new ArrayList<>();
        this.datatypeReferencesIds = new ArrayList<>();
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
    }

    /**
     * Creates a new datatype with the specified name and source location.
     *
     * @param codeItemRepository the code item repository
     * @param name               the name of the datatype
     * @param content            the content of the datatype
     * @param startLine          the 1-indexed start line in the source file, or -1 if unknown
     * @param endLine            the 1-indexed end line in the source file, or -1 if unknown
     */
    public Datatype(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content, int startLine, int endLine) {
        this(codeItemRepository, name, content);
        this.startLine = startLine;
        this.endLine = endLine;
    }

    /**
     * Returns the 1-indexed start line of this datatype in its source file, or -1 if unknown.
     *
     * @return the start line
     */
    public int getStartLine() {
        return startLine;
    }

    /**
     * Returns the 1-indexed end line of this datatype in its source file, or -1 if unknown.
     *
     * @return the end line
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * Creates a new datatype with the specified id, name, and content.
     *
     * @param id                 the identifier of the datatype
     * @param codeItemRepository the code item repository
     * @param name               the name of the datatype
     * @param content            the content of the datatype
     */
    public Datatype(String id, CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content) {
        super(id, codeItemRepository, name);
        this.extendedDataTypesIds = new ArrayList<>();
        this.implementedDataTypesIds = new ArrayList<>();
        this.datatypeReferencesIds = new ArrayList<>();
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
    }

    /**
     * Returns the compilation unit associated with this datatype.
     *
     * @return the compilation unit, or null if not set
     */
    public CodeCompilationUnit getCompilationUnit() {
        CodeItem codeItem = this.codeItemRepository.getCodeItem(this.compilationUnitId);
        if (codeItem instanceof CodeCompilationUnit codeCompilationUnit) {
            return codeCompilationUnit;
        }
        return null;
    }

    /**
     * Returns the parent datatype of this datatype.
     *
     * @return the parent datatype, or null if not set
     */
    public Datatype getParentDatatype() {
        CodeItem codeItem = this.codeItemRepository.getCodeItem(this.parentDatatypeId);
        if (codeItem instanceof Datatype datatype) {
            return datatype;
        }
        return null;
    }

    /**
     * Returns the extended types of this datatype.
     *
     * @return sorted set of extended types
     */
    public SortedSet<Datatype> getExtendedTypes() {
        return this.extendedDataTypesIds.stream().map(id -> {
            CodeItem codeItem = this.codeItemRepository.getCodeItem(id);
            if (codeItem instanceof Datatype datatype) {
                return datatype;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Returns the implemented types of this datatype.
     *
     * @return sorted set of implemented types
     */
    public SortedSet<Datatype> getImplementedTypes() {
        return this.implementedDataTypesIds.stream().map(id -> {
            CodeItem codeItem = this.codeItemRepository.getCodeItem(id);
            if (codeItem instanceof Datatype datatype) {
                return datatype;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Returns the datatype references of this datatype.
     *
     * @return sorted set of datatype references
     */
    public SortedSet<Datatype> getDatatypeReferences() {
        if (this.datatypeReferencesIds == null) {
            return new TreeSet<>();
        }
        return this.datatypeReferencesIds.stream().map(id -> {
            CodeItem codeItem = this.codeItemRepository.getCodeItem(id);
            if (codeItem instanceof Datatype datatype) {
                return datatype;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Returns the content of this datatype as a list of code items.
     *
     * @return list of code items
     */
    @Override
    public List<CodeItem> getContent() {
        return this.codeItemRepository.getCodeItemsByIds(this.content);
    }

    /**
     * Returns the content IDs of this datatype.
     *
     * @return list of content IDs
     */
    @JsonGetter("content")
    public List<String> getContentIds() {
        return new ArrayList<>(this.content);
    }

    /**
     * Replaces the raw content-id list. Used by persistence for a lossless round trip.
     *
     * @param contentIds the raw id list to set
     */
    public void setRawContentIds(List<String> contentIds) {
        this.content = contentIds;
    }

    /**
     * Returns all data types contained in this datatype.
     *
     * @return list of all data types
     */
    @Override
    public List<Datatype> getAllDataTypes() {
        List<Datatype> result = new ArrayList<>();
        result.add(this);
        for (CodeItem codeItem : this.getContent()) {
            result.addAll(codeItem.getAllDataTypes());
        }
        return result;
    }

    /**
     * Returns all class units represented by this datatype.
     *
     * @return list of class units
     */
    public List<ClassUnit> getAllClassUnits() {
        return List.of();
    }

    /**
     * Sets the compilation unit for this datatype.
     *
     * @param compilationUnit the compilation unit to set
     */
    public void setCompilationUnit(CodeCompilationUnit compilationUnit) {
        this.compilationUnitId = compilationUnit.getId();
    }

    /**
     * Sets the parent datatype for this datatype.
     *
     * @param parentDatatype the parent datatype to set
     */
    public void setParentDatatype(Datatype parentDatatype) {
        this.parentDatatypeId = parentDatatype.getId();
    }

    /**
     * Sets the extended types for this datatype.
     *
     * @param extendedDatatypes sorted set of extended datatypes
     */
    public void setExtendedTypes(SortedSet<Datatype> extendedDatatypes) {
        for (Datatype datatype : extendedDatatypes) {
            this.extendedDataTypesIds.add(datatype.getId());
        }
    }

    /**
     * Sets the implemented types for this datatype.
     *
     * @param implementedDatatypes sorted set of implemented datatypes
     */
    public void setImplementedTypes(SortedSet<Datatype> implementedDatatypes) {
        for (Datatype datatype : implementedDatatypes) {
            this.implementedDataTypesIds.add(datatype.getId());
        }
    }

    /**
     * Sets the datatype references for this datatype.
     *
     * @param datatypeDependencies sorted set of datatype dependencies
     */
    public void setDatatypeReference(SortedSet<Datatype> datatypeDependencies) {
        for (Datatype datatype : datatypeDependencies) {
            this.datatypeReferencesIds.add(datatype.getId());
        }
    }

    /**
     * Returns the raw list of extended-datatype ids exactly as stored (may be {@code null} and may contain ids that do not resolve to a datatype in the current
     * repository). Intended for persistence layers that need to round-trip the state verbatim; prefer {@link #getExtendedTypes()} for resolved access.
     *
     * @return the raw extended-datatype id list, possibly {@code null}
     */
    public List<String> getExtendedDataTypesIds() {
        return this.extendedDataTypesIds;
    }

    /**
     * Replaces the raw list of extended-datatype ids. See {@link #getExtendedDataTypesIds()} for the semantics.
     *
     * @param extendedDataTypesIds the raw id list to set (may be {@code null})
     */
    public void setExtendedDataTypesIds(List<String> extendedDataTypesIds) {
        this.extendedDataTypesIds = extendedDataTypesIds;
    }

    /**
     * Returns the raw list of implemented-datatype ids exactly as stored (may be {@code null}). See {@link #getExtendedDataTypesIds()} for the semantics.
     *
     * @return the raw implemented-datatype id list, possibly {@code null}
     */
    public List<String> getImplementedDataTypesIds() {
        return this.implementedDataTypesIds;
    }

    /**
     * Replaces the raw list of implemented-datatype ids. See {@link #getExtendedDataTypesIds()} for the semantics.
     *
     * @param implementedDataTypesIds the raw id list to set (may be {@code null})
     */
    public void setImplementedDataTypesIds(List<String> implementedDataTypesIds) {
        this.implementedDataTypesIds = implementedDataTypesIds;
    }

    /**
     * Returns the raw list of datatype-reference ids exactly as stored (may be {@code null}). See {@link #getExtendedDataTypesIds()} for the semantics.
     *
     * @return the raw datatype-reference id list, possibly {@code null}
     */
    public List<String> getDatatypeReferencesIds() {
        return this.datatypeReferencesIds;
    }

    /**
     * Replaces the raw list of datatype-reference ids. See {@link #getExtendedDataTypesIds()} for the semantics.
     *
     * @param datatypeReferencesIds the raw id list to set (may be {@code null})
     */
    public void setDatatypeReferencesIds(List<String> datatypeReferencesIds) {
        this.datatypeReferencesIds = datatypeReferencesIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Datatype datatype) || !super.equals(o) || !Objects.equals(this.compilationUnitId, datatype.compilationUnitId) || !Objects.equals(
                this.parentDatatypeId, datatype.parentDatatypeId) || this.startLine != datatype.startLine || this.endLine != datatype.endLine) {
            return false;
        }
        if (!Objects.equals(this.extendedDataTypesIds, datatype.extendedDataTypesIds)) {
            return false;
        }
        if (!Objects.equals(this.implementedDataTypesIds, datatype.implementedDataTypesIds)) {
            return false;
        }
        if (!Objects.equals(this.datatypeReferencesIds, datatype.datatypeReferencesIds)) {
            return false;
        }
        return Objects.equals(this.content, datatype.content);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.compilationUnitId != null ? this.compilationUnitId.hashCode() : 0);
        result = 31 * result + (this.parentDatatypeId != null ? this.parentDatatypeId.hashCode() : 0);
        result = 31 * result + this.startLine;
        result = 31 * result + this.endLine;
        result = 31 * result + (this.extendedDataTypesIds != null ? this.extendedDataTypesIds.hashCode() : 0);
        result = 31 * result + (this.implementedDataTypesIds != null ? this.implementedDataTypesIds.hashCode() : 0);
        result = 31 * result + (this.datatypeReferencesIds != null ? this.datatypeReferencesIds.hashCode() : 0);
        return 31 * result + (this.content != null ? this.content.hashCode() : 0);
    }
}
