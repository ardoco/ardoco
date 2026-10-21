/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.informants;

import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.types.ModelEntityAbsentFromTextInconsistency;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

@Deterministic
public class ArtemisMeatInconsistencyInformant extends ArtemisInconsistencyInformant {

    public ArtemisMeatInconsistencyInformant(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(strategy.getId() + "MeatInconsistencyInformant", dataRepository, strategy);
    }

    @Override
    public void process() {
        var modelStates = DataRepositoryHelper.getModelStatesData(getDataRepository());
        var model = modelStates.getModel(getStrategy().getMetamodel());
        var result = new ArdocoResult(getDataRepository());
        var traceabilityState = result.getArtemisConnectionState(getTarget());
        var inconsistencyState = result.getArtemisInconsistencyState(getTarget());

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
