/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.measures.sewordsim;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.sqlite.SQLiteConfig;

import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.WordSimUtils;
import opennlp.tools.stemmer.PorterStemmer;

/**
 * Provides access to the SEWordSim sqlite database. Instances of this class keep an open connection to the sqlite file
 * until {@link #close()} is called.
 */
public class SEWordSimDataSource implements AutoCloseable {

    private static final String EXISTS_QUERY = "SELECT EXISTS(SELECT * FROM `wsim` WHERE `term_1` = ?) AS `ex`;";
    private static final String SELECT_QUERY = "SELECT `similarity` FROM `wsim` WHERE `term_1` = ? AND `term_2` = ?;";
    private static final String SELECT_ALL_QUERY = "SELECT DISTINCT `term_1` FROM `wsim`;";

    private final Connection connection;
    private final PreparedStatement selectStatement;
    private final PorterStemmer stemmer = new PorterStemmer();

    /**
     * Construct a new {@link SEWordSimDataSource}. Once instantiated, a connection to the file will be kept open until
     * {@link #close()} is called on this instance.
     *
     * @param sqliteFile the path to the sqlite database file
     * @throws SQLException if connecting to the sqlite database fails
     */
    public SEWordSimDataSource(Path sqliteFile) throws SQLException {
        if (!Files.exists(sqliteFile)) {
            throw new IllegalArgumentException("sqliteFile does not exist: " + sqliteFile);
        }

        SQLiteConfig cfg = WordSimUtils.getSqLiteConfig();

        this.connection = cfg.createConnection("jdbc:sqlite:" + sqliteFile);
        this.selectStatement = this.connection.prepareStatement(SELECT_QUERY);
    }

    /**
     * Checks whether the stemmed version of the given word is contained in the database.
     *
     * @param word the word to be checked
     * @return {@code true} if the stemmed version of the given word is contained in the database
     * @throws SQLException if a database access error occurs
     */
    public boolean containsWord(String word) throws SQLException {
        Objects.requireNonNull(word);

        if (word.isEmpty()) {
            return false;
        }

        word = this.stemmer.stem(word);

        try (var statement = this.connection.prepareStatement(EXISTS_QUERY)) {
            statement.setString(1, word);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("ex") > 0;
                }
            }
        }

        return false;
    }

    /**
     * Attempts to retrieve the similarity score for the given pair of words.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return the similarity score, ranging from {@code 0.0} to {@code 1.0}, or {@link Optional#empty()} if the
     *         database does not contain the given word pair
     * @throws SQLException if a database access error occurs
     */
    public Optional<Double> getSimilarity(String firstWord, String secondWord) throws SQLException {
        Objects.requireNonNull(firstWord);
        Objects.requireNonNull(secondWord);

        firstWord = this.stemmer.stem(firstWord);
        secondWord = this.stemmer.stem(secondWord);

        this.selectStatement.setString(1, firstWord);
        this.selectStatement.setString(2, secondWord);

        try (var result = this.selectStatement.executeQuery()) {
            if (result.next()) {
                var similarity = result.getDouble("similarity");
                return Optional.of(similarity);
            }
        }

        return Optional.empty();
    }

    /**
     * Gets all words stored in the database.
     *
     * @return a list of all words stored in the database
     * @throws SQLException if a database access error occurs
     */
    public List<String> getAllWords() throws SQLException {
        var words = new ArrayList<String>();

        try (var statement = this.connection.createStatement()) {
            try (var result = statement.executeQuery(SELECT_ALL_QUERY)) {
                while (result.next()) {
                    String word = result.getString("term_1");
                    words.add(word);
                }
            }
        }

        return words;
    }

    /**
     * Closes the connection to the sqlite file.
     *
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void close() throws SQLException {
        this.connection.close();
    }

}
