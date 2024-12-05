/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.vector;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.sqlite.SQLiteConfig;

import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.WordSimUtils;

/**
 * Manages a connection to a sqlite database that contains vector word embeddings using a very specific schema:
 * <p>
 * A table {@code words} has to exist with two columns: {@code word} and {@code vec}. The {@code word} column must be a
 * unique {@code TEXT} column while the {@code vec} column must be a non-nullable {@code BLOB}.
 * <p>
 * Vector blobs must be stored as a consecutive sequence of floats. The amount of floats in a sequence depends on the
 * dimension of the vectors.
 */
public class VectorSqliteDatabase implements WordVectorDataSource, AutoCloseable {

    private static final int BYTES_PER_FLOAT = 4;
    private static final String SELECT_QUERY = "SELECT `vec` FROM `words` WHERE `word` = ?";

    private final Connection connection;
    private final PreparedStatement selectStatement;

    /**
     * Instantiates the {@link VectorSqliteDatabase}. Once instantiated, a connection to the file will be kept open
     * until {@link #close()} is called on this instance.
     *
     * @param sqliteFile the path to the sqlite file
     * @throws SQLException if connecting to the sqlite database fails
     */
    public VectorSqliteDatabase(Path sqliteFile) throws SQLException {
        if (!Files.exists(sqliteFile)) {
            throw new IllegalArgumentException("sqliteFile does not exist: " + sqliteFile);
        }

        SQLiteConfig cfg = WordSimUtils.getSqLiteConfig();

        this.connection = cfg.createConnection("jdbc:sqlite:" + sqliteFile);
        this.selectStatement = this.connection.prepareStatement(SELECT_QUERY);
    }

    /**
     * Attempts to retrieve the vector representation of the given word.
     *
     * @param word the word
     * @return the vector representation, or {@link Optional#empty()} if no representation exists in the database.
     * @throws RetrieveVectorException if a database access error occurs
     */
    @Override
    public Optional<float[]> getWordVector(String word) throws RetrieveVectorException {
        try {
            this.selectStatement.setString(1, word);
        } catch (SQLException e) {
            throw new RetrieveVectorException("unable to pass word to the database: " + word, e);
        }

        ByteBuffer bytes = null;

        try (ResultSet result = this.selectStatement.executeQuery()) {
            if (result.next()) {
                bytes = ByteBuffer.wrap(result.getBytes("vec"));
            }
        } catch (SQLException e) {
            throw new RetrieveVectorException("unable to execute query for word:" + word, e);
        }

        if (bytes == null) {
            return Optional.empty();
        }

        float[] vec = new float[bytes.capacity() / BYTES_PER_FLOAT];

        for (int i = 0; i < vec.length; i++) {
            vec[i] = bytes.getFloat();
        }

        return Optional.of(vec);
    }

    @Override
    public void close() throws Exception {
        this.selectStatement.close();
        this.connection.close();
    }

}
