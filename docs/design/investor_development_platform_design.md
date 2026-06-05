---
design_id: DES-investor-development-platform-001
title: "Investor Development Platform High-Level Design"
status: draft
owner: "Engineering"
architect: "TBD"
last_updated: 2026-06-02
version: "1.0"
linked_prds:
  - PRD-investor-development-platform-001
linked_features:
  - FEAT-investor-development-platform-001
linked_constraints:
  - CON-user-registration-001
  - CON-watchlist-access-001
  - CON-heatmap-scope-001
  - CON-historical-data-range-001
  - CON-market-movers-public-001
  - CON-technical-signals-001
  - CON-portfolio-access-001
  - CON-holding-uniqueness-001
  - CON-sector-cache-001
  - CON-csv-import-001
linked_invariants:
  - INV-watchlist-ownership-001
  - INV-heatmap-dedup-001
  - INV-historical-data-ownership-001
  - INV-portfolio-ownership-001
  - INV-holding-cost-basis-001
  - INV-manual-price-001
  - INV-sector-cache-001
  - INV-allocation-consolidation-001
linked_decisions:
  - DEC-tech-stack-001
  - DEC-chart-library-001
related_contracts:
  - CONR-user-api-001
  - CONR-watchlist-api-001
  - CONR-heatmap-api-001
  - CONR-historical-chart-api-001
  - CONR-market-movers-api-001
  - CONR-news-api-001
  - CONR-portfolio-api-001
slices:
  - "research-workspace"
  - "decision-journal"
  - "review-engine"
  - "behavioral-analytics"
  - "strategy-portfolios"
  - "investor-reputation"
---

# Investor Development Platform High-Level Design

## 1. Summary

This document describes the high-level design for the Investor Development Platform, including component boundaries, data model, API contracts, the Research Workspace widget dashboard, and the Investment Workspace portfolio dashboard.

## 1.1 UX and wireframe requirements

The wireframe and UX requirements are captured in `docs/design/investor_development_platform_wireframe_spec.md` and are based on the dashboard asset at `docs/design/assets/idp.png`.

## 2. System components

- **React frontend**: registration/login, watchlist management sidebar, Research Workspace widget dashboard, and Investment Workspace portfolio dashboard. Uses Recharts for visualization.
- **Spring Boot backend**: REST APIs for users, watchlists, heatmap aggregation, historical data, market movers, news, accounts, holdings, portfolio summaries, and sector cache management. Validates ownership on every user-scoped request.
- **PostgreSQL database**: stores users, watchlists, watchlist items, investment accounts, holdings, stock metadata, and up to 5 years of daily close prices per symbol (`stock_price_history`).
- **Stock quote adapter** (`StockQuoteService`): isolated behind an interface. Fetches current quotes from Yahoo Finance v8 chart API using CompletableFuture for parallel symbol fetches and keeps an in-memory last-known quote cache to avoid repeated closed-market provider calls.
- **Historical data service** (`StockHistoryService`): serves daily close series from the local PostgreSQL cache. On cold start (symbol absent), fetches 5 years from Yahoo Finance. On warm start, fetches only the delta since the last stored date. Refreshes if the most recent row is more than 1 day old.
- **Market movers adapter** (`MarketMoversService`): fetches ranked lists from the Yahoo Finance screener API. No local persistence; data is fetched on each request. Isolated behind an interface.
- **Stock news service** (`StockNewsService`): fetches RSS feeds from Yahoo Finance for one or more symbols in parallel. No local persistence; up to 20 articles per symbol. Uses `HttpURLConnection` + DOM `DocumentBuilder` (avoids content-type issues with `application/rss+xml`).

## 3. Data model

- `User`: id, username, encoded password, created timestamp.
- `Watchlist`: id, user_id, name, created timestamp.
- `WatchlistItem`: id, watchlist_id, symbol, created timestamp.
- `InvestmentAccount`: id, user_id, name, account_type, created timestamp.
- `Holding`: id, account_id, symbol, shares, optional cost_basis, optional manual_price, optional purchase_date, created timestamp.
- `StockMetadata`: symbol, sector, industry, stale tracking for sector cache.
- `StockPriceHistory`: id, symbol, price_date (DATE), close (DOUBLE PRECISION), created_at. Unique constraint on `(symbol, price_date)`. Index on `(symbol, price_date)` for range queries.

Live quotes are fetched on demand and not persisted. The backend keeps a process-local last-known quote cache so repeated closed-market requests can return the most recent known quote without re-querying Yahoo Finance.

## 4. Widget architecture

The frontend is organized as a widget dashboard. Each widget is a self-contained React component that fetches its own data and manages its own loading/error state.

| Widget | Route / trigger | Data source |
| --- | --- | --- |
| Widget 1 — Live quote table | Selecting a watchlist in the sidebar | `GET /api/watchlists/{id}/stocks`, `GET /api/watchlists/{id}/insights` |
| Widget 2 — Heatmap | Selecting "Heatmap" in the top nav | `GET /api/heatmap` |
| Widget 3 — Comparison chart | Selecting a watchlist + "Chart" tab | `GET /api/watchlists/{id}/history?range=` |
| Indicators tab | Selecting a watchlist + "Indicators" tab | `GET /api/watchlists/{id}/history?range=` |
| Widget 4 — Market Movers | Selecting "Movers" in top nav; tab switch | `GET /api/market-movers?tab=` (5 tabs) |
| News panel (watchlist) | On watchlist select; symbol click for filter | `GET /api/watchlists/{id}/news?symbol=` |
| News panel (movers) | On view entry / tab switch (all symbols); row click (single symbol) | `GET /api/market-movers/news?symbol=` |

## 5. API contracts

| Contract | Endpoint | Auth |
| --- | --- | --- |
| CONR-user-api-001 | `POST /api/users/register`, `POST /api/users/login` | Public |
| CONR-watchlist-api-001 | `GET/POST/DELETE /api/watchlists`, `GET/POST/DELETE /api/watchlists/{id}/stocks` | JWT |
| CONR-heatmap-api-001 | `GET /api/heatmap` | JWT |
| CONR-historical-chart-api-001 | `GET /api/watchlists/{id}/history?range=` | JWT |
| CONR-market-movers-api-001 | `GET /api/market-movers?tab=`, `GET /api/market-movers/news?symbol=` | JWT |
| CONR-news-api-001 | `GET /api/watchlists/{id}/news?symbol=` | JWT |
| CONR-portfolio-api-001 | `GET /api/portfolio/summary`, account/holding CRUD, sector cache endpoints | JWT |

Full schemas are defined in the respective `knowledge/contracts/` files.

## 6. Authentication and access control

- Spring Security guards all `/api/**` endpoints except register and login.
- Every watchlist-scoped endpoint (including heatmap and historical chart) resolves the authenticated user from the JWT and filters data to that user's watchlists.
- The heatmap endpoint collects all watchlists for the user, deduplicates symbols, then fetches quotes; no cross-user symbol leakage is possible.

## 7. Stock quote adapter (Widget 1 and Heatmap)

- Endpoint: `https://query2.finance.yahoo.com/v8/finance/chart/{symbol}?interval=1d&range=1d&includePrePost=true`
- Requires `User-Agent` header (Chrome browser string) to avoid 401.
- One HTTP request per symbol, executed in parallel via `CompletableFuture` with a fixed thread pool of 8.
- Fields extracted from `meta` node: `regularMarketPrice`, `regularMarketTime`, `chartPreviousClose`, `preMarketPrice`, `preMarketTime`, `postMarketPrice`, `postMarketTime`. Change is computed as `price − previousClose`.
- Market state (`REGULAR`, `PRE`, `POST`, `CLOSED`) is derived by comparing current time against `currentTradingPeriod` window boundaries.
- The watchlist DTO forwards extended-hours fields; the frontend shows pre-market or after-hours price only when the matching market state and price are present.
- The watchlist insights DTO forwards average volume, optional earnings date, RSI(14), and a compact technical trend verdict. Average volume, RSI, and trend verdicts are derived from local `stock_price_history`; the backend does not call Yahoo `quoteSummary` because unauthenticated access can fail with crumb/cookie authorization errors. The Quotes table uses the verdict only when `trendSignificant` is true and confidence is `Medium` or `High`, with bullish and bearish badge colors; RSI remains available for alert evaluation but is not rendered as the primary row signal.
- Quotes auto-refresh is market-aware. The frontend starts the 30-second timer only when at least one visible quote row is in `PRE`, `REGULAR`, `POST`, or `POSTPOST`; all-`CLOSED` quote tables keep their existing rows and do not issue timer refreshes.
- When the backend has a cached `CLOSED` quote and local US equity extended-hours windows are closed, `StockQuoteServiceImpl` returns the cached quote instead of calling Yahoo Finance again.

## 8. Historical data service (Widget 3)

**Cache strategy (`StockHistoryServiceImpl`)**:
- All daily closes are stored in `stock_price_history`. Reads always go to the DB; writes go to DB first.
- Cold start (no rows for symbol): fetch 5 years (`MAX_HISTORY_YEARS = 5`) using `period1`/`period2` Unix timestamps.
- Warm start: fetch only missing dates from `lastStoredDate + 1` to today. Skip if `lastStoredDate >= today - 1 day` (freshness window).
- Normal startup/history requests do not re-fetch existing cached dates for OHLCV backfill; existing rows are treated as authoritative once cached.
- Symbols are refreshed in parallel via `CompletableFuture` with an 8-thread `ExecutorService`.
- Batch insert uses `ON CONFLICT (symbol, price_date) DO NOTHING` to handle concurrent refreshes safely.

**Yahoo Finance call**:
- URL: `https://query2.finance.yahoo.com/v8/finance/chart/{symbol}?interval=1d&period1={p1}&period2={p2}`
- `User-Agent` header (Chrome browser string) required to avoid 401.
- Close prices extracted from `chart.result[0].indicators.quote[0].close[]`; timestamps from `chart.result[0].timestamp[]`.

**Allowed range values**: `1w`, `1mo`, `3mo`, `6mo`, `1y`, `2y`, `3y`, `4y`, `5y`. Any other value returns HTTP 400.

**Frontend normalization**: `percentChange = (close / close[0] - 1) * 100` — series starts at 0%, not 100.

## 9. Heatmap backend (Widget 2)

1. Load all watchlists for the authenticated user.
2. Collect all `WatchlistItem` symbols; build a map of `symbol → [watchlistNames]` for deduplication.
3. Fetch current quotes for the deduplicated symbol set via `StockQuoteService.getLatestQuotes`.
4. Return `{ stocks: [{ symbol, lastPrice, change, percentChange, marketState, watchlists[] }] }`.

## 10. Frontend widget detail

### Widget 2 — Heatmap
- Squarified treemap rendered as an SVG using a pure-JS implementation of the Bruls-Huizing-van Wijk algorithm.
- Cell size is proportional to `|percentChange|` (minimum size prevents invisible cells); layout recalculates on container resize via `ResizeObserver`.
- Color scale is **dynamic**: `maxAbs = max(|percentChange|)` across all cells in the current view. Each cell's saturation = `sqrt(|percentChange| / maxAbs)`, giving a sqrt curve for mid-range visibility. Neutral grey for null data.
- Period selector (Today / This Week / This Month) switches the data source: Today uses `StockQuoteService`; Week and Month use `StockHistoryService` and compute `(last - first) / first * 100`.
- Floating tooltip on hover shows symbol, last price, change, change %, and watchlist membership.

### Widget 3 — Comparison chart
- Recharts `LineChart` with `ResponsiveContainer`.
- One `Line` per symbol; colors from a fixed palette.
- All series normalized to 0% at the first close in the range: `(close / close[0] - 1) * 100`.
- Y-axis shows percentage change with sign (e.g. `+12%`, `-5%`); domain forced to include 0 with ±2% padding.
- `ReferenceLine` at y=0 as the performance baseline.
- X-axis uses precomputed month-boundary ticks thinned by a range-specific skip factor; labels include the year when it changes.
- Range toggle (1W / 1M / 3M / 6M / 1Y / 2Y / 3Y / 4Y / 5Y) triggers a fresh API call.
- `Legend` allows click-to-hide individual series.

### Indicators tab — validated trend recommendation
- The Indicators tab reuses the historical chart endpoint and filters the returned series to one selected symbol.
- Client-side calculations derive SMA 20, SMA 50, Bollinger Bands, RSI 14, EMA 12/26, MACD, Stochastic, OBV, and MFI from daily OHLCV data where available.
- The recommendation engine scores every eligible historical bar in the selected range using the same formula as the current bar. Inputs include moving-average alignment and slope, EMA/MACD alignment, RSI momentum, OBV/MFI momentum, and Bollinger context.
- The current setup is compared with prior bars that share overlapping directional indicator inputs. Similar setups are validated with 20-trading-day forward returns.
- The recommendation panel displays a conservative verdict (`Bullish Trend`, `Bearish Trend`, `Mixed Trend`, or `No Validated Edge`), confidence, sample size, win rate, median forward return, and contributing indicators. The sample-size metric includes an inline information tooltip explaining similar historical setup matching. It avoids buy/sell order language and is not a trading execution recommendation.
- If there are fewer than five similar prior setups or historical outcomes conflict with the current setup, the UI shows `No Validated Edge` while keeping charts visible.

## 11. Stock news service

**Yahoo Finance RSS API**:
- URL: `https://feeds.finance.yahoo.com/rss/2.0/headline?s={symbol}&region=US&lang=en-US`
- Returns up to 20 items per symbol (fixed RSS limit; no count parameter).
- `User-Agent` header (Chrome browser string) required.
- Date format: `EEE, dd MMM yyyy HH:mm:ss Z` (Locale.ENGLISH).

**Backend service** (`StockNewsService` → `StockNewsServiceImpl`):
- Uses `HttpURLConnection` directly; avoids Spring `RestTemplate` because Yahoo returns `application/rss+xml` which `StringHttpMessageConverter` does not support.
- DOM parsing via `DocumentBuilderFactory` + `DocumentBuilder`.
- Symbols fetched in parallel via `CompletableFuture` with 8-thread `ExecutorService`.
- Results merged and sorted by `publishedAt` descending.
- Each `NewsItem` DTO: `symbol`, `title`, `url`, `publishedAt` (Unix epoch seconds), `source`, `summary` (HTML-stripped, truncated to 220 chars).
- Fetch errors per symbol are swallowed (`log.warn`); partial results are returned.

**Endpoints**:
- `GET /api/watchlists/{id}/news?symbol=` — ownership-checked; returns news for all watchlist symbols or a single symbol if `?symbol=` is provided. Controller: `NewsController`.
- `GET /api/market-movers/news?symbol=` — public-adjacent (JWT required); returns news for any single symbol. Controller: `MarketMoversController`.

**Frontend news panel (watchlist view)**:
- The watchlist content area switches to a two-pane flex layout when a watchlist is selected: left pane (flex 2, ~2/3) holds the tab bar, Quotes table, and Chart; right pane (flex 1, ~1/3) holds the persistent news panel.
- On watchlist selection, `loadNews(watchlistId)` is called immediately in the background to populate the right pane.
- Clicking a `<span class="sym-clickable">` in the Quotes table calls `loadNews(watchlistId, symbol)` and highlights the selected symbol.
- The news header shows either "All stocks" or a colored symbol badge; a clear button (×) restores all-symbol view.
- News panel scrolls independently via `.wl-news-scroll { flex: 1; overflow-y: auto }`.
- The `content-split` CSS class (applied to `<main>` when a watchlist is selected) sets `overflow: hidden; padding: 0` so the two-panel layout fills the full browser height.

## 12. Market movers adapter (Widget 4)

**Yahoo Finance screener API**:
- URL: `https://query2.finance.yahoo.com/v1/finance/screener/predefined/saved?scrIds={scrId}&count=25`
- scrId mapping: `active → most_actives`, `gainers → day_gainers`, `losers → day_losers`, `smallcap → small_cap_gainers`, `aggressive → aggressive_small_caps`
- Most Active, Gainers, Losers are returned in Yahoo's default order. Small Cap and Aggressive are sorted by `percentChange` descending in `MarketMoversServiceImpl` after fetching.
- `User-Agent` header (same Chrome browser string as the quote adapter) is required.
- Fields extracted from `finance.result[0].quotes[]`: `symbol`, `shortName` (→ companyName), `regularMarketPrice`, `regularMarketChange`, `regularMarketChangePercent`, `regularMarketVolume`.

**Backend service**: `MarketMoversService` interface with `MarketMoversServiceImpl`. No DB persistence. Returns a list of `MarketMoverEntry` (record) DTOs. `SORT_BY_PCT_DESC = {"smallcap", "aggressive"}` controls post-fetch sorting.

**Frontend layout**: Uses shared `wl-layout` (flex 2 list + flex 1 news panel), identical DOM structure to the watchlist view.

**Frontend news behavior**: On view entry and tab switch, news for all visible stock symbols is fetched in parallel via `Promise.all` against `/api/market-movers/news?symbol=` and merged by `publishedAt` descending. Clicking a row filters to that symbol; clear button restores all-symbol view.

**Frontend add-to-watchlist flow**:
1. "+" button is shown only when the stock is not already in all of the user's watchlists (checked via `w.stocks` on the watchlist objects from `GET /api/watchlists`). A ✓ badge is shown when the stock is in all watchlists.
2. Dropdown lists only watchlists that do not already contain the symbol.
3. Selecting a watchlist calls `POST /api/watchlists/{id}/stocks` with the symbol.
4. Success: the sidebar watchlist refreshes if that watchlist is currently selected.

## 13. Non-functional concerns

- All external data fetches are parallel and bounded (fixed thread pool).
- Ownership rules are enforced at the backend query level, not only at the controller layer.
- Provider-specific logic is isolated in `StockQuoteServiceImpl`, `StockHistoryServiceImpl`, and `MarketMoversServiceImpl` behind interfaces for future replacement.
- Frontend error boundaries per widget prevent one widget failure from crashing the dashboard.

## 13. Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-05-27 | draft | — | Initial high-level design. |
| 0.2 | 2026-05-27 | draft | — | Added widget architecture, heatmap and historical chart adapter designs, updated component list and API table. |
| 0.3 | 2026-05-27 | draft | — | Added stock_price_history to data model; corrected historical adapter to describe local cache strategy (period1/period2, cold/warm start, delta refresh); added 2Y–5Y; corrected Widget 2 to squarified treemap with dynamic color scale and period selector; corrected Widget 3 to 0%-based normalization with reference line. |
| 0.4 | 2026-05-28 | draft | — | Added Widget 4 (Market Movers panel): MarketMoversService, screener API adapter, add-to-watchlist flow; updated component list, widget table, API contracts table; renumbered sections. |
| 0.5 | 2026-05-28 | draft | — | Added Stock News Service (§11): StockNewsServiceImpl, Yahoo Finance RSS adapter, NewsController, two-panel watchlist layout with persistent news panel, Market Movers news pane; updated component list, widget table, API contracts table; renumbered Non-functional section to §13. |
| 0.6 | 2026-05-28 | draft | — | Updated §12: added Small Cap (small_cap_gainers) and Aggressive (aggressive_small_caps) tabs; backend sort by percentChange desc for those tabs; updated frontend news behavior (all-symbol on load/switch, row-click filter); updated "+" availability logic (hide when in all watchlists, ✓ badge); updated widget table. |
| 0.7 | 2026-06-01 | draft | 0.6 | Added Investment Workspace design trace: portfolio FEAT/CON/INV/CONR links, account and holding data model, portfolio API contract, and portfolio dashboard scope. |
| 0.8 | 2026-06-02 | draft | 0.7 | Added Indicators tab design for validated trend recommendations computed from historical watchlist data. |
| 0.9 | 2026-06-02 | draft | 0.8 | Added similar-setups tooltip behavior to the Indicators recommendation panel. |
| 1.0 | 2026-06-02 | draft | 0.9 | Removed analyst target price from Quotes path and documented extended-hours quote extraction. |
| 1.1 | 2026-06-02 | draft | 1.0 | Tightened daily history cache refresh to query Yahoo only for cold symbols or missing dates after the latest cached date. |
| 1.2 | 2026-06-02 | draft | 1.1 | Added market-aware Quotes auto-refresh and backend closed-market quote cache behavior. |
| 1.3 | 2026-06-04 | draft | 1.2 | Changed Quote row technical signal display to use significant Medium-or-higher confidence trend verdict badges from the insights endpoint. Removed Yahoo quoteSummary from insights and compute average volume from local history. |
