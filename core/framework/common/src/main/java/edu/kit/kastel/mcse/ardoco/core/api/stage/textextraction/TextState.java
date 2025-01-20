/* Licensed under MIT 2021-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction;

import org.eclipse.collections.api.block.predicate.Predicate;
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

    void removeNounMapping(DataRepository dataRepository, NounMapping nounMapping, NounMapping replacement, boolean cascade);

    void addPhraseMapping(PhraseMapping phraseMapping);

    /**
     * Removes the specified phrase mapping from the state and replaces it with an (optional) replacement
     */
    void removePhraseMapping(PhraseMapping phraseMapping, PhraseMapping replacement);

    PhraseMapping getPhraseMappingByNounMapping(NounMapping nounMapping);

    ImmutableList<NounMapping> getNounMappingsByPhraseMapping(PhraseMapping phraseMapping);

    ImmutableList<NounMapping> getNounMappings();

    ImmutableList<PhraseMapping> getPhraseMappings();

    default ImmutableList<NounMapping> getNounMappingsOfKind(MappingKind kind) {
        return this.getNounMappings().select(this.nounMappingIsOfKind(kind)).toImmutable();
    }

    default ImmutableList<NounMapping> getMappingsThatCouldBeOfKind(Word word, MappingKind kind) {
        return this.getNounMappingsByWord(word).select(mapping -> mapping.getProbabilityForKind(kind) > 0);
    }

    ImmutableList<NounMapping> getMappingsThatCouldBeMultipleKinds(Word word, MappingKind... kinds);

    default ImmutableList<NounMapping> getNounMappingsByWord(Word word) {
        return this.getNounMappings().select(nm -> nm.getWords().contains(word));
    }

    default ImmutableList<NounMapping> getNounMappingsByWordAndKind(Word word, MappingKind kind) {
        return this.getNounMappings().select(n -> n.getWords().contains(word)).select(this.nounMappingIsOfKind(kind)).toImmutable();
    }

    default boolean isWordContainedByMappingKind(Word word, MappingKind kind) {
        return this.getNounMappings().select(n -> n.getWords().contains(word)).anySatisfy(this.nounMappingIsOfKind(kind));
    }

    ImmutableList<NounMapping> getNounMappingsWithSimilarReference(String reference);

    private Predicate<? super NounMapping> nounMappingIsOfKind(MappingKind mappingKind) {
        return n -> n.getKind() == mappingKind;
    }

}
