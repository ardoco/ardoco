/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;

/**
 * A code model that is a CMTL instance.
 */
public abstract sealed class CodeModel extends Model permits CoarseGrainedCodeModel, FineGrainedCodeModel {

    protected CodeItemRepository codeItemRepository;

    protected List<String> content;

    private boolean initialized;

    protected CodeModel(CodeItemRepository codeItemRepository, List<String> content) {
        this.initialized = true;
        this.codeItemRepository = codeItemRepository;
        this.content = new ArrayList<>(content);
    }

    /**
     * Creates a new code model that is a CMTL instance. The model has the specified code items as content.
     *
     * @param content the content of the code model
     */
    protected CodeModel(CodeItemRepository codeItemRepository, SortedSet<? extends CodeItem> content) {
        this.initialized = true;
        this.codeItemRepository = codeItemRepository;
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
    }

    public CodeModelDTO createCodeModelDTO() {
        return new CodeModelDTO(codeItemRepository, getContentIds());
    }

    private List<String> getContentIds() {
        this.initialize();
        return this.content;
    }

    @Override
    public abstract List<? extends CodeItem> getContent();

    @Override
    public abstract List<? extends CodeItem> getEndpoints();

    /**
     * Returns all code packages directly or indirectly owned by this code model.
     *
     * @return all code packages of this code model
     */
    public List<? extends CodePackage> getAllPackages() {
        List<CodePackage> codePackages = new ArrayList<>();
        var lContent = this.getContent();
        for (var c : lContent) {
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

    protected synchronized void initialize() {
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
