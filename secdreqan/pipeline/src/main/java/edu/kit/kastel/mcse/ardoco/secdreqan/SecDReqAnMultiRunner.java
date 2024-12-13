package edu.kit.kastel.mcse.ardoco.secdreqan;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class SecDReqAnMultiRunner {

    private final String projectName;

    protected SecDReqAnMultiRunner(String projectName) {
        this.projectName = projectName;
    }

    public Map<String, ArDoCoResult> varyTextkeepSAM(List<File> inputFiles, File inputArchitectureModel, ArchitectureModelType architectureModelType,
            SortedMap<String, String> additionalConfigs, String outputDirectory) {


        Map<String, ArDoCoResult> resultMap = new LinkedHashMap<>();

        File architecture_out = new File(outputDirectory, "architecture");


        for (File inputFile : inputFiles) {
            SecDReqAnRunner runner = new SecDReqAnRunner(projectName);
            runner.setUp(inputArchitectureModel, architectureModelType, additionalConfigs, architecture_out);
            runner.run();
            var dataRepositoryWithModel = runner.getArDoCo().getDataRepository();
            runner = new SecDReqAnRunner(projectName);
            var requirementId = inputFile.getName();
            File tracelink_out = new File(outputDirectory, "traceLinks/" + requirementId.substring(0, requirementId.lastIndexOf('.')));
            //TODO: Implement deepCopy
            // var dataRepositoryVariation = dataRepositoryWithModel.deepCopy();
            runner.setUp(inputFile, dataRepositoryWithModel, additionalConfigs, tracelink_out);
            ArDoCoResult result = runner.run();
            resultMap.put(requirementId.replace(".txt",""), result);
        }

        return resultMap;
    }

}
