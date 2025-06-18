/* Licensed under MIT 2021-2025. */
package edu.kit.kastel.mcse.ardoco.core.tests.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

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
            .haveSimpleName("Model")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..models..", "..recommendationgenerator..", "..connectiongenerator..", "..inconsistency..", "..id..", "..pipeline..",
                    "..common..", "..output..", "..tests..");

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
            .resideInAnyPackage("..tracelink..", "..codetraceability..", "..connectiongenerator..");

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
            .definedBy("..common..", "..data..", "..api..", "..tests..")
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

}
