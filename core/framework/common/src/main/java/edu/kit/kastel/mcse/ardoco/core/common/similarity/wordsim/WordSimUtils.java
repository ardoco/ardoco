/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.measures.equality.EqualityMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.strategy.AverageStrategy;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.strategy.ComparisonStrategy;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.strategy.SimilarityStrategy;

/**
 * A static class that provides various utility methods to calculate similarity between different kinds of objects. This class statically keeps a reference to a
 * fallback {@link ComparisonStrategy} and a fallback list of word similarity measures. These fallbacks can be changed with the {@link #setMeasures(Collection)}
 * and {@link #setStrategy(ComparisonStrategy)} methods. Any calls to methods that provide their own measures or strategies will not utilize these fallbacks.
 * Any calls that do not provide their own measures or strategies will utilize them. As of right now, no protections against simultaneous write access from
 * multiple threads exist. Therefore, this class is not threadsafe.
 */
public class WordSimUtils {

    private MutableList<WordSimMeasure> measures = Lists.mutable.withAll(WordSimLoader.loadUsingProperties());
    private ComparisonStrategy strategy = ComparisonStrategy.AT_LEAST_ONE;
    private SimilarityStrategy similarityStrategy = new AverageStrategy();

    /**
     * Sets which measures should be used for similarity comparison. The specified collection of measures will be used for all subsequent comparisons.
     *
     * @param measures the measures to use
     */
    public void setMeasures(Collection<WordSimMeasure> measures) {
        this.measures = Lists.mutable.withAll(measures);
    }

    /**
     * Adds the specified measure to the measures, which should be used for similarity comparison.
     *
     * @param measure the measure to add
     * @return Whether the measure was added successfully
     */
    public boolean addMeasure(WordSimMeasure measure) {
        return this.measures.add(measure);
    }

    /**
     * Sets the default comparison strategy. The specified strategy will be used for all subsequent comparisons that themselves do not specify a strategy.
     *
     * @param strategy the new default strategy
     */
    public void setStrategy(ComparisonStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Sets the default similarity strategy. The specified strategy will be used for all subsequent comparisons that themselves do not specify a strategy.
     *
     * @param strategy the new default strategy
     */
    public void setStrategy(SimilarityStrategy strategy) {
        this.similarityStrategy = strategy;
    }

    /**
     * Evaluates whether the words from the given {@link ComparisonContext} are similar using the specified comparison strategy.
     *
     * @param ctx      the context
     * @param strategy the strategy
     * @return Returns {@code true} if the given strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(ComparisonContext ctx, ComparisonStrategy strategy) {
        Objects.requireNonNull(ctx);
        Objects.requireNonNull(strategy);

        // Currently, we need the split test as it improves results by a lot. In the future, we should try to avoid its requirement
        if (!this.splitLengthTest(ctx)) {
            return false;
        }

        return strategy.areWordsSimilar(ctx, this.measures.toList());
    }

    private boolean splitLengthTest(ComparisonContext ctx) {
        var first = ctx.firstTerm().toLowerCase();
        var second = ctx.secondTerm().toLowerCase();
        return (first.split(" ").length == second.split(" ").length);
    }

    /**
     * Evaluates whether the words from the given {@link ComparisonContext} are similar using the default comparison strategy. The default strategy can be
     * changed with the {@link #setStrategy(ComparisonStrategy)} method.
     *
     * @param ctx the context
     * @return Returns {@code true} if the default strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(ComparisonContext ctx) {
        Objects.requireNonNull(ctx);
        return this.areWordsSimilar(ctx, this.strategy);
    }

    /**
     * Evaluates whether the given words are similar using the default comparison strategy. The default strategy can be changed with the
     * {@link #setStrategy(ComparisonStrategy)} method.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return Returns {@code true} if the default strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(String firstWord, String secondWord) {
        return this.areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), this.strategy);
    }

    /**
     * Evaluates whether the given words are similar using the specified comparison strategy.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @param strategy   the strategy to use
     * @return Returns {@code true} if the given strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(String firstWord, String secondWord, ComparisonStrategy strategy) {
        return this.areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), strategy);
    }

    /**
     * Evaluates whether the given words are similar using the default comparison strategy. The default strategy can be changed with the
     * {@link #setStrategy(ComparisonStrategy)} method.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return Returns {@code true} if the default strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(Word firstWord, Word secondWord) {
        return this.areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), this.strategy);
    }

    /**
     * Evaluates whether the given words are similar using the specified comparison strategy.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @param strategy   the strategy to use
     * @return Returns {@code true} if the given strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(Word firstWord, Word secondWord, ComparisonStrategy strategy) {
        return this.areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), strategy);
    }

    /**
     * Evaluates whether the given words are similar using the default comparison strategy. The default strategy can be changed with the
     * {@link #setStrategy(ComparisonStrategy)} method.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return Returns {@code true} if the default strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(String firstWord, Word secondWord) {
        return this.areWordsSimilar(new ComparisonContext(firstWord, secondWord.getText(), null, secondWord, false), this.strategy);
    }

    /**
     * Evaluates whether the given words are similar using the specified comparison strategy.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @param strategy   the strategy to use
     * @return Returns {@code true} if the given strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(String firstWord, Word secondWord, ComparisonStrategy strategy) {
        return this.areWordsSimilar(new ComparisonContext(firstWord, secondWord.getText(), null, secondWord, false), strategy);
    }

    /**
     * Evaluates the similarity of the given words using the specified similarity strategy.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @param strategy   the strategy to use
     * @param ignoreCase whether to ignore the case during comparison
     * @return Returns similarity in range [0,1]
     */
    public double getSimilarity(String firstWord, String secondWord, SimilarityStrategy strategy, boolean ignoreCase) {
        var allMeasuresExceptDefault = this.measures.stream().filter(m -> !(m instanceof EqualityMeasure)).collect(Collectors.toCollection(ArrayList::new));
        if (allMeasuresExceptDefault.isEmpty()) {
            allMeasuresExceptDefault.add(new EqualityMeasure());
        }

        return strategy.getSimilarity(new ComparisonContext(ignoreCase ? firstWord.toLowerCase() : firstWord, ignoreCase ?
                secondWord.toLowerCase() :
                secondWord, null, null, false), allMeasuresExceptDefault);
    }

    /**
     * Evaluates the similarity of the given words.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return Returns similarity in range [0,1]
     */
    public double getSimilarity(String firstWord, String secondWord) {
        return this.getSimilarity(firstWord, secondWord, false);
    }

    /**
     * Evaluates the similarity of the given words.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @param ignoreCase whether to ignore the case during comparison
     * @return Returns similarity in range [0,1]
     */
    public double getSimilarity(String firstWord, String secondWord, boolean ignoreCase) {
        return this.getSimilarity(firstWord, secondWord, this.similarityStrategy, ignoreCase);
    }

    public static SQLiteConfig getSqLiteConfig() {
        var cfg = new SQLiteConfig();
        cfg.setReadOnly(true);
        cfg.setLockingMode(SQLiteConfig.LockingMode.EXCLUSIVE);
        cfg.setJournalMode(SQLiteConfig.JournalMode.OFF);
        cfg.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        cfg.setOpenMode(SQLiteOpenMode.NOMUTEX);
        return cfg;
    }
}
