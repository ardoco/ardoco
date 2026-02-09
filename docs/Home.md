# ARDoCo

<p align="center">
 <img alt="ARDoCo" src="https://github.com/ardoco/.github/raw/main/profile/logo.png" height="210"/>
</p>

ARDoCo (Automating Requirements and Documentation Comprehension) is a research project focused on traceability link recovery and consistency analysis between software artifacts. The project connects architecture documentation and models while identifying missing or deviating elements (inconsistencies). An element can be any representable item of the model, like a component or a relation.

Our recent approaches, such as [LiSSA](https://ardoco.de/approaches/lissa/), leverage Large Language Models (LLMs) and Information Retrieval (IR) to enable more generic and effective traceability link recovery across various artifact types, including requirements-to-code, documentation-to-code, and architecture-to-code tracing.

ARDoCo is actively developed by researchers of the [Modelling for Continuous Software Engineering (MCSE) group](https://mcse.kastel.kit.edu) of [KASTEL - Institute of Information Security and Dependability](https://kastel.kit.edu) at the [KIT](https://www.kit.edu).

**Website**: [ardoco.de](https://ardoco.de) | **GitHub**: [github.com/ardoco](https://github.com/ardoco)

Before contributing, please read the [Quickstart Guide](quickstart) and the developer note on [Nullness and JSpecify](jspecify).

<!-- JavaDocs can be found [here](https://ardoco.github.io/Core-Docs/). -->

To get to know the project, please read the following pages:

* [Core Pipeline Definition](pipeline)
* [Intermediate Artifacts](intermediate-artifacts)
* [Text Preprocessing Microservice](Text-Preprocessing-Microservice)
* [Traceability Link Recovery (TLR)](traceability-link-recovery)
* [Inconsistency Detection (ID)](inconsistency-detection)
* [LiSSA](LiSSA)

## Project Structure

This repository serves as the main monorepo containing:

* **[Core](https://github.com/ardoco/ardoco/tree/main/core)**: Core framework with architecture, data structures, interfaces, and common code
  * **Framework**: Pipeline definitions and common utilities
  * **Pipeline Core**: Core pipeline orchestration
  * **Tests Base**: Testing infrastructure and benchmark data
* **[TLR](https://github.com/ardoco/ardoco/tree/main/tlr)**: Traceability Link Recovery modules with multiple approaches
  * **SWATTR**: TLR between Software Architecture Documentation and Models
  * **ArDoCode**: TLR between Software Architecture Documentation and Code
  * **ArCoTL**: TLR between Software Architecture Models and Code
  * **TransArC**: TLR between Documentation, Models, and Code (transitive)
  * **ExArch**: LLM-based Architecture Component Name Extraction for SAD-Code
  * See [ardoco.de/approaches](https://ardoco.de/approaches/) for detailed approach documentation
* **[Inconsistency Detection](https://github.com/ardoco/ardoco/tree/main/inconsistency-detection)**: Modules for detecting inconsistencies between artifacts

### External Repositories

* **[LiSSA](https://github.com/ardoco/lissa)**: LLM-based generic traceability link recovery framework
* **[StanfordCoreNLP-Provider-Service](https://github.com/ardoco/stanfordcorenlp-provider-service)**: RESTful web service for text preprocessing
* **[Benchmark](https://github.com/ardoco/benchmark)**: Evaluation benchmarks and datasets
* **[Evaluator](https://github.com/ardoco/evaluator)**: Evaluation code for comparing results
* **[TraceView](https://github.com/ardoco/traceview-v2)**: Visualization tool for TLR and ID outputs
* **[Actions](https://github.com/ardoco/actions)**: Reusable GitHub Actions

## System Requirements

The project requires **JDK 21**.
Furthermore, we advise at least **4 GB of RAM**.

## Benchmarks

You can test ARDoCo using the projects provided in our [Benchmark repository](https://github.com/ardoco/benchmark).

## Related Publications

For a complete list of publications, visit [ardoco.de/publications](https://ardoco.de/publications/).

### Recent Publications (2024-2025)

* D. Fuchß, S. Schwedt, J. Keim, and T. Hey. "Beyond Retrieval: A Study of Using LLM Ensembles for Candidate Filtering in Requirements Traceability". AIRE 2025. [ardoco.de/c/aire25](https://ardoco.de/c/aire25)

* D. Fuchß, T. Hey, J. Keim, H. Liu, N. Ewald, T. Thirolf, and A. Koziolek. "LiSSA: Toward Generic Traceability Link Recovery through Retrieval-Augmented Generation". ICSE 2025. [ardoco.de/c/icse25](https://ardoco.de/c/icse25)

* T. Hey, D. Fuchß, J. Keim, and A. Koziolek. "Requirements Traceability Link Recovery via Retrieval-Augmented Generation". REFSQ 2025. [ardoco.de/c/refsq25](https://ardoco.de/c/refsq25)

* D. Fuchß, H. Liu, T. Hey, J. Keim, and A. Koziolek. "Enabling Architecture Traceability by LLM-based Architecture Component Name Extraction". ICSA 2025. [ardoco.de/c/icsa25](https://ardoco.de/c/icsa25)

* J. Keim, S. Corallo, D. Fuchß, T. Hey, T. Telge, and A. Koziolek. "Recovering Trace Links Between Software Documentation And Code". ICSE 2024. [ardoco.de/c/icse24](https://ardoco.de/c/icse24)

### Earlier Work

* J. Keim, S. Corallo, D. Fuchß, and A. Koziolek. "Detecting Inconsistencies in Software Architecture Documentation Using Traceability Link Recovery". ICSA 2023. [ardoco.de/c/icsa23](https://ardoco.de/c/icsa23)

* J. Keim, S. Corallo, D. Fuchß, C. Kocher, J. Speit, and A. Koziolek. "Trace Link Recovery for Software Architecture Documentation". ECSA 2021. [ardoco.de/c/ecsa21](https://ardoco.de/c/ecsa21)

* J. Keim and A. Koziolek. "Towards Consistency Checking Between Software Architecture and Informal Documentation". ICSA-C 2019. [doi:10.1109/ICSA-C.2019.00052](https://doi.org/10.1109/ICSA-C.2019.00052)

The initial version of ARDoCo is based on the master thesis [Linking Software Architecture Documentation and Models](https://publikationen.bibliothek.kit.edu/1000126194).

## Contact

ARDoCo is actively developed by researchers at the Karlsruhe Institute of Technology (KIT).

For more information about the team, visit [ardoco.de/people](https://ardoco.de/people/).

Contact: [ardoco@lists.kit.edu](mailto:ardoco@lists.kit.edu)
