package edu.kit.kastel.mcse.ardoco.secdreqan.models.informants;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.secdreqan.Requirement;
import edu.kit.kastel.mcse.ardoco.secdreqan.RequirementsExtractor;
import edu.kit.kastel.mcse.ardoco.secdreqan.models.RequirementData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;

// TODO: Separate ProviderInformant which only should store the raw data in the data repository and
//  GoldstandardBuilder which merges and interprets labels and comments

public class RequirementsProviderInformant extends Informant {

    private static final String REQUIREMENT_DATA = "RequirementData";

    private final List<File> fromFiles = new ArrayList<>();

    private RequirementsProviderInformant() {
        super(null, null);
    }

    protected RequirementsProviderInformant(DataRepository dataRepository, List<File> fromFiles) {
        super("Extractor File", dataRepository);
        this.fromFiles.addAll(fromFiles);
    }

    @Override
    protected void process() {

        for (File file : fromFiles) {
            List<Requirement> requirements = RequirementsExtractor.extractRequirementsFromJSONL(file);

            DataRepository dataRepository = this.getDataRepository();
            Optional<RequirementData> requirementsDataOptional = dataRepository.getData(REQUIREMENT_DATA, RequirementData.class);

            var requirementsData = requirementsDataOptional.orElseGet(RequirementData::new);
            requirements.forEach(requirementsData::addRequirement);

            if (requirementsDataOptional.isEmpty()) {
                dataRepository.addData(REQUIREMENT_DATA, requirementsData);
            }
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> map) {
        // empty
    }

}
