/* Licensed under MIT 2023-2026. */
package io.github.ardoco.core.neo4jschema.mapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(DocumentationMapper.class);

    public TextNode toNode(Text domainText, String documentId) {
        TextNode textNode = new TextNode(documentId);
        int estimatedWordCount = domainText.getSentences().size() * 25;
        Map<Integer, WordNode> wordIndexMap = new HashMap<>(estimatedWordCount); // Global Map: Position -> Node
        Map<Phrase, PhraseNode> phraseCache = new HashMap<>(estimatedWordCount / 2);

        SentenceNode prevSentenceNode = null;

        for (Sentence domainSentence : domainText.getSentences()) {
            SentenceNode sentenceNode = new SentenceNode(domainSentence.getSentenceNumber(), domainSentence.getText());
            textNode.addSentence(sentenceNode);

            if (prevSentenceNode != null) {
                prevSentenceNode.setNextSentence(sentenceNode);
            }

            List<WordNode> sentenceWords = new ArrayList<>(domainSentence.getWords().size());
            WordNode prevWordNode = null;
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
                sentenceNode.getRootPhrases().add(mapPhraseToNode(domainPhrase, wordIndexMap, phraseCache));
            }

            prevSentenceNode = sentenceNode;
        }

        createNodeDependencies(domainText, wordIndexMap);
        return textNode;
    }

    public Text toDomain(TextNode textNode) {
        logger.info("Mapping TextNode (ID: {}) to Domain Model.", textNode.getArdocoId());
        Map<Integer, Neo4jWord> globalWordMap = new HashMap<>();

        var sentenceNodes = new ArrayList<>(textNode.getSentences());
        sentenceNodes.sort(Comparator.comparingInt(SentenceNode::getSentenceNumber));

        List<Neo4jSentence> sentences = new ArrayList<>();
        for (SentenceNode sNode : sentenceNodes) {
            sentences.add(mapSentenceToDomain(sNode, globalWordMap));
        }

        createDomainDependencies(textNode, globalWordMap);

        return new Neo4jText(textNode.getArdocoId(), sentences);
    }

    public SentenceNode convertMapToSentenceNode(Map<String, Object> data) {
        SentenceNode sNode = new SentenceNode(((Number) data.get("sentenceNumber")).intValue(), (String) data.get("text"));

        List<Map<String, Object>> wordsData = (List<Map<String, Object>>) data.get("words");
        Map<Integer, WordNode> wordLookup = new HashMap<>();
        for (Map<String, Object> w : wordsData) {
            WordNode wn = new WordNode(((Number) w.get("position")).intValue(), (String) w.get("text"), (String) w.get("lemma"), (String) w.get("posTag"));
            wordLookup.put(wn.getPosition(), wn);
        }
        sNode.setWords(new ArrayList<>(wordLookup.values()));

        List<String> rootPhraseIds = (List<String>) data.get("rootPhraseIds");
        List<Map<String, Object>> phrasesData = (List<Map<String, Object>>) data.get("phrases");

        Map<String, PhraseNode> phraseLookup = new HashMap<>();
        Map<String, List<String>> hierarchyMap = new HashMap<>();

        for (Map<String, Object> pMap : phrasesData) {
            String id = (String) pMap.get("id");
            PhraseNode pNode = new PhraseNode((String) pMap.get("text"), (String) pMap.get("phraseType"));

            // Link Words to this specific Phrase
            List<Object> wordPos = (List<Object>) pMap.get("containedWords");
            if (wordPos != null) {
                for (Object pos : wordPos) {
                    WordNode wn = wordLookup.get(((Number) pos).intValue());
                    if (wn != null)
                        pNode.addContainedWord(wn);
                }
            }
            phraseLookup.put(id, pNode);
            hierarchyMap.put(id, (List<String>) pMap.get("childIds"));
        }

        for (String parentId : phraseLookup.keySet()) {
            PhraseNode parentNode = phraseLookup.get(parentId);
            List<String> childrenIds = hierarchyMap.get(parentId);

            if (childrenIds != null) {
                for (String childId : childrenIds) {
                    PhraseNode childNode = phraseLookup.get(childId);
                    if (childNode != null) {
                        parentNode.addChildPhrase(childNode);
                    }
                }
            }
        }

        List<PhraseNode> rootPhrases = rootPhraseIds.stream().map(phraseLookup::get).filter(java.util.Objects::nonNull).toList();

        sNode.setRootPhrases(new ArrayList<>(rootPhrases));
        return sNode;
    }

    public Neo4jSentence mapSentenceToDomain(SentenceNode sNode, Map<Integer, Neo4jWord> globalWordMap) {
        Neo4jSentence sentence = new Neo4jSentence(sNode.getSentenceNumber(), sNode.getText());

        var wordNodes = new ArrayList<>(sNode.getWords());
        wordNodes.sort(Comparator.comparingInt(WordNode::getPosition));

        List<Neo4jWord> words = new ArrayList<>();
        Map<Integer, Neo4jWord> sentenceWordMap = new HashMap<>(); // Local map for phrase resolution

        for (WordNode wNode : wordNodes) {
            Neo4jWord word = new Neo4jWord(wNode.getPosition(), wNode.getText(), wNode.getLemma(), wNode.getPosTag(), sentence);
            words.add(word);
            sentenceWordMap.put(wNode.getPosition(), word);
            globalWordMap.put(wNode.getPosition(), word);
        }

        // Link Words (Pre/Next)
        for (int i = 0; i < words.size(); i++) {
            Neo4jWord current = words.get(i);
            if (i > 0)
                current.setPreWord(words.get(i - 1));
            if (i < words.size() - 1)
                current.setNextWord(words.get(i + 1));
        }

        List<Phrase> phrases = new ArrayList<>();
        for (PhraseNode pNode : sNode.getRootPhrases()) {
            phrases.add(mapPhraseToDomain(pNode, sentence, sentenceWordMap));
        }

        sentence.setWords(words);
        sentence.setPhrases(phrases);

        return sentence;
    }

    private Neo4jPhrase mapPhraseToDomain(PhraseNode pNode, Neo4jSentence parentSentence, Map<Integer, Neo4jWord> wordMap) {
        List<Phrase> childPhrases = new ArrayList<>();
        for (PhraseNode childNode : pNode.getChildPhrases()) {
            childPhrases.add(mapPhraseToDomain(childNode, parentSentence, wordMap));
        }

        List<Neo4jWord> containedWords = pNode.getContainedWords().stream().map(wn -> wordMap.get(wn.getPosition())).collect(Collectors.toList());

        return new Neo4jPhrase(pNode.getText(), pNode.getPhraseType(), parentSentence, containedWords, childPhrases);
    }

    private void createDomainDependencies(TextNode textNode, Map<Integer, Neo4jWord> globalWordMap) {
        for (SentenceNode sNode : textNode.getSentences()) {
            for (WordNode wNode : sNode.getWords()) {
                Neo4jWord source = globalWordMap.get(wNode.getPosition());
                if (source == null)
                    continue;

                List<DependencyRelationship> deps = wNode.getDependencies();
                if (deps != null && !deps.isEmpty()) {
                    for (DependencyRelationship rel : deps) {
                        WordNode targetNode = rel.getTargetWord();
                        if (targetNode == null)
                            continue;

                        Neo4jWord target = globalWordMap.get(targetNode.getPosition());

                        try {
                            DependencyTag tag = DependencyTag.valueOf(rel.getDependencyType());
                            if (target != null) {
                                source.addOutgoingDependency(tag, target);
                                target.addIncomingDependency(tag, source);
                            }
                        } catch (IllegalArgumentException e) {
                            logger.warn("Unknown Dependency Tag: {}", rel.getDependencyType());
                        }
                    }
                }
            }
        }
    }

    private void createNodeDependencies(Text domainText, Map<Integer, WordNode> wordMap) {
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

    private PhraseNode mapPhraseToNode(Phrase domainPhrase, Map<Integer, WordNode> wordMap, Map<Phrase, PhraseNode> phraseCache) {
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
            if (subPhrase == domainPhrase)
                continue;
            PhraseNode childNode = mapPhraseToNode(subPhrase, wordMap, phraseCache);
            phraseNode.addChildPhrase(childNode);
        }

        return phraseNode;
    }

}
