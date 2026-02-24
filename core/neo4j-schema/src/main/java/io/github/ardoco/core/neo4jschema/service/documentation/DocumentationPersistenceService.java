/* Licensed under MIT 2023-2026. */
package io.github.ardoco.core.neo4jschema.service.documentation;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public Text loadPreprocessedText(String identifier) {
        boolean exists = textRepository.existsByArdocoId(identifier);
        logger.info("Checking existence of preprocessed text for identifier {}: {}", identifier, exists);
        if (!exists) {
            logger.warn("No preprocessed text found for identifier: {}", identifier);
            return null;
        }
        TextNode textNode = textRepository.findByArdocoId(identifier);
        logger.info("loaded documentation for document ID from neo4j: {}", identifier);
        DocumentationMapper mapper = new DocumentationMapper();
        return mapper.mapToDomain(textNode);
    }

    @Transactional
    public void savePreprocessedText(Text domainText, String documentId) {
        TextNode textNode = new TextNode(documentId);
        Map<Integer, WordNode> wordIndexMap = new HashMap<>(); // Global Map: Position -> Node
        Map<Phrase, PhraseNode> phraseCache = new HashMap<>();

        SentenceNode prevSentenceNode = null;

        for (Sentence domainSentence : domainText.getSentences()) {
            SentenceNode sentenceNode = new SentenceNode(domainSentence.getSentenceNumber(), domainSentence.getText());
            textNode.addSentence(sentenceNode);

            if (prevSentenceNode != null) {
                prevSentenceNode.setNextSentence(sentenceNode);
            }

            WordNode prevWordNode = null;
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
