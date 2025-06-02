import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * todo
 */
public class NamedEntityRecognizer {
    private static final Logger logger = LoggerFactory.getLogger(NamedEntityRecognizer.class);

    //TODO javadoc mit allen params erklären und was für die gelten muss (v.a. für prompt (output format + NamedEntityType) und type usw)
    public static Set<NamedEntity> recognize(Path sadFilePath, ChatModel model, String prompt) {
        String sad = "";
        try {
            sad = Files.readString(sadFilePath);
        } catch (IOException e) {
            logger.error("Error reading software architecture documentation file: {}", sadFilePath);
            System.exit(1);
        }

        String answer = model.chat(prompt + "\nText:\n" + sad);

        return NamedEntity.parse(answer);
    }

}
