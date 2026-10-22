/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.Prompt;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.TwoPartPrompt;

@Deterministic
public class ComponentArtemisNerStrategy implements ArtemisNerStrategy {
    private static final String TASK_PROMPT_RESOURCE = "component-task-prompt.txt";
    private static final String FORMATTING_PROMPT_RESOURCE = "component-formatting-prompt.txt";

    @Override
    public Prompt createPrompt(DataRepository dataRepository) {
        String taskPrompt = readPromptResource(TASK_PROMPT_RESOURCE);
        String formattingPrompt = readPromptResource(FORMATTING_PROMPT_RESOURCE);

        taskPrompt += getPossibleEntities(dataRepository);
        return new TwoPartPrompt(taskPrompt, formattingPrompt);
    }

    protected StringBuilder getPossibleEntities(DataRepository dataRepository) {
        Map<NamedEntityType, Set<String>> possibleEntities = new EnumMap<>(NamedEntityType.class);
        possibleEntities.put(NamedEntityType.COMPONENT, new TreeSet<>());

        var modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        var model = modelStatesData.getModel(getMetamodel());
        for (var endpoint : model.getEndpoints()) {
            String endpointName = endpoint.getName();
            possibleEntities.get(NamedEntityType.COMPONENT).add(Objects.requireNonNull(endpointName));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n\nAs support, here is a list of entities that could be mentioned in the text:\n");
        for (Map.Entry<NamedEntityType, Set<String>> entry : possibleEntities.entrySet()) {
            NamedEntityType type = entry.getKey();
            Set<String> names = entry.getValue();
            if (names.isEmpty()) {
                continue;
            }
            sb.append(type.toString().toLowerCase()).append(" entities: ");
            sb.append(String.join(", ", names));
            sb.append("\n");
        }
        sb.append("\n");

        return sb;
    }

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.ARCHITECTURE_WITH_COMPONENTS;
    }

    @Override
    public NamedEntityType getNamedEntityType() {
        return NamedEntityType.COMPONENT;
    }
}
