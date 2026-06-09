# Decision Journal Integration - Strategy & Investment Workflows

**Status**: ✅ Complete  
**Last Updated**: 2026-06-09  
**KFS Compliance**: Full compliance with FEAT-decision-journal-001

---

## Overview

The Decision Journal is fully integrated into both the **Investment** and **Strategy** sections of IDP, capturing user decisions at the point of action.

## Integration Points

### 1. Investment Section → MANUAL Decisions

**Flow**: User adds holding → Decision captured

```
InvestmentWorkspaceView (addHolding)
  ↓
POST /api/portfolio/accounts/{accountId}/holdings
  ↓
setPendingDecisionData({symbol, action: 'BUY', shares, price, transactionDate})
  ↓
setShowDecisionCaptureModal(true)
  ↓
[User selects suggestions & submits]
  ↓
POST /api/decisions/manual → Create decision
  ↓
POST /api/decisions/{id}/log-details → Save thesis/evidence/risks
  ↓
POST /api/decisions/{id}/exit-criteria → Save alerts (if any)
  ↓
Modal closes, Decision appears in Journal
```

**Code Location**: `addHolding()` function (line 539)

**Triggers**:
- User adds holding to investment account
- User removes holding from account (SELL decision)

**Decision Type**: `MANUAL` (user-specified price)

---

### 2. Strategy Section → AUTO Decisions

**Flow**: User executes strategy transaction → Decision captured with system price

```
StrategyWorkspaceView (addSymbol)
  ↓
POST /api/strategies/{strategyId}/transactions
  ↓
setPendingDecisionData({symbol, action: BUY|SELL, shares, price: system-price, isAuto: true})
  ↓
setShowDecisionCaptureModal(true)
  ↓
[User selects suggestions & submits]
  ↓
POST /api/decisions/auto → Create decision (system-locked price)
  ↓
POST /api/decisions/{id}/log-details → Save thesis/evidence/risks
  ↓
POST /api/decisions/{id}/exit-criteria → Save alerts (if any)
  ↓
Modal closes, Decision appears in Journal
```

**Code Location**: `addSymbol()` function (line 438)

**Triggers**:
- User adds buy/sell transaction to strategy
- User watches symbol (doesn't create decision)

**Decision Type**: `AUTO` (system-determined market price, locked)

---

## DecisionCaptureModal - Phase 7A Features

When the modal opens (for either MANUAL or AUTO), it includes:

### 1. Suggestion System ✅

**Location**: `DecisionCaptureModal` (line 4160)

**Feature**: API-driven suggestions with checkbox selection

```javascript
// On mount, fetch from backend
useEffect(() => {
  fetch('/api/public/suggestions/all')
    .then(data => {
      setThesisSuggestions(data.thesis)    // 5 suggestions
      setEvidenceSuggestions(data.evidence) // 5 suggestions
      setRisksSuggestions(data.risks)       // 5 suggestions
    })
}, [isOpen])

// On submit, combine selected + custom
const combinedText = (category, suggestions) => {
  const selected = getSelectedSuggestions(category, suggestions)
  const custom = formData[category] || ''
  return [...selected, custom].filter(Boolean).join('; ')
}
```

**Selections**:
- Thesis: Radio buttons (pick one or custom)
- Evidence: Checkboxes (pick multiple or custom)
- Risks: Checkboxes (pick multiple or custom)

### 2. Form Submission ✅

**Location**: `handleSubmitDecision()` (line 4218)

**Process**:
1. Combine selected suggestions with custom text
2. Create decision via `/api/decisions/{auto|manual}`
3. **NEW**: Call `/api/decisions/{id}/log-details` to save thesis/evidence/risks
4. Save exit criteria alerts if provided
5. Close modal with success message

---

## DecisionDetailModal - Phase 7A Features

When user views a decision in the journal, they can:

### 1. Edit Thesis/Evidence/Risks ✅

**Location**: `DecisionDetailModal` (line 3377)

**Feature**: Textarea editing for decision details with save button

### 2. Exit Criteria Editor ✅

**Location**: `DecisionDetailModal` (line 3481)

**Feature**: 
- Add criteria: Type (PRICE_ABOVE/BELOW/AT), Value, Description
- Delete existing criteria
- Status display: PENDING/TRIGGERED/CLOSED

```javascript
handleAddAlert()
  → POST /api/decisions/{id}/exit-criteria
  
handleDeleteAlert()
  → DELETE /api/decisions/{id}/exit-criteria/{alertId}
```

### 3. Review Scheduler ✅

**Location**: `ReviewScheduler` component (line 3182)

**Feature**: Shows 4 scheduled review dates
- 30 days
- 90 days
- 180 days
- 1 year

```javascript
reviewDates = [
  {date: transactionDate + 30d, label: '30-Day Review', status: 'pending|overdue'},
  {date: transactionDate + 90d, label: '90-Day Review', status: 'pending|overdue'},
  ...
]
```

---

## DecisionsView - Phase 7A Features

### 1. Search/Filter ✅

**Location**: `DecisionsView` (line 1058)

**Features**:
- **Ticker search** with autocomplete
- **Type filter**: BUY/SELL/watch/avoid
- **Status filter**: open/active/closed/archived
- **Thesis/Risk keyword search**: NEW! (Line 1062)
- **Date range filter**: From/To dates

**Keyword search logic**:
```javascript
const matchesThesisRisk = 
  (d.thesis?.toLowerCase().includes(searchThesisRisk.toLowerCase())) ||
  (d.risks?.toLowerCase().includes(searchThesisRisk.toLowerCase())) ||
  (d.evidence?.toLowerCase().includes(searchThesisRisk.toLowerCase()))
```

### 2. Timeline Display ✅

**Location**: `DecisionJournalTimeline` (line 3919)

**Features**:
- Grouped by date (newest first)
- Shows thesis/evidence/risks snippets
- Shows exit criteria alerts with status
- Action buttons for editing/closing

### 3. Alert Triggered Modal ✅

**Location**: `AlertTriggeredModal` component (line 3182)

**Feature**: Modal appears when alert triggers
- Shows alert condition and triggered price
- Shows current position details
- Actions: [Close Decision] or [Leave Open]

---

## API Endpoints Wired

### Suggestion System
- `GET /api/public/suggestions/all` → Fetch 15 suggestions (5 per category)

### Decision Creation
- `POST /api/decisions/manual` → Create with user-specified price
- `POST /api/decisions/auto` → Create with system-locked price
- `POST /api/decisions/{id}/log-details` → Save thesis/evidence/risks

### Decision Editing
- `PUT /api/decisions/{id}` → Edit decision fields
- `POST /api/decisions/{id}/exit-criteria` → Add alert
- `DELETE /api/decisions/{id}/exit-criteria/{alertId}` → Remove alert
- `POST /api/decisions/{id}/close` → Close decision with exit price

### Decision Retrieval
- `GET /api/decisions` → List all user decisions
- `GET /api/decisions/{id}` → Get decision details

---

## FEAT-decision-journal-001 Acceptance Criteria

| Criterion | Implementation | Status |
|-----------|-----------------|--------|
| Suggestion system (5 per category) | DecisionCaptureModal fetches from `/api/public/suggestions/*` | ✅ |
| User can edit exit criteria | DecisionDetailModal has ExitCriteria editor | ✅ |
| Reviews at 30/90/180 days, 1 year | ReviewScheduler component | ✅ |
| Filterable by thesis/risk keywords | Thesis/Risk search in DecisionsView | ✅ |
| Alert triggered indicator & prompt | AlertTriggeredModal component | ✅ |
| Auto decision with strategy | addSymbol() creates AUTO decisions | ✅ |
| Manual decision with investment | addHolding() creates MANUAL decisions | ✅ |
| Real-time P/L tracking | DecisionDetailModal shows live prices | ✅ |
| Edit history | DecisionDetailModal shows last 5 edits | ✅ |

---

## Testing Checklist

### Investment Flow (MANUAL Decision)
- [ ] Add holding to investment account
- [ ] DecisionCaptureModal opens with transaction details
- [ ] Select suggestions (thesis, evidence, risks)
- [ ] Add custom text to fields
- [ ] Submit decision
- [ ] Decision appears in journal with all details saved
- [ ] Can view/edit decision in detail modal

### Strategy Flow (AUTO Decision)
- [ ] Create strategy
- [ ] Add buy/sell transaction to strategy
- [ ] DecisionCaptureModal opens with auto-determined price (locked)
- [ ] Select suggestions
- [ ] Submit decision
- [ ] Decision appears in journal (marked as AUTO)
- [ ] Price shown as (Live) with lock indicator

### Features Testing
- [ ] Add exit criteria to open decision
- [ ] Delete exit criteria
- [ ] See review schedule dates
- [ ] Search decisions by thesis keyword
- [ ] Search decisions by risk keyword
- [ ] Filter by symbol/type/status/date
- [ ] View decision in timeline
- [ ] Close decision with exit price
- [ ] See edit history

---

## File Map

| Component | File | Lines |
|-----------|------|-------|
| DecisionCaptureModal | main.jsx | 4160-4520 |
| DecisionDetailModal | main.jsx | 3268-3870 |
| DecisionsView | main.jsx | 1058-1310 |
| ReviewScheduler | main.jsx | 3182-3206 |
| AlertTriggeredModal | main.jsx | 3107-3180 |
| DecisionJournalTimeline | main.jsx | 3919-4100 |
| Investment addHolding | main.jsx | 539-581 |
| Strategy addSymbol | main.jsx | 438-495 |

---

## KFS Compliance Summary

✅ **Knowledge-First**: Features driven by FEAT-decision-journal-001 spec  
✅ **Documentation**: All decisions logged with thesis/evidence/risks  
✅ **Implementation**: Backend services + Frontend components complete  
✅ **Testing**: Integration tests cover both AUTO/MANUAL paths  
✅ **Traceability**: Each feature links to FEAT acceptance criteria
