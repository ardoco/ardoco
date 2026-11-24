/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.naer.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.model.SoftwareArchitectureDocumentation;

class PromptTest {
    private static final String TASK = "Identify all components.";
    private static final String ANSWER = """
            BEGIN-OUTPUT
            COMPONENT entities recognized:
            UserDatabase, 'The UserDatabase stores all users.'
            Alternative names:
            UserDatabase: None
            END-OUTPUT
            """;

    private final SoftwareArchitectureDocumentation sad = new SoftwareArchitectureDocumentation("The UserDatabase stores all users.");
    private final Map<NamedEntityType, Set<String>> possibleEntities = Map.of(NamedEntityType.COMPONENT, Set.of("UserDatabase"));

    @Test
    void processWithPossibleEntitiesDoesNotModifyPrompt() {
        RecordingChatModel chatModel = new RecordingChatModel();
        Prompt prompt = new StructuredTextOutputPrompt(TASK);

        prompt.process(chatModel, sad, possibleEntities);
        prompt.process(chatModel, sad);

        assertThat(chatModel.userMessages).hasSize(2);
        assertThat(chatModel.userMessages.get(0)).startsWith(TASK).contains("component entities: UserDatabase");
        assertThat(chatModel.userMessages.get(1)).startsWith(TASK).doesNotContain("As support");
        assertThat(prompt.getText()).isEqualTo(TASK);
    }

    @Test
    void recognizeWithPossibleEntitiesCanBeRepeated() {
        RecordingChatModel chatModel = new RecordingChatModel();
        NamedEntityRecognizer recognizer = new NamedEntityRecognizer.Builder().chatModel(chatModel).prompt(new StructuredTextOutputPrompt(TASK)).build();

        Set<NamedEntity> first = recognizer.recognize(sad, possibleEntities);
        Set<NamedEntity> second = recognizer.recognize(sad, possibleEntities);

        assertThat(first).isEqualTo(second).extracting(NamedEntity::getName).containsExactly("UserDatabase");
        assertThat(chatModel.userMessages).hasSize(2).allSatisfy(message -> assertThat(message).containsOnlyOnce("As support"));
    }

    private static final class RecordingChatModel implements ChatModel {
        private final List<String> userMessages = new ArrayList<>();

        @Override
        public ChatResponse doChat(ChatRequest chatRequest) {
            userMessages.addAll(chatRequest.messages()
                    .stream()
                    .filter(UserMessage.class::isInstance)
                    .map(UserMessage.class::cast)
                    .map(UserMessage::singleText)
                    .toList());
            return ChatResponse.builder().aiMessage(AiMessage.from(ANSWER)).build();
        }
    }
}
