/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.dto.ArchitectureItemDto;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.dto.ArchitectureItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.dto.ArchitectureModelDto;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.dto.ComponentDto;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.dto.InterfaceDto;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.dto.MethodDto;
import edu.kit.kastel.mcse.ardoco.core.architecture.NoHashCodeEquals;

/**
 * Represents an architecture model.
 */
@NoHashCodeEquals
public abstract sealed class ArchitectureModel extends Model permits ArchitectureComponentModel, ArchitectureModelWithComponentsAndInterfaces {

    /**
     * Returns the content of the architecture model.
     *
     * @return list of architecture items
     */
    @Override
    public abstract List<? extends ArchitectureItem> getContent();

    /**
     * Returns the endpoints of this model.
     *
     * @return list of architecture items
     */
    @Override
    public abstract List<? extends ArchitectureItem> getEndpoints();

    /**
     * Creates a DTO for this architecture model suitable for JSON serialization.
     *
     * @return architecture model DTO
     */
    public ArchitectureModelDto createArchitectureModelDto() {
        SortedMap<String, ArchitectureItemDto> repository = new TreeMap<>();
        List<? extends ArchitectureItem> content = getContent();
        for (ArchitectureItem item : content) {
            collectItem(item, repository);
        }
        List<String> contentIds = content.stream().map(ArchitectureItem::getId).toList();
        return new ArchitectureModelDto(getId(), new ArchitectureItemRepository(repository, contentIds));
    }

    private static void collectItem(ArchitectureItem item, SortedMap<String, ArchitectureItemDto> repository) {
        if (repository.containsKey(item.getId())) {
            return;
        }
        switch (item) {
            case ArchitectureComponent component -> collectComponent(component, repository);
            case ArchitectureInterface iface -> collectInterface(iface, repository);
            case ArchitectureMethod method -> collectMethod(method, repository);
        }
    }

    private static void collectComponent(ArchitectureComponent component, SortedMap<String, ArchitectureItemDto> repository) {
        SortedSet<String> subcomponentIds = component.getSubcomponents()
                .stream()
                .map(ArchitectureComponent::getId)
                .collect(Collectors.toCollection(TreeSet::new));
        SortedSet<String> providedIds = component.getProvidedInterfaces()
                .stream()
                .map(ArchitectureInterface::getId)
                .collect(Collectors.toCollection(TreeSet::new));
        SortedSet<String> requiredIds = component.getRequiredInterfaces()
                .stream()
                .map(ArchitectureInterface::getId)
                .collect(Collectors.toCollection(TreeSet::new));
        repository.put(component.getId(), new ComponentDto(component.getId(), component.getName(), component.getType().orElse(null), subcomponentIds,
                providedIds, requiredIds));
        for (ArchitectureComponent sub : component.getSubcomponents()) {
            collectItem(sub, repository);
        }
        for (ArchitectureInterface iface : component.getProvidedInterfaces()) {
            collectItem(iface, repository);
        }
        for (ArchitectureInterface iface : component.getRequiredInterfaces()) {
            collectItem(iface, repository);
        }
    }

    private static void collectInterface(ArchitectureInterface iface, SortedMap<String, ArchitectureItemDto> repository) {
        SortedSet<String> methodIds = iface.getMethodSignatures().stream().map(ArchitectureMethod::getId).collect(Collectors.toCollection(TreeSet::new));
        repository.put(iface.getId(), new InterfaceDto(iface.getId(), iface.getName(), methodIds));
        for (ArchitectureMethod method : iface.getMethodSignatures()) {
            collectItem(method, repository);
        }
    }

    private static void collectMethod(ArchitectureMethod method, SortedMap<String, ArchitectureItemDto> repository) {
        repository.put(method.getId(), new MethodDto(method.getId(), method.getName()));
    }
}
