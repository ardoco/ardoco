/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.readonlytools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The tool replies are part of the cached LLM requests, so they must not depend on the order in which the file system lists
 * files.
 */
class CodeDirectoryReadOnlyToolsTest {

    private static final String NL = System.lineSeparator();

    @TempDir
    Path projectRoot;

    private CodeDirectoryReadOnlyTools tools;

    @BeforeEach
    void setUp() throws IOException {
        // Created in non-alphabetical order on purpose
        write("src/zeta/Z.java", "class Z { // needle }");
        write("src/alpha/A.java", "class A { // needle }");
        write("README.md", "needle");
        write("src/beta/B.java", "class B {}");
        write("target/Ignored.java", "needle");
        tools = new CodeDirectoryReadOnlyTools(projectRoot.toString());
    }

    @Test
    void treeIsSortedByName() {
        String expected = String.join(NL, "file: README.md", "dir: src", "  dir: src/alpha", "    file: src/alpha/A.java", "  dir: src/beta",
                "    file: src/beta/B.java", "  dir: src/zeta", "    file: src/zeta/Z.java") + NL;
        assertEquals(expected, tools.tree(".", 3));
    }

    @Test
    void treeRespectsMaxDepth() {
        assertEquals(String.join(NL, "file: README.md", "dir: src") + NL, tools.tree(".", 1));
        assertEquals("(empty)", tools.tree(".", 0));
    }

    @Test
    void findFilesIsSortedByRelativePath() {
        assertEquals(String.join(NL, "src/alpha/A.java", "src/beta/B.java", "src/zeta/Z.java"), tools.findFiles("**/*.java"));
    }

    @Test
    void searchIsSortedByRelativePath() {
        String result = tools.search("needle");
        int readme = result.indexOf("README.md");
        int alpha = result.indexOf("src/alpha/A.java");
        int zeta = result.indexOf("src/zeta/Z.java");
        assertTrue(readme >= 0 && readme < alpha && alpha < zeta, result);
        assertTrue(!result.contains("Ignored.java"), result);
    }

    @Test
    void truncatedSearchKeepsTheFirstFilesByPath() throws IOException {
        // Create more matching files than the search returns, in descending order
        List<String> names = IntStream.range(0, 60).mapToObj(i -> "many/f%02d.txt".formatted(i)).toList().reversed();
        for (String name : names) {
            write(name, "match");
        }

        String result = tools.search("many/f");

        assertTrue(result.contains("many/f00.txt"), result);
        assertTrue(result.contains("many/f49.txt"), result);
        assertTrue(!result.contains("many/f50.txt"), result);
        assertTrue(result.endsWith("[SEARCH RESULTS TRUNCATED]"), result);
    }

    private void write(String relativePath, String content) throws IOException {
        Path file = projectRoot.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }
}
