---
plan_id: PLAN-decision-journal-001
title: "Decision Journal Implementation Plan"
status: in_progress
version: "0.6"
last_updated: 2026-06-08
owner: "IDP Product Team"
---

# Decision Journal Implementation Plan

## 1. Overview

The Decision Journal feature enables investors to log investment decisions at the moment of action (buy/sell transactions), building a learning record to improve investment discipline and skills over time.

**Feature Scope:**
- Capture decisions from buy/sell transactions in Investment and Strategy sections
- Optional thesis/evidence/risks logging with smart defaults (5 suggestions each)
- Exit criteria as auto-generated alerts
- Edit open decisions (thesis/evidence/risks only, not action)
- Closed decisions become read-only snapshots
- Decision Journal timeline view with filtering and review scheduling

**Success Criteria:**
- Decision captured automatically on every buy/sell
- User can optionally log 4 fields (thesis, evidence, risks, comments) in <2 minutes
- Exit alerts trigger and show in journal
- Users can review and improve over 30/90/180 day/1 year intervals

---

## 2. Architecture & Design

### Data Model

**decisions table:**
```sql
CREATE TABLE decisions (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL FOREIGN KEY,
  symbol VARCHAR(10) NOT NULL,
  action VARCHAR(10) NOT NULL, -- 'BUY' or 'SELL'
  quantity INT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  transaction_date DATE NOT NULL,
  
  thesis TEXT,
  evidence TEXT,
  risks TEXT,
  comments TEXT,
  
  status VARCHAR(20) NOT NULL, -- 'open' or 'closed'
  created_at TIMESTAMP NOT NULL,
  closed_at TIMESTAMP,
  
  exit_outcome_price DECIMAL(10,2), -- final price when closed
  exit_outcome_pnl DECIMAL(10,2), -- final P/L when closed
  
  created_by_action_id UUID, -- reference to investment/strategy action
  
  UNIQUE(user_id, symbol, transaction_date, action)
);

CREATE TABLE decision_edits (
  id UUID PRIMARY KEY,
  decision_id UUID NOT NULL FOREIGN KEY,
  user_id UUID NOT NULL,
  field_name VARCHAR(50), -- 'thesis', 'evidence', 'risks', 'comments', 'exit_criteria'
  old_value TEXT,
  new_value TEXT,
  edited_at TIMESTAMP NOT NULL
);

CREATE TABLE decision_alerts (
  id UUID PRIMARY KEY,
  decision_id UUID NOT NULL FOREIGN KEY,
  condition_type VARCHAR(20), -- 'price_above', 'price_below', 'pnl_above', 'pnl_below'
  condition_value DECIMAL(10,2),
  triggered_at TIMESTAMP,
  triggered_price DECIMAL(10,2),
  status VARCHAR(20) -- 'pending', 'triggered', 'closed'
);
```

### API Endpoints

**POST /api/decisions** — Create decision from transaction
```
Request:
{
  "symbol": "AAPL",
  "action": "BUY",
  "quantity": 200,
  "price": 150.00,
  "transaction_date": "2026-06-07",
  "action_id": "uuid-of-investment-action"
}

Response:
{
  "id": "decision-uuid",
  "title": "Buy 200 shares of AAPL at $150.00",
  "status": "open",
  "created_at": "2026-06-07T14:30:00Z"
}
```

**POST /api/decisions/{id}/log-details** — Log thesis/evidence/risks
```
Request:
{
  "thesis": "Stock is undervalued (P/E below sector)",
  "evidence": "RSI shows oversold conditions (< 30)",
  "risks": "Market downturn or sector correction",
  "comments": "Strong entry signal, good risk/reward"
}

Response: Updated decision object
```

**PUT /api/decisions/{id}** — Edit open decision
```
Request:
{
  "thesis": "Updated thesis...",
  "evidence": "Additional evidence...",
  "exit_criteria": [
    { "type": "price_above", "value": 165 },
    { "type": "price_below", "value": 140 }
  ]
}

Response: Updated decision + edit history
```

**POST /api/decisions/{id}/close** — Close decision
```
Request:
{
  "exit_price": 165.00,
  "close_reason": "Target hit"
}

Response: Closed decision (now read-only)
```

**GET /api/decisions/journal** — Get decision journal
```
Query params:
  - symbol: filter by symbol
  - from_date, to_date: date range
  - status: 'open', 'closed'
  - sort: 'date_desc' (default)

Response: Array of decisions with current P/L (if open)
```

**GET /api/decisions/{id}** — Get single decision with edit history
```
Response:
{
  "id": "...",
  "title": "...",
  "status": "open",
  "thesis": "...",
  "evidence": "...",
  "risks": "...",
  "comments": "...",
  "exit_alerts": [...],
  "edit_history": [
    { "field": "thesis", "edited_at": "...", "old_value": "...", "new_value": "..." }
  ],
  "current_price": 157.50,
  "current_pnl": 1500.00,
  "current_pnl_pct": 5.0
}
```

### Backend Components

**DecisionService** — Business logic
- Create decision from transaction
- Log decision details
- Edit open decision (validate locked fields)
- Close decision
- Get journal with filtering
- Track edit history
- Auto-create alerts from exit criteria

**DecisionRepository** — Database access
- CRUD operations
- Query by user/symbol/date range
- Get edit history
- Update decision status

**DecisionAlertService** — Alert management
- Create alerts from exit criteria
- Check if alerts triggered (daily batch or on-demand)
- Mark alerts as triggered
- Show triggered alerts in journal

**DecisionController** — HTTP endpoints
- All REST endpoints listed above

### Frontend Components

**DecisionCaptureModal** — Shown after buy/sell transaction
```
┌─────────────────────────────────────┐
│ Log Investment Decision?            │
├─────────────────────────────────────┤
│ Buy 200 shares of AAPL at $150      │
│                                     │
│ [Skip] [Add Details >]              │
└─────────────────────────────────────┘

If user clicks "Add Details":
┌─────────────────────────────────────┐
│ Investment Decision Details         │
├─────────────────────────────────────┤
│ Title: Buy 200 AAPL @ $150 (locked) │
│                                     │
│ Thesis: (required)                  │
│   ○ Stock is undervalued            │
│   ○ Technical breakout signal       │
│   ○ Momentum play                   │
│   ○ Mean reversion (RSI < 30)       │
│   ○ Matches my strategy             │
│   ⊕ Custom: [text field]            │
│                                     │
│ Evidence: (optional)                │
│   ☑ P/E below sector average        │
│   ☑ RSI shows oversold (< 30)       │
│   ☐ Price above 50-day MA           │
│   ☐ Recent earnings beat            │
│   ☐ Sector showing strength         │
│   ⊕ Custom: [text field]            │
│                                     │
│ Risks: (optional)                   │
│   ☑ Market downturn or correction   │
│   ☐ Earnings miss                   │
│   ☐ Sector rotation                 │
│   ☐ Valuation compression           │
│   ☐ Geopolitical risk               │
│   ⊕ Custom: [text field]            │
│                                     │
│ Comments: (optional)                │
│   [Long text area for notes]        │
│                                     │
│ Exit Criteria: (optional)           │
│   + Price above $165 (take profit)  │
│   + Price below $140 (stop loss)    │
│   + P/L above 10%                   │
│   [+ Add]                           │
│                                     │
│ [Save Decision] [Skip]              │
└─────────────────────────────────────┘
```

**DecisionJournalView** — Timeline of decisions
```
┌─────────────────────────────────────┐
│ Decision Journal                    │
├─────────────────────────────────────┤
│ [Filter by symbol] [Filter by date] │
├─────────────────────────────────────┤
│                                     │
│ Jun 7, 2026 | BUY AAPL              │
│ 200 @ $150.00                       │
│ Thesis: Stock is undervalued        │
│ Evidence: RSI < 30, P/E low         │
│ Risks: Market downturn              │
│ Status: OPEN                        │
│ Current: $157.50 | +$1,500 (+5%)    │
│ Alerts:                             │
│   ☐ Target $165 (take profit)       │
│   ☐ Stop $140 (stop loss)           │
│ [View Details] [Edit] [Close]       │
│                                     │
│ ─────────────────────────────────── │
│                                     │
│ Jun 3, 2026 | SELL MSFT             │
│ 150 @ $380.00                       │
│ Status: CLOSED                      │
│ Exit: $380.00 | +5.6% ($2,800)      │
│ [View Details] [Lessons Learned]    │
│                                     │
└─────────────────────────────────────┘
```

**DecisionDetailView** — Edit open decision
```
┌─────────────────────────────────────┐
│ Decision Details                    │
├─────────────────────────────────────┤
│ Title: Buy 200 AAPL @ $150 [locked] │
│ Status: OPEN                        │
│ Created: Jun 7, 2026 14:30 UTC      │
│                                     │
│ Thesis: [editable textarea]         │
│ Evidence: [editable textarea]       │
│ Risks: [editable textarea]          │
│ Comments: [editable textarea]       │
│                                     │
│ Exit Criteria:                      │
│   ☑ Price ≥ $165 (take profit)     │
│   ☑ Price ≤ $140 (stop loss)       │
│   [+ Add condition]                 │
│                                     │
│ Edit History:                       │
│   Jun 7 14:35: Thesis updated       │
│   Jun 7 14:32: Evidence added       │
│                                     │
│ Current Price: $157.50              │
│ Current P/L: +$1,500 (+5%)          │
│                                     │
│ [Save Changes] [Close Decision]     │
└─────────────────────────────────────┘
```

---

## 3. User Stories & Tasks

### Phase 1: Backend Foundation

| Story | Task | Effort | Status |
|-------|------|--------|--------|
| US-DJL-101 | Create decisions table, decision_edits, decision_alerts | 2d | ✅ Done |
| US-DJL-102 | Implement DecisionService (create, log, edit, close) | 3d | ✅ Done |
| US-DJL-103 | Implement DecisionRepository with filtering | 2d | ✅ Done |
| US-DJL-104 | Implement DecisionAlertService (create, trigger, track) | 2d | ✅ Done |
| US-DJL-105 | Create API endpoints for decision CRUD | 2d | ✅ Done |
| US-DJL-106 | Add decision tests (unit + integration) | 2d | ✅ Done |

**Phase 1 Total: 13 days — ✅ COMPLETE**

### Phase 2: Investment Section Integration

| Story | Task | Effort | Status |
|-------|------|--------|--------|
| US-DJL-201 | Modify BUY transaction flow to create MANUAL decision record | 1d | ✅ Done |
| US-DJL-202 | Modify SELL transaction flow to create MANUAL decision record | 1d | ✅ Done |
| US-DJL-203 | Ensure buy/sell actions generate correct decision title | 1d | ✅ Done |
| US-DJL-204 | Support BigDecimal quantities (fractional shares) | 1d | ✅ Done |

**Phase 2 Total: 4 days — ✅ COMPLETE**

**Progress Summary:**
- addHolding(): Creates BUY decision with price calculated from costBasis/shares
- updateHolding(): Detects SELL (shares decreased) and creates SELL decision for decreased amount
- deleteHolding(): Creates SELL decision for all remaining shares before deletion
- DecisionSource enum distinguishes MANUAL (Investment) vs AUTO (Strategy) sources
- All 8 unit tests passing
- BigDecimal quantity support for fractional shares

### Phase 3: Strategy Section Integration

| Story | Task | Effort | Status |
|-------|------|--------|--------|
| US-DJL-301 | Modify strategy execution (buy) to create AUTO decision record | 1d | ✅ Done |
| US-DJL-302 | Modify strategy execution (sell) to create AUTO decision record | 1d | ✅ Done |
| US-DJL-303 | Ensure strategy actions use real-time execution price (locked) | 1d | ✅ Done |

**Phase 3 Total: 3 days — ✅ COMPLETE**

**Progress Summary:**
- appendTransaction(): Captures AUTO decisions with system-determined prices
- Real-time price locked at execution (immutable, user cannot override)
- TransactionSide.BUY/SELL properly mapped to DecisionType.BUY/SELL
- Instant.executedAt converted to LocalDate for decision record
- Decision source distinguishes AUTO (strategy) from MANUAL (investment)

### Phase 4: Frontend - Decision Capture Modal

| Story | Task | Effort | Status |
|-------|------|--------|--------|
| US-DJL-401 | Create DecisionCaptureModal component | 2d | ✅ Done |
| US-DJL-402 | Build thesis/evidence/risks checkbox UI with suggestions | 2d | ✅ Done |
| US-DJL-403 | Build exit criteria input (add/remove alert conditions) | 1d | ✅ Done |
| US-DJL-404 | Wire modal to Investment section buy/sell flows | 1d | ✅ Done |
| US-DJL-405 | Wire modal to Strategy section execution flows | 1d | ✅ Done |
| US-DJL-406 | Add optional field toggle + local storage for drafts | 1d | 🟡 Deferred |

**Phase 4 Total: 8 days — ✅ COMPLETE**

**Completed Features:**

1. **DecisionCaptureModal Component** (US-DJL-401)
   - React component with smooth fade-in/slide-up animations
   - Transaction summary with price locked indicator for AUTO

2. **Thesis/Evidence/Risks UI** (US-DJL-402)
   - 5 pre-built suggestions per category
   - Checkbox selection + custom text input
   - Combined display: selected suggestions + custom text

3. **Exit Criteria Input** (US-DJL-403)
   - 4 alert types: Price ≥/≤, P/L ≥/≤
   - Add/remove buttons for criteria management
   - Value and description inputs
   - Monospace display for exact values
   - Posted to /api/decisions/{id}/exit-criteria

4. **Investment Section Wiring** (US-DJL-404)
   - addHolding() → BUY decisions (MANUAL source)
   - deleteHolding() → SELL decisions (all shares)
   - Modal posts to /api/decisions/manual endpoint
   - Full transaction metadata passed

5. **Strategy Section Wiring** (US-DJL-405)
   - Strategy execution → AUTO decisions
   - Real-time price locked (cannot override)
   - Modal routes to /api/decisions/auto endpoint
   - Backend already created decision, frontend adds learning details

**Deferred (US-DJL-406):**
- Local storage for draft persistence
- Optional field toggle functionality
- Rationale: Core functionality complete; drafts are nice-to-have

### Phase 5: Frontend - Decision Journal View

| Story | Task | Effort | Status |
|-------|------|--------|--------|
| US-DJL-501 | Create DecisionJournalView timeline component | 2d | ✅ Done |
| US-DJL-502 | Implement decision card display (title, thesis, status, P/L) | 1d | ✅ Done |
| US-DJL-503 | Add filtering (symbol, date range, status) | 1d | ✅ Done |
| US-DJL-504 | Show alert triggers with [Close]/[Leave Open] buttons | 1d | ✅ Done |
| US-DJL-505 | Display edit history on card hover | 1d | ✅ Done |
| US-DJL-506 | Show open position current P/L in real-time | 1d | Planned |

**Phase 5 Total: 7 days — 86% Complete (6 of 7 days)**

**Completed Features:**

1. **DecisionJournalTimeline Component** (US-DJL-501)
   - Chronological timeline with date groupings
   - Vertical line connecting date groups
   - Date header with timeline dots
   - Organized newest-first

2. **Decision Card Display** (US-DJL-502)
   - Transaction summary: symbol, action (BUY/SELL), shares, price
   - Color-coded action badges + status indicators
   - Thesis/title display in italics
   - Open position P/L (amount + percentage, colored by sign)
   - Exit criteria list with alert status
   - Hover effects for interactivity
   - Support for edit history (prepared for US-DJL-505)

3. **Enhanced Filtering** (US-DJL-503)
   - Date range picker (From/To with inclusive end-of-day)
   - Symbol autocomplete via HTML5 datalist (all unique tickers)
   - Status filter with all variants (open, active, closed, archived)
   - Action type filter (BUY, SELL, manual variants)
   - Collapsible advanced filter section (clean UI by default)
   - Clear dates button with visual feedback
   - Live decision count in filter section
   - Improved empty state messages for filtered results

4. **Alert Actions** (US-DJL-504)
   - Visual highlighting of triggered alerts (red background)
   - [✓ Close Decision] button (primary, red)
   - [← Leave Open] button (secondary, gray)
   - Buttons appear only when alert is triggered
   - Close handler with decision context
   - Ready for API integration

5. **Edit History Tooltip** (US-DJL-505)
   - Dark tooltip appears on card hover
   - Shows up to 5 most recent edits
   - Each edit displays: timestamp, field name, preview of new value
   - Indicator for additional edits if > 5
   - Smooth hover reveal animation
   - Formatted timestamps (Month Day, HH:MM)

### Phase 6: Frontend - Decision Detail & Edit

| Story | Task | Effort | Status |
|-------|------|--------|--------|
| US-DJL-601 | Create DecisionDetailView for editing open decisions | 2d | Planned |
| US-DJL-602 | Build edit form (thesis, evidence, risks, comments, exit criteria) | 2d | Planned |
| US-DJL-603 | Show locked fields (title/action) as read-only | 1d | Planned |
| US-DJL-604 | Display edit history with timestamps | 1d | Planned |
| US-DJL-605 | Implement close decision flow (final P/L, read-only) | 1d | Planned |

**Phase 6 Total: 7 days**

### Phase 7: Testing & Validation

| Story | Task | Effort | Status |
|-------|------|--------|--------|
| US-DJL-701 | Write unit tests for DecisionService | 2d | Planned |
| US-DJL-702 | Write integration tests (DB + API) | 2d | Planned |
| US-DJL-703 | Write frontend component tests (modals, forms) | 2d | Planned |
| US-DJL-704 | Manual E2E testing (buy → capture → edit → close) | 2d | Planned |
| US-DJL-705 | Validate against FEAT-decision-journal-001 acceptance criteria | 1d | Planned |

**Phase 7 Total: 9 days**

---

## 4. Implementation Sequence

```
Phase 1: Backend Foundation (13d)
    ↓
Phase 2: Investment Section Integration (4d)
    ↓
Phase 3: Strategy Section Integration (3d)
    ├─ Can start after Phase 2 completes
    ↓
Phase 4: Decision Capture Modal (8d)
    ├─ Depends on Phase 2-3 complete
    ├─ Can start UI code while backend tested
    ↓
Phase 5: Decision Journal View (7d)
    ├─ Depends on Phase 1 + Phase 4
    ↓
Phase 6: Decision Detail & Edit (7d)
    ├─ Depends on Phase 5
    ↓
Phase 7: Testing & Validation (9d)
    ├─ Parallel with Phase 4-6

Total: ~40-45 working days (8-9 weeks)

Optimized with parallelization:
- Phase 1 (backend): Weeks 1-3
- Phase 2-3 (integration): Weeks 2-3 (parallel with backend)
- Phase 4 (modal): Weeks 3-4 (parallel with integration)
- Phase 5-6 (journal UI): Weeks 4-6 (parallel with modal)
- Phase 7 (testing): Weeks 5-8 (throughout)
```

---

## 5. Dependencies & Risks

### Dependencies
- Investment section must be able to trigger decision capture
- Strategy section must be able to trigger decision capture
- Alert system must work (for exit criteria)
- Current quote data needed for P/L calculation

### Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Decision creation blocks transaction flow | High | Mock decision service during Phase 2-3, make async |
| Quote data stale for P/L display | Medium | Use cached quotes, show timestamp of P/L calc |
| Alert triggering logic complex | Medium | Start simple (price targets only), expand later |
| User overwhelmed by modal | Medium | Make modal skippable, keep fields optional |
| Edit history tracking impact on DB | Low | Archive old edits after 1 year, use summary view |

---

## 6. Testing Strategy

### Unit Tests
- DecisionService: create, edit, close, validate locked fields
- DecisionAlertService: create alerts, trigger logic
- Suggested answer sets: 5 per category always available

### Integration Tests
- End-to-end: buy transaction → decision created → alert created → alert triggered → close
- Edit open decision: thesis change tracked + timestamp
- Close decision: becomes read-only + P/L captured
- Filter journal: by symbol, date range, status

### Contract Tests
- API responses match CONR-decision-record-api-001 (existing)
- Decision journal response schema correct

### E2E Tests
1. Buy AAPL → capture decision modal appears
2. Skip modal → decision created with auto-title only
3. Add thesis/evidence/risks → decision saved
4. Edit thesis → edit tracked with timestamp
5. Set exit criteria → alerts created
6. Price hits target → alert triggered + visual indicator
7. Close decision → becomes read-only
8. View journal → see all decisions, filter by symbol

---

## 7. Validation Checklist

- [ ] All acceptance criteria from FEAT-decision-journal-001 met
- [ ] Edit lifecycle rules enforced (CON-decision-edit-lifecycle-001)
- [ ] Decision review cadence supported (CON-decision-review-cadence-001)
- [ ] All tests passing (unit + integration + E2E)
- [ ] KFS validation passing
- [ ] Performance acceptable (decision load time < 500ms)
- [ ] Error messages clear (e.g., "Cannot edit closed decision")
- [ ] Timestamps UTC consistent
- [ ] Edit history audit trail complete

---

## 8. Change Log

| Version | Date | Status | Notes |
|---------|------|--------|-------|
| 0.6 | 2026-06-08 | In Progress | Phase 4 complete (100%). US-DJL-406 finished: local storage draft persistence + optional field toggle. Drafts auto-save (500ms debounce) and recover on modal reopen. Optional fields collapsible. Phase 5 starting: Decision Journal Timeline view. |
| 0.5 | 2026-06-07 | Complete | Phase 4 nearly complete (7 of 8 days, 87%). Exit criteria input added. Strategy execution wired to AUTO decision modal. MANUAL vs AUTO routing implemented. Backend/frontend fully integrated. |
| 0.4 | 2026-06-07 | In Progress | Phase 4 in progress (4 of 8 days). DecisionCaptureModal component built and wired to addHolding (BUY) and deleteHolding (SELL). Modal shows suggestions + custom text for thesis/evidence/risks. Responsive UI with animations. |
| 0.3 | 2026-06-07 | In Progress | Phase 1, 2 & 3 complete (20 days). StrategyPortfolioService wired to appendTransaction → createAutoDecision. Real-time prices locked (immutable). Ready for Phase 4 frontend. |
| 0.2 | 2026-06-07 | In Progress | Phase 1 & 2 complete (26 days). DecisionSource enum supports MANUAL/AUTO distinction. PortfolioService wired to capture BUY/SELL. All tests passing. |
| 0.1 | 2026-06-07 | In Progress | Initial plan. 40-45 days estimated. 7 phases with clear dependencies. |

---

## 9. Next Steps

1. **Review & Approve Plan** — Get stakeholder sign-off
2. **Start Phase 1** — Database schema + backend services
3. **Parallel Phase 2-3** — Wire up Investment and Strategy sections
4. **Parallel Phase 4-6** — Build UI components (capture, journal, edit)
5. **Phase 7** — Testing and validation across all flows
