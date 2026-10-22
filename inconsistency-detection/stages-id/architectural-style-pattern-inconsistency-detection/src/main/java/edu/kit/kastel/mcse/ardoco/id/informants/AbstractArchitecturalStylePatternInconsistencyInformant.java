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
import edu.kit.kastel.mcse.ardoco.id.informants.llmagents.ArchitecturalStylePatternValidationResult;
import edu.kit.kastel.mcse.ardoco.id.informants.validators.ArchitecturalStylePatternStatementValidatorRegistry;
import edu.kit.kastel.mcse.ardoco.id.informants.validators.ArchitecturalStylePatternValidationContext;
import edu.kit.kastel.mcse.ardoco.id.states.ArchitecturalStylePatternInconsistency;
import edu.kit.kastel.mcse.ardoco.id.states.ArchitecturalStylePatternInconsistencyState;
import edu.kit.kastel.mcse.ardoco.id.states.ArchitecturalStylePatternInconsistencyStateImpl;
import edu.kit.kastel.mcse.ardoco.id.states.ArchitecturalStylePatternStatement;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public abstract class AbstractArchitecturalStylePatternInconsistencyInformant extends Informant {

    private static final Logger logger = LoggerFactory.getLogger(AbstractArchitecturalStylePatternInconsistencyInformant.class);

    protected final ArtemisNerStrategy nerStrategy;
    private final ArchitecturalStylePatternStatementValidatorRegistry validatorRegistry;

    protected AbstractArchitecturalStylePatternInconsistencyInformant(String id, DataRepository dataRepository, ArtemisNerStrategy nerStrategy,
            ArchitecturalStylePatternStatementValidatorRegistry validatorRegistry) {
        super(id, dataRepository);
        this.nerStrategy = nerStrategy;
        this.validatorRegistry = validatorRegistry;
    }

    @Override
    protected void process() {
        var namedEntityState = DataRepositoryHelper.getArtemisConnectionStates(dataRepository).getState(nerStrategy.getTarget());
        var architecturalStylePatternInconsistencyState = dataRepository.getData(ArchitecturalStylePatternInconsistencyState.ID,
                ArchitecturalStylePatternInconsistencyStateImpl.class).orElseThrow();

        var text = DataRepositoryHelper.getSimpleText(dataRepository);
        var lines = text.getLines();
        String fullDocumentationText = String.join(System.lineSeparator(), lines);

        List<ArchitecturalStylePatternStatement> validStatements = new ArrayList<>();
        List<ArchitecturalStylePatternInconsistency> inconsistencies = new ArrayList<>();

        for (var namedEntity : namedEntityState.getNamedEntities()) {
            String architecturalStylePatternName = namedEntity.getName();

            for (var occurrence : namedEntity.getOccurrences()) {
                int sentenceNumber = occurrence.getSentenceNumber();
                int lineIndex = sentenceNumber - 1;

                if (lineIndex < 0 || lineIndex >= lines.size()) {
                    logger.warn("Skipping architectural style/pattern occurrence outside text bounds: sentence {}", sentenceNumber);
                    continue;
                }

                String sentenceText = lines.get(lineIndex);
                var statement = new ArchitecturalStylePatternStatement(sentenceNumber, architecturalStylePatternName, sentenceText);

                var validationResult = validateWithSpecializedValidatorIfAvailable(occurrence.toString(), architecturalStylePatternName, fullDocumentationText,
                        sentenceText);

                if (validationResult.valid()) {
                    validStatements.add(statement);
                } else {
                    inconsistencies.add(new ArchitecturalStylePatternInconsistency(statement, validationResult.explanation()));
                }

                logger.debug("Validated architectural style/pattern statement '{}':\nvalid={}, explanation={}", statement, validationResult.valid(),
                        validationResult.explanation());
            }
        }

        architecturalStylePatternInconsistencyState.addValidatedStatements(Lists.mutable.ofAll(validStatements));
        architecturalStylePatternInconsistencyState.addInconsistencies(Lists.mutable.ofAll(inconsistencies));
    }

    private ArchitecturalStylePatternValidationResult validateWithSpecializedValidatorIfAvailable(String namedEntityOccurrence,
            String architecturalStylePatternName, String fullDocumentationText, String sentenceText) {
        var context = new ArchitecturalStylePatternValidationContext(dataRepository, namedEntityOccurrence, architecturalStylePatternName,
                fullDocumentationText, sentenceText, getCodeProjectPathOrNull());

        return validatorRegistry.findValidator(architecturalStylePatternName)
                .map(validator -> validator.validate(context))
                .orElseGet(() -> validateArchitecturalStylePatternStatement(namedEntityOccurrence, architecturalStylePatternName, fullDocumentationText,
                        sentenceText));
    }

    protected String getCodeProjectPathOrNull() {
        return null;
    }

    protected abstract ArchitecturalStylePatternValidationResult validateArchitecturalStylePatternStatement(String namedEntityOccurrence,
            String architecturalStylePatternName, String fullDocumentationText, String sentenceText);
}
