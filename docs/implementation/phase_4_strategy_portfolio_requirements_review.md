---
review_id: RR-004
title: "Phase 4 Strategy Portfolio Requirements Review"
status: approved
owner: "Investor Development Platform Team"
last_updated: 2026-06-07
version: "0.6"
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
- Strategy UI must distinguish tracked-only `watch` symbols from buy/sell transaction entries that include share quantity.
- Strategy symbol rows must expose next actions: watch symbols can be bought or deleted, and owned symbols can be sold.
- Strategy owners can switch a strategy between private and public visibility after creation.
- Strategy creation is launched from the Strategies panel header; Add Symbol is a separate compact panel for the selected strategy.
- Buy/sell transactions must use the current live quote price supplied by the backend, not a manually entered execution price.
- Buy/sell transactions are accepted only when the current quote indicates a regular/open market.
- Buy transactions must not exceed the strategy's available cash; sell transactions must not exceed currently owned shares.
- Allocation and performance displays must be based on append-only transaction-derived owned positions plus uninvested strategy cash, not tracked-only watch symbols.
- Expose strategy-scoped quote, chart, and indicator surfaces only for symbols that belong to the strategy.
- Show strategy performance history as total strategy value including cash, while preserving individual selected-symbol price series for stock research.
- Support public reads only for public strategies.

## Requirements Decisions

| Topic | Decision | Rationale |
| --- | --- | --- |
| Strategy type | Strategies are virtual records only; no broker execution. | Matches product scope and non-advice boundary. |
| Starting capital | Positive decimal value, immutable after creation in Phase 4; UI defaults to a smaller beginner-friendly amount. | Keeps performance basis stable while avoiding an overly large default for the likely audience. |
| Visibility | `private` and `public`; private endpoints are owner-scoped. | Supports private research and public credibility. |
| Visibility updates | Owners can switch an existing strategy between `private` and `public`. | Publishing state should be reversible while preserving transaction history. |
| Watch symbols | Add to a strategy research set with `trackingStatus=watch`; no transaction or position is created. | Implements portfolio-native private watchlist behavior. |
| Owned symbols | Buy/sell actions append immutable transactions with quantity and server-captured live quote price. | Enables allocation and performance to be recomputed from stored event history without manual fill-price claims. |
| Row actions | Watch rows expose Buy and Delete; owned rows expose Sell. | Makes the next valid action visible where the investor is already looking. |
| Duplicate symbols | A strategy may have one tracked-symbol row per symbol. | Keeps market-data scope unambiguous. |
| Transactions | Transactions are append-only rows; Phase 4 does not implement amendment endpoints. | Preserves auditability and avoids silent rewrites. |
| Decision links | Transaction `decisionId` is optional, but if supplied it must belong to the authenticated user. | Prevents cross-user decision leakage. |
| Market data | Phase 4 backend returns scoped placeholder quote/chart/indicator responses until real providers are wired. | Validates ownership and symbol scope without introducing external data dependencies. |
| Strategy performance chart | The strategy history UI presents total strategy value including cash by default and keeps individual symbol series selectable for research. | Matches the personal ETF mental model while preserving symbol-level inspection. |
| Cash guardrails | Buys require enough strategy cash; sells require enough owned shares. | Prevents impossible paper-portfolio transactions. |
| Public market data | Public endpoints return data only for public strategies and public tracked symbols / transaction symbols. | Respects public/private boundaries. |

## Acceptance Criteria

- Strategy creation returns owner-scoped strategy metadata with fixed starting capital and visibility.
- Updating strategy visibility returns the updated strategy metadata and public endpoints immediately respect the new visibility.
- A tracked symbol can be added without creating a transaction.
- A watch symbol can be deleted when it has no transaction history.
- A watch symbol with transaction history cannot be deleted and returns `409`.
- A strategy position can be opened by recording a buy transaction with quantity while the backend stores the live quote price.
- A buy over available strategy cash returns `409`.
- A sell over currently owned shares returns `409`.
- A buy/sell when the market is not regular/open returns `409`.
- Duplicate tracked symbol adds return `409`.
- Appending a transaction creates an immutable transaction row and makes the symbol eligible for strategy market-data surfaces.
- Strategy quote rows identify each symbol as `owned` or `watch`; owned symbols include quantity, cost basis, market value, unrealized gain/loss, gain/loss percent, and allocation weight when pricing is available.
- Strategy summary totals include starting capital, cash balance, holdings value, total strategy value, total gain/loss, and total gain/loss percent.
- Strategy history shows total strategy performance including cash by default, supports switching to individual symbol price series, and formats price-axis values to two decimals.
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

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-05 | approved | — | Initial Phase 4 requirements review for strategy portfolios. |
| 0.2 | 2026-06-07 | approved | 0.1 | Clarified watch-vs-position strategy actions and transaction-derived allocation/performance fields. |
| 0.3 | 2026-06-07 | approved | 0.2 | Clarified symbol-level strategy price history, symbol switching, and two-decimal price-axis formatting. |
| 0.4 | 2026-06-07 | approved | 0.3 | Reframed strategies as paper portfolios with cash, live quote execution, market-open guardrails, and total strategy performance history. |
| 0.5 | 2026-06-07 | approved | 0.4 | Added row-level strategy symbol actions for watch buy/delete and owned sell flows. |
| 0.6 | 2026-06-07 | approved | 0.5 | Added post-create strategy visibility switching and compact Strategies/Add Symbol panel layout. |
