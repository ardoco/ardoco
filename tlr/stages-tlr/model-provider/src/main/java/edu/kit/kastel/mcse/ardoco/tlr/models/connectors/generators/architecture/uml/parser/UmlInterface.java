/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements.OwnedOperation;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements.PackagedElement;

public class UmlInterface extends UmlElement {

    private final List<OwnedOperation> operations;

    UmlInterface(PackagedElement interfaceElement) {
        super(interfaceElement);
        this.operations = interfaceElement.getOwnedOperations().stream().toList();
    }

    public List<OwnedOperation> getOperations() {
        return operations;
    }
}
