/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants;

import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.isArchitectureModel;
import static edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel.isCodeModel;

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
            if (isArchitectureModel(metamodel)) {
                architectureModel = (ArchitectureModel) modelStates.getModel(metamodel);
            } else if (isCodeModel(metamodel)) {
                codeModel = (CodeModel) modelStates.getModel(metamodel);
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
