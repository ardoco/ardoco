package edu.kit.kastel.mcse.ardoco.tlr.execution;

import java.util.Objects;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants.ComponentNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants.NerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

/**
 * ArTEMiS runner for recovering trace links for component design decisions.
 * <p>
 * This runner configures the pipeline with an architecture model provider using the {@link Metamodel#ARCHITECTURE_WITH_COMPONENTS} metamodel.
 * </p>
 */
public class ComponentArtemis extends AbstractArtemis {

    public ComponentArtemis(String projectName) {
        super(projectName);
    }

    @Override
    protected void addModelProviderPipelineStep(ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llmForNer,
            @Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration) {
        ArchitectureConfiguration architectureConfigurationWithMetamodel = Objects.requireNonNull(architectureConfiguration)
                .withMetamodel(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        ModelProviderAgent modelProviderAgent = ModelProviderAgent.getModelProviderAgent(this.getArdoco().getDataRepository(), additionalConfigs,
                architectureConfigurationWithMetamodel, null);
        this.getArdoco().addPipelineStep(modelProviderAgent);
    }

    @Override
    public NerStrategy getNerStrategy() {
        return new ComponentNerStrategy();
    }

}
