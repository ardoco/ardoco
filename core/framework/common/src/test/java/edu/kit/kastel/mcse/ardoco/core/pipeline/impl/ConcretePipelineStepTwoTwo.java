/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.pipeline.impl;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.impl.ProcessedTextData;
import edu.kit.kastel.mcse.ardoco.core.data.impl.ResultData;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;

/**
 * Example implementation of {@link AbstractPipelineStep}
 */
public class ConcretePipelineStepTwoTwo extends AbstractPipelineStep {
    private static final Logger logger = LoggerFactory.getLogger(ConcretePipelineStepTwoTwo.class);

    private ProcessedTextData processedTextData;
    private ResultData resultData;

    public ConcretePipelineStepTwoTwo(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }

    private void fetchAndInitializeData() {
        var dataRepository = getDataRepository();
        processedTextData = dataRepository.getData("ProcessedTextData", ProcessedTextData.class).orElseThrow();
        resultData = new ResultData();
        dataRepository.addData("ResultData", resultData);
    }

    @Override
    public void process() {
        fetchAndInitializeData();
        logger.info("Greetings from {} with id {}", this.getClass().getSimpleName(), getId());
        var tokens = processedTextData.getImportantTokens();
        var tokenWithLength = tokens.stream().collect(Collectors.toMap(e -> e, String::length, (o1, o2) -> o1, TreeMap::new));
        var firstEntry = tokenWithLength.firstKey();
        resultData.setResult(firstEntry);
    }

    @Override
    protected void before() {
        //Nothing
    }

    @Override
    protected void after() {
        //Nothing
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // NOP
    }
}
