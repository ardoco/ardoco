/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.pipeline.impl;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.impl.TextData;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;

/**
 * Example implementation of {@link AbstractPipelineStep}
 */
public class ConcretePipelineStepOne extends AbstractPipelineStep {
    private static final Logger logger = LoggerFactory.getLogger(ConcretePipelineStepOne.class);

    private TextData textData;
    private final List<String> stopwords = List.of("is", "an", ".", "This");

    public ConcretePipelineStepOne(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }

    private void fetchData() {
        this.textData = getDataRepository().getData("Text", TextData.class).orElseThrow();
    }

    @Override
    public void process() {
        fetchData();
        logger.info("Greetings from {} with id {}", this.getClass().getSimpleName(), getId());
        var text = textData.getText();
        var tokens = Arrays.stream(text.split(" ")).toList();
        tokens = tokens.stream().filter(Predicate.not(stopwords::contains)).toList();
        textData.setTokens(tokens);
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
