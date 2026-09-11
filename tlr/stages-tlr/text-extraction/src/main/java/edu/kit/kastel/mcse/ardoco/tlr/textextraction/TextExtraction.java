/* Licensed under MIT 2021-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction;

import java.util.Collection;
import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.agents.InitialTextAgent;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.agents.PhraseAgent;

/**
 * The Class TextExtractor.
 */
public class TextExtraction extends AbstractExecutionStage {

    private static final Logger logger = LoggerFactory.getLogger(TextExtraction.class);

    /**
     * Instantiates a new text extractor.
     */
    public TextExtraction(DataRepository dataRepository) {
        super(List.of(new InitialTextAgent(dataRepository), new PhraseAgent(dataRepository)), "TextExtraction", dataRepository);
    }

    /**
     * Creates a {@link TextExtraction} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of InconsistencyChecker
     */
    public static TextExtraction get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var textExtractor = new TextExtraction(dataRepository);
        textExtractor.applyConfiguration(additionalConfigs);
        return textExtractor;
    }

    @Override
    protected void initializeState() {
        var dataRepository = this.getDataRepository();
        var optionalTextState = dataRepository.getData(TextState.ID, TextStateImpl.class);
        if (optionalTextState.isEmpty()) {
            var textState = new TextStateImpl();
            hydrateFromPersistenceIfPresent(dataRepository, textState);
            dataRepository.addData(TextState.ID, textState);
        }
    }

    /**
     * Load-on-resume: if Neo4j already has NounMappings and TextState is empty in memory, hydrate once.
     * Does not re-dual-write. Live extraction still writes via {@link TextStateImpl#addNounMapping}.
     */
    private static void hydrateFromPersistenceIfPresent(DataRepository dataRepository, TextStateImpl textState) {
        if (!PersistenceBridge.shouldPersistTextState()) {
            return;
        }
        var handler = PersistenceBridge.getHandler();
        if (handler == null) {
            return;
        }
        Boolean hasMappings = PersistenceBridge.callQuietly("hasNounMappings", handler::hasNounMappings, Boolean.FALSE);
        if (!Boolean.TRUE.equals(hasMappings)) {
            return;
        }
        if (!DataRepositoryHelper.hasAnnotatedText(dataRepository)) {
            logger.warn("Cannot load NounMappings from Neo4j: annotated text is not available yet");
            return;
        }
        Text annotatedText = DataRepositoryHelper.getAnnotatedText(dataRepository);
        Collection<NounMapping> loaded = PersistenceBridge.callQuietly("loadNounMappings", () -> handler.loadNounMappings(annotatedText),
                java.util.List.of());
        for (NounMapping mapping : loaded) {
            textState.addNounMapping(mapping, false);
        }
        if (!loaded.isEmpty()) {
            logger.info("Hydrated {} NounMappings from Neo4j into TextState (resume)", loaded.size());
        }
    }
}
