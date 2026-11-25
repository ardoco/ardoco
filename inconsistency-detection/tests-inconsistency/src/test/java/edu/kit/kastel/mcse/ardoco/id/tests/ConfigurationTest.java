/* Licensed under MIT 2022-2026. */
package edu.kit.kastel.mcse.ardoco.id.tests;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.reflections.Reflections;

import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;
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
     * Lists the default configuration of all configurable classes. Fails for each class whose default configuration cannot be returned.
     */
    @Test
    void listAllDefaultConfigurations() {
        Map<String, String> configs = new TreeMap<>();
        var configurables = new Reflections("edu.kit.kastel.mcse.ardoco").getSubTypesOf(AbstractConfigurable.class)
                .stream()
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .filter(c -> !c.getPackageName().contains("tests"))
                .toList();

        Assertions.assertAll(configurables.stream().map(c -> (Executable) () -> processConfigurationOfClass(configs, c)));

        configs.forEach((key, value) -> System.out.println(key + AbstractConfigurable.KEY_VALUE_CONNECTOR + value));
    }
}
