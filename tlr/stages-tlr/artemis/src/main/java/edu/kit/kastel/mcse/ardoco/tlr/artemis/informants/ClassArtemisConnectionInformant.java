/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.artemis.informants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

/**
 * Artemis connection informant that links named architecture entities from the documentation to class entities in the code model.
 * <p>
 * The informant compares each recognized named architecture entity and its alternative names with the available class names in the configured code model. For
 * every matching class, it creates trace links from all textual occurrences of the named entity to the corresponding class entity and records named entities
 * that remain unlinked.
 * </p>
 */
public class ClassArtemisConnectionInformant extends ArtemisConnectionInformant {

    public ClassArtemisConnectionInformant(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(dataRepository, strategy);
    }

    protected List<ClassUnit> getClasses(CodeModelWithCompilationUnits codeModel) {
        return codeModel.getClasses();
    }

    @Override
    protected void process() {
        var state = DataRepositoryHelper.getArtemisConnectionStates(dataRepository).getState(strategy.getTarget());
        var codeModel = (CodeModelWithCompilationUnits) DataRepositoryHelper.getModelStatesData(dataRepository).getModel(strategy.getMetamodel());
        var classes = getClasses(codeModel);

        var namedEntities = state.getNamedEntities();
        var unlinkedNamedEntities = Lists.mutable.withAll(namedEntities);
        List<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks = new ArrayList<>();

        for (var namedEntity : namedEntities) {
            boolean matched = false;

            for (var clazz : classes) {
                if (areSimilar(namedEntity, clazz)) {
                    traceLinks.addAll(createTraceLinks(namedEntity, clazz));
                    matched = true;
                }
            }

            if (matched) {
                unlinkedNamedEntities.remove(namedEntity);
            }
        }

        state.addTraceLinks(Lists.mutable.ofAll(traceLinks));
        state.addUnlinkedNamedEntities(unlinkedNamedEntities);
    }

    private boolean areSimilar(NamedArchitectureEntity namedEntity, ModelEntity modelEndpoint) {
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
