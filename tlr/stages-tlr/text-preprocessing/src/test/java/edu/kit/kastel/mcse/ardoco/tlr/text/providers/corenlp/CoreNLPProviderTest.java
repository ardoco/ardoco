/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.text.providers.corenlp;

import static edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities.readInputText;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.base.NlpInformantTest;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.CoreNLPProvider;

class CoreNLPProviderTest extends NlpInformantTest {
    protected static String inputText = "src/test/resources/teastore.txt";

    private static CoreNLPProvider coreNLPProvider = null;

    public synchronized static CoreNLPProvider getCoreNLPProvider() {
        if (coreNLPProvider == null) {
            try {
                createCoreNlpProvider();
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return coreNLPProvider;
    }

    private static void createCoreNlpProvider() throws FileNotFoundException {
        DataRepository dataRepository = new DataRepository();
        DataRepositoryHelper.putInputText(dataRepository, readInputText(new FileInputStream(inputText)));
        coreNLPProvider = new CoreNLPProvider(dataRepository);
    }

    @Test
    void constructorTest() {
        try {
            createCoreNlpProvider();
        } catch (FileNotFoundException e) {
            Assertions.fail("Could not instantiate CoreNLPProvider with provided text due to FileNotFoundException.", e.getCause());
        }
        Assertions.assertNotNull(coreNLPProvider);
    }

    @Override
    protected CoreNLPProvider getProvider() {
        return CoreNLPProviderTest.getCoreNLPProvider();
    }
}
