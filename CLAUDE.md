# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ARDoCo (Automating Requirements and Documentation Comprehension) is a research framework for connecting architecture documentation and models via Traceability Link Recovery (TLR) while identifying missing or deviating elements (inconsistencies). Developed by the MCSE group at KASTEL/KIT.

## Repository Structure

This is a monorepo using git subtree with three main components:

- **core/** - Core framework with API definitions, pipeline, and common utilities
- **tlr/** - Traceability Link Recovery modules
- **inconsistency-detection/** - Inconsistency Detection modules
- **cli/** - Command-line interface (plugin-based architecture)

## Build Commands

```bash
# Build entire project
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run unit tests only
mvn test

# Run integration tests
mvn verify

# Format code with Spotless
mvn spotless:apply

# Check formatting without applying
mvn spotless:check

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName

# Run tests in a specific module
mvn test -pl tlr/tests-tlr
```

## Architecture

### Pipeline Pattern (Composite)

The system uses a three-level composite pipeline pattern:

1. **Stages** (top-level Pipeline via `AbstractExecutionStage`) - Major processing phases (text preprocessing, connection generation)
2. **Agents** (mid-level Pipeline via `PipelineAgent`) - Initiate processing and collect heuristic results
3. **Informants** (`Informant` extends `AbstractPipelineStep`) - Execute concrete processing and heuristics

```
Ardoco (Pipeline)
└─ Stage (AbstractExecutionStage)
	├─ Agent (PipelineAgent)
	│   ├─ Informant
	│   └─ Informant
	└─ Agent (PipelineAgent)
		└─ ...
```

Pipeline steps share data via a **DataRepository** (blackboard pattern). Key execution flow:
- `pipeline.run()` → `before()` → iterate steps calling `step.run()` → `after()`
- Each step: `before()` → `process()` → `after()`

### Key Components

**Core Framework** (`core/framework/common/`):
- `edu.kit.kastel.mcse.ardoco.core.api` - API definitions for models, entities, trace links
- `edu.kit.kastel.mcse.ardoco.core.data` - `DataRepository` and `PipelineStepData` for data handling
- `edu.kit.kastel.mcse.ardoco.core.pipeline` - `AbstractPipelineStep`, `Pipeline`, `AbstractExecutionStage`
- `edu.kit.kastel.mcse.ardoco.core.configuration` - `AbstractConfigurable`, `@Configurable` annotation

**Pipeline Execution** (`core/pipeline-core/`):
- `Ardoco` - Main pipeline orchestrator (extends `Pipeline`)
- `ArdocoRunner` - Abstract runner for different task types
- `ArdocoResult` - Aggregated analysis results facade

**TLR Stages** (`tlr/stages-tlr/`):
- `text-preprocessing` - NLP processing (Stanford CoreNLP integration)
- `text-extraction` - Text analysis for architecture mentions
- `recommendation-generator` - Generates candidate model element recommendations
- `connection-generator` - Creates trace links between text and model
- `model-provider` - Model extraction (PCM, UML, code via ANTLR for Java/Python/C++)
- `code-traceability` - SAM-to-Code trace links (ARCOTL)

### TLR Approaches

- **SAD-SAM**: Documentation to Architecture Model
- **SAM-Code**: Architecture Model to Code (supports Java, Python, C++)
- **SAD-SAM-Code**: Documentation to Code via transitive links

### Inconsistency Detection

- **UME** (Undocumented Model Elements): Model elements without trace links
- **MME** (Missing Model Elements): Documented elements not in model

### Configuration System

Uses reflection-based configuration with annotations:
- `@Configurable` - Mark field as configurable (key format: `ClassName::fieldName`)
- `@ChildClassConfigurable` - Use concrete class name in key (for polymorphism)
- Configuration flows down via `applyConfiguration()` calls

Example keys:
```properties
TextExtraction::enabledAgents=InitialTextAgent,PhraseAgent
PhraseAgent::enabledInformants=NounInformant,CompoundAgentInformant
```

### CLI Plugin Architecture

Uses Service Provider Interface (SPI) pattern:
- `TaskPlugin` interface defines task implementations
- `PluginManager` discovers plugins via `ServiceLoader`
- Plugins registered in `META-INF/services/edu.kit.kastel.mcse.ardoco.core.cli.TaskPlugin`

## Code Style

- **Java 21** required
- Use provided `formatter.xml` via Spotless
- **JSpecify** for null-safety:
- Default is non-null (`@NullMarked` auto-generated during build)
- Use `org.jspecify.annotations.Nullable` for nullable references
- Do not commit `package-info.java` (auto-generated, gitignored)
- Package: `edu.kit.kastel.mcse.ardoco.*`

## Environment Configuration

Copy `sample.env` to `.env` for local configuration:

```env
# LLM Integration
OLLAMA_HOST=http://localhost:11434
OLLAMA_MODEL_NAME=
OPENAI_API_KEY=
OPENAI_MODEL_NAME=

# Stanford CoreNLP Microservice (optional, saves memory)
NLP_PROVIDER_SOURCE=microservice
MICROSERVICE_URL=
SCNLP_SERVICE_USER=
SCNLP_SERVICE_PASSWORD=

# Misc
LLM_CACHE_DIR=
```

## Testing

- **Unit tests**: `*Test.java` files - run with `mvn test`
- **Integration tests**: `*IT.java` files - run with `mvn verify`
- **Architecture tests**: ArchUnit tests for structural constraints
- Test resources in `core/tests-base/src/main/resources/benchmark/`

## Git Subtree Commands

```bash
# Pull from subtree repo
git subtree pull --prefix=core git@github.com:ardoco/core <branch>
git subtree pull --prefix=tlr git@github.com:ardoco/tlr <branch>
git subtree pull --prefix=inconsistency-detection git@github.com:ardoco/inconsistency-detection <branch>

# Push to subtree repo
git subtree push --prefix=core git@github.com:ardoco/core <branch>
git subtree push --prefix=tlr git@github.com:ardoco/tlr <branch>
git subtree push --prefix=inconsistency-detection git@github.com:ardoco/inconsistency-detection <branch>
```

## Key Interfaces

- `Model` / `ArchitectureModel` / `CodeModel` - Model representations (sealed hierarchy)
- `ModelEntity` / `ArchitectureEntity` / `CodeEntity` - Model elements
- `Text` / `Sentence` / `Word` / `Phrase` - NLP text structures
- `TraceLink<E1, E2>` / `TransitiveTraceLink` - Trace link types
- `PipelineStepData` - Base for all data in `DataRepository`
- `AbstractConfigurable` / `@Configurable` - Configuration system
