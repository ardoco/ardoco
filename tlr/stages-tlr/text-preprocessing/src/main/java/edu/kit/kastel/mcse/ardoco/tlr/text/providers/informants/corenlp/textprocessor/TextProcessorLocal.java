/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.textprocessor;

import java.util.Properties;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.TextImpl;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * This text processor processes texts locally using CoreNLP.
 */
public class TextProcessorLocal {
    private static final String ANNOTATORS = "tokenize,ssplit,pos,parse,depparse,lemma"; // further: ",ner,coref"
    private static final String DEPENDENCIES_ANNOTATION = "EnhancedPlusPlusDependenciesAnnotation";

    /**
     * processes and annotates a given text locally using CoreNLP.
     *
     * @param inputText the input text
     * @return the annotated text
     */
    public Text processText(String inputText) {
        Properties props = getStanfordProperties(new Properties());
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument document = new CoreDocument(inputText);
        pipeline.annotate(document);
        return new TextImpl(document);
    }

    private static Properties getStanfordProperties(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties are null");
        }
        var allStanfordProperties = new Properties(properties);
        allStanfordProperties.setProperty("annotators", ANNOTATORS);

        allStanfordProperties.put("parse", DEPENDENCIES_ANNOTATION);
        allStanfordProperties.put("depparse", DEPENDENCIES_ANNOTATION);
        allStanfordProperties.put("coref.algorithm", "fastneural");

        return allStanfordProperties;
    }
}
