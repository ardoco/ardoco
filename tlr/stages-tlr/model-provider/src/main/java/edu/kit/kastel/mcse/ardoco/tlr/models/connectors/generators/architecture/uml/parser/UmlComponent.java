/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements.PackagedElement;

public class UmlComponent extends UmlElement {

    private final List<UmlInterface> requiredInterfaces;
    private final List<UmlInterface> providedInterfaces;

    UmlComponent(PackagedElement componentElement, List<UmlInterface> interfaces) {
        super(componentElement);
        this.requiredInterfaces = loadRequired(interfaces);
        this.providedInterfaces = loadProvided(interfaces);
    }

    private List<UmlInterface> loadRequired(List<UmlInterface> interfaces) {
        var usages = element.getUsages();
        if (usages == null)
            return List.of();
        return usages.stream().map(usage -> findById(interfaces, usage.getSupplier())).toList();
    }

    private List<UmlInterface> loadProvided(List<UmlInterface> interfaces) {
        var realizations = element.getInterfaceRealizations();
        if (realizations == null)
            return List.of();
        return realizations.stream().map(realization -> findById(interfaces, realization.getSupplier())).toList();
    }

    private static UmlInterface findById(List<UmlInterface> interfaces, String id) {
        return interfaces.stream()
                .filter(it -> it.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not find interface with UMLId " + id));
    }

    public List<UmlInterface> getRequired() {
        return requiredInterfaces;
    }

    public List<UmlInterface> getProvided() {
        return providedInterfaces;
    }
}
