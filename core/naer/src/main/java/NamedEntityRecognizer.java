import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * todo
 */
public class NamedEntityRecognizer {
    private static final Logger logger = LoggerFactory.getLogger(NamedEntityRecognizer.class);

    //TODO javadoc mit allen params erklären und was für die gelten muss (v.a. für prompt (output format + NamedEntityType) und type usw)
    public static Set<NamedEntity> recognize(Path sadFilePath, ChatModel model, String prompt) {
        //read SAD file into string
        String sad = "";
        try {
            sad = Files.readString(sadFilePath);
        } catch (IOException e) {
            logger.error("Error reading software architecture documentation file: {}", sadFilePath);
            System.exit(1);
        }

        //call LLM
        String answer = model.chat(prompt + "\nText:\n" + sad);

        //remove text before and after JSON object
        int start = answer.indexOf('[');
        int end = answer.lastIndexOf(']');
        if (start != -1 && end != -1 && end > start) {
            answer = answer.substring(start, end + 1);
        } else {
            logger.error("No valid JSON object found in LLM output: {}", answer);
            System.exit(1);
        }

        //parse JSON array to list of NamedEntity instances
        ObjectMapper mapper = new ObjectMapper();
        List<NamedEntity> entities = null;
        try {
            entities = mapper.readValue(answer, new TypeReference<List<NamedEntity>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Error parsing LLM output to named entities.");
            System.exit(1);
        }

        return new HashSet<>(entities);
    }

}
