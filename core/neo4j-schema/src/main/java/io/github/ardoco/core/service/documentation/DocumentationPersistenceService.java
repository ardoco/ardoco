package io.github.ardoco.core.service.documentation;

import io.github.ardoco.core.entities.documentation.PhraseNode;
import io.github.ardoco.core.entities.documentation.SentenceNode;
import io.github.ardoco.core.entities.documentation.TextNode;
import io.github.ardoco.core.entities.documentation.WordNode;
import io.github.ardoco.core.repository.documentation.TextNodeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.kit.kastel.mcse.ardoco.core.api.text.*;
import java.util.*;

@Service
public class DocumentationPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentationPersistenceService.class);

    private final TextNodeRepository textRepository;

    public DocumentationPersistenceService(TextNodeRepository textRepository) {
        this.textRepository = textRepository;
    }

    @Transactional
    public void saveDocumentation(Text domainText, String documentId) {
        TextNode textNode = new TextNode(documentId);
        Map<Integer, WordNode> wordIndexMap = new HashMap<>();

        // Cache to track converted phrases (Prevents cycles & duplication)
        Map<Phrase, PhraseNode> phraseCache = new HashMap<>();

        SentenceNode prevSentenceNode = null;

        for (Sentence domainSentence : domainText.getSentences()) {
            SentenceNode sentenceNode = new SentenceNode(
                    domainSentence.getSentenceNumber(),
                    domainSentence.getText()
            );

            if (prevSentenceNode != null) {
                prevSentenceNode.setNextSentence(sentenceNode);
            }
            textNode.addSentence(sentenceNode);

            WordNode prevWordNode = null;
            for (Word domainWord : domainSentence.getWords()) {
                WordNode wordNode = new WordNode(
                        domainWord.getPosition(),
                        domainWord.getText(),
                        domainWord.getLemma(),
                        domainWord.getPosTag().toString()
                );
                if (prevWordNode != null) {
                    prevWordNode.setNextWord(wordNode);
                }
                sentenceNode.getWords().add(wordNode);
                wordIndexMap.put(domainWord.getPosition(), wordNode);
                prevWordNode = wordNode;
            }

            for (Phrase domainPhrase : domainSentence.getPhrases()) {
                PhraseNode phraseNode = convertPhrase(domainPhrase, wordIndexMap, phraseCache);
                sentenceNode.getRootPhrases().add(phraseNode);
            }

            prevSentenceNode = sentenceNode;
        }
        textRepository.save(textNode);
        logger.info("Saved documentation for document ID to neo4j: {}", documentId);
    }

    private PhraseNode convertPhrase(Phrase domainPhrase,
            Map<Integer, WordNode> wordMap,
            Map<Phrase, PhraseNode> phraseCache) {

        logger.info("Converting phrase: {} of type: {}", domainPhrase.getText(), domainPhrase.getPhraseType());
        // cycle/ duplication detection
        if (phraseCache.containsKey(domainPhrase)) {
            return phraseCache.get(domainPhrase);
        }

        PhraseNode phraseNode = new PhraseNode(
                domainPhrase.getText(),
                domainPhrase.getPhraseType().toString()
        );

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
