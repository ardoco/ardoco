/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeAssembly;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
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

    @JsonGetter("content")
    protected List<String> getContentIds() {
        initialize();
        return content;
    }

    @Override
    public List<? extends CodeItem> getContent() {
        initialize();
        return codeItemRepository.getCodeItemsFromIds(content);
    }

    @Override
    public List<? extends CodeItem> getEndpoints() {
        List<CodeCompilationUnit> compilationUnits = new ArrayList<>();
        getContent().forEach(c -> compilationUnits.addAll(c.getAllCompilationUnits()));
        if (compilationUnits.isEmpty()) {
            List<CodeAssembly> codeAssemblies = new ArrayList<>();
            getContent().forEach(c -> codeAssemblies.addAll(c.getAllCodeAssemblies()));
            return codeAssemblies;
        }
        return compilationUnits;
    }

    /**
     * Returns all code packages directly or indirectly owned by this code model.
     *
     * @return all code packages of this code model
     */
    public List<? extends CodePackage> getAllPackages() {
        List<CodePackage> codePackages = new ArrayList<>();
        var lContent = getContent();
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
        if (initialized)
            return;
        this.codeItemRepository.init();
        initialized = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CodeModel codeModel))
            return false;
        if (!super.equals(o))
            return false;

        if (!Objects.equals(codeItemRepository, codeModel.codeItemRepository))
            return false;
        return Objects.equals(content, codeModel.content);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (codeItemRepository != null ? codeItemRepository.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
