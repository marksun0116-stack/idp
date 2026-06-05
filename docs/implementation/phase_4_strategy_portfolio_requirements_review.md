---
review_id: RR-004
title: "Phase 4 Strategy Portfolio Requirements Review"
status: approved
owner: "Investor Development Platform Team"
last_updated: 2026-06-05
version: "0.1"
linked_plan: PLAN-investor-development-platform-001
linked_story:
  - US-007
  - US-008
  - US-009
  - US-010
linked_primitives:
  - CONR-strategy-portfolio-api-001
  - CON-public-strategy-auditability-001
  - INV-public-strategy-history-001
  - INV-strategy-market-data-scope-001
---

# Phase 4 Strategy Portfolio Requirements Review

## Purpose

Phase 4 adds virtual strategy portfolios that combine public credibility, private tracking, append-only transactions, and strategy-scoped research surfaces.

## Scope

- Create private or public virtual strategy portfolios with fixed starting capital.
- Add tracked-only symbols to a strategy without creating a buy transaction, position, or performance event.
- Append buy/sell transactions linked optionally to an owned decision.
- Expose strategy-scoped quote, chart, and indicator surfaces only for symbols that belong to the strategy.
- Support public reads only for public strategies.

## Requirements Decisions

| Topic | Decision | Rationale |
| --- | --- | --- |
| Strategy type | Strategies are virtual records only; no broker execution. | Matches product scope and non-advice boundary. |
| Starting capital | Positive decimal value, immutable after creation in Phase 4. | Keeps performance basis stable. |
| Visibility | `private` and `public`; private endpoints are owner-scoped. | Supports private research and public credibility. |
| Tracked-only symbols | Add to a strategy research set with `trackingStatus=tracked`; no transaction or position is created. | Implements portfolio-native private watchlist behavior. |
| Duplicate symbols | A strategy may have one tracked-symbol row per symbol. | Keeps market-data scope unambiguous. |
| Transactions | Transactions are append-only rows; Phase 4 does not implement amendment endpoints. | Preserves auditability and avoids silent rewrites. |
| Decision links | Transaction `decisionId` is optional, but if supplied it must belong to the authenticated user. | Prevents cross-user decision leakage. |
| Market data | Phase 4 backend returns scoped placeholder quote/chart/indicator responses until real providers are wired. | Validates ownership and symbol scope without introducing external data dependencies. |
| Public market data | Public endpoints return data only for public strategies and public tracked symbols / transaction symbols. | Respects public/private boundaries. |

## Acceptance Criteria

- Strategy creation returns owner-scoped strategy metadata with fixed starting capital and visibility.
- A tracked symbol can be added without creating a transaction.
- Duplicate tracked symbol adds return `409`.
- Appending a transaction creates an immutable transaction row and makes the symbol eligible for strategy market-data surfaces.
- A transaction linked to another user's decision returns `404`.
- Private strategy reads and market-data endpoints return `404` for non-owners.
- Public strategy reads return `404` for private strategies.
- Quote/chart/indicator endpoints reject symbols outside the strategy with `404`.

## Test Coverage

- `StrategyPortfolioCreateTest`: strategy creation and owner isolation.
- `StrategyTrackedSymbolTest`: tracked-only symbol behavior and duplicate protection.
- `StrategyTransactionTest`: append-only transaction creation and decision ownership validation.
- `StrategyMarketDataScopeTest`: market-data surfaces are scoped to tracked or transaction symbols.

## Parallelization

- `US-007` must complete before `US-008`, `US-009`, and `US-010`.
- `US-008` should precede `US-010` because tracked-symbol membership defines market-data scope.
- `US-009` can proceed after `US-007` and in parallel with `US-008`, but public performance can wait for later refinement.

## Open Follow-Ups

- Add transaction amendment events.
- Add real quote/chart/indicator provider integration.
- Add computed performance and position aggregation.

## Revision History

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-05 | approved | — | Initial Phase 4 requirements review for strategy portfolios. |
