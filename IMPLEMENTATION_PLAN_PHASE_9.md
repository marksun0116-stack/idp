# Phase 9: Market Research Surface & Portfolio Integration

**Plan ID:** PLAN-investor-development-platform-002  
**Status:** draft  
**Owner:** Investor Development Platform Team  
**Last Updated:** 2026-06-05  
**Version:** 1.0

---

## Executive Summary

The IDP MVP has **complete backend implementation** (20+ APIs, full business logic, comprehensive tests) but the **UI research surface is placeholder-based**. This phase completes the investment decision workflow by implementing professional market research features: quotes, charts, technical indicators, and news that match the wireframe and PRD requirements.

**Goal:** Transform portfolio/watchlist from data-only to data + research context, enabling investors to make informed decisions without leaving the application.

---

## Current State

### What Works ✅
- User authentication and workspace isolation
- Decision journal (create, update, close, archive)
- Automated review scheduling (30d/90d/180d/1y)
- DQS scoring with explainable components
- Behavioral analytics (research discipline, risk management, FOMO)
- Strategy portfolio creation and management
- Tracked-only symbols (watchlist within portfolio)
- Public profiles with privacy controls
- All market data APIs (quotes, charts, indicators)

### What's Missing ❌
- **Quote Display:** Prices only shown as text placeholders
- **Chart Visualization:** No price history charts at all
- **Indicator Display:** Text-only verdict without visual data
- **News Integration:** No news feed or research articles
- **Portfolio Context:** Can't see P&L, allocations, or performance
- **Research Workflow:** No way to use market data while deciding

---

## Phase Goals

### Primary Goals
1. ✅ Display current quotes with prices, volume, change % for all tracked symbols
2. ✅ Show interactive price charts with time range selection (1D-ALL)
3. ✅ Display technical indicators (RSI, MACD, moving averages) with signals
4. ✅ Integrate news feed for tracked symbols
5. ✅ Show portfolio performance (value, P&L, allocations)

### Secondary Goals
1. ⚠️ Real-time price updates (if WebSocket support added)
2. ⚠️ Custom alerts for price/indicator signals
3. ⚠️ Watchlist-specific UI (separate from positions)
4. ⚠️ Market context (indices, sector comparisons)

### Non-Goals
1. Full portfolio rebalancing calculator
2. Tax-loss harvesting tools
3. Backtesting engine
4. AI-generated recommendations

---

## User Stories & Requirements

### US-P9-001: Quote Display
**User Journey:**  
> "As an investor, I want to see current prices, volume, and change % for all tracked symbols so I understand the current market context for my decision."

**Acceptance Criteria:**
- Display symbol name, current price, price change ($), change percentage
- Color code (green for +, red for -)
- Show 52-week high/low, trading volume
- Display data freshness timestamp
- Support refresh button
- Responsive grid layout (desktop and mobile)

**API Used:** `GET /api/strategies/{id}/quotes`  
**Frontend Components:** `SymbolQuoteGrid`, `SymbolCard`  
**Dependencies:** None  
**Trace:** FR-007, US-005  

---

### US-P9-002: Historical Price Charts
**User Journey:**  
> "As an investor, I want to see historical price charts for my strategy symbols with different time ranges so I can understand entry points and price trends."

**Acceptance Criteria:**
- Interactive candlestick or line chart
- Time range selection: 1D, 1W, 1M, 3M, YTD, 1Y, ALL
- Show volume bars below price
- Tooltip with price, date, volume on hover
- Support zoom/pan (optional)
- Refresh capability
- Mobile-responsive

**API Used:** `GET /api/strategies/{id}/charts?range={range}`  
**Frontend Components:** `PriceChart`, `ChartControls`  
**Dependencies:** Recharts library  
**Trace:** FR-007, US-005  

---

### US-P9-003: Technical Indicators Display
**User Journey:**  
> "As an investor, I want to see technical indicators (RSI, MACD, moving averages) and buy/sell signals so I can use technical analysis in my trading decisions."

**Acceptance Criteria:**
- Display RSI with overbought/oversold levels
- Show MACD with signal line and histogram
- Display moving averages (50, 200)
- Show Bollinger Bands
- Display buy/sell signal counts
- Show trend verdict (bullish/bearish)
- Color code overbought/oversold conditions
- Mobile-responsive layout

**API Used:** `GET /api/strategies/{id}/indicators`  
**Frontend Components:** `IndicatorPanel`, `RSIIndicator`, `MACDIndicator`, etc.  
**Dependencies:** None  
**Trace:** FR-007, US-005  

---

### US-P9-004: News Feed Integration
**User Journey:**  
> "As an investor, I want to see recent news and research articles for my tracked symbols so I can stay informed about market developments relevant to my decisions."

**Acceptance Criteria:**
- Show news articles for tracked symbols
- Display title, source, date, summary
- Link to full articles
- Filter by symbol or date
- Show sentiment (positive/negative/neutral)
- Newest articles first
- Mobile-responsive cards

**API Used:** `GET /api/symbols/{symbol}/news` (NEW - needs implementation)  
**Frontend Components:** `NewsPanel`, `NewsCard`, `NewsFilter`  
**Dependencies:** Backend news endpoint  
**Trace:** FR-007, Implied in research requirements  

---

### US-P9-005: Portfolio Performance Summary
**User Journey:**  
> "As an investor, I want to see my portfolio's total value, gain/loss, and allocations so I understand my overall position and performance."

**Acceptance Criteria:**
- Display total portfolio value
- Show invested capital vs current value
- Display unrealized gain/loss ($ and %)
- Show today's change ($ and %)
- Display allocation by symbol (%)
- Show cost basis per position
- Performance since inception

**API Used:** Strategy API, Quotes API (combines data)  
**Frontend Components:** `PortfolioSummary`, `AllocationChart`  
**Dependencies:** None  
**Trace:** FR-005 (portfolio tracking)  

---

## Implementation Breakdown

### Phase 9.1: Quotes Display (Sprint 1)

#### Components to Build
```
SymbolQuoteGrid          - Main grid showing all symbols
  ├─ SymbolCard         - Individual symbol with prices
  ├─ PriceCell          - Current price with color
  ├─ ChangeCell         - Price change % with indicator
  └─ QuoteActions       - Quick actions (watch/trade/alert)
```

#### Tasks
1. Create SymbolQuoteGrid component
2. Implement SymbolCard with responsive layout
3. Add price formatting and color coding
4. Implement refresh functionality
5. Add error handling for missing data
6. Test with real API data
7. Mobile responsive design

#### Acceptance Criteria Met
- ✅ All symbols with quotes visible
- ✅ Price, volume, change % displayed
- ✅ Data refreshes on demand
- ✅ Responsive on all devices

---

### Phase 9.2: Chart Visualization (Sprint 2-3)

#### Components to Build
```
PriceChart              - Interactive candlestick/line chart
  ├─ ChartControls     - Time range selector
  ├─ ChartCanvas       - Recharts integration
  ├─ VolumeBar         - Volume visualization
  └─ ChartTooltip      - Interactive info
```

#### Tasks
1. Install Recharts library
2. Create ChartControls component (1D/1W/1M/3M/YTD/1Y/ALL)
3. Implement PriceChart with Recharts ComposedChart
4. Add volume bars
5. Implement tooltips on hover
6. Handle data loading states
7. Add zoom/pan (optional)
8. Test with different time ranges

#### Acceptance Criteria Met
- ✅ Charts visible for all time ranges
- ✅ Interactive tooltips
- ✅ Volume bars displayed
- ✅ Smooth transitions between ranges

---

### Phase 9.3: Indicator Display (Sprint 3-4)

#### Components to Build
```
IndicatorPanel          - Main indicator container
  ├─ RSIIndicator      - RSI gauge with levels
  ├─ MACDIndicator     - MACD chart + signal
  ├─ MAIndicator       - Moving average display
  ├─ BBIndicator       - Bollinger Bands
  └─ TrendSummary      - Buy/sell signals + verdict
```

#### Tasks
1. Create IndicatorPanel component
2. Implement RSIIndicator with color zones
3. Implement MACDIndicator with histogram
4. Implement MAIndicator (50, 200 comparison)
5. Implement BBIndicator with bands visualization
6. Add trend verdict display
7. Add signal counts (buy/sell)
8. Test with real indicator data

#### Acceptance Criteria Met
- ✅ All indicators visible and readable
- ✅ Overbought/oversold warnings
- ✅ Buy/sell signal counts displayed
- ✅ Trend verdict clear

---

### Phase 9.4: News Integration (Sprint 4-5)

#### Backend Work Required
```
POST /api/news/configure     - Set data sources
GET /api/symbols/{symbol}/news - Fetch articles
GET /api/strategies/{id}/news  - Strategy news
```

#### Components to Build
```
NewsPanel              - Main news feed
  ├─ NewsFilter       - Symbol/date filter
  ├─ NewsCard         - Individual article
  ├─ NewsSource       - Source attribution
  └─ SentimentBadge   - Positive/negative/neutral
```

#### Tasks
1. Create backend news endpoint
2. Create NewsPanel component
3. Implement NewsCard layout
4. Implement NewsFilter (by symbol, date)
5. Add sentiment color coding
6. Add article links
7. Implement pagination (load more)
8. Test with mock news data

#### Acceptance Criteria Met
- ✅ News articles visible for symbols
- ✅ Filter functionality works
- ✅ Links to articles functional
- ✅ Sentiment clearly indicated

---

### Phase 9.5: Portfolio Summary (Sprint 2 - Parallel)

#### Components to Build
```
PortfolioSummary       - Top-level metrics
  ├─ ValueDisplay      - Total portfolio value
  ├─ PerformanceMetrics - P&L, allocations
  ├─ AllocationChart   - Pie chart of positions
  └─ PerformanceGraph  - Portfolio growth
```

#### Tasks
1. Create PortfolioSummary component
2. Calculate total portfolio value
3. Calculate invested capital vs current
4. Calculate unrealized P&L
5. Create AllocationChart (pie chart)
6. Show cost basis per position
7. Display performance over time
8. Test calculations with real data

#### Acceptance Criteria Met
- ✅ Portfolio value calculations correct
- ✅ P&L clearly displayed
- ✅ Allocations shown visually
- ✅ All metrics update on data refresh

---

## Technical Decisions

### Chart Library: Recharts
**Why:**
- Native React components
- Easy time range updates
- Good performance with large datasets
- Active community support
- MIT licensed

**Installation:**
```bash
npm install recharts
```

### Data Fetching Strategy
**Poll-based (MVP):** Refresh on button click or interval
**Future:** WebSocket for real-time updates

### State Management
**Keep simple:** useState in App component or context if needed

### API Data Format
**Use existing endpoints:** No changes needed, just consume data

---

## Priority & Timeline

### Timeline: 5 Sprints (10 weeks)

| Sprint | Focus | Deliverable |
| --- | --- | --- |
| 1 | Quotes & Grid | Symbol prices visible with current data |
| 2 | Portfolio Summary + Chart Start | Value metrics + basic chart |
| 3 | Charts Complete | Interactive price history for all ranges |
| 4 | Indicators | Technical signals displayed and interpreted |
| 5 | News + Polish | News integration, optimization, bug fixes |

### Success Metrics
- All components render without errors
- Real API data displays correctly
- Performance acceptable (< 2s load for 20+ symbols)
- Mobile responsive on all components
- Test coverage > 80% for new components

---

## Dependencies

### Required
- ✅ Backend quote/chart/indicator APIs (already exist)
- ✅ React 18+ (already in project)
- ❌ Recharts (need to npm install)

### Optional
- Chart export/download
- Custom indicators
- Alert triggers
- Performance analytics

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Chart library performance | High latency with large datasets | Use data aggregation, implement virtual scrolling |
| News API delays | Feature slips into next sprint | Create mock data, parallel implementation |
| Real-time update complexity | Scope creep, timeline slip | Stick with polling for MVP, WebSocket in Phase 10 |
| Mobile responsiveness bugs | Poor user experience | Test on real devices early and often |
| Data freshness inconsistency | User confusion | Show timestamps, clear refresh indicators |

---

## Testing Strategy

### Unit Tests
```javascript
// Test price formatting
expect(formatPrice(1245.67)).toBe('$1,245.67')

// Test color coding
expect(getChangeColor(+2.3)).toBe('green')
expect(getChangeColor(-1.5)).toBe('red')

// Test indicator levels
expect(getRSILevel(72)).toBe('overbought')
expect(getRSILevel(28)).toBe('oversold')
```

### Integration Tests
- Load quotes → display in grid → refresh data
- Select symbol → load chart → change time range
- Load indicators → display signals → update on refresh

### E2E Tests
- User creates portfolio
- Adds multiple symbols
- Views all research data
- Makes informed decision

---

## Success Criteria

### Quotes Phase
- ✅ All symbols display with prices
- ✅ Change % color-coded correctly
- ✅ Volume and 52W range visible
- ✅ Refresh works smoothly
- ✅ Mobile responsive

### Chart Phase
- ✅ Charts render without lag
- ✅ Time range switching instant
- ✅ Tooltips show accurate data
- ✅ Volume bars visible
- ✅ Mobile swipe-friendly

### Indicator Phase
- ✅ All indicators calculated correctly
- ✅ Overbought/oversold clear
- ✅ Buy/sell signals visible
- ✅ Trend verdict understandable
- ✅ Mobile readable

### News Phase
- ✅ News articles display
- ✅ Filter by symbol works
- ✅ Links functional
- ✅ Load more pagination works
- ✅ Mobile card layout

### Overall
- ✅ All features integrated seamlessly
- ✅ No performance issues
- ✅ Mobile-first responsive
- ✅ Error states handled
- ✅ User can make informed decisions

---

## Change Log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.0 | 2026-06-05 | draft | — | Phase 9 plan for market research surface completion |

