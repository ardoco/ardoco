/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import java.io.File;
import java.util.Optional;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.common.IdentifierProvider;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.Extractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.CodeExtractor;

/**
 * The model extractor extracts the instances and relations via a connector. The extracted items are stored in a model extraction state.
 */
public final class ArCoTLModelProviderInformant extends Informant {
    private static final String MODEL_STATES_DATA = "ModelStatesData";
    private final Extractor extractor;
    private final File fromFile;

    // Needed for Configuration Generation
    private ArCoTLModelProviderInformant() {
        super(null, null);
        this.extractor = null;
        this.fromFile = null;
    }

    /**
     * Instantiates a new model provider to extract information into the {@link DataRepository}.
     *
     * @param dataRepository the data repository
     * @param fromFile       whether the model should be read from a file
     */
    public ArCoTLModelProviderInformant(DataRepository dataRepository, File fromFile) {
        super("Extractor File", dataRepository);
        this.fromFile = fromFile;
        this.extractor = null;
    }

    /**
     * Instantiates a new model provider to extract information into the {@link DataRepository}.
     *
     * @param dataRepository the data repository
     * @param extractor      the model connector
     */
    public ArCoTLModelProviderInformant(DataRepository dataRepository, Extractor extractor) {
        super("Extractor " + (extractor == null ? "File" : extractor.getClass().getSimpleName()), dataRepository);
        this.extractor = extractor;
        this.fromFile = null;
    }

    @Override
    public void process() {
        Model extractedModel;

        if (this.fromFile != null) {
            extractedModel = CodeExtractor.readInCodeModel(this.fromFile);
            this.addModelStateToDataRepository(Metamodel.CODE, extractedModel);
            return;
        }

        IdentifierProvider.reset();
        this.getLogger().info("Extracting code model.");
        extractedModel = this.extractor.extractModel();
        if (this.extractor instanceof CodeExtractor codeExtractor && extractedModel instanceof CodeModel codeModel) {
            this.getLogger().info("Writing out code model to file in directory.");
            codeExtractor.writeOutCodeModel(codeModel);
        }
        this.addModelStateToDataRepository(this.extractor.getModelId(), extractedModel);
    }

    private void addModelStateToDataRepository(Metamodel modelId, Model model) {
        var dataRepository = this.getDataRepository();
        Optional<ModelStates> modelStatesOptional = dataRepository.getData(ArCoTLModelProviderInformant.MODEL_STATES_DATA, ModelStates.class);
        var modelStates = modelStatesOptional.orElseGet(ModelStates::new);

        modelStates.addModel(modelId, model);

        if (modelStatesOptional.isEmpty()) {
            dataRepository.addData(ArCoTLModelProviderInformant.MODEL_STATES_DATA, modelStates);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> map) {
        // empty
    }
}
