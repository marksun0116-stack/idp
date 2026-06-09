# Phase 7: Testing & Validation - Quick Start Guide

**Status**: Ready to Execute  
**Total Test Cases**: 20 E2E scenarios + 12 acceptance criteria  
**Estimated Duration**: 3-4 hours  
**Prerequisites**: All Phase 7A features deployed and running  

---

## Pre-Test Setup (5 minutes)

### 1. Verify Services Running

```bash
# Check all containers are running
docker-compose ps

# Should see:
# - postgres (healthy)
# - idp (running)
# - finance-data-service (running)
# - idp-frontend (running)
```

### 2. Clear Browser & Database

```bash
# Clear database (optional - start fresh)
docker-compose exec postgres psql -U postgres -d idp <<EOF
DELETE FROM investment_decision_alerts;
DELETE FROM investment_decisions;
DELETE FROM investment_decision_edits;
EOF

# In browser: F12 → Application → Clear Storage → Clear All
```

### 3. Verify API Endpoints

```bash
# Test suggestion endpoint
curl -s http://localhost:8081/api/public/suggestions/all | python3 -m json.tool | head -20

# Should show thesis, evidence, risks arrays with 5 items each
```

### 4. Login to Frontend

- Navigate to: `http://localhost:3000`
- Login: `alice` / `password123`
- Should see dashboard

---

## Test Execution Flow

### Phase 1: API Integration (15 minutes)

**Test**: Suggestion endpoints return correct data  
**Document**: `PHASE_7_E2E_TEST_PLAN.md` → TC-01

```bash
# Run these curl commands and verify responses
curl http://localhost:8081/api/public/suggestions/thesis
curl http://localhost:8081/api/public/suggestions/evidence
curl http://localhost:8081/api/public/suggestions/risks

# Each should return 200 with 5 suggestions in "suggestions" array
```

**Expected**: ✅ All 3 endpoints return 200 with 5 items

---

### Phase 2: Manual Decision Flow (30 minutes)

**Tests**: TC-02 to TC-08  
**Focus**: Investment section + Suggestion system

**Manual Steps**:
1. Go to Investment workspace
2. Create account "Test Account"
3. Add holding: AAPL, 100 shares, $150
4. DecisionCaptureModal appears ✓
5. Select suggestions for thesis/evidence/risks ✓
6. Add custom text to some fields ✓
7. Submit decision ✓
8. Verify in Decision Journal ✓

**Pass Criteria**:
- [ ] Modal opens automatically with transaction details
- [ ] All 5 suggestions show as checkboxes for each category
- [ ] Can select multiple for evidence/risks, single for thesis
- [ ] Custom text fields work
- [ ] Submit creates decision
- [ ] Decision appears in journal with combined text

---

### Phase 3: Review & Exit Criteria (20 minutes)

**Tests**: TC-07 to TC-10  
**Focus**: Decision detail view features

**Manual Steps**:
1. Open decision detail (from TC-08)
2. Scroll down to "Review Schedule" ✓
3. Verify 4 dates shown (30/90/180/365 days) ✓
4. Scroll to "Exit Criteria" section ✓
5. Click "+ Add Exit Criteria" ✓
6. Add alert: Price ≥ $165 ✓
7. Verify alert displays ✓
8. Delete alert ✓

**Pass Criteria**:
- [ ] Review Schedule shows exactly 4 dates
- [ ] Dates are calculated correctly from transaction date
- [ ] Add criteria form appears inline
- [ ] Can select condition type, enter value, add description
- [ ] Alert displays with status (PENDING)
- [ ] Remove button deletes alert

---

### Phase 4: Search & Filtering (15 minutes)

**Tests**: TC-11 to TC-13  
**Focus**: Journal view filters

**Manual Steps**:
1. Go to Decision Journal
2. In search field, type "undervalued" ✓
3. Verify filtering works ✓
4. Clear search ✓
5. Type "correction" ✓
6. Verify different results ✓
7. Combine with other filters (symbol, type, status) ✓

**Pass Criteria**:
- [ ] Search field present in filter row
- [ ] Filters by thesis/evidence/risks keywords
- [ ] Case-insensitive matching
- [ ] Partial word matching works
- [ ] Works with other filters combined
- [ ] Decision count updates

---

### Phase 5: Strategy AUTO Decision (20 minutes)

**Tests**: TC-14 to TC-15  
**Focus**: Strategy execution flow

**Manual Steps**:
1. Go to Strategy section
2. Create strategy "Test Strategy" ✓
3. Add transaction: BUY MSFT, 50 shares ✓
4. DecisionCaptureModal opens ✓
5. Shows system price (locked) ✓
6. Select suggestions ✓
7. Submit decision ✓
8. Verify in Decision Journal ✓

**Pass Criteria**:
- [ ] Modal opens automatically
- [ ] Price shown as system-determined (locked)
- [ ] Shows "(Live)" indicator
- [ ] All Phase 7A features available
- [ ] Decision created as AUTO (not MANUAL)
- [ ] Suggestions saved correctly

---

### Phase 6: Decision Editing & Closing (20 minutes)

**Tests**: TC-16 to TC-19  
**Focus**: Decision lifecycle

**Manual Steps**:
1. Open decision detail
2. Click Edit (or edit mode)
3. Change thesis/evidence/risks ✓
4. Save changes ✓
5. Verify edit history appears ✓
6. Click "Close Decision" ✓
7. Enter exit price ✓
8. Select close reason ✓
9. Confirm close ✓
10. Verify decision is read-only ✓

**Pass Criteria**:
- [ ] Edit form appears with textareas
- [ ] Changes persist after save
- [ ] Edit history shows change with timestamp
- [ ] Close form shows entry vs exit calculation
- [ ] P/L preview updates as you type
- [ ] Decision closes and becomes read-only
- [ ] Final P/L saved

---

### Phase 7: Real-Time Updates (10 minutes)

**Tests**: TC-20  
**Focus**: Live price tracking

**Manual Steps**:
1. Open any decision detail
2. Look for "Current Price" (Live) ✓
3. Wait 5+ seconds without refresh ✓
4. Verify price updates ✓
5. P/L updates ✓

**Pass Criteria**:
- [ ] Shows "(Live)" indicator
- [ ] Price updates every 5 seconds
- [ ] P/L updates automatically
- [ ] No page refresh needed

---

## Quick Results Tracker

As you test, mark off each phase:

```
Phase 1: API Integration         ☐ PASS ☐ FAIL
Phase 2: Manual Decisions        ☐ PASS ☐ FAIL
Phase 3: Review & Exit Criteria  ☐ PASS ☐ FAIL
Phase 4: Search & Filtering      ☐ PASS ☐ FAIL
Phase 5: Strategy AUTO Decision  ☐ PASS ☐ FAIL
Phase 6: Editing & Closing       ☐ PASS ☐ FAIL
Phase 7: Real-Time Updates       ☐ PASS ☐ FAIL
```

---

## Browser Console Checks

After each test phase, open browser console (F12) and verify:

```
☐ No JavaScript errors (red X)
☐ No network 5xx errors (red status codes)
☐ No CORS warnings
☐ No undefined variable warnings
```

---

## Common Issues & Fixes

### Issue: "Modal doesn't open after adding holding"
**Fix**: 
- Check backend logs: `docker-compose logs idp | tail -20`
- Verify modal styling isn't off-screen (F12 → Inspector)
- Try refreshing page and retry

### Issue: "Suggestions don't appear in modal"
**Fix**:
- Verify API endpoint: `curl http://localhost:8081/api/public/suggestions/all`
- Check browser Network tab (F12 → Network)
- Look for failed `/api/public/suggestions/all` request
- Check for CORS errors

### Issue: "Decision doesn't save after submission"
**Fix**:
- Check backend logs for errors
- Verify decision creation call succeeded (Network tab)
- Verify log-details call succeeded (should see 2 requests)
- Check localStorage for draft (might be showing old draft)

### Issue: "Exit criteria not showing"
**Fix**:
- Scroll down in decision detail (section is below edit form)
- Verify decision is OPEN (not CLOSED)
- Check browser console for JavaScript errors

### Issue: "Search doesn't filter results"
**Fix**:
- Verify you typed in the correct search field (thesis/risk search)
- Try different keywords
- Clear filters and try again
- Check Network tab for filter API call

---

## Test Documentation

| Document | Purpose | When to Use |
|----------|---------|------------|
| `PHASE_7_E2E_TEST_PLAN.md` | Detailed test cases (TC-01 to TC-20) | Reference during testing |
| `ACCEPTANCE_VALIDATION_CHECKLIST.md` | FEAT criteria validation (12 criteria) | Final acceptance sign-off |
| `PHASE_7_QUICK_START.md` | This document - quick execution | Use to run tests efficiently |

---

## Success Criteria

✅ **Test Passes If**:
- All 20 test cases (TC-01 to TC-20) pass
- All 12 acceptance criteria validated
- No critical JavaScript errors
- No network 5xx errors
- No data consistency issues

❌ **Test Fails If**:
- Any critical feature doesn't work
- Data not persisted correctly
- UI crashes or hangs
- Significant performance issues

---

## Sign-Off Template

When done, fill this out:

```
TEST EXECUTION SUMMARY
======================
Date: _______________
Tester: ______________

Test Results:
- E2E Test Cases (20):       ☐ PASS ☐ FAIL
- Acceptance Criteria (12):  ☐ PASS ☐ FAIL
- Overall Status:            ☐ PASS ☐ FAIL

Issues Found: ___ (critical) ___ (major) ___ (minor)

Sign-off:
Tester Signature: ________________
Date: ________________
```

---

## Next Steps After Testing

1. **If All Pass** (✅)
   - Sign off on acceptance validation
   - Features ready for production
   - Update version number
   - Create release notes

2. **If Some Fail** (⚠️)
   - Document issues in GitHub/Linear
   - Prioritize fixes (critical first)
   - Re-test after fixes
   - Update test results

3. **If Critical Fails** (❌)
   - Investigate root cause
   - Check recent commits
   - Roll back if needed
   - Fix and re-test

---

## Estimated Timeline

| Activity | Duration | Status |
|----------|----------|--------|
| Setup & Prerequisites | 5 min | Ready |
| Phase 1: API | 15 min | Ready |
| Phase 2: Manual Decisions | 30 min | Ready |
| Phase 3: Review & Exit | 20 min | Ready |
| Phase 4: Search | 15 min | Ready |
| Phase 5: Strategy | 20 min | Ready |
| Phase 6: Edit & Close | 20 min | Ready |
| Phase 7: Real-time | 10 min | Ready |
| Issues & Fixes | 30 min | Flexible |
| **Total** | **3-4 hours** | ✅ |

---

## Questions or Issues?

Refer to:
- Detailed test plan: `docs/test/PHASE_7_E2E_TEST_PLAN.md`
- Acceptance criteria: `docs/test/ACCEPTANCE_VALIDATION_CHECKLIST.md`
- Architecture: `docs/DECISION_JOURNAL_INTEGRATION.md`
- Implementation: `docs/implementation/decision_journal_kfs_alignment_plan.md`

