/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.tlr.text.providers.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.text.NlpInformant;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.CoreNLPProvider;

public abstract class NlpInformantTest {
    protected static String inputText = "src/test/resources/teastore.txt";

    private NlpInformant provider = null;

    @BeforeEach
    void beforeEach() {
        provider = getProvider();
    }

    protected abstract CoreNLPProvider getProvider();

    @Test
    void getTextTest() {
        var text = provider.getAnnotatedText();
        Assertions.assertNotNull(text);
    }
}
