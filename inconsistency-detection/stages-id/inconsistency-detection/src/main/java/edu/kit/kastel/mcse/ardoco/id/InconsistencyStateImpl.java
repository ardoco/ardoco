/* Licensed under MIT 2021-2026. */
package edu.kit.kastel.mcse.ardoco.id;

import java.io.Serial;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;

public class InconsistencyStateImpl extends AbstractState implements InconsistencyState {

    @Serial
    private static final long serialVersionUID = 4970092712795607439L;
    private MutableList<RecommendedInstance> recommendedInstances;
    private final MutableList<Inconsistency> inconsistencies;

    public InconsistencyStateImpl() {
        super();
        inconsistencies = Lists.mutable.empty();
        recommendedInstances = Lists.mutable.empty();
    }

    /**
     * Add an Inconsistency to this state. Does not add duplicate inconsistencies.
     *
     * @param inconsistency the inconsistency to add
     * @return true if added successfully
     */
    @Override
    public boolean addInconsistency(Inconsistency inconsistency) {
        if (!inconsistencies.contains(inconsistency)) {
            inconsistencies.add(inconsistency);
        }
        if (PersistenceBridge.isAvailable()) {
            Boolean saved = PersistenceBridge.callQuietly("addInconsistencies",
                    () -> PersistenceBridge.getHandler().addInconsistencies(List.of(inconsistency)), Boolean.FALSE);
            return Boolean.TRUE.equals(saved) || inconsistencies.contains(inconsistency);
        }
        return inconsistencies.contains(inconsistency);
    }

    /**
     * Returns a list of inconsistencies held by this state.
     * <p>
     * The in-memory list is authoritative during a run (same rule as for the connection and code traceability states): it also holds
     * inconsistencies whose database write failed. The database is only read when this state is empty (resume). The stored inconsistencies are not
     * scoped by metamodel, so on resume this state receives all stored inconsistencies.
     *
     * @return list of inconsistencies
     */
    @Override
    public ImmutableList<Inconsistency> getInconsistencies() {
        if (this.inconsistencies.isEmpty() && PersistenceBridge.isAvailable()) {
            var loadedInconsistencies = PersistenceBridge.callQuietly("getInconsistencies",
                    () -> PersistenceBridge.getHandler().getInconsistencies(), java.util.Collections.<Inconsistency>emptyList());
            this.inconsistencies.addAll(loadedInconsistencies);
        }
        return inconsistencies.toImmutable();
    }

    @Override
    public boolean addRecommendedInstance(RecommendedInstance recommendedInstance) {
        return this.recommendedInstances.add(recommendedInstance);
    }

    /**
     * @return the recommendedInstances
     */
    @Override
    public MutableList<RecommendedInstance> getRecommendedInstances() {
        return recommendedInstances;
    }

    /**
     * @param recommendedInstances the recommendedInstances to set
     */
    @Override
    public void setRecommendedInstances(ImmutableList<RecommendedInstance> recommendedInstances) {
        this.recommendedInstances = Lists.mutable.empty();
        this.recommendedInstances.addAll(recommendedInstances.castToList());
    }

}
