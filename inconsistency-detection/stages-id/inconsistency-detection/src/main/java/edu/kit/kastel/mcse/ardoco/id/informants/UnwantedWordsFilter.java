/* Licensed under MIT 2022-2025. */
package edu.kit.kastel.mcse.ardoco.id.informants;

import static edu.kit.kastel.mcse.ardoco.core.common.JsonHandling.createObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import com.fasterxml.jackson.core.type.TypeReference;

import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;

// This is only a prototype. It should be improved, e.g, using following ideas:
// - use ComputerScienceWords
// - improve handling: instead of filtering them completely, reduce probability? Idea: we don't want to remove super high probability RIs
// - maybe consider phrases
// - maybe check if the word is a named entity or starts with capital letter in the middle of a sentence (sign of being a named entity)
public class UnwantedWordsFilter extends Filter {

    @Configurable
    private List<String> commonFileEndings = //
            List.of("pdf", "png", "md", "xml", "yml", "json", "html", "sh", "bat", //
                    "java", "gradle", "cpp", "c", "h", "groovy", "js", "ts", "css", "sc", "scala");

    @Configurable
    private boolean enableCommonBlacklist = true;
    private final List<String> commonBlacklist;

    @Configurable
    private List<String> customBlacklist = List.of("meta", "log", "browser", "task", "operation", "case", "instance", "script");

    public UnwantedWordsFilter(DataRepository dataRepository) {
        super(UnwantedWordsFilter.class.getSimpleName(), dataRepository);
        this.commonBlacklist = this.loadCommonBlacklist();
    }

    @Override
    protected void filterRecommendedInstances(InconsistencyState inconsistencyState) {
        var recommendedInstancesToKeep = Lists.mutable.<RecommendedInstance>empty();
        var recommendedInstances = inconsistencyState.getRecommendedInstances();

        for (var recommendedInstance : recommendedInstances) {
            boolean shallBeFiltered = this.checkRecommendedInstance(recommendedInstance);
            if (!shallBeFiltered) {
                recommendedInstancesToKeep.add(recommendedInstance);
            }
        }

        inconsistencyState.setRecommendedInstances(recommendedInstancesToKeep.toImmutable());
    }

    private boolean checkRecommendedInstance(RecommendedInstance recommendedInstance) {
        for (var nounMapping : recommendedInstance.getNameMappings()) {
            // apply heuristics
            if (this.referenceContainsUnwantedWord(nounMapping) || referenceContainsPluralWord(nounMapping) || referenceContainsOnlyNumbers(nounMapping) || this
                    .referenceEndsWithFileEnding(nounMapping)) {
                return true;
            }
        }
        return false;
    }

    private boolean referenceContainsUnwantedWord(NounMapping nounMapping) {
        var referenceWords = nounMapping.getReferenceWords();
        for (var referenceWord : referenceWords) {
            var lemma = referenceWord.getLemma().toLowerCase();
            if (this.customBlacklist.contains(lemma) || (this.enableCommonBlacklist && this.commonBlacklist.contains(lemma))) {
                return true;
            }
        }

        return false;
    }

    private static boolean referenceContainsPluralWord(NounMapping nounMapping) {
        for (var word : nounMapping.getReferenceWords()) {
            if (word.getPosTag().name().toLowerCase().contains("plural")) {
                return true;
            }
        }
        return false;
    }

    private static boolean referenceContainsOnlyNumbers(NounMapping nounMapping) {
        var reference = nounMapping.getReference().toLowerCase();
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(reference);
        return matcher.matches();
    }

    private boolean referenceEndsWithFileEnding(NounMapping nounMapping) {
        var referenceWords = nounMapping.getReferenceWords();
        for (var referenceWord : referenceWords) {
            var text = referenceWord.getText().toLowerCase();
            for (var commonFileEnding : this.commonFileEndings) {
                if (text.endsWith("." + commonFileEnding)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(ImmutableSortedMap<String, String> map) {
        // nothing
    }

    private List<String> loadCommonBlacklist() {
        try {
            return Collections.unmodifiableList(createObjectMapper().readValue(this.getClass().getResourceAsStream("/unwanted_words_filter_common.json"),
                    new TypeReference<List<String>>() {
                    }));
        } catch (IOException e) {
            this.getLogger().error(e.getMessage(), e);
            return List.of();
        }
    }

}
