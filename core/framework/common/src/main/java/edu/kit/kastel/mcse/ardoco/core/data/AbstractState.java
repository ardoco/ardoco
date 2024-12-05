/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.data;

import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;

/**
 * This abstract class represents a state that can be saved to the {@link DataRepository} as {@link PipelineStepData}.
 */
public abstract class AbstractState extends AbstractConfigurable implements PipelineStepData {

    private static final long serialVersionUID = -3318799425973820663L;

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> map) {
        // empty
    }
}
