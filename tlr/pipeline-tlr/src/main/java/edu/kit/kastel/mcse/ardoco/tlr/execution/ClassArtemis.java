package edu.kit.kastel.mcse.ardoco.tlr.execution;

import java.util.Objects;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.strategies.ClassNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.strategies.NerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

/**
 * ArTEMiS runner for recovering trace links for class design decisions.
 * <p>
 * This runner configures the pipeline with a code model provider using the {@link Metamodel#CODE_WITH_COMPILATION_UNITS} metamodel.
 * </p>
 */
public class ClassArtemis extends AbstractArtemis {

    public ClassArtemis(String projectName) {
        super(projectName);
    }

    @Override
    protected void addModelProviderPipelineStep(ImmutableSortedMap<String, String> additionalConfigs, LargeLanguageModel llmForNer,
            @Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration) {
        CodeConfiguration codeConfigurationWithMetamodel = Objects.requireNonNull(codeConfiguration).withMetamodel(Metamodel.CODE_WITH_COMPILATION_UNITS);
        ModelProviderAgent modelProviderAgent = ModelProviderAgent.getModelProviderAgent(this.getArdoco().getDataRepository(), additionalConfigs, null,
                codeConfigurationWithMetamodel);
        this.getArdoco().addPipelineStep(modelProviderAgent);
    }

    /*TODO
    * Wenn wir eine weitere Artemis Art hinzufügen die die gleiche modelPRovider Piepline braucht kann man das auch so machen:
    * AbstractArtemis
    *   |
    *   +-- AbstractCodeArtemis -> definiert die pipeline
    *           |
    *           +-- ClassArtemis -> definiert die ner strategy
    *           |
    *           +-- FunctionArtemis
    * */

    @Override
    public NerStrategy getNerStrategy() {
        return new ClassNerStrategy();
    }

}
