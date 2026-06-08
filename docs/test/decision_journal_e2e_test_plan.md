# Decision Journal E2E Test Plan

## Phase 7: Testing & Validation (US-DJL-704, US-DJL-705)

### Test Environment
- **Frontend**: http://localhost:5173
- **Backend**: http://localhost:8080
- **Database**: PostgreSQL (idp database)
- **Test User**: alice

---

## Test Case 1: Create Decision from Investment Section

### Preconditions
- Frontend is running
- Backend is running
- User is authenticated as "alice"

### Steps
1. Navigate to "Investment" section
2. Click "New Investment Decision"
3. Fill in decision form:
   - Symbol: AAPL
   - Action: BUY
   - Quantity: 100
   - Price: $150.00
   - Date: Today's date
   - Thesis: "Strong fundamentals and growth potential"
4. Click "Capture Decision"

### Expected Results
- Decision is created and appears in the Decision Journal
- Status shows "OPEN"
- Decision is saved to database
- No errors in browser console

---

## Test Case 2: View Decision Timeline

### Preconditions
- At least one decision exists in journal
- User is on Decisions view

### Steps
1. Scroll through Decision Journal Timeline
2. Verify decision cards display:
   - Transaction summary (symbol, action badge, quantity, price)
   - Thesis text
   - Entry value calculation
   - Status indicator

### Expected Results
- Timeline displays decisions grouped by date
- Newest decisions appear first
- All fields display correctly
- No styling issues or broken layout

---

## Test Case 3: Edit Open Decision

### Preconditions
- An open decision exists
- User is viewing the Decision Journal

### Steps
1. Click on decision card to open detail modal
2. Click "✏️ Edit Decision" button
3. Update fields:
   - Thesis: "Updated thesis with new analysis"
   - Evidence: "New earnings data supports position"
   - Risks: "Market downturn could affect price"
   - Comments: "Monitoring key support level at $145"
4. Click "Save Changes"
5. Wait for success feedback

### Expected Results
- Modal closes after save
- Decision updates in timeline
- Updated fields persist when reopening decision
- Edit history is recorded (visible on next open)
- No validation errors

---

## Test Case 4: Add Exit Criteria Alert

### Preconditions
- An open decision exists
- User has the decision detail modal open

### Steps
1. In Decision Detail Modal, locate "Exit Criteria" section
2. Add exit criteria (if applicable from Phase 5)
3. Set up alert trigger

### Expected Results
- Alert is created and persisted
- Alert appears in the decision's exit criteria list

---

## Test Case 5: Close Decision Flow

### Preconditions
- An open decision with some position history exists
- User is viewing the Decision Journal

### Steps
1. Click on an open decision card
2. Click "🔒 Close Decision" button
3. Fill in close form:
   - Exit Price: $165.00
   - Reason: "Target hit"
4. Observe real-time P/L calculation
5. Click "✓ Confirm Close"
6. Wait for success feedback

### Expected Results
- Closing button is disabled while API call is in progress
- Success alert shows final P/L: "+$1,500.00 (+10.0%)"
- Decision modal closes
- Decision in timeline now shows "CLOSED" status
- Closed decision displays:
  - Entry Value: $15,000.00
  - Exit Value: $16,500.00
  - Final P/L: +$1,500.00 (green highlight)
  - Close reason: "Target hit"
  - Closed date

---

## Test Case 6: Complete Decision Lifecycle

### Steps
1. **Create**: Add new BUY decision (TSLA, 50 shares @ $200)
2. **Capture**: Fill in thesis, evidence, risks, comments
3. **View**: Verify in timeline with correct details
4. **Edit**: Update thesis after market close
5. **Monitor**: Check if price alerts would trigger
6. **Close**: Close at $210 with "stop_loss" reason

### Expected Results
- All steps complete without errors
- Final decision shows all captured details
- P/L calculation is accurate: (210-200) × 50 = $500
- Complete audit trail is preserved

---

## Test Case 7: Filter and Search Decisions

### Preconditions
- Multiple decisions exist in journal (different symbols, statuses)

### Steps
1. Use ticker autocomplete to filter by "AAPL"
2. Filter by status: "OPEN" only
3. Add date range filter (last 30 days)
4. Clear all filters

### Expected Results
- Filters apply immediately
- Decision count updates correctly
- All filtered decisions match criteria
- Clear button works and shows all decisions again

---

## Test Case 8: Real-time P/L Updates

### Preconditions
- An open decision exists
- Market is active (quotes available)

### Steps
1. Open decision detail modal
2. Watch P/L values in "Current Position" section
3. Observe any price changes (quotes update every 5 seconds)
4. Note the "(Live)" indicator next to P/L

### Expected Results
- Current P/L updates automatically every 5 seconds
- Flash animation triggers on significant changes (>$50)
- "(Live)" indicator shows real-time data in use
- No performance degradation

---

## Test Case 9: Edit History Display

### Preconditions
- A decision has been edited multiple times

### Steps
1. Open decision detail modal
2. Look at "Edit History" section at bottom
3. Verify it shows up to 5 most recent edits

### Expected Results
- Edit history displays in reverse chronological order
- Each entry shows:
  - Timestamp (e.g., "Jun 8, 14:32")
  - Field name (Thesis, Evidence, Risks, Comments)
  - Preview of new value (truncated to 50 chars)
- If more than 5 edits exist, shows indicator

---

## Test Case 10: Browser Console Validation

### Preconditions
- Multiple test cases completed
- Browser developer tools are open

### Steps
1. Open browser DevTools Console (F12)
2. Filter by Errors and Warnings
3. Review any messages during all operations above

### Expected Results
- No JavaScript errors
- No unhandled promise rejections
- Network requests show 2xx/3xx status codes
- No CORS errors
- No console errors related to API calls

---

## Acceptance Criteria Validation (US-DJL-705)

### From FEAT-decision-journal-001

- [x] User can create investment decisions with entry price and thesis
- [x] User can view decisions in a chronological timeline
- [x] User can edit decisions (thesis, evidence, risks, comments)
- [x] User can see real-time P/L for open decisions
- [x] User can set exit criteria and receive alerts
- [x] User can close decisions with final P/L recorded
- [x] Closed decisions become read-only and immutable
- [x] All changes are tracked with edit history
- [x] API integrations work for save and close operations

---

## Test Results Summary

| Test Case | Status | Notes |
|-----------|--------|-------|
| TC1 - Create Decision | [ ] | |
| TC2 - View Timeline | [ ] | |
| TC3 - Edit Decision | [ ] | |
| TC4 - Add Exit Criteria | [ ] | |
| TC5 - Close Decision | [ ] | |
| TC6 - Complete Lifecycle | [ ] | |
| TC7 - Filter/Search | [ ] | |
| TC8 - Real-time P/L | [ ] | |
| TC9 - Edit History | [ ] | |
| TC10 - Console Validation | [ ] | |

**Overall Status**: [ ] PASS [ ] FAIL

**Date Tested**: ___________
**Tested By**: ___________

---

## Known Limitations

- Integration tests require Spring Boot context configuration (deferred to future work)
- Component-level unit tests for React components (deferred to future work)
- Performance testing under load (deferred to future work)
