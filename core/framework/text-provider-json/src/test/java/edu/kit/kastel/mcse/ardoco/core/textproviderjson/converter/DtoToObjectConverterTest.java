/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.converter;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.error.NotConvertableException;
import edu.kit.kastel.mcse.ardoco.core.textproviderjson.textobject.TextImpl;

class DtoToObjectConverterTest {

    @Test
    void testConvertText() throws IOException, NotConvertableException {
        DtoToObjectConverter converter = new DtoToObjectConverter();

        // convert null
        Assertions.assertThrows(NotConvertableException.class, () -> converter.convertText(null));

        // convert invalid text dto
        Assertions.assertThrows(NotConvertableException.class, () -> converter.convertText(TestUtil.generateInvalidDTO()));

        // convert empty text dto
        Text expected1 = new TextImpl();
        Assertions.assertDoesNotThrow(() -> converter.convertText(TestUtil.generateEmptyDTO()));
        Text actual1 = converter.convertText(TestUtil.generateEmptyDTO());
        Assertions.assertEquals(expected1, actual1);

        // convert text dto with one sentence
        Text expected2 = TestUtil.generateDefaultText();
        Assertions.assertDoesNotThrow(() -> converter.convertText(TestUtil.generateDefaultDTO()));
        Text actual2 = converter.convertText(TestUtil.generateDefaultDTO());
        Assertions.assertEquals(expected2, actual2);

        // convert text dto with multiple sentences
        Text expected3 = TestUtil.generateTextWithMultipleSentences();
        Assertions.assertDoesNotThrow(() -> converter.convertText(TestUtil.generateDTOWithMultipleSentences()));
        Text actual3 = converter.convertText(TestUtil.generateDTOWithMultipleSentences());
        Assertions.assertEquals(expected3, actual3);

        // convert text dto with incoming and outgoing dependencies
        Text expected4 = TestUtil.generateTextWithDependencies();
        Assertions.assertDoesNotThrow(() -> converter.convertText(TestUtil.generateTextDtoWithDependencies()));
        Text actual4 = converter.convertText(TestUtil.generateTextDtoWithDependencies());
        Assertions.assertEquals(expected4, actual4);

    }
}
