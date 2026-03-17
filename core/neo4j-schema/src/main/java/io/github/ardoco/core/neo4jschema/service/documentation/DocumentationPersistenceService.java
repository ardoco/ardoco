/* Licensed under MIT 2023-2026. */
package io.github.ardoco.core.neo4jschema.service.documentation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import io.github.ardoco.core.neo4jschema.entities.documentation.PhraseNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.TextNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.WordNode;
import io.github.ardoco.core.neo4jschema.repository.documentation.TextNodeRepository;

@Service
public class DocumentationPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentationPersistenceService.class);

    private final TextNodeRepository textRepository;

    public DocumentationPersistenceService(TextNodeRepository textRepository) {
        this.textRepository = textRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasPreprocessedText(String identifier) {
        return textRepository.existsByArdocoId(identifier);
    }

    @Nullable
    @Transactional(readOnly = true)
    public Text loadPreprocessedText(String identifier) {
//        boolean exists = textRepository.existsByArdocoId(identifier);
//        logger.info("Checking existence of preprocessed text for identifier {}: {}", identifier, exists);
//        if (!exists) {
//            logger.warn("No preprocessed text found for identifier: {}", identifier);
//            return null;
//        }
        Optional<TextNode> textNode = textRepository.findByArdocoId(identifier);
        if (textNode.isEmpty()) {
            logger.warn("No preprocessed text found for identifier: {}", identifier);
            return null;
        }

        DocumentationMapper mapper = new DocumentationMapper();
        return mapper.mapToDomain(textNode.get());
    }

    public Text loadPreprocessedTextBySentence(SentenceNode sentenceNode) {
        TextNode textNode = textRepository.findTextBySentenceId(sentenceNode.getSentenceNumber());
        DocumentationMapper mapper = new DocumentationMapper();
        return mapper.mapToDomain(textNode);
    }

    @Transactional(readOnly = true)
    @Nullable
    public Text loadPreprocessedText() {
        return textRepository.findByArdocoId("PreprocessingData") // TODO: this requires that the preprocessing data is always stored with this ID, which is not ideal. Consider a more flexible approach.
                .map(new DocumentationMapper()::mapToDomain)
                .orElseGet(() -> {
                    logger.warn("No preprocessed text found in database!");
                    return null; // Or return an empty Text object
                });
//        TextNode textNode = textRepository.findByArdocoId(PreprocessingData.ID);
//        logger.info("loaded documentation for document ID from neo4j: {}", PreprocessingData.ID);
//        DocumentationMapper mapper = new DocumentationMapper();
//        return mapper.mapToDomain(textNode);
    }

    @Transactional
    public void savePreprocessedText(Text domainText, String documentId) {

        Optional<TextNode> existingNode = textRepository.findByArdocoId(documentId);
        if (existingNode.isPresent()) {
            logger.info("Existing documentation found for document ID: {}. It will be deleted and replaced.", documentId);
            Long deletedCount = textRepository.deleteByArdocoId(documentId);
            long numDeleted = (deletedCount != null) ? deletedCount : 0L;
            logger.info("Deleted {} existing nodes for document ID: {}", numDeleted, documentId);
        }

        TextNode textNode = new TextNode(documentId);
        Map<Integer, WordNode> wordIndexMap = new HashMap<>(); // Global Map: Position -> Node
        Map<Phrase, PhraseNode> phraseCache = new HashMap<>();

        SentenceNode prevSentenceNode = null;
        WordNode prevWordNode = null;
        for (Sentence domainSentence : domainText.getSentences()) {
            SentenceNode sentenceNode = new SentenceNode(domainSentence.getSentenceNumber(), domainSentence.getText());
            textNode.addSentence(sentenceNode);

            if (prevSentenceNode != null) {
                prevSentenceNode.setNextSentence(sentenceNode);
            }

            for (Word domainWord : domainSentence.getWords()) {
                WordNode wordNode = new WordNode(domainWord.getPosition(), domainWord.getText(), domainWord.getLemma(), domainWord.getPosTag().toString());

                sentenceNode.getWords().add(wordNode);
                wordIndexMap.put(domainWord.getPosition(), wordNode);

                if (prevWordNode != null) {
                    prevWordNode.setNextWord(wordNode);
                }
                prevWordNode = wordNode;
            }

            for (Phrase domainPhrase : domainSentence.getPhrases()) {
                PhraseNode phraseNode = convertPhrase(domainPhrase, wordIndexMap, phraseCache);
                sentenceNode.getRootPhrases().add(phraseNode);
            }

            prevSentenceNode = sentenceNode;
        }

        createDependencyLinks(domainText, wordIndexMap);

        textRepository.save(textNode);
        logger.info("Saved documentation for document ID to neo4j: {}", documentId);
    }

    private void createDependencyLinks(Text domainText, Map<Integer, WordNode> wordMap) {
        for (Word word : domainText.words()) {
            WordNode sourceNode = wordMap.get(word.getPosition());
            if (sourceNode == null)
                continue;

            for (DependencyTag tag : DependencyTag.values()) {
                for (Word target : word.getOutgoingDependencyWordsWithType(tag)) {
                    WordNode targetNode = wordMap.get(target.getPosition());
                    if (targetNode != null) {
                        // Assuming addDependency(type, target) exists on WordNode
                        sourceNode.addDependency(tag.name(), targetNode);
                    }
                }
            }
        }
    }

    private PhraseNode convertPhrase(Phrase domainPhrase, Map<Integer, WordNode> wordMap, Map<Phrase, PhraseNode> phraseCache) {
        if (phraseCache.containsKey(domainPhrase)) {
            return phraseCache.get(domainPhrase);
        }

        PhraseNode phraseNode = new PhraseNode(domainPhrase.getText(), domainPhrase.getPhraseType().toString());
        phraseCache.put(domainPhrase, phraseNode);

        for (Word containedWord : domainPhrase.getContainedWords()) {
            if (wordMap.containsKey(containedWord.getPosition())) {
                phraseNode.addContainedWord(wordMap.get(containedWord.getPosition()));
            }
        }

        for (Phrase subPhrase : domainPhrase.getSubphrases()) {
            PhraseNode childNode = convertPhrase(subPhrase, wordMap, phraseCache);
            phraseNode.addChildPhrase(childNode);
        }

        return phraseNode;
    }
}
