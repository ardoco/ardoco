/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.configuration;

import static edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

@Deterministic
public class ConfigurationUtility {
    private ConfigurationUtility() {
        throw new IllegalStateException("Cannot be instantiated");
    }

    /**
     * {@return a map of configurations that will enable the specified agents}
     *
     * @param enabledAgents Set of agents that should be enabled
     */
    public static SortedMap<String, String> enableAgents(Class<? extends AbstractExecutionStage> stage, Set<Class<? extends PipelineAgent>> enabledAgents) {
        var map = new TreeMap<String, String>();
        for (var agent : enabledAgents) {
            var listString = List.of(agent.getSimpleName()).toString();
            map.put(stage.getSimpleName() + CLASS_ATTRIBUTE_CONNECTOR + "enabledAgents", listString.substring(1, listString.length() - 1));
        }
        return Collections.unmodifiableSortedMap(map);
    }

    /**
     * {@return a map of configurations that will enable the specified informants} Will also enable the necessary agents.
     *
     * @param enabledInformant Set of informants that should be enabled
     */
    public static SortedMap<String, String> enableInformants(AbstractExecutionStage stage, Set<Class<? extends Informant>> enabledInformant) {
        var map = new TreeMap<String, String>();
        var agentsToEnable = new LinkedHashSet<Class<? extends PipelineAgent>>();
        for (var informant : enabledInformant) {
            getInformantsMap(stage).entrySet().stream().filter(e -> e.getValue().contains(informant)).forEach(e -> {
                agentsToEnable.add(e.getKey());
                var listString = List.of(informant.getSimpleName()).toString();
                map.put(e.getKey().getSimpleName() + CLASS_ATTRIBUTE_CONNECTOR + "enabledInformants", listString.substring(1, listString.length() - 1));
            });
        }
        //Make sure we enable the agents which run the informants
        map.putAll(enableAgents(stage.getClass(), new LinkedHashSet<>(agentsToEnable)));
        return Collections.unmodifiableSortedMap(map);
    }

    public static Set<Class<? extends PipelineAgent>> getAgents(AbstractExecutionStage stage) {
        var agents = stage.getAgents();
        var clazzes = new ArrayList<Class<? extends PipelineAgent>>();
        for (var agent : agents) {
            var clazz = agent.getClass();
            clazzes.add(clazz);
        }
        return new LinkedHashSet<>(clazzes);
    }

    public static Set<Class<? extends Informant>> getInformants(PipelineAgent agent) {
        var informants = agent.getInformants();
        var clazzes = new ArrayList<Class<? extends Informant>>();
        for (var informant : informants) {
            var clazz = informant.getClass();
            clazzes.add(clazz);
        }
        return new LinkedHashSet<>(clazzes);
    }

    public static Map<Class<? extends PipelineAgent>, Set<Class<? extends Informant>>> getInformantsMap(AbstractExecutionStage stage) {
        var map = new LinkedHashMap<Class<? extends PipelineAgent>, Set<Class<? extends Informant>>>();
        var agents = stage.getAgents();
        for (var agent : agents) {
            map.put(agent.getClass(), getInformants(agent));
        }

        return new LinkedHashMap<>(map);
    }
}
