/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.dto.IncomingDependencyDto;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.dto.OutgoingDependencyDto;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.dto.SentenceDto;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.dto.TextDto;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.dto.WordDto;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.error.InvalidJsonException;

class JsonConverterTest {

    @Test
    void testValidateJson() throws IOException {
        Assertions.assertTrue(JsonConverter.validateJson(Files.readString(Path.of("./src/test/resources/valid-example-text.json"))));
        Assertions.assertFalse(JsonConverter.validateJson(Files.readString(Path.of("./src/test/resources/invalid-example-text.json"))));
    }

    @Test
    void testToJsonString() throws IOException, InvalidJsonException {
        // invalid text
        TextDto invalidText = new TextDto();
        Assertions.assertThrows(InvalidJsonException.class, () -> JsonConverter.toJsonString(invalidText));

        // valid text
        TextDto validText = getValidTextDtoExample();
        Assertions.assertDoesNotThrow(() -> JsonConverter.toJsonString(validText));
        String generatedJson = JsonConverter.toJsonString(validText);
        Assertions.assertDoesNotThrow(() -> JsonConverter.fromJsonString(generatedJson));
        Assertions.assertEquals(validText, JsonConverter.fromJsonString(generatedJson));
    }

    @Test
    void testFromJsonString() throws IOException, InvalidJsonException {
        // invalid json
        String invalidJsonText = Files.readString(Path.of("./src/test/resources/invalid-example-text.json"));
        Assertions.assertThrows(InvalidJsonException.class, () -> JsonConverter.fromJsonString(invalidJsonText));

        // valid json
        String validJsonText = getValidJsonExample();
        Assertions.assertDoesNotThrow(() -> JsonConverter.fromJsonString(validJsonText));
        TextDto generatedText = JsonConverter.fromJsonString(validJsonText);
        TextDto expectedText = getValidTextDtoExample();
        Assertions.assertEquals(expectedText, generatedText);
    }

    private String getValidJsonExample() throws IOException {
        return Files.readString(Path.of("./src/test/resources/valid-example-text.json"));
    }

    private TextDto getValidTextDtoExample() throws IOException {
        WordDto expectedWord = new WordDto();
        expectedWord.setId(1);
        expectedWord.setSentenceNo(1);
        expectedWord.setLemma("hello");
        expectedWord.setText("Hello");
        expectedWord.setPosTag(POSTag.forValue("UH"));

        OutgoingDependencyDto expectedOutDep = new OutgoingDependencyDto();
        expectedOutDep.setTargetWordId(1);
        expectedOutDep.setDependencyTag(DependencyTag.APPOS);
        List<OutgoingDependencyDto> expectedOutList = new ArrayList<>();
        expectedOutList.add(expectedOutDep);
        expectedWord.setOutgoingDependencies(expectedOutList);

        IncomingDependencyDto expectedInDep = new IncomingDependencyDto();
        expectedInDep.setSourceWordId(1);
        expectedInDep.setDependencyTag(DependencyTag.APPOS);
        List<IncomingDependencyDto> expectedInList = new ArrayList<>();
        expectedInList.add(expectedInDep);
        expectedWord.setIncomingDependencies(expectedInList);

        List<WordDto> expectedWords = new ArrayList<>();
        expectedWords.add(expectedWord);

        SentenceDto expectedSentence = new SentenceDto();
        expectedSentence.setSentenceNo(1);
        expectedSentence.setText("Hello World!");
        expectedSentence.setConstituencyTree("(ROOT (FRAG (INTJ (UH Hello)) (NP (NNP World)) (. !)))");
        expectedSentence.setWords(expectedWords);

        List<SentenceDto> expectedSentences = new ArrayList<>();
        expectedSentences.add(expectedSentence);

        TextDto expectedText = new TextDto();
        expectedText.setSentences(expectedSentences);
        return expectedText;
    }
}
