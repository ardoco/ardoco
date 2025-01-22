/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;

/**
 * A code model that is a CMTL instance.
 */
public final class CodeModel extends Model {

    @JsonProperty
    private CodeItemRepository codeItemRepository;

    @JsonProperty
    private List<String> content;

    @JsonIgnore
    private boolean initialized;

    @SuppressWarnings("unused")
    private CodeModel() {
        // Jackson
        this.initialized = false;
    }

    /**
     * Creates a new code model that is a CMTL instance. The model has the specified code items as content.
     *
     * @param content the content of the code model
     */
    public CodeModel(CodeItemRepository codeItemRepository, SortedSet<? extends CodeItem> content) {
        this.initialized = true;
        this.codeItemRepository = codeItemRepository;
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
    }

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.CODE;
    }

    @Override
    public SortedSet<String> getTypeIdentifiers() {
        SortedSet<String> identifiers = new TreeSet<>();

        for (var codeItem : this.getContent()) {
            var type = codeItem.getType();
            if (type.isPresent()) {
                identifiers.add(type.get());
            }
        }
        return identifiers;
    }

    @JsonGetter("content")
    private List<String> getContentIds() {
        this.initialize();
        return this.content;
    }

    @Override
    public List<? extends CodeItem> getContent() {
        this.initialize();
        return this.codeItemRepository.getCodeItemsFromIds(this.content);
    }

    @Override
    public List<? extends ModelEntity> getEndpoints() {

        List<ModelEntity> entities = new ArrayList<>();
        this.getContent().forEach(c -> entities.addAll(c.getAllCompilationUnits()));

        return entities;
    }

    /**
     * Returns all code packages directly or indirectly owned by this code model.
     *
     * @return all code packages of this code model
     */
    public List<? extends CodePackage> getAllPackages() {
        List<CodePackage> codePackages = new ArrayList<>();
        var lContent = this.getContent();
        for (CodeItem c : lContent) {
            var allPackages = c.getAllPackages();
            for (CodePackage cp : allPackages) {
                if (!codePackages.contains(cp)) {
                    codePackages.add(cp);
                }
            }
        }
        codePackages.sort(Comparator.comparing(Entity::getName));
        return codePackages;
    }

    private synchronized void initialize() {
        if (this.initialized) {
            return;
        }
        this.codeItemRepository.init();
        this.initialized = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CodeModel codeModel) || !super.equals(o) || !Objects.equals(this.codeItemRepository, codeModel.codeItemRepository)) {
            return false;
        }
        return Objects.equals(this.content, codeModel.content);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.codeItemRepository != null ? this.codeItemRepository.hashCode() : 0);
        return 31 * result + (this.content != null ? this.content.hashCode() : 0);
    }
}
