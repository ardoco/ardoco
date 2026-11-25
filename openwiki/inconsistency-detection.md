---
type: "Reference"
title: "Inconsistency Detection"
description: "Inconsistency detection module for ARDoCo: classic TEAM/MEAT detection between SAD and SAM via InconsistencyChecker, Artemis-based per-strategy detection, the LLM-validated programming-language and architectural-style-pattern checkers, their state models, the four pipeline-id runners, and pre-filter configuration."
tags: [inconsistency-detection, team, meat, llm, pipeline, evaluation]
openwiki:
  roles: [domain, architecture, workflow, testing]
  change_kinds: [lifecycle]
  source_paths: [inconsistency-detection/pipeline-id/src/main/java, inconsistency-detection/stages-id/inconsistency-detection/src/main/java, inconsistency-detection/stages-id/artemis-inconsistency-detection/src/main/java, inconsistency-detection/stages-id/programming-language-inconsistency-detection/src/main/java, inconsistency-detection/stages-id/architectural-style-pattern-inconsistency-detection/src/main/java]
  symbols: [InconsistencyChecker, TextEntityAbsentFromModelInconsistencyAgent, ModelEntityAbsentFromTextInconsistencyAgent, InconsistencyStatesImpl, InconsistencyStateImpl, ArtemisInconsistencyChecker, ArtemisInconsistencyAgent, ProgrammingLanguageInconsistencyChecker, ArchitecturalStylePatternInconsistencyChecker]
  invariants: ["InconsistencyChecker runs after the SWATTR connection pipeline", "TEAM flags text entities with no trace link; MEAT flags model entities with insufficient trace links", "InconsistencyStates is keyed by Metamodel, ArtemisInconsistencyStates by ArtemisTarget, and the LLM-validated stages hold a single state per run", "LLM validation agents run uncached with read-only tools and bounded retries"]
  test_paths: [inconsistency-detection/stages-id/inconsistency-detection/src/test/java, inconsistency-detection/tests-inconsistency/src/test/java]
  validation_commands: ["mvn -pl inconsistency-detection clean verify"]
sources:
  - id: openwiki-source-058fa35a53fcea51698d015a
    resource: repo://core/framework/common/src/main/java/edu/kit/kastel/mcse/ardoco/core/api/stage/connectiongenerator/artemis/ArtemisTarget.java
  - id: openwiki-source-acf713794fda9144cc3a7948
    resource: repo://core/framework/common/src/main/java/edu/kit/kastel/mcse/ardoco/core/api/stage/inconsistency/AbstractInconsistencyState.java
  - id: openwiki-source-a8e9bdea6df0265b48d802e2
    resource: repo://core/framework/common/src/main/java/edu/kit/kastel/mcse/ardoco/core/api/stage/inconsistency/ArtemisInconsistencyStates.java
  - id: openwiki-source-e0774eaab1b506ff2becb24c
    resource: repo://core/framework/common/src/main/java/edu/kit/kastel/mcse/ardoco/core/api/stage/inconsistency/BaseInconsistencyState.java
  - id: openwiki-source-72aad5dc7092822341541825
    resource: repo://core/framework/common/src/main/java/edu/kit/kastel/mcse/ardoco/core/common/readonlytools/CodeDirectoryReadOnlyTools.java
  - id: openwiki-source-5594c4f4a4aa3285141f28cc
    resource: repo://core/framework/common/src/main/java/edu/kit/kastel/mcse/ardoco/core/common/readonlytools/CodeModelReadOnlyTools.java
  - id: openwiki-source-47920583ad38b6d3aa8a630c
    resource: repo://core/pipeline-core/src/main/java/edu/kit/kastel/mcse/ardoco/core/api/output/ArdocoResult.java
  - id: openwiki-source-e47e2e073032a4943d447b9a
    resource: repo://core/pipeline-core/src/main/java/edu/kit/kastel/mcse/ardoco/core/common/util/FilePrinter.java
  - id: openwiki-source-8461f9d3c6ce6ece9e1ae7ae
    resource: repo://core/pipeline-core/src/main/java/edu/kit/kastel/mcse/ardoco/core/execution/Ardoco.java
  - id: openwiki-source-b0f594a7d3e6330bcef64b54
    resource: repo://core/tests-base/pom.xml
  - id: openwiki-source-2716f8b0e6cb2c55d3124163
    resource: repo://inconsistency-detection/pipeline-id/pom.xml
  - id: openwiki-source-175735bbae2438702256767b
    resource: repo://inconsistency-detection/pipeline-id/src/main/java/edu/kit/kastel/mcse/ardoco/id/execution/runner/ArchitecturalStylePatternInconsistencyDetection.java
  - id: openwiki-source-febe4429135101c0f952e2c2
    resource: repo://inconsistency-detection/pipeline-id/src/main/java/edu/kit/kastel/mcse/ardoco/id/execution/runner/ArtemisInconsistencyDetection.java
  - id: openwiki-source-032eecef581f79839de83eb9
    resource: repo://inconsistency-detection/pipeline-id/src/main/java/edu/kit/kastel/mcse/ardoco/id/execution/runner/InconsistencyDetection.java
  - id: openwiki-source-f4cfb62c5a18a88ca09b883c
    resource: repo://inconsistency-detection/pipeline-id/src/main/java/edu/kit/kastel/mcse/ardoco/id/execution/runner/ProgrammingLanguageInconsistencyDetection.java
  - id: openwiki-source-b40e29eb69418d1b82bc9a2b
    resource: repo://inconsistency-detection/pom.xml
  - id: openwiki-source-c6a18db4c8399f6baa034a4c
    resource: repo://inconsistency-detection/stages-id/architectural-style-pattern-inconsistency-detection/pom.xml
  - id: openwiki-source-0e816b3d7c20b1ab4976bb3f
    resource: repo://inconsistency-detection/stages-id/architectural-style-pattern-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/ArchitecturalStylePatternInconsistencyChecker.java
  - id: openwiki-source-7000d585453bc20767937df4
    resource: repo://inconsistency-detection/stages-id/architectural-style-pattern-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/ArchitecturalStylePatternNerStrategy.java
  - id: openwiki-source-2a6c56985f74e984d0bc5434
    resource: repo://inconsistency-detection/stages-id/architectural-style-pattern-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/AbstractArchitecturalStylePatternInconsistencyInformant.java
  - id: openwiki-source-c763af94ec814b59d1d49ac5
    resource: repo://inconsistency-detection/stages-id/architectural-style-pattern-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/CodeDirectoryArchitecturalStylePatternInconsistencyInformant.java
  - id: openwiki-source-ca1a2e7f9bea12767db44edd
    resource: repo://inconsistency-detection/stages-id/architectural-style-pattern-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/CodeModelArchitecturalStylePatternInconsistencyInformant.java
  - id: openwiki-source-4d2aa02ef37e07a4e825f7ee
    resource: repo://inconsistency-detection/stages-id/architectural-style-pattern-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/llmagents/ArchitecturalStylePatternValidationLlmAgent.java
  - id: openwiki-source-6fbe76fa05aa501e1ca87741
    resource: repo://inconsistency-detection/stages-id/architectural-style-pattern-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/validators/ArchitecturalStylePatternStatementValidatorRegistry.java
  - id: openwiki-source-7fd70ef6e6c389e93e42bc02
    resource: repo://inconsistency-detection/stages-id/architectural-style-pattern-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/validators/ArchitecturalStylePatternValidationContext.java
  - id: openwiki-source-856e9fde49db88fe39c9e2e4
    resource: repo://inconsistency-detection/stages-id/architectural-style-pattern-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/states/ArchitecturalStylePatternInconsistencyStateImpl.java
  - id: openwiki-source-4055eec9684b0ba52c85c7a5
    resource: repo://inconsistency-detection/stages-id/architectural-style-pattern-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/states/ArchitecturalStylePatternStatement.java
  - id: openwiki-source-4120dd77c012bad785ece592
    resource: repo://inconsistency-detection/stages-id/artemis-inconsistency-detection/pom.xml
  - id: openwiki-source-95f102bbbec3b57083f1caaf
    resource: repo://inconsistency-detection/stages-id/artemis-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/agents/ArtemisInconsistencyAgent.java
  - id: openwiki-source-b1931eb454663e6ef979f5a5
    resource: repo://inconsistency-detection/stages-id/artemis-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/ArtemisInconsistencyChecker.java
  - id: openwiki-source-7f6d2b7f3ceba15a50dce298
    resource: repo://inconsistency-detection/stages-id/artemis-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/ArtemisInconsistencyInformant.java
  - id: openwiki-source-71717fa89f65caa5782aea20
    resource: repo://inconsistency-detection/stages-id/artemis-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/ArtemisMeatInconsistencyInformant.java
  - id: openwiki-source-0546793c8db53db262e44388
    resource: repo://inconsistency-detection/stages-id/artemis-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/ArtemisTeamInconsistencyInformant.java
  - id: openwiki-source-39fc9dbac53912585f8d5e7a
    resource: repo://inconsistency-detection/stages-id/artemis-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/states/ArtemisInconsistencyStatesImpl.java
  - id: openwiki-source-f3058c048dde164d331e0907
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/agents/InitialInconsistencyAgent.java
  - id: openwiki-source-6923b72dec5419ea030b5b50
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/InconsistencyChecker.java
  - id: openwiki-source-cd379e1a2352c619ec921459
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/InconsistencyStateImpl.java
  - id: openwiki-source-fe74e32e8a96b56671af4718
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/InconsistencyStatesImpl.java
  - id: openwiki-source-3b46c94a3d434218c3078608
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/Filter.java
  - id: openwiki-source-949fd9fa06dbeb62a72b5729
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/ModelEntityAbsentFromTextInconsistencyInformant.java
  - id: openwiki-source-306372671dd725b88b5aa392
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/OccasionFilter.java
  - id: openwiki-source-e610c02a25f4602f0e3eef73
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/RecommendedInstanceProbabilityFilter.java
  - id: openwiki-source-0f6f95a33e06e306606eaaa2
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/TextEntityAbsentFromModelInconsistencyInformant.java
  - id: openwiki-source-483673b6a94018597ec42ec1
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/UnwantedWordsFilter.java
  - id: openwiki-source-72ddfadae08a3338c19f27ce
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/MissingElementInconsistencyCandidate.java
  - id: openwiki-source-a50447bf270088ff1adb28c4
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/MissingElementSupport.java
  - id: openwiki-source-4a4b148ae8eae68306b60732
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/types/ModelEntityAbsentFromTextInconsistency.java
  - id: openwiki-source-e765fdd0f68f17c25394247e
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/types/TextEntityAbsentFromModelInconsistency.java
  - id: openwiki-source-16474d78d6a2d91a07302dfe
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/test/java/edu/kit/kastel/mcse/ardoco/id/agents/ModelEntityAbsentFromTextInconsistencyTest.java
  - id: openwiki-source-dda6a8955474a7938892d976
    resource: repo://inconsistency-detection/stages-id/inconsistency-detection/src/test/java/edu/kit/kastel/mcse/ardoco/id/types/AbstractInconsistencyTypeTest.java
  - id: openwiki-source-17e801a0d4e1b83de6d7102e
    resource: repo://inconsistency-detection/stages-id/pom.xml
  - id: openwiki-source-8da0a5e2293145624dd67247
    resource: repo://inconsistency-detection/stages-id/programming-language-inconsistency-detection/pom.xml
  - id: openwiki-source-af23797106a860b4f63601d3
    resource: repo://inconsistency-detection/stages-id/programming-language-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/AbstractProgrammingLanguageInconsistencyInformant.java
  - id: openwiki-source-63f4272969e8405bf685d763
    resource: repo://inconsistency-detection/stages-id/programming-language-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/CodeDirectoryProgrammingLanguageInconsistencyInformant.java
  - id: openwiki-source-945aa30ab4cfbe289305fd43
    resource: repo://inconsistency-detection/stages-id/programming-language-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/CodeModelProgrammingLanguageInconsistencyInformant.java
  - id: openwiki-source-057086d55b53dfe114866060
    resource: repo://inconsistency-detection/stages-id/programming-language-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/informants/llmagents/ProgrammingLanguageValidationLlmAgent.java
  - id: openwiki-source-548e74f249ba7cbde391737c
    resource: repo://inconsistency-detection/stages-id/programming-language-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/ProgrammingLanguageInconsistencyChecker.java
  - id: openwiki-source-e1153af376afd79d9781ec38
    resource: repo://inconsistency-detection/stages-id/programming-language-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/ProgrammingLanguageNerStrategy.java
  - id: openwiki-source-39cc2271c9cdbfcb68915d06
    resource: repo://inconsistency-detection/stages-id/programming-language-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/states/ProgrammingLanguageInconsistencyState.java
  - id: openwiki-source-5a7abd2ed5f765245be47ab4
    resource: repo://inconsistency-detection/stages-id/programming-language-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/states/ProgrammingLanguageInconsistencyStateImpl.java
  - id: openwiki-source-34f0b55d15259152c89b0864
    resource: repo://inconsistency-detection/stages-id/programming-language-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/states/ProgrammingLanguageStatement.java
  - id: openwiki-source-b1947d838c05c65e6ca3f79d
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/eval/baseline/InconsistencyBaselineInformant.java
  - id: openwiki-source-c7af0c692f4fed88d777cca5
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/ArchitecturalStylePatternInconsistencyDetectionEvaluationIT.java
  - id: openwiki-source-90195298ac80c7ce64285239
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/InconsistencyDetectionEvaluationArtemisIT.java
  - id: openwiki-source-edf5c30c4e5b71b28c079cd1
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/InconsistencyDetectionEvaluationIT.java
  - id: openwiki-source-58130a68841df028da063129
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/ArchitecturalStylePatternInconsistencyDetectionEvaluation.java
  - id: openwiki-source-583c83f4725d40df783cdad0
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/artemis/ArtemisInconsistencyEvaluationWriter.java
  - id: openwiki-source-8c04f77db28d033d3ccefa28
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/artemis/evaluators/ArtemisInconsistencyEvaluator.java
  - id: openwiki-source-d7821439799d95eae67e3c3f
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/artemis/evaluators/ComponentArtemisInconsistencyEvaluator.java
  - id: openwiki-source-0d4edadeea54c0317699ad03
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/artemis/HoldbackClassArtemisConnectionInformant.java
  - id: openwiki-source-da67a2c4e771b9f183f2f547
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/artemis/HoldBackComponentArtemisNerStrategy.java
  - id: openwiki-source-828b66799673fc11bba23bb8
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/artemis/runproducer/ArtemisInconsistencyRunProducer.java
  - id: openwiki-source-37c52c86ac883ae23eab08b2
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/artemis/runproducer/ArtemisInconsistencyRunSupport.java
  - id: openwiki-source-e94ed452e30a2c4b7a07e54d
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/artemis/runproducer/ClassHoldBackArtemisInconsistencyRunProducer.java
  - id: openwiki-source-eba775d639d92ca504e556b2
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/artemis/runproducer/ComponentHoldBackArtemisInconsistencyRunProducer.java
  - id: openwiki-source-09524e676dc56b59d49796a2
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/artemis/runproducer/DatafileExtendedArtemisInconsistencyRunProducer.java
  - id: openwiki-source-cf52a54a58ac65b3376cf427
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/artemis/runproducer/SingleArtemisInconsistencyRunProducer.java
  - id: openwiki-source-9bcce459849159fe4496bbfd
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/HoldBackArCoTLModelProvider.java
  - id: openwiki-source-fd8ac5724feb762c433689e1
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/HoldBackRunResultsProducer.java
  - id: openwiki-source-af363b099a3f39a1a9f8fceb
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/InconsistencyDetectionEvaluationUtil.java
  - id: openwiki-source-c19a94607b86bb35bc32ad97
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/inconsistencyhelper/ProgrammingLanguageInconsistencyDetectionEvaluation.java
  - id: openwiki-source-9c6ae28db6be1ea5b6623315
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/integration/ProgrammingLanguageInconsistencyDetectionEvaluationIT.java
  - id: openwiki-source-678e3d567fe88b6ac0ebebd5
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/tasks/ArchitecturalStylePatternInconsistencyDetectionTask.java
  - id: openwiki-source-f45db2c21812815ccc201cde
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/tasks/ClassArtemisInconsistencyTask.java
  - id: openwiki-source-326646621e88912b5aa7cd3d
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/tasks/ComponentArtemisInconsistencyTask.java
  - id: openwiki-source-5e63262211c4cb2876f30cd2
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/tasks/DatafileArtemisInconsistencyTask.java
  - id: openwiki-source-c9530f5e893d970e1910ab0c
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/tasks/InconsistencyDetectionTask.java
  - id: openwiki-source-7cf39731aa5b88c863aaf959
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/java/edu/kit/kastel/mcse/ardoco/id/tests/tasks/ProgrammingLanguageInconsistencyDetectionTask.java
  - id: openwiki-source-071e631b9ac4019ab44c2254
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/resources/architectural-style-pattern-text-extensions/README.md
  - id: openwiki-source-31a552bfef467ce39212f914
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/resources/datafile-artemis-text-extensions/README.md
  - id: openwiki-source-e73a2b2601ba11f8dc80c66e
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/resources/datafile-artemis-text-extensions/rod/extension-0.txt
  - id: openwiki-source-1deeceb078aa5673b4f247c9
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/resources/datafile-artemis-text-extensions/rod/goldstandard-extension-0.csv
  - id: openwiki-source-282eaa26c0df2cde9569bb7f
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/resources/programming-language-text-extensions/README.md
  - id: openwiki-source-e3fd1b01ca4360aeba2c46e0
    resource: repo://inconsistency-detection/tests-inconsistency/src/test/resources/programming-language-text-extensions/rod/goldstandard-extension-0.csv
  - id: openwiki-source-c3324edd02fb573e63e5301b
    resource: repo://tlr/pipeline-tlr/src/main/java/edu/kit/kastel/mcse/ardoco/tlr/execution/Swattr.java
  - id: openwiki-source-3db4b953fa4c9dbe8b88791b
    resource: repo://tlr/stages-tlr/artemis/src/main/java/edu/kit/kastel/mcse/ardoco/tlr/artemis/strategies/ArtemisNerStrategy.java
  - id: openwiki-source-02915942e497a9802282dcbf
    resource: repo://tlr/stages-tlr/model-provider/src/main/java/edu/kit/kastel/mcse/ardoco/tlr/models/agents/ArchitectureConfiguration.java
  - id: openwiki-source-635f754fd3f1d8825c4386b9
    resource: repo://tlr/tests-tlr/src/test/resources/datafile-artemis-text-extensions/rod/extension-0.txt
generated: { by: "openwiki/0.9.0", at: "2026-11-23T16:40:38.555Z" }
verified:
  - by: openwiki/0.9.0
    at: 2026-11-23T16:40:38.555Z
---

# Inconsistency Detection

ARDoCo's Inconsistency Detection (ID) identifies missing or deviating elements (**inconsistencies**) between artifacts such as software architecture documentation (SAD), the software architecture model (SAM), and code. The classic stage reuses trace link recovery results as a bridge: once trace links connect documentation sentences to model elements, ID flags the "orphan" elements on both sides of that bridge. The implementation lives in the `inconsistency-detection` module tree (`io.github.ardoco.id`); the classic stage is appended to the SWATTR pipeline, and three newer LLM-validated stages build on the ArTEMiS named-entity-recognition pipeline.

> **Terminology note**: In ARDoCo V1 the two SAD/SAM inconsistency kinds were called "Missing Model Elements" (MME) and "Unmentioned/Undocumented Model Elements" (UME). V2 renamed them to Text Entity Absent from Model (TEAM) and Model Entity Absent from Text (MEAT). The V1 names survive in file names, e.g., the `*_UME.csv` gold standards used by the evaluation.

## Module Layout

The `inconsistency-detection` tree (parent POM `io.github.ardoco.id:parent-id`) contains three Maven aggregators:

| Aggregator | Contents |
|------------|----------|
| `pipeline-id` | The four executable runners (`InconsistencyDetection`, `ArtemisInconsistencyDetection`, `ProgrammingLanguageInconsistencyDetection`, `ArchitecturalStylePatternInconsistencyDetection`); depends on all four stage modules plus the TLR `artemis`, `connection-generator`, `model-provider`, `recommendation-generator`, and `text-preprocessing` stages |
| `stages-id` | The stage modules `artemis-inconsistency-detection`, `inconsistency-detection`, and `programming-language-inconsistency-detection` |
| `tests-inconsistency` | Evaluation ITs, the hold-back harness, and the baseline stage (only built under the default `complete` profile) |

The fourth stage, `stages-id/architectural-style-pattern-inconsistency-detection`, is mounted **directly** in the parent POM's module list rather than in `stages-id`, so it is built alongside the other three stage modules.

The `artemis-inconsistency-detection` stage depends on the TLR `artemis` stage (NER strategies and connection states) and on the classic `inconsistency-detection` stage, whose inconsistency type records it reuses. The two LLM-validated stages depend on the TLR `artemis` stage and core `common`.

## The Four Pipeline-Id Runners

Each runner extends `ArdocoRunner`, defines its pipeline in `definePipeline`, rejects blank input text with an `IllegalArgumentException`, and puts the raw text into the `DataRepository` itself via `DataRepositoryHelper.putInputText` (no separate preprocessing pipeline step). The runner contract (`isSetUp` flag, `run()` vs. `runWithoutSaving()`) is documented on [Architecture](architecture.md).

| Runner | Composed stages |
|--------|-----------------|
| `InconsistencyDetection` | `TextPreprocessingAgent` → `ModelProviderAgent` → `TextExtraction` → `RecommendationGenerator` → `ConnectionGenerator` → `InconsistencyChecker` |
| `ArtemisInconsistencyDetection` | `ArtemisPreprocessing` → `ArtemisNer` → `ArtemisConnectionGenerator` → `ArtemisInconsistencyChecker` |
| `ProgrammingLanguageInconsistencyDetection` | `SimpleTextPreprocessingAgent` → `ArtemisNer` (single `ProgrammingLanguageNerStrategy`) → `ModelProviderAgent` + `ProgrammingLanguageInconsistencyChecker.getForCodeModel` (ACM_FILE) or `ProgrammingLanguageInconsistencyChecker.getForCodeDirectory` (DIRECTORY) |
| `ArchitecturalStylePatternInconsistencyDetection` | `SimpleTextPreprocessingAgent` → `ArtemisNer` (single `ArchitecturalStylePatternNerStrategy`) → `ModelProviderAgent` + `ArchitecturalStylePatternInconsistencyChecker.getForCodeModel` (ACM_FILE) or `ArchitecturalStylePatternInconsistencyChecker.getForCodeDirectory` (DIRECTORY) |

### Runner: InconsistencyDetection (classic SAD/SAM)

The end-to-end runner is `InconsistencyDetection` (`inconsistency-detection/pipeline-id/.../execution/runner/InconsistencyDetection.java`), an `ArdocoRunner` whose `setUp` calls `definePipeline`. This is the SWATTR trace link recovery pipeline (see [TLR Approaches](tlr-approaches.md)) with the `InconsistencyChecker` stage appended. Three details matter:

- The runner pins the architecture `Metamodel` to `ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES` by constructing the `ArchitectureConfiguration` directly with that metamodel and passing it to `ModelProviderAgent.getModelProviderAgent(...)` (unlike the TLR runners, whose `setUp` forbids a pre-set metamodel with an `IllegalArgumentException` and then calls `withMetamodel(...)` itself; see [TLR Approaches](tlr-approaches.md)). Note the contrast with the SWATTR runner, which pins `ARCHITECTURE_WITH_COMPONENTS`.
- Blank input text is rejected up front with an `IllegalArgumentException`.
- `definePipeline` puts the input text into the `DataRepository` itself rather than adding a separate preprocessing pipeline step.

Because the informants read `ConnectionStates`, `ModelStates`, and `InconsistencyStates` from the shared `DataRepository`, the `InconsistencyChecker` only produces results when the upstream stages have already run.

### Runner: ArtemisInconsistencyDetection

`ArtemisInconsistencyDetection.setUp` takes an input text, an optional `ArchitectureConfiguration`, an optional `CodeConfiguration`, additional configs, an output directory, a `LargeLanguageModel` for NER, and a list of `ArtemisNerStrategy` objects. It:

- **rejects pre-set metamodels**: if either configuration already carries a metamodel, `setUp` throws `IllegalArgumentException` — "Metamodel shall not be set in configurations. The runner defines the metamodels." It then pins the architecture configuration to `Metamodel.ARCHITECTURE_WITH_COMPONENTS` and the code configuration to `Metamodel.CODE_WITH_COMPILATION_UNITS` via `withMetamodel(...)`.
- composes `ArtemisPreprocessing.get(...)` (a `SimpleTextPreprocessingAgent` plus a `ModelProviderAgent` for the optional models), `ArtemisNer.get(...)` (one LLM NER agent per strategy), `ArtemisConnectionGenerator.get(...)`, and `ArtemisInconsistencyChecker.get(...)`.

The NER and connection-generation stages themselves are part of the ArTEMiS approach and are documented on [TLR Approaches](tlr-approaches.md); the model-selection and prompt mechanics they rely on are documented on [LLM Integration](llm-integration.md).

### Runners: ProgrammingLanguageInconsistencyDetection / ArchitecturalStylePatternInconsistencyDetection

Both runners share the same shape: `setUp` takes an input text, a mandatory `CodeConfiguration`, additional configs, an optional output directory, a `LargeLanguageModel` for NER (`llmForNer`), and a second `LargeLanguageModel` used by the validation agent (`validationAgentLlm`). `definePipeline` adds `SimpleTextPreprocessingAgent`, then `ArtemisNer` with exactly one NER strategy (the stage's own), and then dispatches on `CodeConfiguration.type()`:

- `ACM_FILE`: a `ModelProviderAgent` for `Metamodel.CODE_WITH_COMPILATION_UNITS` plus the checker's `getForCodeModel` variant;
- `DIRECTORY`: the checker's `getForCodeDirectory` variant with the code directory path;
- anything else: `IllegalArgumentException`.

## Classic TEAM/MEAT: The InconsistencyChecker Stage

**Source**: `inconsistency-detection/stages-id/inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/InconsistencyChecker.java`

`InconsistencyChecker` extends `AbstractExecutionStage` and constructs exactly three agents in this order:

1. `InitialInconsistencyAgent`
2. `TextEntityAbsentFromModelInconsistencyAgent` (TEAM)
3. `ModelEntityAbsentFromTextInconsistencyAgent` (MEAT)

Its `initializeState()` builds an `InconsistencyStatesImpl` and registers it in the `DataRepository` under `InconsistencyStates.ID` (`"InconsistencyStates"`); this happens in `preparePipelineSteps` before any agent runs. The static factory `InconsistencyChecker.get(additionalConfigs, dataRepository)` applies configuration before execution. As with every stage, agents can be switched off via the stage's `enabledAgents` configuration (see [Architecture](architecture.md)).

### Inconsistency Types

#### TEAM — Text Entity Absent from Model

Elements described in the documentation that cannot be traced to any model element.

- **Agent**: `TextEntityAbsentFromModelInconsistencyAgent`
- **Informant**: `TextEntityAbsentFromModelInconsistencyInformant`
- **Type class**: `TextEntityAbsentFromModelInconsistency` — a record `(name, sentence, confidence, origin)` implementing `TextInconsistency`
- **Type string**: `"TextEntityAbsentFromModel"`

**Detection logic** (`minSupport` configurable, default `1`):

1. Read the recommended instances that `InitialInconsistencyAgent` copied into the inconsistency state (already reduced by the pre-filters), for each metamodel of `ModelStates`.
2. Remove every recommended instance that participates in a trace link, i.e., that appears as the first endpoint of an instance link in `ConnectionState.getInstanceLinks()`.
3. Additionally remove candidates whose words overlap with any linked recommended instance (they describe the same concept that is already linked).
4. Remaining candidates become `MissingElementInconsistencyCandidate`s with `ELEMENT_WITH_NO_TRACE_LINK` support; candidates that overlap each other accumulate `MULTIPLE_OVERLAPPING_RECOMMENDED_INSTANCES` support.
5. Candidates whose accumulated support is at least `minSupport` produce one `TextEntityAbsentFromModelInconsistency` **per distinct word** of the recommended instance, recording the word's 1-based sentence number (`getSentenceNumber() + 1`) and the recommended instance's probability as confidence.

#### MEAT — Model Entity Absent from Text

Model elements that have no (or insufficient) corresponding documentation.

- **Agent**: `ModelEntityAbsentFromTextInconsistencyAgent`
- **Informant**: `ModelEntityAbsentFromTextInconsistencyInformant`
- **Type class**: `ModelEntityAbsentFromTextInconsistency` (implements `ModelInconsistency`, wraps the flagged `ModelEntity`)
- **Type string**: `"ModelEntityAbsentFromText"`

**Detection logic**:

1. Collect the distinct model entities that appear as second endpoints of instance links, per metamodel of `ModelStates`.
2. Walk all `Model.getEndpoints()` and keep entities of a targeted type whose link count is below `minimumNeededTraceLinks` (default `1`).
3. Remove whitelist matches: a whitelist entry is a regex that must **fully match** the entity name or any of its name parts; matching entities are excluded (the helper is `static` and directly unit-tested).
4. Each remaining candidate becomes one `ModelEntityAbsentFromTextInconsistency`.

**Configuration options**:

| Option | Default | Meaning |
|--------|---------|---------|
| `minimumNeededTraceLinks` | `1` | Minimum number of trace links to a model entity required to avoid being flagged |
| `whitelist` | `[]` | Regex patterns (full match on name or name parts) for entities to exclude |
| `types` | `["Component", "BasicComponent", "CompositeComponent"]` | Model entity types (or type parts) to check |

### Detection Flow

```mermaid
flowchart TD
    RS["RecommendationState recommended instances"] --> COPY["InitialInconsistencyAgent copies them into the InconsistencyState"]
    COPY --> F1["RecommendedInstanceProbabilityFilter"]
    F1 --> F2["OccasionFilter"]
    F2 --> F3["UnwantedWordsFilter"]
    F3 --> TEAM["TextEntityAbsentFromModelInconsistencyAgent"]
    F3 -.->|"runs after"| MEAT["ModelEntityAbsentFromTextInconsistencyAgent"]
    CS["ConnectionState instance links"] --> TEAM
    CS --> MEAT
    MS["ModelStates model endpoints"] --> MEAT
    TEAM --> TI["TextEntityAbsentFromModelInconsistency"]
    MEAT --> MI["ModelEntityAbsentFromTextInconsistency"]
    TI --> ST[("InconsistencyState per Metamodel")]
    MI --> ST
```

*Agent order and data flow in the `InconsistencyChecker`: recommended instances are pre-filtered by three informants before the TEAM agent derives text inconsistencies from them; MEAT reads model endpoints and instance links directly. Both agents write into the per-metamodel inconsistency state.*

Within the stage, the TEAM and MEAT informants each iterate the metamodels of `ModelStates` and write into the per-metamodel `InconsistencyState`; MEAT works directly from `Model.getEndpoints()` and `ConnectionState` instance links and consumes no output of `InitialInconsistencyAgent`, whereas TEAM consumes the recommended instances that `InitialInconsistencyAgent` copied and pre-filtered.

### Agent 1: InitialInconsistencyAgent (Pre-filtering)

`initializeState()` copies the recommended instances from the per-metamodel `RecommendationState` into the matching per-metamodel `InconsistencyState`. The agent then runs three filter informants. All of them extend the `Filter` base class, an `Informant` whose `process()` iterates the metamodels derived from `ModelStates`, resolves each metamodel's inconsistency state, and rewrites that state's recommended instances by calling the abstract `filterRecommendedInstances(InconsistencyState)`.

| Informant | Purpose | Key defaults |
|-----------|---------|--------------|
| `RecommendedInstanceProbabilityFilter` | Drops RIs with low probability | `threshold=0.5`, `dynamicThreshold=true`, `dynamicThresholdFactor=0.7`, `thresholdNameAndTypeProbability=0.3`, `thresholdNameOrTypeProbability=0.8` |
| `OccasionFilter` | Drops RIs that do not recur | `expectedAppearances=2` |
| `UnwantedWordsFilter` | Drops RIs with unwanted words | `enableCommonBlacklist=true`, custom/common word lists, common file endings |

**RecommendedInstanceProbabilityFilter**: with `dynamicThreshold=true`, the threshold is recomputed per run as `dynamicThresholdFactor × highest RI probability` (calculated over the RIs the state currently holds). An RI survives if its probability exceeds the threshold **and** `checkProbabilitiesForNounMappingTypes` passes one of the three gates: highest type and highest name probability both above `thresholdNameAndTypeProbability`, or the type probability above `thresholdNameOrTypeProbability`, or the name probability above `thresholdNameOrTypeProbability`. Implementation quirk: the "name" probability is computed via `getHighestNameProbability(getTypeMappings())` rather than from the name mappings, so with the default thresholds all three gates collapse to a single test of the highest type-mapping probability against `thresholdNameAndTypeProbability`; real name mappings are never consulted and an RI without any type mapping is always dropped.

**OccasionFilter**: counts how many *distinct text positions* the RI's name-mapping words occupy (consecutive word positions count as one position); the RI survives only if the count is at least `expectedAppearances`.

**UnwantedWordsFilter**: removes an RI when any name mapping's reference trips one of four heuristics:

- the **lemma** of any reference word is on the blacklist — the configurable `customBlacklist` (default `meta`, `log`, `browser`, `task`, `operation`, `case`, `instance`, `script`) or, when `enableCommonBlacklist=true`, the common blacklist loaded once from the classpath resource `unwanted_words_filter_common.json` (`cpu`, `gpu`, `file`, `directory`, `event`, `bus`, `browser`, `chrome`, `firefox`, `safari`, `edge`, `instance`, `object`, `module`, `code`, `java`, `javascript`, `nodejs`, `npm`, `kotlin`, `request`, `response`, `servlet`, `unit`, `test`);
- any reference word's POS tag name contains `plural`;
- the lowercased reference **consists only of numbers** (full match of the pattern `-?\d+(\.\d+)?`);
- any reference word's text **ends with a common file ending** — a word like `model.pdf` matches the configurable `commonFileEndings` list (default `pdf`, `png`, `md`, `xml`, `yml`, `json`, `html`, `sh`, `bat`, `java`, `gradle`, `cpp`, `c`, `h`, `groovy`, `js`, `ts`, `css`, `sc`, `scala`).

Surviving RIs are written back with `setRecommendedInstances`. The source notes this filter is a prototype; the comment lists improvement ideas such as using computer-science words, lowering probabilities instead of removing high-probability RIs, considering phrases, and checking whether the word looks like a named entity.

### Agents 2 and 3

- `TextEntityAbsentFromModelInconsistencyAgent` is a thin `PipelineAgent` wrapper around `TextEntityAbsentFromModelInconsistencyInformant`, which implements the five-step TEAM logic described above. Its only `@Configurable` is `minSupport` (default `1`).
- `ModelEntityAbsentFromTextInconsistencyAgent` is a thin `PipelineAgent` wrapper around `ModelEntityAbsentFromTextInconsistencyInformant`, which implements the MEAT logic and owns the three `@Configurable` options listed in the table above. Type targeting checks the entity's `type` and `typeParts` against the configured `types` list, so `"Component"` also matches a `BasicComponent`.

## State and Candidate Model

| Class | Role |
|-------|------|
| `InconsistencyStatesImpl` | `EnumMap<Metamodel, InconsistencyStateImpl>` container; `build()` creates one state per `Metamodel` value |
| `InconsistencyStateImpl` | `AbstractState` holding `recommendedInstances` and `inconsistencies` per metamodel |
| `MissingElementInconsistencyCandidate` | Wraps a `RecommendedInstance` plus the set of accumulated `MissingElementSupport`s |
| `MissingElementSupport` | Enum: `ELEMENT_WITH_NO_TRACE_LINK`, `TRACED_ELEMENT_IN_SAME_SENTENCE`, `DEPENDENCY_TO_TRACED_ELEMENT`, `MULTIPLE_OVERLAPPING_RECOMMENDED_INSTANCES` |

Behavioral notes:

- `InconsistencyStateImpl.addInconsistency` (inherited from `AbstractInconsistencyState`) skips duplicates (equality per inconsistency type), so repeated word-level TEAM records with identical name/sentence/confidence collapse; `setRecommendedInstances` replaces the stored list in place.
- The pre-filters do not just remove entries; they **replace** the state's recommended instances via `setRecommendedInstances`, which is why filter order (probability → occasion → unwanted words) defines a narrowing funnel.
- `MissingElementInconsistencyCandidate.getAmountOfSupport()` is the number of *distinct* support kinds; `equals`/`hashCode` are keyed only by the wrapped `RecommendedInstance`.
- Of the four support kinds, only `ELEMENT_WITH_NO_TRACE_LINK` and `MULTIPLE_OVERLAPPING_RECOMMENDED_INSTANCES` are currently produced; `TRACED_ELEMENT_IN_SAME_SENTENCE` and `DEPENDENCY_TO_TRACED_ELEMENT` are declared for future heuristics.

### Shared State API vs. Per-Stage States

The shared interfaces and the common base implementation live in `core/framework/common` under `edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency`:

- `BaseInconsistencyState` — `addInconsistency(Inconsistency)`, `getInconsistencies()`, and a default `getInconsistenciesOfType(Class<T>)`; extends `IConfigurable`.
- `AbstractInconsistencyState` — adds the duplicate-skipping `addInconsistency` and the backing inconsistency list.
- `InconsistencyState` / `InconsistencyStates` — add the recommended-instance API and the per-`Metamodel` keying.
- `ArtemisInconsistencyState` (marker extension of `BaseInconsistencyState`) and `ArtemisInconsistencyStates` — per-`ArtemisTarget` keying, where `ArtemisTarget` is the record `(Metamodel, NamedEntityType)`.

The state accessors `getInconsistencyState(Metamodel)` and `getArtemisInconsistencyState(ArtemisTarget)` return `null` (with a warning) when the corresponding state is absent, while the collection accessors built on them (`getAllInconsistenciesForModel`, `getInconsistenciesOfTypeForModel`, `getAllInconsistencies`, `getAllTextInconsistencies`, `getAllModelInconsistencies`) return an empty list in that case; `getInconsistentSentences()` groups text inconsistencies by sentence number.

The two LLM-validated stages deliberately do **not** use this API: `ProgrammingLanguageInconsistencyState` and `ArchitecturalStylePatternInconsistencyState` are plain `PipelineStepData` interfaces registered under their own IDs, with exactly one state instance per run (no metamodel or target keying).

## Artemis-Based Inconsistency Detection

**Source**: `inconsistency-detection/stages-id/artemis-inconsistency-detection/src/main/java/edu/kit/kastel/mcse/ardoco/id/ArtemisInconsistencyChecker.java`

`ArtemisInconsistencyChecker` detects TEAM/MEAT inconsistencies between the SAD and a **code or architecture model produced by the ArTEMiS NER pipeline** rather than between SAD and SAM. Its constructor receives the `DataRepository` and the strategy list, builds the agents up front via the `ArtemisInconsistencyAgent` factory, and applies configuration through the static `get(additionalConfigs, dataRepository, strategies)` factory.

- `ArtemisInconsistencyAgent.createAgents` creates one `ArtemisTeamInconsistencyAgent` per strategy, plus an `ArtemisMeatInconsistencyAgent` **only** when the strategy's `NamedEntityType` is `COMPONENT` — `CLASS` and `DATAFILE` strategies are TEAM-only. Agent ids are derived from the strategy id (`<ENTITY_TYPE>Artemis`), e.g., `COMPONENTArtemisTeamInconsistencyAgent`.
- `initializeState()` builds an `ArtemisInconsistencyStatesImpl` over the strategies' `ArtemisTarget`s (a `LinkedHashMap` keyed by target) and registers it under `ArtemisInconsistencyStates.ID`. `getState(target)` lazily creates a state for unknown targets via `computeIfAbsent`, so states are keyed per `(Metamodel, NamedEntityType)` pair rather than by metamodel alone.
- `ArtemisTeamInconsistencyInformant` reads the per-target `ArtemisConnectionState` and reports **every occurrence of every unlinked named entity** as a `TextEntityAbsentFromModelInconsistency` with a fixed confidence of `DEFAULT_PROBABILITY = 0.92` and no origin candidate.
- `ArtemisMeatInconsistencyInformant` (only for `COMPONENT`) collects the model-entity ids referenced by the target's trace links into a set and flags every endpoint of the strategy's model (`modelStates.getModel(strategy.getMetamodel()).getEndpoints()`) whose id is not in that set, as a `ModelEntityAbsentFromTextInconsistency`. The informant is `@Deterministic`.
- Both informants extend the shared base class `ArtemisInconsistencyInformant`, which stores the `ArtemisNerStrategy` handed to the constructor and exposes `getStrategy()`/`getTarget()` so subclasses read traceability data for exactly their target; informant ids are derived from the strategy id as well (`<ENTITY_TYPE>ArtemisTeamInconsistencyInformant` / `…MeatInconsistencyInformant`).

Both informants reuse the type records of the classic `inconsistency-detection` stage and write into the per-target `ArtemisInconsistencyState`, which extends the core `AbstractInconsistencyState` (duplicate-skipping adds).

## LLM-Validated Checkers

Two newer stages validate documentation claims against code instead of trace links: `ProgrammingLanguageInconsistencyChecker` (are the documented programming-language statements true of the code?) and `ArchitecturalStylePatternInconsistencyChecker` (are the documented architectural styles/patterns implemented?). Both share the same skeleton:

### Stage Shape and Factories

Each checker is an `AbstractExecutionStage` that wraps **exactly one agent**, created by one of two static factories:

- `getForCodeModel(additionalConfigs, dataRepository, nerStrategy, validationAgentLlm)` → `CodeModel…InconsistencyAgent` (validates against the code model in the `DataRepository`);
- `getForCodeDirectory(additionalConfigs, dataRepository, nerStrategy, codeProjectPath, validationAgentLlm)` → `CodeDirectory…InconsistencyAgent` (validates against the source-code directory on disk).

Both factories call `applyConfiguration(additionalConfigs)`. `initializeState()` registers a **single** state instance under the stage's own ID — `ProgrammingLanguageInconsistencyState.ID` / `ArchitecturalStylePatternInconsistencyState.ID` — with no metamodel or target keying.

### NER Strategies and Prompts

Each stage ships an `ArtemisNerStrategy` implementation that tells the shared `ArtemisNer` stage what to recognize:

- `ProgrammingLanguageNerStrategy`: `NamedEntityType.PROGRAMMING_LANGUAGE`, metamodel `CODE_WITH_COMPILATION_UNITS`, prompt assembled from `programming-language-task-prompt.txt` + `programming-language-formatting-prompt.txt`.
- `ArchitecturalStylePatternNerStrategy`: `NamedEntityType.ARCHITECTURAL_PATTERN`, metamodel `CODE_WITH_COMPILATION_UNITS`, prompt assembled from `architectural-style-pattern-task-prompt.txt` + `architectural-style-pattern-formatting-prompt.txt`.

The strategy interface derives `ArtemisTarget(metamodel, entityType)` and the id `<ENTITY_TYPE>Artemis`; prompts are read from `/artemis/prompts/` below the stage's resources via `readPromptResource`. Neither ID strategy is `@Configurable` — it has no knobs beyond the runner-supplied LLMs. The task prompts define what counts as an occurrence (full documentation lines; explicit mentions only; specific rules for file extensions, web/query languages, or recognized patterns), and the formatting prompts fix the JSON shape (`name`, `type`, `alternativeNames`, `occurrences`). Prompt conventions and LLM selection are covered on [LLM Integration](llm-integration.md).

### Informant Loop and Statement/State Records

Both abstract informants (`AbstractProgrammingLanguageInconsistencyInformant`, `AbstractArchitecturalStylePatternInconsistencyInformant`) implement the same `process()`:

1. Resolve the per-target `ArtemisConnectionState` via `DataRepositoryHelper.getArtemisConnectionStates(dataRepository).getState(nerStrategy.getTarget())` and the stage's own inconsistency state.
2. Read the `SimpleText` produced by `SimpleTextPreprocessingAgent` and join its lines into the full documentation text. Occurrence sentence numbers are 1-based documentation line numbers, mapped to line index `sentenceNumber - 1`; out-of-bounds occurrences are skipped with a warning.
3. For each named entity and each occurrence, build a statement record and validate it (via the abstract hook, or — for the architectural-style stage — via the specialized-validator registry first).
4. Valid results are collected into `validatedStatements`; invalid results into `inconsistencies` together with the validator's `explanation`; both lists are added to the state in bulk.

The state records are deterministic and `TreeSet`-backed, so duplicates collapse by `compareTo`:

- `ProgrammingLanguageStatement(sentenceNumber, programmingLanguage, originalProgrammingLanguageName, sentenceText)` — `sentenceNumber` must be `>= 1`; the constructor resolves the `ProgrammingLanguage` enum by normalized name or file extension (fallback `OTHER`) and `getProgrammingLanguageDisplayName()` lowercases the resolved name. Ordering: sentence number, language, original name.
- `ProgrammingLanguageInconsistency(statement, explanation)` — comparable by statement then explanation; the state keeps `SortedSet`s of both statements and inconsistencies.
- `ArchitecturalStylePatternStatement(sentenceNumber, architecturalStylePatternName, normalizedArchitecturalStylePatternName, sentenceText)` — `normalize` lowercases, replaces underscores with dashes, collapses whitespace, and canonicalizes common aliases (e.g., MVC variants → `mvc`; layered/n-tier variants → `layered architecture`; microservice variants → `microservices`; client-server variants → `client-server`; pipes-and-filters → `pipe-and-filter`; event-driven variants → `event-driven architecture`; ports-and-adapters/hexagonal → `hexagonal architecture`).
- `ArchitecturalStylePatternInconsistencyStateImpl` mirrors the programming-language state with `TreeSet`s.

### Validation Agents, Tools, and Retries

Validation is delegated to a langchain4j **AiServices** agent interface per stage — `ProgrammingLanguageValidationLlmAgent` and `ArchitecturalStylePatternValidationLlmAgent` — each with two `@SystemMessage`/`@UserMessage`-annotated methods, one per evidence source (code directory vs. code model). The programming-language agent implements `validateProgrammingLanguageStatementAgainstCodeDirectory` and `validateProgrammingLanguageStatementAgainstCodeModel`; the architectural-style agent implements the corresponding `validateArchitecturalStylePatternStatementAgainstCodeDirectory`/`…AgainstCodeModel` methods. Both return a `(valid, explanation)` record (`ProgrammingLanguageValidationResult` / `ArchitecturalStylePatternValidationResult`). The system messages impose the same core contract: use only the provided read-only tools, never guess, treat only *repository-specific* claims as falsifiable (non-specific statements return `true`), base the final determination on source-code evidence, and do not use repository documentation as final evidence. The programming-language agent must explicitly verify "strictly/only/purely/exclusively/entirely in language X" claims; the architectural-style agent must not accept a mere naming coincidence (a `controller` package alone does not prove MVC).

The agents are built with:

- `validationAgentLlm.createUncached()` as the chat model — deliberately uncached because the ARDoCo `CachedChatLanguageModel` does not support tool calling;
- `CodeDirectoryReadOnlyTools(codeProjectPath)` for the directory variant, or `CodeModelReadOnlyTools(dataRepository)` for the code-model variant, plus a `.beforeToolExecution(...)` hook that lazily initializes the code model from `ModelStates` (`CODE_WITH_COMPILATION_UNITS`) on first tool use.

`CodeDirectoryReadOnlyTools` exposes `listDirectory`, `tree`, `readFile`, `readFileLines`, `findFiles`, `search`, and `getFileInfo` over a project root it resolves to a real path for symlink-aware containment; outputs are bounded (max 50 search results, 20 000 chars per file, 200 directory entries, 500 tree entries, depth 8) and generated/IDE directories (`.git`, `.idea`, `.gradle`, `.mvn`, `target`, `build`, `node_modules`, `out`, `bin`) are skipped. `CodeModelReadOnlyTools` provides the navigation/metadata surface (`listDirectory`, `tree`, `findFiles`, `getFileInfo`) over the `CodeModelWithCompilationUnits`.

Every validation call is retried up to `MAX_VALIDATION_RETRIES` with a warning per attempt; the retry limits and failure semantics differ per informant:

| Informant | `MAX_VALIDATION_RETRIES` | Catch / rethrow |
|-----------|--------------------------|-----------------|
| `CodeDirectoryProgrammingLanguageInconsistencyInformant` | `3` | `IllegalArgumentException` → rethrow last |
| `CodeModelProgrammingLanguageInconsistencyInformant` | `3` | `IllegalArgumentException` → rethrow last |
| `CodeModelArchitecturalStylePatternInconsistencyInformant` | `3` | `Exception` → rethrow wrapped in `RuntimeException` |
| `CodeDirectoryArchitecturalStylePatternInconsistencyInformant` | `5` | `Exception` → rethrow wrapped in `RuntimeException` |

So a persistently failing validation aborts the informant (and thus the agent/stage) rather than being silently swallowed.

### Specialized Validator Registry (Architectural Style/Pattern Only)

The architectural-style-pattern stage adds a pluggable pre-LLM validation layer:

- `ArchitecturalStylePatternStatementValidator` — interface with `supports(name)` and `validate(context)`.
- `ArchitecturalStylePatternStatementValidatorRegistry` — holds an immutable validator list; `withDefaultValidators()` currently returns an **empty** list with a hook comment where concrete validators can be added. `normalize` mirrors the statement record's normalization, and `findValidator` returns the **first** validator whose `supports(normalizedName)` matches.
- `ArchitecturalStylePatternValidationContext` — record `(dataRepository, namedEntityOccurrence, architecturalStylePatternName, fullDocumentationText, sentenceText, codeProjectPath)` with non-null checks on all but the path and a `hasCodeProjectPath()` helper.

The abstract informant's `validateWithSpecializedValidatorIfAvailable` builds the context (with `codeProjectPath` supplied by `getCodeProjectPathOrNull()`, which only the `CodeDirectory` informant overrides) and dispatches to the registry validator when one matches; only if none matches does it fall back to the generic LLM validation hook `validateArchitecturalStylePatternStatement(...)`. A registered validator therefore takes precedence over the LLM for the pattern names it supports.

### LLM-Validated Checker Flow

```mermaid
flowchart TD
    DOC["Documentation text"] --> STP["SimpleTextPreprocessingAgent"]
    STP --> TXT[("SimpleText in DataRepository")]
    TXT --> NER["ArtemisNer agent for the stage strategy"]
    NER --> CONN[("ArtemisConnectionState per ArtemisTarget")]
    CONN --> LOOP["Abstract informant iterates named entities and occurrences"]
    LOOP --> REC["Build statement record for the line"]
    REC --> REG{"Specialized validator registered for this name?"}
    REG -->|"yes"| SPEC["Validator validate context"]
    REG -->|"no"| LLM["LLM validation agent AiServices call"]
    LLM --> TOOLS["Read-only tools CodeModel or CodeDirectory"]
    TOOLS --> LLM
    SPEC --> RES{"Result"}
    LLM --> RES
    RES -->|"valid"| OK["state.addValidatedStatements"]
    RES -->|"invalid"| BAD["state.addInconsistencies with explanation"]
```

*Shared flow of the programming-language and architectural-style/pattern checkers: NER produces the named entities and occurrences; each occurrence line becomes a statement; a specialized validator (if registered) or the LLM validation agent with read-only tools decides validity, and the per-stage state collects both validated statements and explained inconsistencies.*

## Inconsistency Types and Output

`TextEntityAbsentFromModelInconsistency` is a record with `name`, `sentence` (1-based), `confidence`, and the originating `MissingElementInconsistencyCandidate`; equality compares name, sentence, and confidence. `ModelEntityAbsentFromTextInconsistency` wraps the `ModelEntity` and exposes its id via `getModelInstanceUid()`, which is what `ArdocoResult.getAllModelInconsistencies()` consumers (and the MEAT evaluation) compare against. Both provide `getType()`, a human-readable `getReason()`, and `toFileOutput()` entries; `FilePrinter.writeInconsistencyOutput` renders inconsistent sentences plus model inconsistency reasons into `inconsistencyDetection_<project>.txt` when the pipeline is run with output saving.

Note that the Artemis TEAM informant and the classic baseline reuse `TextEntityAbsentFromModelInconsistency` with `null` origin, so only the classic TEAM path populates the origin candidate.

## Testing and Evaluation

- **Unit tests** (`stages-id/inconsistency-detection/src/test/java/.../id/`): `types/AbstractInconsistencyTypeTest.java` plus `types/TextEntityAbsentFromModelInconsistencyTest.java` and `types/ModelEntityAbsentFromTextInconsistencyTest.java` verify type strings, equality, and (currently `@Disabled` because expected values change regularly) reason and file output; `agents/ModelEntityAbsentFromTextInconsistencyTest.java` covers the MEAT whitelist regex and type-targeting helpers.
- **Shared ArchUnit tests** (`tests-inconsistency`): `ArchitectureTest`, `ConfigurationTest`, and `DeterministicArdocoTest` extend the rule base classes from `core/tests-base`, enforcing the architecture rules, `@Configurable` validity, and determinism over `core`, `tlr`, and `id` packages.
- **Evaluation ITs**: `InconsistencyDetectionEvaluationIT` evaluates TEAM (micro-averaged metrics asserted against the project's `ExpectedResults` minimums), MEAT (UID comparison against the `*_UME.csv` unmentioned-elements gold standard on the cached base run), and a baseline variant gated by the `testBaseline` environment variable that swaps `InconsistencyChecker` for `InconsistencyBaseline` (a TEAM record with empty name and fixed confidence `0.69` for every sentence lacking a trace link) and logs weighted-average metrics without asserting them. The newer LLM-based ITs (`InconsistencyDetectionEvaluationArtemisIT`, `ProgrammingLanguageInconsistencyDetectionEvaluationIT`, `ArchitecturalStylePatternInconsistencyDetectionEvaluationIT`) are disabled for CI runs and are meant for manual execution with an LLM-backed configuration.

The hold-back run producers/evaluators, gold standards, metric gating, and run recipes for all of these are documented on [Testing & Evaluation](testing.md).

Within this module tree, the harness has three layers worth naming:

- **Run producers** (`integration/inconsistencyhelper/`): `HoldBackRunResultsProducer` (classic TEAM hold-back over an `AnonymousRunner` pipeline) and, for the ArTEMiS ITs, the `inconsistencyhelper/artemis/runproducer/` family producing `ImmutableMap<ArtemisEvaluationRun, ArdocoResult>` (base run plus held-back/extended runs): `SingleArtemisInconsistencyRunProducer` produces only the base run, `ComponentHoldBackArtemisInconsistencyRunProducer` produces one run per architecture component with a `HoldBackComponentArtemisNerStrategy` whose NER candidate list omits the held-back component id, `ClassHoldBackArtemisInconsistencyRunProducer` produces `numberOfRuns` hold-back runs selecting `numberOfHeldBackClassesPerRun` classes each (weighted toward rare classes with `1 / occurrences^0.5`, seeded per run via `seed + runIndex`) and runs an `AnonymousRunner` pipeline whose `HoldbackClassArtemisConnectionInformant` filters those class names out of the connection candidates, and `DatafileExtendedArtemisInconsistencyRunProducer` runs the extended SAD documents. Base runs and component hold-back runs go through the `ArtemisInconsistencyRunSupport.run(...)` helper, which sets up and runs the `ArtemisInconsistencyDetection` runner; the class hold-back runs instead use a bespoke `AnonymousRunner` pipeline, and the datafile extended runs construct that runner directly with a per-run output directory — with strategies in all three cases coming from the `ArtemisInconsistencyEvaluationConfiguration` bundles (`component()`/`clazz()`/`datafile()`). The class hold-back candidate set is derived from the `ClassArtemisInconsistencyTask` trace-link gold standard, whose parser strips package prefixes (`Environment.Base` → `Base`).
- **Evaluators**: per-entity `ArtemisInconsistencyEvaluator` implementations (`ComponentArtemisInconsistencyEvaluator` supports both MEAT and TEAM; `ClassArtemisInconsistencyEvaluator` and `DatafileArtemisInconsistencyEvaluator` are TEAM-only with hold-back/extended variants, since the interface's default `evaluateMeat` throws `UnsupportedOperationException`) compute `SingleClassificationResult<String>` via `ClassificationMetricsCalculator`, and `ArtemisInconsistencyEvaluationWriter` writes per-run and weighted-average results (`MEAT_…`/`TEAM_…` files plus confusion sets) into `target/testout/artemis_eval_id`; the LLM-stage helpers `ProgrammingLanguageInconsistencyDetectionEvaluation` and `ArchitecturalStylePatternInconsistencyDetectionEvaluation` evaluate from the stage state — inconsistency strings `"<sentence>,<name>,false"` (validated statements additionally feed a NER-occurrence evaluation as `"<sentence>,<name>"`) — via `ClassificationMetricsCalculator` with `confusionMatrixSum = null`.
- **Text-extension gold standards** (`src/test/resources/`): `programming-language-text-extensions/` (`rod`, `scons`, `teammates`, `zengarden`; six template sentence types — IMPLEMENTATION, LANGUAGE_USE, LANGUAGE_FILES, LANGUAGE_ARTIFACT, LANGUAGE_COMPONENT, LANGUAGE_STACK — filled with a fixed seed, documented in the folder `README.md`) and `architectural-style-pattern-text-extensions/` (`beets`, `coronawarnapp`, `rod`, `teammates`; LLM-written continuations that present fictional styles/patterns as established facts, prompt documented in `README.md`) both hold `extension-N.txt` documents plus `goldstandard-extension-N.csv` files with `line,pattern/language,valid` rows (the evaluators keep only the `valid=false` rows), and the extended runs load `extension-<runIndex>.txt` and merge the extension gold rows into the expected set. `datafile-artemis-text-extensions/` instead carries `sentenceID,file` gold rows mapping extended sentences to LLM-generated datafile names absent from the code model — every row is an expected TEAM inconsistency for `DatafileArtemisInconsistencyEvaluator`. A parallel tree under `tlr/tests-tlr` follows the same README-documented generation process but weaves a different, tree-specific set of generated files (e.g., `.feature` files) into its extension texts, with matching trace-link gold standards for the TLR datafile evaluation.

The classic TEAM/MEAT gold standards still come from the task enums: `InconsistencyDetectionTask` and `ComponentArtemisInconsistencyTask` reference the `*_UME.csv` unmentioned-elements files plus per-project `ExpectedResults` (real minimums) and `filterlists_all.txt` filter configuration, while the LLM-stage enums reference placeholder results: `ArchitecturalStylePatternInconsistencyDetectionTask`/`ProgrammingLanguageInconsistencyDetectionTask` the per-pattern/per-language CSVs and `ClassArtemisInconsistencyTask`/`DatafileArtemisInconsistencyTask` the class/datafile TEAM gold standards, all with `ExpectedResults(.420, …)`.

Run commands live on [Quickstart](quickstart.md) and [Operations](operations.md).

## Use Cases

- **Architecture Evolution**: identifying outdated or missing documentation during system evolution
- **Quality Assurance**: ensuring documentation completeness before releases
- **Onboarding**: helping new team members understand documentation gaps
- **Consistency Checking**: verifying alignment between informal documents and formal models
- **Reverse Engineering**: finding unmentioned components when analyzing existing systems
- **Documentation Validation**: checking that documented technologies and architectural patterns match the actual code (LLM-validated checkers)
