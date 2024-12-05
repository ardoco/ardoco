/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.impl.ResultData;
import edu.kit.kastel.mcse.ardoco.core.data.impl.TextData;
import edu.kit.kastel.mcse.ardoco.core.pipeline.impl.ConcretePipelineStepOne;
import edu.kit.kastel.mcse.ardoco.core.pipeline.impl.ConcretePipelineStepTwoOne;
import edu.kit.kastel.mcse.ardoco.core.pipeline.impl.ConcretePipelineStepTwoTwo;

/**
 * Test that defines an example pipeline and executes it.
 */
class PipelineTest {
    private static final Logger logger = LoggerFactory.getLogger(PipelineTest.class);

    @Test
    void pipelineDefinitionTest() {
        DataRepository dataRepository = new DataRepository();
        String text = "This is an example text containing multiple words.";
        logger.info("Input: \"{}\"", text);
        TextData textData = new TextData(text);
        dataRepository.addData("Text", textData);
        Pipeline pipeline = new Pipeline("Main-Pipeline", dataRepository);

        pipeline.addPipelineStep(new ConcretePipelineStepOne("Preprocessing", dataRepository));
        List<AbstractPipelineStep> pipelineSteps = List.of(new ConcretePipelineStepTwoOne("Main-Processing 2.1 First", dataRepository),
                new ConcretePipelineStepTwoOne("Main-Processing 2.1 Second", dataRepository), new ConcretePipelineStepTwoTwo("Main-Processing 2",
                        dataRepository));
        pipeline.addPipelineStep(new Pipeline("Processing-Pipeline", dataRepository, pipelineSteps));

        pipeline.run();

        var resultData = dataRepository.getData("ResultData", ResultData.class).orElseThrow();
        logger.info("Result: {}", resultData.getResult());
        Assertions.assertNotNull(resultData);
    }
}
