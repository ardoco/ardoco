# CLI User Guide

The ARDoCo CLI provides command-line access to Traceability Link Recovery tasks.

## Build

```bash
mvn clean package -DskipTests
```

Creates `cli/target/cli-2.0.0-SNAPSHOT-jar-with-dependencies.jar`.

## Usage

```bash
java -jar cli/target/cli-2.0.0-SNAPSHOT-jar-with-dependencies.jar \
    -t <task> -n <name> -o <output> [options]
```

## Tasks

| Task | Description | Required Options |
|------|-------------|------------------|
| `sad-sam` | Documentation to Architecture Model | `-d`, `-m`, `--model-format` |
| `sam-code` | Architecture Model to Code | `-m`, `-c`, `--model-format` |
| `sad-code` | Documentation to Code (transitive) | `-d`, `-m`, `-c`, `--model-format` |

## Options

| Option | Description |
|--------|-------------|
| `-t, --task` | Task name |
| `-n, --name` | Project name |
| `-o, --output` | Output directory |
| `-d, --documentation` | Documentation text file |
| `-m, --model` | Architecture model file |
| `-c, --code` | Source code directory or `.acm` file |
| `--model-format` | Model format: `PCM`, `UML`, `COMPONENT_LISTING`, or `ACM` (required) |

## Examples

```bash
# SAD-SAM
java -jar cli.jar -t sad-sam -n MediaStore \
    -d docs/mediastore.txt -m models/ms.repository --model-format PCM -o ./output

# SAM-Code
java -jar cli.jar -t sam-code -n MyProject \
    -m model.repository --model-format PCM -c src/ -o ./output

# SAD-Code (transitive)
java -jar cli.jar -t sad-code -n TeaStore \
    -d docs/teastore.txt -m models/teastore.repository --model-format PCM -c code.acm -o ./output
```

## Code Input

- **Directory**: Analyzes source code directly (Java, Python, C++)
- **ACM file**: Uses pre-extracted code model
