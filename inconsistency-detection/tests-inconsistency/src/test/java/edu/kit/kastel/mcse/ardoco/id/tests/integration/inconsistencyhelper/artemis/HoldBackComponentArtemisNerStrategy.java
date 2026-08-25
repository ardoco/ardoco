package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper.artemis;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ComponentArtemisNerStrategy;

public class HoldBackComponentArtemisNerStrategy extends ComponentArtemisNerStrategy {
    private final String excludedModelElementId;

    public HoldBackComponentArtemisNerStrategy(String excludedModelElementId) {
        this.excludedModelElementId = Objects.requireNonNull(excludedModelElementId);
    }

    @Override
    protected StringBuilder getPossibleEntities(DataRepository dataRepository) {
        Map<NamedEntityType, Set<String>> possibleEntities = new EnumMap<>(NamedEntityType.class);
        possibleEntities.put(NamedEntityType.COMPONENT, new TreeSet<>());

        var modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        var model = modelStatesData.getModel(getMetamodel());
        for (var endpoint : model.getEndpoints()) {
            if (excludedModelElementId.equals(endpoint.getId())) { //this is the only difference to ComponentArtemisNerStrategy superclass
                continue;
            }
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

}

