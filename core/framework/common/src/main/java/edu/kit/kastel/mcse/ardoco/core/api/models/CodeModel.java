/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeFile;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.architecture.NoHashCodeEquals;

/**
 * Represents a code model. This includes compilation units and packages.
 */
@NoHashCodeEquals
public abstract sealed class CodeModel extends Model permits CodeModelWithCompilationUnitsAndPackages, CodeModelWithCompilationUnits {

    protected CodeItemRepository codeItemRepository;

    protected List<String> content;

    protected List<CodeFile> codeFiles;

    private boolean initialized;

    /**
     * Creates a new code model with the specified code item repository and content IDs.
     *
     * @param codeItemRepository the code item repository
     * @param content            list of code item IDs
     */
    protected CodeModel(CodeItemRepository codeItemRepository, List<String> content) {
        this(codeItemRepository, content, List.of());
    }

    /**
     * Creates a new code model with the specified code item repository, content IDs, and code files.
     *
     * @param codeItemRepository the code item repository
     * @param content            list of code item IDs
     * @param codeFiles          list of code files
     */
    protected CodeModel(CodeItemRepository codeItemRepository, List<String> content, List<CodeFile> codeFiles) {
        this.initialized = true;
        this.codeItemRepository = codeItemRepository;
        this.content = new ArrayList<>(content);
        this.codeFiles = new ArrayList<>(codeFiles);
    }

    /**
     * Creates a new code model with the specified id, code item repository, and content IDs.
     *
     * @param id                 the model id
     * @param codeItemRepository the code item repository
     * @param content            list of code item IDs
     */
    protected CodeModel(String id, CodeItemRepository codeItemRepository, List<String> content) {
        this(id, codeItemRepository, content, List.of());
    }

    /**
     * Creates a new code model with the specified id, code item repository, content IDs, and code files.
     *
     * @param id                 the model id
     * @param codeItemRepository the code item repository
     * @param content            list of code item IDs
     * @param codeFiles          list of code files
     */
    protected CodeModel(String id, CodeItemRepository codeItemRepository, List<String> content, List<CodeFile> codeFiles) {
        super(id);
        this.initialized = true;
        this.codeItemRepository = codeItemRepository;
        this.content = new ArrayList<>(content);
        this.codeFiles = new ArrayList<>(codeFiles);
    }

    /**
     * Creates a new code model with the specified code item repository and content.
     *
     * @param codeItemRepository the code item repository
     * @param content            set of code items
     */
    protected CodeModel(CodeItemRepository codeItemRepository, SortedSet<? extends CodeItem> content) {
        this(codeItemRepository, content, List.of());
    }

    /**
     * Creates a new code model with the specified code item repository, content, and code files.
     *
     * @param codeItemRepository the code item repository
     * @param content            set of code items
     * @param codeFiles          list of code files
     */
    protected CodeModel(CodeItemRepository codeItemRepository, SortedSet<? extends CodeItem> content, List<CodeFile> codeFiles) {
        this.initialized = true;
        this.codeItemRepository = codeItemRepository;
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
        this.codeFiles = new ArrayList<>(codeFiles);
    }

    /**
     * Returns a list of all classes ({@link ClassUnit}) present in the code model.
     *
     * @return list of all class units present in the code model
     */
    public List<ClassUnit> getClasses() {
        return this.codeItemRepository.getAllClassUnits();
    }

    /**
     * Returns the files contained in this code model.
     *
     * @return list of code files
     */
    public List<CodeFile> getCodeFiles() {
        this.initialize();
        return new ArrayList<>(this.codeFiles);
    }

    /**
     * Creates a DTO for this code model.
     *
     * @return code model DTO
     */
    public CodeModelDto createCodeModelDto() {
        return new CodeModelDto(getId(), codeItemRepository, getContentIds(), getCodeFiles());
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

    /**
     * Data transfer object for the code model. Contains a {@link CodeItemRepository}, a list of content identifiers, and all code files.
     *
     * @param id                 the model id
     * @param codeItemRepository the repository of code items
     * @param content            the list of content identifiers
     * @param codeFiles          the list of code files
     */
    public record CodeModelDto(@JsonProperty String id, @JsonProperty CodeItemRepository codeItemRepository, @JsonProperty List<String> content,
                               @JsonProperty List<CodeFile> codeFiles) {
        /**
         * Returns the code item repository, initializing it if necessary.
         *
         * @return the code item repository
         */
        @Override
        public CodeItemRepository codeItemRepository() {
            codeItemRepository.init();
            return codeItemRepository;
        }
    }
}
