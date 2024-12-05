/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModule;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.InterfaceUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguages;

public class LegacyModelExtractionStateByArCoTL implements LegacyModelExtractionState {
    private static final long serialVersionUID = -9079063953852495629L;

    private static final Logger logger = LoggerFactory.getLogger(LegacyModelExtractionStateByArCoTL.class);

    private final String modelId;
    private final Metamodel metamodel;

    private final ImmutableList<ModelInstance> instances;

    private final MutableSortedSet<String> instanceTypes;
    private final MutableSortedSet<String> names;
    private SortedMap<String, String> lastConfig;

    public LegacyModelExtractionStateByArCoTL(ArchitectureModel architectureModel) {
        this.modelId = architectureModel.getId();
        this.instances = LegacyModelExtractionStateByArCoTL.initArchitectureInstances(architectureModel);
        this.metamodel = Metamodel.ARCHITECTURE;
        this.instanceTypes = SortedSets.mutable.empty();
        this.names = SortedSets.mutable.empty();
        this.collectTypesAndNames();
    }

    public LegacyModelExtractionStateByArCoTL(CodeModel codeModel) {
        this.modelId = codeModel.getId();
        this.instances = LegacyModelExtractionStateByArCoTL.initCodeInstances(codeModel);
        this.metamodel = Metamodel.CODE;

        this.instanceTypes = SortedSets.mutable.empty();
        this.names = SortedSets.mutable.empty();
        this.collectTypesAndNames();
    }

    private static ImmutableList<ModelInstance> initArchitectureInstances(ArchitectureModel architectureModel) {
        MutableList<ModelInstance> instances = Lists.mutable.empty();
        for (ArchitectureItem architectureItem : architectureModel.getEndpoints()) {
            switch (architectureItem) {
                case ArchitectureComponent component -> instances.add(new ModelInstanceImpl(component.getName(), component.getType(), component.getId()));
                case ArchitectureInterface ignored -> LegacyModelExtractionStateByArCoTL.logger.debug("Skipping .. ArchitectureInterface not supported yet");
                case ArchitectureMethod ignored -> LegacyModelExtractionStateByArCoTL.logger.debug("Skipping .. ArchitectureMethod not supported yet");
            }
        }
        return instances.toImmutable();
    }

    private static ImmutableList<ModelInstance> initCodeInstances(CodeModel codeModel) {
        List<ModelInstance> instances = new ArrayList<>();
        LegacyModelExtractionStateByArCoTL.fillPackages(codeModel.getAllPackages(), instances);
        LegacyModelExtractionStateByArCoTL.fillCompilationUnits(codeModel.getEndpoints(), instances);
        return Lists.immutable.withAll(instances);
    }

    private static void fillPackages(Collection<? extends CodePackage> packages, List<ModelInstance> instances) {
        for (var modelElement : packages) { 
            String path = modelElement.getName();
            CodeModule parent = modelElement.getParent();
            while (parent != null) {
                path = parent.getName() + "/" + path;
                parent = parent.getParent();
            }
            // Ensure that package is handled as directory
            path += "/";
            instances.add(new ModelInstanceImpl(modelElement.getName(), "Package", path));
        }
    }

    private static void fillCompilationUnits(Collection<? extends CodeCompilationUnit> units, List<ModelInstance> instances) {
        for (var unit : units) {
            String type = LegacyModelExtractionStateByArCoTL.findType(unit);
            instances.add(new ModelInstanceImpl(unit.getName(), type, unit.getPath()));
        }

    }

    private static String findType(CodeCompilationUnit unit) {
        // Assumption mostly one class per unit
        var content = unit.getContent().stream().filter(it -> unit.getName().contains(it.getName())).findFirst().orElse(null);
        if (content instanceof ClassUnit) {
            return "Class";
        }
        if (content instanceof InterfaceUnit) {
            return "Interface";
        }
        if (unit.getPath().endsWith("package-info.java")) {
            return "PackageInfo";
        }
        if (unit.getPath().endsWith(".java")) {
            // Default to Class
            return "Class";
        }
        if (ProgrammingLanguages.SHELL.equals(unit.getLanguage())) {
            return "ShellScript";
        }
        throw new IllegalStateException("Unknown type of CodeCompilationUnit");
    }

    private void collectTypesAndNames() {
        for (ModelInstance i : this.instances) {
            this.instanceTypes.addAll(i.getTypeParts().castToCollection());
            this.names.addAll(i.getNameParts().castToCollection());
        }
    }

    @Override
    public String getModelId() {
        return this.modelId;
    }

    @Override
    public Metamodel getMetamodel() {
        return this.metamodel;
    }

    @Override
    public ImmutableList<ModelInstance> getInstancesOfType(String type) {
        return this.instances.select(i -> i.getTypeParts().contains(type));
    }

    @Override
    public ImmutableSortedSet<String> getInstanceTypes() {
        return this.instanceTypes.toImmutable();
    }

    @Override
    public ImmutableSortedSet<String> getNames() {
        return this.names.toImmutable();
    }

    @Override
    public ImmutableList<ModelInstance> getInstances() {
        return this.instances;
    }

    @Override
    public String toString() {
        var output = new StringBuilder("Instances:\n");
        for (ModelInstance i : this.instances) {
            output.append(i.toString()).append("\n");
        }
        return output.toString();
    }

    @Override
    public void applyConfiguration(SortedMap<String, String> additionalConfiguration) {
        this.lastConfig = new TreeMap<>(additionalConfiguration);
    }

    @Override
    public SortedMap<String, String> getLastAppliedConfiguration() {
        return this.lastConfig;
    }
}
