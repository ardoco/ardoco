/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.ChildClassConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;

@Deterministic
class ConfigurationHelperTest {

    @Test
    void getDefaultConfigurationOptionsTest() {
        var configs = ConfigurationHelper.getDefaultConfigurationOptions();
        Assertions.assertNotNull(configs);

        for (var entry : configs.entrySet()) {
            Assertions.assertAll(//
                    () -> Assertions.assertNotNull(entry.getKey()), //
                    () -> Assertions.assertNotNull(entry.getValue()));
        }
    }

    @Test
    void testBasicConfigurable() throws Exception {
        SortedMap<String, String> configs = new TreeMap<>();
        ConfigurationHelper.processConfigurationOfClass(configs, TestConfigurable.class);
        Assertions.assertEquals(5, configs.size());

        var t = new TestConfigurable();

        Assertions.assertEquals(24, t.testInt);
        Assertions.assertEquals(24, t.testIntNo);
        Assertions.assertEquals(2.0, t.testDouble);
        Assertions.assertEquals(2.0, t.testDoubleNo);
        Assertions.assertTrue(t.testBool);
        Assertions.assertTrue(t.testBoolNo);
        Assertions.assertEquals(List.of("A", "B", "C"), t.testList);
        Assertions.assertEquals(List.of("A", "B", "C"), t.testListNo);
        Assertions.assertEquals(TestConfigurable.MyEnum.A, t.testEnum);
        Assertions.assertEquals(TestConfigurable.MyEnum.B, t.testEnumNo);

        //@formatter:off
        configs = new TreeMap<>(Map.of(//
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testInt", "42", //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testIntNo", "42", //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testDouble", "48", //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testDoubleNo", "48", //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testBool", "false", //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testBoolNo", "false", //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testList", String.join(AbstractConfigurable.LIST_SEPARATOR, "X", "Y", "Z"), //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testListNo", String.join(AbstractConfigurable.LIST_SEPARATOR, "X", "Y", "Z"), //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testEnum", TestConfigurable.MyEnum.C.name(), //
                TestConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "testEnumNo", TestConfigurable.MyEnum.C.name()

        ));
        //@formatter:on

        t.applyConfiguration(configs);
        Assertions.assertEquals(42, t.testInt);
        Assertions.assertEquals(24, t.testIntNo);
        Assertions.assertEquals(48, t.testDouble);
        Assertions.assertEquals(2.0, t.testDoubleNo);
        Assertions.assertFalse(t.testBool);
        Assertions.assertTrue(t.testBoolNo);
        Assertions.assertEquals(List.of("X", "Y", "Z"), t.testList);
        Assertions.assertEquals(List.of("A", "B", "C"), t.testListNo);
        Assertions.assertEquals(TestConfigurable.MyEnum.C, t.testEnum);
        Assertions.assertEquals(TestConfigurable.MyEnum.B, t.testEnumNo);

    }

    @Test
    void testBaseAndChildConfigurable() throws Exception {
        SortedMap<String, String> configs = new TreeMap<>();
        ConfigurationHelper.processConfigurationOfClass(configs, TestBaseConfigurable.class);
        Assertions.assertEquals(1, configs.size());
        Assertions.assertEquals("1", configs.get(TestBaseConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "value"));

        configs = new TreeMap<>();
        ConfigurationHelper.processConfigurationOfClass(configs, TestChildConfigurable.class);
        Assertions.assertEquals(1, configs.size());
        Assertions.assertEquals("2", configs.get(TestChildConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "value"));

        configs = new TreeMap<>(Map.of(//
                TestBaseConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "value", "42", //
                TestChildConfigurable.class.getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "value", "43" //
        ));

        var t1 = new TestBaseConfigurable();
        t1.applyConfiguration(configs);
        Assertions.assertEquals(42, t1.value);

        var t2 = new TestChildConfigurable();
        t2.applyConfiguration(configs);
        Assertions.assertEquals(43, t2.value);
    }

    private static class TestBaseConfigurable extends AbstractConfigurable {
        @Configurable
        @ChildClassConfigurable
        public int value;

        public TestBaseConfigurable() {
            value = 1;
        }

        @Override
        protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
            // NOP
        }
    }

    private static final class TestChildConfigurable extends TestBaseConfigurable {
        public TestChildConfigurable() {
            super();
            value = 2;
        }
    }

    private static final class TestConfigurable extends AbstractConfigurable {

        @Configurable
        private int testInt = 24;
        @Configurable
        private double testDouble = 2.0;
        @Configurable
        private boolean testBool = true;
        @Configurable
        private List<String> testList = List.of("A", "B", "C");
        @Configurable
        private MyEnum testEnum = MyEnum.A;

        private int testIntNo = 24;
        private double testDoubleNo = 2.0;
        private boolean testBoolNo = true;
        private List<String> testListNo = List.of("A", "B", "C");
        private MyEnum testEnumNo = MyEnum.B;

        public TestConfigurable() {
            // NOP
        }

        @Override
        protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        }

        private enum MyEnum {
            A, B, C
        }
    }
}
