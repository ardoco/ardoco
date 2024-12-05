/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ClassUnit")
public final class ClassUnit extends Datatype {

    private static final long serialVersionUID = 354013115794534271L;

    @JsonProperty
    private final List<String> content;

    @SuppressWarnings("unused")
    private ClassUnit() {
        // Jackson
        this.content = new ArrayList<>();
    }

    public ClassUnit(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, name);
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
    }

    @JsonGetter("content")
    protected List<String> getContentIds() {
        return this.content;
    }

    @Override
    public List<CodeItem> getContent() {
        return this.codeItemRepository.getCodeItemsFromIds(this.content);
    }

    @Override
    public List<Datatype> getAllDataTypes() {
        List<Datatype> result = new ArrayList<>();
        result.add(this);
        this.getContent().forEach(c -> result.addAll(c.getAllDataTypes()));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClassUnit classUnit) || !super.equals(o)) {
            return false;
        }
        return this.content.equals(classUnit.content);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return 31 * result + this.content.hashCode();
    }
}
