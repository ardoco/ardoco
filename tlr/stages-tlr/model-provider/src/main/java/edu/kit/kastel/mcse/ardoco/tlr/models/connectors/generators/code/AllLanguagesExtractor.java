/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguages;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.java.JavaExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.shell.ShellExtractor;

@Deterministic
public final class AllLanguagesExtractor extends CodeExtractor {

    private final Map<String, CodeExtractor> codeExtractors;

    private CodeModel extractedModel = null;

    public AllLanguagesExtractor(CodeItemRepository codeItemRepository, String path) {
        super(codeItemRepository, path);
        this.codeExtractors = Map.of(ProgrammingLanguages.JAVA, new JavaExtractor(codeItemRepository, path), ProgrammingLanguages.SHELL, new ShellExtractor(
                codeItemRepository, path));
    }

    @Override
    public synchronized CodeModel extractModel() {
        if (this.extractedModel == null) {
            List<CodeModel> models = new ArrayList<>();
            for (CodeExtractor extractor : this.codeExtractors.values()) {
                var model = extractor.extractModel();
                models.add(model);
            }
            SortedSet<CodeItem> codeEndpoints = new TreeSet<>();
            for (CodeModel model : models) {
                codeEndpoints.addAll(model.getContent());
            }
            this.extractedModel = new CodeModel(this.codeItemRepository, codeEndpoints);
        }
        return this.extractedModel;
    }

}
