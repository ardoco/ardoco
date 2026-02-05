# ARDoCo - Automating Requirements and Documentation Comprehension

[![Maven Verify](https://github.com/ardoco/ardoco/actions/workflows/verify.yml/badge.svg?branch=main)](https://github.com/ardoco/ardoco/actions/workflows/verify.yml)
[![Maven Central](https://maven-badges.sml.io/maven-central/io.github.ardoco/parent/badge.svg)](https://maven-badges.sml.io/maven-central/io.github.ardoco/parent)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ArDoCo_ArDoCo&metric=alert_status)](https://sonarcloud.io/dashboard?id=ArDoCo_ArDoCo)
[![Latest Release](https://img.shields.io/github/release/ardoco/ardoco.svg)](https://github.com/ardoco/ardoco/releases/latest)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7274034.svg)](https://doi.org/10.5281/zenodo.7274034)

The goal of the ARDoCo project is to connect architecture documentation and models with Traceability Link Recovery (TLR) while identifying missing or deviating elements (inconsistencies).
An element can be any representable item of the model, like a component or a relation.
To do so, we first create trace links and then make use of them and other information to identify inconsistencies.

Our recent approaches, such as [LiSSA](https://ardoco.de/approaches/lissa/), leverage Large Language Models (LLMs) and Information Retrieval (IR) to enable more generic and effective traceability link recovery across various artifact types.

ARDoCo is actively developed by researchers of the _[Modelling for Continuous Software Engineering (MCSE) group](https://mcse.kastel.kit.edu)_ of _[KASTEL - Institute of Information Security and Dependability](https://kastel.kit.edu)_ at the [KIT](https://www.kit.edu).

**Website**: [ardoco.de](https://ardoco.de)

This repository contains the framework and the approaches.
As such, there is the definition of our pipeline and the data handling as well as the definitions for the various pipeline steps, inputs, outputs, etc.

For more information about the setup, the project structure, or the architecture, please have a look at the [Wiki](https://github.com/ardoco/ardoco/wiki).

## Additional Resources

* [Approaches](https://ardoco.de/approaches/) - Overview of different approaches including LiSSA
* [Publications](https://ardoco.de/publications/) - Research papers and presentations
* [People](https://ardoco.de/people/) - Team members and contributors

## Maven

```xml

<dependencies>
	<dependency>
		<groupId>io.github.ardoco</groupId>
		<artifactId>parent</artifactId> <!-- or any other subproject -->
		<version>VERSION</version>
	</dependency>
</dependencies>
```

## Relevant repositories
The following is an excerpt of repositories that use this framework and implement the different approaches and pipelines of ARDoCo:
* [ardoco/core](https://github.com/ardoco/core): implementing the core of the approach
* [ardoco/tlr](https://github.com/ardoco/tlr): implementing different traceability link recovery approaches
* [ardoco/inconsistency-detection](https://github.com/ardoco/inconsistency-detection): implementing inconsistency detection approaches
