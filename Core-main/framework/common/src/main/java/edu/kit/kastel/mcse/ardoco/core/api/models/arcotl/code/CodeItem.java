/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

/**
 * A code item of a code model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ //
        @JsonSubTypes.Type(value = CodeModule.class, name = "CodeModule"),//
        @JsonSubTypes.Type(value = ComputationalObject.class, name = "ComputationalObject"), //
        @JsonSubTypes.Type(value = Datatype.class, name = "Datatype") //
})
public abstract sealed class CodeItem extends Entity permits CodeModule, ComputationalObject, Datatype {

    @JsonIgnore
    protected CodeItemRepository codeItemRepository;
    protected String comment;

    CodeItem() {
        // Jackson
    }

    /**
     * Creates a new code item with the specified name.
     *
     * @param name the name of the code item to be created
     */
    protected CodeItem(CodeItemRepository codeItemRepository, String name) {
        super(name);
        this.codeItemRepository = Objects.requireNonNull(codeItemRepository);
        this.codeItemRepository.addCodeItem(this);
    }

    void registerCurrentCodeItemRepository(CodeItemRepository codeItemRepository) {
        this.codeItemRepository = codeItemRepository;
    }

    /**
     * Returns the content of this code item.
     *
     * @return the content of this code item
     */
    public List<CodeItem> getContent() {
        return new ArrayList<>();
    }

    public List<Datatype> getAllDataTypes() {
        return new ArrayList<>();
    }

    public SortedSet<CodeItem> getAllDataTypesAndSelf() {
        SortedSet<CodeItem> result = new TreeSet<>(getAllDataTypes());
        result.add(this);
        return result;
    }

    public SortedSet<ControlElement> getDeclaredMethods() {
        SortedSet<ControlElement> methods = new TreeSet<>();
        for (CodeItem codeItem : getContent()) {
            if (codeItem instanceof ControlElement codeMethod) {
                methods.add(codeMethod);
            }
        }
        return methods;
    }

    public SortedSet<CodeCompilationUnit> getAllCompilationUnits() {
        return new TreeSet<>();
    }

    public SortedSet<CodePackage> getAllPackages() {
        return new TreeSet<>();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
