# Business Plan Review & Gap Analysis

**Date:** 2026-06-05  
**Status:** MVP Complete but UI Features Incomplete

---

## Executive Summary

The IDP MVP is **feature-complete on the backend** (APIs, data models, business logic) but **UI implementation is minimal/placeholder-based**. The current frontend shows basic shells for the market research features (quotes, charts, indicators, news) that are critical to the investor workflow.

**Key Gap:** The portfolio/watchlist research surface lacks the rich market data display that makes the platform useful for actual investment decision-making.

---

## Current State vs. PRD Requirements

### ✅ Backend Features Implemented

| Feature | Backend Status | API Endpoints | Tests |
| --- | --- | --- | --- |
| Decision Journal | ✅ Complete | 5+ endpoints | 2 test classes |
| Decision Reviews | ✅ Complete | 3+ endpoints | 2 test classes |
| DQS Scoring | ✅ Complete | 2 endpoints | 1 test class |
| Behavioral Analytics | ✅ Complete | 2 endpoints | 1 test class |
| Strategy Portfolios | ✅ Complete | 10+ endpoints | 1 test class |
| Tracked Symbols | ✅ Complete | Included in portfolio API | Covered |
| Market Data (Quotes) | ✅ Complete | `/api/strategies/{id}/quotes` | Tested |
| Market Data (Charts) | ✅ Complete | `/api/strategies/{id}/charts` | Tested |
| Technical Indicators | ✅ Complete | `/api/strategies/{id}/indicators` | Tested |
| Public Profiles | ✅ Complete | 3+ endpoints | 1 test class |
| Authentication | ✅ Complete | Register/login | 1 test class |

### ❌ Frontend UI Features Incomplete

| Feature | PRD Requirement | Current UI Status | Gap |
| --- | --- | --- | --- |
| **Portfolio/Watchlist Creation** | ✅ Required (FR-005) | ✅ Basic form | Minimal, works |
| **Symbol Tracking** | ✅ Required (FR-008) | ✅ Basic form | Minimal, works |
| **Symbol Quotes Display** | ✅ Required (FR-007) | ❌ Placeholder only | Major - shows basic info, no pricing |
| **Historical Charts** | ✅ Required (FR-007) | ❌ Label only | Major - no chart visualization |
| **Technical Indicators** | ✅ Required (FR-007) | ❌ Label only | Major - no indicator display |
| **News Feed** | ⚠️ Implied in research | ❌ Not implemented | Major - completely missing |
| **Portfolio Symbol List** | ✅ Required (US-006) | ✅ Basic list | Minimal |
| **Market Data Freshness** | ✅ Mentioned (FR-007) | ✅ Shows timestamp | Minimal |
| **Research Context** | ✅ Needed for decisions | ❌ Minimal display | Major - can't review strategy context |
| **Price History** | ✅ For watchlist comparisons | ❌ No visualization | Major |

---

## Detailed Gap Analysis

### 1. Portfolio/Watchlist Combined Feature

**PRD Requirement (US-006, FR-008):**
> "As an investor, I want to add a stock to a strategy portfolio for tracking without buying it so the portfolio can act as a private watchlist before any transaction."

**Current Implementation:**
- ✅ Backend supports tracked-only symbols
- ✅ Frontend has form to add symbols
- ❌ UI doesn't differentiate between "watched" vs "position"
- ❌ Can't easily see all watchlist items with their current prices

**What's Missing:**
- Symbol status indicator (watched vs held)
- Easy toggle between "watch only" and "buy"
- Watchlist-style grid showing all symbols with current prices

---

### 2. Quotes Display

**PRD Requirement (FR-007):**
> "Strategy portfolios must include portfolio-scoped quote rows for symbols in the strategy."

**Current Implementation:**
```jsx
<div className="quote" key={quote.symbol}>
  <strong>{quote.symbol}</strong>
  <span>{quote.trackingStatus}</span>
  <small>{strategyQuotes.dataFreshness}</small>
</div>
```

**What's Shown:**
- Symbol ticker
- Tracking status (text)
- Data freshness timestamp

**What's Missing:**
- Current price
- Price change ($ and %)
- 52-week high/low
- Volume
- Market cap
- PE ratio (if applicable)
- Color coding (red/green for up/down)
- Real-time updates

**Example Needed:**
```
NVDA        $1,245.67 ↑ +2.3%
52W: 800 - 1500  |  Vol: 50.2M  |  PE: 62.4
```

---

### 3. Historical Charts

**PRD Requirement (FR-007):**
> "Strategy portfolios must include historical charts for symbols in the strategy."

**Current Implementation:**
```jsx
<div>
  <h3><BarChart3 size={17} />History</h3>
  <p>{strategyHistory?.series?.length || 0} symbol series · {strategyHistory?.range || '1mo'}</p>
</div>
```

**What's Shown:**
- Label "History"
- Number of symbol series
- Range (1mo)

**What's Missing:**
- Actual SVG/canvas chart visualization
- Time range selector (1D, 1W, 1M, 3M, YTD, 1Y, ALL)
- Interactive price points
- Candlestick or line chart
- Volume bars below price
- Trend lines/moving averages
- Comparison against benchmarks (S&P 500, sector)

**Example Implementation Needed:**
```
Chart showing NVDA price movement over last 3 months
with interactive tooltips and time range tabs
```

---

### 4. Technical Indicators

**PRD Requirement (FR-007):**
> "Strategy portfolios must include technical indicator views for symbols in the strategy."

**Current Implementation:**
```jsx
<div>
  <h3><ShieldCheck size={17} />Indicators</h3>
  <p>{indicator ? `${indicator.symbol}: ${indicator.trendVerdict}` : 'No symbol selected'}</p>
</div>
```

**What's Shown:**
- Indicator label
- Symbol name
- Trend verdict text

**What's Missing:**
- Specific indicator calculations (RSI, MACD, Bollinger Bands, etc.)
- Buy/sell signals
- Momentum indicators
- Trend analysis
- Overbought/oversold warnings
- Indicator graphs/visualizations

**Example Implementation Needed:**
```
RSI: 72 (overbought) ⚠️
MACD: Bearish divergence detected
Moving Averages: Price above 50MA, below 200MA
Bollinger Bands: Price near upper band
```

---

### 5. News & Research

**Not in Current UI at All**

**Stock Monitor Reference (should be integrated):**
- News feed for tracked symbols
- Recent news aggregation
- Research articles
- Earnings announcements
- Analyst reports

**What's Missing:**
- News feed section
- Article previews
- Source links
- Date/time filtering
- Relevance scoring

---

## Stock Monitor Integration Points

The user wants to bring these key features from stock-monitor into IDP:

### 1. **Watchlist/Portfolio Combined**
- ✅ Backend: Supports tracked symbols
- ❌ UI: Needs rich watchlist grid view
- **Action:** Create watchlist component with symbol grid, status indicators, prices

### 2. **Quotes with Real-Time Data**
- ✅ Backend: Fetches from Yahoo Finance
- ❌ UI: Only shows placeholder data
- **Action:** Create quotes grid component showing price, change %, 52W range, volume

### 3. **Charts with Time Range Selection**
- ✅ Backend: Returns historical data
- ❌ UI: No chart visualization
- **Action:** Create SVG/canvas chart component with interactive time range tabs

### 4. **Technical Indicators**
- ✅ Backend: Calculates indicators
- ❌ UI: Only shows text verdict
- **Action:** Create indicators component showing RSI, MACD, moving averages, signals

### 5. **News Feed**
- ❌ Backend: No news API implemented
- ❌ UI: Not shown anywhere
- **Action:** Add news endpoint and create news feed component

---

## Current Backend Capabilities (Not Fully Used in UI)

### Quotes Endpoint
```
GET /api/strategies/{id}/quotes
Returns: {
  symbols: [
    {
      symbol: "NVDA",
      price: 1245.67,
      change: +2.3,
      percentChange: 0.185,
      volume: 50200000,
      marketCap: 3073000000000,
      peRatio: 62.4,
      week52High: 1500,
      week52Low: 800
    }
  ],
  dataFreshness: "2026-06-05T14:30:00Z"
}
```

### Charts Endpoint
```
GET /api/strategies/{id}/charts?range=1M
Returns: {
  series: [
    {
      symbol: "NVDA",
      data: [
        { date: "2026-05-05", open: 1100, high: 1150, low: 1090, close: 1145, volume: 45000000 },
        ...
      ]
    }
  ],
  range: "1M"
}
```

### Indicators Endpoint
```
GET /api/strategies/{id}/indicators
Returns: {
  symbol: "NVDA",
  rsi: 72,
  rsiSignal: "overbought",
  macd: { value: 12.5, signal: 10.2, histogram: 2.3, trend: "bearish" },
  movingAverages: { ma50: 1200, ma200: 1050 },
  bollingerBands: { upper: 1350, middle: 1200, lower: 1050 },
  trendVerdict: "bearish divergence",
  buySignals: 2,
  sellSignals: 3
}
```

**All this data exists on the backend but the UI doesn't render it!**

---

## Recommended UI Improvements (Priority Order)

### Phase 1: Core Market Data Display (High Priority)

**Timeline:** 2-3 sprints

| Feature | Effort | Impact | Dependencies |
| --- | --- | --- | --- |
| Quotes Grid Component | Medium | High | None |
| Symbol Price Card | Small | High | Quotes endpoint |
| Time Range Selector | Small | Medium | None |
| Chart.js/D3 Integration | Large | High | Charts endpoint |
| Indicator Display | Medium | High | Indicators endpoint |

### Phase 2: Enhanced Research Surface (Medium Priority)

**Timeline:** 2-3 sprints

| Feature | Effort | Impact | Dependencies |
| --- | --- | --- | --- |
| News Feed Integration | Medium | High | New news endpoint |
| Portfolio Performance View | Medium | Medium | Strategy API |
| Symbol Comparison Grid | Small | Medium | Quotes endpoint |
| Market Context Panel | Small | Medium | Market data |

### Phase 3: Advanced Features (Lower Priority)

**Timeline:** 3-4 sprints

| Feature | Effort | Impact | Dependencies |
| --- | --- | --- | --- |
| Custom Indicators | Large | Medium | Backend support |
| Alert Triggers | Medium | Medium | Notification system |
| Backtesting | Large | Low | Complex math |
| Social Sentiment | Large | Low | Third-party APIs |

---

## Missing Components Summary

### UI Components to Build

1. **QuotesGrid** — Display all tracked symbols with current prices, changes, volumes
2. **SymbolCard** — Individual symbol with price, change %, sparkline
3. **PriceChart** — Interactive candlestick/line chart with time range tabs
4. **IndicatorPanel** — RSI, MACD, moving averages, trend signals
5. **NewsPanel** — Recent news/articles for tracked symbols
6. **WatchlistGrid** — Watch-only symbols with easy "add to portfolio" action
7. **PortfolioComparison** — Position vs cost basis vs current value
8. **MarketSnapshot** — Market indices, sector performance

### Backend Endpoints to Add

1. **News API** — `/api/strategies/{id}/news` or `/api/symbols/{symbol}/news`
2. **Real-Time Quotes** — WebSocket or polling for live prices
3. **Watchlist Operations** — Separate from portfolio transactions
4. **Custom Alerts** — Price alerts, indicator signals

---

## Impact on User Workflows

### Current Limitation
Investor creates a strategy, adds symbols, but then:
- Can't see current prices without leaving the app
- Can't review chart history to understand entry points
- Can't use technical indicators to make trade decisions
- Can't see relevant news while researching

### With Improvements
Investor creates a strategy, adds symbols, and:
- ✅ Sees all prices, volumes, changes in one grid
- ✅ Reviews 1D/1W/1M/1Y charts interactively
- ✅ Checks RSI, MACD, moving averages before buying
- ✅ Reads recent news in research panel
- ✅ Makes informed decisions without context-switching

---

## Alignment with Stock Monitor

The stock-monitor project has working implementations of:
- ✅ Watchlist UI with symbol grids
- ✅ Quote display with real-time pricing
- ✅ Interactive charts with time ranges
- ✅ Technical indicator panels
- ✅ News feed integration

**These should be ported/adapted into IDP's portfolio research surface.**

---

## Risks & Blockers

### Technical Risks
- Chart library choice (D3, Chart.js, Recharts?)
- Real-time data performance (polling vs WebSocket)
- Browser rendering limits with large datasets

### Timeline Risks
- Chart integration complexity (3-5 days)
- News API integration (2-3 days)
- Testing interactive components (2-3 days)

### Business Risks
- Incomplete MVP feels unfinished to users
- High bounce rate without data visualization
- User satisfaction depends on research surface quality

---

## Recommendations

### Immediate (Next Sprint)
1. **Build QuotesGrid component** — Show all symbols with price/volume/change
2. **Implement basic Chart** — Even simple SVG line chart is better than text
3. **Add IndicatorPanel** — Display RSI, MACD text + color coding
4. **Fix Portfolio/Watchlist UX** — Clear distinction between watch-only and positions

### Short Term (2-3 Sprints)
1. **Interactive Charts** — Time range tabs, interactive tooltips
2. **News Integration** — Add news API endpoint and panel
3. **Symbol Search/Add** — Better UI for discovering symbols
4. **Market Context** — Show S&P 500, sector indices as benchmarks

### Medium Term (Next Quarter)
1. **Custom Watchlists** — Create focused watchlists within portfolio
2. **Alerts & Notifications** — Price, indicator, news-based alerts
3. **Portfolio Analytics** — Performance tracking, allocations, rebalancing
4. **Social Features** — Share portfolio, see peer strategies

---

## Change Log

| Revision | Date | Status | Notes |
| --- | --- | --- | --- |
| 1.0 | 2026-06-05 | draft | Initial gap analysis of frontend vs PRD requirements |

