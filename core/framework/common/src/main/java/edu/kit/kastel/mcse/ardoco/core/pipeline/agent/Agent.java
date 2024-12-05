/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline.agent;

/**
 * An Agent is a {@link Claimant} with an ID
 */
public interface Agent extends Claimant {

    /**
     * Return the id of the agent
     *
     * @return the id of the agent
     */
    String getId();
}
