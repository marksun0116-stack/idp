---
title: "Phase 9 Investment Workspace Core Requirements Review"
status: approved
reviewed_on: 2026-06-07
phase: 9
linked_plan: PLAN-investor-development-platform-001
linked_features:
  - FEAT-account-management-001
  - FEAT-holding-management-001
  - FEAT-portfolio-dashboard-001
linked_constraints:
  - CON-portfolio-access-001
  - CON-holding-uniqueness-001
linked_invariants:
  - INV-portfolio-ownership-001
  - INV-holding-cost-basis-001
  - INV-manual-price-001
  - INV-allocation-consolidation-001
related_contracts:
  - CONR-portfolio-api-001
---

# Phase 9 Investment Workspace Core Requirements Review

## 1. Scope

Phase 9 implements the core Investment Workspace backend and a compact authenticated UI surface for manually tracked portfolios.

Included:
- Owner-scoped investment account create, list, and delete.
- Owner-scoped holding create, update, and delete inside an owned account.
- Per-account duplicate-symbol rejection.
- Portfolio summary totals across accounts using manual prices first and live market data when available.
- Optional cost basis handling without calculation failures.
- Investment Workspace UI prioritizes the holdings table for repeat visits; lower-frequency account creation and holding-entry controls may be grouped behind a collapsible management panel.

Deferred:
- CSV import and conflict resolution.
- Sector cache refresh and manual sector override.
- Watchlist ownership badges.
- Full allocation charts.

## 2. Acceptance Criteria

| Area | Acceptance criteria | Trace |
| --- | --- | --- |
| Account ownership | A user can see and delete only their own investment accounts. Cross-user reads and deletes return no data or `404`. | CON-portfolio-access-001, INV-portfolio-ownership-001 |
| Account uniqueness | Duplicate account names for the same user return `409`; the same name may be used by another user. | FEAT-account-management-001 |
| Holding ownership | Holding writes resolve through an owned account; another user cannot add, update, or delete holdings in that account. | INV-portfolio-ownership-001 |
| Holding uniqueness | A duplicate symbol in the same account returns `409`; symbols normalize to uppercase. | CON-holding-uniqueness-001 |
| Cost basis | `costBasis` may be null. Gain fields are null when either cost basis or price is unavailable. | INV-holding-cost-basis-001 |
| Manual price | `manualPrice` takes priority over market data and is reflected in value/gain calculations. | INV-manual-price-001 |
| Summary | `GET /api/portfolio/summary` returns account totals plus aggregate total value, total cost, total gain, total gain percent, daily gain, and daily gain percent. | CONR-portfolio-api-001, FEAT-portfolio-dashboard-001 |
| Holdings table | The holdings table shows market value, daily gain/loss with percentage, and total gain/loss with percentage so repeated portfolio checks focus on current holdings. | FEAT-portfolio-dashboard-001 |

## 3. Implementation Notes

- Use existing bearer subject authentication behavior from Phase 7 for owner IDs.
- Keep account and holding persistence in the current Spring Data JPA style.
- Return `404` for cross-user account and holding access to avoid revealing resource existence.
- Use the existing `MarketDataService` adapter for live quotes and gracefully leave price-derived fields null if provider data is unavailable.

## 4. Test Plan

- `PortfolioAccountTest`: account create/list/delete, owner isolation, duplicate-name conflict, and cascade delete.
- `PortfolioHoldingTest`: holding CRUD, duplicate-symbol conflict, owner isolation, optional cost basis, and manual price priority.
- `PortfolioSummaryTest`: aggregate totals, per-account totals, null-safe gain calculations, and market-data-unavailable behavior.

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-07 | approved | - | Initial Phase 9 requirements review for Investment Workspace core. |
| 0.2 | 2026-06-07 | approved | 0.1 | Clarified Investment Workspace UI priority: holdings first, account/holding controls grouped in a collapsible management panel, and gain/loss percentages visible in the holdings table. |
