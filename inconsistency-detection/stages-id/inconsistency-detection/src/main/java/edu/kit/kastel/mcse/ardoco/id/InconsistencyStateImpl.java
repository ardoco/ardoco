/* Licensed under MIT 2021-2025. */
package edu.kit.kastel.mcse.ardoco.id;

import java.io.Serial;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.AbstractInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;

public class InconsistencyStateImpl extends AbstractInconsistencyState implements InconsistencyState {

    @Serial
    private static final long serialVersionUID = 4970092712795607439L;

    private MutableList<RecommendedInstance> recommendedInstances;

    public InconsistencyStateImpl() {
        super();
        this.recommendedInstances = Lists.mutable.empty();
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
        return this.recommendedInstances;
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
