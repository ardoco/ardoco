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
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.java.JavaExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.shell.ShellExtractor;

@Deterministic
public final class AllLanguagesExtractor extends CodeExtractor {

    private final Map<ProgrammingLanguage, CodeExtractor> codeExtractors;

    private CodeModel extractedModel = null;

    public AllLanguagesExtractor(CodeItemRepository codeItemRepository, String path) {
        super(codeItemRepository, path);
        codeExtractors = Map.of(ProgrammingLanguage.JAVA, new JavaExtractor(codeItemRepository, path), ProgrammingLanguage.SHELL, new ShellExtractor(
                codeItemRepository, path));
    }

    @Override
    public synchronized CodeModel extractModel() {
        if (extractedModel == null) {
            List<CodeModel> models = new ArrayList<>();
            for (CodeExtractor extractor : codeExtractors.values()) {
                var model = extractor.extractModel();
                models.add(model);
            }
            SortedSet<CodeItem> codeEndpoints = new TreeSet<>();
            for (CodeModel model : models) {
                codeEndpoints.addAll(model.getContent());
            }
            this.extractedModel = new CodeModel(codeItemRepository, codeEndpoints);
        }
        return extractedModel;
    }

}
