/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.configuration;

import java.util.SortedMap;

public interface IConfigurable {
    void applyConfiguration(SortedMap<String, String> additionalConfiguration);

    SortedMap<String, String> getLastAppliedConfiguration();
}
