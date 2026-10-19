package edu.kit.kastel.mcse.ardoco.id;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.agents.CodeDirectoryArchitecturalStylePatternInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.id.agents.CodeModelArchitecturalStylePatternInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.id.states.ArchitecturalStylePatternInconsistencyState;
import edu.kit.kastel.mcse.ardoco.id.states.ArchitecturalStylePatternInconsistencyStateImpl;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ArchitecturalStylePatternInconsistencyChecker extends AbstractExecutionStage {

    private ArchitecturalStylePatternInconsistencyChecker(DataRepository dataRepository, PipelineAgent agent) {
        super(List.of(agent), "ArchitecturalStylePatternInconsistencyChecker", dataRepository);
    }

    /**
     * Creates and configures an instance of {@link ArchitecturalStylePatternInconsistencyChecker} for analyzing a code directory.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @param nerStrategy       the named entity recognition strategy to be used for processing architectural elements in the code directory
     * @param codeProjectPath   the path to the code directory that will be analyzed for architectural style pattern inconsistencies
     * @return a configured instance of {@link ArchitecturalStylePatternInconsistencyChecker} with an associated agent for analyzing the specified code
     * directory
     */
    public static ArchitecturalStylePatternInconsistencyChecker getForCodeDirectory(ImmutableSortedMap<String, String> additionalConfigs,
            DataRepository dataRepository, ArtemisNerStrategy nerStrategy, String codeProjectPath) {
        var agent = new CodeDirectoryArchitecturalStylePatternInconsistencyAgent(dataRepository, nerStrategy, codeProjectPath);
        var checker = new ArchitecturalStylePatternInconsistencyChecker(dataRepository, agent);
        checker.applyConfiguration(additionalConfigs);
        return checker;
    }

    /**
     * Creates and configures an instance of {@link ArchitecturalStylePatternInconsistencyChecker} for analyzing a code model.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @param nerStrategy       the named entity recognition strategy to be used for processing architectural elements in the code model
     * @return a configured instance of {@link ArchitecturalStylePatternInconsistencyChecker} with an associated agent for analyzing the specified code model
     */
    public static ArchitecturalStylePatternInconsistencyChecker getForCodeModel(ImmutableSortedMap<String, String> additionalConfigs,
            DataRepository dataRepository, ArtemisNerStrategy nerStrategy) {
        var agent = new CodeModelArchitecturalStylePatternInconsistencyAgent(dataRepository, nerStrategy);
        var checker = new ArchitecturalStylePatternInconsistencyChecker(dataRepository, agent);
        checker.applyConfiguration(additionalConfigs);
        return checker;
    }

    @Override
    protected void initializeState() {
        var state = new ArchitecturalStylePatternInconsistencyStateImpl();
        dataRepository.addData(ArchitecturalStylePatternInconsistencyState.ID, state);
    }
}
