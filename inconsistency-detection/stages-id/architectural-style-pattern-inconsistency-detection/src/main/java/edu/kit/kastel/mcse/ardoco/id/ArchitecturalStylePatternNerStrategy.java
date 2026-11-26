/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.Prompt;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.TwoPartPrompt;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ArchitecturalStylePatternNerStrategy implements ArtemisNerStrategy {

    private static final String TASK_PROMPT_RESOURCE = "architectural-style-pattern-task-prompt.txt";
    private static final String FORMATTING_PROMPT_RESOURCE = "architectural-style-pattern-formatting-prompt.txt";

    @Override
    public Prompt createPrompt(DataRepository dataRepository) {
        String taskPrompt = readPromptResource(TASK_PROMPT_RESOURCE);
        String formattingPrompt = readPromptResource(FORMATTING_PROMPT_RESOURCE);

        return new TwoPartPrompt(taskPrompt, formattingPrompt);
    }

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.CODE_WITH_COMPILATION_UNITS;
    }

    @Override
    public NamedEntityType getNamedEntityType() {
        return NamedEntityType.ARCHITECTURAL_PATTERN;
    }
}
