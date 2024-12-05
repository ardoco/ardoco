package edu.kit.kastel.mcse.ardoco.core;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import java.util.List;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

@AnalyzeClasses(packages = "edu.kit.kastel.mcse.ardoco")
class TraceLinkArchitectureTest {
    private static final DescribedPredicate<JavaClass> isSubclassOfTraceLink = new DescribedPredicate<>("subclass of " + TraceLink.class) {
        @Override
        public boolean test(JavaClass clazz) {
            return clazz.isAssignableTo(TraceLink.class);
        }
    };

    private static final DescribedPredicate<List<JavaClass>> isSubclassOfTraceLinkList = new DescribedPredicate<>("subclass of " + TraceLink.class) {
        @Override
        public boolean test(List<JavaClass> clazzes) {
            return clazzes.stream().anyMatch(clazz -> clazz.isAssignableTo(TraceLink.class));
        }
    };

    private static final DescribedPredicate<List<JavaClass>> isSubclassOfTraceLinkListButNotTracelink = new DescribedPredicate<>(
            "subclass of " + TraceLink.class + " without base class") {
        @Override
        public boolean test(List<JavaClass> clazzes) {
            return clazzes.stream().anyMatch(clazz -> clazz.isAssignableTo(TraceLink.class) && !clazz.isEquivalentTo(TraceLink.class));
        }
    };

    @ArchTest
    static final ArchRule onlyTraceLinkAsReturnType = methods().that()
            .haveRawReturnType(isSubclassOfTraceLink)
            .and()
            .areNotPrivate()
            .should()
            .haveRawReturnType(TraceLink.class)
            .because("the specific subclasses of TraceLink shall not be used as return type in non-private methods");

    @ArchTest
    static final ArchRule onlyTraceLinkAsParameterType = methods().that()
            .haveRawParameterTypes(isSubclassOfTraceLinkList)
            .and()
            .areNotPrivate()
            .should()
            .notHaveRawParameterTypes(isSubclassOfTraceLinkListButNotTracelink)
            .because("the specific subclasses of TraceLink shall not be used as parameter type in non-private methods");
}
