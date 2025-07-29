/* Licensed under MIT 2022-2025. */
package edu.kit.kastel.mcse.ardoco.core.configuration;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

/**
 * Interface for components that can be configured with additional configuration parameters.
 */
public interface IConfigurable {
    /**
     * Applies the given configuration to this component.
     *
     * @param additionalConfiguration the configuration to apply
     */
    void applyConfiguration(ImmutableSortedMap<String, String> additionalConfiguration);

    /**
     * Returns the last applied configuration.
     *
     * @return the last applied configuration as an unmodifiable map
     */
    ImmutableSortedMap<String, String> getLastAppliedConfiguration();
}
