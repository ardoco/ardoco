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

    @Configurable
    private boolean usePersistence = false;

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

    @Override
    protected void delegateApplyConfigurationToInternalObjects(ImmutableSortedMap<String, String> additionalConfiguration) {
        usePersistenceStatic = this.usePersistence;
    }
}
