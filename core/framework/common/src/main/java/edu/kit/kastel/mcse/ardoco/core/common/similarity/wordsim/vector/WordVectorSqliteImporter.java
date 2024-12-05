/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

/**
 * Reads a file containing word vector embeddings and inserts them into a sqlite database.
 * <p>
 * The database must contain a table called {@code words} with two columns: {@code word} and {@code vec}. The
 * {@code word} column must be a {@code TEXT} column while the {@code vec} column must be a {@code BLOB}. Vector
 * representations will be inserted as a consecutive sequence of floats. The amount of floats in a sequence depends on
 * the dimension of the vectors.
 * <p>
 * This class can be customized by extending it and overriding the {@link #processWord(String)} and
 * {@link #filterWord(String)} methods. Both methods are called for each word and allow filtering/modifying words before
 * they are inserted into the databse.
 */
public class WordVectorSqliteImporter {

    private static final int DEFAULT_MAX_WORD_LENGTH = 300;
    private static final Logger LOGGER = LoggerFactory.getLogger(WordVectorSqliteImporter.class);

    /**
     * Launches an import process. The first string in the args array must be the path to the file containing the vector
     * representations. The second string in the args array must be the path to the sqlite database file. The third
     * string in the args array must be the dimension of the vectors.
     *
     * @param args the args array
     * @throws IOException  if an io error occurs
     * @throws SQLException if a database related error occurs
     */
    public static void main(String[] args) throws SQLException, IOException {
        ImportResult result = new WordVectorSqliteImporter(args[0], args[1], Integer.parseInt(args[2])).beginImport();

        LOGGER.info("Inserted: {}\n", result.inserted);
        LOGGER.info("Skipped: ({})", result.skippedWords.size());
        result.skippedWords.forEach(word -> LOGGER.info("{}\n", word));
    }

    record ImportResult(long inserted, ImmutableList<String> skippedWords) {
    }

    private final String vectorFile;
    private final String dbFile;
    private final int dimension;

    private final long startLine;
    private final long endLine;
    private final int maxWordLength;
    private final boolean dryRun;

    /**
     * Constructs a new {@link WordVectorSqliteImporter} instance.
     *
     * @param vectorFile the path to the file that contains the vector representations for each word
     * @param dbFile     the path to the sqlite database into which the vector representations will be inserted
     * @param dimension  the dimension of the vectors
     */
    public WordVectorSqliteImporter(String vectorFile, String dbFile, int dimension) {
        this(vectorFile, dbFile, dimension, DEFAULT_MAX_WORD_LENGTH, 0, -1L, false);
    }

    /**
     * Constructs a new {@link WordVectorSqliteImporter} instance. To start the import process, call
     * {@link #beginImport()}.
     *
     * @param vectorFile    the path ot the file that contains the vector representations for each word
     * @param dbFile        the path to the sqlite database into which the vector representations will be inserted
     * @param dimension     the dimension of the vectors
     * @param maxWordLength the maximum length a word is allowed to have to be inserted into the database
     * @param startLine     at which line of the {@code vectorFile} this importer will start inserting
     * @param endLine       at which line of the {@code vectorFile} this importer will stop inserting
     * @param dryRun        whether this importer should actually insert. Use {@code false} to run this importer without
     *                      actually inserting anything
     */
    public WordVectorSqliteImporter(String vectorFile, String dbFile, int dimension, int maxWordLength, long startLine, long endLine, boolean dryRun) {
        this.vectorFile = vectorFile;
        this.dbFile = dbFile;
        this.dimension = dimension;
        this.maxWordLength = maxWordLength;
        this.startLine = startLine;
        this.endLine = endLine;
        this.dryRun = dryRun;

        if (!Files.exists(Path.of(vectorFile))) {
            throw new IllegalStateException("vectorFile does not exist: " + vectorFile);
        }

        if (!Files.exists(Path.of(dbFile))) {
            throw new IllegalStateException("dbFile does not exist: " + dbFile);
        }

        if (this.maxWordLength < 0) {
            throw new IllegalArgumentException("maxWordLength must be a non-negative integer");
        }
    }

    /**
     * Starts the import process.
     *
     * @return the result of the import process
     * @throws SQLException          if an error occurs while interacting with the database
     * @throws IOException           if an error occurs while interacting with the vector file
     * @throws IllegalStateException if a vector with an invalid dimension is found
     */
    public ImportResult beginImport() throws SQLException, IOException, IllegalStateException {
        final List<String> skippedWords = new ArrayList<>();

        long linesRead = 0;
        long inserted = 0;

        try (Connection connection = this.connect();
                PreparedStatement statement = this.prepareSelect(connection);
                var in = Files.newInputStream(Path.of(this.vectorFile), StandardOpenOption.READ);
                var bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            ByteBuffer buffer = ByteBuffer.allocate(this.dimension * 4);

            while (bufferedReader.ready() && linesRead < this.endLine) {
                var line = bufferedReader.readLine();
                linesRead++;

                if (linesRead >= this.startLine) {
                    var parts = line.split(" ", -1);
                    if (parts.length - 1 != this.dimension) {
                        throw new IllegalStateException("importer has read line with invalid vector dimension: \"" + line + "\"");
                    }

                    // Process the word
                    String word = parts[0];
                    // Filter out weird words from dataset
                    if (word.length() > this.maxWordLength || !this.filterWord(word)) {
                        skippedWords.add(word);
                        continue;
                    }
                    word = this.processWord(word);

                    // Process the vector
                    buffer.clear();
                    for (int i = 0; i < parts.length - 1; i++) {
                        float value = Float.parseFloat(parts[i + 1]);
                        buffer.putFloat(value);
                    }

                    this.insertIntoDatabase(statement, buffer, word);
                    inserted++;
                }
            }
        }

        return new ImportResult(inserted, Lists.immutable.withAll(skippedWords));
    }

    private void insertIntoDatabase(PreparedStatement statement, ByteBuffer buffer, String word) throws SQLException {
        if (!this.dryRun) {
            statement.setString(1, word);
            statement.setBytes(2, buffer.array());
            statement.execute();
        } else {
            LOGGER.debug("Would have inserted: {}", word);
        }
    }

    private Connection connect() throws SQLException {
        var cfg = new SQLiteConfig();
        cfg.setLockingMode(SQLiteConfig.LockingMode.EXCLUSIVE);
        cfg.setJournalMode(SQLiteConfig.JournalMode.OFF);
        cfg.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        cfg.setOpenMode(SQLiteOpenMode.NOMUTEX);

        return cfg.createConnection("jdbc:sqlite:" + Path.of(this.dbFile).toAbsolutePath());
    }

    private PreparedStatement prepareSelect(Connection conn) throws SQLException {
        return conn.prepareStatement("INSERT INTO `words` (`word`, `vec`) VALUES (?, ?);");
    }

    /**
     * This method is called for each word that is read from the vector file. The string that is returned will then be
     * used for insertion.
     *
     * @param word the word to process
     * @return the processed version of the word
     */
    protected String processWord(String word) {
        return word;
    }

    /**
     * This method is called for each word that is read from the vector file. It allows filtering which words are
     * inserted into database and which words are skipped.
     *
     * @param word the word
     * @return returns {@code true} if the should should be inserted into the database, {@code false} if not.
     */
    protected boolean filterWord(String word) {
        return true;
    }

}
