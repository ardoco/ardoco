/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CoarseGrainedCodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModule;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
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

        CoarseGrainedCodeModel coarseGrainedCodeModel = this.findCoarseGrainedCodeModel(modelStatesData);

        for (var traceLink : connectionStates.getConnectionState(Metamodel.CODE_AS_ARCHITECTURE).getTraceLinks()) {
            var modelElement = traceLink.getSecondEndpoint();
            var mentionedCodeModelElements = this.findMentionedCodeModelElementsById(modelElement, coarseGrainedCodeModel);
            for (var mid : mentionedCodeModelElements) {
                sadCodeTracelinks.add(new SadCodeTraceLink(traceLink.getFirstEndpoint(), mid));
            }
        }

        CodeTraceabilityState codeTraceabilityState = new CodeTraceabilityStateImpl();
        this.getDataRepository().addData(CodeTraceabilityState.ID, codeTraceabilityState);
        codeTraceabilityState.addSadCodeTraceLinks(sadCodeTracelinks);
    }

    private String retrievePath(CodePackage codePackage) {
        StringBuilder path = new StringBuilder(codePackage.getName());
        CodeModule parent = codePackage.getParent();

        while (parent instanceof CodePackage) {
            path.insert(0, parent.getName() + "/");
            parent = parent.getParent();
        }
        path.append("/");
        return path.toString();
    }

    private List<CodeCompilationUnit> findMentionedCodeModelElementsById(ModelEntity modelElement, CoarseGrainedCodeModel coarseGrainedCodeModel) {

        if (modelElement instanceof CodePackage codePackage) {
            String packagePath = retrievePath(codePackage);
            return this.findAllClassesInPackage(packagePath, coarseGrainedCodeModel);
        }
        return this.findCompilationUnitById(modelElement.getId(), coarseGrainedCodeModel);
    }

    private List<CodeCompilationUnit> findAllClassesInPackage(String packagePath, CoarseGrainedCodeModel coarseGrainedCodeModel) {
        List<CodeCompilationUnit> codeCompilationUnits = new ArrayList<>();
        List<CodeCompilationUnit> allCodeCompilationUnits = coarseGrainedCodeModel.getEndpoints()
                .stream()
                .filter(endpoint -> endpoint instanceof CodeCompilationUnit)
                .map(endpoint -> (CodeCompilationUnit) endpoint)
                .toList();

        for (var codeCompilationUnit : allCodeCompilationUnits) {
            var path = codeCompilationUnit.getPath();
            if (path.contains(packagePath)) {
                codeCompilationUnits.add(codeCompilationUnit);
            }
        }
        if (codeCompilationUnits.isEmpty()) {
            throw new IllegalStateException("Could not find any code for " + packagePath);
        }
        return codeCompilationUnits;
    }

    private List<CodeCompilationUnit> findCompilationUnitById(String modelElementId, CoarseGrainedCodeModel coarseGrainedCodeModel) {
        for (var entity : coarseGrainedCodeModel.getEndpoints()) {
            if (entity instanceof CodeCompilationUnit codeCompilationUnit) {
                if (codeCompilationUnit.getId().equals(modelElementId)) {
                    return List.of(codeCompilationUnit);
                }
            }
        }
        throw new IllegalStateException("Could not find model element " + modelElementId);
    }

    private CoarseGrainedCodeModel findCoarseGrainedCodeModel(ModelStates models) {
        for (var metamodel : models.getMetamodels()) {
            var model = models.getModel(metamodel);
            if (model instanceof CoarseGrainedCodeModel codeModel) {
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
