package edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.simple;

import edu.kit.kastel.mcse.ardoco.core.api.SimplePreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class SimpleTextProvider extends Informant {

    public SimpleTextProvider(DataRepository data) {
        super(SimpleTextProvider.class.getSimpleName(), data);
    }

    @Override
    protected void process() {
        String text = DataRepositoryHelper.getInputText(this.getDataRepository());
        var simpleText = new SimpleTextImpl(text);
        var simplePreprocessingData = new SimplePreprocessingData(simpleText);
        DataRepositoryHelper.putSimplePreprocessingData(this.getDataRepository(), simplePreprocessingData);
    }

}
