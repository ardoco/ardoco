/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.informants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ProgrammingLanguageValidationResult;
import edu.kit.kastel.mcse.ardoco.id.states.ProgrammingLanguageInconsistency;
import edu.kit.kastel.mcse.ardoco.id.states.ProgrammingLanguageInconsistencyState;
import edu.kit.kastel.mcse.ardoco.id.states.ProgrammingLanguageInconsistencyStateImpl;
import edu.kit.kastel.mcse.ardoco.id.states.ProgrammingLanguageStatement;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

/**
 * Base informant for detecting inconsistencies between programming language statements in the documentation and the available code representation.
 * <p>
 * This informant processes programming language named entities found in the documentation, validates each occurrence, and stores either validated statements or
 * detected inconsistencies in the {@link ProgrammingLanguageInconsistencyState}. Subclasses provide the concrete validation mechanism, for example by checking
 * a code model or a source-code directory.
 * </p>
 */
public abstract class AbstractProgrammingLanguageInconsistencyInformant extends Informant {

    private static final Logger logger = LoggerFactory.getLogger(AbstractProgrammingLanguageInconsistencyInformant.class);

    protected final ArtemisNerStrategy nerStrategy;

    protected AbstractProgrammingLanguageInconsistencyInformant(String id, DataRepository dataRepository, ArtemisNerStrategy nerStrategy) {
        super(id, dataRepository);
        this.nerStrategy = nerStrategy;
    }

    @Override
    protected void process() {
        var namedEntityState = DataRepositoryHelper.getArtemisConnectionStates(dataRepository).getState(nerStrategy.getTarget());
        var programmingLanguageInconsistencyState = dataRepository.getData(ProgrammingLanguageInconsistencyState.ID,
                ProgrammingLanguageInconsistencyStateImpl.class).orElseThrow();

        var text = DataRepositoryHelper.getSimpleText(dataRepository);
        var lines = text.getLines();
        String fullDocumentationText = String.join(System.lineSeparator(), lines);

        List<ProgrammingLanguageStatement> validStatements = new ArrayList<>();
        List<ProgrammingLanguageInconsistency> inconsistencies = new ArrayList<>();

        for (var namedEntity : namedEntityState.getNamedEntities()) {
            String programmingLanguageName = namedEntity.getName();

            for (var occurrence : namedEntity.getOccurrences()) {
                int sentenceNumber = occurrence.getSentenceNumber();
                int lineIndex = sentenceNumber - 1;

                if (lineIndex < 0 || lineIndex >= lines.size()) {
                    logger.warn("Skipping programming language occurrence outside text bounds: sentence {}", sentenceNumber);
                    continue;
                }

                String sentenceText = lines.get(lineIndex);
                var statement = new ProgrammingLanguageStatement(sentenceNumber, programmingLanguageName, sentenceText);

                var validationResult = validateProgrammingLanguageStatement(occurrence.toString(), programmingLanguageName, fullDocumentationText,
                        sentenceText);

                if (validationResult.valid()) {
                    validStatements.add(statement);
                } else {
                    inconsistencies.add(new ProgrammingLanguageInconsistency(statement, validationResult.explanation()));
                }

                logger.debug("Validated programming-language statement '{}':\nvalid={}, explanation={}", statement, validationResult.valid(), validationResult
                        .explanation());
            }
        }

        programmingLanguageInconsistencyState.addValidatedStatements(Lists.mutable.ofAll(validStatements));
        programmingLanguageInconsistencyState.addInconsistencies(Lists.mutable.ofAll(inconsistencies));
    }

    /**
     * Validates a programming language-related statement extracted from a text.
     *
     * @param namedEntityOccurrence   the named entity occurrence being validated
     * @param programmingLanguageName the name of the programming language referenced
     * @param fullDocumentationText   the full text of the documentation being analyzed
     * @param sentenceText            the specific sentence containing the programming language statement
     * @return a {@code ProgrammingLanguageValidationResult} that indicates whether the statement is valid and provides an explanation for its validity or
     *         inconsistency
     */
    protected abstract ProgrammingLanguageValidationResult validateProgrammingLanguageStatement(String namedEntityOccurrence, String programmingLanguageName,
            String fullDocumentationText, String sentenceText);
}
