/* Licensed under MIT 2022-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.text.providers.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.text.NlpInformant;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.CoreNLPProvider;

public abstract class NlpInformantTest {
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
