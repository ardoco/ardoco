---
type: "Reference"
title: "ARDoCo — OpenWiki Quickstart"
description: "Entry point to the ARDoCo OpenWiki: what ARDoCo is, the monorepo module layout, build prerequisites and commands, environment configuration summary, documentation map, and task routing to the domain pages."
tags: [quickstart, monorepo, maven, build, environment, overview]
openwiki:
  roles: [repository, architecture]
  source_paths: [README.md, docs/Home.md, docs/Quickstart.md, pom.xml, sample.env]
  invariants: ["Monorepo of core, tlr, and inconsistency-detection modules under parent io.github.ardoco:parent"]
  validation_commands: ["mvn clean install", "mvn -pl core clean verify", "mvn -pl tlr clean verify", "mvn -pl inconsistency-detection clean verify"]
verified:
  - by: openwiki/0.7.0
    at: 2026-10-05T12:50:45.506Z
sources:
  - id: openwiki-source-d558e38ccd1b08e055e4f3f5
    resource: repo://.github/workflows/docs.yml
  - id: openwiki-source-64c4c51956e735b4f8b4f2ad
    resource: repo://core/.mvn/jvm.config
  - id: openwiki-source-85d7e1bba1ef1c69d3421831
    resource: repo://core/.mvn/maven.config
  - id: openwiki-source-46076f2b227dff6775e63f89
    resource: repo://core/framework/common/src/main/java/edu/kit/kastel/mcse/ardoco/core/common/util/Environment.java
  - id: openwiki-source-b04125c9ac927fa0d9397cf1
    resource: repo://core/framework/pom.xml
  - id: openwiki-source-8461f9d3c6ce6ece9e1ae7ae
    resource: repo://core/pipeline-core/src/main/java/edu/kit/kastel/mcse/ardoco/core/execution/Ardoco.java
  - id: openwiki-source-4953616e2e42dce9b871023a
    resource: repo://core/pom.xml
  - id: openwiki-source-e6a9915b73df8322a4b13396
    resource: repo://core/tests-base/src/main/java/edu/kit/kastel/mcse/ardoco/core/tests/architecture/BasicArchitectureTest.java
  - id: openwiki-source-7d177c0f26ceaecd7fd6d232
    resource: repo://docs/Home.md
  - id: openwiki-source-a07d83c86ab5dff75745e6ce
    resource: repo://docs/Quickstart.md
  - id: openwiki-source-032eecef581f79839de83eb9
    resource: repo://inconsistency-detection/pipeline-id/src/main/java/edu/kit/kastel/mcse/ardoco/id/execution/runner/InconsistencyDetection.java
  - id: openwiki-source-b40e29eb69418d1b82bc9a2b
    resource: repo://inconsistency-detection/pom.xml
  - id: openwiki-source-2355f81d7cf522f8dbdaabd4
    resource: repo://pom.xml
  - id: openwiki-source-23775c3de52f3ab95a13cb8b
    resource: repo://README.md
  - id: openwiki-source-05268939a714aa11095abad9
    resource: repo://sample.env
  - id: openwiki-source-c3324edd02fb573e63e5301b
    resource: repo://tlr/pipeline-tlr/src/main/java/edu/kit/kastel/mcse/ardoco/tlr/execution/Swattr.java
  - id: openwiki-source-ca56ac93c4121e1f63ec2ebf
    resource: repo://tlr/pom.xml
  - id: openwiki-source-b6cef04a363b97fb3f8a9e1a
    resource: repo://tlr/stages-tlr/connection-generator-ner/src/main/java/edu/kit/kastel/mcse/ardoco/tlr/connectiongenerator/ner/informants/NerConnectionInformant.java
  - id: openwiki-source-fb84ef887f21fdedcc742e23
    resource: repo://tlr/stages-tlr/model-provider/src/main/java/edu/kit/kastel/mcse/ardoco/tlr/models/informants/CachedChatLanguageModel.java
  - id: openwiki-source-13a39754b1871635f3d3754d
    resource: repo://tlr/stages-tlr/model-provider/src/main/java/edu/kit/kastel/mcse/ardoco/tlr/models/informants/LargeLanguageModel.java
  - id: openwiki-source-9c80623f9bfadd1c3b52923a
    resource: repo://tlr/stages-tlr/pom.xml
  - id: openwiki-source-5e389d3989eaef1939dbaded
    resource: repo://tlr/stages-tlr/text-preprocessing/src/main/java/edu/kit/kastel/mcse/ardoco/tlr/text/providers/informants/corenlp/config/ConfigManager.java
  - id: openwiki-source-450804860414bc3b2e2751f7
    resource: repo://tlr/stages-tlr/text-preprocessing/src/main/java/edu/kit/kastel/mcse/ardoco/tlr/text/providers/informants/corenlp/textprocessor/HttpCommunicator.java
  - id: openwiki-source-6fc674d44f4df015eca8837e
    resource: repo://tlr/stages-tlr/text-preprocessing/src/main/resources/config.properties
generated: { by: "openwiki/0.7.0", at: "2026-10-05T12:50:45.506Z" }
---

# ARDoCo — OpenWiki Quickstart

Welcome to the OpenWiki documentation for **ARDoCo**. This page is the entry point: it explains what ARDoCo is, how the monorepo is organized, how to build and configure it, and where to go next for each change area. Start here, then follow the [documentation map](#documentation-map) and the [task routing table](#task-routing).

## What is ARDoCo?

ARDoCo (**A**utomating **R**equirements and **Do**cumentation **Co**mprehension) is a research framework whose goal is to connect architecture documentation and models with **Traceability Link Recovery (TLR)** while identifying missing or deviating elements (**inconsistencies**). An element can be any representable item of the model, like a component or a relation. ARDoCo first creates trace links between artifacts (software architecture documentation, software architecture models, code) and then makes use of them — plus other information — to detect inconsistencies.

Recent approaches such as [LiSSA](https://ardoco.de/approaches/lissa/) leverage Large Language Models (LLMs) and Information Retrieval (IR) for more generic TLR across artifact types, including requirements-to-code, documentation-to-code, and architecture-to-code tracing.

The project is actively developed by the [MCSE group](https://mcse.kastel.kit.edu) of [KASTEL](https://kastel.kit.edu) at the [Karlsruhe Institute of Technology (KIT)](https://www.kit.edu). Website: [ardoco.de](https://ardoco.de).

## Repository Layout

This repository is the **monorepo** containing the framework and all approaches: the pipeline and data-handling definitions as well as the individual pipeline steps, inputs, and outputs. The root POM `io.github.ardoco:parent` (version `2.1.0-SNAPSHOT` via the CI-friendly `${revision}` placeholder, MIT license) is the parent for three top-level module trees:

| Module | Path | Aggregator POM | Purpose | Submodules |
|--------|------|----------------|---------|------------|
| **Core** | `/core` | `io.github.ardoco.core:parent-core` | Framework: pipeline engine, data model, API interfaces, common utilities, execution runners | `framework` (with `common`, `text-provider-json`), `pipeline-core`, `tests-base` |
| **TLR** | `/tlr` | `io.github.ardoco.tlr:parent-tlr` | Traceability Link Recovery: SWATTR, ArDoCode, ArCoTL, TransArC, ExArch, ArTEMiS and their pipeline stages | `pipeline-tlr`, `stages-tlr` (with `code-traceability`, `connection-generator`, `connection-generator-ner`, `model-provider`, `recommendation-generator`, `text-extraction`, `text-preprocessing`), `tests-tlr` |
| **Inconsistency Detection** | `/inconsistency-detection` | `io.github.ardoco.id:parent-id` | TEAM/MEAT inconsistency detection between SAD and SAM | `pipeline-id`, `stages-id` (with `inconsistency-detection`), `tests-inconsistency` |

Two Maven profiles control what gets built, and they are defined in the root POM and in each module POM:

- **`complete`** (`activeByDefault=true`) — the default. The root profile lists `core`, `inconsistency-detection`, `tlr`; each tree's profile also includes its test module (`tests-base`, `tests-tlr`, `tests-inconsistency`), so a plain `mvn` invocation builds everything including tests.
- **`deployment`** — restricts each tree to its publishable modules (core: `framework`, `pipeline-core`, `tests-base`; tlr: `pipeline-tlr`, `stages-tlr`; id: `pipeline-id`, `stages-id`), skips test compilation, and GPG-signs artifacts for Maven Central. The root POM additionally carries a `deployment-parent` profile with the same build tweaks for the root POM itself.

## System Requirements

- **Java**: JDK 21 or higher (`java.version=21` drives `maven.compiler.release` in the root [pom.xml](../pom.xml)).
- **Maven**: 3.9 or higher.
- **RAM**: at least 4 GB advised.
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions (optional).

Each module tree ships its own `.mvn/` configuration (`maven.config` enables parallel builds with `-T1C`; `jvm.config` tunes the JVM for the forking compiler and Mockito/ByteBuddy under JDK 21). These apply when invoking Maven inside `core/`, `tlr/`, or `inconsistency-detection/`; from the repository root the default `complete` profile aggregates all three trees.

## Build

```bash
mvn clean install     # Full build with tests, install to local repository
mvn spotless:apply    # Apply code formatting before committing
mvn clean verify      # Full build with tests, no install
```

`mvn clean verify` runs both the Surefire unit/architecture tests and the Failsafe integration tests (the root POM binds `maven-failsafe-plugin` to the `integration-test` phase). The LLM-gated and full-clone integration tests skip automatically when their environment variables are absent — see [Testing & Evaluation](testing.md) for the gating matrix.

Per-module validation (from the repository root):

```bash
mvn -pl core clean verify                  # Framework, pipeline-core, tests-base
mvn -pl tlr clean verify                   # pipeline-tlr, stages-tlr, tests-tlr
mvn -pl inconsistency-detection clean verify   # pipeline-id, stages-id, tests-inconsistency
```

Build gotchas worth knowing before your first change:

- **Pinned `flatten-maven-plugin`**: all POMs resolve `${revision}` through `flatten-maven-plugin`, pinned to `1.7.3` because the unversioned "latest" resolution (1.8.0) has an upstream NPE. Do not remove the pin.
- **Spotless** enforces formatting with the per-tree `formatter.xml`, `spotless.importorder`, and `license-header` files and only touches files changed relative to `origin/main` (`ratchetFrom`); `mvn spotless:check` verifies without rewriting.
- **JSpecify**: `package-info.java` files with `@NullMarked` are generated into your source directories during the build and are git-ignored — do not commit them.

Details, publishing, and CI workflows: [Operations](operations.md).

## Using ARDoCo as a Dependency

ARDoCo is published to Maven Central; embed any subproject by adding it as a dependency:

```xml
<dependencies>
  <dependency>
    <groupId>io.github.ardoco</groupId>
    <artifactId>parent</artifactId> <!-- or any other subproject, e.g. io.github.ardoco.core:pipeline-core -->
    <version>VERSION</version>
  </dependency>
</dependencies>
```

See the root [pom.xml](../pom.xml) for all available module coordinates. Snapshots are published to Sonatype Central Snapshots; releases go through `central-publishing-maven-plugin` (see [Operations](operations.md)).

## Running a Pipeline

End-user entry points are `ArdocoRunner` subclasses that assemble a pipeline and run it against input files:

- The eight TLR runners live in `tlr/pipeline-tlr/.../execution/`: `Swattr`, `Ardocode`, `Arcotl`, `Transarc`, `ExArch`, `Artemis`, `ArtemisInExArch`, and `ArtemisInTransArC`. Each `setUp(...)` validates inputs, wires its stage composition, and pins the metamodels — see [TLR Approaches](tlr-approaches.md).
- The end-to-end inconsistency detection runner is `InconsistencyDetection` (`inconsistency-detection/pipeline-id`), which appends the `InconsistencyChecker` stage (with its `InitialInconsistencyAgent`, TEAM agent, and MEAT agent) to the SWATTR-style pipeline — see [Inconsistency Detection](inconsistency-detection.md).
- Every runner wraps an `Ardoco` pipeline (`core/pipeline-core`); `runAndSave(outputDir)` writes `traceLinks_<project>.txt`, a trace-link CSV, and `inconsistencyDetection_<project>.txt`.

## Environment Configuration

All environment lookups funnel through the `Environment` utility (`core/framework/common`), backed by `dotenv-java`. If a `.env` file exists in the working directory, it is loaded once and its values **take precedence over system environment variables**; missing values resolve to system env or `null`. An ArchUnit rule (`noGetEnv`) forbids every other class from calling `System.getenv`, so this utility is the single environment gateway. Copy [`/sample.env`](../sample.env) as a starting point.

| Variable | Consumed by | Purpose |
|----------|-------------|---------|
| `NLP_PROVIDER_SOURCE` | text-preprocessing `ConfigManager` | `microservice` selects the remote Stanford CoreNLP service; anything else uses local CoreNLP |
| `MICROSERVICE_URL` | text-preprocessing `ConfigManager` | URL of the StanfordCoreNLP provider service (default `http://localhost:8080`) |
| `SCNLP_SERVICE_USER` / `SCNLP_SERVICE_PASSWORD` | `HttpCommunicator` | HTTP basic-auth credentials for the microservice |
| `OPENAI_API_KEY` | `LargeLanguageModel`, `LlmArchitectureProviderInformant`, `NerConnectionInformant` | OpenAI chat models; also required for NER embedding similarity (`text-embedding-3-large`) |
| `OPENAI_ORGANIZATION_ID` | `LargeLanguageModel`, `LlmArchitectureProviderInformant` | Required for OpenAI chat-model creation |
| `OPENAI_MODEL_NAME` | `LargeLanguageModel.OPENAI_GENERIC` | Custom OpenAI chat model name |
| `OLLAMA_HOST` | `LargeLanguageModel` (and test gating) | Ollama host URL for local LLMs |
| `OLLAMA_USER` / `OLLAMA_PASSWORD` | `LargeLanguageModel` | Ollama basic-auth header |
| `OLLAMA_TOKEN` | `LargeLanguageModel` | OpenAI-compatible token against the Ollama host |
| `OLLAMA_MODEL_NAME` | `LargeLanguageModel.OLLAMA_GENERIC` | Custom Ollama model name |
| `LLM_CACHE_DIR` | `CachedChatLanguageModel` | Prompt/response cache directory for LLM runs (default `.cache-llm/`) |
| `SEED` | `LargeLanguageModel` | Random seed for reproducibility (default `422413373`; an unparsable value aborts model creation) |
| `MODEL_NAME_NER` | — | Declared in `sample.env` as a template placeholder; currently no consumer in the codebase |

Test-selection variables (`CI`, `testCodeFull`, `testBaseline`, `mutipleRuns`) and their effects are covered in [Testing & Evaluation](testing.md).

## Documentation Map

| Page | Covers |
|------|--------|
| [Architecture](architecture.md) | Pipeline composite pattern (stage → agent → informant), `DataRepository` blackboard, intermediate artifacts (Text, SAM, Code Model), configuration system, execution runners |
| [TLR Approaches](tlr-approaches.md) | All eight TLR approaches and runners, the seven stage modules, model/code extraction backends, LLM integration, ArCoTL heuristic computation tree |
| [Inconsistency Detection](inconsistency-detection.md) | TEAM and MEAT inconsistency types, the `InconsistencyChecker` stage and its three agents, pre-filter funnel, configuration options, hold-back evaluation harness |
| [Operations](operations.md) | Build system and module matrix, pinned-plugin gotchas, code formatting and JSpecify nullness, CI/CD workflows, publishing, environment configuration, external services |
| [Testing & Evaluation](testing.md) | Shared ArchUnit rule suites, benchmark projects and gold standards, env-gated TLR integration tests, ID hold-back harness, run recipes, the `ExpectedResults` invariant |

## Task Routing

When changing a specific area, start at the listed page and validate with the focused command. All commands run from the repository root.

| Change area / intent | Wiki page | Source entry points | Key symbols / types | Focused tests | Minimal validation |
|----------------------|-----------|---------------------|---------------------|---------------|--------------------|
| Add or modify a pipeline stage/agent/informant | [Architecture](architecture.md) | `core/framework/common/.../pipeline/` (incl. `pipeline/agent/`), `tlr/stages-tlr/` | `AbstractPipelineStep`, `Pipeline`, `AbstractExecutionStage`, `PipelineAgent`, `Informant`, `DataRepository` | `core/tests-base` ArchUnit suites (re-enabled per test tree) | `mvn -pl core clean verify` |
| Add or change a TLR approach / runner | [TLR Approaches](tlr-approaches.md) | `tlr/pipeline-tlr/.../execution/`, `tlr/stages-tlr/` | `Swattr`, `Ardocode`, `Arcotl`, `Transarc`, `ArdocoRunner`, `ModelProviderAgent` | `tlr/tests-tlr` integration tests (`*IT.java`) | `mvn -pl tlr clean verify` |
| Modify ArCoTL heuristics or transitive linking | [TLR Approaches](tlr-approaches.md) | `tlr/stages-tlr/code-traceability/.../informants/arcotl/`, `.../informants/TraceLinkCombiner.java` | `TraceLinkGenerator`, `TraceLinkCombiner`, `ArCoTLInformant` | `tlr/tests-tlr` integration tests (`ArcotlIT`, `TransarcIT`) | `mvn -pl tlr/stages-tlr/code-traceability clean verify` |
| Change LLM/NER-based TLR (ExArch, ArTEMiS) | [TLR Approaches](tlr-approaches.md) | `tlr/stages-tlr/model-provider/`, `tlr/stages-tlr/connection-generator-ner/` | `LargeLanguageModel`, `LlmArchitectureProviderInformant`, `NerConnectionGenerator`, `NerConnectionInformant` | `ArtemisIT`, `ArtemisInTransarcIT` (env-gated) | `mvn -pl tlr clean verify` |
| Add or modify inconsistency detection | [Inconsistency Detection](inconsistency-detection.md) | `inconsistency-detection/stages-id/inconsistency-detection/`, `inconsistency-detection/pipeline-id/` | `InconsistencyChecker`, `InitialInconsistencyAgent`, `TextEntityAbsentFromModelInconsistencyAgent`, `ModelEntityAbsentFromTextInconsistencyAgent`, `InconsistencyDetection` | `tests-inconsistency` (`InconsistencyDetectionEvaluationIT`) | `mvn -pl inconsistency-detection clean verify` |
| Build, dependencies, CI, formatting, env config | [Operations](operations.md) | `pom.xml`, `.github/workflows/`, `{module}/formatter.xml` | parent POM, Spotless, JSpecify, flatten-maven-plugin | `format.yml`, `verify.yml` workflows | `mvn spotless:check && mvn clean verify` |
| Intermediate artifact / data model (Text, SAM, Code) | [Architecture](architecture.md) | `core/framework/common/.../api/` | `Text`, `ArchitectureModel`, `CodeModel`, `ModelStates`, `ArdocoResult` | `core/tests-base` ArchUnit suites | `mvn -pl core/framework/common clean verify` |
| Code model data model (line ranges, content, imports, callees) | [Architecture](architecture.md) | `core/framework/common/.../api/models/code/`, `.../api/models/CodeModel.java` | `Datatype`, `ClassUnit`, `InterfaceUnit`, `ControlElement` (`calleeNames`), `CodeCompilationUnit` / `CodeAssembly` (`importedModuleNames`), `CodeModel.CodeModelDto` | `tlr/stages-tlr/model-provider` mapper/extractor tests | `mvn -pl core/framework/common clean verify && mvn -pl tlr/stages-tlr/model-provider clean verify` |
| Code model extraction / ANTLR mappers / legacy JavaModel | [TLR Approaches](tlr-approaches.md) | `tlr/stages-tlr/model-provider/.../antlr/mapping/{cpp,java,python3}/mappers/`, `.../generators/code/java/JavaModel.java` | `ClassMapper`, `FunctionMapper`, `InterfaceMapper`, `JavaModel` | `JavaControlExtractorTest`, `JavaModelMapperTest`, `Python3ModelMapperTest`, `JavaExtractorTest` | `mvn -pl tlr/stages-tlr/model-provider clean verify` |
| Test or evaluation behavior (gold standards, metric gating, test selection) | [Testing & Evaluation](testing.md) | `core/tests-base/`, `tlr/tests-tlr/`, `inconsistency-detection/tests-inconsistency/` | `ExpectedResults`, `EvaluationProject`, `AbstractArdocoIT`, `InconsistencyDetectionEvaluationIT` | the suites themselves | `mvn clean verify` (env-gated tests skip without credentials) |

## Where the Docs Live

The project maintains a [GitHub Wiki](https://github.com/ardoco/ardoco/wiki) whose source files live in [`/docs`](../docs/Home.md) (sidebar: [`_Sidebar.md`](../docs/_Sidebar.md)). This OpenWiki is an opinionated map and synthesis over those pages with added source-level detail for developers and agents. CI republishes the wiki on every push to `main` that touches `docs/**` or `openwiki/**`; each root-level `/openwiki/*.md` page is rendered as `OpenWiki-<name>.md`, so **wiki pages must stay flat in `/openwiki/`** (subdirectories are never rendered) — see [Operations](operations.md).

## External Repositories

- [LiSSA](https://github.com/ardoco/lissa) — LLM-based generic TLR framework
- [StanfordCoreNLP-Provider-Service](https://github.com/ardoco/StanfordCoreNLP-Provider-Service) — text preprocessing microservice
- [Benchmark](https://github.com/ardoco/benchmark) — evaluation benchmarks and datasets (bundled as a snapshot in `core/tests-base` test resources)
- [Evaluator](https://github.com/ardoco/evaluator) — evaluation code for comparing results (`io.github.ardoco:metrics`)
- [TraceView](https://github.com/ardoco/traceview-v2) — visualization tool for TLR and ID outputs
- [Actions](https://github.com/ardoco/actions) — reusable GitHub Actions (Maven verify and OpenWiki workflows)
