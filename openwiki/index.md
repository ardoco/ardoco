---
okf_version: "0.2"
---

# Files

- [Architecture](architecture.md) - ARDoCo pipeline composite pattern (stage → agent → informant), DataRepository blackboard, intermediate artifacts (Text, SAM, Code Model), execution runners, and configuration system.
- [Inconsistency Detection](inconsistency-detection.md) - Inconsistency detection module for ARDoCo: classic TEAM/MEAT detection between SAD and SAM via InconsistencyChecker, Artemis-based per-strategy detection, the LLM-validated programming-language and architectural-style-pattern checkers, their state models, the four pipeline-id runners, and pre-filter configuration.
- [LLM Integration](llm-integration.md) - Cross-system LLM usage shared by TLR and inconsistency detection: the LargeLanguageModel enum and model selection, CachedChatLanguageModel disk caching and deterministic seeding, the per-stage prompt resource layout, and the langchain4j AiServices validation agents restricted to read-only code tools.
- [Operations](operations.md) - Build system, code quality, CI/CD workflows, monorepo sync scripts, environment configuration, external services, and the AGENTS.md/CLAUDE.md OpenWiki maintenance contract for ARDoCo.
- [ARDoCo — OpenWiki Quickstart](quickstart.md) - Entry point to the ARDoCo OpenWiki: what ARDoCo is, the monorepo module layout, build prerequisites and commands, environment configuration summary, documentation map, and task routing to the domain pages.
- [Testing & Evaluation](testing.md) - Map of ARDoCo's test and evaluation infrastructure — the shared ArchUnit rule suites in core/tests-base, benchmark-driven TLR and ID integration tests with gold standards and ExpectedResults gating, env-gated test selection, the ArTEMiS TLR and ID evaluation harnesses with hold-back strategies and text extensions, and module-scoped run recipes.
- [TLR Approaches](tlr-approaches.md) - Traceability Link Recovery approaches (SWATTR, ArDoCode, ArCoTL, TransArC, ExArch, ArTEMiS and the two ArTEMiS hybrids), their eight runner pipelines, the seven stage modules including the ArTEMiS NER stage family, model/code extraction backends, LLM integration, and the ArCoTL heuristic computation tree.
