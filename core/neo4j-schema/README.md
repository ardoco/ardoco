# Neo4j-Schema
This component provides functionality to save and load architecture models, code models and preprocessed texts in a Neo4j graph database.
Moreover it provides functionality to insert and retrieve tracelinks and inconsistencies.
The component is designed to be used in conjunction with other components of the Ardoco Core, such as the Architecture Model and Code Model components.

## Schema
### Preprocessed Text
The graph for representing a preprocessed text follows a structure similar to the one provided by TextImpl.java, SentenceImpl.java, WordImpl.java and PhraseImpl.java
in ArDoCo.
The main node is the `PreprocessedText` node.
The image below shows an example of how a preprocessed text is represented in the graph, 
where the `PreprocessedText` node is connected to `Sentence` nodes, which are further connected to `Word` and `Phrase` nodes.

![Preprocessed_Text.png](Preprocessed_Text.png)

### Architecture Model
The graph for representing an architecture model follows a structure similar to the one provided ArchitectureItem, ArchitectureComponent,
ArchitectureInterface and ArchitectureMethod in ArDoCo.
The main node is the `ArchitectureModel` node.
The image below shows an example of how an architecture model is represented in the graph.

![Architecture_Model.png](ArchitectureModel.png)

### Code Model

### Tracelinks
Tracelinks are represented in the graph as relationships between nodes of the architecture model, nodes of the code model and sentence nodes of preprocessed text.
All the nodes between which a tracelink can be established also have the `traceable` label.
The relationship between tracelinks is called `TRACES_TO` and has the properties `traceLinkType`, which specifies the Type of Tracelink such as
`ARCHITECTURE_CODE` or `SENTENCE_ARCHITECTURE`  and `confidence` which represents the confidence of the tracelink (if given).
Transitive Tracelinks are not explicitly represented in the graph, but can be inferred by traversing the graph. 
For example, if there is a tracelink between an architecture component and a code component, and there is a tracelink between the code component and a sentence, 
we can infer that there is a transitive tracelink between the architecture component and the sentence.


Since neo4j only allows to have directed relationships, 
TODO: explain how we handle the fact that tracelinks are undirected in ArDoCo but neo4j only allows directed relationships.



The image below shows an example of how tracelinks are represented in the graph.
![Tracelinks.png](Tracelinks.png)

