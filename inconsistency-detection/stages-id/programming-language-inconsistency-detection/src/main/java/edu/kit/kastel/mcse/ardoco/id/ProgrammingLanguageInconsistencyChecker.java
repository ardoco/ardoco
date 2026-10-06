package edu.kit.kastel.mcse.ardoco.id;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.agents.CodeDirectoryProgrammingLanguageInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.id.agents.CodeModelProgrammingLanguageInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.id.states.ProgrammingLanguageInconsistencyState;
import edu.kit.kastel.mcse.ardoco.id.states.ProgrammingLanguageInconsistencyStateImpl;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ProgrammingLanguageInconsistencyChecker extends AbstractExecutionStage {

    private ProgrammingLanguageInconsistencyChecker(DataRepository dataRepository, PipelineAgent agent) {
        super(List.of(agent), "ProgrammingLanguageInconsistencyChecker", dataRepository);
    }

    /**
     * Creates and initializes a ProgrammingLanguageInconsistencyChecker for analyzing inconsistencies related to programming languages using a specified code
     * directory.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @param nerStrategy       the Named Entity Recognition strategy to be used
     * @param codeProjectPath   the path to the code directory to be analyzed
     * @return an instance of ProgrammingLanguageInconsistencyChecker configured for the given directory
     */
    public static ProgrammingLanguageInconsistencyChecker getForCodeDirectory(ImmutableSortedMap<String, String> additionalConfigs,
            DataRepository dataRepository, ArtemisNerStrategy nerStrategy, String codeProjectPath) {
        var agent = new CodeDirectoryProgrammingLanguageInconsistencyAgent(dataRepository, nerStrategy, codeProjectPath);
        var programmingLanguageInconsistencyChecker = new ProgrammingLanguageInconsistencyChecker(dataRepository, agent);
        programmingLanguageInconsistencyChecker.applyConfiguration(additionalConfigs);
        return programmingLanguageInconsistencyChecker;
    }

    /**
     * Creates and initializes a ProgrammingLanguageInconsistencyChecker for analyzing inconsistencies related to programming languages using a specified code
     * model.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @param nerStrategy       the Named Entity Recognition strategy to be used
     * @return an instance of ProgrammingLanguageInconsistencyChecker configured for the given directory
     */
    public static ProgrammingLanguageInconsistencyChecker getForCodeModel(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository,
            ArtemisNerStrategy nerStrategy) {
        var agent = new CodeModelProgrammingLanguageInconsistencyAgent(dataRepository, nerStrategy);
        var programmingLanguageInconsistencyChecker = new ProgrammingLanguageInconsistencyChecker(dataRepository, agent);
        programmingLanguageInconsistencyChecker.applyConfiguration(additionalConfigs);
        return programmingLanguageInconsistencyChecker;
    }

    @Override
    protected void initializeState() {
        var state = new ProgrammingLanguageInconsistencyStateImpl();
        dataRepository.addData(ProgrammingLanguageInconsistencyState.ID, state);
    }
}
