/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.NameComparisonUtils;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.Confidence;

public class ComponentNameResemblanceTest extends DependentHeuristic {

    private static final SortedSet<String> commonWords = new TreeSet<>(List.of("Test", "Action", "Impl", "Factory", "Exception"));

    @Override
    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        return this.calculateNameResemblance(archComponent, compUnit);
    }

    @Override
    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        if (!archInterface.getSignatures().isEmpty()) {
            return new Confidence();
        }
        return this.calculateNameResemblance(archInterface, compUnit);
    }

    private Confidence calculateNameResemblance(ArchitectureItem archEndpoint, CodeCompilationUnit compUnit) {
        if (this.getNodeResult().getConfidence(new Pair<>(archEndpoint, compUnit)).hasValue()) {
            return new Confidence();
        }
        SortedSet<String> filteredCommonWords = new TreeSet<>(commonWords);
        for (Entity ae : this.getArchModel().getEndpoints()) {
            filteredCommonWords = NameComparisonUtils.removeWords(filteredCommonWords, ae);
        }
        SortedSet<CodeItem> items = compUnit.getAllDataTypesAndSelf();
        if (areSimilar(items, new TreeSet<>(List.of(archEndpoint)), filteredCommonWords)) {
            return new Confidence(1.0);
        }
        Confidence maxConfidence = new Confidence();
        SortedSet<Entity> linkedEndpoints = this.getNodeResult().getLinkedEndpoints(archEndpoint);
        for (Entity linkedEndpoint : linkedEndpoints) {
            CodeCompilationUnit linkedCompUnit = (CodeCompilationUnit) linkedEndpoint;
            if (InheritLinks.areInDifferentPackages(compUnit, linkedCompUnit) || !areSimilar(items, linkedCompUnit.getAllDataTypesAndSelf(),
                    filteredCommonWords)) {
                continue;
            }
            Confidence extendedConfidence = this.getNodeResult().getConfidence(new Pair<>(archEndpoint, linkedCompUnit));
            if (extendedConfidence.compareTo(maxConfidence) > 0) {
                maxConfidence = extendedConfidence;
            }
        }
        return maxConfidence;
    }

    private static boolean areSimilar(SortedSet<? extends Entity> entities1, SortedSet<? extends Entity> entities2, SortedSet<String> filteredCommonWords) {
        for (var entity1 : entities1) {
            for (var entity2 : entities2) {
                List<String> words1 = NameComparisonUtils.removeWords(entity1, filteredCommonWords);
                List<String> words2 = NameComparisonUtils.removeWords(entity2, filteredCommonWords);
                if (words1.equals(words2)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ComponentNameResemblanceTest";
    }
}
