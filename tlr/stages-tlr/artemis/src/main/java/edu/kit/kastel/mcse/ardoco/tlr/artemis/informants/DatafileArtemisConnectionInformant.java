package edu.kit.kastel.mcse.ardoco.tlr.artemis.informants;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeFile;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class DatafileArtemisConnectionInformant extends ArtemisConnectionInformant {

    public DatafileArtemisConnectionInformant(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(dataRepository, strategy);
    }

    protected List<CodeFile> getCodeFiles(CodeModelWithCompilationUnits codeModel) {
        return codeModel.getCodeFiles();
    }

    @Override
    protected void process() {
        var state = getConnectionState();
        var codeModel = (CodeModelWithCompilationUnits) getModelStatesData().getModel(strategy.getMetamodel());
        var datafiles = getCodeFiles(codeModel);

        var namedEntities = state.getNamedEntities();
        var unlinkedNamedEntities = Lists.mutable.withAll(namedEntities);
        List<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks = new ArrayList<>();

        for (var namedEntity : namedEntities) {
            boolean matched = false;

            for (var datafile : datafiles) {
                if (areSimilar(namedEntity, datafile)) {
                    traceLinks.addAll(createTraceLinks(namedEntity, datafile));
                    matched = true;
                }
            }

            if (matched) {
                unlinkedNamedEntities.remove(namedEntity);
            }
        }

        state.addTraceLinks(traceLinks);
        state.addUnlinkedNamedEntities(unlinkedNamedEntities);
    }

    private boolean areSimilar(NamedArchitectureEntity namedEntity, CodeFile modelEndpoint) {
        return namedEntity.getName().equalsIgnoreCase(modelEndpoint.getName() + "." + modelEndpoint.getExtension());
    }
}
