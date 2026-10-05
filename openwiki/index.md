---
okf_version: "0.2"
---

# Files

- [Architecture](architecture.md) - ARDoCo pipeline composite pattern (stage → agent → informant), DataRepository blackboard, intermediate artifacts (Text, SAM, Code Model), execution runners, and configuration system.
- [Inconsistency Detection](inconsistency-detection.md) - TEAM and MEAT inconsistency detection between SAD and SAM, the InconsistencyChecker stage and its three agents, pre-filter configuration, state/candidate model, and the hold-back evaluation harness.
- [Operations](operations.md) - Build system, code quality, CI/CD workflows, monorepo sync scripts, environment configuration, and external services for ARDoCo.
- [ARDoCo — OpenWiki Quickstart](quickstart.md) - Entry point to the ARDoCo OpenWiki: what ARDoCo is, the monorepo module layout, build prerequisites and commands, environment configuration summary, documentation map, and task routing to the domain pages.
- [Testing & Evaluation](testing.md) - Map of ARDoCo's test and evaluation infrastructure — the shared ArchUnit rule suites in core/tests-base, benchmark-driven TLR and ID integration tests with gold standards and ExpectedResults gating, env-gated test selection, the inconsistency-detection hold-back harness, and module-scoped run recipes.
- [TLR Approaches](tlr-approaches.md) - Traceability Link Recovery approaches (SWATTR, ArDoCode, ArCoTL, TransArC, ExArch, ArTEMiS and the two ArTEMiS hybrids), their eight runner pipelines, the seven stage modules, model/code extraction backends, LLM integration, and the ArCoTL heuristic computation tree.
