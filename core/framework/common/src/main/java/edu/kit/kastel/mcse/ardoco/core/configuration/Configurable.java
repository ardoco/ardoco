/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.SortedMap;

/**
 * Marks a field as configurable. Should be used in conjunction with {@link IConfigurable}. The annotated field should not be marked as final or static, since
 * the purpose of the field is to be written using an implementation of {@link IConfigurable#applyConfiguration(SortedMap)}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface Configurable {
    String key() default "";
}
