/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark configurable classes that are skipped when extracting the default configuration, e.g., because they cannot be instantiated without
 * specific constructor arguments. Their configurable fields can still be configured.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface NoDefaultConfiguration {
}
