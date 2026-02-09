/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

/**
 * This class serves as a holder for a concrete implementation of the persistence handler
 * (i.e. by the neo4j-schema module) to be able to statically access the handler from
 * classes like DatarepositoryHelper.java or ModelStates.java
 *
 * THe persistanceHandler in this class is populated from the neo4j-schema
 */
public class PersistenceBridge {
    private static PersistenceHandler handler;

    public static void setHandler(PersistenceHandler newHandler) {
        handler = newHandler;
    }

    public static PersistenceHandler getHandler() {
        return handler;
    }

    public static boolean isAvailable() {
        return handler != null;
    }
}
