# KFS Implementation Gap Analysis: Decision Journal

**Date**: 2026-06-09  
**Status**: 🚨 Significant deviations from plan

---

## Summary

The Decision Journal implementation has significant deviations from the documented plan. The core functionality works, but the UI/UX design does not match the specified architecture.

**Deviation Count**: 12 major gaps across UI design, API structure, and user flows.

---

## Detailed Gap Analysis

### 1. DecisionCaptureModal - UI Design

**Planned Design** (from docs/implementation/decision_journal_implementation_plan.md, lines 207-257):
- Radio button selection for thesis suggestions (5 options + custom)
- Checkboxes for evidence suggestions (5 options + custom)
- Checkboxes for risks suggestions (5 options + custom)
- Structured exit criteria input (price_above, price_below, etc.)
- Clear "Save Decision" vs "Skip" buttons
- Modal explains "Log Investment Decision?" with transaction details locked

**Actual Implementation** (src/frontend/src/main.jsx, ~2350-2700):
- Simple textarea inputs for thesis, evidence, risks, comments
- ❌ No suggestion options or radio buttons
- ❌ No checkbox-based evidence/risks selection
- ❌ No structured exit criteria editor in capture modal
- ✅ Optional fields toggle (Phase 4)
- ✅ Draft auto-save with recovery (Phase 4)
- ✅ Skip button present

**Gap**: Design went from structured suggestions to free-form text entry

---

### 2. DecisionJournalView - Timeline Layout

**Planned Design** (lines 260-289):
```
Timeline with CARDS:
- Horizontal card layout
- Symbol badge (BUY/SELL)
- Transaction details on same line
- Thesis/Evidence/Risks snippets inline
- Alerts displayed as checklist in card
- Action buttons at bottom of card
```

**Actual Implementation** (src/frontend/src/main.jsx, ~2679-3074):
```
Timeline with VERTICAL LAYOUT:
- Grouped by DATE (newest first)
- Vertical timeline connector
- Date header with timeline dots
- Card content displayed vertically
- Alerts shown as badges with alert status
- No inline thesis snippet in timeline view
- Must click card to see full details
```

**Gap**: Card-based horizontal design → vertical grouped timeline

---

### 3. Exit Criteria Management

**Planned Design** (lines 250-254, 305-308):
- Structured format: `type` (price_above/below, pnl_above/below) + `value`
- Checkboxes to enable/disable alerts
- `[+ Add condition]` button to add more
- Displayed as structured list

**Actual Implementation**:
- ❌ No exit criteria editor in DecisionDetailModal
- ❌ Alerts shown but cannot add/remove from detail view
- ✅ Exit criteria can be added via separate endpoint
- ✅ Alerts show triggered status

**Gap**: Exit criteria should be editable in DecisionDetailView, not separate flow

---

### 4. DecisionDetailView - Information Architecture

**Planned Design** (lines 291-318):
- Locked title field at top
- Editable thesis/evidence/risks/comments below
- Exit criteria with checkboxes
- Edit history as list
- Current price & P/L at bottom
- Action buttons: [Save Changes] [Close Decision]

**Actual Implementation**:
- ✅ Locked transaction details (symbol, action, quantity, price)
- ✅ Editable thesis/evidence/risks/comments
- ✅ Edit history displayed (last 5)
- ✅ Current P/L shown with (Live) indicator
- ✅ Close decision flow with exit price input
- ❌ No exit criteria management in detail view
- ❌ No inline edit capabilities for exit criteria

**Gap**: Exit criteria visible but not editable in detail modal

---

### 5. API Endpoint Design

**Planned Endpoints** (lines 85-175):
- `POST /api/decisions` - Create decision
- `POST /api/decisions/{id}/log-details` - Log thesis/evidence/risks/comments
- `PUT /api/decisions/{id}` - Edit with exit_criteria array
- `POST /api/decisions/{id}/close` - Close with exit_price and close_reason
- `GET /api/decisions/journal` - Get paginated list with filters
- `GET /api/decisions/{id}` - Get single decision with edit_history

**Actual Implementation**:
- ✅ `POST /api/decisions/manual` - Create manual decision (different endpoint)
- ✅ `POST /api/decisions/auto` - Create from strategy (not in plan)
- ✅ `POST /api/decisions/{id}/log-details` - Log details
- ✅ `PUT /api/decisions/{id}` - Edit decision
- ✅ `POST /api/decisions/{id}/close` - Close decision
- ❌ `GET /api/decisions/journal` - Not implemented (added `GET /api/decisions` instead)
- ✅ `GET /api/decisions/{id}` - Get single decision
- ❌ Missing `POST /api/decisions/{id}/exit-criteria` for adding alerts
- ✅ `DELETE /api/decisions/{decisionId}/exit-criteria/{alertId}` - Remove alerts

**Gap**: Endpoint naming and exit criteria management differs from plan

---

### 6. Suggestion System

**Planned** (lines 223-246):
- Thesis: 5 predefined suggestions + custom option
- Evidence: 5 predefined suggestions with checkboxes + custom
- Risks: 5 predefined suggestions with checkboxes + custom
- Smart defaults shown as radio/checkboxes in modal

**Actual**:
- ❌ No suggestion system implemented
- ✅ Suggestion lists exist in code but not wired to UI
- Free-form textarea inputs only

**Gap**: Entire suggestion/defaults system not surfaced in UI

---

### 7. Exit Criteria Data Model

**Planned** (lines 74-82):
```
decision_alerts:
  condition_type: 'price_above', 'price_below', 'pnl_above', 'pnl_below'
  condition_value: DECIMAL
  status: 'pending', 'triggered', 'closed'
```

**Actual** (InvestmentDecisionAlert model):
```
AlertConditionType: PRICE_ABOVE, PRICE_BELOW, PRICE_AT, etc.
status: PENDING, TRIGGERED, CLOSED
```

**Gap**: Minor - enum naming differs (PRICE_ABOVE vs price_above)

---

### 8. Draft Persistence

**Planned**: Not explicitly documented

**Actual**:
- ✅ Phase 4 added localStorage draft auto-save
- ✅ Draft recovery button
- ✅ Clear draft button
- **No gap** - Feature enhanced beyond plan

---

### 9. Real-time P/L Updates

**Planned**: Not explicitly documented

**Actual**:
- ✅ Phase 5 added live quote fetching every 5 seconds
- ✅ Flash animation on >$50 changes
- ✅ "(Live)" indicator
- **No gap** - Feature enhanced beyond plan

---

### 10. Close Decision Flow

**Planned** (line 136-144):
- Simple POST to `/api/decisions/{id}/close`
- Expects exit_price and close_reason
- Returns closed decision (read-only)

**Actual**:
- ✅ Close form with exit price input
- ✅ Real-time P/L preview
- ✅ Close reason dropdown
- ✅ Closed decision shows as read-only card
- ✅ closeReason persisted to database
- **No gap** - Fully implemented and enhanced

---

### 11. Edit History Display

**Planned** (line 310-312):
- Simple list: Date, Field, Action
- Example: "Jun 7 14:35: Thesis updated"

**Actual**:
- ✅ Shows timestamp (Month Day, HH:MM)
- ✅ Shows field name
- ✅ Shows preview of new value (truncated to 50 chars)
- ✅ Shows up to 5 most recent edits
- ✅ Indicator for >5 edits
- **No gap** - Fully implemented and enhanced

---

### 12. Filtering & Search

**Planned** (line 147-155):
- Filters: symbol, date range, status, sort
- Query params: `?symbol=AAPL&from_date=2026-05-01&status=open`

**Actual** (Phase 5, US-DJL-503):
- ✅ Symbol filter with autocomplete
- ✅ Type filter (BUY/SELL/watch/avoid)
- ✅ Status filter (open/active/closed/archived)
- ✅ Date range filter (from/to dates, inclusive of end day)
- ✅ Shows live decision count
- ✅ Clear filters button
- **No gap** - Fully implemented and enhanced

---

## Recommendation

**Choice 1: Align Implementation to Plan** (High effort)
- Add suggestion system to DecisionCaptureModal
- Refactor timeline from vertical to card-based horizontal layout
- Move exit criteria editing into DecisionDetailModal
- Rename endpoints to match plan (journal vs decisions)
- **Effort**: ~3-5 days
- **Benefit**: Full KFS compliance, consistency with docs

**Choice 2: Update Plan to Reflect Implementation** (Low effort)
- Update docs/implementation/decision_journal_implementation_plan.md
- Document actual UI layout (vertical timeline)
- Document actual endpoint naming
- Document that suggestions are optional (not in MVP)
- Remove unimplemented features from design specs
- **Effort**: ~4 hours
- **Benefit**: Single source of truth, KFS compliant going forward

**Choice 3: Hybrid - Phased Alignment** (Medium effort)
- Keep current implementation as MVP (Phase 1-6 complete)
- Plan Phase 7+ for UI enhancements per original design
- Update plan to show MVP vs full feature scope
- **Effort**: ~1 hour (plan update) + 3-5 days (future work)
- **Benefit**: Ship MVP now, iterate toward full design

**Recommendation**: **Choice 2** (Update plan) in near term, then **Phase 7+** for UI/UX refinements per original design. The core functionality is solid; the deviation is primarily in UI presentation.

---

## Action Items

- [ ] Clarify with user which approach to take (1, 2, or 3)
- [ ] If Choice 1: Create Phase 7-8 for UI refinements
- [ ] If Choice 2: Update decision_journal_implementation_plan.md with actual design
- [ ] Update FEAT-decision-journal-001 in knowledge/specs/ if scope changed
- [ ] Validate against KFS traceability rules

