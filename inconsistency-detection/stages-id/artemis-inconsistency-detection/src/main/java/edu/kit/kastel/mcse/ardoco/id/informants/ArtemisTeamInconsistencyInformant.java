/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.informants;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.types.TextEntityAbsentFromModelInconsistency;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

/**
 * Informant that detects named text entities without a corresponding model entity (TEAM).
 * <p>
 * The informant evaluates the unlinked named entities in the Artemis connection state of its configured target and reports each occurrence as a
 * {@link TextEntityAbsentFromModelInconsistency}.
 * </p>
 */
public class ArtemisTeamInconsistencyInformant extends ArtemisInconsistencyInformant {
    public static final double DEFAULT_PROBABILITY = 0.92;

    public ArtemisTeamInconsistencyInformant(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(strategy.getId() + "TeamInconsistencyInformant", dataRepository, strategy);
    }

    @Override
    public void process() {
        var result = new ArdocoResult(getDataRepository());
        var traceabilityState = DataRepositoryHelper.getArtemisConnectionStates(getDataRepository()).getState(getTarget());
        var inconsistencyState = DataRepositoryHelper.getArtemisInconsistencyStates(getDataRepository()).getState(getTarget());

        for (var namedEntity : traceabilityState.getUnlinkedNamedEntities()) {
            for (var occurrence : namedEntity.getOccurrences()) {
                var sentenceNumber = occurrence.getSentenceNumber();
                inconsistencyState.addInconsistency(new TextEntityAbsentFromModelInconsistency(namedEntity.getName(), sentenceNumber, DEFAULT_PROBABILITY,
                        null));
            }
        }
    }

}
