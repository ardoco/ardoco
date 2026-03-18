/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.util.models;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jPhrase;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jSentence;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jText;
import io.github.ardoco.core.neo4jschema.adapter.Neo4jWord;

public class TextFactory {

    public static Text createComplexText(String id) {
        // Sentence 0: "The login component handles authentication."
        Neo4jSentence s0 = new Neo4jSentence(0, "The login component handles authentication.");

        Neo4jWord w0 = new Neo4jWord(0, "The", "the", "DT", s0);
        Neo4jWord w1 = new Neo4jWord(1, "login", "login", "NN", s0);
        Neo4jWord w2 = new Neo4jWord(2, "component", "component", "NN", s0);
        Neo4jWord w3 = new Neo4jWord(3, "handles", "handle", "VBZ", s0);
        Neo4jWord w4 = new Neo4jWord(4, "authentication", "authentication", "NN", s0);

        w0.setNextWord(w1);
        w1.setPreWord(w0);
        w1.setNextWord(w2);
        w2.setPreWord(w1);
        w2.setNextWord(w3);
        w3.setPreWord(w2);
        w3.setNextWord(w4);
        w4.setPreWord(w3);

        w3.addOutgoingDependency(DependencyTag.NSUBJ, w2);
        w3.addOutgoingDependency(DependencyTag.OBJ, w4);
        w2.addIncomingDependency(DependencyTag.NSUBJ, w2);
        w4.addIncomingDependency(DependencyTag.OBJ, w3);

        Neo4jPhrase npSub = new Neo4jPhrase("login component", "NP", s0, List.of(w1, w2), List.of());
        Neo4jPhrase npRoot = new Neo4jPhrase("The login component", "NP", s0, List.of(w0, w1, w2), List.of(npSub));

        s0.setWords(List.of(w0, w1, w2, w3, w4));
        s0.setPhrases(List.of(npRoot));

        // Sentence 1: "It uses a database."
        Neo4jSentence s1 = new Neo4jSentence(1, "It uses a database.");

        Neo4jWord w5 = new Neo4jWord(5, "It", "it", "PRP", s1);
        Neo4jWord w6 = new Neo4jWord(6, "uses", "use", "VBZ", s1);
        Neo4jWord w7 = new Neo4jWord(7, "a", "a", "DT", s1);
        Neo4jWord w8 = new Neo4jWord(8, "database", "database", "NN", s1);

        w5.setNextWord(w6);
        w6.setPreWord(w5);
        w6.setNextWord(w7);
        w7.setPreWord(w6);
        w7.setNextWord(w8);
        w8.setPreWord(w7);

        s1.setWords(List.of(w5, w6, w7, w8));
        s1.setPhrases(List.of());

        return new Neo4jText(id, List.of(s0, s1));
    }

    public static Text createEmptyText(String id) {
        return new Neo4jText(id, new ArrayList<>());
    }
}
