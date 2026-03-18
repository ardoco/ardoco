/* Licensed under MIT 2023-2026. */
package io.github.ardoco.core.neo4jschema.service.documentation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jPhrase;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jSentence;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jText;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jWord;
import io.github.ardoco.core.neo4jschema.entities.documentation.DependencyRelationship;
import io.github.ardoco.core.neo4jschema.entities.documentation.PhraseNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.TextNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.WordNode;

@Component
public class DocumentationMapper {

    /**
     * Converts a domain Text object into a TextNode entity for persistence. (For Saving) This method builds the hierarchical structure of sentences, words, and
     * phrases, and also establishes dependency links between words.
     *
     * @param domainText The domain Text object to convert.
     * @param documentId The identifier to associate with the created TextNode entity.
     * @return A TextNode entity representing the provided domain Text, ready for Neo4j persistence.
     */
    public TextNode toEntity(Text domainText, String documentId) {
        TextNode textNode = new TextNode(documentId);
        Map<Integer, WordNode> wordMap = new HashMap<>();
        Map<Phrase, PhraseNode> phraseCache = new HashMap<>();

        toEntityHelperBuildTextHierarchy(domainText, textNode, wordMap, phraseCache);
        toEntityHelperCreateDependencyLinks(domainText, wordMap);

        return textNode;
    }

    /**
     * Converts a TextNode entity from the database into a domain Text object. (For Loading) This method reconstructs the domain Text structure, including
     * sentences, words, phrases, and their relationships, based on the data stored in the TextNode and its related entities.
     *
     * @param textNode The TextNode entity to convert.
     * @return A domain Text object representing the provided TextNode entity.
     */
    public Text toDomain(TextNode textNode) {
        Map<Integer, Neo4jWord> globalWordMap = new HashMap<>();
        List<Neo4jSentence> sentences = textNode.getSentences()
                .stream()
                .sorted(Comparator.comparingInt(SentenceNode::getSentenceNumber))
                .map(sNode -> toDomainHelperMapSentence(sNode, globalWordMap))
                .toList();

        toDomainHelperLinkDependencies(textNode, globalWordMap);
        return new Neo4jText(textNode.getArdocoId(), sentences);
    }

    private void toEntityHelperBuildTextHierarchy(Text domainText, TextNode textNode, Map<Integer, WordNode> wordIndexMap,
            Map<Phrase, PhraseNode> phraseCache) {
        SentenceNode prevSentenceNode = null;
        WordNode prevWordNode = null;
        for (Sentence domainSentence : domainText.getSentences()) {
            SentenceNode sentenceNode = new SentenceNode(domainSentence.getSentenceNumber(), domainSentence.getText());
            textNode.addSentence(sentenceNode);

            if (prevSentenceNode != null) {
                prevSentenceNode.setNextSentence(sentenceNode);
            }

            List<WordNode> sentenceWords = new ArrayList<>(domainSentence.getWords().size());
            for (Word domainWord : domainSentence.getWords()) {
                WordNode wordNode = new WordNode(domainWord.getPosition(), domainWord.getText(), domainWord.getLemma(), domainWord.getPosTag().toString());

                sentenceWords.add(wordNode);
                wordIndexMap.put(domainWord.getPosition(), wordNode);

                if (prevWordNode != null) {
                    prevWordNode.setNextWord(wordNode);
                }
                prevWordNode = wordNode;
            }
            sentenceNode.setWords(sentenceWords);

            for (Phrase domainPhrase : domainSentence.getPhrases()) {
                sentenceNode.getRootPhrases().add(toEntityHelperConvertPhrase(domainPhrase, wordIndexMap, phraseCache));
            }

            prevSentenceNode = sentenceNode;
        }
    }

    private void toEntityHelperCreateDependencyLinks(Text domainText, Map<Integer, WordNode> wordMap) {
        for (Word word : domainText.words()) {
            WordNode sourceNode = wordMap.get(word.getPosition());
            if (sourceNode == null)
                continue;

            for (DependencyTag tag : DependencyTag.values()) {
                for (Word target : word.getOutgoingDependencyWordsWithType(tag)) {
                    WordNode targetNode = wordMap.get(target.getPosition());
                    if (targetNode != null) {
                        sourceNode.addDependency(tag.name(), targetNode);
                    }
                }
            }
        }
    }

    private PhraseNode toEntityHelperConvertPhrase(Phrase domainPhrase, Map<Integer, WordNode> wordMap, Map<Phrase, PhraseNode> phraseCache) {
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
            PhraseNode childNode = toEntityHelperConvertPhrase(subPhrase, wordMap, phraseCache);
            phraseNode.addChildPhrase(childNode);
        }

        return phraseNode;
    }

    private Neo4jSentence toDomainHelperMapSentence(SentenceNode sNode, Map<Integer, Neo4jWord> globalWordMap) {
        Neo4jSentence sentence = new Neo4jSentence(sNode.getSentenceNumber(), sNode.getText());

        // Map and sort words
        List<Neo4jWord> words = sNode.getWords().stream().sorted(Comparator.comparingInt(WordNode::getPosition)).map(wNode -> {
            Neo4jWord w = new Neo4jWord(wNode.getPosition(), wNode.getText(), wNode.getLemma(), wNode.getPosTag(), sentence);
            globalWordMap.put(wNode.getPosition(), w);
            return w;
        }).toList();

        for (int i = 0; i < words.size(); i++) {
            if (i > 0)
                words.get(i).setPreWord(words.get(i - 1));
            if (i < words.size() - 1)
                words.get(i).setNextWord(words.get(i + 1));
        }

        // Map Phrases
        List<Phrase> phrases = sNode.getRootPhrases()
                .stream()
                .map(pNode -> toDomainHelperMapPhrase(pNode, sentence, globalWordMap))
                .collect(Collectors.toList());

        sentence.setWords(words);
        sentence.setPhrases(phrases);
        return sentence;
    }

    private Neo4jPhrase toDomainHelperMapPhrase(PhraseNode pNode, Neo4jSentence sentence, Map<Integer, Neo4jWord> wordMap) {
        List<Phrase> children = pNode.getChildPhrases().stream().map(child -> toDomainHelperMapPhrase(child, sentence, wordMap)).collect(Collectors.toList());

        List<Neo4jWord> words = pNode.getContainedWords().stream().map(wn -> wordMap.get(wn.getPosition())).filter(Objects::nonNull).toList();

        return new Neo4jPhrase(pNode.getText(), pNode.getPhraseType(), sentence, words, children);
    }

    private void toDomainHelperLinkDependencies(TextNode textNode, Map<Integer, Neo4jWord> wordMap) {
        textNode.getSentences().stream().flatMap(s -> s.getWords().stream()).forEach(wNode -> {
            Neo4jWord source = wordMap.get(wNode.getPosition());
            if (source == null || wNode.getDependencies() == null)
                return;

            for (DependencyRelationship rel : wNode.getDependencies()) {
                Neo4jWord target = wordMap.get(rel.getTargetWord().getPosition());
                if (target == null)
                    continue;

                try {
                    DependencyTag tag = DependencyTag.valueOf(rel.getDependencyType());
                    source.addOutgoingDependency(tag, target);
                    target.addIncomingDependency(tag, source);
                } catch (IllegalArgumentException ignored) {
                }
            }
        });
    }
}
