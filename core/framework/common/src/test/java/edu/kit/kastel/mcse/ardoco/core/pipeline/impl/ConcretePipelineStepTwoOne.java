/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.pipeline.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.impl.ProcessedTextData;
import edu.kit.kastel.mcse.ardoco.core.data.impl.TextData;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;

/**
 * Example implementation of {@link AbstractPipelineStep}
 */
public class ConcretePipelineStepTwoOne extends AbstractPipelineStep {
    private static final Logger logger = LoggerFactory.getLogger(ConcretePipelineStepTwoOne.class);

    private TextData textData;
    private ProcessedTextData processedTextData;

    public ConcretePipelineStepTwoOne(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }

    private void fetchAndInitializeData() {
        var dataRepository = getDataRepository();
        textData = dataRepository.getData("Text", TextData.class).orElseThrow();
        var processedTextDataOptional = dataRepository.getData("ProcessedTextData", ProcessedTextData.class);
        if (processedTextDataOptional.isPresent()) {
            processedTextData = processedTextDataOptional.get();
        } else {
            processedTextData = new ProcessedTextData();
            dataRepository.addData("ProcessedTextData", processedTextData);
        }
    }

    @Override
    public void process() {
        fetchAndInitializeData();
        logger.info("Greetings from {} with id {}", this.getClass().getSimpleName(), getId());
        List<String> tokens = getTokens();
        List<String> filteredTokens = new ArrayList<>();
        StringBuilder outputBuilder = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            if ((i & 1) == 0) {
                String token = tokens.get(i);
                filteredTokens.add(token);
                outputBuilder.append(" ").append(token);
            }
        }
        logger.info(outputBuilder.toString());
        logger.info("{}", filteredTokens.size());
        processedTextData.setImportantTokens(filteredTokens);

    }

    @Override
    protected void before() {
        //Nothing
    }

    @Override
    protected void after() {
        //Nothing
    }

    private List<String> getTokens() {
        var tokens = processedTextData.getImportantTokens();
        if (tokens == null || tokens.isEmpty()) {
            logger.debug("No preprocessedTextData, fetching textData.");
            tokens = textData.getTokens();
        }
        return tokens;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // NOP
    }
}
