---
type: "Reference"
title: "Inconsistency Detection"
description: "TEAM and MEAT inconsistency detection between SAD and SAM, the InconsistencyChecker stage, state management, and evaluation/testing infrastructure."
openwiki:
  roles: [domain, architecture, workflow, testing]
  change_kinds: [lifecycle]
  source_paths: [inconsistency-detection/pipeline-id/src/main/java, inconsistency-detection/stages-id/inconsistency-detection/src/main/java]
  symbols: [InconsistencyChecker, TextEntityAbsentFromModelInconsistencyAgent, ModelEntityAbsentFromTextInconsistencyAgent, InconsistencyStatesImpl, InconsistencyStateImpl]
  invariants: ["InconsistencyChecker runs after the SWATTR connection pipeline", "TEAM flags text entities with no trace link; MEAT flags model entities with insufficient trace links"]
  test_paths: [inconsistency-detection/stages-id/inconsistency-detection/src/test/java, inconsistency-detection/tests-inconsistency/src/test/java]
  validation_commands: ["mvn -pl inconsistency-detection clean verify"]
---

# Inconsistency Detection

ARDoCo's Inconsistency Detection (ID) identifies discrepancies between software architecture documentation (SAD) and architecture models (SAM). It uses trace link recovery results as a bridge: after establishing trace links, it identifies "orphan" elements on both sides.

## Inconsistency Types

Two types of inconsistencies are detected:

### TEAM — Text Entity Absent from Model

Elements described in the documentation that cannot be traced to any model element.

> **Terminology note**: In ARDoCo V1, this was called "Missing Model Element" (MME). Renamed in V2 to be more descriptive.

- **Agent**: `TextEntityAbsentFromModelInconsistencyAgent`
- **Informant**: `TextEntityAbsentFromModelInconsistencyInformant`
- **Type class**: `TextEntityAbsentFromModelInconsistency` (implements `TextInconsistency`)
- **Type string**: `"TextEntityAbsentFromModel"`

**Detection logic**:
1. Get all recommended instances (entities extracted from text)
2. Remove those already linked via trace links (`ConnectionState.getInstanceLinks()`)
3. Further remove candidates whose words overlap with linked recommended instances
4. Remaining candidates with sufficient support (`minSupport` default: 1) become inconsistencies

### MEAT — Model Entity Absent from Text

Model elements that have no (or insufficient) corresponding documentation.

> **Terminology note**: In ARDoCo V1, this was called "Undocumented Model Element" (UME). Renamed in V2.

- **Agent**: `ModelEntityAbsentFromTextInconsistencyAgent`
- **Informant**: `ModelEntityAbsentFromTextInconsistencyInformant`
- **Type class**: `ModelEntityAbsentFromTextInconsistency` (implements `ModelInconsistency`)
- **Type string**: `"ModelEntityAbsentFromText"`

**Detection logic**:
1. Get all model entities matching configured `types` (default: `["Component", "BasicComponent", "CompositeComponent"]`)
2. Filter out those with ≥ `minimumNeededTraceLinks` (default: 1) trace links
3. Apply whitelist filtering (regex-based name matching)
4. Remaining candidates become inconsistencies

**Configuration options**:
- `minimumNeededTraceLinks` (default: 1) — minimum trace links required to avoid being flagged
- `whitelist` (default: `[]`) — regex patterns for model element names to exclude
- `types` (default: `["Component", "BasicComponent", "CompositeComponent"]`) — model element types to check

## Detection Pipeline

The full inconsistency detection pipeline is wired in `InconsistencyDetection.java` (`/inconsistency-detection/pipeline-id/.../execution/runner/InconsistencyDetection.java`):

```
TextPreprocessingAgent → ModelProviderAgent → TextExtraction → RecommendationGenerator → ConnectionGenerator → InconsistencyChecker
```

This is essentially the SWATTR TLR pipeline plus the `InconsistencyChecker` stage at the end.

## InconsistencyChecker Stage

**Source**: `/inconsistency-detection/stages-id/inconsistency-detection/src/main/java/.../id/InconsistencyChecker.java`

Extends `AbstractExecutionStage` and orchestrates three agents in order:

### Agent 1: InitialInconsistencyAgent (Pre-filtering)

Copies recommended instances from the recommendation state into the inconsistency state, then applies three filter informants:

| Informant | Purpose | Key Parameters |
|-----------|---------|----------------|
| `RecommendedInstanceProbabilityFilter` | Filters by probability thresholds | `threshold=0.5`, `dynamicThreshold=true`, `thresholdNameAndTypeProbability=0.3`, `thresholdNameOrTypeProbability=0.8` |
| `OccasionFilter` | Removes RIs appearing fewer than `expectedAppearances` times | `expectedAppearances=2` |
| `UnwantedWordsFilter` | Filters RIs containing unwanted words, file endings, plurals, or numbers | Custom + common blacklists |

### Agent 2: TextEntityAbsentFromModelInconsistencyAgent (TEAM)

Wraps `TextEntityAbsentFromModelInconsistencyInformant` — finds recommended instances with no trace link and creates `TextEntityAbsentFromModelInconsistency` records.

### Agent 3: ModelEntityAbsentFromTextInconsistencyAgent (MEAT)

Wraps `ModelEntityAbsentFromTextInconsistencyInformant` — finds model entities with insufficient trace links and creates `ModelEntityAbsentFromTextInconsistency` objects.

## State Management

| Class | Source | Role |
|-------|--------|------|
| `InconsistencyChecker` | `stages-id/.../id/InconsistencyChecker.java` | Stage; `initializeState()` creates and registers `InconsistencyStatesImpl` |
| `InconsistencyStatesImpl` | `stages-id/.../id/InconsistencyStatesImpl.java` | `EnumMap<Metamodel, InconsistencyStateImpl>` container |
| `InconsistencyStateImpl` | `stages-id/.../id/InconsistencyStateImpl.java` | Holds `recommendedInstances` and `inconsistencies` lists per metamodel |
| `MissingElementInconsistencyCandidate` | `stages-id/.../id/MissingElementInconsistencyCandidate.java` | Tracks a `RecommendedInstance` + accumulated `MissingElementSupport` |
| `MissingElementSupport` | `stages-id/.../id/MissingElementSupport.java` | Enum: `ELEMENT_WITH_NO_TRACE_LINK`, `TRACED_ELEMENT_IN_SAME_SENTENCE`, `DEPENDENCY_TO_TRACED_ELEMENT`, `MULTIPLE_OVERLAPPING_RECOMMENDED_INSTANCES` |

## Testing Infrastructure

### Unit Tests

Located in `/inconsistency-detection/stages-id/inconsistency-detection/src/test/java/.../id/`:
- `types/AbstractInconsistencyTypeTest.java` — base test for inconsistency types
- `types/TextEntityAbsentFromModelInconsistencyTest.java` — TEAM type tests
- `types/ModelEntityAbsentFromTextInconsistencyTest.java` — MEAT type tests
- `agents/ModelEntityAbsentFromTextInconsistencyTest.java` — MEAT agent/informant tests

### Integration Tests

Located in `/inconsistency-detection/tests-inconsistency/src/test/java/.../id/tests/`:
- `integration/InconsistencyDetectionEvaluationIT.java` — full evaluation harness
- `integration/inconsistencyhelper/HoldBackArCoTLModelProvider.java` — simulates missing model elements for evaluation
- `integration/inconsistencyhelper/HoldBackRunResultsProducer.java` — produces evaluation results
- `eval/baseline/InconsistencyBaseline*.java` — baseline stage/agent/informant for comparison

### Architecture and Configuration Tests

- `ArchitectureTest.java` — architecture rule enforcement
- `ConfigurationTest.java` — configuration validation
- `DeterministicArdocoTest.java` — determinism verification

## Use Cases

- **Architecture Evolution**: Identifying outdated or missing documentation during system evolution
- **Quality Assurance**: Ensuring documentation completeness before releases
- **Onboarding**: Helping new team members understand documentation gaps
- **Consistency Checking**: Verifying alignment between informal documents and formal models
- **Reverse Engineering**: Finding unmentioned components when analyzing existing systems
