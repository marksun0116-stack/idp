---
arch_intent_id: ARCH-investor-development-platform-001
title: "Investor Development Platform Architecture Intent"
status: draft
owner: "Architecture Team"
architect: "TBD"
last_updated: 2026-06-01
version: "0.6"
linked_decisions:
  - DEC-tech-stack-001
  - DEC-chart-library-001
related_constraints:
  - CON-watchlist-access-001
  - CON-heatmap-scope-001
  - CON-historical-data-range-001
  - CON-market-movers-public-001
  - CON-portfolio-access-001
  - CON-holding-uniqueness-001
  - CON-sector-cache-001
  - CON-csv-import-001
related_invariants:
  - INV-watchlist-ownership-001
  - INV-heatmap-dedup-001
  - INV-historical-data-ownership-001
  - INV-portfolio-ownership-001
  - INV-holding-cost-basis-001
  - INV-manual-price-001
  - INV-sector-cache-001
  - INV-allocation-consolidation-001
related_prds:
  - PRD-investor-development-platform-001
slices:
  - "research-workspace"
  - "decision-journal"
  - "review-engine"
  - "behavioral-analytics"
  - "strategy-portfolios"
  - "investor-reputation"
---

# Investor Development Platform Architecture Intent

## 1. Summary

This document describes the architecture intent for the Investor Development Platform: a user-facing investment and research workspace backed by Java Spring Boot, PostgreSQL, and a React frontend. The system enforces per-user data ownership and delegates all market data to the shared finance-data-service.

**Research Workspace**: Watchlist management with multi-widget dashboard (live quote table, heatmap, comparison chart, market movers panel, news panel). Sources market data from shared finance-data-service via `/api/finance/*` endpoints.

**Investment Workspace**: Portfolio management with accounts, holdings, CSV import, allocation dashboard (by account type and sector). Market data sourced from shared finance-data-service.

## 2. Context and drivers

The application must support authenticated users, persistent watchlists, live stock quote display, and user-owned portfolio analysis with minimal operational complexity. The primary driver is a lightweight tracking and investment workspace rather than trading execution or broker integration.

## 3. Stakeholders and consumers

- Product team: defines user-facing scope.
- Engineering: builds the backend, frontend, and integration.
- Operations: supports deployment and monitoring.

## 4. Principles and constraints

- AP-001: Keep data ownership boundaries explicit; each watchlist belongs to a single user. (Trace: CON-watchlist-access-001, INV-watchlist-ownership-001)
- AP-002: Use standard frameworks for backend and frontend to minimize implementation risk. (Trace: DEC-tech-stack-001)
- AP-003: Prefer a free stock quote provider for the first release, with caching and graceful degradation for feed failures. (Trace: NFR-003)

## 5. Context and boundaries

- User-facing UI is a React single-page app with tabbed workspace (Research and Investment).
- **Research Workspace**: REST endpoints for watchlist lifecycle, heatmap aggregation, historical chart data, market movers, and news. (Trace: CONR-watchlist-api-001, CONR-heatmap-api-001, CONR-historical-chart-api-001, CONR-market-movers-api-001, CONR-news-api-001)
- **Investment Workspace**: REST endpoints for account management, holding CRUD, portfolio summary with live pricing, and CSV bulk import. (Trace: CONR-portfolio-api-001)
- PostgreSQL stores users, watchlists, items, accounts, holdings. Market data stored in separate finance-data-service instance.
- **Market Data Integration**: All market data fetched via HTTP delegation to shared finance-data-service (`/api/finance/*` endpoints):
  - Historical OHLCV: GET `/api/finance/history/{symbol}` (daily cache with delta-fetch)
  - Intraday bars: GET `/api/finance/intraday/{symbol}` (5-minute with market-aware staleness)
  - Live quotes: GET `/api/finance/quote?symbols={symbols}` (bulk endpoint)
  - Market movers: GET `/api/finance/movers/{gainers|losers}` (1-hour cache)
  - Company metadata: GET `/api/finance/metadata` (exchange data with sector/industry)
  - News: GET `/api/finance/news/{symbol}` (aggregated from RSS)
  - Indicators: GET `/api/finance/indicators/{symbol}` (SMA, EMA, RSI, etc.)
- Service adapters: `RemoteFinanceDataService` delegates all market data calls to finance-data-service. Graceful degradation on errors (202 Accepted with empty/cached results).
- No local market data caching; finance-data-service handles all caching strategy and delta-fetch logic.
- No trading execution, order submission, or real-time streaming is in scope.

## 6. Runtime and deployment (conceptual)

The backend can be deployed as a Spring Boot service in a containerized environment or managed Java runtime. The frontend is served as a static bundle, either from the same backend or a dedicated static hosting service.

## 7. Data and platform

- PostgreSQL is the primary persistent store:
  - **Research Workspace**: users, watchlists, watchlist_items, user-owned stock alerts.
  - **Investment Workspace**: investment_accounts (with account_type), holdings (with optional cost_basis, manual_price).
- **Market Data**: Fetched on-demand from finance-data-service via HTTP (no local market data persistence):
  - Historical OHLCV: `RemoteFinanceDataService.getHistory(symbol)` → GET `/api/finance/history/{symbol}`
  - Intraday bars: `RemoteFinanceDataService.getIntraday(symbol)` → GET `/api/finance/intraday/{symbol}`
  - Live quotes: `RemoteFinanceDataService.getQuotes(symbols)` → GET `/api/finance/quote?symbols={symbols}`
  - Market movers: `RemoteFinanceDataService.getMovers(type)` → GET `/api/finance/movers/{gainers|losers}`
  - Metadata: `RemoteFinanceDataService.getMetadata(symbol)` → GET `/api/finance/metadata?symbol={symbol}`
  - News: `RemoteFinanceDataService.getNews(symbol)` → GET `/api/finance/news/{symbol}`
  - Indicators: `RemoteFinanceDataService.getIndicators(symbol)` → GET `/api/finance/indicators/{symbol}`
- All remote calls handle errors gracefully: 202 Accepted with empty/partial results instead of 500 errors.
- Portfolio summary enriches holdings with live prices via remote quote endpoint; daily changes calculated from prior-day close (fetched from remote history endpoint).
- No sensitive financial data beyond user account metadata is stored.

## 8. Non-functional requirements

- The system must isolate user watchlists to prevent cross-user data access.
- The backend should be resilient to temporary data feed outages.
- The application should be maintainable with a standard Java + React stack.

## 9. Risks and open decisions

- DEC-001: ~~The chosen free stock data provider will affect available fields and cache behavior.~~ **Closed.** Yahoo Finance v8 chart API. See `DEC-tech-stack-001`.
- DEC-002: ~~Authentication approach for the first release needs confirmation.~~ **Closed.** JWT token-based. See `DEC-tech-stack-001`.
- DEC-003: ~~Which charting library for the widget dashboard?~~ **Closed.** Recharts for line charts; custom SVG squarify for heatmap treemap. See `DEC-chart-library-001`.

## 10. Traceability

- PRD: PRD-investor-development-platform-001
- Features: FEAT-investor-development-platform-001
- Decisions: DEC-tech-stack-001, DEC-chart-library-001
- Constraints: CON-user-registration-001, CON-watchlist-access-001, CON-heatmap-scope-001, CON-historical-data-range-001, CON-market-movers-public-001, CON-portfolio-access-001, CON-holding-uniqueness-001, CON-sector-cache-001, CON-csv-import-001
- Invariants: INV-watchlist-ownership-001, INV-heatmap-dedup-001, INV-historical-data-ownership-001, INV-portfolio-ownership-001, INV-holding-cost-basis-001, INV-manual-price-001, INV-sector-cache-001, INV-allocation-consolidation-001
- Contracts: CONR-user-api-001, CONR-watchlist-api-001, CONR-heatmap-api-001, CONR-historical-chart-api-001, CONR-market-movers-api-001, CONR-news-api-001, CONR-portfolio-api-001

## 11. Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-05-27 | draft | — | Initial architecture intent document. |
| 0.2 | 2026-05-27 | draft | — | Updated for three-widget dashboard; added stock_price_history persistence; closed open decisions; expanded traceability and boundaries. |
| 0.3 | 2026-05-28 | draft | — | Added Widget 4 (Market Movers); added MarketMoversService boundary; updated API list, constraints, and traceability; corrected freshness window to 1 day. |
| 0.4 | 2026-05-28 | draft | — | Added StockNewsService boundary (Yahoo Finance RSS); added persistent news panel in watchlist view; updated traceability, boundaries, and data/platform section. |
| 0.5 | 2026-05-31 | draft | 0.4 | Added Investment Workspace (portfolio management): account/holding CRUD, CSV import, sector caching, allocation dashboard. Introduced dual-workspace architecture (Research vs Investment). Updated summary, context/boundaries, data/platform, traceability. |
| 0.6 | 2026-06-01 | draft | 0.5 | Refreshed metadata and trace lists for portfolio constraints, invariants, and Investment Workspace intent. |
| 0.7 | 2026-06-07 | in_progress | 0.6 | **Phase I US-131**: Updated for shared finance-data-service. Removed all direct Yahoo Finance references. All market data (quotes, history, movers, news, metadata, indicators) delegated to `/api/finance/*` endpoints. Removed local stock_price_history cache. Added RemoteFinanceDataService adapter documentation. |
