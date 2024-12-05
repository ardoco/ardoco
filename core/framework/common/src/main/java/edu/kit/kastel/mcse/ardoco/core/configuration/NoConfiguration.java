/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark classes that should not be configured. This means that the fields of the class will not be modified. The cascade
 * configuration will be applied to the fields of the class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface NoConfiguration {
}
