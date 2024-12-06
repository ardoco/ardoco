package edu.kit.kastel.mcse.ardoco.secdreqan;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.PcmExtractor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        String inputTextDir = "pipeline/src/test/resources/everest_dataset.json";
        File architecture = new File("pipeline/src/test/resources/everest.repository");
        File additionalConfig = new File("pipeline/src/test/resources/additionalConfig.txt");
        String outputDir = "../target";

        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(additionalConfig);

        List<File> inputRequirements = RequirementsExtractor.writeRequirementsTextsToFiles(new File(inputTextDir), outputDir + "/requirements");

        SecDReqAnMultiRunner runner = new SecDReqAnMultiRunner("EVerest");
        runner.varyTextkeepSAM(inputRequirements, architecture, ArchitectureModelType.PCM, additionalConfigsMap, outputDir);
        ArchitectureModel architectureModel = new PcmExtractor(architecture.getPath()).extractModel();
        printArchitecture(architectureModel);

        generateGoldStandardFiles(inputRequirements.size(), outputDir);

    }

    private static void generateGoldStandardFiles(int size, String outputDir) {

        outputDir = outputDir + "/goldstandard/generatedFiles";
        new File(outputDir).mkdirs();

        FileWriter fw = null;

        for (int i = 0; i < size; i++) {
            String fileName = "requirement_" + i + "_goldstandard.csv";

            try {
                File file = new File(outputDir, fileName);
                fw = new FileWriter(file);
                fw.write("modelElementID,sentence");
                fw.close();
            } catch (IOException e) {
                throw new IllegalStateException("File Writer could not write");
            }
        }

    }

    public static void printArchitecture(ArchitectureModel architectureModel) {
        Map<String, String> architectureItemMap = new TreeMap<>();

        for (ArchitectureItem architectureItem : architectureModel.getEndpoints()) {
            architectureItemMap.put(architectureItem.getName(), architectureItem.getId());
        }
        printMap(architectureItemMap, "../target/architectureItems.txt");

    }

    private static void printMap(Map<String, String> map, String outputDir) {
        File file = new File(outputDir);

        BufferedWriter bf = null;

        try {
            bf = new BufferedWriter(new FileWriter(file));
            for (Map.Entry<String, String> entry : map.entrySet()) {

                bf.write(entry.getKey() + ":" + entry.getValue());

                bf.newLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Buffered Writer could not write");
        }
        try {
            bf.close();
        } catch (IOException e) {
            throw new IllegalStateException("Buffered Writer could not be closed");
        }
    }
}
