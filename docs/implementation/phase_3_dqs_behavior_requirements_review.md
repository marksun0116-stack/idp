---
review_id: RR-003
title: "Phase 3 DQS and Behavioral Analytics Requirements Review"
status: approved
owner: "Investor Development Platform Team"
last_updated: 2026-06-05
version: "0.1"
linked_plan: PLAN-investor-development-platform-001
linked_story:
  - US-005
  - US-006
linked_primitives:
  - CONR-dqs-api-001
  - CONR-behavioral-analytics-api-001
  - INV-dqs-explainability-001
  - CON-investment-non-advice-001
---

# Phase 3 DQS and Behavioral Analytics Requirements Review

## Purpose

Phase 3 turns private decision and review data into coaching-oriented analytics. Scores must explain process quality, not predict returns or instruct trades.

## Scope

- Compute a current Decision Quality Score for the authenticated user.
- Return component weights, component scores, and explainability drivers.
- Compute a behavioral scorecard for FOMO, loss aversion, research discipline, and risk discipline.
- Generate private, coaching-language insights linked to the user's own decision records.

## Requirements Decisions

| Topic | Decision | Rationale |
| --- | --- | --- |
| Persistence | Phase 3 v0.1 computes analytics on read from private decisions and reviews. | Avoids premature score-history tables before formulas stabilize. |
| Score scale | Scores are integers from 0 to 100. | Easy to read and compare across components. |
| DQS weights | Research quality 0.25, decision discipline 0.25, risk management 0.20, strategy consistency 0.15, outcome quality 0.15. | Outcome quality remains below total process quality, satisfying `INV-dqs-explainability-001`. |
| Research quality | Based on average evidence count per decision, capped at 3 evidence items per decision. | Rewards documented support without encouraging long noisy notes. |
| Decision discipline | Based on review completion rate for scheduled reviews. | Reinforces the learning loop rather than trade outcome. |
| Risk management | Based on average risk factor count and exit criteria count, capped at 2 each. | Rewards pre-defined risk and exit thinking. |
| Strategy consistency | Based on active/closed decisions that have gone through lifecycle transitions. | Early proxy for following a process while strategy portfolios are not yet implemented. |
| Outcome quality | Based on completed review thesis and risk accuracy ratings. | Uses learning review inputs and remains non-dominant. |
| Trend | Phase 3 v0.1 returns `0` until score history exists. | Honest representation before persisted score snapshots. |
| Behavioral insights | Use coaching language only; no buy/sell/hold instruction language. | Enforces `CON-investment-non-advice-001`. |

## Acceptance Criteria

- `GET /api/analytics/dqs` returns score, trend, component weights, component scores, and at least one driver when user data exists.
- DQS scores use only authenticated user's private records and reviews.
- Outcome quality weight remains 0.15 and does not dominate the score.
- Drivers include related decision IDs when a driver is derived from specific decisions.
- `GET /api/analytics/behavior` returns behavioralScore, FOMO, loss aversion, research discipline, risk discipline, and coaching insights.
- Behavioral insights avoid investment instruction language.
- Empty analytics responses are valid and return neutral component values rather than errors.

## Test Coverage

- `AnalyticsDqsTest`: DQS includes component weights, explainable drivers, owner isolation, and outcome weight cap.
- `BehavioralAnalyticsTest`: behavior endpoint returns private coaching insights and neutral empty-state scores.

## Parallelization

- `US-005` and `US-006` can proceed in parallel after this review because both read from the same decision/review data but expose separate endpoints.
- Dashboard UI can mock against v0.2 analytics contracts after both backend endpoints pass tests.

## Open Follow-Ups

- Add persisted score snapshots and trend history after the formula stabilizes.
- Refine FOMO and loss-aversion signals when portfolio transactions and market movement context exist.
- Add explicit non-advice copy to frontend scorecard surfaces.

## Revision History

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-05 | approved | — | Initial Phase 3 requirements review for DQS and behavioral analytics. |
