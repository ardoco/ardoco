/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.parser;

import java.util.ArrayList;
import java.util.List;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;
import org.jspecify.annotations.Nullable;

@XMLClass
public final class PcmSignature {

    @XMLValue
    private String id;

    @XMLValue
    private String entityName;

    @XMLValue(name = "returnType__OperationSignature", mandatory = false)
    private String returnTypeId;

    @XMLList(name = "parameters__OperationSignature", elementType = PcmParameter.class)
    private List<PcmParameter> parameters;

    private @Nullable PcmDatatype returnType;

    PcmSignature() {
        // NOP
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public @Nullable PcmDatatype getReturnType() {
        return returnType;
    }

    public List<PcmParameter> getParameters() {
        return new ArrayList<>(parameters);
    }

    void init(List<PcmDatatype> datatypes) {
        for (PcmParameter parameter : parameters) {
            parameter.init(datatypes);
        }
        returnType = datatypes.stream().filter(datatype -> datatype.getId().equals(returnTypeId)).findFirst().orElse(null);
    }
}
