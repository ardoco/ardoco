package edu.kit.kastel.mcse.ardoco.core.common.readonlytools;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.agent.tool.Tool;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeFile;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;

/**
 * Utility class to provide read-only tools for interacting with a code model, including listing directories, retrieving metadata about project files,
 * visualizing project trees, and finding files by pattern.
 */
@Deterministic
public class CodeModelReadOnlyTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeModelReadOnlyTools.class);

    private static final int MAX_SEARCH_RESULTS = 50;
    private static final int MAX_DIRECTORY_ENTRIES = 200;
    private static final int MAX_TREE_ENTRIES = 500;
    private static final int MAX_TREE_DEPTH = 8;

    private final DataRepository dataRepository;
    private CodeModelWithCompilationUnits codeModel;

    public CodeModelReadOnlyTools(DataRepository dataRepository) {
        this.dataRepository = Objects.requireNonNull(dataRepository, "Data Repository must not be null.");
        LOGGER.info("CodeModelReadOnlyTools initialized.");
    }

    // -------------------------------------------------------------------------
    // Directory navigation
    // -------------------------------------------------------------------------

    @Tool("""
            Lists files and directories directly below the given project-relative path.
            The path should be relative to the project root. Use "." for the project root.
            """)
    public String listDirectory(String path) {
        LOGGER.debug("Listing code model directory: {}", path);

        String normalizedPath = normalizeDirectoryPath(path);

        if (!directoryExists(normalizedPath)) {
            return "Path does not exist: " + path;
        }

        Set<String> entries = new TreeSet<>();

        for (CodeFile codeFile : codeModel.getCodeFiles()) {
            String filePath = normalizeFilePath(codeFile.getPath());

            if (!isDirectlyBelow(normalizedPath, filePath)) {
                continue;
            }

            String childName = getDirectChildName(normalizedPath, filePath);
            boolean directory = hasRemainingPathBelow(normalizedPath, filePath);

            entries.add(formatPath(joinPath(normalizedPath, childName), directory));
        }

        if (entries.isEmpty()) {
            return "(empty)";
        }

        return entries.stream().limit(MAX_DIRECTORY_ENTRIES).collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Shows a bounded recursive project tree.
     */
    @Tool("""
            Shows a bounded tree of files and directories below a project-relative path.
            maxDepth controls recursion depth. Use a small value such as 2 or 3 for overview.
            """)
    public String tree(String path, int maxDepth) {
        LOGGER.debug("Creating code model tree for {} with depth {}", path, maxDepth);

        if (maxDepth < 0) {
            return "maxDepth must be >= 0";
        }

        int boundedMaxDepth = Math.min(maxDepth, MAX_TREE_DEPTH);
        String normalizedPath = normalizeDirectoryPath(path);

        if (!directoryExists(normalizedPath)) {
            return "Path does not exist: " + path;
        }

        Set<TreeEntry> entries = new TreeSet<>();

        for (CodeFile codeFile : codeModel.getCodeFiles()) {
            String filePath = normalizeFilePath(codeFile.getPath());

            if (!isBelowOrEqual(normalizedPath, filePath)) {
                continue;
            }

            String relativePath = relativize(normalizedPath, filePath);

            if (relativePath.isEmpty()) {
                continue;
            }

            String[] parts = relativePath.split("/");

            for (int i = 0; i < parts.length; i++) {
                int depth = i + 1;

                if (depth > boundedMaxDepth) {
                    break;
                }

                boolean directory = i < parts.length - 1;
                String entryRelativePath = joinParts(parts, i + 1);

                entries.add(new TreeEntry(entryRelativePath, directory));
            }
        }

        if (entries.isEmpty()) {
            return "(empty)";
        }

        StringBuilder result = new StringBuilder();
        int entryCount = 0;

        for (TreeEntry entry : entries) {
            if (entryCount >= MAX_TREE_ENTRIES) {
                result.append(System.lineSeparator()).append("[TREE TRUNCATED]");
                break;
            }

            appendTreeEntry(result, entry.relativePath(), entry.directory());
            entryCount++;
        }

        return result.toString();
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
        LOGGER.debug("Finding code model files with glob: {}", glob);

        CodeModelWithCompilationUnits codeModel = (CodeModelWithCompilationUnits) DataRepositoryHelper.getModelStatesData(dataRepository)
                .getModel(Metamodel.CODE_WITH_COMPILATION_UNITS);
        if (glob == null || glob.isBlank()) {
            return "Glob must not be empty.";
        }

        final PathMatcher matcher;

        try {
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        } catch (IllegalArgumentException e) {
            return "Invalid glob pattern: " + glob;
        }

        String result = codeModel.getCodeFiles()
                .stream()
                .map(CodeFile::getPath)
                .map(this::normalizeFilePath)
                .filter(path -> matcher.matches(Path.of(path)))
                .sorted()
                .limit(MAX_SEARCH_RESULTS)
                .collect(Collectors.joining(System.lineSeparator()));

        if (result.isEmpty()) {
            return "No files found for glob: " + glob;
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // File metadata
    // -------------------------------------------------------------------------

    @Tool("""
            Returns metadata about a project-relative file or directory.
            For files, this includes path, type, extension, file name, containing directory,
            and whether a compilation unit is referenced.
            """)
    public String getFileInfo(String path) {
        LOGGER.debug("Getting code model file info: {}", path);

        String normalizedPath = normalizePath(path);

        if (normalizedPath.equals(".")) {
            return directoryInfo(".");
        }

        return findCodeFile(normalizedPath).map(this::fileInfo).orElseGet(() -> {
            String directoryPath = normalizeDirectoryPath(path);

            if (directoryExists(directoryPath)) {
                return directoryInfo(directoryPath);
            }

            return "Path does not exist: " + path;
        });
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private String fileInfo(CodeFile codeFile) {
        String path = normalizeFilePath(codeFile.getPath());
        String directory = parentOf(path);

        return """
                Path: %s
                Type: file
                Name: %s
                Extension: %s
                Directory: %s
                Has compilation unit: %s
                Compilation unit id: %s
                """.formatted(path, fileNameOf(path), codeFile.getExtension(), directory, codeFile.hasCompilationUnit(),
                codeFile.getCompilationUnitId().orElse(""));
    }

    private String directoryInfo(String directoryPath) {
        long directFiles = codeModel.getCodeFiles()
                .stream()
                .map(CodeFile::getPath)
                .map(this::normalizeFilePath)
                .filter(filePath -> isDirectlyBelow(directoryPath, filePath))
                .filter(filePath -> !hasRemainingPathBelow(directoryPath, filePath))
                .count();

        long directDirectories = codeModel.getCodeFiles()
                .stream()
                .map(CodeFile::getPath)
                .map(this::normalizeFilePath)
                .filter(filePath -> isDirectlyBelow(directoryPath, filePath))
                .filter(filePath -> hasRemainingPathBelow(directoryPath, filePath))
                .map(filePath -> getDirectChildName(directoryPath, filePath))
                .distinct()
                .count();

        long containedFiles = codeModel.getCodeFiles()
                .stream()
                .map(CodeFile::getPath)
                .map(this::normalizeFilePath)
                .filter(filePath -> isBelowOrEqual(directoryPath, filePath))
                .count();

        return """
                Path: %s
                Type: directory
                Direct files: %d
                Direct directories: %d
                Contained files: %d
                """.formatted(directoryPath, directFiles, directDirectories, containedFiles);
    }

    private java.util.Optional<CodeFile> findCodeFile(String normalizedPath) {
        return codeModel.getCodeFiles().stream().filter(codeFile -> normalizeFilePath(codeFile.getPath()).equals(normalizedPath)).findFirst();
    }

    private boolean directoryExists(String normalizedDirectoryPath) {
        if (normalizedDirectoryPath.equals(".")) {
            return true;
        }

        String prefix = normalizedDirectoryPath + "/";

        return codeModel.getCodeFiles().stream().map(CodeFile::getPath).map(this::normalizeFilePath).anyMatch(path -> path.startsWith(prefix));
    }

    private boolean isDirectlyBelow(String directoryPath, String filePath) {
        if (!isBelowOrEqual(directoryPath, filePath)) {
            return false;
        }

        return !relativize(directoryPath, filePath).isEmpty();
    }

    private boolean isBelowOrEqual(String directoryPath, String filePath) {
        if (directoryPath.equals(".")) {
            return true;
        }

        return filePath.equals(directoryPath) || filePath.startsWith(directoryPath + "/");
    }

    private boolean hasRemainingPathBelow(String directoryPath, String filePath) {
        return relativize(directoryPath, filePath).contains("/");
    }

    private String getDirectChildName(String directoryPath, String filePath) {
        String relativePath = relativize(directoryPath, filePath);
        int slashIndex = relativePath.indexOf('/');

        if (slashIndex < 0) {
            return relativePath;
        }

        return relativePath.substring(0, slashIndex);
    }

    private String relativize(String directoryPath, String filePath) {
        if (directoryPath.equals(".")) {
            return filePath;
        }

        if (filePath.equals(directoryPath)) {
            return "";
        }

        return filePath.substring(directoryPath.length() + 1);
    }

    private String formatPath(String path, boolean directory) {
        return directory ? path + "/" : path;
    }

    private void appendTreeEntry(StringBuilder result, String relativePath, boolean directory) {
        int depth = Math.max(0, relativePath.split("/").length - 1);

        result.append("  ".repeat(depth)).append(directory ? "dir: " : "file: ").append(relativePath).append(System.lineSeparator());
    }

    private String normalizeDirectoryPath(String path) {
        String normalized = normalizePath(path);

        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized.isBlank() ? "." : normalized;
    }

    private String normalizeFilePath(String path) {
        String normalized = normalizePath(path);

        if (normalized.equals(".")) {
            return normalized;
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private String normalizePath(String path) {
        String safePath = path == null || path.isBlank() ? "." : path.trim();

        safePath = safePath.replace('\\', '/');

        while (safePath.contains("//")) {
            safePath = safePath.replace("//", "/");
        }

        if (safePath.equals("/") || safePath.equals("./")) {
            return ".";
        }

        if (safePath.startsWith("./")) {
            safePath = safePath.substring(2);
        }

        if (safePath.startsWith("/") || safePath.contains("../") || safePath.equals("..") || safePath.endsWith("/..")) {
            throw new IllegalArgumentException("Path escapes project root: " + path);
        }

        return safePath.isBlank() ? "." : safePath;
    }

    private String joinPath(String parent, String child) {
        if (parent.equals(".")) {
            return child;
        }

        return parent + "/" + child;
    }

    private String joinParts(String[] parts, int length) {
        return List.of(parts).subList(0, length).stream().collect(Collectors.joining("/"));
    }

    private String parentOf(String path) {
        int slashIndex = path.lastIndexOf('/');

        if (slashIndex < 0) {
            return ".";
        }

        return path.substring(0, slashIndex);
    }

    private String fileNameOf(String path) {
        int slashIndex = path.lastIndexOf('/');

        if (slashIndex < 0) {
            return path;
        }

        return path.substring(slashIndex + 1);
    }

    public void initializeCodeModel() {
        if (codeModel != null) {
            return;
        }

        LOGGER.debug("Initializing code model...");

        codeModel = (CodeModelWithCompilationUnits) DataRepositoryHelper.getModelStatesData(dataRepository).getModel(Metamodel.CODE_WITH_COMPILATION_UNITS);

        Objects.requireNonNull(codeModel, "Code model must not be null.");

        LOGGER.debug("Code model initialized.");
    }

    private record TreeEntry(String relativePath, boolean directory) implements Comparable<TreeEntry> {

        @Override
        public int compareTo(TreeEntry other) {
            return Comparator.comparing(TreeEntry::relativePath).thenComparing(TreeEntry::directory).compare(this, other);
        }
    }
}
