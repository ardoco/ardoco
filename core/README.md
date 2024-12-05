# ArDoCo Core

[![Maven Verify](https://github.com/ArDoCo/Core/actions/workflows/verify.yml/badge.svg)](https://github.com/ArDoCo/Core/actions/workflows/verify.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.ardoco.core/parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.ardoco.core/parent)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ArDoCo_Core&metric=alert_status)](https://sonarcloud.io/dashboard?id=ArDoCo_Core)
[![Latest Release](https://img.shields.io/github/release/ArDoCo/Core.svg)](https://github.com/ArDoCo/Core/releases/latest)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7274034.svg)](https://doi.org/10.5281/zenodo.7274034)

The goal of the ArDoCo project is to connect architecture documentation and models with Traceability Link Recovery (TLR) while identifying missing or deviating elements (inconsistencies).
An element can be any representable item of the model, like a component or a relation.
To do so, we first create trace links and then make use of them and other information to identify inconsistencies.

ArDoCo is actively developed by researchers of the _[Modelling for Continuous Software Engineering (MCSE) group](https://mcse.kastel.kit.edu)_ of _[KASTEL - Institute of Information Security and Dependability](https://kastel.kit.edu)_ at the [KIT](https://www.kit.edu).

This **Core** repository contains the framework and core definitions for the other approaches.
As such, there is the definition of our pipeline and the data handling as well as the definitions for the various pipeline steps, inputs, outputs, etc.

For more information about the setup, the project structure, or the architecture, please have a look at the [Wiki](https://github.com/ArDoCo/Core/wiki).

## Maven

```xml

<dependencies>
	<dependency>
		<groupId>io.github.ardoco.core</groupId>
		<artifactId>framework</artifactId> <!-- or any other subproject -->
		<version>VERSION</version>
	</dependency>
</dependencies>
```

For snapshot releases, make sure to add the following repository

```xml

<repositories>
	<repository>
		<releases>
			<enabled>false</enabled>
		</releases>
		<snapshots>
			<enabled>true</enabled>
		</snapshots>
		<id>mavenSnapshot</id>
		<url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
	</repository>
</repositories>
```

## Relevant repositories
The following is an excerpt of repositories that use this framework and implement the different approaches and pipelines of ArDoCo:
* [ArDoCo/TLR](https://github.com/ArDoCo/TLR): implementing different traceability link recovery approaches
* [ArDoCo/InconsistencyDetection](https://github.com/ArDoCo/InconsistencyDetection): implementing inconsistency detection approaches
* [ArDoCo/LiSSA](https://github.com/ArDoCo/LiSSA): implementing processing of sketches and diagrams for, e.g., TLR