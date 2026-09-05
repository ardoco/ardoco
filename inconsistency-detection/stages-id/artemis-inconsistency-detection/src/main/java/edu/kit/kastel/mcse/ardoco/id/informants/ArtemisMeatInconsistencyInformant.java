package edu.kit.kastel.mcse.ardoco.id.informants;

import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.types.ModelEntityAbsentFromTextInconsistency;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ArtemisMeatInconsistencyInformant extends ArtemisInconsistencyInformant {

    public ArtemisMeatInconsistencyInformant(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(strategy.getId() + "MeatInconsistencyInformant", dataRepository, strategy);
    }

    @Override
    public void process() {
        var modelStates = DataRepositoryHelper.getModelStatesData(getDataRepository());
        var model = modelStates.getModel(getStrategy().getMetamodel());
        var traceabilityState = getArtemisConnectionState();
        var inconsistencyState = getArtemisInconsistencyState();

        Set<String> linkedModelEntityIds = traceabilityState.getTraceLinks()
                .stream()
                .map(TraceLink::getSecondEndpoint)
                .map(ModelEntity::getId)
                .collect(Collectors.toSet());

        for (var endpoint : model.getEndpoints()) {
            if (!linkedModelEntityIds.contains(endpoint.getId())) {
                inconsistencyState.addInconsistency(new ModelEntityAbsentFromTextInconsistency(endpoint));
            }
        }
    }

}
