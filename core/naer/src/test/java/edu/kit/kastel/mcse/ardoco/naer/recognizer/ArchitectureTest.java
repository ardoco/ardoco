/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.naer.recognizer;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packages = "edu.kit.kastel.mcse.ardoco.naer")
class ArchitectureTest {
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
    public static final ArchRule forbidHashMapAndHashSetInFavorOfLinkedVersions = noClasses().that()
            .doNotHaveFullyQualifiedName(ArchitectureTest.class.getName())
            .should()
            .accessClassesThat()
            .haveNameMatching(HashMap.class.getName() + "|" + HashSet.class.getName())
            .orShould()
            .dependOnClassesThat()
            .haveNameMatching(HashMap.class.getName() + "|" + HashSet.class.getName());

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
            .should(haveComparableReturn())
            .allowEmptyShould(true);

    private static ArchCondition<? super JavaField> haveComparableGenericType() {
        return new ArchCondition<>("have Comparable generic type") {
            @Override
            public void check(JavaField javaField, ConditionEvents conditionEvents) {
                var type = javaField.getType();
                if (type instanceof JavaParameterizedType parameterizedType) {
                    var typeParameter = parameterizedType.getActualTypeArguments().getFirst();
                    if ((typeParameter instanceof JavaClass typeParameterClass) && typeParameterClass.getAllRawInterfaces()
                            .stream()
                            .anyMatch(i -> i.getFullName().equals(Comparable.class.getName()))) {

                        satisfied(conditionEvents, javaField, "Field " + javaField.getFullName() + " has a Comparable generic type");
                    } else {
                        violated(conditionEvents, javaField, "Field " + javaField.getFullName() + " has a non-Comparable generic type");
                    }
                } else if (type instanceof JavaClass) {
                    // Classes generated from lambdas cannot be checked :(
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

                var typeParameter = parameterizedType.getActualTypeArguments().getFirst();
                if ((typeParameter instanceof JavaClass typeParameterClass) && typeParameterClass.getAllRawInterfaces()
                        .stream()
                        .anyMatch(i -> i.getFullName().equals(Comparable.class.getName()))) {

                    satisfied(conditionEvents, javaMethod, "Method " + javaMethod.getFullName() + " has a Comparable generic type");
                } else if ((typeParameter instanceof JavaWildcardType typeParameterWildCard)) {
                    var upperBound = typeParameterWildCard.getUpperBounds().getFirst();

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
