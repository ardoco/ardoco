# Automating Requirements and Documentation Comprehension (ARDoCo)

[![Maven Verify](https://github.com/ardoco/ARDoCo/actions/workflows/verify.yml/badge.svg?branch=main)](https://github.com/ArDoCo/ArDoCo/actions/workflows/verify.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.ardoco.core/parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.ardoco.core/parent)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ArDoCo_ArDoCo&metric=alert_status)](https://sonarcloud.io/dashboard?id=ArDoCo_ArDoCo)
[![Latest Release](https://img.shields.io/github/release/ardoco/ARDoCo.svg)](https://github.com/ardoco/ARDoCo/releases/latest)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7274034.svg)](https://doi.org/10.5281/zenodo.7274034)

The goal of the ARDoCo project is to connect architecture documentation and models with Traceability Link Recovery (TLR) while identifying missing or deviating elements (inconsistencies).
An element can be any representable item of the model, like a component or a relation.
To do so, we first create trace links and then make use of them and other information to identify inconsistencies.

ARDoCo is actively developed by researchers of the _[Modelling for Continuous Software Engineering (MCSE) group](https://mcse.kastel.kit.edu)_ of _[KASTEL - Institute of Information Security and Dependability](https://kastel.kit.edu)_ at the [KIT](https://www.kit.edu).

This repository contains the framework and the approaches.
As such, there is the definition of our pipeline and the data handling as well as the definitions for the various pipeline steps, inputs, outputs, etc.

For more information about the setup, the project structure, or the architecture, please have a look at the [Wiki](https://github.com/ardoco/ARDoCo/wiki).

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
* [ardoco/tlr](https://github.com/ardoco/TLR): implementing different traceability link recovery approaches
* [ardoco/inconsistency-detection](https://github.com/ardoco/inconsistency-detection): implementing inconsistency detection approaches

### Pull
* `git subtree pull --prefix=core git@github.com:ardoco/core <<branch>>`
* `git subtree pull --prefix=tlr git@github.com:ardoco/tlr <<branch>>`
* `git subtree pull --prefix=inconsistency-detection git@github.com:ardoco/inconsistency-detection <<branch>>`

### Push
* `git subtree push --prefix=core git@github.com:ardoco/core <<branch>>`
* `git subtree push --prefix=tlr git@github.com:ardoco/tlr <<branch>>`
* `git subtree push --prefix=inconsistency-detection git@github.com:ardoco/inconsistency-detection <<branch>>`
