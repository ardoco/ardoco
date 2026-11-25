/* Licensed under MIT 2022-2026. */
package edu.kit.kastel.mcse.ardoco.id.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ConfigurationTestBase;

/**
 * This test class deals with the configurations.
 *
 * @see AbstractConfigurable
 */
class ConfigurationTest extends ConfigurationTestBase {
    @Override
    protected void assertFalse(boolean result, String message) {
        Assertions.assertFalse(result, message);
    }

    @Override
    protected void fail(String message) {
        Assertions.fail(message);
    }

    /**
     * This test verifies that all configurable values are able to be configured. It also prints all configurable values
     * as they should be contained in a configuration file.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    @Override
    public void showCurrentConfiguration() throws Exception {
        super.showCurrentConfiguration();
    }

    @Test
    @Override
    public void testValidityOfConfigurableFields() {
        super.testValidityOfConfigurableFields();
    }

    /**
     * Lists the default configuration of all configurable classes via {@link ConfigurationHelper#getDefaultConfigurationOptions()}. Fails if the default
     * configuration cannot be returned.
     */
    @Test
    void listAllDefaultConfigurations() {
        var configs = Assertions.assertDoesNotThrow(ConfigurationHelper::getDefaultConfigurationOptions, "Default configuration cannot be returned");
        Assertions.assertFalse(configs.isEmpty(), "Default configuration shall not be empty");
        configs.forEachKeyValue((key, value) -> System.out.println(key + AbstractConfigurable.KEY_VALUE_CONNECTOR + value));
    }
}
