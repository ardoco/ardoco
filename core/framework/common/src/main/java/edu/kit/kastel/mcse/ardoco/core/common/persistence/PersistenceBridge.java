/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;

/**
 * This class serves as a holder for a concrete implementation of the persistence handler
 * (i.e. by the neo4j-schema module) to be able to statically access the handler from
 * classes like DatarepositoryHelper.java or ModelStates.java
 *
 * THe persistanceHandler in this class is populated from the neo4j-schema
 */
public class PersistenceBridge extends AbstractConfigurable {
    //singleton instance of the bringe
    private static final PersistenceBridge INSTANCE = new PersistenceBridge();

    private static PersistenceHandler handler;

    public static boolean usePersistenceStatic = false;

    public static boolean persistTextStateStatic = false;

    public static boolean persistRecommendationsStatic = false;

    @Configurable
    private boolean usePersistence = false;

    /**
     * When true (and {@link #isAvailable()}), TextState noun mappings are dual-written to Neo4j.
     * Default false so existing persistence tests stay unchanged.
     */
    @Configurable
    private boolean persistTextState = false;

    /**
     * When true (and {@link #isAvailable()}), RecommendedInstances are dual-written to Neo4j.
     * Default false. Enable together with {@link #persistTextState} so HAS_NAME_MAPPING can resolve NounMapping nodes.
     */
    @Configurable
    private boolean persistRecommendations = false;

    private PersistenceBridge() {
        // Private constructor for Singleton
    }

    public static PersistenceBridge getInstance() {
        return INSTANCE;
    }

    public static void setHandler(PersistenceHandler newHandler) {
        handler = newHandler;
    }

    public static PersistenceHandler getHandler() {
        return usePersistenceStatic ? handler : null;
    }

    public static boolean isAvailable() {
        return usePersistenceStatic && handler != null;
    }

    /**
     * @return true if Neo4j persistence is available and TextState dual-write is enabled
     */
    public static boolean shouldPersistTextState() {
        return isAvailable() && persistTextStateStatic;
    }

    /**
     * @return true if Neo4j persistence is available and recommendation dual-write is enabled
     */
    public static boolean shouldPersistRecommendations() {
        return isAvailable() && persistRecommendationsStatic;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(ImmutableSortedMap<String, String> additionalConfiguration) {
        usePersistenceStatic = this.usePersistence;
        persistTextStateStatic = this.persistTextState;
        persistRecommendationsStatic = this.persistRecommendations;
    }
}
