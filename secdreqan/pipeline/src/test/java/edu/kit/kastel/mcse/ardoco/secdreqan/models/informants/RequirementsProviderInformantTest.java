package edu.kit.kastel.mcse.ardoco.secdreqan.models.informants;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.secdreqan.Requirement;
import edu.kit.kastel.mcse.ardoco.secdreqan.RequirementsExtractor;
import edu.kit.kastel.mcse.ardoco.secdreqan.models.RequirementData;
import org.junit.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public class RequirementsProviderInformantTest {

    protected File inputFile1 = new File("../evaluation/EVerest_Components_R1.jsonl");
    protected File inputFile2 = new File("../evaluation/EVerest_Components_R2.jsonl");
    protected final String REQUIREMENT_DATA = "RequirementData";


    @BeforeEach
    public void checkFileExistence(){
        Assumptions.assumeTrue(inputFile1.exists());
        Assumptions.assumeTrue(inputFile2.exists());
    }


    @Test
    @DisplayName("Create RequirementData")
    public void testRequirementDataCreation() {
        DataRepository dataRepository = new DataRepository();
        RequirementsProviderInformant informant = new RequirementsProviderInformant(dataRepository, List.of(inputFile1));
        informant.process();
        Optional<RequirementData> requirementsDataOptional = dataRepository.getData(REQUIREMENT_DATA, RequirementData.class);

        assertThat(requirementsDataOptional).isPresent().isNotEmpty();

        List<Requirement> requirements = RequirementsExtractor.extractRequirementsFromJSONL(inputFile1);
        Requirement fileRequirement = requirements.getFirst();

        Requirement repoRequirement = requirementsDataOptional.get().getRequirement(fileRequirement.getUID());
        assertThat(repoRequirement).isNotNull().isEqualTo(fileRequirement);

        assertThat(repoRequirement.getEntities()).hasSameElementsAs(fileRequirement.getEntities());
        assertThat(repoRequirement.getComments()).hasSameElementsAs(fileRequirement.getComments());
    }

    @Test
    @DisplayName("Merge RequirementData")
    public void testRequirementDataMerge() {
        DataRepository dataRepository = new DataRepository();
        RequirementsProviderInformant informant = new RequirementsProviderInformant(dataRepository, List.of(inputFile1, inputFile2));
        informant.process();
        Optional<RequirementData> requirementsDataOptional = dataRepository.getData(REQUIREMENT_DATA, RequirementData.class);

        assertThat(requirementsDataOptional).isPresent().isNotEmpty();

        Requirement file1Requirement = RequirementsExtractor.extractRequirementsFromJSONL(inputFile1).get(1);
        Requirement file2Requirement = RequirementsExtractor.extractRequirementsFromJSONL(inputFile2).get(1);

        Requirement repoRequirement = requirementsDataOptional.get().getRequirement(file1Requirement.getUID());
        assertThat(repoRequirement).isNotNull().isEqualTo(file1Requirement).isEqualTo(file2Requirement);

        assertThat(repoRequirement.getEntities()).containsAll(file1Requirement.getEntities()).containsAll(file2Requirement.getEntities());
        assertThat(repoRequirement.getComments()).containsAll(file1Requirement.getComments()).containsAll(file2Requirement.getComments());
    }

}
