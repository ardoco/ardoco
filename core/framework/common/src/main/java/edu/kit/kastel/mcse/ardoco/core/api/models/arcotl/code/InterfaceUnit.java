/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("InterfaceUnit")
public final class InterfaceUnit extends Datatype {

    private static final long serialVersionUID = 7746781256077022392L;

    @JsonProperty
    private List<String> content;

    @SuppressWarnings("unused")
    private InterfaceUnit() {
        // Jackson
    }

    public InterfaceUnit(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content) {
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
        if (!(o instanceof InterfaceUnit that) || !super.equals(o)) {
            return false;
        }

        return Objects.equals(this.content, that.content);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return 31 * result + (this.content != null ? this.content.hashCode() : 0);
    }
}
