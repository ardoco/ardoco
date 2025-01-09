/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.CodeTraceabilityStateImpl;

@Deterministic
public class ArchitectureLinkToCodeLinkTransformerInformant extends Informant {

    public ArchitectureLinkToCodeLinkTransformerInformant(DataRepository dataRepository) {
        super(ArchitectureLinkToCodeLinkTransformerInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        MutableSet<SadCodeTraceLink> sadCodeTracelinks = Sets.mutable.empty();

        ModelStates modelStatesData = DataRepositoryHelper.getModelStatesData(this.getDataRepository());
        ConnectionStates connectionStates = DataRepositoryHelper.getConnectionStates(this.getDataRepository());
        if (modelStatesData == null || connectionStates == null) {
            return;
        }

        CodeModel codeModel = this.findCodeModel(modelStatesData);

        for (var traceLink : connectionStates.getConnectionState(Metamodel.CODE).getTraceLinks()) {
            var modelElement = traceLink.getSecondEndpoint().getId();
            var mentionedCodeModelElements = this.findMentionedCodeModelElementsById(modelElement, codeModel);
            for (var mid : mentionedCodeModelElements) {
                sadCodeTracelinks.add(new SadCodeTraceLink(traceLink.getFirstEndpoint(), mid));
            }
        }

        CodeTraceabilityState codeTraceabilityState = new CodeTraceabilityStateImpl();
        this.getDataRepository().addData(CodeTraceabilityState.ID, codeTraceabilityState);
        codeTraceabilityState.addSadCodeTraceLinks(sadCodeTracelinks);
    }

    private List<CodeCompilationUnit> findMentionedCodeModelElementsById(String modelElementId, CodeModel codeModel) {
        boolean isPackage = modelElementId.endsWith("/");
        if (isPackage) {
            return this.findAllClassesInPackage(modelElementId, codeModel);
        }
        return this.findCompilationUnitById(modelElementId, codeModel);
    }

    private List<CodeCompilationUnit> findAllClassesInPackage(String modelElementId, CodeModel codeModel) {
        List<CodeCompilationUnit> codeCompilationUnits = new ArrayList<>();
        for (var codeCompilationUnit : codeModel.getEndpoints()) {
            var path = codeCompilationUnit.getPath();
            if (path.contains(modelElementId)) {
                codeCompilationUnits.add(codeCompilationUnit);
            }
        }
        if (codeCompilationUnits.isEmpty()) {
            throw new IllegalStateException("Could not find any code for " + modelElementId);
        }
        return codeCompilationUnits;
    }

    private List<CodeCompilationUnit> findCompilationUnitById(String modelElementId, CodeModel codeModel) {
        for (var codeCompilationUnit : codeModel.getEndpoints()) {
            if (codeCompilationUnit.getPath().equals(modelElementId)) {
                return List.of(codeCompilationUnit);
            }
        }
        throw new IllegalStateException("Could not find model element " + modelElementId);
    }

    private CodeModel findCodeModel(ModelStates models) {
        for (var metamodel : models.metamodels()) {
            var model = models.getModel(metamodel);
            if (model instanceof CodeModel codeModel) {
                return codeModel;
            }
        }
        return null;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // empty
    }
}
