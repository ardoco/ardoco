# ARDoCo Traceability Link Recovery

The goal of this project is to create trace links between artifacts like software architecture documentation, software architecture, and code.

## Approaches

This repository contains several TLR approaches:

* **SWATTR** - Agent-based framework for linking textual architecture documentation (SAD) and formal models (SAM) using NLP and heuristics
* **ArDoCode** - Directly matches architecture documentation with code elements without requiring a formal model
* **ArCoTL** - Links architecture models (SAM) to source code using intermediate representations and matching heuristics
* **TransArC** - Transitive approach that connects documentation to code via an intermediate model (SAD → SAM → Code)
* **ExArch** - Uses Large Language Models to automatically extract architecture components from documentation and match them to code

For more information about these and other approaches like [LiSSA](https://ardoco.de/approaches/lissa/), visit [ardoco.de/approaches](https://ardoco.de/approaches).

ARDoCo is actively developed by researchers of
the _[Modelling for Continuous Software Engineering (MCSE) group](https://mcse.kastel.kit.edu)_
of _[KASTEL - Institute of Information Security and Dependability](https://kastel.kit.edu)_ at
the [KIT](https://www.kit.edu).
