---
title: "Phase 6 Dashboard Integration Requirements Review"
status: approved
owner: "Investor Development Platform Team"
last_updated: 2026-06-05
version: "0.1"
---

# Phase 6 Dashboard Integration Requirements Review

## Scope

Phase 6 turns the first private workspace UI into a dashboard-first product experience. The dashboard should summarize the investor's process quality, next review work, recent decisions, strategies, behavior insights, public profile status, and contextual market/community placeholders.

## Included

- Persistent left navigation with Dashboard, Decisions, Portfolios, Reviews, Analytics, Community, Profile, and Settings entries.
- Dashboard landing page with score cards, recent activity, next reviews, active strategies, behavior insights, decision funnel, market snapshot, recent decisions, and community signal placeholders.
- Reuse existing owner-scoped APIs from Phases 1-5.
- Keep existing decision, strategy, tracked-symbol, and public-profile write workflows available in focused sections.
- Responsive layout for desktop and mobile widths.

## Deferred

- Real market index providers.
- Real community discovery feeds.
- Multi-page routing.
- Advanced dashboard charting beyond lightweight SVG/dashboard summaries.

## Acceptance Criteria

- `http://localhost:3000` opens to a dashboard-first UI.
- Navigation changes visible workspace sections without a full page reload.
- Dashboard cards summarize DQS, research discipline, risk management, and behavioral score from API data.
- Dashboard lists next reviews, recent decisions, active strategies, behavior insights, and public profile status.
- Existing write workflows remain usable after dashboard integration.

## Change Log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-05 | approved | - | Initial Phase 6 dashboard integration requirements review. |
