package deprecated;

import dev.langchain4j.model.chat.ChatModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class ComponentRecognizer {
    private static final String prompt = """
            You are an experienced software engineer with expertise in software architecture analysis.
            Given a text, describing a software architecture (one sentence per line), identify all software components mentioned in each sentence.
            For each occurrence of a component, output a line in the following format:
            ComponentName,LineNumber
            
            Instructions:
            Use the exact casing of the component as it appears in the text.
            Use line numbers to indicate where each component is mentioned (starting at 1).
            If a component appears in multiple lines, list it multiple times (once per line).
            If a sentence contains multiple components, include a separate entry for each.
            Normalize similar component names if they clearly refer to the same concept (e.g., treat Database and User Database as the same component when appropriate).
            Use the most descriptive name.
            Only return the list of ComponentName,LineNumber pairs (separated by new line characters), nothing else.""";

    /**
     * Recognizes components in software architecture documentations (SADs).
     *
     * @param model       {@link ChatModel} used for the LLM call
     * @param sadFilepath file path of the SAD to be analyzed
     * @return a set containing all found occurrences of components (as {@link ComponentOccurrence})
     */
    public static Set<ComponentOccurrence> recognizeComponents(ChatModel model, Path sadFilepath) {
        String sad = "";
        try {
            sad = Files.readString(sadFilepath);
        } catch (IOException e) {
            System.err.println("Error reading software architecture documentation file: " + sadFilepath);
            System.exit(1);
        }
        String answer = model.chat(prompt + "\nText:\n" + sad);
        return ComponentOccurrence.parse(answer,false);
    }
}
