package edu.kit.kastel.mcse.ardoco.id.informants;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.id.types.TextEntityAbsentFromModelInconsistency;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ArtemisTeamInconsistencyInformant extends ArtemisInconsistencyInformant {
    public static final double DEFAULT_PROBABILITY = 0.92;

    public ArtemisTeamInconsistencyInformant(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(strategy.getId() + "TeamInconsistencyInformant", dataRepository, strategy);
    }

    @Override
    public void process() {
        var traceabilityState = getArtemisConnectionState();
        var inconsistencyState = getArtemisInconsistencyState();

        for (var namedEntity : traceabilityState.getUnlinkedNamedEntities()) {
            for (var occurrence : namedEntity.getOccurrences()) {
                var sentenceNumber = occurrence.getSentenceNumber();
                inconsistencyState.addInconsistency(
                        new TextEntityAbsentFromModelInconsistency(namedEntity.getName(), sentenceNumber, DEFAULT_PROBABILITY, null));
            }
        }
    }

}
