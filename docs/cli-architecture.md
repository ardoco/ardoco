# CLI Architecture

The CLI uses a plugin-based architecture with Java's Service Provider Interface (SPI).

## Module Structure

```
cli/                    # Main entry point (ArdocoCli, PluginManager)
core/cli-api/           # Plugin interface (TaskPlugin, TaskContext)
tlr/cli-plugins-tlr/    # TLR plugin implementations
```

## Plugin Discovery

Plugins are discovered via `ServiceLoader` with Google AutoService:

1. Annotate plugin with `@AutoService(TaskPlugin.class)`
2. AutoService generates `META-INF/services/edu.kit.kastel.mcse.ardoco.core.cli.TaskPlugin`
3. `PluginManager` loads plugins at runtime via `ServiceLoader.load()`

## TaskPlugin Interface

```java
public interface TaskPlugin {
    String getTaskName();
    String getDescription();
    List<Option> getRequiredOptions();
    List<Option> getOptionalOptions();
    boolean validateParameters(CommandLine cmd);
    void execute(TaskContext context);
}
```

## TaskContext

Immutable record passed to `execute()`:

```java
public record TaskContext(
    CommandLine commandLine,
    String projectName,
    File outputDirectory,
    ImmutableSortedMap<String, String> additionalConfigs
) {}
```

## Execution Flow

```
ArdocoCli.main()
  → PluginManager.discoverPlugins()
  → parseCommandLine()
  → plugin.validateParameters()
  → plugin.execute(TaskContext)
```

## Adding a Plugin

```java
@AutoService(TaskPlugin.class)
public class MyTaskPlugin implements TaskPlugin {
    @Override
    public String getTaskName() { return "my-task"; }

    @Override
    public List<Option> getRequiredOptions() {
        return List.of(Option.builder("i").longOpt("input").hasArg().required().build());
    }

    @Override
    public boolean validateParameters(CommandLine cmd) {
        return cmd.hasOption("input");
    }

    @Override
    public void execute(TaskContext context) {
        // Task implementation
    }
}
```

Add dependency on `cli-api` (provided scope) and include plugin module in CLI's runtime dependencies.

## Current Plugins

| Plugin | Task | Runner | Required Options |
|--------|------|--------|------------------|
| `SadSamTaskPlugin` | `sad-sam` | Swattr | `-d`, `-m`, `--model-format` |
| `SamCodeTaskPlugin` | `sam-code` | Arcotl | `-m`, `-c`, `--model-format` |
| `SadCodeTaskPlugin` | `sad-code` | Transarc | `-d`, `-m`, `-c`, `--model-format` |
