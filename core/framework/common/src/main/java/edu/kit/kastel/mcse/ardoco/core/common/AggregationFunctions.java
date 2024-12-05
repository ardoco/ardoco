/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common;

import java.util.Collection;
import java.util.function.ToDoubleFunction;

/**
 * A set of various aggregation functions for collections of numbers.
 */
public enum AggregationFunctions implements ToDoubleFunction<Collection<? extends Number>> {
    /**
     * Use the median of the scores as final score.
     */
    MEDIAN(s -> {
        var sortedNormalized = s.stream().sorted().toList();
        var sizeHalf = sortedNormalized.size() / 2;

        if (sortedNormalized.size() % 2 == 0) {
            return (sortedNormalized.get(sizeHalf) + sortedNormalized.get(sizeHalf - 1)) / 2;
        }
        return sortedNormalized.get(sizeHalf);
    }),

    HARMONIC(s -> {
        var quotient = s.stream().mapToDouble(d -> 1.0 / d).sum();
        return s.size() / quotient;
    }),

    ROOTMEANSQUARED(s -> {
        var squaredValuesSum = s.stream().mapToDouble(d -> Math.pow(d, 2)).sum();
        return Math.sqrt(squaredValuesSum / s.size());
    }),

    CUBICMEAN(s -> {
        var squaredValuesSum = s.stream().mapToDouble(d -> Math.pow(d, 3)).sum();
        return Math.cbrt(squaredValuesSum / s.size());
    }),

    USE_MOST_RECENT(s -> s.stream().reduce((first, second) -> second).orElse(0.0)),

    /**
     * Use the average of the scores as final score.
     */
    AVERAGE(s -> s.stream().mapToDouble(d -> d).average().getAsDouble()),

    ROLLING_AVERAGE(s -> s.stream().mapToDouble(d -> d).reduce((a, b) -> (a + b) / 2).getAsDouble()),

    /**
     * Use the max value of the scores as final score.
     */
    MAX(s -> s.stream().mapToDouble(d -> d).max().getAsDouble()),

    /**
     * Use the min value of the scores as final score.
     */
    MIN(s -> s.stream().mapToDouble(d -> d).min().getAsDouble()),

    /**
     * Use the sum of the scores as final score.
     */
    SUM(s -> s.stream().mapToDouble(d -> d).sum());

    private final ToDoubleFunction<Collection<Double>> function;

    AggregationFunctions(ToDoubleFunction<Collection<Double>> function) {
        this.function = function;
    }

    @Override
    public double applyAsDouble(Collection<? extends Number> value) {
        var doubleList = value.stream().map(Number::doubleValue).toList();
        return this.function.applyAsDouble(doubleList);
    }
}
