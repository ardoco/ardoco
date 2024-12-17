/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import static edu.kit.kastel.mcse.ardoco.core.common.JsonHandling.createObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamWriteConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.core.common.JsonHandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

@Deterministic
public class CachedChatLanguageModel implements ChatLanguageModel {

    private static final Logger logger = LoggerFactory.getLogger(CachedChatLanguageModel.class);

    private static final String CACHE_DIR = "cache-llm/";
    static {
        new File(CACHE_DIR).mkdirs();
    }

    private final ChatLanguageModel chatLanguageModel;
    private final String cacheKey;

    private Map<String, String> cache = new LinkedHashMap<>();

    public CachedChatLanguageModel(ChatLanguageModel chatLanguageModel, String cacheKey) {
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
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        if (cache.containsKey(messages.toString())) {
            return Response.from(new AiMessage(cache.get(messages.toString())));
        }
        Response<AiMessage> response = chatLanguageModel.generate(messages);
        cache.put(messages.toString(), response.content().text());
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
