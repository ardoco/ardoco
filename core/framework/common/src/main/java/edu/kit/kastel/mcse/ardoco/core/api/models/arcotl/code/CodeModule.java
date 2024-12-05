/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ //
        @JsonSubTypes.Type(value = CodeAssembly.class, name = "CodeAssembly"),//
        @JsonSubTypes.Type(value = CodeCompilationUnit.class, name = "CodeCompilationUnit"), //
        @JsonSubTypes.Type(value = CodePackage.class, name = "CodePackage"), //
})
@JsonTypeName("CodeModule")
public sealed class CodeModule extends CodeItem permits CodeAssembly, CodeCompilationUnit, CodePackage {

    private static final long serialVersionUID = -7941299662945801101L;

    @JsonProperty
    private String parentId;
    @JsonProperty
    private List<String> content;

    CodeModule() {
        // Jackson
    }

    public CodeModule(CodeItemRepository codeItemRepository, String name, SortedSet<? extends CodeItem> content) {
        super(codeItemRepository, name);
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
        this.parentId = null;
    }

    @JsonGetter("content")
    protected List<String> getContentIds() {
        return this.content;
    }

    @Override
    public List<CodeItem> getContent() {
        return this.codeItemRepository.getCodeItemsFromIds(this.content);
    }

    public void setContent(List<? extends CodeItem> content) {
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
    }

    public void addContent(CodeItem content) {
        this.content.add(content.getId());
    }

    public void addContent(List<? extends CodeItem> content) {
        this.content.addAll(content.stream().map(CodeItem::getId).toList());
    }

    public CodeModule getParent() {
        CodeItem codeItem = this.codeItemRepository.getCodeItem(this.parentId);
        if (codeItem instanceof CodeModule codeModule) {
            return codeModule;
        }
        return null;
    }

    public boolean hasParent() {
        return this.getParent() != null;
    }

    public void setParent(CodeModule parent) {
        this.parentId = parent.getId();
        if (!this.codeItemRepository.containsCodeItem(this.parentId)) {
            this.codeItemRepository.addCodeItem(parent);
        }
    }

    @Override
    public SortedSet<CodeCompilationUnit> getAllCompilationUnits() {
        SortedSet<CodeCompilationUnit> result = new TreeSet<>();
        this.getContent().forEach(c -> result.addAll(c.getAllCompilationUnits()));
        return result;
    }

    @Override
    public SortedSet<CodePackage> getAllPackages() {
        SortedSet<CodePackage> result = new TreeSet<>();
        this.getContent().forEach(c -> result.addAll(c.getAllPackages()));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CodeModule that) || !super.equals(o) || !Objects.equals(this.parentId, that.parentId)) {
            return false;
        }
        return Objects.equals(this.content, that.content);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.parentId != null ? this.parentId.hashCode() : 0);
        return 31 * result + (this.content != null ? this.content.hashCode() : 0);
    }
}
