/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.Extractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.PcmExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.ArCoTLModelProviderInformant;

public class HoldBackArCoTLModelProvider {

    private final File inputArchitectureModel;
    private int currentHoldBackIndex = -1;
    private final ArchitectureModel initialModel;
    private final ImmutableList<ArchitectureComponent> components;

    public HoldBackArCoTLModelProvider(File inputArchitectureModel) {
        this.inputArchitectureModel = inputArchitectureModel;
        var model = this.getExtractor().extractModel();
        assert model instanceof ArchitectureModel;
        this.initialModel = (ArchitectureModel) model;
        this.components = Lists.immutable.fromStream(this.initialModel.getContent()
                .stream()
                .filter(ArchitectureComponent.class::isInstance)
                .map(it -> (ArchitectureComponent) it));
    }

    private Extractor getExtractor() {
        return new PcmExtractor(this.inputArchitectureModel.getAbsolutePath());
    }

    /**
     * Set the index of the element that should be hold back. Set the index to <0 if nothing should be held back.
     *
     * @param currentHoldBackIndex the index of the element to be hold back. If negative, nothing is held back
     */
    public void setCurrentHoldBackIndex(int currentHoldBackIndex) {
        this.currentHoldBackIndex = currentHoldBackIndex;
    }

    /**
     * Returns the number of actual instances (including all held back elements)
     *
     * @return the number of actual instances (including all held back elements)
     */
    public int numberOfActualInstances() {
        return this.components.size();
    }

    /**
     * Returns the ModelInstance that is held back. If nothing is held back, returns null
     *
     * @return the ModelInstance that is held back. If nothing is held back, returns null
     */
    public ArchitectureComponent getCurrentHoldBack() {
        if (this.currentHoldBackIndex < 0) {
            return null;
        }
        return this.components.get(this.currentHoldBackIndex);
    }

    public PipelineAgent get(SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        PipelineAgent agent = new PipelineAgent(List.of(new ArCoTLModelProviderInformant(dataRepository, new Extractor("") {
            @Override
            public Model extractModel() {
                var elements = new ArrayList<>(HoldBackArCoTLModelProvider.this.initialModel.getContent());
                var elementToRemove = HoldBackArCoTLModelProvider.this.getCurrentHoldBack();
                elements.remove(elementToRemove);
                return new ArchitectureModel(elements);
            }

            @Override
            public ModelType getModelType() {
                return ArchitectureModelType.PCM;
            }
        })), ArCoTLModelProviderAgent.class.getSimpleName(), dataRepository) {

            private static final long serialVersionUID = -816629373178698328L;

            @Override
            protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
                // empty
            }
        };
        agent.applyConfiguration(additionalConfigs);
        return agent;
    }

}
