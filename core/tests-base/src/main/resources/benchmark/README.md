[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.6966831.svg)](https://doi.org/10.5281/zenodo.6966831)

# Benchmark
This repository contains a benchmark for traceability link recovery (TLR) between textual Software Architecture Documentation (SAD) and Software Architecture Models (SAM).
It was initially published in the paper [Establishing a Benchmark Dataset for Traceability Link Recovery Between Software Architecture Documentation and Models](https://doi.org/10.1007/978-3-031-36889-9_30).

Each project of the benchmark is structured as follows:

* The README of each project contains some information about the used languages and lines of code created with [cloc](https://github.com/AlDanial/cloc).
* The folder `model_<year>` contains the architecture model of the project. 
	* The folder `pcm` contains a Palladio Component Model (PCM) of the system. It has at least the repository view (components) of the system.
	* The folder `uml` contains a Papyrus UML model. It is created from the PCM Repository using [PCM2UML](https://github.com/InFormALin/PCM2UML).
	* The folder `code` contains a code model. The version of the code is stated in a README.md next to the model. The model is an `ArDoCo Code Model`. The model can be loaded using the [ArDoCo Code Extractor](https://github.com/ArDoCo/Core/blob/main/stages/model-provider/src/main/java/edu/kit/kastel/mcse/ardoco/core/models/connectors/generators/code/CodeExtractor.java#L47).
* The folder `text_<year>` contains a documentation of the project.
	* The text file(s) in the project folder contains the SAD of the projects as plain text.
* The folder `diagrams_<year>` contains the informal diagrams of the project.
* The folder `goldstandards` contains all gold standards for the project. In the following, the `<year>` refers to the year of the artifact. Additional classifiers are added to the end of the file names. You will find more information about these classifiers in the README of the respective project artifacts.
	* The file `goldstandard_sad_<year>-sam_<year>.csv` contains the traceability links between SAD and SAM. It links the model elements by id with the sentences by their number 
	* The file `goldstandard_sad_<year>-sam_<year>_UME.csv` contains all IDs of model elements that are contained in the model but not described in the text.
	* The file `goldstandard_sad_<year>_code_<year>.csv` contains the traceability links between SAD and code models.
	* The file `goldstandard_sam_<year>-code_<year>.csv` is a gold standard for mapping the architecture elements and code elements.
	* The file `goldstandard_sad_id_<year>.json` contains the traceability links between SAD and informal diagrams. The JSON schema is specified in the [DiagramSchema](DiagramSchema.json) file.
	


## Using the benchmark
In order to provide an easy approach to use the benchmark, we provide an example TLR approach called [Simple Tracelink Discovery (STD)](https://github.com/ArDoCo/SimpleTracelinkDiscovery/) that uses this benchmark in its [evaluation](https://github.com/ArDoCo/SimpleTracelinkDiscovery/tree/main/src/test/java/io/github/ardoco/simpletracelinkdiscovery/eval).
Therefore, the benchmark is linked to the STD repository via a [git subtree](https://github.com/ArDoCo/SimpleTracelinkDiscovery/tree/main/src/test/resources/benchmark).

## Projects

### BigBlueButton
BigBlueButton (BBB) is a non-scientific application that provides a web conferencing system with the focus on creating a "global teaching platform".

### MediaStore
MediaStore is a "model application built after the iTunes Store".
Its architecture was used for exemplary performance analyses on software architecture models.

### Teammates
TEAMMATES is an open-source "online tool for manageing peer evaluations and other feedback paths of your students".

### Teastore
Teastore is a scientific application that is used as a "micro-service reference test application".

### JabRef
JabRef is a tool to manage citations and references in your bibliographies. It has features to collect, organize, cite, and share research work.


## LICENSE
> **Note**
>
> Our LICENSE does only apply to the models and the Gold Standards (CSV & JSON files). The texts and diagrams are licensed w.r.t. to the actual projects.
> More details about the LICENSE can be found in the README files of the respective texts and diagrams.
