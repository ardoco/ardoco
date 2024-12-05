/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp;

import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.text.NlpInformant;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.textprocessor.TextProcessor;

public class CoreNLPProvider extends NlpInformant {

    private Text annotatedText;

    // Needed for Configuration Generation
    private CoreNLPProvider() {
        super(CoreNLPProvider.class.getSimpleName(), null);
    }

    public CoreNLPProvider(DataRepository data) {
        super(CoreNLPProvider.class.getSimpleName(), data);
        this.annotatedText = null;
    }

    @Override
    public void process() {
        if (!DataRepositoryHelper.hasAnnotatedText(this.getDataRepository())) {
            var preprocessingData = new PreprocessingData(this.getAnnotatedText());
            DataRepositoryHelper.putPreprocessingData(this.getDataRepository(), preprocessingData);
        }
    }

    @Override
    public Text getAnnotatedText(String textName) {
        this.getLogger().warn("Returning annotated text ignoring the provided name");
        return this.getAnnotatedText();
    }

    @Override
    public synchronized Text getAnnotatedText() {
        if (this.annotatedText == null) {
            if (DataRepositoryHelper.hasAnnotatedText(this.getDataRepository())) {
                this.annotatedText = DataRepositoryHelper.getAnnotatedText(this.getDataRepository());
            } else {
                String text = DataRepositoryHelper.getInputText(this.getDataRepository());
                this.annotatedText = this.processText(text);
            }
        }
        return this.annotatedText;
    }

    private Text processText(String inputText) {
        return new TextProcessor().processText(inputText);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> map) {
        // empty
    }
}
