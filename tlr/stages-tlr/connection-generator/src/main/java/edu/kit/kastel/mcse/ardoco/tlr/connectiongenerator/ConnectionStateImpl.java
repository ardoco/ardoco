/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The connection state encapsulates all connections between the model extraction state and the recommendation state.
 * These connections are stored in instance and relation links.
 */
public class ConnectionStateImpl extends AbstractState implements ConnectionState {

    private MutableList<InstanceLink> instanceLinks;

    /**
     * Creates a new connection state.
     */
    public ConnectionStateImpl() {
        super();
        instanceLinks = Lists.mutable.empty();
    }

    /**
     * Returns all instance links.
     *
     * @return all instance links
     */
    @Override
    public ImmutableList<InstanceLink> getInstanceLinks() {
        return Lists.immutable.withAll(instanceLinks);
    }

    /**
     * Returns all instance links with a model instance containing the given name.
     *
     * @param name the name of a model instance
     * @return all instance links with a model instance containing the given name as list
     */
    @Override
    public ImmutableList<InstanceLink> getInstanceLinksByName(String name) {
        return Lists.immutable.fromStream(instanceLinks.stream().filter(imapping -> imapping.getModelInstance().getNameParts().contains(name)));
    }

    /**
     * Returns all instance links with a model instance containing the given type.
     *
     * @param type the type of a model instance
     * @return all instance links with a model instance containing the given type as list
     */
    @Override
    public ImmutableList<InstanceLink> getInstanceLinksByType(String type) {
        return Lists.immutable.fromStream(instanceLinks.stream().filter(ilink -> ilink.getModelInstance().getTypeParts().contains(type)));
    }

    @Override
    public ImmutableList<InstanceLink> getInstanceLinksByRecommendedInstance(RecommendedInstance recommendedInstance) {
        return Lists.immutable.fromStream(instanceLinks.stream().filter(il -> il.getTextualInstance().equals(recommendedInstance)));
    }

    /**
     * Returns all instance links with a model instance containing the given name and type.
     *
     * @param type the type of a model instance
     * @param name the name of a model instance
     * @return all instance links with a model instance containing the given name and type as list
     */
    @Override
    public ImmutableList<InstanceLink> getInstanceLinks(String name, String type) {
        return Lists.immutable.fromStream(instanceLinks.stream()
                .filter(imapping -> imapping.getModelInstance().getNameParts().contains(name))//
                .filter(imapping -> imapping.getModelInstance().getTypeParts().contains(type)));
    }

    /**
     * Adds the connection of a recommended instance and a model instance to the state. If the model instance is already
     * contained by the state it is extended. Elsewhere a new instance link is created
     *
     * @param recommendedModelInstance the recommended instance
     * @param instance                 the model instance
     * @param probability              the probability of the link
     */
    @Override
    public void addToLinks(RecommendedInstance recommendedModelInstance, ModelInstance instance, Claimant claimant, double probability) {

        var newInstanceLink = new InstanceLink(recommendedModelInstance, instance, claimant, probability);
        if (!isContainedByInstanceLinks(newInstanceLink)) {
            instanceLinks.add(newInstanceLink);
        } else {
            var optionalInstanceLink = instanceLinks.stream().filter(il -> il.equals(newInstanceLink)).findFirst();
            if (optionalInstanceLink.isPresent()) {
                var existingInstanceLink = optionalInstanceLink.get();
                var newNameMappings = newInstanceLink.getTextualInstance().getNameMappings();
                var newTypeMappings = newInstanceLink.getTextualInstance().getTypeMappings();
                existingInstanceLink.getTextualInstance().addMappings(newNameMappings, newTypeMappings);
            }
        }
    }

    /**
     * Checks if an instance link is already contained by the state.
     *
     * @param instanceLink the given instance link
     * @return true if it is already contained
     */
    @Override
    public boolean isContainedByInstanceLinks(InstanceLink instanceLink) {
        return instanceLinks.contains(instanceLink);
    }

    /**
     * Removes an instance link from the state
     *
     * @param instanceMapping the instance link to remove
     */
    @Override
    public void removeFromMappings(InstanceLink instanceMapping) {
        instanceLinks.remove(instanceMapping);
    }

    /**
     * Removes all instance links containing the given instance
     *
     * @param instance the given instance
     */
    @Override
    public void removeAllInstanceLinksWith(ModelInstance instance) {
        instanceLinks.removeIf(mapping -> mapping.getModelInstance().equals(instance));
    }

    /**
     * Removes all instance links containing the given recommended instance
     *
     * @param instance the given recommended instance
     */
    @Override
    public void removeAllInstanceLinksWith(RecommendedInstance instance) {
        instanceLinks.removeIf(mapping -> mapping.getTextualInstance().equals(instance));
    }

}
