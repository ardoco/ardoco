package edu.kit.kastel.mcse.ardoco.secdreqan;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SecurityObjective {

    @JsonProperty("confidentiality") CONFIDENTIALITY, @JsonProperty("integrity") INTEGRITY, @JsonProperty("availability") AVAILABILITY, @JsonProperty("authentication") AUTHENTICATION, @JsonProperty("other") OTHER
}
