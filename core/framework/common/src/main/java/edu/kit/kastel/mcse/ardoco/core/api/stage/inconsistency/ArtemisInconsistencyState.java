package edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;

public interface ArtemisInconsistencyState
        extends IConfigurable { //TODO in the future: combine this with the InconsistencyState(s) since they are very similar (using a nice design)

    boolean addInconsistency(Inconsistency inconsistency);

    ImmutableList<Inconsistency> getInconsistencies();

    <T extends Inconsistency> ImmutableList<T> getInconsistenciesOfType(Class<T> type);
}
