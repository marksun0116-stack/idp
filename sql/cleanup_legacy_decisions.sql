-- Cleanup script for legacy decision tables
-- Run this after confirming migration to unified decisions table is complete
-- Date: 2026-06-10

-- Drop old decision record tables
DROP TABLE IF EXISTS decision_record_exit_criteria CASCADE;
DROP TABLE IF EXISTS decision_record_risk_factors CASCADE;
DROP TABLE IF EXISTS decision_record_evidence CASCADE;
DROP TABLE IF EXISTS decision_records CASCADE;

-- Drop old investment decision tables
DROP TABLE IF EXISTS investment_decision_edits CASCADE;
DROP TABLE IF EXISTS investment_decision_alerts CASCADE;
DROP TABLE IF EXISTS investment_decisions CASCADE;

-- Verify unified decisions table exists and has data
-- SELECT COUNT(*) as decision_count FROM decisions;
