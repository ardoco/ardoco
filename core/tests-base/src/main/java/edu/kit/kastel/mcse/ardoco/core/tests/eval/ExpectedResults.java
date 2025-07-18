/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.Serializable;

/**
 * This record represents expected results for an evaluation
 *
 * @param precision      the expected precision
 * @param recall         the expected recall
 * @param f1             the expected F1 score
 * @param accuracy       the expected accuracy
 * @param phiCoefficient the expected Phi Coefficient
 * @param specificity    the expected specificity
 */
public record ExpectedResults(double precision, double recall, double f1, double accuracy, double phiCoefficient, double specificity) implements Serializable {
}
