/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnitsAndPackages;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeFile;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp.CppExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3.Python3Extractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.java.JavaExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.shell.ShellExtractor;

@Deterministic
public final class AllLanguagesExtractor extends CodeExtractor {

    private final Map<ProgrammingLanguage, CodeExtractor> codeExtractors;
    private @Nullable CodeModel codeModel;

    @Override
    public String getIdPrefix() {
        return "ale";
    }

    public AllLanguagesExtractor(CodeItemRepository codeItemRepository, String path, Metamodel metamodelToExtract) {
        super(codeItemRepository, path, metamodelToExtract);
        this.codeExtractors = Map.of(ProgrammingLanguage.JAVA, new JavaExtractor(codeItemRepository, path, metamodelToExtract), //
                ProgrammingLanguage.SHELL, new ShellExtractor(codeItemRepository, path, metamodelToExtract), //
                ProgrammingLanguage.PYTHON, new Python3Extractor(codeItemRepository, path), //
                ProgrammingLanguage.CPP, new CppExtractor(codeItemRepository, path));
    }

    @Override
    public synchronized CodeModel extractModel() {

        CodeModel model = this.codeModel;
        if (model == null) {
            List<CodeModel> models = new ArrayList<>();
            for (CodeExtractor extractor : this.codeExtractors.values()) {
                models.add(extractor.extractModel());
            }
            SortedSet<CodeItem> codeEndpoints = new TreeSet<>();
            for (CodeModel extractedModel : models) {
                codeEndpoints.addAll(extractedModel.getContent());
            }

            List<CodeFile> codeFiles = extractCodeFiles(codeEndpoints);

            switch (this.metamodelToExtract) {
                case CODE_WITH_COMPILATION_UNITS_AND_PACKAGES -> model = new CodeModelWithCompilationUnitsAndPackages(this.codeItemRepository, codeEndpoints,
                        codeFiles);
                case CODE_WITH_COMPILATION_UNITS -> model = new CodeModelWithCompilationUnits(this.codeItemRepository, codeEndpoints, codeFiles);
                default -> throw new IllegalStateException("This extractor does not support this metamodel");
            }
            this.codeModel = model;
        }
        return model;
    }

}
