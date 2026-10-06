/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.lang.reflect.Method;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.RecommendationModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.RecommendationStatesImpl;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.RecommendedInstanceImpl;

/**
 * Unit-style check for {@link ConnectionGenerator} metamodel resolution during resume hydrate.
 */
class ConnectionGeneratorMetamodelTest {

    private static final Claimant TEST_CLAIMANT = new Claimant() {
    };

    @Test
    @DisplayName("findMetamodelForLink returns null when RecommendedInstance id is unknown")
    void testFindMetamodelForLinkReturnsNullForUnknownRi() throws Exception {
        RecommendationStatesImpl states = (RecommendationStatesImpl) RecommendationStatesImpl.build(new Metamodel[] { Metamodel.ARCHITECTURE_WITH_COMPONENTS });
        states.getRecommendationState(Metamodel.ARCHITECTURE_WITH_COMPONENTS).addRecommendedInstance("Auth", "Service", TEST_CLAIMANT, 1.0,
                Lists.immutable.empty(), Lists.immutable.empty());

        ArchitectureEntity arch = new ArchitectureComponent("Other", "arch-id", new TreeSet<>(), new TreeSet<>(), new TreeSet<>(), "Service");
        RecommendedInstanceImpl unknownRi = new RecommendedInstanceImpl("Ghost", "Type", TEST_CLAIMANT, 1.0, Lists.immutable.empty(),
                Lists.immutable.empty());
        RecommendationModelTraceLink link = new RecommendationModelTraceLink(unknownRi, arch);

        Method method = ConnectionGenerator.class.getDeclaredMethod("findMetamodelForLink", java.util.Collection.class, RecommendationStates.class,
                RecommendationModelTraceLink.class);
        method.setAccessible(true);
        Object resolved = method.invoke(null, List.of(Metamodel.ARCHITECTURE_WITH_COMPONENTS), states, link);

        Assertions.assertNull(resolved, "Unknown RI id must not fall back to the first active metamodel");
    }

    @Test
    @DisplayName("findMetamodelForLink uses RecommendedInstance metamodel when set")
    void testFindMetamodelForLinkUsesRiMetamodel() throws Exception {
        RecommendationStatesImpl states = (RecommendationStatesImpl) RecommendationStatesImpl.build(new Metamodel[] { Metamodel.ARCHITECTURE_WITH_COMPONENTS });
        RecommendedInstanceImpl knownRi = new RecommendedInstanceImpl("Auth", "Service", TEST_CLAIMANT, 1.0, Lists.immutable.empty(),
                Lists.immutable.empty());
        knownRi.setMetamodel(Metamodel.ARCHITECTURE_WITH_COMPONENTS);
        states.getRecommendationState(Metamodel.ARCHITECTURE_WITH_COMPONENTS).hydrateRecommendedInstance(knownRi);

        ArchitectureEntity arch = new ArchitectureComponent("Auth", "arch-id", new TreeSet<>(), new TreeSet<>(), new TreeSet<>(), "Service");
        RecommendationModelTraceLink link = new RecommendationModelTraceLink(knownRi, arch);

        Method method = ConnectionGenerator.class.getDeclaredMethod("findMetamodelForLink", java.util.Collection.class, RecommendationStates.class,
                RecommendationModelTraceLink.class);
        method.setAccessible(true);
        Object resolved = method.invoke(null, List.of(Metamodel.ARCHITECTURE_WITH_COMPONENTS), states, link);

        Assertions.assertEquals(Metamodel.ARCHITECTURE_WITH_COMPONENTS, resolved);
    }
}
