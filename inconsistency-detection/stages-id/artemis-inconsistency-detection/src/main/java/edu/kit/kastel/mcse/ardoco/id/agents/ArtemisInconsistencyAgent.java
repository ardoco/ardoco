package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public final class ArtemisInconsistencyAgent {

    private ArtemisInconsistencyAgent() {
        throw new IllegalStateException("Utility class");
    }

    public static List<PipelineAgent> createAgents(DataRepository dataRepository, List<ArtemisNerStrategy> strategies) {
        var agents = new ArrayList<PipelineAgent>();

        for (var strategy : strategies) {
            agents.add(new ArtemisMeatInconsistencyAgent(dataRepository, strategy));

            if (supportsTeamInconsistencies(strategy)) {
                agents.add(new ArtemisTeamInconsistencyAgent(dataRepository, strategy));
            }
        }

        return List.copyOf(agents);
    }

    private static boolean supportsTeamInconsistencies(ArtemisNerStrategy strategy) {
        return switch (strategy.getNamedEntityType()) {
            case NamedEntityType.COMPONENT -> true;
            case NamedEntityType.CLASS -> false;
            default -> false;
        };
    }
}
