# UI Implementation Roadmap

**Objective:** Complete the market research features (quotes, charts, indicators, news) to match PRD and enable actual investment decision-making workflow.

---

## Priority 1: Quotes & Symbol Grid (1-2 sprints)

### Features
- Symbol grid showing all tracked/position symbols
- Current price with change % and color coding
- 52-week range, volume, market cap
- Quick toggle between "watch only" and "position"
- Real-time price updates (or refresh button)

### Components to Create

#### 1. SymbolQuoteGrid
```jsx
// Shows all symbols with real price data
<SymbolQuoteGrid 
  symbols={strategyQuotes.symbols}
  onSymbolClick={selectSymbol}
  onTogglePosition={togglePosition}
/>

// Renders:
// NVDA  $1245.67 ↑ 2.3%  | Vol: 50.2M | 52W: 800-1500
// AAPL  $185.42  ↓ -1.1% | Vol: 42.5M | 52W: 120-195
```

#### 2. SymbolCard
```jsx
// Individual symbol detail
<SymbolCard symbol={symbol} quoteData={quote} />

// Renders:
// ┌─ NVDA ─────────────────┐
// │ $1245.67  ↑ +2.3%      │
// │ 52W: 800 - 1500        │
// │ Vol: 50.2M             │
// │ Market Cap: $3.07T     │
// │ P/E: 62.4              │
// │ [Watch] [Position] ... │
// └────────────────────────┘
```

### Implementation Steps
1. Parse quotes endpoint response
2. Format numbers (prices, volumes, percentages)
3. Add color coding (green for +, red for -)
4. Create responsive grid layout
5. Add refresh button
6. Test with real data

---

## Priority 2: Interactive Charts (2-3 sprints)

### Features
- Line/candlestick charts for each symbol
- Time range selector (1D, 1W, 1M, 3M, YTD, 1Y, ALL)
- Interactive tooltips showing price at date
- Volume bars below price
- Support multiple symbols overlaid

### Components to Create

#### 1. PriceChart
```jsx
// Interactive price chart
<PriceChart 
  symbol="NVDA"
  data={strategyHistory.series[0].data}
  range={timeRange}
  onRangeChange={setTimeRange}
  type="candlestick" // or "line"
/>

// Renders:
// [1D] [1W] [1M] [3M] [YTD] [1Y] [ALL]
// ┌─────────────────────────────┐
// │     NVDA Price (1M)         │
// │  ↗ ╱╲  ╱  ╲            ╱   │
// │ ╱  ╲╱  ╲   ╲  ╱╲    ╱      │
// │╱     ╲  ╲   ╲╱  ╲╱         │
// ├─────────────────────────────┤
// │  Vol │││  │  ││  │ ││ │ │  │
// └─────────────────────────────┘
// [Tooltip on hover: Date, Price, Volume]
```

#### 2. ChartControls
```jsx
// Time range selector
<ChartControls 
  ranges={['1D', '1W', '1M', '3M', 'YTD', '1Y', 'ALL']}
  selected={timeRange}
  onChange={onChangeRange}
/>
```

### Library Choice
**Recommended: Recharts** (React-friendly, good charts, easy customization)

```bash
npm install recharts
```

### Implementation Steps
1. Choose charting library (Recharts recommended)
2. Create ChartControls component for time ranges
3. Create PriceChart component with candlestick/line
4. Add volume bars below price
5. Implement interactive tooltips
6. Add zoom/pan capabilities (optional)
7. Test with real historical data

---

## Priority 3: Technical Indicators (1-2 sprints)

### Features
- RSI (Relative Strength Index) with overbought/oversold levels
- MACD (Moving Average Convergence Divergence)
- Moving averages (50, 200)
- Bollinger Bands
- Trend verdict (bullish/bearish)
- Buy/sell signal counts

### Components to Create

#### 1. IndicatorPanel
```jsx
// Display all indicators for symbol
<IndicatorPanel symbol="NVDA" indicators={indicator} />

// Renders:
// ┌─ Technical Indicators: NVDA ────┐
// │                                 │
// │ RSI: 72 ⚠️ OVERBOUGHT            │
// │ [====##########--] 50    70     │
// │                                 │
// │ MACD: Bearish Divergence        │
// │ Value: 12.5  |  Signal: 10.2    │
// │ Histogram: 2.3                  │
// │                                 │
// │ Moving Averages:                │
// │ MA50: $1200  |  MA200: $1050    │
// │ Price above MA50 ✓              │
// │ Price below MA200 ✓             │
// │                                 │
// │ Bollinger Bands:                │
// │ Upper: $1350  |  Lower: $1050   │
// │ Price near upper band ⚠️        │
// │                                 │
// │ Buy Signals: 2  |  Sell: 3      │
// │ Verdict: ⬇️ BEARISH DIVERGENCE  │
// └─────────────────────────────────┘
```

#### 2. RSIIndicator
```jsx
<RSIIndicator value={72} level="overbought" />
```

#### 3. MACDIndicator
```jsx
<MACDIndicator value={12.5} signal={10.2} histogram={2.3} trend="bearish" />
```

### Implementation Steps
1. Create IndicatorPanel main component
2. Create RSI sub-component with color coding
3. Create MACD sub-component with trend indicator
4. Create MovingAverages sub-component
5. Create BollingerBands sub-component
6. Add signal summary line
7. Use color coding (green/red for signals)
8. Test with real indicator data

---

## Priority 4: News Feed (2-3 sprints)

### Features
- News articles for tracked symbols
- Date/time stamps
- Source attribution
- Quick links to full articles
- Filter by symbol or date
- Sentiment indicators (optional)

### Backend Required First
```
GET /api/symbols/{symbol}/news
Returns: {
  articles: [
    {
      id: "news-123",
      title: "NVDA reports record Q2 earnings",
      source: "Reuters",
      url: "https://...",
      publishedAt: "2026-06-05T10:30:00Z",
      summary: "Nvidia reported earnings...",
      sentiment: "positive" // or neutral, negative
    },
    ...
  ]
}
```

### Components to Create

#### 1. NewsPanel
```jsx
// News feed for strategy symbols
<NewsPanel symbols={symbols} />

// Renders:
// ┌─ Research News ─────────────────┐
// │ Filter by symbol: [All ▼]       │
// │                                 │
// │ NVDA                    6/5/26  │
// │ "NVDA reports record Q2..."     │
// │ Reuters  [→ Read more]          │
// │                                 │
// │ AAPL                    6/4/26  │
// │ "Apple unveils new AI chip..."  │
// │ Bloomberg  [→ Read more]        │
// │                                 │
// │ MSFT                    6/3/26  │
// │ "Microsoft cloud revenue grows" │
// │ TechCrunch  [→ Read more]       │
// └─────────────────────────────────┘
```

#### 2. NewsCard
```jsx
<NewsCard 
  article={article}
  symbol="NVDA"
/>
```

#### 3. NewsFilter
```jsx
<NewsFilter 
  symbols={symbols}
  onFilter={setFilterSymbol}
/>
```

### Implementation Steps
1. Create backend news endpoint (or mock data)
2. Create NewsPanel component
3. Create NewsCard for individual articles
4. Create NewsFilter for symbol selection
5. Add date/time formatting
6. Add link handling
7. Add sentiment colors (optional)
8. Test with real news data

---

## Priority 5: Portfolio/Watchlist UI Refinement (1 sprint)

### Features
- Clear distinction between "watched" (no position) and "held" (position)
- Quick actions per symbol (add position, remove, alert, etc.)
- Total portfolio value display
- Position cost basis and P&L
- Allocation percentages

### Components to Refine

#### 1. SymbolStatus Indicator
```jsx
// Show watch vs position status
<SymbolStatus status="watched" />  // Blue "eye" icon
<SymbolStatus status="held" />     // Green "position" icon
```

#### 2. PortfolioSummary
```jsx
// Top of portfolio view
<PortfolioSummary
  totalValue={1234567}
  investedCapital={1000000}
  gainLoss={234567}
  gainLossPercent={23.4}
  dayChange={+5000}
  dayChangePercent={+0.4}
/>

// Renders:
// Total Portfolio Value: $1,234,567
// Invested Capital: $1,000,000
// Unrealized Gain/Loss: $234,567 (+23.4%)
// Today's Change: +$5,000 (+0.4%)
```

#### 3. PositionRow (Enhanced)
```jsx
// Individual position or watched symbol
<PositionRow
  symbol="NVDA"
  type="held"  // or "watched"
  quantity={100}
  costBasis={1200}
  currentPrice={1245.67}
  change={+2.3}
/>

// Renders:
// NVDA (100 shares)
// Avg Cost: $1200  Current: $1245.67 ↑ 2.3%
// Value: $124,567  Gain: +$4,567 (+3.8%)
// [Watch] [Trade] [Alert] [Remove]
```

### Implementation Steps
1. Update SymbolCard to show status clearly
2. Create PortfolioSummary component
3. Update SymbolQuoteGrid with position data
4. Add quick action buttons (trade, alert, remove)
5. Calculate P&L and percentages
6. Test with real portfolio data

---

## Implementation Timeline

### Sprint 1: Quotes & Basic Layout
- SymbolQuoteGrid component
- SymbolCard with real data
- Responsive grid layout
- Refresh data functionality
- **Deliverable:** Can see all symbols with current prices

### Sprint 2: Charts Foundation
- ChartControls component
- PriceChart with Recharts
- Time range functionality
- Volume visualization
- **Deliverable:** Can view price charts for any time range

### Sprint 3: Indicators & Polish
- IndicatorPanel component
- RSI, MACD, MA, Bollinger Bands sub-components
- Color coding for signals
- Responsive indicator layout
- **Deliverable:** Can see technical signals for decision-making

### Sprint 4: News Integration (if backend ready)
- News endpoint implementation
- NewsPanel component
- News cards and filtering
- Article link handling
- **Deliverable:** Can read relevant news in research surface

### Sprint 5: Polish & Optimization
- Refine UI/UX based on feedback
- Performance optimization
- Real-time data updates
- Error handling and edge cases
- **Deliverable:** Production-ready research surface

---

## Tech Stack Decision Matrix

### Chart Library

| Library | Pros | Cons | Recommendation |
| --- | --- | --- | --- |
| **Recharts** | React-friendly, good defaults, easy customization | Limited advanced features | ✅ **BEST for MVP** |
| D3.js | Extremely powerful, unlimited customization | Steep learning curve, verbose | For Phase 2+ |
| Chart.js | Simple, good performance, popular | Less React-friendly | Alternative to Recharts |
| Plotly | Feature-rich, good defaults | Heavy bundle size | For advanced analytics |

### Recommendation: **Recharts**
- Native React components
- Easy time range updates
- Good performance with 1000+ data points
- Active community support

---

## API Contract Verification

### Required Endpoints (Already Implemented)

**Quotes:**
```bash
GET /api/strategies/{strategyId}/quotes
Response: { symbols: [...], dataFreshness: "..." }
```

**History:**
```bash
GET /api/strategies/{strategyId}/charts?range=1M
Response: { series: [...], range: "1M" }
```

**Indicators:**
```bash
GET /api/strategies/{strategyId}/indicators
Response: { symbol, rsi, macd, ma50, ma200, bollinger, trendVerdict, ... }
```

**Missing - Need to Create:**
```bash
GET /api/symbols/{symbol}/news
Response: { articles: [...] }
```

---

## Data Flow

### Current Limited Flow
```
User adds symbol
  → Backend creates tracked symbol
  → Frontend shows in list
  ❌ No price/chart/indicator data shown
```

### Target Complete Flow
```
User adds symbol
  → Backend creates tracked symbol
  → Fetch quotes → Show prices, volume, change
  → Fetch charts → Show price history
  → Fetch indicators → Show RSI, MACD, signals
  → Fetch news → Show recent articles
  ✅ User has all context to make decision
```

---

## Testing Strategy

### Unit Tests
- Component rendering with mock data
- Data formatting (prices, percentages, dates)
- Event handlers (range selection, symbol click)

### Integration Tests
- End-to-end flow: add symbol → view all data
- Chart data loading and rendering
- Indicator calculations and display

### E2E Tests
- User journey: create portfolio → add symbols → review data → decide
- Real data from backend
- Performance testing with large datasets

---

## Risk Mitigation

### Performance Risk
- **Problem:** Charts with 1000+ data points slow down rendering
- **Mitigation:** Implement virtual scrolling, data aggregation for long ranges

### Data Freshness
- **Problem:** Market data may be stale during market hours
- **Mitigation:** Add refresh button, show last update timestamp

### Mobile Responsiveness
- **Problem:** Charts don't display well on mobile
- **Mitigation:** Stack vertically on mobile, ensure touch-friendly controls

### API Rate Limiting
- **Problem:** Frequent data refreshes hit rate limits
- **Mitigation:** Cache data locally, implement smart refresh intervals

---

## Success Criteria

✅ Quotes grid shows all symbols with current prices, volume, change %  
✅ Charts display interactive price history with time range selection  
✅ Indicators show RSI, MACD, moving averages with buy/sell signals  
✅ News panel shows recent articles for tracked symbols  
✅ Portfolio summary shows total value and P&L  
✅ All features responsive on mobile devices  
✅ Performance acceptable (< 2s load time for portfolio with 20+ symbols)  
✅ Real-time data updates when new quotes fetched  

---

## Change Log

| Revision | Date | Status | Notes |
| --- | --- | --- | --- |
| 1.0 | 2026-06-05 | draft | Initial UI implementation roadmap with 5 priority phases |

