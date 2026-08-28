---
type: "Reference"
title: "ARDoCo — OpenWiki Quickstart"
description: "Entry point to the ARDoCo OpenWiki: repository overview, build prerequisites, documentation map, and links to architecture, TLR, inconsistency detection, and operations pages."
openwiki:
  roles: [repository, architecture]
  source_paths: [pom.xml, README.md]
  invariants: ["Monorepo of core, tlr, and inconsistency-detection modules under parent io.github.ardoco:parent"]
  validation_commands: ["mvn clean install"]
---

# ARDoCo — OpenWiki Quickstart

Welcome to the OpenWiki documentation for **ARDoCo** (Automating Requirements and Documentation Comprehension). This page is your entry point. Start here, then follow the links below to dive deeper.

## What is ARDoCo?

ARDoCo is a research framework that connects software architecture documentation and models using **Traceability Link Recovery (TLR)**, then leverages those links to detect **inconsistencies** — missing or deviating elements between artifacts. An element can be any representable item of the model, such as a component or a relation.

The project is developed by the [MCSE group](https://mcse.kastel.kit.edu) at KASTEL, Karlsruhe Institute of Technology (KIT). Recent approaches like [LiSSA](https://ardoco.de/approaches/lissa/) use LLMs and Information Retrieval for generic TLR.

## Repository Overview

This is a **monorepo** containing three major modules. The standalone `ardoco/core`, `ardoco/tlr`, and `ardoco/inconsistency-detection` repositories are archived; the project now lives only in this monorepo:

| Module | Path | Maven Coordinates | Purpose |
|--------|------|-------------------|---------|
| **Core** | `/core` | `io.github.ardoco.core` | Framework: pipeline engine, data model, API interfaces, text/code/architecture representations, execution runners |
| **TLR** | `/tlr` | `io.github.ardoco.tlr` | Traceability Link Recovery: all TLR approaches (SWATTR, ArDoCode, ArCoTL, TransArC, ExArch, ArTEMiS) and their pipeline stages |
| **Inconsistency Detection** | `/inconsistency-detection` | `io.github.ardoco.id` | Inconsistency detection between SAD and SAM (TEAM and MEAT inconsistencies) |

The root `pom.xml` (`io.github.ardoco:parent`) is the parent POM that defines shared dependency versions, plugin configuration, and build settings for all three modules.

## System Requirements

- **Java**: JDK 21 or higher
- **Maven**: 3.9 or higher
- **RAM**: At least 4 GB recommended

## Build

```bash
mvn clean install
```

This builds all three modules and their submodules. To run formatting checks and verification:

```bash
mvn spotless:apply   # Apply code formatting
mvn clean verify     # Full build with tests
```

## Using ARDoCo as a Dependency

```xml
<dependencies>
  <dependency>
    <groupId>io.github.ardoco.core</groupId>
    <artifactId>pipeline-core</artifactId> <!-- or another published module such as io.github.ardoco.tlr:pipeline-tlr -->
    <version>VERSION</version>
  </dependency>
</dependencies>
```

See the [main pom.xml](../pom.xml) for available modules and coordinates.

## Environment Configuration

ARDoCo supports local NLP preprocessing (via Stanford CoreNLP) or a remote microservice. LLM-based approaches require API keys. See [`/sample.env`](../sample.env) for all available environment variables. Key variables:

- `NLP_PROVIDER_SOURCE` — set to `microservice` to use the remote StanfordCoreNLP service
- `MICROSERVICE_URL`, `SCNLP_SERVICE_USER`, `SCNLP_SERVICE_PASSWORD` — microservice credentials
- `OPENAI_API_KEY`, `OPENAI_ORGANIZATION_ID` — for LLM-based approaches
- `OLLAMA_HOST`, `OLLAMA_USER`, `OLLAMA_PASSWORD`, `OLLAMA_TOKEN` — for local LLM via Ollama
- `LLM_CACHE_DIR` — caching directory for LLM requests
- `SEED` — random seed for reproducibility

## Documentation Map

| Page | Covers |
|------|--------|
| [Architecture](architecture.md) | Pipeline composite pattern, DataRepository blackboard, intermediate artifacts (Text, SAM, Code Model) |
| [TLR Approaches](tlr-approaches.md) | All traceability link recovery approaches, their pipeline compositions, and stage modules |
| [Inconsistency Detection](inconsistency-detection.md) | TEAM and MEAT inconsistency types, detection pipeline, configuration options |
| [Operations](operations.md) | Build system, code formatting, JSpecify nullness, CI, monorepo sync scripts, external services |

## Task Routing

When changing a specific area, start at the listed page and use the focused validation command. All commands run from the repository root.

| Change area / intent | Wiki page | Source entry points | Key symbols / types | Focused tests | Minimal validation |
|----------------------|----------|---------------------|---------------------|---------------|--------------------|
| Add or modify a pipeline stage/agent/informant | [Architecture](architecture.md) | `core/framework/common/.../pipeline/`, `tlr/stages-tlr/` | `AbstractPipelineStep`, `AbstractExecutionStage`, `PipelineAgent`, `Informant`, `DataRepository` | `core/tests-base` architecture tests | `mvn -pl core clean verify` |
| Add or change a TLR approach / runner | [TLR Approaches](tlr-approaches.md) | `tlr/pipeline-tlr/.../execution/`, `tlr/stages-tlr/` | `Swattr`, `Arcotl`, `Transarc`, `ArCoTLInformant`, `TraceLinkGenerator` | `tlr/tests-tlr` integration tests (`*IT.java`) | `mvn -pl tlr clean verify` |
| Modify ArCoTL heuristics or transitive linking | [TLR Approaches](tlr-approaches.md) | `tlr/stages-tlr/code-traceability/.../arcotl/`, `.../informants/TraceLinkCombiner.java` | `TraceLinkGenerator`, `TraceLinkCombiner` | `tlr/tests-tlr` integration tests | `mvn -pl tlr/stages-tlr/code-traceability clean verify` |
| Change LLM/NER-based TLR (ExArch, ArTEMiS) | [TLR Approaches](tlr-approaches.md) | `tlr/stages-tlr/model-provider/`, `tlr/stages-tlr/connection-generator-ner/` | `LlmArchitectureProviderInformant`, `NerConnectionGenerator`, `NerAgent` | `tlr/tests-tlr` integration tests | `mvn -pl tlr clean verify` |
| Add or modify inconsistency detection | [Inconsistency Detection](inconsistency-detection.md) | `inconsistency-detection/stages-id/inconsistency-detection/`, `inconsistency-detection/pipeline-id/` | `InconsistencyChecker`, `TextEntityAbsentFromModelInconsistencyAgent`, `ModelEntityAbsentFromTextInconsistencyAgent` | `tests-inconsistency` integration tests | `mvn -pl inconsistency-detection clean verify` |
| Build, dependencies, CI, formatting, env config | [Operations](operations.md) | `pom.xml`, `.github/workflows/`, `{module}/formatter.xml` | parent POM, Spotless, JSpecify, flatten-maven-plugin | `format.yml`, `verify.yml` | `mvn spotless:check && mvn clean verify` |
| Intermediate artifact / data model (Text, SAM, Code) | [Architecture](architecture.md) | `core/framework/common/.../api/` | `Text`, `ArchitectureItem`, `CodeItem`, `Entity` | `core/tests-base` | `mvn -pl core/framework/common clean verify` |
| Code model data model (line ranges, content, id, imports, callees) | [Architecture](architecture.md) | `core/framework/common/.../api/models/code/`, `.../api/models/Model.java`, `.../api/models/CodeModel.java` | `Datatype`, `ClassUnit`, `InterfaceUnit`, `ControlElement` (`calleeNames`), `CodeCompilationUnit` (`importedModuleNames`), `CodeAssembly` (`importedModuleNames`), `Model`, `CodeModelDto` | `tlr/stages-tlr/model-provider` mapper/extractor tests (`JavaControlExtractorTest`, `JavaModelMapperTest`, `Python3ModelMapperTest`, `JavaExtractorTest`) | `mvn -pl core/framework/common clean verify && mvn -pl tlr/stages-tlr/model-provider clean verify` |
| Code model extraction / ANTLR mappers / legacy JavaModel | [TLR Approaches](tlr-approaches.md) | `tlr/stages-tlr/model-provider/.../antlr/mapping/{cpp,java,python3}/mappers/`, `.../generators/code/java/JavaModel.java` | `ClassMapper`, `FunctionMapper`, `InterfaceMapper`, `JavaModel` | `tlr/stages-tlr/model-provider` tests | `mvn -pl tlr/stages-tlr/model-provider clean verify` |

## Existing Wiki Documentation

The project maintains a [GitHub Wiki](https://github.com/ardoco/ardoco/wiki) with source files in [`/docs`](../docs/Home.md). This OpenWiki serves as an opinionated map and synthesis over those docs, with added source-level detail for developers and agents.

## External Repositories

- [LiSSA](https://github.com/ardoco/lissa) — LLM-based generic TLR framework
- [StanfordCoreNLP-Provider-Service](https://github.com/ardoco/StanfordCoreNLP-Provider-Service) — text preprocessing microservice
- [Benchmark](https://github.com/ardoco/benchmark) — evaluation benchmarks and datasets
- [Evaluator](https://github.com/ardoco/evaluator) — evaluation code for comparing results
- [TraceView](https://github.com/ardoco/traceview-v2) — visualization tool for TLR and ID outputs
- [Actions](https://github.com/ardoco/actions) — reusable GitHub Actions
