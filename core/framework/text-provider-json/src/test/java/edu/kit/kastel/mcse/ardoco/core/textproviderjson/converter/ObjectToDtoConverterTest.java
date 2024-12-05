/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.converter;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.textproviderjson.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.dto.TextDto;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.error.NotConvertableException;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.textobject.TextImpl;

class ObjectToDtoConverterTest {

    @Test
    void testConvertTextToDTO() throws NotConvertableException, IOException {
        ObjectToDtoConverter converter = new ObjectToDtoConverter();

        // convert null Text
        Assertions.assertThrows(NotConvertableException.class, () -> converter.convertTextToDTO(null));

        // convert empty Text
        TextDto expected1 = TestUtil.generateEmptyDTO();
        Assertions.assertDoesNotThrow(() -> converter.convertTextToDTO(new TextImpl()));
        Assertions.assertEquals(expected1, converter.convertTextToDTO(new TextImpl()));

        // convert Text with one sentence
        TextDto expected2 = TestUtil.generateDefaultDTO();
        Assertions.assertDoesNotThrow(() -> converter.convertTextToDTO(TestUtil.generateDefaultText()));
        Assertions.assertEquals(expected2, converter.convertTextToDTO(TestUtil.generateDefaultText()));

        // convert Text with multiple sentences
        TextDto expected3 = TestUtil.generateDTOWithMultipleSentences();
        Assertions.assertDoesNotThrow(() -> converter.convertTextToDTO(TestUtil.generateTextWithMultipleSentences()));
        Assertions.assertEquals(expected3, converter.convertTextToDTO(TestUtil.generateTextWithMultipleSentences()));

        // convert Text with incoming and outgoing dependencies
        TextDto expected4 = TestUtil.generateTextDtoWithDependencies();
        Assertions.assertDoesNotThrow(() -> converter.convertTextToDTO(TestUtil.generateTextWithDependencies()));
        Assertions.assertEquals(expected4, converter.convertTextToDTO(TestUtil.generateTextWithDependencies()));

    }
}
