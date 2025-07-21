/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import static edu.kit.kastel.mcse.ardoco.core.common.JsonHandling.createObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.JsonHandling;

@Deterministic
public class CachedChatLanguageModel implements ChatModel {

    private static final Logger logger = LoggerFactory.getLogger(CachedChatLanguageModel.class);

    private static final String CACHE_DIR = "cache-llm/";
    static {
        new File(CACHE_DIR).mkdirs();
    }

    private final ChatModel chatLanguageModel;
    private final String cacheKey;

    private SortedMap<String, String> cache = new TreeMap<>();

    public CachedChatLanguageModel(ChatModel chatLanguageModel, String cacheKey) {
        this.chatLanguageModel = chatLanguageModel;
        this.cacheKey = cacheKey;
        try {
            this.cache = createObjectMapper().readValue(new File(CACHE_DIR + cacheKey + "-cache.json"), new TypeReference<>() {
            });
        } catch (IOException e) {
            logger.debug("Could not read cache file", e);
        }
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        if (cache.containsKey(messages.toString())) {
            return ChatResponse.builder().aiMessage(new AiMessage(cache.get(messages.toString()))).build();
        }
        ChatResponse response = chatLanguageModel.chat(messages);
        cache.put(messages.toString(), response.aiMessage().text());
        try {
            createObjectMapper().writeValue(new File(CACHE_DIR + cacheKey + "-cache.json"), cache);
        } catch (IOException e) {
            logger.error("Could not write cache file", e);
        }
        return response;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper oom = JsonHandling.createObjectMapper();
        oom.getFactory().setStreamReadConstraints(StreamReadConstraints.builder().maxNameLength(100000).build());
        return oom;
    }
}
