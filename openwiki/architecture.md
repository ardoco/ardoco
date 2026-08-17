---
type: "Reference"
title: "Architecture"
description: "ARDoCo pipeline composite pattern (stage → agent → informant), DataRepository blackboard, intermediate artifacts (Text, SAM, Code Model), execution runners, and configuration system."
openwiki:
  roles: [architecture, domain]
  change_kinds: [lifecycle, public-api]
  source_paths: [core/framework/common/src/main/java, core/pipeline-core/src/main/java]
  symbols: [AbstractPipelineStep, Pipeline, AbstractExecutionStage, PipelineAgent, Informant, DataRepository, Ardoco, ArdocoRunner, AbstractConfigurable]
  invariants: ["Pipeline components communicate only through the DataRepository", "All configurable fields are overridable via @Configurable"]
  validation_commands: ["mvn -pl core clean verify"]
---

# Architecture

This page covers ARDoCo's pipeline architecture, data model, and intermediate artifacts. For the original wiki pages, see [Pipeline.md](../docs/Pipeline.md) and [Intermediate-Artifacts.md](../docs/Intermediate-Artifacts.md).

## Pipeline Composite Pattern

ARDoCo's pipeline follows a **composite pattern** with three levels of hierarchy:

```
Ardoco (extends Pipeline)
  └── AbstractExecutionStage (Level 1: Stage)
        └── PipelineAgent (Level 2: Agent)
              └── Informant (Level 3: actual computation)
```

### Core Classes

| Class | Source | Role |
|-------|--------|------|
| `AbstractPipelineStep` | `core/framework/common/.../pipeline/AbstractPipelineStep.java` | Base class for all pipeline components; provides lifecycle (`before()` → `process()` → `after()`), logging, and `DataRepository` access |
| `Pipeline` | `core/framework/common/.../pipeline/Pipeline.java` | Composite container that executes steps sequentially; `addPipelineStep()`, `process()` |
| `AbstractExecutionStage` | `core/framework/common/.../pipeline/AbstractExecutionStage.java` | Specialized `Pipeline` for stages; manages a list of `PipelineAgent`s and calls `initializeState()` |
| `PipelineAgent` | `core/framework/common/.../pipeline/agent/PipelineAgent.java` | A `Pipeline` + `Agent` that runs a list of `Informant`s; configurable via `enabledInformants` |
| `Informant` | `core/framework/common/.../pipeline/agent/Informant.java` | Abstract `AbstractPipelineStep` that performs actual computation within an agent |
| `Claimant` | `core/framework/common/.../pipeline/agent/Claimant.java` | Marker interface for classes that claim intermediate results (with confidence) |

### Three-Level Hierarchy

1. **Stages** — high-level pipeline phases (e.g., Text Preprocessing, Recommendation Generation, Connection Generation, Inconsistency Detection)
2. **Agents** — each stage contains multiple agents that coordinate informants and aggregate results
3. **Informants** — concrete `PipelineStep` implementations that execute specific heuristics or algorithms

### Data Repository (Blackboard Pattern)

All pipeline steps share a central **DataRepository** (`core/framework/common/.../data/DataRepository.java`) — a key-value store implementing `Serializable`:

- **Universal Access**: All steps can read/write via `addData(id, data)` and `getData(id, class)`
- **Type-Safe Storage**: Data is stored with unique identifiers and type information
- **Incremental Processing**: Each step builds on results from previous steps
- **Modular Communication**: Steps communicate through the repository, not directly

This enables heuristic cooperation (multiple informants contribute complementary analyses), result caching/reuse, and observability of intermediate results.

## Execution Entry Points

| Class | Source | Role |
|-------|--------|------|
| `Ardoco` | `core/pipeline-core/.../execution/Ardoco.java` | Main pipeline entry point; extends `Pipeline`; constructor takes a project name; `runAndSave()` runs pipeline and returns `ArdocoResult` |
| `ArdocoRunner` | `core/pipeline-core/.../execution/runner/ArdocoRunner.java` | Abstract runner wrapping an `Ardoco` instance; subclasses configure pipeline steps via `initializePipelineSteps()` |
| `AnonymousRunner` | `core/pipeline-core/.../execution/runner/AnonymousRunner.java` | Abstract `ArdocoRunner` subclass for tests; implementers override `initializePipelineSteps()` |
| `ArdocoResult` | `core/pipeline-core/.../api/output/ArdocoResult.java` | Record wrapping the `DataRepository` after execution; provides typed accessors for trace links, inconsistencies, etc. |

## Intermediate Artifacts

ARDoCo converts inputs into standardized intermediate representations that enable uniform analysis across different formats, modeling languages, and programming languages.

### Text Representation

Internal model for natural language documentation preserving all NLP annotations from preprocessing:

- **Tokenization**: word boundaries and sentence segmentation
- **Part-of-Speech Tags**: grammatical categories
- **Dependency Parsing**: syntactic relationships between words
- **Named Entity Recognition**: identified entities and types
- **Lemmatization**: base forms of words

**Source**: `core/framework/common/.../api/text/` — interfaces: `Text`, `Word`, `Sentence`, `Phrase`, `POSTag`, `DependencyTag`, `NlpInformant`

### Software Architecture Models (SAM)

Unified representation of architecture models independent of original modeling language (UML, PCM, etc.).

**Source**: `core/framework/common/.../api/models/architecture/`

| Element | Description |
|---------|-------------|
| `ArchitectureItem` | Base class; inherits from `Entity`, provides `name` and `identifier` |
| `ArchitectureComponent` | Components with provided/required interfaces and sub-components |
| `ArchitectureInterface` | Interface contracts containing `ArchitectureMethod` signatures |
| `ArchitectureMethod` | Method declarations within interfaces |

**Hierarchy**: Component → provides/requires → Interface → contains → ArchitectureMethod

### Code Model

Standardized representation of source code based on the [Knowledge Discovery Model (KDM)](https://www.omg.org/spec/KDM/1.3/PDF).

**Source**: `core/framework/common/.../api/models/code/`

| Category | Classes |
|----------|---------|
| **Module** | `CodeCompilationUnit` (source file), `CodePackage` (namespace), `CodeAssembly` (runnable unit) |
| **Datatype** | `Datatype` (sealed base) → `ClassUnit` (class), `InterfaceUnit` (interface); supports `implementedTypes` and `extendedTypes` relationships |
| **ComputationalObject** | `ControlElement` (callable methods) |

All code elements inherit from `CodeItem` (which extends `Entity`).

### Source line ranges and content ownership

`Datatype` and `ControlElement` carry 1-indexed source line ranges to locate each element in its
source file:

- `getStartLine()` / `getEndLine()` return the inclusive start and end line, or `-1` when the
  position is unknown (e.g. elements deserialized from older models, or constructs without a
  concrete source span).
- The fields are `@JsonProperty`-serialized, so line ranges round-trip through the persisted
  `CodeModel`/`CodeModelDto`.

Two related structural changes were folded in with the line-range work:

- **`content` ownership** — `ClassUnit` and `InterfaceUnit` no longer declare their own
  `content` list or `getContent()`/`getContentIds()`/`getAllDataTypes()`. That responsibility moved
  up to `Datatype` (`.../api/models/code/Datatype.java`), which stores content IDs and provides
  `getContent()`, `getContentIds()` (via `@JsonGetter("content")`), and `getAllDataTypes()`. The
  sealed `Datatype` is now the single source of truth; `ClassUnit`/`InterfaceUnit` only add
  type-specific constructors.
- **Model id persistence** — `Model` (`.../api/models/Model.java`) gained a protected
  `Model(String id)` constructor (a `null` id falls back to `IdentifierProvider.createId()`), and
  `CodeModel.CodeModelDto` now carries an `id` field so a deserialized `CodeModel` retains its
  original id rather than being regenerated. `equals`/`hashCode` on `Datatype` and `ControlElement`
  now incorporate `startLine`/`endLine` (and, for `Datatype`, `content`), so identity follows the
  new fields.

When extending the code model, add new fields on the appropriate base (`Datatype` for class-like
members, `ControlElement` for callables), keep them `@JsonProperty`-annotated, and update the
corresponding `equals`/`hashCode` so deserialized instances stay identity-stable.

### Entity Hierarchy

**Source**: `core/framework/common/.../api/entity/`

```
Entity (base)
├── ArchitectureEntity → ModelEntity
├── CodeEntity → ModelEntity
└── TextEntity
```

### Stage State Interfaces

Each pipeline stage has corresponding state interfaces stored in the DataRepository:

| Package | Key Interfaces |
|---------|----------------|
| `...api.stage.textextraction` | `TextState`, `NounMapping`, `PhraseMapping`, `MappingKind` |
| `...api.stage.connectiongenerator` | `ConnectionState`, `ConnectionStates`, `RecommendationModelTraceLink` |
| `...api.stage.connectiongenerator.ner` | `NerConnectionState`, `NamedArchitectureEntity` |
| `...api.stage.recommendationgenerator` | `RecommendationState`, `RecommendedInstance` |
| `...api.stage.inconsistency` | `InconsistencyState`, `Inconsistency`, `ModelInconsistency`, `TextInconsistency` |
| `...api.stage.codetraceability` | `CodeTraceabilityState`, `ArchitectureCodeTraceLink` |

## Configuration System

All pipeline components extend `AbstractConfigurable` (`core/framework/common/.../configuration/AbstractConfigurable.java`). Fields annotated with `@Configurable` can be overridden at runtime via configuration files, enabling fine-grained control over thresholds, filters, and algorithm parameters without code changes.

## Extending the Pipeline

To add a new approach or analysis step:

1. Implement a new `Informant` with your algorithm
2. Add it to an existing `PipelineAgent` or create a new one
3. Read required data from the `DataRepository` using the appropriate state interface
4. Store results back to the repository under a well-known identifier
5. Wire the agent into the pipeline stage or create a new `AbstractExecutionStage`

## Source Map

| Area | Key Path |
|------|----------|
| Pipeline framework | `/core/framework/common/src/main/java/.../core/pipeline/` |
| Data layer | `/core/framework/common/src/main/java/.../core/data/` |
| API interfaces | `/core/framework/common/src/main/java/.../core/api/` |
| Execution runners | `/core/pipeline-core/src/main/java/.../core/execution/` |
| Text provider (JSON) | `/core/framework/text-provider-json/` |
| Configuration | `/core/framework/common/src/main/java/.../core/configuration/` |
| Similarity utilities | `/core/framework/common/src/main/java/.../core/common/similarity/` |
