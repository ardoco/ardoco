/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.informants.llmagents;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import edu.kit.kastel.mcse.ardoco.core.common.readonlytools.CodeDirectoryReadOnlyTools;

/**
 * The chat requests of the validation agent are cached by their messages, so neither the prompt nor the tool results may contain the location of the code
 * project on this machine.
 */
class ArchitecturalStylePatternValidationLlmAgentPromptTest {

    @TempDir
    Path codeDirectory;

    @Test
    void promptAndToolResultsDoNotContainTheProjectPath() throws IOException {
        Files.createDirectories(codeDirectory.resolve("src"));
        Files.writeString(codeDirectory.resolve("src/Main.java"), "class Main {}");
        var chatModel = new ScriptedChatModel();
        var agent = AiServices.builder(ArchitecturalStylePatternValidationLlmAgent.class)
                .chatModel(chatModel)
                .tools(new CodeDirectoryReadOnlyTools(codeDirectory.toString()))
                .build();

        agent.validateArchitecturalStylePatternStatementAgainstCodeDirectory("MVC", "MVC", "The system uses MVC.", "The system uses MVC.");

        String conversation = chatModel.lastMessages();
        assertTrue(conversation.contains("text = \"src/\""), "The tool result is missing: " + conversation);
        assertFalse(conversation.contains(codeDirectory.toString()), () -> "Absolute path in " + conversation);
        assertFalse(conversation.contains(codeDirectory.getFileName().toString()), () -> "Directory name in " + conversation);
    }

    /**
     * Lists the project root once and then answers.
     */
    private static final class ScriptedChatModel implements ChatModel {
        private final List<String> conversations = new ArrayList<>();

        @Override
        public ChatResponse chat(ChatRequest request) {
            conversations.add(request.messages().toString());
            boolean toolResultPresent = conversations.size() > 1;
            AiMessage answer = toolResultPresent ?
                    AiMessage.from("{\"valid\": true, \"explanation\": \"ok\"}") :
                    AiMessage.from(ToolExecutionRequest.builder().id("1").name("listDirectory").arguments("{\"arg0\": \".\", \"path\": \".\"}").build());
            return ChatResponse.builder().aiMessage(answer).build();
        }

        private String lastMessages() {
            return conversations.getLast();
        }
    }
}
