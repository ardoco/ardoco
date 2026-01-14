/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.tests.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.JsonHandling;
import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;

public class BasicArchitectureTest {
    @ArchTest
    public static final ArchRule configurableFieldsOnlyInConfigurableClasses = fields().that()
            .areAnnotatedWith(Configurable.class)
            .should()
            .beDeclaredInClassesThat()
            .areAssignableTo(AbstractConfigurable.class);

    @ArchTest
    public static final ArchRule traceLinksShouldBeFinal = classes().that()
            .areAssignableTo(TraceLink.class)
            .and()
            .doNotHaveFullyQualifiedName(TraceLink.class.getName())
            .should()
            .haveModifier(JavaModifier.FINAL);

    @ArchTest
    public static final ArchRule jacksonIsConfiguredGlobally = noClasses().that()
            .doNotHaveFullyQualifiedName(JsonHandling.class.getName())
            .should()
            .callConstructor(ObjectMapper.class);

    /**
     * Rule that enforces environment variable access restrictions.
     * <p>
     * Only the {@link Environment} utility class may call {@code System.getenv()}.
     * All other classes must use the {@link Environment} class for environment variable access.
     */
    @ArchTest
    static final ArchRule noGetEnv = noClasses().that()
            .haveNameNotMatching(Environment.class.getName())
            .should()
            .callMethod(System.class, "getenv")
            .orShould()
            .callMethod(System.class, "getenv", String.class);

    /**
     * Rule that enforces functional programming practices.
     * <p>
     * Discourages the use of {@code forEach} and {@code forEachOrdered} on streams and lists,
     * as these are typically used for side effects. Prefer functional operations instead.
     */
    @ArchTest
    static final ArchRule noForEachInCollectionsOrStream = noClasses().should()
            .callMethod(Stream.class, "forEach", Consumer.class)
            .orShould()
            .callMethod(Stream.class, "forEachOrdered", Consumer.class)
            .orShould()
            .callMethod(List.class, "forEach", Consumer.class)
            .orShould()
            .callMethod(List.class, "forEachOrdered", Consumer.class)
            .because("Lambdas should be functional. ForEach is typically used for side-effects.");

    @ArchTest
    public static final ArchRule preferEclipseCollections = noMethods().that()
            .areDeclaredInClassesThat()
            .areInterfaces()
            .and()
            .areDeclaredInClassesThat()
            .resideOutsideOfPackage("..metrics..")
            .should()
            .haveRawReturnType(List.class)
            .orShould()
            .haveRawReturnType(Set.class)
            .orShould()
            .haveRawReturnType(SortedSet.class)
            .orShould()
            .haveRawReturnType(Map.class)
            .orShould()
            .haveRawReturnType(SortedMap.class)
            .orShould()
            .haveRawParameterTypes(List.class)
            .orShould()
            .haveRawParameterTypes(Set.class)
            .orShould()
            .haveRawParameterTypes(SortedSet.class)
            .orShould()
            .haveRawParameterTypes(Map.class)
            .orShould()
            .haveRawParameterTypes(SortedMap.class);
}
