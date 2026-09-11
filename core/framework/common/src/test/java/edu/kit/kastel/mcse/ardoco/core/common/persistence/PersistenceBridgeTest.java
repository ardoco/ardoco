/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PersistenceBridge} configuration side effects.
 */
class PersistenceBridgeTest {

    @AfterEach
    void resetFlags() {
        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "false", "PersistenceBridge::persistTextState", "false",
                        "PersistenceBridge::persistRecommendations", "false"));
    }

    @Test
    @DisplayName("persistRecommendations without persistTextState auto-enables TextState persistence")
    void persistRecommendationsImpliesPersistTextState() {
        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true", "PersistenceBridge::persistTextState", "false",
                        "PersistenceBridge::persistRecommendations", "true"));

        Assertions.assertTrue(PersistenceBridge.persistTextStateStatic);
        Assertions.assertTrue(PersistenceBridge.persistRecommendationsStatic);
    }

    @Test
    @DisplayName("persistTextState alone does not enable recommendations")
    void persistTextStateDoesNotEnableRecommendations() {
        PersistenceBridge.getInstance()
                .applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true", "PersistenceBridge::persistTextState", "true",
                        "PersistenceBridge::persistRecommendations", "false"));

        Assertions.assertTrue(PersistenceBridge.persistTextStateStatic);
        Assertions.assertFalse(PersistenceBridge.persistRecommendationsStatic);
    }
}
