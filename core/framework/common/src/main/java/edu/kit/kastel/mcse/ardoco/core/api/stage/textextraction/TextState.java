/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction;

import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * The Interface ITextState.
 */
public interface TextState extends IConfigurable, PipelineStepData {
    Logger logger = LoggerFactory.getLogger(TextState.class);

    String ID = "TextState";

    void addNounMapping(NounMapping nounMapping);

    void addPhraseMapping(PhraseMapping phraseMapping);

    /**
     * Removes a noun mapping from the state. Also removes phrase mappings that are associated with the noun mapping.
     *
     * @param dataRepository the data repository to sync the states after deleting
     * @param nounMapping    noun mapping to remove
     * @param replacement    the (optional) future replacement of the noun mapping
     */
    void removeNounMapping(DataRepository dataRepository, NounMapping nounMapping, NounMapping replacement);

    /**
     * Removes the specified phrase mapping from the state and replaces it with an (optional) replacement
     *
     * @param phraseMapping the mapping
     * @param replacement   the replacement
     * @return true if removed, false otherwise
     */
    boolean removePhraseMapping(PhraseMapping phraseMapping, PhraseMapping replacement);

    PhraseMapping getPhraseMappingByNounMapping(NounMapping nounMapping);

    ImmutableList<NounMapping> getNounMappingsByPhraseMapping(PhraseMapping phraseMapping);

    /**
     * Gets the all noun mappings.
     *
     * @return the all mappings
     */
    ImmutableList<NounMapping> getNounMappings();

    ImmutableList<PhraseMapping> getPhraseMappings();

    ImmutableList<NounMapping> getNounMappingsOfKind(MappingKind mappingKind);

    ImmutableList<NounMapping> getMappingsThatCouldBeOfKind(Word word, MappingKind kind);

    ImmutableList<NounMapping> getMappingsThatCouldBeMultipleKinds(Word word, MappingKind... kinds);

    ImmutableList<NounMapping> getNounMappingsByWord(Word word);

    ImmutableList<NounMapping> getNounMappingsByWordAndKind(Word word, MappingKind kind);

    boolean isWordContainedByMappingKind(Word word, MappingKind kind);

    ImmutableList<NounMapping> getNounMappingsWithSimilarReference(String reference);
}
