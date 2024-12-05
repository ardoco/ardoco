/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a field that is configurable as configured by child class. That means that the key that is used to configure the field is based on the
 * actual class (not on the class where the configurable field is defined).
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface ChildClassConfigurable {
}
