/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

import java.io.Serializable;

import com.google.errorprone.annotations.Immutable;

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
@Immutable
public record ExpectedResults(double precision, double recall, double f1, double accuracy, double phiCoefficient, double specificity) implements Serializable {

    public ExpectedResults(double precision, double recall, double f1) {
        this(precision, recall, f1, .0, .0, .0);
    }
}
