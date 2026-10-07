/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.parser;

import java.util.List;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;
import org.jspecify.annotations.Nullable;

@XMLClass
public final class PcmParameter {

    @XMLValue
    private String parameterName;

    @XMLValue(name = "dataType__Parameter", mandatory = false)
    private String typeId;

    private @Nullable PcmDatatype type;

    PcmParameter() {
        // NOP
    }

    public String getName() {
        return parameterName;
    }

    public @Nullable PcmDatatype getType() {
        return type;
    }

    void init(List<PcmDatatype> datatypes) {
        type = datatypes.stream().filter(datatype -> datatype.getId().equals(typeId)).findFirst().orElse(null);
    }
}
