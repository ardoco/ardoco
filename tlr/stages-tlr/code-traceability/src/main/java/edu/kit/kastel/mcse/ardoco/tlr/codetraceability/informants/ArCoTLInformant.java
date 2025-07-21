/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants;

import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.TraceLinkGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.Node;

public class ArCoTLInformant extends Informant {
    public ArCoTLInformant(DataRepository dataRepository) {
        super(ArCoTLInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        var dataRepository = this.getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var samCodeTraceabilityState = DataRepositoryHelper.getCodeTraceabilityState(dataRepository);

        ArchitectureModel architectureModel = null;
        CodeModel codeModel = null;

        for (var metamodel : modelStates.getMetamodels()) {
            var model = modelStates.getModel(metamodel);
            if (model == null) {
                continue;
            }

            switch (model) {
                case ArchitectureModel arch -> {
                    if (architectureModel != null) {
                        throw new IllegalStateException("Multiple architecture models found in the data repository.");
                    }
                    architectureModel = arch;
                }
                case CodeModel code -> {
                    if (codeModel != null) {
                        throw new IllegalStateException("Multiple code models found in the data repository.");
                    }
                    codeModel = code;
                }
            }
        }

        Node root = TraceLinkGenerator.getRoot(); //TODO maybe add preprocessing
        var traceLinks = TraceLinkGenerator.generateTraceLinks(root, architectureModel, codeModel);
        samCodeTraceabilityState.addSamCodeTraceLinks(traceLinks);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // empty
    }

}
