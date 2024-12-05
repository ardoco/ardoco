/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModule;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.Datatype;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.NameComparisonUtils;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.Confidence;

public class ProvidedInterfaceCorrespondence extends DependentHeuristic {

    @Override
    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        return this.calculateProvidedInterfaceCorrespondence(archComponent, compUnit);
    }

    private Confidence calculateProvidedInterfaceCorrespondence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        if (!this.getNodeResult().getConfidence(new Pair<>(archComponent, compUnit)).hasValue() || !compUnit.hasParent()) {
            return new Confidence();
        }

        SortedSet<Entity> interfaceLinks = new TreeSet<>();
        for (ArchitectureInterface providedInterface : archComponent.getProvidedInterfaces()) {
            interfaceLinks.addAll(this.getLinks(providedInterface));
        }

        SortedSet<CodeModule> componentPackage = this.getPackage(archComponent, compUnit);
        if (this.containsAny(interfaceLinks, componentPackage)) {
            return new Confidence();
        }

        SortedSet<CodeModule> allPackages = this.getPackages(archComponent, this.getLinks(archComponent));
        if (this.containsAny(interfaceLinks, allPackages)) {
            return new Confidence(1.0);
        }
        return new Confidence();
    }

    private boolean containsAny(SortedSet<Entity> interfaces, SortedSet<CodeModule> componentPackages) {
        if (componentPackages.isEmpty() || interfaces.isEmpty()) {
            return false;
        }
        return interfaces.stream().anyMatch(i -> componentPackages.stream().anyMatch(p -> this.overrides(p.getAllCompilationUnits(), i)));
    }

    private boolean overrides(SortedSet<CodeCompilationUnit> ces, Entity i) {
        for (CodeCompilationUnit ce : ces) {
            if (this.getAllOverridenTypes(ce).contains(i)) {
                return true;
            }
        }
        return false;
    }

    private SortedSet<CodeCompilationUnit> getAllOverridenTypes(CodeCompilationUnit ce) {
        SortedSet<CodeCompilationUnit> overridenCompUnits = new TreeSet<>();
        SortedSet<Datatype> overridenTypes = new TreeSet<>();
        for (Datatype codeType : ce.getAllDataTypes()) {
            overridenTypes.addAll(MethodResemblance.getAllExtendedTypes(codeType));
            MethodResemblance.getAllImplementedInterfaces(codeType).forEach(i -> overridenTypes.addAll(MethodResemblance.getAllExtendedTypes(i)));
        }
        overridenTypes.forEach(t -> overridenCompUnits.add(t.getCompilationUnit()));
        return overridenCompUnits;
    }

    private SortedSet<CodeCompilationUnit> getLinks(Entity ae) {
        SortedSet<CodeCompilationUnit> ces = new TreeSet<>();
        SortedSet<Entity> endpoints = this.getNodeResult().getLinkedEndpoints(ae);
        endpoints.forEach(endpoint -> ces.add((CodeCompilationUnit) endpoint));
        return ces;
    }

    private SortedSet<CodeModule> getPackages(Entity ae, SortedSet<CodeCompilationUnit> ces) {
        SortedSet<CodeModule> packages = new TreeSet<>();
        for (CodeCompilationUnit ce : ces) {
            packages.addAll(this.getPackage(ae, ce));
        }
        return packages;
    }

    private SortedSet<CodeModule> getPackage(Entity ae, CodeCompilationUnit ce) {
        List<CodePackage> cePackages = NameComparisonUtils.getMatchedPackages(ae, ce);
        if (!cePackages.isEmpty()) {
            return new TreeSet<>(List.of(cePackages.get(cePackages.size() - 1)));
        }
        if (ce.hasParent()) {
            return new TreeSet<>(List.of(ce.getParent()));
        }
        return new TreeSet<>(List.of());
    }

    @Override
    public String toString() {
        return "ProvidedInterfaceCorrespondence";
    }
}
