/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CoarseGrainedCodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.FineGrainedCodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguages;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.java.JavaExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.shell.ShellExtractor;

@Deterministic
public final class AllLanguagesExtractor extends CodeExtractor {

    private final Map<String, CodeExtractor> codeExtractors;
    private CodeModel codeModel;

    public AllLanguagesExtractor(CodeItemRepository codeItemRepository, String path, Metamodel metamodelToExtract) {
        super(codeItemRepository, path, metamodelToExtract);
        this.codeExtractors = Map.of(ProgrammingLanguages.JAVA, new JavaExtractor(codeItemRepository, path, metamodelToExtract), ProgrammingLanguages.SHELL,
                new ShellExtractor(codeItemRepository, path, metamodelToExtract));
    }

    @Override
    public synchronized CodeModel extractModel() {

        if (this.codeModel == null) {
            List<CodeModel> models = new ArrayList<>();
            for (CodeExtractor extractor : this.codeExtractors.values()) {
                var model = extractor.extractModel();
                models.add(model);
            }
            SortedSet<CodeItem> codeEndpoints = new TreeSet<>();
            for (CodeModel model : models) {
                codeEndpoints.addAll(model.getContent());
            }

            switch (this.metamodelToExtract) {
            case CODE_AS_ARCHITECTURE -> this.codeModel = new CoarseGrainedCodeModel(this.codeItemRepository, codeEndpoints);
            case CODE -> this.codeModel = new FineGrainedCodeModel(this.codeItemRepository, codeEndpoints);
            default -> throw new IllegalStateException("This extractor does not support this metamodel");
            }
        }
        return this.codeModel;
    }
}
