# JSON for text providers

In this repository, we present a definition for transferring information about a preprocessed text via JSON.


## JSON-Schema
Below, you can find a simple presentation of the JSON-schema for the definition of a text for ArDoCo. Basically, a text consists of sentences and sentences consist of words along with the constituency tree. Words contain information about themselves and the relations (dependencies) to other words.

The interfaces of ArDoCo require more, but the rest should be recoverable/calculateable from these entities with their data fields.

An example JSON-file that validates against the schema can be found in [examples/example-text.json](./examples/example-text.json).

### Text
- sentences: [Sentence]

### Sentence
- sentenceNo: integer
- constituencyTree: string
- text: string
- words: [Word]

### Word
- sentenceNo: integer
- id: integer
- text: string
- lemma: string
- posTag: posTag
- outgoingDependencies: [{dependencyType, TargetWordId}]
- incomingDependencies: [{dependencyType, TargetWordId}]

### posTags
```json
enum: ["JJ", "JJR", "JJS", "RB", "RBR", "RBS", "WRB", "CC", "IN", "CD", "DT", "WDT", "EX", "FW", "LS", "NN", "NNS", "NNP", "NNPS", "PDT", "POS", "PRP", "PRP$", "WP$", "WP", "RP", "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "MD", ".", ",", ":", "-LRB-", "-RRB-", "-NONE-", "``", "''", "$", "#"]
```

### dependencyTypes
```json
enum: ["APPOS", "NSUBJ", "POSS", "OBJ", "IOBJ", "NMOD", "NSUBJPASS", "POBJ", "AGENT", "NUM", "PREDET", "RCMOD", "CSUBJ", "CCOMP", "XCOMP", "OBL", "VOCATIVE", "EXPL", "DISLOCATED", "ADVCL", "ADVMOD", "DISCOURSE", "AUXILIARY", "COP", "MARK", "ACL", "AMOD", "DET", "CLF", "CASE", "CONJ", "CC", "FIXED", "FLAT", "COMPOUND", "LIST", "PARATAXIS", "ORPHAN", "GOES_WITH", "REPARANDUM", "PUNCT", "CSUBJ_PASS", "ACL_RELCL", "COMPOUND_PRT", "NMOD_POSS", "REF", "NSUBJ_XSUBJ", "NSUBJ_PASS_XSUBJ", "NSUBJ_RELSUBJ", "NSUBJ_PASS_RELSUBJ", "OBJ_RELOBJ"]
```

## Maven

```xml
<dependencies>
	<dependency>
		<groupId>io.github.ardoco.core</groupId>
		<artifactId>text-provider-json</artifactId>
		<version>YOUR_VERSION_HERE</version>
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
