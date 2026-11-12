/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.informants;

import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.types.ModelEntityAbsentFromTextInconsistency;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

/**
 * Informant that detects model entities without a corresponding named text entity (MEAT).
 * <p>
 * The informant compares the endpoints of the model configured by its Artemis NER strategy with the model entities referenced by existing Artemis trace links.
 * Model endpoints without a trace link are reported as {@link ModelEntityAbsentFromTextInconsistency}.
 * </p>
 */
@Deterministic
public class ArtemisMeatInconsistencyInformant extends ArtemisInconsistencyInformant {

    public ArtemisMeatInconsistencyInformant(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(strategy.getId() + "MeatInconsistencyInformant", dataRepository, strategy);
    }

    @Override
    public void process() {
        var modelStates = DataRepositoryHelper.getModelStatesData(getDataRepository());
        var model = modelStates.getModel(getStrategy().getMetamodel());
        var traceabilityState = DataRepositoryHelper.getArtemisConnectionStates(getDataRepository()).getState(getTarget());
        var inconsistencyState = DataRepositoryHelper.getArtemisInconsistencyStates(getDataRepository()).getState(getTarget());

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
