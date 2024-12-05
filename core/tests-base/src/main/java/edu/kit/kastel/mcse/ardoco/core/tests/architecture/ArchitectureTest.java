/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaFieldAccess;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.domain.JavaTypeVariable;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packages = "edu.kit.kastel.mcse.ardoco")
public class ArchitectureTest {
    @ArchTest
    public static final ArchRule noDependencyOnExecution = classes().that()
            .resideInAPackage("..execution..")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..execution..", "..tests..");
    @ArchTest
    public static final ArchRule modelInstancesOnlyAfterModelExtraction = classes().that()
            .haveSimpleName("ModelInstance")
            .or()
            .haveSimpleName("Model")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..models..", "..connectiongenerator..", "..inconsistency..", "..id..", "..pipeline..", "..common..", "..output..",
                    "..tests..");

    @ArchTest
    public static final ArchRule linksOnlyAfterConnectionGenerator = classes().that()
            .haveSimpleNameEndingWith("Link")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..connectiongenerator..", "..codetraceability..", "..tracelinks..", "..inconsistency..", "..id..", "..pipeline..",
                    "..common..", "..api..", "..tests..");

    @ArchTest
    public static final ArchRule usingLinkAsNamingOnlyInConnectionGenerator = classes().that()
            .haveSimpleNameEndingWith("Link")
            .should()
            .resideInAnyPackage("..models.tracelinks..", "..connectiongenerator..", "..output..", "..tests..");

    @ArchTest
    public static final ArchRule inconsistencyOnlyAfterInconsistencyDetection = classes().that()
            .haveSimpleNameContaining("Inconsistency")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..inconsistency..", "..id..", "..execution..", "..api..", "..common..", "..tests..");

    @ArchTest
    public static final ArchRule layerRule = layeredArchitecture().consideringAllDependencies()
            // Layer definition
            .layer("Common")
            .definedBy("..common..", "..api..", "..tests..")
            .layer("TextExtractor")
            .definedBy("..textextraction..")
            .layer("ModelExtractor")
            .definedBy("..models..")
            .layer("RecommendationGenerator")
            .definedBy("..recommendationgenerator..")
            .layer("ConnectionGenerator")
            .definedBy("..connectiongenerator..")
            .layer("InconsistencyDetection")
            .definedBy("..inconsistency..", "..id..")
            .layer("CodeTraceability")
            .definedBy("..codetraceability..")
            .layer("Pipeline")
            .definedBy("..pipeline..")
            .layer("Execution")
            .definedBy("..execution..")
            // rule definition
            .whereLayer("Execution")
            .mayOnlyBeAccessedByLayers("Common") // Needed for tests
            .whereLayer("InconsistencyDetection")
            .mayOnlyBeAccessedByLayers("Pipeline", "Common", "Execution")
            .whereLayer("ConnectionGenerator")
            .mayOnlyBeAccessedByLayers("CodeTraceability", "InconsistencyDetection", "Pipeline", "Common", "Execution")
            .whereLayer("RecommendationGenerator")
            .mayOnlyBeAccessedByLayers("ConnectionGenerator", "InconsistencyDetection", "Pipeline", "Common", "Execution")
            .whereLayer("TextExtractor")
            .mayOnlyBeAccessedByLayers("RecommendationGenerator", "ConnectionGenerator", "InconsistencyDetection", "Pipeline", "Common", "Execution")
            .whereLayer("ModelExtractor")
            .mayOnlyBeAccessedByLayers("RecommendationGenerator", "ConnectionGenerator", "CodeTraceability", "InconsistencyDetection", "Pipeline", "Common",
                    "Execution");

    @ArchTest
    public static final ArchRule transientRule = fields().that()
            .areDeclaredInClassesThat()
            .resideOutsideOfPackages("..tests..")
            .and()
            .haveModifier(JavaModifier.TRANSIENT)
            .should(new ArchCondition<>("beAccessedIndirectly") {
                @Override
                public void check(JavaField javaField, ConditionEvents conditionEvents) {
                    javaField.getAccessesToSelf().forEach(fieldAccess -> {
                        var origin = fieldAccess.getOrigin();
                        if (fieldAccess.getAccessType().equals(JavaFieldAccess.AccessType.GET)) {
                            if (origin.isMethod()) {
                                if (origin.getName().equalsIgnoreCase("get" + fieldAccess.getName())) {
                                    satisfied(conditionEvents, javaField, null);
                                } else {
                                    violated(conditionEvents, javaField, "Method accesses " + origin.getFullName() + " accesses transient field " + javaField
                                            .getFullName() + ", but is not a getter or does not match the name pattern for getters 'get{$FieldName}'");
                                }
                            } else {
                                violated(conditionEvents, javaField, "Transient field " + javaField
                                        .getFullName() + " has to be accessed by a getter with name 'get{$fieldName}'");
                            }
                        } else {
                            if (origin.isConstructor()) {
                                violated(conditionEvents, javaField, "Transient field " + javaField
                                        .getFullName() + " has to be set outside of the constructor");
                            } else {
                                satisfied(conditionEvents, javaField, null);
                            }
                        }
                    });
                }
            })
            .orShould(new ArchCondition<>("belongToClassWithCustomSerialization") {
                @Override
                public void check(JavaField javaField, ConditionEvents conditionEvents) {
                    if (javaField.getOwner().getMethods().stream().noneMatch(method -> method.getName().equalsIgnoreCase("writeObject"))) {
                        violated(conditionEvents, javaField, "Transient field " + javaField
                                .getFullName() + " doesn't belong to a class with a custom serialization");
                    }
                }
            });

    @ArchTest
    private static final ArchRule serializableRule = classes().that()
            .areNotInterfaces()
            .and()
            .doNotHaveModifier(JavaModifier.ABSTRACT)
            .and()
            .areAssignableTo(Serializable.class)
            .and()
            .areNotEnums()
            .and()
            .resideOutsideOfPackages("..tests..")
            .should(new ArchCondition<>("beSerializable") {
                @Override
                public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
                    if (javaClass.getMethods().stream().noneMatch(method -> method.getName().equalsIgnoreCase("writeObject"))) {
                        Predicate<? super JavaField> transientOrStatic = (JavaField javaField) -> new LinkedHashSet<>(javaField.getModifiers()).removeAll(List
                                .of(JavaModifier.STATIC, JavaModifier.TRANSIENT));
                        var fields = javaClass.getFields();
                        for (var field : fields) {
                            if (transientOrStatic.test(field))
                                continue;
                            var erasure = field.getType().toErasure();
                            if (isContainer.test(erasure)) {
                                getAllInvolvedRawTypesExceptSelf(field).stream().filter(erasureIsSerializableShallow.negate()).forEach(parameter -> {
                                    violated(conditionEvents, javaClass, "Non-transient field " + field.getFullName() + " of serializable class " + javaClass
                                            .getFullName() + " needs to be serializable or the class must have custom serialization, but has non-serializable parameter " + parameter
                                                    .getName());
                                });
                            } else {
                                if (erasureIsSerializableShallow.negate().test(erasure)) {
                                    //Class has non-transient field that is not serializable
                                    violated(conditionEvents, javaClass, "Non-transient field " + field.getFullName() + " of serializable class " + javaClass
                                            .getFullName() + " needs to be serializable or the class must have custom serialization");
                                }
                            }
                        }
                    }
                }
            });

    private static final Predicate<? super JavaClass> isContainer = (JavaClass javaClass) -> {
        return javaClass.isArray() || javaClass.isAssignableTo(Collection.class) || javaClass.isAssignableTo(Map.class) || javaClass.isAssignableTo(
                Iterable.class);
    };

    private static final Predicate<? super JavaClass> erasureIsSerializableShallow = (JavaClass javaClass) -> {
        return javaClass.isPrimitive() || javaClass.isAssignableTo(Serializable.class) || isContainer.test(javaClass);
    };

    /**
     * Returns all types of a field, except the (outer) type of the field itself. Generic type variables are not considered.
     *
     * @param javaField the field
     * @return all types of a field
     */
    private static LinkedHashSet<JavaClass> getAllInvolvedRawTypesExceptSelf(JavaField javaField) {
        var javaType = javaField.getType();
        LinkedHashSet<JavaClass> set;
        if (javaType instanceof JavaParameterizedType javaParameterizedType) {
            set = javaParameterizedType.getActualTypeArguments()
                    .stream()
                    .filter(typeArgument -> !(typeArgument instanceof JavaTypeVariable<?>))
                    .map(JavaType::toErasure)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            set = new LinkedHashSet<>(javaType.getAllInvolvedRawTypes());
        }
        set.remove(javaType);
        return set;
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
