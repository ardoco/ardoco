/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics;

import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.Datatype;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.NameComparisonUtils;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.Confidence;

public class MethodResemblance extends StandaloneHeuristic {

    @Override
    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        int numArchMethods = archInterface.getSignatures().size();
        if (0 == numArchMethods) {
            return new Confidence();
        }

        SortedSet<ControlElement> firstMethods = compUnit.getDeclaredMethods();
        for (Datatype datatype : compUnit.getAllDataTypes()) {
            for (ControlElement codeMethod : datatype.getDeclaredMethods()) {
                if (!isImplementedMethod(codeMethod, datatype) && !isExtendedMethod(codeMethod, datatype)) {
                    firstMethods.add(codeMethod);
                }
            }
        }

        //

        double sumConfidenceValue = 0.0;
        for (ArchitectureMethod archMethod : archInterface.getSignatures()) {
            double maxConfidenceValue = 0.0;
            for (ControlElement codeMethod : firstMethods) {
                double similarity = NameComparisonUtils.areEqual(archMethod, codeMethod) ? 1.0 : 0.0;
                maxConfidenceValue = Math.max(maxConfidenceValue, similarity);
            }
            sumConfidenceValue += maxConfidenceValue;
        }
        if (sumConfidenceValue <= 1) {
            return new Confidence();
        }
        double avgConfidenceValue = (2 * sumConfidenceValue) / (numArchMethods + firstMethods.size());

        // could be necessary if one code-method-name is equal to multiple arch-method-names and there are more arch-methods than code-methods
        avgConfidenceValue = Math.min(avgConfidenceValue, 1.0);

        return new Confidence(avgConfidenceValue);
    }

    private boolean isImplementedMethod(ControlElement codeMethod, Datatype codeType) {
        SortedSet<ControlElement> implMethods = new TreeSet<>();
        getAllImplementedInterfaces(codeType).forEach(i -> getAllExtendedTypes(i).forEach(j -> implMethods.addAll(j.getDeclaredMethods())));
        return implMethods.stream().anyMatch(implMethod -> implMethod.getName().equalsIgnoreCase(codeMethod.getName()));
    }

    private boolean isExtendedMethod(ControlElement codeMethod, Datatype codeType) {
        SortedSet<ControlElement> extendedMethods = new TreeSet<>();
        SortedSet<Datatype> extendedTypes = getAllExtendedTypes(codeType);
        extendedTypes.remove(codeType);
        extendedTypes.forEach(i -> extendedMethods.addAll(i.getDeclaredMethods()));
        return extendedMethods.stream().anyMatch(i -> i.getName().equalsIgnoreCase(codeMethod.getName()));
    }

    // returns extended types + type itself
    public static SortedSet<Datatype> getAllExtendedTypes(Datatype codeType) {
        SortedSet<Datatype> allExtendedTypes = new TreeSet<>();
        allExtendedTypes.add(codeType);
        for (Datatype extendedType : codeType.getExtendedTypes()) {
            allExtendedTypes.add(extendedType);
            allExtendedTypes.addAll(getAllExtendedTypes(extendedType));
        }
        return allExtendedTypes;
    }

    public static SortedSet<Datatype> getAllImplementedInterfaces(Datatype codeType) {
        SortedSet<Datatype> allImplementedInterfaces = new TreeSet<>();
        for (Datatype extendedType : getAllExtendedTypes(codeType)) {
            allImplementedInterfaces.addAll(extendedType.getImplementedTypes());
        }
        return allImplementedInterfaces;
    }

    @Override
    public String toString() {
        return "MethodResemblance";
    }
}
