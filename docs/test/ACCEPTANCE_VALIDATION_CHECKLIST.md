# Acceptance Validation Checklist - US-DJL-705

**Document**: FEAT-decision-journal-001 Acceptance Criteria Validation  
**Date**: 2026-06-09  
**Status**: Ready for Validation  
**Reference**: KFS decision_journal_implementation_plan.md

---

## Overview

This checklist validates that the Decision Journal implementation meets all acceptance criteria from the FEAT-decision-journal-001 specification. Each criterion maps to observable behavior that users can verify.

---

## Acceptance Criteria Validation

### Criterion 1: Decision Capture - Automatic Triggering

**FEAT Requirement** (Line 37-40):
> "Decision is automatically captured when user performs buy/sell transaction in Investment or Strategy sections. Modal appears with transaction pre-filled."

**Validation Steps**:

1. **Investment Section (MANUAL Decision)**
   - [ ] Go to Investment workspace
   - [ ] Add holding (buy transaction)
   - [ ] DecisionCaptureModal appears automatically
   - [ ] Modal shows transaction details pre-filled
   - [ ] Symbol: correct ✓
   - [ ] Action: BUY ✓
   - [ ] Quantity: correct ✓
   - [ ] Price: user-specified ✓

2. **Strategy Section (AUTO Decision)**
   - [ ] Go to Strategy workspace
   - [ ] Execute transaction (buy/sell)
   - [ ] DecisionCaptureModal appears automatically
   - [ ] Modal shows transaction details pre-filled
   - [ ] Symbol: correct ✓
   - [ ] Action: BUY/SELL ✓
   - [ ] Quantity: correct ✓
   - [ ] Price: system-determined (locked) ✓
   - [ ] Shows "(Live)" indicator ✓

**Evidence**:
- Screenshots showing modal appearance
- Browser console: no errors
- Network tab: successful API calls

**Status**: ☐ PASS ☐ FAIL

**Notes**: 
```
[Validation notes]
```

---

### Criterion 2: Suggestion System - 5 Options per Category

**FEAT Requirement** (Line 66-81):
> "5 pre-populated suggestions for Thesis, Evidence, Risks. User can select from suggestions or add custom text. Suggestions fetched from backend."

**Validation Steps**:

1. **API Returns Correct Data**
   - [ ] GET `/api/public/suggestions/all` returns 200
   - [ ] Response includes "thesis" array with 5 items
   - [ ] Response includes "evidence" array with 5 items
   - [ ] Response includes "risks" array with 5 items
   - [ ] Each suggestion is a non-empty string

   Test:
   ```bash
   curl http://localhost:8081/api/public/suggestions/all | python3 -m json.tool
   ```

2. **UI Displays Suggestions**
   - [ ] Thesis section shows 5 checkboxes
   - [ ] Evidence section shows 5 checkboxes
   - [ ] Risks section shows 5 checkboxes
   - [ ] Each checkbox has label with suggestion text
   - [ ] Custom text input available below suggestions

3. **User Can Select Suggestions**
   - [ ] Can click/check thesis suggestion
   - [ ] Can check multiple evidence suggestions
   - [ ] Can check multiple risk suggestions
   - [ ] Selection state persists
   - [ ] Can toggle selections on/off

4. **Custom Text Works**
   - [ ] Can enter custom text in thesis field
   - [ ] Can enter custom text in evidence field
   - [ ] Can enter custom text in risks field
   - [ ] Custom text persists
   - [ ] Can use suggestions + custom text together

**Evidence**:
- Screenshots of suggestion checkboxes
- API response showing 5 items per category
- Submitted decision showing combined text

**Status**: ☐ PASS ☐ FAIL

**Notes**:
```
[Validation notes]
```

---

### Criterion 3: Suggestion Submission

**FEAT Requirement** (Line 82-101):
> "When user submits decision, selected suggestions are combined with custom text using '; ' separator and saved."

**Validation Steps**:

1. **Selection Combination Logic**
   - [ ] Select 1 thesis suggestion
   - [ ] Add custom thesis text
   - [ ] Submit decision
   - [ ] In journal, thesis shows: "Suggestion; CustomText"

2. **Multiple Selections**
   - [ ] Select 2 evidence suggestions
   - [ ] Add custom evidence text
   - [ ] Submit decision
   - [ ] In journal, shows: "Suggestion1; Suggestion2; CustomText"

3. **Empty Selections**
   - [ ] Submit with only custom text (no suggestions)
   - [ ] Submit with only suggestions (no custom text)
   - [ ] Submit with nothing (field is optional)
   - [ ] All variations work without error

**Evidence**:
- Decision detail view showing combined text
- Database query showing "; " separator used
- No null values when custom text provided

**Status**: ☐ PASS ☐ FAIL

**Notes**:
```
[Validation notes]
```

---

### Criterion 4: Exit Criteria Management

**FEAT Requirement** (Line 40, 72-81):
> "User CAN edit exit criteria. Alerts show condition type and value. User can add/remove alerts."

**Validation Steps**:

1. **Add Exit Criteria**
   - [ ] In decision detail, click "+ Add Exit Criteria"
   - [ ] Form appears with:
     - [ ] Condition Type dropdown (PRICE_ABOVE, PRICE_BELOW, PRICE_AT)
     - [ ] Condition Value input (numeric)
     - [ ] Description input (optional)
   - [ ] Can enter price target
   - [ ] Click "Add" creates alert
   - [ ] Alert appears in list

2. **Display Exit Criteria**
   - [ ] Alert shows description or "Type $Value"
   - [ ] Status badge shows: PENDING/TRIGGERED/CLOSED
   - [ ] Multiple alerts can be displayed
   - [ ] Edit history shows alert changes

3. **Delete Exit Criteria**
   - [ ] Click "Remove" button on alert
   - [ ] Alert deleted from list
   - [ ] List updates immediately
   - [ ] No page refresh needed

4. **Edit After Creation**
   - [ ] Add alert
   - [ ] Edit decision's other fields (thesis, etc.)
   - [ ] Alert persists
   - [ ] Can add more alerts
   - [ ] Can delete alerts

**Evidence**:
- Screenshots of add/delete flows
- Alert appears in decision detail
- Edit history shows alert operations
- Database shows alert records created

**Status**: ☐ PASS ☐ FAIL

**Notes**:
```
[Validation notes]
```

---

### Criterion 5: Review Schedule

**FEAT Requirement** (Line 78-79):
> "Reviews available at 30/90/180 days and 1 year."

**Validation Steps**:

1. **Display Review Dates**
   - [ ] Open any decision detail
   - [ ] "Review Schedule" section visible
   - [ ] Shows 4 review dates:
     - [ ] 30-Day Review (date shown)
     - [ ] 90-Day Review (date shown)
     - [ ] 180-Day Review (date shown)
     - [ ] 1-Year Review (date shown)

2. **Date Calculations**
   - [ ] Decision created on 2026-06-09
   - [ ] 30-day review = 2026-07-09 (approx)
   - [ ] 90-day review = 2026-09-07 (approx)
   - [ ] 180-day review = 2026-12-06 (approx)
   - [ ] 1-year review = 2027-06-09

3. **Status Indicators**
   - [ ] Pending reviews show "Pending" badge (blue)
   - [ ] Overdue reviews show "Overdue" badge (red)
   - [ ] Shows days until review for pending
   - [ ] Color coding works correctly

**Evidence**:
- Screenshots showing 4 dates
- Date format: MM/DD/YYYY
- Math verification for 30/90/180/365 day calculations

**Status**: ☐ PASS ☐ FAIL

**Notes**:
```
[Validation notes]
```

---

### Criterion 6: Alert Triggered Indicators

**FEAT Requirement** (Line 73):
> "Visual indicator when exit alerts trigger (status badge, color change)."

**Validation Steps**:

1. **Alert Status Display**
   - [ ] Alert shows status: PENDING (blue)
   - [ ] Alert shows status: TRIGGERED (red with 🎯)
   - [ ] Alert shows status: CLOSED (gray)
   - [ ] Color coding is clear and distinct

2. **Triggered Indicator**
   - [ ] When alert triggers, status changes to TRIGGERED
   - [ ] Red color used for triggered
   - [ ] 🎯 emoji appears (optional visual)
   - [ ] UI updates without page refresh

3. **Visual Hierarchy**
   - [ ] Pending alerts clearly visible
   - [ ] Triggered alerts stand out (color, icon)
   - [ ] User can easily identify action items

**Evidence**:
- Screenshots of different alert statuses
- Color hex codes verified
- Status updates in real-time

**Status**: ☐ PASS ☐ FAIL

**Notes**:
```
[Validation notes]
```

---

### Criterion 7: Alert Triggered Prompt

**FEAT Requirement** (Line 74):
> "User prompted to close or leave open when exit alert triggers."

**Validation Steps**:

1. **Triggered Alert Modal**
   - [ ] When alert triggers, modal appears
   - [ ] Modal shows alert details:
     - [ ] Alert condition (e.g., "Price ≥ $165")
     - [ ] Triggered price
     - [ ] Current position details
   - [ ] Two action buttons:
     - [ ] "✓ Close Decision" (primary, red)
     - [ ] "← Leave Open" (secondary)

2. **Action Workflows**
   - [ ] Click "Close Decision":
     - [ ] Modal opens close form
     - [ ] Triggered price pre-filled in exit price (optional)
     - [ ] User can adjust price if needed
     - [ ] Confirm closes decision
   - [ ] Click "Leave Open":
     - [ ] Modal closes
     - [ ] Decision remains open
     - [ ] Alert status stays TRIGGERED

3. **User Choice**
   - [ ] User has clear options
   - [ ] Both actions work without error
   - [ ] Decision state matches action taken

**Evidence**:
- Screenshots of AlertTriggeredModal
- Workflow execution showing both paths
- Decision state verification after actions

**Status**: ☐ PASS ☐ FAIL

**Notes**:
```
[Validation notes]
```

---

### Criterion 8: Search/Filter - Thesis & Risk Keywords

**FEAT Requirement** (Line 78):
> "Filterable by thesis and risk keywords."

**Validation Steps**:

1. **Thesis Search**
   - [ ] Go to Decision Journal
   - [ ] In search field, type: "undervalued"
   - [ ] Decisions with "undervalued" in thesis appear
   - [ ] Other decisions filtered out
   - [ ] Partial matches work ("under" finds "undervalued")
   - [ ] Case-insensitive ("UNDERVALUED" works)

2. **Risk Search**
   - [ ] Clear previous search
   - [ ] Type: "correction"
   - [ ] Decisions with "correction" in risks appear
   - [ ] Partial matches work
   - [ ] Case-insensitive works

3. **Evidence Search** (bonus)
   - [ ] Search also finds text in evidence field
   - [ ] Evidence keywords match too

4. **Combined Filters**
   - [ ] Search works with other filters (symbol, type, status, date)
   - [ ] All filters apply simultaneously
   - [ ] Decision count updates correctly

**Evidence**:
- Screenshots showing search in action
- Before/after filtering
- Multiple search term examples

**Status**: ☐ PASS ☐ FAIL

**Notes**:
```
[Validation notes]
```

---

### Criterion 9: Close Decision Flow

**FEAT Requirement** (Line 136-144):
> "User can close decision with exit price. System calculates final P/L. Closed decisions are read-only."

**Validation Steps**:

1. **Close Form**
   - [ ] Open open decision
   - [ ] Click "Close Decision" button
   - [ ] Form appears with:
     - [ ] Entry details (locked, read-only)
     - [ ] Exit price input
     - [ ] Close reason dropdown
     - [ ] Real-time P/L preview

2. **P/L Calculation**
   - [ ] Entry value shown: qty × entry price
   - [ ] Exit value updates: qty × exit price
   - [ ] P/L calculated: exit value - entry value
   - [ ] P/L % calculated: P/L / entry value × 100
   - [ ] Values update as you type exit price

3. **Close Confirmation**
   - [ ] Click "Close Decision"
   - [ ] Decision closes successfully
   - [ ] Final P/L saved
   - [ ] Close date recorded
   - [ ] Close reason saved

4. **Closed Decision State**
   - [ ] Closed decision appears as read-only card
   - [ ] Cannot click edit
   - [ ] Cannot modify any fields
   - [ ] Shows final P/L and close date
   - [ ] Shows close reason

**Evidence**:
- Screenshots of close form
- P/L calculations verified
- Decision shows read-only after close
- Database shows close_price and final_pnl populated

**Status**: ☐ PASS ☐ FAIL

**Notes**:
```
[Validation notes]
```

---

### Criterion 10: Real-time P/L Tracking

**FEAT Requirement** (Not explicitly in FEAT but in plan):
> "Current price shown with (Live) indicator. P/L updates without page refresh."

**Validation Steps**:

1. **Live Price Display**
   - [ ] Open decision detail
   - [ ] Shows "Current Price" field
   - [ ] Shows "(Live)" indicator
   - [ ] Shows current market price
   - [ ] Shows unrealized P/L
   - [ ] Shows percentage change

2. **Real-time Updates**
   - [ ] Wait 5 seconds without refresh
   - [ ] Price updates automatically
   - [ ] P/L updates automatically
   - [ ] No page reload needed
   - [ ] Updates every 5 seconds (approx)

3. **Flash Animation** (optional)
   - [ ] On price change > $50: background flashes
   - [ ] Flash is visible but not annoying
   - [ ] Draws attention to significant changes

**Evidence**:
- Screenshots of live price display
- Time-lapse showing updates over 10+ seconds
- Network tab showing API calls every 5 seconds

**Status**: ☐ PASS ☐ FAIL

**Notes**:
```
[Validation notes]
```

---

### Criterion 11: Edit History Tracking

**FEAT Requirement** (Line 310-312):
> "Edit history shows date, field, and action. Displays up to 5 recent edits."

**Validation Steps**:

1. **Edit Decision**
   - [ ] Open decision detail
   - [ ] Click Edit (or open edit mode)
   - [ ] Change thesis field
   - [ ] Click Save
   - [ ] Change evidence field
   - [ ] Click Save (twice now)

2. **History Display**
   - [ ] "Edit History" section shows edits
   - [ ] Shows most recent edits first
   - [ ] Each edit shows:
     - [ ] Timestamp (Month Day, HH:MM format)
     - [ ] Field name (thesis, evidence, risks, etc.)
     - [ ] Value preview (truncated)
   - [ ] Shows up to 5 edits
   - [ ] Indicates if >5 edits exist

3. **Edit Tracking**
   - [ ] Make 3+ edits
   - [ ] All appear in history
   - [ ] Oldest edits shown at bottom
   - [ ] Database shows edit records created

**Evidence**:
- Screenshots of edit history
- Timestamp format verification
- Multiple edits shown in correct order

**Status**: ☐ PASS ☐ FAIL

**Notes**:
```
[Validation notes]
```

---

### Criterion 12: MANUAL vs AUTO Decisions

**FEAT Requirement** (Implicit in plan):
> "MANUAL decisions (Investment) use user-specified price. AUTO decisions (Strategy) use system-locked price."

**Validation Steps**:

1. **MANUAL Decision (Investment)**
   - [ ] Create decision via Investment section
   - [ ] User enters price in holdings form
   - [ ] Decision uses that price (not market price)
   - [ ] Price can be edited if needed
   - [ ] No "(Live)" indicator

2. **AUTO Decision (Strategy)**
   - [ ] Create decision via Strategy section
   - [ ] System determines price (market price)
   - [ ] Price shown as locked/immutable
   - [ ] Shows "(Live)" indicator
   - [ ] Cannot edit price in decision
   - [ ] Modal clearly shows difference

3. **Decision Record**
   - [ ] MANUAL decisions show source: "manual"
   - [ ] AUTO decisions show source: "auto"
   - [ ] Database distinguishes them
   - [ ] API returns correct decision type

**Evidence**:
- Screenshots of MANUAL modal (price input)
- Screenshots of AUTO modal (price locked)
- API response showing decision source

**Status**: ☐ PASS ☐ FAIL

**Notes**:
```
[Validation notes]
```

---

## Test Environment Sign-Off

- [ ] All prerequisites met
- [ ] Backend services running
- [ ] Frontend accessible
- [ ] Test user created (alice/password123)
- [ ] Database in clean state

---

## Validation Results

### Summary

| Criterion | Status | Notes |
|-----------|--------|-------|
| 1. Auto-Capture | ☐ PASS ☐ FAIL | |
| 2. Suggestions | ☐ PASS ☐ FAIL | |
| 3. Submission | ☐ PASS ☐ FAIL | |
| 4. Exit Criteria | ☐ PASS ☐ FAIL | |
| 5. Reviews | ☐ PASS ☐ FAIL | |
| 6. Alert Indicator | ☐ PASS ☐ FAIL | |
| 7. Alert Prompt | ☐ PASS ☐ FAIL | |
| 8. Search/Filter | ☐ PASS ☐ FAIL | |
| 9. Close Flow | ☐ PASS ☐ FAIL | |
| 10. Live P/L | ☐ PASS ☐ FAIL | |
| 11. Edit History | ☐ PASS ☐ FAIL | |
| 12. MANUAL/AUTO | ☐ PASS ☐ FAIL | |

### Overall Status

- **All Criteria PASS**: ☐ Feature complete, ready to ship
- **Some Criteria FAIL**: ☐ Issues found, see below
- **Critical Issues**: ☐ Blocking issues found

---

## Issues & Blockers

| # | Criterion | Issue | Severity | Resolution |
|---|-----------|-------|----------|------------|
| 1 | | | HIGH/MED/LOW | |
| 2 | | | HIGH/MED/LOW | |
| 3 | | | HIGH/MED/LOW | |

---

## Sign-Off

**Validated By**: ________________  
**Date**: ________________  
**Time**: ________________  

**Overall Result**: ☐ PASS ☐ FAIL

**Signature**: ________________

---

## Appendix: Quick Test Commands

```bash
# Test suggestion endpoints
curl http://localhost:8081/api/public/suggestions/all

# Test decision creation
TOKEN=$(curl -s -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}' \
  | grep -o '"token":"[^"]*' | cut -d'"' -f4)

curl -X POST http://localhost:8081/api/decisions/manual \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"TEST","action":"BUY","quantity":100,"price":150,"transaction_date":"2026-06-09"}'

# Test log-details
# (Requires decision ID from above response)
curl -X POST http://localhost:8081/api/decisions/{ID}/log-details \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"thesis":"Test","evidence":"Test","risks":"Test"}'
```

