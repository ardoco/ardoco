/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaWildcardType;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.architecture.NoHashCodeEquals;

@AnalyzeClasses(packages = "edu.kit.kastel.mcse.ardoco")
public class DeterministicArDoCoTest {

    private static final Logger logger = LoggerFactory.getLogger(DeterministicArDoCoTest.class);

    @ArchTest
    public static final ArchRule forbidUnorderedSetsAndMaps = noClasses().that()
            .resideOutsideOfPackages("..tests..", "..metrics..")
            .and(areNotDirectlyAnnotatedWith(Deterministic.class))
            .should()
            .accessClassesThat(areForbiddenClasses())
            .orShould()
            .dependOnClassesThat(areForbiddenClasses());

    private static DescribedPredicate<? super JavaClass> areNotDirectlyAnnotatedWith(Class<? extends Annotation> targetAnnotation) {
        return new DescribedPredicate<>("not directly annotated with " + targetAnnotation.getName()) {
            @Override
            public boolean test(JavaClass javaClass) {
                var annotations = javaClass.getAnnotations();
                for (var annotation : annotations) {
                    if (annotation.getRawType().getFullName().equals(targetAnnotation.getName())) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private static DescribedPredicate<? super JavaClass> areForbiddenClasses() {
        Set<Class<?>> forbiddenClasses = Set.of(Set.class, HashSet.class, MutableSet.class, ImmutableSet.class, Sets.class, //
                Map.class, HashMap.class, MutableMap.class, ImmutableMap.class, Maps.class //
        );
        return new DescribedPredicate<>("forbidden classes") {
            @Override
            public boolean test(JavaClass javaClass) {
                return forbiddenClasses.stream().map(Class::getName).anyMatch(it -> it.equals(javaClass.getFullName()));
            }
        };
    }

    @ArchTest
    public static final ArchRule forbidHashMapAndHashSetInFavorOfLinkedVersions = noClasses().that()
            .resideOutsideOfPackages("..tests..")
            .and()
            .doNotHaveFullyQualifiedName(DeterministicArDoCoTest.class.getName())
            .should()
            .accessClassesThat()
            .haveNameMatching(HashMap.class.getName() + "|" + HashSet.class.getName())
            .orShould()
            .dependOnClassesThat()
            .haveNameMatching(HashMap.class.getName() + "|" + HashSet.class.getName());

    @ArchTest
    public static final ArchRule forbidEqualsAndHashCodeInCertainClasses = noClasses().that()
            .areAnnotatedWith(NoHashCodeEquals.class)
            .should(implementEqualsOrHashCode());

    private static ArchCondition<? super JavaClass> implementEqualsOrHashCode() {
        return new ArchCondition<>("implement equals or hashCode") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
                var methods = javaClass.getAllMethods();
                for (var method : methods) {
                    if (!method.getFullName().contains(javaClass.getFullName()))
                        continue;
                    if (method.getName().equals("hashCode") || method.getName().equals("equals")) {
                        satisfied(conditionEvents, javaClass, "Class " + javaClass.getName() + " implements " + method.getFullName());
                    }
                }
            }
        };
    }

    @ArchTest
    public static final ArchRule ensureContractBetweenEqualsHashCodeAndCompareTo = classes().that(directlyImplement(Comparable.class))
            .and()
            .areNotEnums()
            .and()
            .areNotInterfaces()
            .and()
            .areNotAnonymousClasses() // e.g., type references for jackson
            .should(implementEqualsAndHashCode());

    private static DescribedPredicate<? super JavaClass> directlyImplement(Class<?> targetClass) {
        return new DescribedPredicate<>("directly implement " + targetClass.getName()) {
            @Override
            public boolean test(JavaClass javaClass) {
                var directInterfaces = javaClass.getRawInterfaces();
                for (var di : directInterfaces) {
                    if (di.getName().equals(targetClass.getName())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private static ArchCondition<? super JavaClass> implementEqualsAndHashCode() {
        return new ArchCondition<>("implement equals or hashCode") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
                var methods = javaClass.getAllMethods();
                boolean equals = false;
                boolean hashCode = false;
                for (var method : methods) {
                    if (!method.getFullName().contains(javaClass.getFullName()))
                        continue;

                    if (method.getName().equals("hashCode")) {
                        hashCode = true;
                    } else if (method.getName().equals("equals")) {
                        equals = true;
                    }
                }

                if (equals && hashCode) {
                    satisfied(conditionEvents, javaClass, "Class " + javaClass.getName() + " implements equals and hashCode");
                } else if (equals) {
                    violated(conditionEvents, javaClass, "Class " + javaClass.getName() + " implements equals but not hashCode");
                } else if (hashCode) {
                    violated(conditionEvents, javaClass, "Class " + javaClass.getName() + " implements hashCode but not equals");
                } else {
                    violated(conditionEvents, javaClass, "Class " + javaClass.getName() + " implements neither equals nor hashCode");
                }
            }
        };
    }

    @ArchTest
    public static final ArchRule ensureSortedCollectionsOnlyForComparableTypes = fields().that()
            .haveRawType(SortedMap.class)
            .or()
            .haveRawType(ImmutableSortedMap.class)
            .or()
            .haveRawType(MutableSortedMap.class)
            .or()
            .haveRawType(SortedSet.class)
            .or()
            .haveRawType(ImmutableSortedSet.class)
            .or()
            .haveRawType(MutableSortedSet.class)
            .should(haveComparableGenericType());

    @ArchTest
    public static final ArchRule ensureSortedCollectionsOnlyForComparableTypesInReturn = methods().that()
            .haveRawReturnType(SortedSet.class)
            .or()
            .haveRawReturnType(ImmutableSortedSet.class)
            .or()
            .haveRawReturnType(MutableSortedSet.class)
            .should(haveComparableReturn());

    @ArchTest
    public static final ArchRule ensureSortedMapOnlyForComparableTypesInReturn = methods().that()
            .haveRawReturnType(SortedMap.class)
            .or()
            .haveRawReturnType(ImmutableSortedMap.class)
            .or()
            .haveRawReturnType(MutableSortedMap.class)
            .should(haveComparableReturn());

    private static ArchCondition<? super JavaField> haveComparableGenericType() {
        return new ArchCondition<>("have Comparable generic type") {
            @Override
            public void check(JavaField javaField, ConditionEvents conditionEvents) {
                var type = javaField.getType();
                if (type instanceof JavaParameterizedType parameterizedType) {
                    var typeParameter = parameterizedType.getActualTypeArguments().get(0);
                    if ((typeParameter instanceof JavaClass typeParameterClass) && typeParameterClass.getAllRawInterfaces()
                            .stream()
                            .anyMatch(i -> i.getFullName().equals(Comparable.class.getName()))) {

                        satisfied(conditionEvents, javaField, "Field " + javaField.getFullName() + " has a Comparable generic type");
                    } else {
                        violated(conditionEvents, javaField, "Field " + javaField.getFullName() + " has a non-Comparable generic type");
                    }
                } else if (type instanceof JavaClass) {
                    // Classes generated from lambdas cannot be checked :(
                    logger.debug("Skipping field {}", javaField.getFullName());
                } else {
                    violated(conditionEvents, javaField, "Field " + javaField.getFullName() + " is not a parameterized type");
                }
            }
        };
    }

    private static ArchCondition<? super JavaMethod> haveComparableReturn() {
        return new ArchCondition<>("have Comparable generic type") {
            @Override
            public void check(JavaMethod javaMethod, ConditionEvents conditionEvents) {
                var type = javaMethod.getReturnType();
                if (!(type instanceof JavaParameterizedType parameterizedType)) {
                    violated(conditionEvents, javaMethod, "Method " + javaMethod.getFullName() + " is not a parameterized type");
                    return;
                }

                var typeParameter = parameterizedType.getActualTypeArguments().get(0);
                if ((typeParameter instanceof JavaClass typeParameterClass) && typeParameterClass.getAllRawInterfaces()
                        .stream()
                        .anyMatch(i -> i.getFullName().equals(Comparable.class.getName()))) {

                    satisfied(conditionEvents, javaMethod, "Method " + javaMethod.getFullName() + " has a Comparable generic type");
                } else if ((typeParameter instanceof JavaWildcardType typeParameterWildCard)) {
                    var upperBound = typeParameterWildCard.getUpperBounds().get(0);

                    if (!(upperBound instanceof JavaClass upperBoundClass) || upperBoundClass.getAllRawInterfaces()
                            .stream()
                            .noneMatch(i -> i.getFullName().equals(Comparable.class.getName()))) {
                        violated(conditionEvents, javaMethod, "Method " + javaMethod.getFullName() + " has a non-Comparable generic type");
                        return;
                    }

                    satisfied(conditionEvents, javaMethod, "Method " + javaMethod.getFullName() + " has a Comparable generic type");
                } else {
                    violated(conditionEvents, javaMethod, "Method " + javaMethod.getFullName() + " has a non-Comparable generic type");
                }
            }
        };
    }

    private static void satisfied(ConditionEvents events, Object location, String message) {
        var event = new SimpleConditionEvent(location, true, message);
        events.add(event);
    }

    private static void violated(ConditionEvents events, Object location, String message) {
        var event = new SimpleConditionEvent(location, false, message);
        events.add(event);
    }

}
