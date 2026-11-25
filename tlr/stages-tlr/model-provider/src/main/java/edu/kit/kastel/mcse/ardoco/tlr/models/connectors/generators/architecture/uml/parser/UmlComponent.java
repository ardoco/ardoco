/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser;

import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.parser.xmlelements.PackagedElement;

public class UmlComponent extends UmlElement {

    private @Nullable List<UmlInterface> requiredInterfaces;
    private @Nullable List<UmlInterface> providedInterfaces;

    UmlComponent(PackagedElement componentElement) {
        super(componentElement);
    }

    void init(UmlModelRoot umlModelRoot) {
        this.requiredInterfaces = loadRequired(umlModelRoot);
        this.providedInterfaces = loadProvided(umlModelRoot);
    }

    private List<UmlInterface> loadRequired(UmlModelRoot umlModelRoot) {
        var usages = element.getUsages();
        if (usages == null)
            return List.of();
        return usages.stream().map(usage -> findById(umlModelRoot, usage.getSupplier())).toList();
    }

    private List<UmlInterface> loadProvided(UmlModelRoot umlModelRoot) {
        var realizations = element.getInterfaceRealizations();
        if (realizations == null)
            return List.of();
        return realizations.stream().map(realization -> findById(umlModelRoot, realization.getSupplier())).toList();
    }

    private static UmlInterface findById(UmlModelRoot umlModelRoot, String id) {
        return umlModelRoot.getInterfaces()
                .stream()
                .filter(it -> it.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not find interface with UMLId " + id));
    }

    public List<UmlInterface> getRequired() {
        return Objects.requireNonNull(requiredInterfaces, "The component has not been initialized");
    }

    public List<UmlInterface> getProvided() {
        return Objects.requireNonNull(providedInterfaces, "The component has not been initialized");
    }
}
