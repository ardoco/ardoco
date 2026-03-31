package io.github.ardoco.core.neo4jschema.adapter;

import java.util.Locale;
import java.util.Objects;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.TextInconsistency;

/**
 * This class serves as an adapter to convert a TextInconsistencyNode from the Neo4j database into a TextInconsistency that can be used in the Ardoco framework.
 * The original TextEntityAbsentFromModelInconsistency could not be used directly because it requires a field MissingElementInconsistencyCandidate which is not
 * available in the Neo4j database and would require additional effort to construct.
 *
 */
public class Neo4jTextInconsistency implements TextInconsistency {

    private static final String INCONSISTENCY_TYPE_NAME = "TextEntityAbsentFromModel";

    String name;
    int sentenceNumber;
    double confidence;

    public Neo4jTextInconsistency(String name, int sentenceNumber, double confidence) {
        this.name = name;
        this.sentenceNumber = sentenceNumber;
        this.confidence = confidence;
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public String getReason() {
        return String.format(Locale.US, "Text indicates that \"%s\" should be contained in the model(s) but could not be found. (confidence: %.2f)", name,
                confidence);
    }

    @Override
    public String getType() {
        return INCONSISTENCY_TYPE_NAME;
    }

    @Override
    public ImmutableCollection<String[]> toFileOutput() {
        MutableList<String[]> entries = Lists.mutable.empty();

        var sentenceNoString = "" + sentenceNumber;
        String[] entry = { getType(), sentenceNoString, name, Integer.toString(sentenceNumber), Double.toString(confidence) };
        entries.add(entry);

        return entries.toImmutable();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sentenceNumber, confidence);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Neo4jTextInconsistency other)) {
            return false;
        }
        return Objects.equals(name, other.name) && sentenceNumber == other.sentenceNumber && Math.abs(confidence - other.confidence) < 1e-5;
    }

    @Override
    public String toString() {
        return "TextEntityAbsentFromModelInconsistency [name=" + name + ", sentence= " + sentenceNumber + "]";
    }
}
