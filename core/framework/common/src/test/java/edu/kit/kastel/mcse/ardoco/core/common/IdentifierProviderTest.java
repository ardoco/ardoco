/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;

class IdentifierProviderTest {
    @Test
    void architectureIdsDoNotCollideWithDefaultIdsAfterReset() {
        IdentifierProvider.reset();
        String defaultId = IdentifierProvider.createId();
        IdentifierProvider.reset();
        String architectureId = new ArchitectureMethod("m").getId();
        assertNotEquals(defaultId, architectureId);
    }
}
