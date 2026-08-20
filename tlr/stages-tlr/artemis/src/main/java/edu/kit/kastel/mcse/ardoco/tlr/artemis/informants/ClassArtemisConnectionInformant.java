package edu.kit.kastel.mcse.ardoco.tlr.artemis.informants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ClassArtemisConnectionInformant extends ArtemisConnectionInformant {

    public ClassArtemisConnectionInformant(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(dataRepository, strategy);
    }

    @Override
    protected void process() {
        var similarityUtils = SimilarityUtils.getInstance();

        var state = getTraceabilityState();
        var codeModel = (CodeModelWithCompilationUnits) getModelStatesData().getModel(strategy.getMetamodel());
        var classes = codeModel.getClasses();
        //var modelEndpointsClasses = ((List<CodeCompilationUnit>)modelEndpoints).stream().map(CodeCompilationUnit::getAllDataTypes).flatMap(List::stream).toList(); //bzw so

        var namedEntities = state.getNamedEntities();
        var unlinkedNamedEntities = Lists.mutable.withAll(namedEntities);
        List<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks = new ArrayList<>();

        for (var namedEntity : namedEntities) {
            boolean matched = false;

            for (var clazz : classes) {
                if (areSimilar(similarityUtils, namedEntity, clazz)) {
                    System.out.println("Matched: " + namedEntity.getName() + " <-> " + clazz.getName());
                    traceLinks.addAll(createTraceLinks(namedEntity, clazz));
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

    private boolean areSimilar(SimilarityUtils similarityUtils, NamedArchitectureEntity namedEntity, ModelEntity modelEndpoint) {
        if (namedEntity.getName().equalsIgnoreCase(modelEndpoint.getName())) {
            return true;
        }
        
        for (var alternativeName : namedEntity.getAlternativeNames()) {
            if (alternativeName.equalsIgnoreCase(modelEndpoint.getName())) {
                return true;
            }
        }

        return false;
    }
}
