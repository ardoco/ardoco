package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.model.SoftwareArchitectureDocumentation;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.NamedEntityRecognizer;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.TwoPartPrompt;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.NerConnectionStatesImpl;

public class NerInformant extends Informant {

    public NerInformant(DataRepository dataRepository) {
        super(NerInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        var nerConnectionStates = (NerConnectionStatesImpl) DataRepositoryHelper.getNerConnectionStates(dataRepository);

        var text = DataRepositoryHelper.getSimpleText(dataRepository);
        SoftwareArchitectureDocumentation sad = new SoftwareArchitectureDocumentation(text.getText());

        var llmSettings = nerConnectionStates.getLlmSettings();
        var chatModel = llmSettings.createChatModel();
        var prompt = TwoPartPrompt.getDefault();
        var namedEntityRecognizer = new NamedEntityRecognizer.Builder().chatModel(chatModel).prompt(prompt).build();

        var modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        for (var metamodel : modelStatesData.getMetamodels()) {
            var possibleEntities = getPossibleEntities(metamodel);
            var namedArchitectureEntities = recognizeNamedArchitectureEntities(namedEntityRecognizer, sad, possibleEntities);

            var nerConnectionState = nerConnectionStates.getNerConnectionState(metamodel);
            nerConnectionState.addNamedEntities(namedArchitectureEntities);
        }
    }

    private static Set<NamedArchitectureEntity> recognizeNamedArchitectureEntities(NamedEntityRecognizer namedEntityRecognizer,
            SoftwareArchitectureDocumentation sad, Map<NamedEntityType, Set<String>> possibleEntities) {
        Set<NamedEntity> namedEntities = namedEntityRecognizer.recognize(sad, possibleEntities);
        return transformNamedEntitiesToNamedArchitectureEntities(namedEntities);
    }

    @NotNull
    private static Set<NamedArchitectureEntity> transformNamedEntitiesToNamedArchitectureEntities(Set<NamedEntity> namedEntities) {
        Set<NamedArchitectureEntity> namedArchitectureEntities = new TreeSet<>();
        for (var namedEntity : namedEntities) {
            var name = namedEntity.getName();
            var alternativeNames = namedEntity.getAlternativeNames();
            var occurrences = namedEntity.getOccurrenceLines();

            MutableList<NamedArchitectureEntityOccurrence> namedArchitectureEntityOccurrences = getNamedArchitectureEntityOccurrences(name, occurrences);

            var namedArchitectureEntity = new NamedArchitectureEntity(name, alternativeNames, namedArchitectureEntityOccurrences);
            namedArchitectureEntities.add(namedArchitectureEntity);
        }
        return namedArchitectureEntities;
    }

    private static MutableList<NamedArchitectureEntityOccurrence> getNamedArchitectureEntityOccurrences(String name, Set<Integer> occurrences) {
        MutableList<NamedArchitectureEntityOccurrence> namedArchitectureEntityOccurrences = Lists.mutable.of();
        for (var occurrence : occurrences) {
            var namedArchitectureEntityOccurrence = new NamedArchitectureEntityOccurrence(name, occurrence);
            namedArchitectureEntityOccurrences.add(namedArchitectureEntityOccurrence);
        }
        return namedArchitectureEntityOccurrences;
    }

    private Map<NamedEntityType, Set<String>> getPossibleEntities(Metamodel metamodel) {
        Map<NamedEntityType, Set<String>> possibleEntities = new EnumMap<>(NamedEntityType.class);
        for (var type : NamedEntityType.values()) {
            possibleEntities.put(type, new TreeSet<>());
        }

        var modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        var model = modelStatesData.getModel(metamodel);
        for (var endpoint : model.getEndpoints()) {
            String endpointName = endpoint.getName();
            NamedEntityType namedEntityType = getNamedEntityType(endpoint);
            possibleEntities.get(namedEntityType).add(endpointName);
        }

        return possibleEntities;
    }

    @NotNull
    private static NamedEntityType getNamedEntityType(ModelEntity endpoint) {
        NamedEntityType namedEntityType = NamedEntityType.COMPONENT;
        var type = endpoint.getType();
        if (type.isPresent()) {
            String typeName = type.get().toLowerCase();
            if (typeName.contains("interface")) {
                namedEntityType = NamedEntityType.INTERFACE;
            }
        }
        return namedEntityType;
    }
}
