# Phase 7: E2E Testing Plan - Decision Journal with Phase 7A Features

**Test Execution Date**: 2026-06-09  
**Tester**: QA Team  
**Coverage**: All Phase 7A features + original decision journal flows  
**Status**: Ready for execution

---

## Test Scope

This E2E test plan covers:
- ✅ Suggestion System (Sprint 1)
- ✅ Exit Criteria Editor (Sprint 2)
- ✅ Review Scheduler (Sprint 3)
- ✅ Search/Filter (Sprint 4)
- ✅ Alert UX (Sprint 5)
- ✅ Investment section (MANUAL decisions)
- ✅ Strategy section (AUTO decisions)

---

## Test Environment Setup

### Prerequisites
1. Start all services: `docker-compose up -d`
2. Verify backend is running: `curl http://localhost:8081/api/public/suggestions/thesis`
3. Verify frontend is running: `http://localhost:3000`
4. Login as: `alice` / `password123`
5. Clear browser cache (F12 → Application → Clear storage)

### Data Reset
```bash
# Optional: Reset database between test runs
docker-compose exec postgres psql -U postgres -d idp \
  -c "DELETE FROM investment_decision_alerts; 
      DELETE FROM investment_decisions;"
```

---

## Test Cases

### TC-01: Suggestion System - API Integration

**Objective**: Verify suggestion endpoints return correct data

**Steps**:
1. Open browser DevTools (F12 → Network tab)
2. Navigate to any page
3. Check Network tab for API calls

**Expected Results**:
- ✅ GET `/api/public/suggestions/thesis` returns 200 with 5 suggestions
- ✅ GET `/api/public/suggestions/evidence` returns 200 with 5 suggestions
- ✅ GET `/api/public/suggestions/risks` returns 200 with 5 suggestions
- ✅ Each response is properly formatted JSON

**Test Data**:
```bash
curl -s http://localhost:8081/api/public/suggestions/all | python3 -m json.tool
# Should show:
# {
#   "thesis": ["...", "...", "...", "...", "..."],
#   "evidence": ["...", "...", "...", "...", "..."],
#   "risks": ["...", "...", "...", "...", "..."]
# }
```

**Pass/Fail**: _____ 

---

### TC-02: Manual Decision Creation with Suggestions

**Objective**: Create decision from Investment section with suggestions

**Steps**:
1. Go to Investment workspace
2. Click "Manage Accounts and Holdings"
3. Create new brokerage account: "Test Account"
4. Click "Add Holding"
5. Fill form:
   - Ticker: `AAPL`
   - Shares: `100`
   - Cost Basis: `150.00`
6. Click "Add Holding"

**Expected Results**:
- ✅ Holding appears in Holdings table
- ✅ DecisionCaptureModal opens automatically
- ✅ Modal shows title "Log Investment Decision"
- ✅ Modal shows "BUY 100 shares of AAPL at $150.00"

**Pass/Fail**: _____ 

---

### TC-03: Suggestion Selection - Thesis

**Objective**: Select thesis suggestion and verify in modal

**Steps** (continue from TC-02):
1. Scroll to "Thesis" section
2. Verify checkboxes appear with suggestions:
   - "Stock is undervalued (P/E or price/book below peers)"
   - "Technical breakout signal (price breaks resistance)"
   - "Momentum play (trend continuation)"
   - "Mean reversion (oversold indicator like RSI < 30)"
   - "Matches my investment strategy or watchlist criteria"
3. Check first checkbox: "Stock is undervalued..."
4. Verify checkbox is checked

**Expected Results**:
- ✅ All 5 suggestions appear
- ✅ Checkbox toggles on/off correctly
- ✅ Can select multiple suggestions
- ✅ Custom text field below suggestions

**Pass/Fail**: _____ 

---

### TC-04: Suggestion Selection - Evidence & Risks

**Objective**: Test multi-select checkboxes for evidence and risks

**Steps** (continue from TC-03):
1. Scroll to "Evidence" section
2. Check 2-3 evidence suggestions
3. Scroll to "Risks" section
4. Check 2-3 risk suggestions
5. Verify all selections remain checked

**Expected Results**:
- ✅ Evidence shows 5 checkboxes (can select multiple)
- ✅ Risks shows 5 checkboxes (can select multiple)
- ✅ All selections persist
- ✅ Custom text fields available for each section

**Pass/Fail**: _____ 

---

### TC-05: Custom Text Input

**Objective**: Verify custom text can be entered alongside suggestions

**Steps** (continue from TC-04):
1. In "Thesis" section, enter custom text: "Custom thesis reason"
2. In "Evidence" section, enter custom text: "Custom evidence point"
3. In "Risks" section, enter custom text: "Custom risk factor"
4. Verify text is saved in input fields

**Expected Results**:
- ✅ Custom text input fields are editable
- ✅ Text persists in fields
- ✅ Can mix suggestions + custom text
- ✅ Draft auto-saves to localStorage

**Pass/Fail**: _____ 

---

### TC-06: Decision Submission - Suggestions Combined

**Objective**: Submit decision and verify suggestions are saved

**Steps** (continue from TC-05):
1. Verify modal shows:
   - Thesis with 1 selected suggestion
   - Evidence with 2-3 selected suggestions
   - Risks with 2-3 selected suggestions
2. Optional: Add comment: "Test decision"
3. Click "Save Decision" button
4. Wait for modal to close

**Expected Results**:
- ✅ Submit button is enabled
- ✅ Modal closes on successful submission
- ✅ Notice message shows: "Decision captured..."
- ✅ Investment workspace reloads
- ✅ No JavaScript errors in console (F12)

**Pass/Fail**: _____ 

---

### TC-07: Verify Decision in Journal

**Objective**: Confirm decision appears in Decision Journal

**Steps**:
1. Click "Decision Journal" in main nav
2. Look for decision with symbol "AAPL" in timeline
3. Click on decision card to open detail view

**Expected Results**:
- ✅ Decision appears in journal
- ✅ Shows transaction: "BUY 100 shares of AAPL at $150.00"
- ✅ Shows combined thesis (suggestion + custom)
- ✅ Shows combined evidence
- ✅ Shows combined risks
- ✅ Shows comment if added

**Pass/Fail**: _____ 

---

### TC-08: Review Scheduler Display

**Objective**: Verify review dates appear in decision detail

**Steps** (continue from TC-07):
1. In Decision Detail modal, scroll down
2. Look for "Review Schedule" section
3. Verify 4 review dates are shown:
   - 30-Day Review
   - 90-Day Review
   - 180-Day Review
   - 1-Year Review

**Expected Results**:
- ✅ Review Schedule section appears below transaction details
- ✅ Shows all 4 review dates
- ✅ Each shows date (MM/DD/YYYY format)
- ✅ Shows "days until" or "Overdue" status
- ✅ Color coding: blue (pending), red (overdue)

**Pass/Fail**: _____ 

---

### TC-09: Exit Criteria Editor - Add Alert

**Objective**: Create exit criteria alert

**Steps** (continue from TC-08):
1. Scroll to "Exit Criteria & Alerts" section
2. Click "+ Add Exit Criteria" button
3. Form appears with:
   - Condition Type dropdown
   - Condition Value input
   - Description input
4. Select: "Price ≥ (take profit)"
5. Enter Value: `165.00`
6. Enter Description: "Take profit at $165"
7. Click "Add Criteria"

**Expected Results**:
- ✅ Form appears inline
- ✅ Dropdown shows 3 options (PRICE_ABOVE, PRICE_BELOW, PRICE_AT)
- ✅ Value input accepts numbers
- ✅ Description input accepts text
- ✅ Add button creates alert
- ✅ Form closes after creation

**Pass/Fail**: _____ 

---

### TC-10: Exit Criteria - Display & Delete

**Objective**: Verify alert displays and can be deleted

**Steps** (continue from TC-09):
1. Verify alert appears in list:
   - Shows "Take profit at $165"
   - Shows status "Pending" (blue badge)
2. Click "Remove" button on the alert
3. Verify alert is deleted from list

**Expected Results**:
- ✅ Alert displays with full description
- ✅ Status shows "Pending" in blue
- ✅ Remove button is clickable
- ✅ Alert disappears after deletion
- ✅ List updates immediately

**Pass/Fail**: _____ 

---

### TC-11: Search/Filter - Thesis Keyword Search

**Objective**: Filter decisions by thesis keywords

**Steps**:
1. Go to Decision Journal view
2. Look for filter row with multiple inputs
3. In "Search thesis, evidence, or risks..." field, type: "undervalued"
4. Press Enter or wait for auto-filter

**Expected Results**:
- ✅ Search input field appears
- ✅ Decisions are filtered in real-time
- ✅ Only decisions containing "undervalued" appear
- ✅ Decision count updates
- ✅ Clear button appears next to search

**Pass/Fail**: _____ 

---

### TC-12: Search/Filter - Risk Keyword Search

**Objective**: Filter by risk keywords

**Steps** (continue from TC-11):
1. Clear previous search (click Clear button)
2. Type "correction" in search field
3. Verify decisions are filtered to those with "correction" in risks

**Expected Results**:
- ✅ Search filters by risk keywords
- ✅ Both upper and lowercase work
- ✅ Partial matches work (e.g., "correct" finds "correction")
- ✅ Decision count updates

**Pass/Fail**: _____ 

---

### TC-13: Combined Filters

**Objective**: Verify multiple filters work together

**Steps**:
1. Set Symbol filter: "AAPL"
2. Set Type filter: "BUY"
3. Set Status filter: "open"
4. Set Thesis/Risk search: "undervalued"
5. Verify results

**Expected Results**:
- ✅ All 4 filters apply simultaneously
- ✅ Results show only AAPL BUY decisions that are open with "undervalued"
- ✅ Decision count reflects combined filters
- ✅ Clear All button resets all filters

**Pass/Fail**: _____ 

---

### TC-14: Strategy AUTO Decision Creation

**Objective**: Create AUTO decision from strategy execution

**Steps**:
1. Go to Strategy section
2. Create new strategy: "Test Strategy"
3. Add symbol to strategy:
   - Action: Buy
   - Symbol: MSFT
   - Quantity: 50
4. Click "Add Transaction"

**Expected Results**:
- ✅ Transaction created
- ✅ DecisionCaptureModal opens with "isAuto" indicator
- ✅ Price shown as system-determined (locked)
- ✅ Modal shows "(Live)" price indicator
- ✅ All Phase 7A features available

**Pass/Fail**: _____ 

---

### TC-15: Decision Submission & Log-Details

**Objective**: Verify decision details are persisted via log-details endpoint

**Steps** (from TC-14):
1. In DecisionCaptureModal:
   - Select thesis suggestion
   - Add custom evidence
   - Select risks suggestion
2. Click "Save Decision"
3. Switch to Decision Journal
4. Find MSFT decision
5. Open detail view

**Expected Results**:
- ✅ Modal closes successfully
- ✅ Decision appears in journal (AUTO source)
- ✅ Thesis shows selected suggestion
- ✅ Evidence shows custom text
- ✅ Risks shows selected suggestion
- ✅ All combined correctly with "; " separator

**Test**: Verify via API
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/decisions \
  | grep -A 5 "MSFT"
# Should show thesis/evidence/risks populated (not null)
```

**Pass/Fail**: _____ 

---

### TC-16: Edit Decision Details

**Objective**: Edit decision thesis/evidence/risks in detail view

**Steps**:
1. Open any decision in detail view
2. Look for "Edit" button or edit mode
3. Change thesis: append " - Updated"
4. Change evidence: "New evidence point"
5. Click "Save Changes"

**Expected Results**:
- ✅ Edit form appears with textarea inputs
- ✅ Can modify thesis/evidence/risks/comments
- ✅ Save button enabled
- ✅ Changes persist
- ✅ Edit history updated

**Pass/Fail**: _____ 

---

### TC-17: Edit History Display

**Objective**: Verify edits are tracked

**Steps** (continue from TC-16):
1. In decision detail view, scroll to "Edit History"
2. Verify latest edit appears:
   - Shows field name (e.g., "thesis")
   - Shows timestamp
   - Shows preview of change

**Expected Results**:
- ✅ Edit History section displays
- ✅ Shows up to 5 most recent edits
- ✅ Each edit shows: Date, Field, Preview
- ✅ Newest edits appear first

**Pass/Fail**: _____ 

---

### TC-18: Close Decision Flow

**Objective**: Close decision with exit price

**Steps**:
1. In open decision detail view
2. Click "Close Decision" button
3. Form appears asking for exit price
4. Enter exit price: `160.00`
5. Select close reason: "Profit taking"
6. Click "Close Decision"

**Expected Results**:
- ✅ Close form appears
- ✅ Shows entry vs exit summary
- ✅ Shows P/L preview (updates as you type)
- ✅ Close button enabled
- ✅ Decision closes successfully
- ✅ Final P/L calculated and shown

**Pass/Fail**: _____ 

---

### TC-19: Closed Decision Read-Only

**Objective**: Verify closed decisions are read-only

**Steps** (continue from TC-18):
1. Click on closed decision to open detail view
2. Verify edit button is disabled/hidden
3. Try to click edit areas (should not open form)

**Expected Results**:
- ✅ Closed decisions show as read-only cards
- ✅ Edit button is disabled
- ✅ Cannot modify closed decision
- ✅ Shows close date and final P/L
- ✅ Shows exit price and reason

**Pass/Fail**: _____ 

---

### TC-20: Real-time P/L Tracking

**Objective**: Verify P/L updates in real-time

**Steps**:
1. Open decision detail with current price
2. Look for "Current Price" showing "(Live)" indicator
3. Wait 5-10 seconds
4. Verify price updates periodically

**Expected Results**:
- ✅ Shows current market price
- ✅ Shows "(Live)" indicator
- ✅ P/L updates without refresh
- ✅ Flash animation on >$50 change (optional)
- ✅ Shows unrealized gain/loss
- ✅ Shows percentage change

**Pass/Fail**: _____ 

---

## Test Execution Summary

### Browser Testing Checklist

- [ ] Tested in Chrome (latest)
- [ ] Tested in Firefox (latest)
- [ ] Tested in Safari (if available)
- [ ] Responsive design check (mobile view)
- [ ] No JavaScript errors in console
- [ ] No network 5xx errors
- [ ] All images/icons load correctly

### Performance Checks

- [ ] Suggestion API responds < 200ms
- [ ] Decision creation submits < 2 seconds
- [ ] Journal loads < 3 seconds
- [ ] Search filters apply < 500ms
- [ ] Modal opens/closes smoothly

### Edge Cases

- [ ] Create decision without suggestions (skip all checkboxes)
- [ ] Create decision with only custom text (no suggestions)
- [ ] Create decision with special characters in thesis
- [ ] Add multiple exit criteria (5+)
- [ ] Edit decision repeatedly (verify edit history)
- [ ] Close and reopen decision (verify read-only persists)

---

## Issues Found

| # | Component | Issue | Severity | Status |
|---|-----------|-------|----------|--------|
| 1 | | | | |
| 2 | | | | |
| 3 | | | | |

---

## Sign-Off

**Tester Name**: ________________  
**Date**: ________________  
**Result**: ☐ PASS ☐ FAIL  

**Comments/Notes**:
```
[Test execution notes]
```

**Approver**: ________________

---

## Notes

- All test steps assume clean database
- Each test case is independent and can run in any order
- Success criteria must ALL pass for test case to pass
- Screenshots should be captured for any failures
- Keep this plan updated with actual results
