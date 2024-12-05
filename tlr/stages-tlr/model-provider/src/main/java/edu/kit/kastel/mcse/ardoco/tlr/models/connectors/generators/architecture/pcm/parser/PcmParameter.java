/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.parser;

import java.util.List;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass
public final class PcmParameter {

    @XMLValue
    private String parameterName;

    @XMLValue(name = "dataType__Parameter", mandatory = false)
    private String typeId;

    private PcmDatatype type;

    PcmParameter() {
        // NOP
    }

    public String getName() {
        return parameterName;
    }

    public PcmDatatype getType() {
        return type;
    }

    void init(List<PcmDatatype> datatypes) {
        type = datatypes.stream().filter(datatype -> datatype.getId().equals(typeId)).findFirst().orElse(null);
    }
}
