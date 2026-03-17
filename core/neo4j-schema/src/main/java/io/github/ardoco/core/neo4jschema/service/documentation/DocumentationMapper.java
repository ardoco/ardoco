/* Licensed under MIT 2023-2026. */
package io.github.ardoco.core.neo4jschema.service.documentation;

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
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
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

    public Text mapToDomain(TextNode textNode) {
        logger.info("Mapping TextNode (ID: {}) to Domain Model.", textNode.getArdocoId());

        // Global map to resolve dependencies later
        Map<Integer, Neo4jWord> globalWordMap = new HashMap<>();

        var sentenceNodes = new ArrayList<>(textNode.getSentences());
        sentenceNodes.sort(Comparator.comparingInt(SentenceNode::getSentenceNumber));

        List<Neo4jSentence> sentences = new ArrayList<>();
        for (SentenceNode sNode : sentenceNodes) {
            sentences.add(mapSentence(sNode, globalWordMap));
        }

        linkDependencies(textNode, globalWordMap);

        return new Neo4jText(textNode.getArdocoId(), sentences);
    }

    private Neo4jSentence mapSentence(SentenceNode sNode, Map<Integer, Neo4jWord> globalWordMap) {
        // Create Shell
        Neo4jSentence sentence = new Neo4jSentence(sNode.getSentenceNumber(), sNode.getText());

        // Map Words
        var wordNodes = new ArrayList<>(sNode.getWords());
        wordNodes.sort(Comparator.comparingInt(WordNode::getPosition));

        List<Neo4jWord> words = new ArrayList<>();
        Map<Integer, Neo4jWord> sentenceWordMap = new HashMap<>(); // Local map for phrase resolution

        for (WordNode wNode : wordNodes) {
            Neo4jWord word = new Neo4jWord(wNode.getPosition(), wNode.getText(), wNode.getLemma(), wNode.getPosTag(), sentence);
            words.add(word);
            // Add to both maps
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

        // Map Phrases (using local word references)
        List<Phrase> phrases = new ArrayList<>();
        for (PhraseNode pNode : sNode.getRootPhrases()) {
            phrases.add(mapPhrase(pNode, sentence, sentenceWordMap));
        }

        // Set Content
        sentence.setWords(words);
        sentence.setPhrases(phrases);

        return sentence;
    }

    private Neo4jPhrase mapPhrase(PhraseNode pNode, Neo4jSentence parentSentence, Map<Integer, Neo4jWord> wordMap) {
        List<Phrase> childPhrases = new ArrayList<>();
        for (PhraseNode childNode : pNode.getChildPhrases()) {
            childPhrases.add(mapPhrase(childNode, parentSentence, wordMap));
        }

        List<Neo4jWord> containedWords = pNode.getContainedWords().stream().map(wn -> wordMap.get(wn.getPosition())).collect(Collectors.toList());

        return new Neo4jPhrase(pNode.getText(), pNode.getPhraseType(), parentSentence, containedWords, childPhrases);
    }

    private void linkDependencies(TextNode textNode, Map<Integer, Neo4jWord> globalWordMap) {
        for (SentenceNode sNode : textNode.getSentences()) {
            for (WordNode wNode : sNode.getWords()) {
                Neo4jWord source = globalWordMap.get(wNode.getPosition());
                if (source == null) continue;

                List<DependencyRelationship> deps = wNode.getDependencies();
                if (deps != null && !deps.isEmpty()) {
                    for (DependencyRelationship rel : deps) {
                        // Critical: The targetWord in the relationship must be resolved to our Neo4jWord adapter
                        WordNode targetNode = rel.getTargetWord();
                        if (targetNode == null) continue;

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
}
