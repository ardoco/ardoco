/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.naer.model.SoftwareArchitectureDocumentation;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.NamedEntityRecognizer;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.NerConnectionStatesImpl;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.strategies.NerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

@Deterministic("Currently not fully deterministic due to NAER.")
public class NerInformant extends Informant {

    private final LargeLanguageModel llm;
    private final NerStrategy strategy;

    public NerInformant(DataRepository dataRepository, LargeLanguageModel llm, NerStrategy strategy) {
        super(NerInformant.class.getSimpleName(), dataRepository);
        this.llm = llm;
        this.strategy = strategy;
    }

    @Override
    public void process() {
        var nerConnectionStates = DataRepositoryHelper.getNerConnectionStates(dataRepository).asPipelineStepData(NerConnectionStatesImpl.class).orElseThrow();

        var text = DataRepositoryHelper.getSimpleText(dataRepository);
        SoftwareArchitectureDocumentation sad = new SoftwareArchitectureDocumentation(text.getText());

        var chatModel = llm.create();

        var namedEntityRecognizer = new NamedEntityRecognizer.Builder().chatModel(chatModel).prompt(strategy.getPrompt(dataRepository)).build();
        var namedArchitectureEntities = recognizeNamedArchitectureEntities(namedEntityRecognizer, sad);

        var nerConnectionState = nerConnectionStates.getNerConnectionState(strategy.getMetamodel());
        nerConnectionState.addNamedEntities(namedArchitectureEntities);
    }

    private static Set<NamedArchitectureEntity> recognizeNamedArchitectureEntities(NamedEntityRecognizer namedEntityRecognizer,
            SoftwareArchitectureDocumentation sad) {
        // TODO This is not fully deterministic .. as the hashset has a random order. This should be fixed in NAER.
        var namedEntities = namedEntityRecognizer.recognize(sad);

        MutableList<NamedEntity> namedEntitiesList = Lists.mutable.ofAll(namedEntities);
        // Sort the named entities by their name to ensure a deterministic order
        namedEntitiesList.sortThisBy(NamedEntity::getName);
        return transformNamedEntitiesToNamedArchitectureEntities(namedEntitiesList);
    }

    private static Set<NamedArchitectureEntity> transformNamedEntitiesToNamedArchitectureEntities(MutableList<NamedEntity> namedEntities) {
        SortedSet<NamedArchitectureEntity> namedArchitectureEntities = new TreeSet<>();
        for (var namedEntity : namedEntities) {
            var name = namedEntity.getName();
            var alternativeNames = new TreeSet<>(namedEntity.getAlternativeNames());
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
}
