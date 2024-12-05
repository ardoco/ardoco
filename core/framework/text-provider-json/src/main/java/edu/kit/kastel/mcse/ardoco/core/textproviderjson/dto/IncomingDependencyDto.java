/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.*;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;

public class IncomingDependencyDto {
    private DependencyTag dependencyType;
    private long sourceWordId;

    @JsonProperty("dependencyType")
    public DependencyTag getDependencyTag() {
        return dependencyType;
    }

    @JsonProperty("dependencyType")
    public void setDependencyTag(DependencyTag value) {
        this.dependencyType = value;
    }

    @JsonProperty("sourceWordId")
    public long getSourceWordId() {
        return sourceWordId;
    }

    @JsonProperty("sourceWordId")
    public void setSourceWordId(long value) {
        this.sourceWordId = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof IncomingDependencyDto that))
            return false;
        return sourceWordId == that.sourceWordId && dependencyType == that.dependencyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyType, sourceWordId);
    }
}
