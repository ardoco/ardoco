/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.*;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;

public class OutgoingDependencyDto {
    private DependencyTag dependencyType;
    private long targetWordId;

    @JsonProperty("dependencyType")
    public DependencyTag getDependencyTag() {
        return dependencyType;
    }

    @JsonProperty("dependencyType")
    public void setDependencyTag(DependencyTag value) {
        this.dependencyType = value;
    }

    @JsonProperty("targetWordId")
    public long getTargetWordId() {
        return targetWordId;
    }

    @JsonProperty("targetWordId")
    public void setTargetWordId(long value) {
        this.targetWordId = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OutgoingDependencyDto that))
            return false;
        return targetWordId == that.targetWordId && Objects.equals(dependencyType, that.dependencyType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyType, targetWordId);
    }
}
