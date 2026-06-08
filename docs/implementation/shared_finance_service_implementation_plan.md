---
plan_id: PLAN-shared-finance-service-001
title: "Shared Finance Data Service Implementation Plan"
status: in_progress
owner: "Workspace team"
last_updated: 2026-06-07
version: "0.2"
linked_prds:
  - docs/prd/investor_development_platform_prd.md
  - /home/msun/projects/stock-monitor/docs/prd/stock_monitor_prd.md
linked_features:
  - FEAT-shared-finance-service-001
  - FEAT-investor-development-platform-001
  - FEAT-stock-monitor-001
workspace_repos:
  - name: idp
    path: /home/msun/projects/idp
    role: program repo, consumer repo
  - name: finance-data-service
    path: /home/msun/projects/finance-data-service
    role: shared-service repo
  - name: stock-monitor
    path: /home/msun/projects/stock-monitor
    role: consumer repo
---

# Shared Finance Data Service Implementation Plan

## 1. Scope

Extract market-data fetching, caching, reference metadata, and technical analysis into a standalone shared service, then re-wire IDP and stock-monitor to consume that service over HTTP.

In scope:

- Workspace-level KFS artifacts and shared contract ownership in the IDP program repo.
- Standalone `finance-data-service` on port `8082`.
- Shared PostgreSQL cache for daily history, intraday bars, and stock metadata.
- Yahoo Finance provider abstraction inside the shared service.
- REST endpoints under `/api/finance/*`.
- IDP adapter from `MarketDataService` to the shared service.
- stock-monitor adapters for quote, history, market movers, news, insights/metadata as needed.
- Per-repo validation and smoke checks.

Out of scope for this plan:

- Paid provider migration such as Polygon or Alpha Vantage.
- Real-time streaming.
- Trading execution.
- Full CI/CD rollout beyond local validation and repo readiness.

## 2. Planning Principles

- IDP is the current program repo for cross-repo KFS governance until a separate governance repo exists.
- `CONR-finance-service-api-001` is the shared API contract owner for IDP and stock-monitor consumers.
- `finance-data-service` owns shared market-data runtime behavior, provider integration, cache tables, metadata seeding, and shared-service tests.
- IDP and stock-monitor own their local adapter behavior and user-facing fallback behavior.
- A story is `done` only when implementation, required KFS/docs, and relevant validation are complete.
- Existing direct Yahoo implementations may remain temporarily only if they are not selected as primary beans and the plan records the cleanup story.
- Cross-repo validation must be recorded in this plan before the overall plan can move to `done`.

## 3. Workspace Repositories

| Repo | Path | Role | Owns | Notes |
| --- | --- | --- | --- | --- |
| idp | `/home/msun/projects/idp` | program repo, consumer repo | Workspace KFS, shared implementation plan, shared finance contract, IDP adapter | KFS validator currently fails on malformed finance primitives and orphan primitives. |
| finance-data-service | `/home/msun/projects/finance-data-service` | shared-service repo | Shared market data service, provider integration, cache schema, service tests | Git repo exists and is pushed to GitHub. Local KFS/docs bootstrap is planned so it can evolve independently. |
| stock-monitor | `/home/msun/projects/stock-monitor` | consumer repo | stock-monitor adapters, repo-local docs/knowledge/tests | Has its own KFS/docs. Tests currently fail on metadata/cache handoff. |

## 4. Phase Overview

| Phase | Goal | Status | Owning repo(s) | Depends on | Parallelizable with | Trace |
| --- | --- | --- | --- | --- | --- | --- |
| Phase A - KFS and scaffold | Establish shared-service KFS intent, contract, and standalone service repo scaffold. | done | idp, finance-data-service | None | None | FEAT-shared-finance-service-001, DEC-shared-finance-service-001, CONR-finance-service-api-001 |
| Phase B - Cache layer | Move daily history, intraday cache, and metadata ownership into the shared service. | done | finance-data-service, idp | Phase A | Phase D after repos exist | CON-finance-cache-001 |
| Phase C - Provider and services | Centralize Yahoo provider and quote/history/movers/news service logic in the shared service. | done | finance-data-service | Phase B schema | Phase D | DEC-shared-finance-service-001 |
| Phase D - Technical analysis | Move canonical BigDecimal indicators and recommendation logic into the shared service. | in_progress | finance-data-service | Phase B repos | Phase C | FEAT-shared-finance-service-001 |
| Phase E - REST API and shared-service tests | Publish `/api/finance/*` endpoints and verify with meaningful service tests. | in_progress | finance-data-service, idp | Phase C, Phase D | None | CONR-finance-service-api-001 |
| Phase F - IDP consumer re-wire | Make IDP consume shared service correctly and remove direct Yahoo fallback implementation. | in_progress | idp, finance-data-service | Phase E endpoint contract | Phase G after contract stabilizes | CONR-finance-service-api-001, FEAT-investor-development-platform-001 |
| Phase G - stock-monitor consumer re-wire | Make stock-monitor consume shared service and retire local shared-cache/Yahoo ownership. | blocked | stock-monitor, finance-data-service, idp | Phase E endpoint contract, Phase G review | Phase F | CONR-finance-service-api-001, FEAT-stock-monitor-001 |
| Phase H - Shared-service KFS independence | Add local KFS/docs to finance-data-service and clarify shared contract ownership/mirroring. | planned | idp, finance-data-service | Phase A, RR-104 | Phase E tests | FEAT-shared-finance-service-001, CONR-finance-service-api-001 |
| Phase I - Workspace hardening | Align docs, implementation plan, validation, smoke flows, and repo handoff readiness. | planned | idp, finance-data-service, stock-monitor | Phases A-H | None | FEAT-shared-finance-service-001 |

## 5. User Story Plan

| Story ID | Phase | Repo(s) | User Story | Status | Depends on | Can run in parallel? | Trace | Validation | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RR-100 | Phase A | idp | Review workspace KFS ownership, repo roles, shared contract owner, and plan shape before further implementation. | done | None | No | FEAT-shared-finance-service-001 | Plan updated 2026-06-07 | Uses updated generic multi-repo KFS model. |
| US-100 | Phase A | idp | Create shared finance KFS primitives and add them to `knowledge/catalog.yml`. | done | RR-100 | No | FEAT-shared-finance-service-001 | KFS validation passed | Fixed malformed finance YAML and cataloged/normalized technical analysis primitives. |
| US-101 | Phase A | finance-data-service | Create standalone Spring Boot Maven project with app config, security, health endpoint, Docker basics, and GitHub repo. | done | US-100 | No | FEAT-shared-finance-service-001 | `mvn test -q` passed | Service repo exists, is committed, and tracks GitHub remote. |
| US-102 | Phase A | finance-data-service | Add Flyway V1 baseline migration. | done | US-101 | No | CON-finance-cache-001 | `mvn test -q` passed | `V1__baseline.sql` exists. |
| RR-101 | Phase B | idp, finance-data-service | Review cache ownership, DDL source, metadata seeding, and bootstrap defaults. | done | US-100 | No | CON-finance-cache-001 | KFS validation passed | DDL is mostly consolidated from stock-monitor; bootstrap is explicitly opt-in/disabled-by-default. Broader cache behavior tests remain in US-119. |
| US-103 | Phase B | finance-data-service | Add `stock_price_history` and `stock_intraday_cache` Flyway migrations, entities, and repositories. | done | RR-101 | Yes, with US-104 | CON-finance-cache-001 | `mvn test -q` passed | Added explicit batch `ON CONFLICT DO NOTHING` upserts and last-trading-date intraday cache behavior. Deeper PostgreSQL behavior tests remain in US-119. |
| US-104 | Phase B | finance-data-service | Add `stock_metadata` Flyway migration, entity, repository, and CSV files. | done | RR-101 | Yes, with US-103 | CON-finance-cache-001 | `mvn test -q` passed with local Docker/Testcontainers | Metadata schema/repository test validates PostgreSQL columns, seeded row count, and representative symbol data. |
| US-105 | Phase B | finance-data-service | Seed NASDAQ/NYSE/AMEX CSV metadata idempotently on startup. | done | US-104 | No | CON-finance-cache-001 | `mvn test -q` passed with local Docker/Testcontainers | CSV seeding test reruns the loader and validates idempotent row count plus non-destructive upsert behavior. |
| US-106 | Phase B | finance-data-service | Pre-warm daily history from `stock_metadata` with configurable async bootstrap. | done | US-105 | No | FEAT-shared-finance-service-001 | `mvn test -q` passed | Runner is disabled by default, supports opt-in async prewarm, clamps invalid concurrency, waits for scheduled refresh tasks before completion logging, and has config/concurrency unit coverage. |
| RR-102 | Phase C | idp, finance-data-service | Review provider interface, HTTP client choice, User-Agent, provider error contract, and service mapping. | done | US-100 | No | DEC-shared-finance-service-001 | `mvn test -q` and KFS validation passed | RestTemplate has timeout/User-Agent config; provider methods return empty results on provider errors; review fixed fragile Yahoo parsing, quote miss handling, and news service mapping. |
| US-107 | Phase C | finance-data-service | Add `MarketDataProvider` and Yahoo implementation with raw DTO records. | done | RR-102 | No | DEC-shared-finance-service-001 | `mvn test -q` passed | Provider interface and Yahoo implementation exist with tests for quote parsing, missing results, daily history null bars, and RSS news parsing. |
| US-108 | Phase C | finance-data-service | Add quote service with closed-market cache and bulk quote support. | done | US-107 | Yes, with US-109 | FEAT-shared-finance-service-001 | `mvn test -q` passed | Bulk quote path normalizes symbols and omits provider misses. Closed-market cache reuse and market-open fresh fetch behavior are covered with fixed-clock unit tests. |
| US-109 | Phase C | finance-data-service | Add history service with delta daily fetch and market-aware intraday cache. | done | US-107 | Yes, with US-108 | CON-finance-cache-001 | `mvn test -q` passed | History service uses injectable clock and has tests for cold-start daily fetch, delta fetch, fresh-skip behavior, market-open intraday freshness/staleness, closed-market cache serving, and old intraday purge cutoff. |
| US-110 | Phase C | finance-data-service | Add market movers and news services backed by Yahoo provider and metadata enrichment. | done | US-107 | Yes, with US-108 and US-109 | FEAT-shared-finance-service-001 | `mvn test -q` passed | News service delegates to provider with normalization/dedup coverage. Market mover metadata enrichment and controller response shape are covered. |
| RR-103 | Phase D | idp, finance-data-service | Review canonical indicator precision, trend verdict scope, and recommendation outputs. | in_progress | US-100 | No | FEAT-shared-finance-service-001 | Not complete | Needs explicit acceptance criteria for insights output compatibility. |
| US-111 | Phase D | finance-data-service | Port BigDecimal technical indicator calculations. | in_progress | RR-103 | Yes, with US-112 after review | FEAT-shared-finance-service-001 | Compile passed, behavior tests missing | `TechnicalIndicatorService` exists. Needs numerical regression tests. |
| US-112 | Phase D | finance-data-service | Port technical recommendation engine. | in_progress | US-111 | Yes, with US-113 | FEAT-shared-finance-service-001 | Compile passed, behavior tests missing | `TechnicalRecommendationEngine` exists. Needs regression tests. |
| US-113 | Phase D | finance-data-service | Provide insights behavior for RSI, average volume, and trend verdict using cached history. | in_progress | US-111 | Yes, with US-112 | FEAT-shared-finance-service-001 | Compile passed, behavior tests missing | Logic exists in `InsightsController`, not a dedicated `InsightsServiceImpl`; decide whether controller-local logic is acceptable. |
| RR-104 | Phase E | idp, finance-data-service, stock-monitor | Finalize all endpoint signatures, response shapes, error handling, and CORS before consumers rely on them. | blocked | US-110, US-113 | No | CONR-finance-service-api-001 | KFS validation failed | Current single quote endpoint and IDP parser disagree. Contract version remains draft. |
| US-114 | Phase E | finance-data-service | Publish single and bulk quote endpoints. | in_progress | RR-104 | No | CONR-finance-service-api-001 | Compile passed, smoke not run | Endpoints exist. Single endpoint returns a plain quote object; bulk endpoint returns `quotes` map. |
| US-115 | Phase E | finance-data-service | Publish daily history and intraday endpoints. | in_progress | US-114 | Yes, with US-116 | CONR-finance-service-api-001, CON-finance-cache-001 | Compile passed, smoke not run | Endpoints exist. Needs smoke and cache tests. |
| US-116 | Phase E | finance-data-service | Publish movers and news endpoints. | in_progress | US-114 | Yes, with US-115 | CONR-finance-service-api-001 | Compile passed, smoke not run | Endpoints exist. Needs smoke tests. |
| US-117 | Phase E | finance-data-service | Publish indicators and analysis endpoints. | in_progress | US-115 | Yes, with US-118 | CONR-finance-service-api-001 | Compile passed, smoke not run | Endpoints exist. Needs compatibility tests. |
| US-118 | Phase E | finance-data-service | Publish insights and metadata endpoints. | in_progress | US-117 | Yes, with US-117 | CONR-finance-service-api-001 | Compile passed, smoke not run | Endpoints exist. Needs tests and stock-monitor adapter alignment. |
| US-119 | Phase E | finance-data-service | Add Testcontainers or equivalent PostgreSQL integration tests for seeding, cache refresh, intraday staleness, and all endpoints. | planned | US-114, US-115, US-116, US-117, US-118 | No | CONR-finance-service-api-001, CON-finance-cache-001 | No tests present | This is the biggest shared-service readiness gap. |
| RR-105 | Phase F | idp, finance-data-service | Review IDP adapter behavior, fallback semantics, timeout, and response-shape compatibility. | in_progress | RR-104 | No | CONR-finance-service-api-001 | Not complete | Review exposed quote response mismatch. |
| US-120 | Phase F | idp | Replace IDP direct Yahoo calls with `RemoteFinanceDataService` while keeping `MarketDataService` stable. | in_progress | RR-105 | Yes, with stock-monitor adapters after contract fixed | CONR-finance-service-api-001, FEAT-investor-development-platform-001 | IDP tests passed, real contract broken | Remote service exists and is `@Primary`, but quote parser expects bulk shape while calling single endpoint. `YahooMarketDataService` still exists. |
| US-121 | Phase F | idp | Keep IDP tests green and add adapter contract tests for finance service responses. | in_progress | US-120 | No | CONR-finance-service-api-001 | Existing IDP `mvn test -q` passed | Need explicit tests for single quote, history, service-unavailable fallback, and URL encoding. |
| RR-106 | Phase G | stock-monitor, idp, finance-data-service | Review stock-monitor services to replace, metadata/cache ownership handoff, and test migration strategy. | blocked | RR-104 | No | CONR-finance-service-api-001, FEAT-stock-monitor-001 | stock-monitor tests failed | Need decide whether stock-monitor keeps local metadata for local features or delegates to shared metadata endpoint. |
| US-122 | Phase G | stock-monitor | Add HTTP-delegating stock-monitor services for quote, history, movers, news, and insights/metadata as required. | in_progress | RR-106 | Yes, with US-120 after contract fixed | CONR-finance-service-api-001 | stock-monitor tests failed | Remote quote/history/movers/news exist and are primary. No remote insights replacement. Local metadata/cache dependencies remain. |
| US-123 | Phase G | stock-monitor | Retire or supersede local Yahoo-fetching and shared-cache ownership in stock-monitor. | planned | US-122 | No | CONR-finance-service-api-001, CON-finance-cache-001 | Not run | Original `StockQuoteServiceImpl`, `StockHistoryServiceImpl`, `MarketMoversServiceImpl`, `StockNewsServiceImpl`, local cache migrations, and metadata services remain. |
| US-124 | Phase G | stock-monitor | Restore stock-monitor backend test pass after metadata/cache handoff. | blocked | RR-106, US-122 | No | FEAT-stock-monitor-001 | `mvn test -q` failed | H2 test context lacks `stock_metadata`; loader/watchlist paths still query it. |
| RR-107 | Phase H | idp, finance-data-service | Review shared-service KFS independence: which primitives move, which remain workspace-owned, and how consumers reference the contract. | planned | RR-104 | No | FEAT-shared-finance-service-001, CONR-finance-service-api-001 | Not run | Decide whether `CONR-finance-service-api-001` owner moves to finance-data-service now or after endpoint tests pass. |
| US-125 | Phase H | finance-data-service | Add local agent instructions and KFS/docs scaffold to finance-data-service. | planned | RR-107 | No | FEAT-shared-finance-service-001 | Not run | Add `AGENTS.md`, `CLAUDE.md`, `knowledge/catalog.yml`, service-local `docs/`, and optionally `.knowledge-first-system/` validation tooling. |
| US-126 | Phase H | finance-data-service, idp | Create or migrate service-owned primitives into finance-data-service without forking workspace truth. | planned | US-125 | No | FEAT-shared-finance-service-001, CON-finance-cache-001, CONR-finance-service-api-001 | Not run | Service-local knowledge should own cache/provider/service tests; workspace repo keeps cross-repo migration plan until governance changes. |
| US-127 | Phase H | idp, finance-data-service, stock-monitor | Define shared contract ownership and consumer mirroring/reference rules. | planned | US-126, RR-104 | No | CONR-finance-service-api-001 | Not run | Preferred future: finance-data-service owns API contract; IDP and stock-monitor reference or mirror the contract version. |
| US-130 | Phase I | idp | Fix KFS validation for finance primitives and existing orphan primitives. | done | US-100 | No | FEAT-shared-finance-service-001 | KFS validation passed | Completed while closing Phase A. |
| US-131 | Phase I | idp, finance-data-service, stock-monitor | Update PRD/architecture/design docs in each affected repo to describe shared-service ownership and remove stale direct-Yahoo claims. | planned | US-130, RR-104, RR-106, US-127 | Yes, after contract owner resolved | FEAT-shared-finance-service-001 | Not run | stock-monitor docs still contain direct Yahoo/local cache statements. |
| US-132 | Phase I | idp, finance-data-service, stock-monitor | Run end-to-end smoke with all services and verify IDP strategy quotes and stock-monitor watchlist quotes. | planned | US-119, US-121, US-124 | No | CONR-finance-service-api-001 | Not run | Requires finance service, IDP, stock-monitor, and DBs running. |

## 6. Parallelization Notes

- US-130 should happen before broad continuation because KFS validation is currently blocking trustworthy status.
- US-119 can proceed in parallel with IDP/stock-monitor adapter cleanup once RR-104 resolves response shapes.
- US-121 and stock-monitor US-124 should not depend on live Yahoo; use stubbed shared-service responses where possible.
- Phase F and Phase G can run in parallel only after `CONR-finance-service-api-001` is valid and response shapes are fixed.
- Phase H can start after RR-104 clarifies endpoint contract direction; it should not block service tests, but contract ownership transfer should wait until response shapes are stable.
- Docs updates in US-131 can begin after contract ownership is stable, but final wording should wait for adapter behavior and cache ownership decisions.

## 7. Progress Updates

- 2026-06-07: Created initial shared finance service plan as draft `0.1`.
- 2026-06-07: Created and pushed `/home/msun/projects/finance-data-service` Git repo to GitHub.
- 2026-06-07: Updated KFS process to require generic multi-repo repository ownership, validation logs, and implementation-plan updates across agents.
- 2026-06-07: Rewrote this plan to updated KFS plan spec `0.2`; marked actual status based on current repo inspection and validation results.
- 2026-06-07: Started Phase A cleanup; fixed malformed finance YAML, normalized/cataloged technical analysis primitives, and moved Phase A to `done`.
- 2026-06-07: Added future Phase H for finance-data-service local KFS/docs independence and explicit shared contract ownership/mirroring.
- 2026-06-07: Started Phase B requirements review (`RR-101`) for cache DDL, metadata seeding, and bootstrap defaults.
- 2026-06-07: Completed `RR-101`; aligned bootstrap policy to opt-in/disabled-by-default, confirmed DDL/loader are mostly ported, and identified Phase B implementation gaps in explicit batch upsert, last-trading-date intraday behavior, and PostgreSQL tests.
- 2026-06-07: Started `US-103`; hardening finance-data-service daily/intraday cache persistence and last-trading-date behavior.
- 2026-06-07: Completed `US-103`; finance-data-service now uses explicit batch upserts for daily and intraday cache rows and resolves intraday cache reads against the last trading date.
- 2026-06-07: Started `US-104` and `US-105`; validating finance-data-service metadata schema and CSV seeding idempotency.
- 2026-06-07: Added metadata PostgreSQL integration test for `US-104` and `US-105`; fixed local Docker/Testcontainers by passing Docker API `1.40` into Maven Surefire, then validated the PostgreSQL container test with no skipped tests.
- 2026-06-07: Completed `US-106`; bootstrap prewarm is disabled by default, opt-in with bounded concurrency, and covered by focused unit tests.
- 2026-06-07: Completed `RR-102` and `US-107`; hardened Yahoo provider parsing/error tolerance, wired news service to provider output, and made bulk quote misses safe.
- 2026-06-07: Completed `US-108`; quote service now uses injectable clock and has deterministic tests for closed-market cache reuse and market-open refresh.
- 2026-06-07: Completed `US-109` and `US-110`; history service clock-dependent cache behavior, market mover enrichment, and mover response shape now have focused tests. Phase C is done.

## 8. Validation Log

- 2026-06-07: `python3 .knowledge-first-system/scripts/validate_knowledge.py` in `idp` failed: malformed YAML in `CON-finance-cache-001` and `DEC-shared-finance-service-001`; orphan primitives `INV-indicator-accuracy-001`, `INV-strategy-validation-001`, and `CONR-technical-analysis-api-001`.
- 2026-06-07: `python3 .knowledge-first-system/scripts/validate_knowledge.py` in `idp` passed after fixing finance primitive YAML and cataloging normalized technical analysis primitives.
- 2026-06-07: `python3 .knowledge-first-system/scripts/validate_knowledge.py` in `idp` passed after aligning bootstrap intent in `CON-finance-cache-001` and `FEAT-shared-finance-service-001`.
- 2026-06-07: `mvn test -q` in `finance-data-service` passed, but no `src/test` files exist, so this is compile/package confidence only.
- 2026-06-07: `mvn test -q` in `finance-data-service` passed after `US-103` cache persistence changes; behavioral PostgreSQL coverage remains planned in `US-119`.
- 2026-06-07: `mvn test -q` in `finance-data-service` passed after adding `StockReferenceDataLoaderIntegrationTest`, but Surefire reported 1 skipped test because `disabledWithoutDocker=true` and Docker is unavailable. Earlier non-skipped attempts failed with Docker API negotiation error: client version 1.32 below daemon minimum 1.40.
- 2026-06-07: `mvn test -q` in `finance-data-service` passed after configuring Maven Surefire to pass Docker API `1.40`; Surefire reported `StockReferenceDataLoaderIntegrationTest` with Tests run: 1, Failures: 0, Errors: 0, Skipped: 0.
- 2026-06-07: `mvn test -q` in `finance-data-service` passed after `US-106`; `HistoryBootstrapRunnerTest` covers disabled, empty metadata, bounded concurrency, and invalid-concurrency clamp behavior.
- 2026-06-07: `mvn test -q` in `finance-data-service` passed after `RR-102`/`US-107`; provider/service tests cover Yahoo quote/history/news parsing, missing provider results, bulk quote misses, and news service delegation.
- 2026-06-07: `mvn test -q` in `finance-data-service` passed after `US-108`; `QuoteServiceImplTest` covers bulk normalization/misses, closed-market cache reuse, and market-open fresh fetch.
- 2026-06-07: `mvn test -q` in `finance-data-service` passed after completing Phase C; `HistoryServiceImplTest`, `MarketMoversServiceImplTest`, and `MarketMoversControllerTest` cover history cache rules, mover metadata enrichment, and mover response shape.
- 2026-06-07: `mvn test -q` in `idp/src/backend` passed.
- 2026-06-07: `mvn test -q` in `stock-monitor/src/backend` failed: test context lacks `stock_metadata` while loader/watchlist paths query it.
- 2026-06-07: Manual contract inspection found IDP quote adapter mismatch: IDP calls `/api/finance/quote/{symbol}` but parses a bulk `quotes` object; finance service single quote endpoint returns a plain quote object.

## 9. Open Questions

1. Which repo permanently owns `CONR-finance-service-api-001`: IDP program repo, finance-data-service, or a future governance repo?
2. Should `finance-data-service` install a copied `.knowledge-first-system/` validation toolchain now, or start with lightweight local `knowledge/`, `docs/`, `AGENTS.md`, and `CLAUDE.md` that reference the program repo?
3. Should stock-monitor retain local `stock_metadata` for portfolio/segment features, delegate all metadata to `/api/finance/metadata`, or use a hybrid?
4. Should IDP delete `YahooMarketDataService` immediately after adapter tests pass, or keep it temporarily as a non-primary emergency fallback behind explicit config?
5. Should single quote endpoint return a plain quote object or a `quotes` map wrapper? Consumers and contract must agree.

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-07 | draft | - | Initial plan. |
| 0.2 | 2026-06-07 | in_progress | 0.1 | Rebuilt plan using updated KFS implementation-plan spec with workspace repos, repo ownership, current story status, progress updates, validation log, and blockers. |
| 0.3 | 2026-06-07 | in_progress | 0.2 | Added future Phase H for finance-data-service local KFS/docs independence and shared contract ownership/mirroring. |
| 0.4 | 2026-06-07 | in_progress | 0.3 | Completed Phase B requirements review and aligned bootstrap policy to opt-in/disabled-by-default. |
| 0.5 | 2026-06-07 | in_progress | 0.4 | Completed Phase B cache-layer stories through optional bounded history bootstrap. |
| 0.6 | 2026-06-07 | in_progress | 0.5 | Completed Phase C provider review and Yahoo provider implementation tests. |
| 0.7 | 2026-06-07 | in_progress | 0.6 | Completed quote service closed-market cache and bulk quote behavior tests. |
| 0.8 | 2026-06-07 | in_progress | 0.7 | Completed Phase C provider/service work through history, movers, and news tests. |
