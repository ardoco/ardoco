package edu.kit.kastel.mcse.ardoco.tlr.artemis.informants;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.naer.model.SoftwareArchitectureDocumentation;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.NamedEntityRecognizer;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisConnectionStatesImpl;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public class ArtemisNerInformant extends Informant {

    private final LargeLanguageModel llm;
    private final ArtemisNerStrategy strategy;

    public ArtemisNerInformant(DataRepository dataRepository, LargeLanguageModel llm, ArtemisNerStrategy strategy) {
        super(strategy.getId() + "NerInformant", dataRepository);
        this.llm = llm;
        this.strategy = strategy;
    }

    private static Set<NamedArchitectureEntity> recognizeNamedArchitectureEntities(NamedEntityRecognizer namedEntityRecognizer,
            SoftwareArchitectureDocumentation sad) {
        var namedEntities = namedEntityRecognizer.recognize(sad);

        MutableList<NamedEntity> namedEntitiesList = Lists.mutable.ofAll(namedEntities);
        namedEntitiesList.sortThisBy(NamedEntity::getName);

        return transformNamedEntitiesToNamedArchitectureEntities(namedEntitiesList);
    }

    private static Set<NamedArchitectureEntity> transformNamedEntitiesToNamedArchitectureEntities(MutableList<NamedEntity> namedEntities) {
        SortedSet<NamedArchitectureEntity> namedArchitectureEntities = new TreeSet<>();

        for (var namedEntity : namedEntities) {
            var name = namedEntity.getName();
            var alternativeNames = new TreeSet<>(namedEntity.getAlternativeNames());
            var occurrences = namedEntity.getOccurrenceLines();

            MutableList<NamedArchitectureEntityOccurrence> namedArchitectureEntityOccurrences = Lists.mutable.empty();
            for (var occurrence : occurrences) {
                namedArchitectureEntityOccurrences.add(new NamedArchitectureEntityOccurrence(name, occurrence));
            }

            namedArchitectureEntities.add(new NamedArchitectureEntity(name, alternativeNames, namedArchitectureEntityOccurrences));
        }

        return namedArchitectureEntities;
    }

    @Override
    protected void process() {
        var states = dataRepository.getData(ArtemisConnectionStates.ID, ArtemisConnectionStatesImpl.class).orElseThrow();
        var state = states.getState(strategy.getTarget());

        var text = DataRepositoryHelper.getSimpleText(dataRepository);
        SoftwareArchitectureDocumentation sad = new SoftwareArchitectureDocumentation(text.getText());

        var chatModel = llm.create();
        var namedEntityRecognizer = new NamedEntityRecognizer.Builder().chatModel(chatModel).prompt(strategy.createPrompt(dataRepository)).build();

        var namedArchitectureEntities = recognizeNamedArchitectureEntities(namedEntityRecognizer, sad);
        state.addNamedEntities(namedArchitectureEntities);
    }
}
