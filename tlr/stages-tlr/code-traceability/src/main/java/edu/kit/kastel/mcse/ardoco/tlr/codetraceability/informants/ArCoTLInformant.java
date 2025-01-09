/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants;

import java.util.Arrays;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.TraceLinkGenerator;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.Node;

public class ArCoTLInformant extends Informant {
    public ArCoTLInformant(DataRepository dataRepository) {
        super(ArCoTLInformant.class.getSimpleName(), dataRepository);
    }

    private static boolean isACodeModel(Metamodel metamodel) {
        return Arrays.stream(CodeModelType.values()).anyMatch(codeModelType -> codeModelType.getMetamodel().equals(metamodel));
    }

    private static boolean isAnArchitectureModel(Metamodel metamodel) {
        return Arrays.stream(ArchitectureModelType.values()).anyMatch(architectureModelType -> architectureModelType.getMetamodel().equals(metamodel));
    }

    @Override
    public void process() {
        var dataRepository = this.getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var samCodeTraceabilityState = DataRepositoryHelper.getCodeTraceabilityState(dataRepository);

        ArchitectureModel architectureModel = null;
        CodeModel codeModel = null;
        for (var metamodel : modelStates.metamodels()) {
            if (ArCoTLInformant.isAnArchitectureModel(metamodel)) {
                architectureModel = (ArchitectureModel) modelStates.getModel(metamodel);
            } else if (ArCoTLInformant.isACodeModel(metamodel)) {
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
