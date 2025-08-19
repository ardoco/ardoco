/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
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
import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;

@Deterministic
public class CachedChatLanguageModel implements ChatModel {

    private static final Logger logger = LoggerFactory.getLogger(CachedChatLanguageModel.class);

    private static final String CACHE_DIR = loadCacheDir();

    private final ChatModel chatLanguageModel;
    private final String cacheKey;

    private SortedMap<String, String> cache = new TreeMap<>();

    public CachedChatLanguageModel(ChatModel chatLanguageModel, String cacheKey) {
        this.chatLanguageModel = chatLanguageModel;
        this.cacheKey = cacheKey;
        try {
            this.cache = createObjectMapper().readValue(new File(CACHE_DIR + cacheKey + "-cache.json"), new TypeReference<>() {
            });
            var keys = new HashSet<>(cache.keySet());
            for (String key : keys) {
                String cleanedKey = cleanEndings(key);
                String cleanedValue = cleanEndings(cache.get(key));
                cache.put(cleanedKey, cleanedValue);
            }
        } catch (IOException e) {
            logger.debug("Could not read cache file", e);
        }
    }

    @Override
    public synchronized ChatResponse chat(List<ChatMessage> messages) {
        if (cache.containsKey(cleanEndings(messages.toString()))) {
            return ChatResponse.builder().aiMessage(new AiMessage(cache.get(cleanEndings(messages.toString())))).build();
        }
        ChatResponse response = chatLanguageModel.chat(messages);
        cache.put(cleanEndings(messages.toString()), cleanEndings(response.aiMessage().text()));
        try {
            createObjectMapper().writeValue(new File(CACHE_DIR + cacheKey + "-cache.json"), cache);
        } catch (IOException e) {
            logger.error("Could not write cache file", e);
        }
        return response;
    }

    private String cleanEndings(String text) {
        return text.replace("\r\n", "\n").replace("\r", "\n");
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper oom = JsonHandling.createObjectMapper();
        oom.getFactory().setStreamReadConstraints(StreamReadConstraints.builder().maxNameLength(100000).build());
        return oom;
    }

    private static synchronized String loadCacheDir() {
        if (CACHE_DIR != null) {
            return CACHE_DIR;
        }

        String cacheDir = Environment.getEnv("LLM_CACHE_DIR");
        if (cacheDir == null) {
            cacheDir = ".cache-llm/";
        }
        File cache = new File(cacheDir);
        if (!cache.mkdirs() && !cache.isDirectory()) {
            logger.error("Failed to create cache directory: {}", cacheDir);
            throw new IllegalStateException("Could not create cache directory: " + cacheDir);
        }
        return cacheDir;
    }
}
