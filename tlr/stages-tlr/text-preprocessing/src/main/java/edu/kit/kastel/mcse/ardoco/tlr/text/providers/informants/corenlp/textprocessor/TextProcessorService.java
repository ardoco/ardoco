/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.textprocessor;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.config.ConfigManager;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.converter.DtoToObjectConverter;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.converter.JsonConverter;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.dto.TextDto;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.error.InvalidJsonException;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.error.NotConvertableException;

/**
 * This text processor processes texts by sending requests to a microservice, which provides text processing using CoreNLP.
 */
public class TextProcessorService {

    /**
     * processes and annotates a given text by sending requests to a microservice
     *
     * @param inputText the input text
     * @return the annotated text
     */
    public Text processText(String inputText) throws IOException, InvalidJsonException, NotConvertableException {
        TextDto textDto;
        String jsonText = sendCoreNlpRequest(inputText);
        textDto = JsonConverter.fromJsonString(jsonText);
        return new DtoToObjectConverter().convertText(textDto);
    }

    private String sendCoreNlpRequest(String inputText) throws IOException {
        String encodedText = encodeText(inputText);
        ConfigManager configManager = ConfigManager.INSTANCE;
        String requestUrl = configManager.getMicroserviceUrl() + configManager.getCorenlpService();
        return sendAuthenticatedPostRequest(requestUrl, encodedText);
    }

    private static String encodeText(String inputText) {
        return URLEncoder.encode(inputText, StandardCharsets.UTF_8);
    }

    private String sendAuthenticatedPostRequest(String requestUrl, String encodedText) throws IOException {
        HttpCommunicator httpCommunicator = new HttpCommunicator();
        String body = getRequestBodyString(encodedText);
        return httpCommunicator.sendAuthenticatedPostRequest(requestUrl, body);
    }

    private static String getRequestBodyString(String encodedText) {
        return "{\"text\": \"" + encodedText + "\"}";
    }
}
