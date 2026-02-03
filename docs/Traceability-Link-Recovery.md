# Traceability Link Recovery

ARDoCo provides multiple approaches for recovering traceability links between different software artifacts. This repository contains several TLR approaches that can be applied depending on the artifacts involved.

## Repository Approaches

### SWATTR

**Artifacts**: SAD (Software Architecture Documentation) ↔ SAM (Software Architecture Model)

SWATTR establishes traceability links between Software Architecture Documentation (SAD) and Software Architecture Models (SAM) using an agent-based framework with NLP and heuristics. The approach uses a multi-stage pipeline:

1. **Model Extraction**: Processes the architecture model to extract components and their properties
2. **Text Preprocessing**: Initial text processing including tokenization, part-of-speech tagging, and dependency parsing
3. **Text Extraction**: Identifies potentially relevant architecture elements mentioned in the text
4. **Recommendation Generator**: Processes extracted text to generate recommendations for model elements
5. **Connection Generator**: Maps recommended textual elements to actual model components

SWATTR focuses on identifying architecture elements (like component names) mentioned in documentation and connecting them to the corresponding elements in the formal model.

### ArDoCode

**Artifacts**: SAD ↔ Code

ArDoCode establishes traceability links between Software Architecture Documentation (SAD) and Code. It treats source code itself as the model. Instead of requiring a formal architecture model, ArDoCode directly matches architecture documentation content with code elements using heuristics designed for doc-to-code linking. It extracts key terms from documentation and aligns them with names in the code (e.g., class or module names).

### ArCoTL

**Artifacts**: SAM ↔ Code

ArCoTL establishes traceability links between Software Architecture Models (SAM) and Code. It transforms both the architecture model and code into intermediate representations:
* **AMTL** (Architecture Model for Trace Links): Intermediate representation of the architecture
* **CMTL** (Code Model for Trace Links): Intermediate representation of the code

The approach applies various heuristics to match elements between these representations:
* Standalone rules for individual element matching
* Dependent rules that consider relationships between elements
* Filters to refine the discovered links

ArCoTL supports multiple programming languages (Java, Python, Shell) and metamodels (PCM, UML).

### TransArC

**Artifacts**: SAD → SAM → Code

TransArC establishes traceability links between Software Architecture Documentation, Models, and Code through transitive relationships. It combines:

1. **SWATTR**: Links documentation (SAD) to the architecture model (SAM)
2. **ArCoTL**: Links the model (SAM) to code
3. **Transitive Inference**: Creates SAD-to-Code links via the model intermediary

This transitive approach helps bridge the semantic gap between informal text and code by using the formal model as an intermediary.

## Additional Approaches

### ExArch

**Artifacts**: SAD ↔ Code

ExArch extends the TransArC idea by using Large Language Models (LLMs) to automatically extract or invent a simple architecture model from the software architecture documentation (and optionally from code). Instead of requiring a hand-made formal architecture model, ExArch prompts an LLM (such as GPT-4) to generate a list of likely component names, which forms a minimal "Simple Software Architecture Model" (SSAM). These LLM-derived components are then matched to code using the same heuristics as TransArC. The goal is to bridge the SAD-to-code gap without manual modeling.

**How it works**:
1. Extract component names from SAD using an LLM
2. Create a simple architecture model (SSAM) from the extracted names
3. Match SSAM components to code elements using similarity heuristics
4. Establish SAD-to-Code trace links

This approach demonstrates that LLMs can automatically infer key architectural components and serve as a bridge between documentation and code.

### LiSSA

LiSSA uses Large Language Models (LLMs) combined with Information Retrieval (IR) for generic Traceability Link Recovery across various artifact types. It can be applied to requirements-to-code, documentation-to-code, architecture-to-code, and other tracing scenarios.

For detailed information, see the [LiSSA page](lissa) or visit [ardoco.de/approaches/lissa](https://ardoco.de/approaches/lissa/).

## Choosing an Approach

* Use **SWATTR** when you have architecture documentation and a formal model
* Use **ArDoCode** when you only have documentation and code (no formal model)
* Use **ArCoTL** when you have a formal architecture model and want to link it to code
* Use **TransArC** when you need complete SAD-to-Code traceability with a model intermediary
* Use **ExArch** when you want LLM-based component extraction for SAD-to-Code linking
* Use **LiSSA** when you need generic TLR across various artifact combinations
