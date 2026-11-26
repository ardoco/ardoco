/* Licensed under MIT 2021-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.common.IdentifierProvider;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.Extractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.CodeExtractor;

/**
 * The model extractor extracts the instances and relations via a connector. The extracted items are stored in a model extraction state.
 */
public final class ModelProviderInformant extends Informant {
    private static final String MODEL_STATES_DATA = "ModelStatesData";

    private final ModelSource source;

    /**
     * Instantiates a new model provider to extract information into the {@link DataRepository}.
     *
     * @param dataRepository the data repository
     * @param fromFile       the file the code model is read from
     * @param metamodel      the metamodel of the code model in the file
     */
    public ModelProviderInformant(DataRepository dataRepository, File fromFile, Metamodel metamodel) {
        super("Extractor File", dataRepository);
        this.source = new FromFile(Objects.requireNonNull(fromFile), Objects.requireNonNull(metamodel));
    }

    /**
     * Instantiates a new model provider to extract information into the {@link DataRepository}.
     *
     * @param dataRepository the data repository
     * @param extractor      the model connector
     */
    public ModelProviderInformant(DataRepository dataRepository, Extractor extractor) {
        super("Extractor " + extractor.getClass().getSimpleName(), dataRepository);
        this.source = new FromExtractor(extractor);
    }

    @Override
    public void process() {
        switch (this.source) {
            case FromFile(File file, Metamodel metamodel) -> {
                Model extractedModel = Objects.requireNonNull(CodeExtractor.readInCodeModel(file, metamodel), "The code model could not be read from file");
                this.addModelStateToDataRepository(extractedModel.getMetamodel(), extractedModel);
            }
            case FromExtractor(Extractor extractor) -> {
                IdentifierProvider.reset(extractor.getIdPrefix());
                this.getLogger().info("Extracting code model.");
                Model extractedModel = extractor.extractModel();
                this.addModelStateToDataRepository(extractedModel.getMetamodel(), extractedModel);
                if (extractor instanceof CodeExtractor codeExtractor && extractedModel instanceof CodeModel codeModel) {
                    this.getLogger().info("Writing out code model to file in directory.");
                    codeExtractor.writeOutCodeModel(codeModel);
                }
                this.addModelStateToDataRepository(extractor.getMetamodel(), extractedModel);
            }
        }
    }

    private void addModelStateToDataRepository(Metamodel metamodel, Model model) {
        var dataRepository = this.getDataRepository();
        Optional<ModelStates> modelStatesOptional = dataRepository.getData(ModelProviderInformant.MODEL_STATES_DATA, ModelStates.class);
        var modelStates = modelStatesOptional.orElseGet(ModelStates::new);

        modelStates.addModel(metamodel, model);

        if (modelStatesOptional.isEmpty()) {
            dataRepository.addData(ModelProviderInformant.MODEL_STATES_DATA, modelStates);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(ImmutableSortedMap<String, String> map) {
        // empty
    }

    /**
     * Where the model comes from: read from a file or extracted by an {@link Extractor}.
     */
    private sealed interface ModelSource permits FromFile, FromExtractor {
    }

    private record FromFile(File file, Metamodel metamodel) implements ModelSource {
    }

    private record FromExtractor(Extractor extractor) implements ModelSource {
    }
}
