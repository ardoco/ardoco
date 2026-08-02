package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation;

import java.io.File;
import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArdocoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisNer;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisPostprocessing;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.ArtemisPreprocessing;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

final class ArtemisEvaluationRunnerFactory {

    private ArtemisEvaluationRunnerFactory() {
        throw new IllegalAccessError("Utility class");
    }

    static ArdocoRunner createRunner(String projectName, File inputText, @Nullable ArchitectureConfiguration architectureConfiguration,
            @Nullable CodeConfiguration codeConfiguration, ImmutableSortedMap<String, String> additionalConfigs, File outputDirectory, LargeLanguageModel llm,
            List<ArtemisNerStrategy> strategies) {
        return new ArdocoRunner(projectName) {
            {
                Objects.requireNonNull(inputText);
                Objects.requireNonNull(additionalConfigs);
                Objects.requireNonNull(outputDirectory);
                Objects.requireNonNull(llm);
                Objects.requireNonNull(strategies);

                if ((architectureConfiguration != null && architectureConfiguration.metamodel() != null) || (codeConfiguration != null && codeConfiguration.metamodel() != null)) {
                    throw new IllegalArgumentException("Metamodel shall not be set in configurations. The evaluation defines the metamodels.");
                }

                var dataRepository = getArdoco().getDataRepository();
                String text = CommonUtilities.readInputText(inputText);
                if (text.isBlank()) {
                    throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
                }
                DataRepositoryHelper.putInputText(dataRepository, text);

                getArdoco().addPipelineStep(ArtemisPreprocessing.get(additionalConfigs, dataRepository,
                        architectureConfiguration != null ? architectureConfiguration.withMetamodel(Metamodel.ARCHITECTURE_WITH_COMPONENTS) : null,
                        codeConfiguration != null ? codeConfiguration.withMetamodel(Metamodel.CODE_WITH_COMPILATION_UNITS) : null));

                getArdoco().addPipelineStep(ArtemisNer.get(additionalConfigs, dataRepository, llm, strategies));
                getArdoco().addPipelineStep(ArtemisConnectionGenerator.get(additionalConfigs, dataRepository, strategies));
                getArdoco().addPipelineStep(ArtemisPostprocessing.get(additionalConfigs, dataRepository));

                setOutputDirectory(outputDirectory);
                isSetUp = true;
            }
        };
    }
}
