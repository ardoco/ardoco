/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.readonlytools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.agent.tool.Tool;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * A utility class for read-only tools that operate on code directories within a defined project root. This class provides methods for navigating, inspecting,
 * and searching files and directories in a controlled manner.
 */
@Deterministic
public class CodeDirectoryReadOnlyTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeDirectoryReadOnlyTools.class);

    // General limits:
    private static final int MAX_SEARCH_RESULTS = 50;
    private static final int MAX_FILE_CHARS = 20_000;
    private static final int MAX_DIRECTORY_ENTRIES = 200;
    private static final int MAX_TREE_ENTRIES = 500;
    private static final int MAX_TREE_DEPTH = 8;

    // Search limits:
    private static final long MAX_SEARCH_FILE_SIZE = 1_000_000;
    private static final int MAX_SEARCH_CONTEXT_LINES = 1;
    private static final int MAX_SEARCH_SNIPPET_CHARS = 500;

    // Files/directories that usually contain generated dependencies, IDE metadata, or other things an AI agent normally does not need to inspect:
    private static final Set<String> IGNORED_DIRECTORIES = Set.of(".git", ".idea", ".gradle", ".mvn", "target", "build", "node_modules", "out", "bin");
    public static final String PATH_DOES_NOT_EXIST = "Path does not exist: ";

    private final Path projectRoot;
    private final Path realProjectRoot;

    public CodeDirectoryReadOnlyTools(String projectRoot) {
        if (projectRoot == null || projectRoot.isBlank()) {
            throw new IllegalArgumentException("Project root must not be null or blank.");
        }

        this.projectRoot = Path.of(projectRoot).toAbsolutePath().normalize();

        if (!Files.isDirectory(this.projectRoot)) {
            throw new IllegalArgumentException("Project root is not a directory: " + this.projectRoot);
        }

        try {
            //Real path is used for symlink-aware security checks.
            this.realProjectRoot = this.projectRoot.toRealPath();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not resolve project root: " + this.projectRoot, e);
        }

        LOGGER.info("ProjectReadOnlyTools initialized for {}", this.projectRoot);
    }

    // -------------------------------------------------------------------------
    // Directory navigation
    // -------------------------------------------------------------------------

    @Tool("""
            Lists files and directories directly below the given project-relative path.
            The path should be relative to the project root. Use "." for the project root.
            """)
    public String listDirectory(String path) {
        LOGGER.debug("Listing directory: {}", path);

        Path directory = resolveInsideProject(path);

        if (!Files.exists(directory)) {
            return PATH_DOES_NOT_EXIST + path;
        }

        if (!Files.isDirectory(directory)) {
            return "Path is not a directory: " + path;
        }

        try (Stream<Path> stream = Files.list(directory)) {
            return stream.sorted(Comparator.comparing(Path::toString))
                    .limit(MAX_DIRECTORY_ENTRIES)
                    .map(this::formatDirectoryEntry)
                    .collect(Collectors.joining(System.lineSeparator()));

        } catch (IOException e) {
            throw new UncheckedIOException("Could not list directory: " + path, e);
        }
    }

    /**
     * Shows a bounded recursive project tree.
     */
    @Tool("""
            Shows a bounded tree of files and directories below a project-relative path.
            maxDepth controls recursion depth. Use a small value such as 2 or 3 for overview.
            """)
    public String tree(String path, int maxDepth) {
        LOGGER.debug("Creating tree for {} with depth {}", path, maxDepth);

        if (maxDepth < 0) {
            return "maxDepth must be >= 0";
        }

        maxDepth = Math.min(maxDepth, MAX_TREE_DEPTH);

        Path root = resolveInsideProject(path);

        if (!Files.exists(root)) {
            return PATH_DOES_NOT_EXIST + path;
        }

        if (!Files.isDirectory(root)) {
            return "Path is not a directory: " + path;
        }

        TreeBuilder builder = new TreeBuilder(root, maxDepth);

        try {
            builder.addChildren(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create tree for: " + path, e);
        }

        return builder.render();
    }

    // -------------------------------------------------------------------------
    // File reading
    // -------------------------------------------------------------------------

    @Tool("""
            Reads a UTF-8 text file from a project-relative path.
            Large files are truncated to a safe maximum size.
            """)
    public String readFile(String path) {
        LOGGER.debug("Reading file: {}", path);

        Path file = resolveInsideProject(path);

        if (!Files.exists(file)) {
            return "File does not exist: " + path;
        }

        if (!Files.isRegularFile(file)) {
            return "Path is not a regular file: " + path;
        }

        if (isProbablyBinary(file)) {
            return "File appears to be binary or too large to read: " + path;
        }

        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);

            if (content.length() <= MAX_FILE_CHARS) {
                return content;
            }

            return content.substring(0, MAX_FILE_CHARS) + System.lineSeparator() + "[TRUNCATED: file exceeds " + MAX_FILE_CHARS + " characters]";

        } catch (IOException e) {
            throw new UncheckedIOException("Could not read file: " + path, e);
        }
    }

    @Tool("""
            Reads a specific range of lines from a UTF-8 text file.
            Line numbers are 1-based and inclusive.
            """)
    public String readFileLines(String path, int startLine, int endLine) {

        LOGGER.debug("Reading lines {}-{} from {}", startLine, endLine, path);

        if (startLine < 1) {
            return "startLine must be >= 1";
        }

        if (endLine < startLine) {
            return "endLine must be >= startLine";
        }

        /*
         * Prevent the agent from accidentally requesting millions of lines.
         */
        int requestedLines = endLine - startLine + 1;

        if (requestedLines > 1_000) {
            return "Line range is too large. Maximum range is 1000 lines.";
        }

        Path file = resolveInsideProject(path);

        if (!Files.exists(file)) {
            return "File does not exist: " + path;
        }

        if (!Files.isRegularFile(file)) {
            return "Path is not a regular file: " + path;
        }

        if (isProbablyBinary(file)) {
            return "File appears to be binary or too large to read: " + path;
        }

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {

            StringBuilder result = new StringBuilder();

            String line;
            int currentLine = 0;

            while (currentLine < endLine && (line = reader.readLine()) != null) {
                currentLine++;

                if (currentLine >= startLine) {
                    result.append(currentLine).append(": ").append(line).append(System.lineSeparator());
                }
            }

            if (result.isEmpty()) {
                return "No lines found in requested range.";
            }

            return result.toString();

        } catch (IOException e) {
            throw new UncheckedIOException("Could not read lines from file: " + path, e);
        }
    }

    // -------------------------------------------------------------------------
    // File discovery
    // -------------------------------------------------------------------------

    @Tool("""
            Finds project files matching a glob pattern.
            Examples:
            **/*.java
            **/pom.xml
            src/main/**/*.java
            **/*.json
            """)
    public String findFiles(String glob) {
        LOGGER.debug("Finding files with glob: {}", glob);

        if (glob == null || glob.isBlank()) {
            return "Glob must not be empty.";
        }

        final PathMatcher matcher;

        try {
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        } catch (IllegalArgumentException e) {
            return "Invalid glob pattern: " + glob;
        }

        try (Stream<Path> stream = Files.walk(projectRoot)) {
            var result = stream.filter(Files::isRegularFile).filter(path -> !isInsideIgnoredDirectory(path)).filter(path -> {
                Path relative = projectRoot.relativize(path);
                return matcher.matches(relative);
            }).map(this::toRelativeString).sorted().limit(MAX_SEARCH_RESULTS).collect(Collectors.joining(System.lineSeparator()));
            return result.isEmpty() ? "No files found matching glob pattern: " + glob : result;

        } catch (IOException e) {
            throw new UncheckedIOException("Could not search for files: " + glob, e);
        }
    }

    // -------------------------------------------------------------------------
    // Content search
    // -------------------------------------------------------------------------

    @Tool("""
            Searches project file paths and UTF-8 text contents for a query.
            Returns matching project-relative paths, line numbers, and snippets.
            Searches are case-insensitive.
            """)
    public String search(String query) {
        LOGGER.debug("Searching project for: {}", query);

        if (query == null || query.isBlank()) {
            return "Search query must not be empty.";
        }

        String normalizedQuery = query.toLowerCase(Locale.ROOT);

        SearchVisitor visitor = new SearchVisitor(normalizedQuery);

        try {
            Files.walkFileTree(projectRoot, visitor);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not search project.", e);
        }

        List<String> results = visitor.search();

        if (results.isEmpty()) {
            return "No matches found for: " + query;
        }

        String result = String.join(System.lineSeparator() + System.lineSeparator(), results);

        if (results.size() >= MAX_SEARCH_RESULTS) {
            result += System.lineSeparator() + System.lineSeparator() + "[SEARCH RESULTS TRUNCATED]";
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // File metadata
    // -------------------------------------------------------------------------

    @Tool("""
            Returns metadata about a project-relative file or directory,
            including type and size.
            """)
    public String getFileInfo(String path) {
        LOGGER.debug("Getting file info: {}", path);

        Path file = resolveInsideProject(path);

        if (!Files.exists(file)) {
            return PATH_DOES_NOT_EXIST + path;
        }

        try {
            String type;

            if (Files.isDirectory(file)) {
                type = "directory";
            } else if (Files.isRegularFile(file)) {
                type = "file";
            } else {
                type = "other";
            }

            long size = Files.isRegularFile(file) ? Files.size(file) : 0;

            // No modification time: it differs between checkouts and would make the (cached) tool replies differ as well.
            return """
                    Path: %s
                    Type: %s
                    Size: %d bytes
                    """.formatted(toRelativeString(file), type, size);

        } catch (IOException e) {
            throw new UncheckedIOException("Could not get file information: " + path, e);
        }
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private String formatDirectoryEntry(Path path) {
        String suffix = Files.isDirectory(path) ? "/" : "";

        return toRelativeString(path) + suffix;
    }

    private boolean isProbablyBinary(Path path) {
        try {
            if (Files.size(path) > MAX_SEARCH_FILE_SIZE) {
                return true;
            }

            /*
             * Inspect only the first small chunk.
             */
            byte[] bytes = Files.readAllBytes(path);

            int length = Math.min(bytes.length, 4_096);

            for (int i = 0; i < length; i++) {
                if (bytes[i] == 0) {
                    return true;
                }
            }

            return false;

        } catch (IOException e) {
            return true;
        }
    }

    private boolean shouldIgnoreDirectory(Path directory) {
        Path fileName = directory.getFileName();

        if (fileName == null) {
            return false;
        }

        return IGNORED_DIRECTORIES.contains(fileName.toString());
    }

    private boolean isInsideIgnoredDirectory(Path path) {
        Path relative = projectRoot.relativize(path);

        for (Path part : relative) {
            if (IGNORED_DIRECTORIES.contains(part.toString())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Resolves a project-relative path and prevents both:
     *
     * 1. lexical traversal such as ../../foo 2. symlink traversal outside the project root
     */
    private Path resolveInsideProject(String path) {

        String safePath = path == null || path.isBlank() ? "." : path;

        Path resolved = projectRoot.resolve(safePath).normalize();

        /*
         * First check the lexical path.
         */
        if (!resolved.startsWith(projectRoot)) {
            throw new IllegalArgumentException("Path escapes project root: " + path);
        }

        /*
         * If the path exists, check the real filesystem path as well.
         * This prevents a symlink inside the project from pointing outside.
         */
        if (Files.exists(resolved)) {
            try {
                Path realPath = resolved.toRealPath();

                if (!realPath.startsWith(realProjectRoot)) {
                    throw new IllegalArgumentException("Path escapes project root through a symlink: " + path);
                }

            } catch (IOException e) {
                throw new UncheckedIOException("Could not resolve path: " + path, e);
            }
        }

        return resolved;
    }

    private String toRelativeString(Path path) {
        return projectRoot.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }

    /**
     * Collects a bounded tree listing. Entries of a directory are visited in the order of their names, so the listing does
     * not depend on the file system.
     */
    private final class TreeBuilder {
        private final Path root;
        private final int maxDepth;
        private final StringBuilder result = new StringBuilder();
        private int entryCount = 0;

        private TreeBuilder(Path root, int maxDepth) {
            this.root = root;
            this.maxDepth = maxDepth;
        }

        private void addChildren(Path directory) throws IOException {
            // The children of this directory would be deeper than maxDepth.
            if (depthOf(directory) >= maxDepth) {
                return;
            }

            List<Path> children;
            try (Stream<Path> stream = Files.list(directory)) {
                children = stream.sorted(Comparator.comparing(child -> child.getFileName().toString())).toList();
            }

            for (Path child : children) {
                if (isLimitReached()) {
                    return;
                }

                if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                    if (!shouldIgnoreDirectory(child)) {
                        addEntry(child, true);
                        addChildren(child);
                    }
                } else {
                    addEntry(child, false);
                }
            }
        }

        private int depthOf(Path path) {
            return path.equals(root) ? 0 : root.relativize(path).getNameCount();
        }

        private boolean isLimitReached() {
            return entryCount >= MAX_TREE_ENTRIES;
        }

        private void addEntry(Path path, boolean directory) {
            appendTreeEntry(root.relativize(path), directory);
            entryCount++;
        }

        private String render() {
            if (isLimitReached()) {
                result.append(System.lineSeparator()).append("[TREE TRUNCATED]");
            }

            if (result.isEmpty()) {
                return "(empty)";
            }

            return result.toString();
        }

        private void appendTreeEntry(Path relativePath, boolean directory) {

            int depth = Math.max(0, relativePath.getNameCount() - 1);

            result.append("  ".repeat(depth))
                    .append(directory ? "dir: " : "file: ")
                    .append(relativePath.toString().replace('\\', '/'))
                    .append(System.lineSeparator());
        }
    }

    /**
     * Collects the files to search while walking the project tree and searches them afterwards.
     */
    private final class SearchVisitor extends SimpleFileVisitor<Path> {
        private final String normalizedQuery;
        private final List<Path> files = new ArrayList<>();
        private final List<String> results = new ArrayList<>();

        private SearchVisitor(String normalizedQuery) {
            this.normalizedQuery = normalizedQuery;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) {
            if (!directory.equals(projectRoot) && shouldIgnoreDirectory(directory)) {
                return FileVisitResult.SKIP_SUBTREE;
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (Files.isRegularFile(file)) {
                files.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        /**
         * Searches the collected files in the order of their project-relative paths, so the (possibly truncated) results do
         * not depend on the order in which the file system lists them.
         */
        private List<String> search() {
            List<Path> sortedFiles = files.stream().sorted(Comparator.comparing(CodeDirectoryReadOnlyTools.this::toRelativeString)).toList();

            for (Path file : sortedFiles) {
                if (isLimitReached()) {
                    break;
                }

                if (matchesPath(file)) {
                    results.add(toRelativeString(file) + ": [path match]");
                } else if (!isProbablyBinaryOrTooLarge(file)) {
                    searchFileContent(file);
                }
            }

            return results;
        }

        private boolean isLimitReached() {
            return results.size() >= MAX_SEARCH_RESULTS;
        }

        private boolean matchesPath(Path path) {

            return toRelativeString(path).toLowerCase(Locale.ROOT).contains(normalizedQuery);
        }

        private void searchFileContent(Path file) {

            String relativePath = toRelativeString(file);

            try {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

                for (int i = 0; i < lines.size(); i++) {

                    if (results.size() >= MAX_SEARCH_RESULTS) {
                        return;
                    }

                    String line = lines.get(i);

                    if (!line.toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                        continue;
                    }

                    int start = Math.max(0, i - MAX_SEARCH_CONTEXT_LINES);

                    int end = Math.min(lines.size() - 1, i + MAX_SEARCH_CONTEXT_LINES);

                    StringBuilder snippet = new StringBuilder();

                    for (int lineIndex = start; lineIndex <= end; lineIndex++) {

                        String contextLine = lines.get(lineIndex);

                        if (contextLine.length() > MAX_SEARCH_SNIPPET_CHARS) {

                            contextLine = contextLine.substring(0, MAX_SEARCH_SNIPPET_CHARS) + "…";
                        }

                        if (lineIndex == i) {
                            snippet.append(">> ");
                        } else {
                            snippet.append("   ");
                        }

                        snippet.append(lineIndex + 1).append(": ").append(contextLine).append(System.lineSeparator());
                    }

                    results.add(relativePath + System.lineSeparator() + snippet);
                }

            } catch (IOException | RuntimeException e) {
                LOGGER.debug("Could not search file {}", file, e);
            }
        }

        private boolean isProbablyBinaryOrTooLarge(Path path) {
            try {
                return Files.size(path) > MAX_SEARCH_FILE_SIZE || isProbablyBinary(path);
            } catch (IOException e) {
                return true;
            }
        }
    }
}
