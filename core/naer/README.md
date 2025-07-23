# Named Entity Recognition for Architecture

A Java library for identifying and extracting named entities from textual Software Architecture Documentation (SAD)
using Large Language Models (LLMs).

## Overview

This library uses natural language processing techniques and LLMs to automatically identify and extract named entities (
e.g. architectural components) from SAD.
It supports multiple LLM providers and configurable prompts to optimize recognition accuracy.
The library is also designed for easy extension, enabling integration with custom or locally hosted LLMs.

## Features

- Identify named entities in SAD
- Support for multiple LLM providers (OpenAI and
  VDL ([Virtual Design Lab Server from KIT SDQ](https://sdq.kastel.kit.edu/wiki/Virtual_Design_Lab_Server)), with easy
  extensibility to add more)
- Configurable prompts for different recognition strategies
- Evaluation against goldstandard data
- Flexible builder pattern for easy configuration

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- Access to LLM APIs (OpenAI API key, VDL account, etc.)

## Installation

Add the following dependency to your Maven project:

```xml

<dependency>
    <groupId>edu.kit</groupId>
    <artifactId>vongeisaujohannes</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Or clone the repository and build it locally:

```bash
git clone https://gitlab.kit.edu/kit/kastel/sdq/stud/praktika/sose2025/vongeisaujohannes.git
cd vongeisaujohannes
mvn clean install
```

## Usage

### Basic Usage

```java
// Create a recognizer using the builder pattern
NamedEntityRecognizer recognizer = new NamedEntityRecognizer.Builder(Path.of("software/architecture/documentation.txt")).build();

// Recognize named entities
Set<NamedEntity> entities = recognizer.recognize();
```

### Using Custom Configuration

```java
// Create a recognizer with custom configuration of the used LLM chatmodel and prompt
NamedEntityRecognizer recognizer = new NamedEntityRecognizer.Builder(Path.of("software/architecture/documentation.txt"))
                .chatModel(ChatModelFactory.withProvider(ModelProvider.OPEN_AI).modelName("gpt-4.1").temperature(0.5).timeout(60).build())
                .prompt(new JsonOutputPrompt("Perform NER and use the following output format..."))
                .build();

// Recognize named entities
Set<NamedEntity> entities = recognizer.recognize();
```

### Using Possible Entities

```java
// Create a recognizer
NamedEntityRecognizer recognizer = new NamedEntityRecognizer.Builder(Path.of("software/architecture/documentation.txt")).build();

// Prepare possible entities that could be mentioned in the SAD (to help the model)
Map<NamedEntityType, Set<String>> possibleEntities = Map.of(NamedEntityType.COMPONENT, Set.of("AuthenticationService", "UserDatabase"));

// Recognize named entities with possible entities
Set<NamedEntity> entities = recognizer.recognize(possibleEntities);
```

## Evaluation

The library includes test functionality for evaluating the performance of the named entity recognition against
goldstandard data... (sollte das mit in der Readme stehen?)