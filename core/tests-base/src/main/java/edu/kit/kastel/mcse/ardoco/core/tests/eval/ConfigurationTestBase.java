/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.ConfigurationInstantiatorUtils;

/**
 * This test class deals with the configurations.
 *
 * @see AbstractConfigurable
 */
@SuppressWarnings({ "java:S106", "java:S3011" })
public abstract class ConfigurationTestBase {

    private static final String ARDOCO = "edu.kit.kastel.mcse.ardoco";

    protected abstract void assertFalse(boolean result, String message);

    protected abstract void fail(String message);

    /**
     * This test verifies that all configurable values are able to be configured. It also prints all configurable values
     * as they should be contained in a configuration file.
     *
     * @throws Exception if anything goes wrong
     */
    protected void showCurrentConfiguration() throws Exception {
        Map<String, String> configs = new TreeMap<>();
        var reflectAccess = new Reflections(ARDOCO);
        var classesThatMayBeConfigured = reflectAccess.getSubTypesOf(AbstractConfigurable.class)
                .stream()
                .filter(c -> c.getPackageName().startsWith(ARDOCO))
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .filter(c -> !c.getPackageName().contains("tests"))
                .toList();
        for (var clazz : classesThatMayBeConfigured) {
            processConfigurationOfClass(configs, clazz);
        }
        assertFalse(configs.isEmpty(), "Configuration shall not be empty");

        System.out.println("-".repeat(50));
        System.out.println("Current Default Configuration");
        System.out.println(configs.entrySet()
                .stream()
                .map(e -> e.getKey() + AbstractConfigurable.KEY_VALUE_CONNECTOR + e.getValue())
                .collect(Collectors.joining("\n")));
        System.out.println("-".repeat(50));
    }

    protected void testValidityOfConfigurableFields() {
        var reflectAccess = new Reflections(ARDOCO);
        var classesThatMayBeConfigured = reflectAccess.getSubTypesOf(AbstractConfigurable.class)
                .stream()
                .filter(c -> c.getPackageName().startsWith(ARDOCO))
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .filter(c -> !c.getPackageName().contains("tests"))
                .toList();

        for (var clazz : classesThatMayBeConfigured) {
            List<Field> configurableFields = new ArrayList<>();
            findImportantFields(clazz, configurableFields);

            for (var field : configurableFields) {
                int modifiers = field.getModifiers();
                assertFalse(Modifier.isFinal(modifiers), "Field " + field.getName() + "@" + field.getDeclaringClass().getSimpleName() + " is final!");
                assertFalse(Modifier.isStatic(modifiers), "Field " + field.getName() + "@" + field.getDeclaringClass().getSimpleName() + " is static!");
            }
        }
    }

    protected void processConfigurationOfClass(Map<String, String> configs, Class<? extends AbstractConfigurable> clazz) throws InvocationTargetException,
            InstantiationException, IllegalAccessException {
        var object = ConfigurationInstantiatorUtils.createObject(clazz);
        List<Field> fields = new ArrayList<>();
        findImportantFields(object.getClass(), fields);
        fillConfigs(object, fields, configs);
    }

    private void fillConfigs(AbstractConfigurable object, List<Field> fields, Map<String, String> configs) throws IllegalAccessException {
        for (Field f : fields) {
            f.setAccessible(true);
            var key = AbstractConfigurable.getKeyOfField(object, f.getDeclaringClass(), f);
            var rawValue = f.get(object);
            var value = getValue(rawValue);
            if (configs.containsKey(key)) {
                fail("Found duplicate entry in map: " + key);
            }
            configs.put(key, value);
        }
    }

    private String getValue(Object rawValue) {
        if (rawValue instanceof Integer i) {
            return Integer.toString(i);
        }
        if (rawValue instanceof Double d) {
            return String.format(Locale.ENGLISH, "%f", d);
        }
        if (rawValue instanceof Boolean b) {
            return String.valueOf(b);
        }
        if (rawValue instanceof List<?> s && s.stream().allMatch(it -> it instanceof String)) {
            return s.stream().map(Object::toString).collect(Collectors.joining(AbstractConfigurable.LIST_SEPARATOR));
        }
        if (rawValue instanceof Enum<?> e) {
            return e.name();
        }

        throw new IllegalArgumentException("RawValue has no type that may be transformed to an Configuration" + rawValue + "[" + rawValue.getClass() + "]");

    }

    private void findImportantFields(Class<?> clazz, List<Field> fields) {
        if (clazz == Object.class || clazz == AbstractConfigurable.class) {
            return;
        }

        for (var field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Configurable.class)) {
                fields.add(field);
            }
        }
        findImportantFields(clazz.getSuperclass(), fields);
    }
}
