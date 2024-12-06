package edu.kit.kastel.mcse.ardoco.secdreqan.models;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.secdreqan.Requirement;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class RequirementData implements PipelineStepData {
    public static final String ID = "RequirementsData";
    private final SortedMap<String, Requirement> requirements = new TreeMap<>();

    public RequirementData() {
    }

    public SortedSet<String> requirementIds() {
        return new TreeSet<>(this.requirements.keySet());
    }

    public void addRequirement(Requirement requirement) {
        String uid = requirement.getUID();
        if (requirements.containsKey(uid)) {
            var currentRequirement = getRequirement(uid);
            if (currentRequirement.equals(requirement)) {
                currentRequirement.addComments(requirement.getComments());
                currentRequirement.addEntityLabels(requirement.getEntities());
            } else {
                throw new IllegalArgumentException("Requirement ID: " + uid + " is not unique");
            }
        } else {
            this.requirements.put(requirement.getUID(), requirement);
        }
    }

    public Requirement getRequirement(String uid) {
        return this.requirements.get(uid);
    }

}
