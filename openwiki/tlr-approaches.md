# TLR Approaches

ARDoCo provides multiple Traceability Link Recovery (TLR) approaches that connect different types of software artifacts. This page documents each approach, its pipeline composition, and the underlying stage modules.

## Artifact Types

| Abbreviation | Meaning |
|--------------|---------|
| **SAD** | Software Architecture Documentation (natural language text) |
| **SAM** | Software Architecture Model (formal model: UML, PCM) |
| **Code** | Source code (Java, Python, etc.) |

## Approach Overview

| Approach | Artifacts | Description |
|----------|-----------|-------------|
| **SWATTR** | SAD ↔ SAM | Agent-based NLP pipeline linking documentation to architecture models |
| **ArDoCode** | SAD ↔ Code | Treats code as the model; matches documentation directly to code elements |
| **ArCoTL** | SAM ↔ Code | Uses heuristic computation tree to match architecture model to code |
| **TransArC** | SAD → SAM → Code | Combines SWATTR (SAD→SAM) + ArCoTL (SAM→Code) + transitive inference |
| **ExArch** | SAD ↔ Code | Uses LLM to extract component names from SAD, then matches to code via ArCoTL |
| **ArTEMiS** | SAD ↔ SAM | Uses LLM-based NER for entity recognition and matching |
| **ArTEMiS+ExArch** | SAD ↔ Code | LLM-generated SAM + ArTEMiS NER matching + ArCoTL SAM→Code |
| **ArTEMiS+TransArC** | SAD ↔ Code | Manual SAM + ArTEMiS NER matching + ArCoTL SAM→Code |

## Pipeline Compositions

Each approach is implemented as a runner class extending `ArdocoRunner`. The runner's `definePipeline()` method wires together stages from the `stages-tlr/` module.

### Runners

All runners are in `/tlr/pipeline-tlr/src/main/java/.../tlr/execution/`:

| Runner | Source File |
|--------|-------------|
| `Swattr` | `Swattr.java` |
| `Ardocode` | `Ardocode.java` |
| `Arcotl` | `Arcotl.java` |
| `Transarc` | `Transarc.java` |
| `ExArch` | `ExArch.java` |
| `Artemis` | `Artemis.java` |
| `ArtemisInExArch` | `ArtemisInExArch.java` |
| `ArtemisInTransArC` | `ArtemisInTransArC.java` |

### Pipeline Steps per Approach

**SWATTR** (SAD → SAM):
```
TextPreprocessingAgent → ModelProviderAgent → TextExtraction → RecommendationGenerator → ConnectionGenerator
```

**ArDoCode** (SAD → Code):
```
ModelProviderAgent → TextPreprocessingAgent → TextExtraction → RecommendationGenerator → ConnectionGenerator → SadCodeTraceabilityLinkRecovery
```

**ArCoTL** (SAM → Code):
```
ModelProviderAgent → SamCodeTraceabilityLinkRecovery
```

**TransArC** (SAD → SAM → Code, transitive):
```
TextPreprocessingAgent → ModelProviderAgent → TextExtraction → RecommendationGenerator → ConnectionGenerator → SamCodeTraceabilityLinkRecovery → SadSamCodeTraceabilityLinkRecovery
```

**ExArch** (SAD → Code via LLM-generated model):
```
TextPreprocessingAgent → ModelProviderAgent → LlmArchitectureProviderAgent → TextExtraction → RecommendationGenerator → ConnectionGenerator → SamCodeTraceabilityLinkRecovery → SadSamCodeTraceabilityLinkRecovery
```

**ArTEMiS** (SAD ↔ SAM via LLM NER):
```
SimpleTextPreprocessingAgent → ModelProviderAgent → NerConnectionGenerator
```

## Stage Modules

All stages live under `/tlr/stages-tlr/`. Each is a separate Maven module implementing an `AbstractExecutionStage`.

### 1. Text Preprocessing (`text-preprocessing/`)

NLP preprocessing using Stanford CoreNLP.

- **Agents**: `TextPreprocessingAgent`, `SimpleTextPreprocessingAgent` (lightweight variant)
- **Informants**: `CoreNLPProvider`, `SimpleTextProvider`
- **Output**: `Text` object with tokenization, POS tags, dependency parsing, NER, lemmatization

### 2. Text Extraction (`text-extraction/`)

Extracts noun mappings and phrase mappings from preprocessed text.

- **Stage**: `TextExtraction`
- **Agents**: `InitialTextAgent`, `PhraseAgent`
- **Informants**: `CompoundAgentInformant`, `InDepArcsInformant`, `NounInformant`, `OutDepArcsInformant`, `SeparatedNamesInformant`, `MappingCombinerInformant`
- **Output**: `TextState` with `NounMapping`s and `PhraseMapping`s

### 3. Model Provider (`model-provider/`)

Extracts architecture models (PCM, UML) and code models (Java, C++, Python).

- **Agents**: `ModelProviderAgent`, `LlmArchitectureProviderAgent` (generates SAM from text via LLM)
- **Informants**: `ModelProviderInformant`, `LlmArchitectureProviderInformant`
- **Configurations**: `ArchitectureConfiguration` (PCM/UML), `CodeConfiguration` (Java/C++/Python)
- **Code extraction**: Uses ANTLR-based parsers under `connectors/generators/antlr/`
- **Output**: `ArchitectureModel` or `CodeModel` stored in `ModelStates`

### 4. Recommendation Generator (`recommendation-generator/`)

Generates recommended instances from text + model data.

- **Stage**: `RecommendationGenerator`
- **Agents**: `InitialRecommendationAgent`, `PhraseRecommendationAgent`
- **Informants**: `NameTypeInformant`, `CompoundRecommendationInformant`
- **Output**: `RecommendationState` with `RecommendedInstance`s

### 5. Connection Generator (`connection-generator/`)

Creates trace links between text and model elements (SWATTR pipeline).

- **Stage**: `ConnectionGenerator`
- **Agents**: `InitialConnectionAgent`, `ReferenceAgent`, `ProjectNameFilterAgent`, `InstanceConnectionAgent`
- **Informants**: `ExtractionDependentOccurrenceInformant`, `InstantConnectionInformant`, `NameTypeConnectionInformant`, `ProjectNameInformant`, `ReferenceInformant`
- **Output**: `ConnectionState` with trace links between `RecommendedInstance`s and model elements

### 6. Connection Generator NER (`connection-generator-ner/`)

LLM-based entity recognition and matching (ArTEMiS pipeline).

- **Stage**: `NerConnectionGenerator`
- **Agents**: `NerAgent` (LLM-based entity recognition), `NerConnectionAgent` (matches entities to architecture components)
- **Informants**: `NerInformant`, `NerConnectionInformant`
- **Output**: `NerConnectionState` with `NamedArchitectureEntity` mappings

### 7. Code Traceability (`code-traceability/`)

SAM ↔ Code traceability links (ArCoTL) and transitive links (TransArC).

- **Stages**: `SamCodeTraceabilityLinkRecovery`, `SadCodeTraceabilityLinkRecovery`, `SadSamCodeTraceabilityLinkRecovery`
- **Agents**: `InitialCodeTraceabilityAgent`, `ArchitectureLinkToCodeLinkTransformerAgent`, `TransitiveTraceabilityAgent`
- **Informants**: `ArCoTLInformant`, `ArchitectureLinkToCodeLinkTransformerInformant`, `TraceLinkCombiner`

## ArCoTL Heuristic Computation Tree

The `ArCoTLInformant` delegates to `TraceLinkGenerator` (`/tlr/stages-tlr/code-traceability/src/main/java/.../arcotl/TraceLinkGenerator.java`), which constructs a computation tree of heuristic nodes:

```
Root = Filter(
    Maximum(
        PathResemblance → MatchBest → MatchBest,
        ComponentNameResemblance → MatchBest → InheritLinks → MatchSequentially → SubpackageFilter,
        ComponentNameResemblance(interface) → MatchBest → MatchBest → MatchBest → Maximum → MatchBest → MatchBest
    ),
    ProvidedInterfaceCorrespondence filter
)
```

**Standalone heuristics**: `ComponentNameResemblance`, `PackageResemblance`, `MethodResemblance`, `PathResemblance`
**Dependent heuristics**: `InheritLinks`, `SubpackageFilter`, `Required`, `ProvidedInterfaceCorrespondence`
**Aggregation functions**: `Maximum`, `MatchBest`, `MatchSequentially`, `Filter`

## Transitive Linking

The `TraceLinkCombiner` (`/tlr/stages-tlr/code-traceability/src/main/java/.../informants/TraceLinkCombiner.java`) combines SAD→SAM links (from `ConnectionStates` or `NerConnectionStates`) with SAM→Code links (from ArCoTL) into transitive SAD→Code links using `combineToTransitiveTraceLinks()`.

## Testing

Integration tests for each approach are in `/tlr/tests-tlr/src/test/java/.../execution/runner/`:
- `SwattrTest.java`, `ArdocodeTest.java`, `ArcotlTest.java`, `TransarcTest.java`

These tests evaluate against benchmark datasets and measure precision, recall, and F1 scores against expected results.

## Choosing an Approach

- **SWATTR** — when you have architecture documentation and a formal model
- **ArDoCode** — when you have documentation and code (no formal model)
- **ArCoTL** — when you have a formal architecture model and want to link it to code
- **TransArC** — when you need complete SAD→Code traceability with a model intermediary
- **ExArch** — when you want LLM-based component extraction for SAD→Code linking without a manual model
- **ArTEMiS** — when you want LLM-based NER matching between SAD and SAM
- **LiSSA** — for generic TLR across arbitrary artifact combinations (see [external repo](https://github.com/ardoco/lissa))
