/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.architecture.NoHashCodeEquals;

/**
 * Represents a code model. This includes compilation units and packages.
 */
@NoHashCodeEquals
public abstract sealed class CodeModel extends Model permits CodeModelWithCompilationUnitsAndPackages, CodeModelWithCompilationUnits {

    protected CodeItemRepository codeItemRepository;

    protected List<String> content;

    private boolean initialized;

    /**
     * Creates a new code model with the specified code item repository and content IDs.
     *
     * @param codeItemRepository the code item repository
     * @param content            list of code item IDs
     */
    protected CodeModel(CodeItemRepository codeItemRepository, List<String> content) {
        this.initialized = true;
        this.codeItemRepository = codeItemRepository;
        this.content = new ArrayList<>(content);
    }

    /**
     * Creates a new code model with the specified code item repository and content.
     *
     * @param codeItemRepository the code item repository
     * @param content            set of code items
     */
    protected CodeModel(CodeItemRepository codeItemRepository, SortedSet<? extends CodeItem> content) {
        this.initialized = true;
        this.codeItemRepository = codeItemRepository;
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
    }

    /**
     * Creates a DTO for this code model.
     *
     * @return code model DTO
     */
    public CodeModelDTO createCodeModelDto() {
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
     * @return list of all code packages
     */
    public List<CodePackage> getAllPackages() {
        List<CodePackage> codePackages = new ArrayList<>();
        for (var codeItem : this.getContent()) {
            var allPackages = codeItem.getAllPackages();
            for (CodePackage codePackage : allPackages) {
                if (!codePackages.contains(codePackage)) {
                    codePackages.add(codePackage);
                }
            }
        }
        codePackages.sort(Comparator.comparing(Entity::getName));
        return codePackages;
    }

    /**
     * Initializes the code model if not already initialized.
     */
    protected synchronized void initialize() {
        if (this.initialized) {
            return;
        }
        this.codeItemRepository.init();
        this.initialized = true;
    }
}
