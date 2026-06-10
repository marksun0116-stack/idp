---
title: "Phase 10 Advanced Technical Analysis Requirements Review"
status: in_progress
reviewed_on: 2026-06-09
phase: 10
linked_plan: PLAN-investor-development-platform-001
linked_features:
  - FEAT-finance-technical-indicators-001
  - FEAT-finance-pattern-matching-001
linked_constraints:
  - CON-strategy-analysis-scope-001
linked_invariants:
  - INV-strategy-validation-001
  - INV-indicator-accuracy-001
related_contracts:
  - CONR-technical-analysis-api-001
  - CONR-finance-service-api-001
---

# Phase 10 Advanced Technical Analysis Requirements Review

## 1. Scope

Phase 10 integrates finance-data-service technical analysis into IDP strategy research UI. The shared finance service provides all indicator calculations and recommendation engine; IDP Phase 10 focuses on **frontend display and strategy-scoped integration**.

**Included:**
- Consume finance-data-service `/api/finance/analysis/{symbol}` endpoint
- Display technical indicators (SMA20/50, EMA12/26, RSI, MACD, Stochastic, Bollinger, OBV, MFI) in strategy symbol charts
- Display recommendation (regime, strategy pattern, confidence) in strategy expand panel
- Show similar historical setups with win rates and forward returns
- Add technical analysis widgets to strategy dashboard
- Handle unavailable/fallback scenarios gracefully

**Deferred:**
- Backend technical indicator implementation (owned by finance-data-service)
- Backend pattern matching (owned by finance-data-service)
- Change detection / alert notifications (post-Phase 10)
- Custom indicator creation

## 2. Architecture & Dependencies

**Data Flow:**
```
IDP Strategy UI
  ↓ (GET /api/strategies/{id}/analysis/{symbol}?range=1y)
IDP Backend Proxy (optional passthrough or lightweight transform)
  ↓
finance-data-service (CONR-finance-service-api-001)
  ↓ (GET /api/finance/analysis/{symbol}?range=1y)
Yahoo Finance (OHLCV data + indicators + analysis)
```

**Key Dependencies:**
- finance-data-service must be running (localhost:8082 by default)
- `/api/finance/analysis/{symbol}` endpoint available and responsive
- CONR-finance-service-api-001 contract stability (version 1.0, final)

## 3. Implementation Approach

Phase 10 will be split into **4 sprints** (frontend-focused):

### Sprint 1: Requirements Review & UI Contract Definition
- Document expected UI display layout (expand panel mockup)
- Define error/fallback UX (network errors, unavailable symbols, insufficient data)
- Map finance-data-service response to strategy UI state shape
- Test API connectivity and response format

### Sprint 2: Expand Panel Enhancement
- Add indicator chart selector (SMA20, RSI, MACD, etc.)
- Display recommendation card (regime, confidence, win rate)
- Show scorecard with signal alignment per regime
- Display similar historical setups table (top 10, date/outcome/return)

### Sprint 3: Dashboard Integration
- Add "Technical Outlook" widget to strategy dashboard
- Show current regime and confidence for top 3 strategies
- Add quick recommendation summary (bullish/bearish/neutral)
- Link to full expand panel for details

### Sprint 4: Polish & Edge Cases
- Performance optimization for large indicator datasets
- Graceful degradation when analysis unavailable
- Consistent styling with stock-monitor / existing IDP UI
- Error messaging and retry logic

## 4. Acceptance Criteria

| Area | Acceptance Criteria | Trace |
| --- | --- | --- |
| API integration | IDP can successfully fetch and parse `/api/finance/analysis/{symbol}` responses | CONR-finance-service-api-001 |
| Indicator display | Selected indicators render in symbol chart (SMA20, RSI, MACD, Bollinger) | FEAT-finance-technical-indicators-001, INV-indicator-accuracy-001 |
| Recommendation display | Expand panel shows regime, confidence, win rate, and similar setups clearly | CONR-technical-analysis-api-001 |
| Fallback behavior | UI gracefully handles network errors, unavailable data, insufficient history | CON-strategy-analysis-scope-001 |
| Data freshness | Indicators refresh when strategy quote data is refreshed | CON-strategy-analysis-scope-001 |
| Dashboard widget | Strategy dashboard shows technical outlook (regime + top 3 strategies) | FEAT-investor-development-platform-001 |
| Error messages | Users understand why analysis is unavailable (insufficient data, network, symbol) | CON-investment-non-advice-001 |

## 5. Implementation Notes

- **No backend implementation**: All technical analysis is in finance-data-service (FEAT-finance-technical-indicators-001, FEAT-finance-pattern-matching-001)
- **IDP responsibilities**: Fetch from finance-data-service, transform/adapt for strategy UI, display in expand panel + dashboard
- Expand panel should use side-by-side layout: chart on left, recommendation/similar setups on right (consistent with stock-monitor)
- Similar setups table limited to top 10 most recent matches (ranked by date, most recent first)
- Network timeouts handled gracefully: show "Loading analysis..." then "Analysis unavailable" after 10s timeout
- Finance-data-service handles insufficient historical data (returns 422 Unprocessable Entity); IDP shows helpful message

## 6. Test Plan

- **Integration tests**: Verify IDP can fetch and parse finance-data-service responses for multiple symbols and ranges
- **UI tests**: Expand panel renders correctly with indicators, recommendation, and similar setups
- **Fallback tests**: Network errors, timeouts, missing symbols, insufficient history all handled gracefully
- **Performance tests**: Dashboard with 5+ strategies loads in under 2s

## 7. Cross-Repo Dependencies

- **Dependency**: finance-data-service `CONR-finance-service-api-001` v1.0 (stable, final)
- **Owned by**: finance-data-service (`FEAT-finance-technical-indicators-001`, `FEAT-finance-pattern-matching-001`)
- **IDP contributes**: Strategy UI integration, expand panel display, dashboard widget
- Synchronization: IDP and finance-data-service share `FEAT-shared-finance-service-001` from IDP workspace

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-07 | in_progress | - | Initial Phase 10 requirements review. Defined 5-sprint approach to port stock-monitor technical analysis. |
| 0.2 | 2026-06-09 | in_progress | 0.1 | Refactored to consumer contract: IDP integrates (not implements) finance-data-service technical analysis. Changed from 5 backend sprints to 4 frontend sprints. |
