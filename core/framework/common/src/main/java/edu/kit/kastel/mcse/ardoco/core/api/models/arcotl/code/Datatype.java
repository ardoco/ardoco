/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ //
        @JsonSubTypes.Type(value = ClassUnit.class, name = "ClassUnit"),//
        @JsonSubTypes.Type(value = InterfaceUnit.class, name = "InterfaceUnit") //
})
@JsonTypeName("Datatype")
public sealed class Datatype extends CodeItem permits ClassUnit, InterfaceUnit {

    private static final long serialVersionUID = -1925023806648753973L;

    @JsonProperty
    private String compilationUnitId;
    @JsonProperty
    private String parentDatatypeId;
    @JsonProperty
    private List<String> extendedDataTypesIds;
    @JsonProperty
    private List<String> implementedDataTypesIds;
    @JsonProperty
    private List<String> datatypeReferencesIds;

    Datatype() {
        // Jackson
    }

    public Datatype(CodeItemRepository codeItemRepository, String name) {
        super(codeItemRepository, name);
        this.extendedDataTypesIds = new ArrayList<>();
        this.implementedDataTypesIds = new ArrayList<>();
        this.datatypeReferencesIds = new ArrayList<>();
    }

    public CodeCompilationUnit getCompilationUnit() {
        CodeItem codeItem = this.codeItemRepository.getCodeItem(this.compilationUnitId);
        if (codeItem instanceof CodeCompilationUnit codeCompilationUnit) {
            return codeCompilationUnit;
        }
        return null;
    }

    public Datatype getParentDatatype() {
        CodeItem codeItem = this.codeItemRepository.getCodeItem(this.parentDatatypeId);
        if (codeItem instanceof Datatype datatype) {
            return datatype;
        }
        return null;
    }

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

    public SortedSet<Datatype> getDatatypeReferences() {
        return this.datatypeReferencesIds.stream().map(id -> {
            CodeItem codeItem = this.codeItemRepository.getCodeItem(id);
            if (codeItem instanceof Datatype datatype) {
                return datatype;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));
    }

    public void setCompilationUnit(CodeCompilationUnit compilationUnit) {
        this.compilationUnitId = compilationUnit.getId();
    }

    public void setParentDatatype(Datatype parentDatatype) {
        this.parentDatatypeId = parentDatatype.getId();
    }

    public void setExtendedTypes(SortedSet<Datatype> extendedDatatypes) {
        for (Datatype datatype : extendedDatatypes) {
            this.extendedDataTypesIds.add(datatype.getId());
        }
    }

    public void setImplementedTypes(SortedSet<Datatype> implementedDatatypes) {
        for (Datatype datatype : implementedDatatypes) {
            this.implementedDataTypesIds.add(datatype.getId());
        }
    }

    public void setDatatypeReference(SortedSet<Datatype> datatypeDependencies) {
        for (Datatype datatype : datatypeDependencies) {
            this.datatypeReferencesIds.add(datatype.getId());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Datatype datatype) || !super.equals(o) || !Objects.equals(this.compilationUnitId, datatype.compilationUnitId) || !Objects.equals(this.parentDatatypeId, datatype.parentDatatypeId)) {
            return false;
        }
        if (!Objects.equals(this.extendedDataTypesIds, datatype.extendedDataTypesIds)) {
            return false;
        }
        if (!Objects.equals(this.implementedDataTypesIds, datatype.implementedDataTypesIds)) {
            return false;
        }
        return Objects.equals(this.datatypeReferencesIds, datatype.datatypeReferencesIds);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.compilationUnitId != null ? this.compilationUnitId.hashCode() : 0);
        result = 31 * result + (this.parentDatatypeId != null ? this.parentDatatypeId.hashCode() : 0);
        result = 31 * result + (this.extendedDataTypesIds != null ? this.extendedDataTypesIds.hashCode() : 0);
        result = 31 * result + (this.implementedDataTypesIds != null ? this.implementedDataTypesIds.hashCode() : 0);
        return 31 * result + (this.datatypeReferencesIds != null ? this.datatypeReferencesIds.hashCode() : 0);
    }
}
