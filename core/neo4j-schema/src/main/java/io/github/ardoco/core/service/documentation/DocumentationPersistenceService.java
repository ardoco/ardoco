package io.github.ardoco.core.service.documentation;

import io.github.ardoco.core.entities.documentation.PhraseNode;
import io.github.ardoco.core.entities.documentation.SentenceNode;
import io.github.ardoco.core.entities.documentation.TextNode;
import io.github.ardoco.core.entities.documentation.WordNode;
import io.github.ardoco.core.repository.documentation.TextNodeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.kit.kastel.mcse.ardoco.core.api.text.*;
import java.util.*;

@Service
public class DocumentationPersistenceService {

    private final TextNodeRepository textRepository;

    public DocumentationPersistenceService(TextNodeRepository textRepository) {
        this.textRepository = textRepository;
    }

    @Transactional
    public void saveDocumentation(Text domainText, String documentId) {
        // 1. Create Root
        TextNode textNode = new TextNode(documentId);

        // Maps to keep track of created nodes to maintain relationships
        Map<Integer, WordNode> wordIndexMap = new HashMap<>();

        SentenceNode prevSentenceNode = null;

        // 2. Iterate Sentences
        for (Sentence domainSentence : domainText.getSentences()) {
            SentenceNode sentenceNode = new SentenceNode(
                    domainSentence.getSentenceNumber(),
                    domainSentence.getText()
            );

            // Link Sentences (Doubly linked list logic handled by maintaining 'next')
            if (prevSentenceNode != null) {
                prevSentenceNode.setNextSentence(sentenceNode);
            }
            textNode.addSentence(sentenceNode);

            // 3. Process Words
            WordNode prevWordNode = null;
            for (Word domainWord : domainSentence.getWords()) {
                WordNode wordNode = new WordNode(
                        domainWord.getPosition(),
                        domainWord.getText(),
                        domainWord.getLemma(),
                        domainWord.getPosTag().toString()
                );

                // Link Words
                if (prevWordNode != null) {
                    prevWordNode.setNextWord(wordNode);
                }

                // Add to Sentence and Index
                sentenceNode.getWords().add(wordNode);
                wordIndexMap.put(domainWord.getPosition(), wordNode);

                prevWordNode = wordNode;
            }

            // 4. Process Phrases (Recursive)
            // Note: ArDoCo Sentence.getPhrases() typically returns top-level phrases.
            // If it returns flat list, you might need to filter for root phrases only.
            for (Phrase domainPhrase : domainSentence.getPhrases()) {
                // Convert only if it's a root phrase (not contained in another phrase in this list)
                // Or typically, just convert the hierarchy provided by the domain object
                PhraseNode phraseNode = convertPhrase(domainPhrase, wordIndexMap);
                sentenceNode.getRootPhrases().add(phraseNode);
            }

            prevSentenceNode = sentenceNode;
        }

        // 5. Save the entire graph starting from Text
        textRepository.save(textNode);
    }

    private PhraseNode convertPhrase(Phrase domainPhrase, Map<Integer, WordNode> wordMap) {
        PhraseNode phraseNode = new PhraseNode(
                domainPhrase.getText(),
                domainPhrase.getPhraseType().toString()
        );

        // Link Words
        for (Word containedWord : domainPhrase.getContainedWords()) {
            if (wordMap.containsKey(containedWord.getPosition())) {
                phraseNode.addContainedWord(wordMap.get(containedWord.getPosition()));
            }
        }

        // Recursively Link Subphrases
        for (Phrase subPhrase : domainPhrase.getSubphrases()) {
            PhraseNode childNode = convertPhrase(subPhrase, wordMap);
            phraseNode.addChildPhrase(childNode);
        }

        return phraseNode;
    }
}
