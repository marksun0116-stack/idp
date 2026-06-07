---
plan_id: PLAN-investor-development-platform-001
title: "Investor Development Platform Implementation Plan"
status: active
owner: "Investor Development Platform Team"
last_updated: 2026-06-07
version: "3.5"
linked_prds:
  - PRD-investor-development-platform-001
linked_features:
  - FEAT-investor-development-platform-001
---

# Investor Development Platform Implementation Plan

## 1. Scope

This plan sequences the MVP implementation for the Investor Development Platform business concept: structured decision records, scheduled reviews, Decision Quality Score, behavioral analytics, strategy portfolios with tracked symbols and strategy-scoped quotes/charts/indicators, auditable transactions when positions are opened, and public investor profiles.

The plan coordinates implementation work only. Product intent remains in `docs/prd/`, architecture/design remain in `docs/architecture/` and `docs/design/`, and enforceable rules remain in `knowledge/`.

## 2. Planning Principles

- Build the private decision learning loop before public reputation surfaces.
- Treat privacy, non-advice language, and auditability as foundational constraints.
- Keep each story traceable to `FEAT-investor-development-platform-001` and linked CON/INV/CONR primitives.
- Allow parallel UI and backend work only after API contracts and data ownership behavior are clear.
- Start every phase with a requirements-review gate so phase details, acceptance criteria, blockers, and parallelization boundaries are explicit before implementation.

## 3. Phase Overview

| Phase | Goal | Status | Depends on | Parallelizable with | Trace |
| --- | --- | --- | --- | --- | --- |
| Phase 0 — Foundation | Align KFS, implementation plan, privacy/non-advice/auditability primitives, and API contract drafts. | done | None | None | FEAT-investor-development-platform-001 |
| Phase 1 — Decision Journal | Implement private decision records and owner-scoped decision lifecycle. | done | Phase 0 | Phase 2 UI shell after contracts stabilize | CONR-decision-record-api-001, INV-decision-record-integrity-001 |
| Phase 2 — Review Loop | Implement scheduled reviews and lessons learned capture. | done | Phase 1 | Phase 3 scoring design | CONR-decision-review-api-001, CON-decision-review-cadence-001 |
| Phase 3 — DQS and Behavioral Analytics | Implement scorecard APIs and explainable score drivers. | done | Phase 1, partial Phase 2 | Dashboard UI after API shape stabilizes | CONR-dqs-api-001, CONR-behavioral-analytics-api-001, INV-dqs-explainability-001 |
| Phase 4 — Strategy Portfolios | Implement strategy portfolios with tracked symbols, append-only transaction history, and strategy-scoped quote, chart, and indicator research surfaces. | done | Phase 1 | Public profile UI shell; strategy market-data UI after symbol model stabilizes | CONR-strategy-portfolio-api-001, INV-public-strategy-history-001, INV-strategy-market-data-scope-001 |
| Phase 5A — Investor Workspace UI | Implement the first browser-visible private workspace over the completed Phase 1-4 APIs. | done | Phases 1-4 | Phase 5 public-profile backend after private workspace UI contracts stabilize | FEAT-investor-development-platform-001 |
| Phase 5 — Public Profiles and Reputation | Implement public investor profiles and approved reputation signals. | done | Phase 3, Phase 4 | Phase 5A private workspace UI | CONR-public-profile-api-001, INV-private-data-visibility-001 |
| Phase 6 — Dashboard Integration | Bring scorecards, activity, reviews, strategies, recent decisions, strategy research widgets, and community decisions into the primary dashboard. | done | Phases 1-5 and Phase 5A UI surfaces | None | FEAT-investor-development-platform-001 |
| Phase 7 — Auth and Market Data Hardening | Replace MVP shortcuts with local user auth and real strategy market data with graceful fallback. | done | Phase 6 | UI polish and demo data can proceed after contracts stabilize | CONR-user-api-001, CONR-strategy-portfolio-api-001 |
| Phase 8 — Auth-First UX | Make login/register the default app entry while preserving explicit demo-mode access. | done | Phase 7 | None | CONR-user-api-001 |
| Phase 9 — Investment Workspace Core | Implement owner-scoped investment accounts, holdings, and portfolio summary using live or manual prices. | done | Phase 8 | Sector cache and CSV import can follow after account/holding contract tests pass. | CONR-portfolio-api-001, INV-portfolio-ownership-001, INV-holding-cost-basis-001, INV-manual-price-001 |

## 4. User Story Plan

| Story ID | Phase | User Story | Status | Depends on | Can run in parallel? | Trace | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RR-000 | Phase 0 | Review Phase 0 KFS/project-management requirements before changing KFS process. | done | None | No | RULE-014 | Completed while adding the KFS planning rule, spec, skill, and initial plan. |
| US-000 | Phase 0 | As the team, I want KFS to track implementation phases and story progress so future work stays ordered and traceable. | done | None | No | RULE-014 | KFS now includes RULE-014, a plan spec, a plan-management skill, and this initial plan. |
| RR-001 | Phase 1 | Review Phase 1 decision-journal requirements before implementation starts. | done | US-000 | No | CONR-decision-record-api-001, INV-decision-record-integrity-001, CON-private-research-default-001 | Completed in `docs/implementation/phase_1_decision_journal_requirements_review.md`; contract updated to v0.2. |
| US-001 | Phase 1 | As an investor, I want to create a private decision record with thesis, evidence, risks, confidence, horizon, and exit criteria. | done | RR-001 | Backend and UI can split after CONR-decision-record-api-001 is accepted. | CONR-decision-record-api-001, INV-decision-record-integrity-001, CON-private-research-default-001 | Implemented backend create endpoint, private bearer-subject ownership, validation, and create-time revision history. |
| US-002 | Phase 1 | As an investor, I want to list, view, update, close, and archive my decision records without exposing them to other users. | done | US-001 | Yes, UI list/detail can run with backend lifecycle tests after API contract stabilizes. | CONR-decision-record-api-001, INV-private-data-visibility-001 | Implemented backend list/detail/update/transition endpoints with owner isolation, lifecycle conflicts, and revision preservation. |
| RR-002 | Phase 2 | Review Phase 2 decision-review requirements before review-loop implementation starts. | done | US-002 | No | CONR-decision-review-api-001, CON-decision-review-cadence-001, INV-review-schedule-integrity-001 | Completed in `docs/implementation/phase_2_decision_review_requirements_review.md`; contract updated to v0.2. |
| US-003 | Phase 2 | As an investor, I want review tasks generated at 30d, 90d, 180d, and 1y intervals for active decisions. | done | RR-002 | No | CONR-decision-review-api-001, CON-decision-review-cadence-001, INV-review-schedule-integrity-001 | Implemented schedule generation from first active transition and owner-scoped review listing. |
| US-004 | Phase 2 | As an investor, I want to complete a review with outcome, thesis accuracy, risk accuracy, and lessons learned. | done | US-003 | UI can run in parallel with backend after review contract stabilizes. | CONR-decision-review-api-001, INV-review-schedule-integrity-001 | Implemented review completion endpoint, learning-field persistence, validation, owner isolation, and state conflict handling. |
| RR-003 | Phase 3 | Review Phase 3 DQS and behavioral analytics requirements before scoring implementation starts. | done | US-001, US-004 | No | CONR-dqs-api-001, CONR-behavioral-analytics-api-001, INV-dqs-explainability-001 | Completed in `docs/implementation/phase_3_dqs_behavior_requirements_review.md`; analytics contracts updated to v0.2. |
| US-005 | Phase 3 | As an investor, I want a DQS score with component weights and drivers so I know how to improve my process. | done | RR-003 | Backend scoring and frontend scorecard can split after response shape stabilizes. | CONR-dqs-api-001, INV-dqs-explainability-001, CON-investment-non-advice-001 | Implemented read-time DQS scoring with component weights, score drivers, owner isolation, and neutral empty state. |
| US-006 | Phase 3 | As an investor, I want behavioral insights for research discipline, risk discipline, FOMO, and loss aversion patterns. | done | RR-003 | Yes, can proceed alongside US-005 if shared analytics inputs are agreed. | CONR-behavioral-analytics-api-001, CON-private-research-default-001, CON-investment-non-advice-001 | Implemented private behavioral scorecard with coaching-language insights and owner isolation. |
| RR-004 | Phase 4 | Review Phase 4 strategy-portfolio requirements before portfolio implementation starts. | done | US-001 | No | CONR-strategy-portfolio-api-001, INV-public-strategy-history-001, INV-strategy-market-data-scope-001 | Completed in `docs/implementation/phase_4_strategy_portfolio_requirements_review.md`; contract updated to v0.4. |
| US-007 | Phase 4 | As a creator, I want to create a strategy portfolio with fixed starting capital and visibility controls. | done | RR-004 | Yes, can begin after decision record lifecycle exists. | CONR-strategy-portfolio-api-001, CON-public-strategy-auditability-001 | Implemented virtual strategy creation, public/private visibility, owner reads, and public strategy reads. |
| US-008 | Phase 4 | As an investor, I want to add symbols to a strategy portfolio for tracking without buying them. | done | US-007 | Yes, UI and backend can split after tracked-symbol request/response stabilizes. | CONR-strategy-portfolio-api-001, INV-strategy-market-data-scope-001, CON-private-research-default-001 | Implemented tracked-only symbols without transaction creation, duplicate protection, and public/private symbol visibility. |
| US-009 | Phase 4 | As a creator, I want append-only strategy transactions linked to decisions so my public record is credible. | done | US-007, US-008 | No | CONR-strategy-portfolio-api-001, INV-public-strategy-history-001 | Implemented append-only transaction creation with optional owned decision links. |
| US-010 | Phase 4 | As an investor reviewing a strategy portfolio, I want quotes, historical charts, and technical indicators for that strategy's tracked and position symbols. | done | US-008 | Yes, with US-009 after strategy symbol membership is defined. | CONR-strategy-portfolio-api-001, CONR-historical-chart-api-001, CONR-insights-api-001, INV-strategy-market-data-scope-001, CON-technical-signals-001 | Implemented owner/public scoped placeholder quote/history/indicator responses limited to strategy symbols. |
| RR-005A | Phase 5A | Review private investor workspace UI requirements before frontend implementation starts. | done | US-010 | No | FEAT-investor-development-platform-001 | Completed in `docs/implementation/phase_5a_investor_workspace_ui_requirements_review.md`; first UI scope focuses on private workspace over Phase 1-4 APIs. |
| US-010A | Phase 5A | As an investor, I want a browser workspace for decisions, reviews, analytics, and strategy portfolios so I can use the local app visually. | done | RR-005A, US-010 | Can proceed in parallel with Phase 5 public-profile backend once private UI API contracts are stable. | FEAT-investor-development-platform-001, CONR-decision-record-api-001, CONR-decision-review-api-001, CONR-dqs-api-001, CONR-strategy-portfolio-api-001 | Implemented React workspace, Docker Compose frontend service, Nginx `/api` proxy, concept-image asset, and owner-scoped strategy list endpoint. |
| RR-005 | Phase 5 | Review Phase 5 public-profile and reputation requirements before public profile implementation starts. | done | US-005, US-009, US-010 | No | CONR-public-profile-api-001, INV-private-data-visibility-001, INV-dqs-explainability-001 | Completed in `docs/implementation/phase_5_public_profile_requirements_review.md`; scoped explicit profile publishing, approved metrics, public strategies, and privacy filtering. |
| US-011 | Phase 5 | As an investor, I want a public profile that exposes only approved reputation signals and published strategies. | done | RR-005 | UI can run in parallel with public profile API after privacy tests exist. | CONR-public-profile-api-001, INV-private-data-visibility-001 | Implemented public profile API, approved reputation filtering, public strategy summaries, privacy tests, and workspace publish controls. |
| RR-006 | Phase 6 | Review Phase 6 dashboard integration requirements before dashboard implementation starts. | done | US-002, US-004, US-005, US-006, US-011 | No | FEAT-investor-development-platform-001 | Completed in `docs/implementation/phase_6_dashboard_integration_requirements_review.md`; scoped dashboard shell, integrated widgets, navigation, and preserved write workflows. |
| US-012 | Phase 6 | As an investor, I want the dashboard to summarize DQS, reviews, strategies, recent decisions, behavior insights, and community decisions. | done | RR-006 | No | FEAT-investor-development-platform-001 | Implemented dashboard-first React shell with integrated scorecards, activity, reviews, strategies, decision funnel, market snapshot placeholders, community placeholders, and focused workflow sections. |
| RR-007 | Phase 7 | Review auth and market-data hardening requirements before replacing MVP shortcuts. | done | US-012 | No | CONR-user-api-001, CONR-strategy-portfolio-api-001 | Completed in `docs/implementation/phase_7_auth_market_data_requirements_review.md`; scoped local auth, token compatibility, real Yahoo-backed market data, and graceful fallback. |
| US-013 | Phase 7 | As an investor, I want to register and log in so my workspace is attached to a real local account. | done | RR-007 | Can proceed in parallel with US-014 after auth compatibility is defined. | CONR-user-api-001 | Implemented register/login/me endpoints, token persistence, BCrypt password hashing, frontend auth controls, and dev bearer fallback compatibility. |
| US-014 | Phase 7 | As an investor, I want real quotes, charts, and indicators in strategy portfolios instead of placeholder market data. | done | RR-007 | Can proceed in parallel with US-013 after strategy symbol scoping is stable. | CONR-strategy-portfolio-api-001, CONR-historical-chart-api-001, CONR-insights-api-001 | Implemented Yahoo chart-backed strategy quotes/history/indicators with market-data-unavailable fallback. |
| RR-008 | Phase 8 | Review auth-first UX requirements before changing the application entry flow. | done | US-013 | No | CONR-user-api-001 | Completed in `docs/implementation/phase_8_auth_first_ux_requirements_review.md`; scoped login/register-first entry, explicit demo mode, logout behavior, and identity copy. |
| US-015 | Phase 8 | As an investor, I want the app to start at login/register so my private workspace is not opened through an implicit development identity. | done | RR-008 | No | CONR-user-api-001, CON-private-research-default-001 | Implemented auth-first React entry screen, explicit demo mode, workspace state clearing on logout, and authenticated/demo identity indicators. |
| RR-009 | Phase 9 | Review Investment Workspace core requirements before account and holding implementation starts. | done | US-015 | No | FEAT-account-management-001, FEAT-holding-management-001, FEAT-portfolio-dashboard-001, CONR-portfolio-api-001 | Completed in `docs/implementation/phase_9_investment_workspace_core_requirements_review.md`; scoped account CRUD, holding CRUD, owner isolation, optional cost basis, manual price, and summary math. |
| US-016 | Phase 9 | As an investor, I want to create and manage investment accounts so holdings are grouped by account type. | done | RR-009 | Can proceed in parallel with frontend shell after backend ownership tests exist. | FEAT-account-management-001, CONR-portfolio-api-001, INV-portfolio-ownership-001 | Implemented owner-scoped account create/list/delete through portfolio summary with per-user name uniqueness and cascade behavior. |
| US-017 | Phase 9 | As an investor, I want to add, update, and remove holdings with optional cost basis and manual price. | done | US-016 | Backend holding CRUD and summary UI can split after DTO shape is stable. | FEAT-holding-management-001, CON-holding-uniqueness-001, INV-holding-cost-basis-001, INV-manual-price-001 | Implemented owner-scoped holding CRUD, duplicate-symbol conflict handling, optional cost basis, and manual price priority. |
| US-018 | Phase 9 | As an investor, I want a portfolio summary that calculates value, gain/loss, daily change, and account totals. | done | US-017 | Frontend table/allocation view can proceed once summary response is contract-tested. | FEAT-portfolio-dashboard-001, CONR-portfolio-api-001, INV-allocation-consolidation-001 | Implemented portfolio summary API and Investment Workspace UI with totals, account list, holding entry, and holding table. |

## 5. Parallelization Notes

- Decision record backend and decision journal UI can proceed in parallel only after `CONR-decision-record-api-001` is stable enough for mocked frontend work.
- Review UI can proceed alongside review backend after US-003 defines due-date and status semantics.
- DQS and behavioral analytics can proceed in parallel after decision/review data contracts are stable, but they should share scoring input definitions.
- Public profiles can begin with a static/read model while strategy portfolio write behavior is still in progress, provided privacy filtering is tested first.
- Strategy portfolio quote/chart/indicator UI can proceed in parallel with append-only transaction work after tracked-symbol membership is clear; it must not write to transaction history.
- Tracked-symbol work should precede transaction history because both market-data surfaces and future public strategy views need a common strategy symbol model.
- Dashboard integration should wait until the underlying private and public APIs exist; otherwise it risks becoming a mock-only surface.
- Phase 5A is an intentional early UI slice: it can start before Phase 5 because it uses the already completed private APIs from Phases 1-4 and does not expose public reputation data.
- Phase implementation stories should not move to `in_progress` until the phase's `RR-*` requirements review is `done` or explicitly `in_progress` with named accepted risks.
- Auth-first UX should not run in parallel with auth backend hardening because the entry flow depends on settled token and demo-compatibility behavior.
- Phase 9 account and holding backend work must land before CSV import or sector-cache UI expansion; those depend on stable owner-scoped account and holding identities.

## 6. Progress Updates

- 2026-06-05: Created initial implementation plan and marked Phase 0 / US-000 `in_progress` while adding KFS project-management support.
- 2026-06-05: Completed US-000; KFS now has RULE-014, `implementation_plan_spec.md`, `manage-implementation-plan`, and this tracked implementation plan.
- 2026-06-05: Added US-009 for strategy portfolio quotes, charts, and indicators; updated Phase 4 parallelization and downstream dependencies.
- 2026-06-05: Split strategy portfolio symbol tracking into its own story; tracked-only symbols now model the private watchlist-within-portfolio concept and precede transaction/history work.
- 2026-06-05: Added phase-start `RR-*` requirements-review gates so each phase can drill into detailed scope before implementation starts.
- 2026-06-05: Started Phase 1 by completing `RR-001`; added detailed decision-journal requirements review and updated `CONR-decision-record-api-001` to v0.2. Marked `US-001` ready.
- 2026-06-05: Started `US-001`; backend create endpoint and revision-on-create implementation in progress.
- 2026-06-05: Completed `US-001`; added Spring Boot backend scaffold, `POST /api/decisions`, private bearer-subject ownership, create validation, and revision-on-create tests. Marked `US-002` ready.
- 2026-06-05: Started `US-002`; implementing owner-scoped list/detail/update/transition endpoints for private decision lifecycle.
- 2026-06-05: Completed `US-002`; added owner-scoped decision listing/detail, update revisions, close/archive lifecycle transitions, privacy 404 behavior, and lifecycle conflict tests. Marked Phase 1 done and `RR-002` ready.
- 2026-06-05: Started `RR-002`; reviewing Phase 2 decision-review scheduling and completion requirements before implementation.
- 2026-06-05: Completed `RR-002`; added Phase 2 requirements review and updated `CONR-decision-review-api-001` to v0.2. Started `US-003` schedule generation.
- 2026-06-05: Completed `US-003`; active decisions now generate 30d/90d/180d/1y review tasks and expose owner-scoped review listing. Started `US-004`.
- 2026-06-05: Completed `US-004`; review completion now records outcome, thesis/risk accuracy, lessons learned, and optional next action with owner isolation and state validation. Marked Phase 2 done and `RR-003` ready.
- 2026-06-05: Started `RR-003`; reviewing DQS and behavioral analytics scoring requirements before implementation.
- 2026-06-05: Completed `RR-003`; added Phase 3 requirements review and updated DQS/behavior contracts to v0.2. Started `US-005` and `US-006` analytics implementation.
- 2026-06-05: Completed `US-005` and `US-006`; added DQS and behavioral analytics endpoints with component explainability, coaching insights, owner isolation, and neutral empty states. Marked Phase 3 done and `RR-004` ready.
- 2026-06-05: Started `RR-004`; reviewing strategy portfolio requirements before implementation.
- 2026-06-05: Completed `RR-004`; added Phase 4 requirements review and updated strategy portfolio contract to v0.4. Started `US-007`.
- 2026-06-05: Completed `US-007` through `US-010`; added virtual strategy portfolios, tracked-only symbols, append-only transactions, owner/public visibility rules, and strategy-scoped placeholder quote/history/indicator surfaces. Marked Phase 4 done and `RR-005` ready.
- 2026-06-05: Added Phase 5A as an early private workspace UI slice; completed `RR-005A` and started `US-010A`.
- 2026-06-05: Completed `US-010A`; added React workspace UI, frontend Docker service, Nginx API proxy, strategy list endpoint, and local Docker verification. Marked Phase 5A done.
- 2026-06-05: Started Phase 5; completed `RR-005` requirements review and began `US-011` public profile implementation.
- 2026-06-05: Completed `US-011`; added public profile persistence, owner/public profile endpoints, reputation metric publishing controls, public strategy summaries, privacy tests, and workspace UI controls. Marked Phase 5 done and `RR-006` ready.
- 2026-06-05: Started Phase 6; completed `RR-006` dashboard integration requirements review and began `US-012`.
- 2026-06-05: Completed `US-012`; replaced the single workspace surface with a dashboard-first shell, left navigation, integrated scorecards, activity, reviews, strategy overview, behavior insights, decision funnel, market/community placeholders, and focused workflow sections. Marked Phase 6 done.
- 2026-06-05: Started Phase 7; completed `RR-007` auth and market-data requirements review and began `US-013`/`US-014`.
- 2026-06-05: Completed `US-013` and `US-014`; added local account registration/login, opaque bearer tokens, frontend auth controls, Yahoo-backed strategy quotes/history/indicators, and graceful provider fallback. Marked Phase 7 done.
- 2026-06-05: Completed Phase 8; added `RR-008`, auth-first app entry, explicit demo mode, logout-to-login behavior, and authenticated/demo identity indicators. Marked `US-015` done.
- 2026-06-06: Closed the current MVP implementation plan after confirming all tracked phases and stories are `done`; no active implementation story remains in this plan.
- 2026-06-07: Reopened the plan for Phase 9 Investment Workspace Core; completed `RR-009` and started `US-016`.
- 2026-06-07: Completed Phase 9 Investment Workspace Core; added account and holding persistence, owner-scoped portfolio APIs, summary math, Investment Workspace UI, and portfolio contract tests.
- 2026-06-07: Refined Phase 9 Investment Workspace UI so holdings are the primary view, account/holding setup controls are collapsible, and daily/total gain/loss percentages are visible per holding.
- 2026-06-07: Renamed the user-facing Strategy Portfolio navigation label from Portfolios to Strategies so it is distinct from the Investment accounts and holdings page.
- 2026-06-07: Refined Strategies symbol entry so watch symbols stay research-only while buy/sell actions collect shares and price for transaction-derived allocation and performance.
- 2026-06-07: Clarified and implemented symbol-level strategy price history with an in-chart symbol selector and two-decimal price-axis formatting.
- 2026-06-07: Reframed Strategies as paper portfolios with uninvested cash, live-quote transaction fills, market-open/cash/share guardrails, and total strategy performance history.
- 2026-06-07: Added strategy symbol row actions so watch symbols can buy/delete and owned symbols can sell from the tracked-symbol list.
- 2026-06-07: Moved strategy creation behind the Strategies panel `+`, made Add Symbol the right-side panel, added strategy visibility switching, and compressed tracked-symbol rows.

## 7. Open Questions

- No open blockers for Phase 9 account/holding core.
- CSV import conflict resolution and sector-cache refresh remain deferred until the core portfolio API and UI are stable.

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-05 | draft | — | Initial implementation plan created from the IDP concept and KFS primitives. |
| 0.2 | 2026-06-05 | draft | 0.1 | Marked Phase 0 / US-000 done after adding KFS project-management support. |
| 0.3 | 2026-06-05 | draft | 0.2 | Added strategy portfolio quote/chart/indicator user story and dependency updates. |
| 0.4 | 2026-06-05 | draft | 0.3 | Split tracked-only strategy symbols into their own user story before strategy transactions. |
| 0.5 | 2026-06-05 | draft | 0.4 | Added phase-start requirements-review gates before implementation stories. |
| 0.6 | 2026-06-05 | draft | 0.5 | Completed Phase 1 requirements review and marked decision-record creation story ready. |
| 0.7 | 2026-06-05 | draft | 0.6 | Marked decision-record creation story in progress. |
| 0.8 | 2026-06-05 | draft | 0.7 | Completed decision-record creation backend slice and marked lifecycle/list story ready. |
| 0.9 | 2026-06-05 | draft | 0.8 | Started lifecycle/list story implementation for Phase 1 decision records. |
| 1.0 | 2026-06-05 | draft | 0.9 | Completed Phase 1 decision journal backend lifecycle and marked Phase 2 requirements review ready. |
| 1.1 | 2026-06-05 | draft | 1.0 | Started Phase 2 requirements review for decision reviews. |
| 1.2 | 2026-06-05 | draft | 1.1 | Completed Phase 2 requirements review and started scheduled-review generation. |
| 1.3 | 2026-06-05 | draft | 1.2 | Completed review schedule generation and started review completion. |
| 1.4 | 2026-06-05 | draft | 1.3 | Completed Phase 2 review loop backend and marked Phase 3 requirements review ready. |
| 1.5 | 2026-06-05 | draft | 1.4 | Started Phase 3 requirements review for DQS and behavioral analytics. |
| 1.6 | 2026-06-05 | draft | 1.5 | Completed Phase 3 requirements review and started DQS/behavior analytics implementation. |
| 1.7 | 2026-06-05 | draft | 1.6 | Completed Phase 3 analytics backend and marked Phase 4 requirements review ready. |
| 1.8 | 2026-06-05 | draft | 1.7 | Started Phase 4 requirements review for strategy portfolios. |
| 1.9 | 2026-06-05 | draft | 1.8 | Completed Phase 4 requirements review and started strategy creation. |
| 2.0 | 2026-06-05 | draft | 1.9 | Completed Phase 4 strategy portfolio backend and marked Phase 5 requirements review ready. |
| 2.1 | 2026-06-05 | draft | 2.0 | Added Phase 5A private investor workspace UI and started the first frontend implementation story. |
| 2.2 | 2026-06-05 | draft | 2.1 | Completed Phase 5A private investor workspace UI and Docker frontend integration. |
| 2.3 | 2026-06-05 | draft | 2.2 | Started Phase 5 public profiles; completed requirements review and began public profile implementation. |
| 2.4 | 2026-06-05 | draft | 2.3 | Completed Phase 5 public profile and reputation publishing slice. |
| 2.5 | 2026-06-05 | draft | 2.4 | Started Phase 6 dashboard integration with requirements review and dashboard shell implementation. |
| 2.6 | 2026-06-05 | draft | 2.5 | Completed Phase 6 dashboard integration and marked the MVP implementation plan done through the dashboard slice. |
| 2.7 | 2026-06-05 | draft | 2.6 | Started Phase 7 auth and market-data hardening. |
| 2.8 | 2026-06-05 | draft | 2.7 | Completed Phase 7 auth and market-data hardening. |
| 2.9 | 2026-06-05 | draft | 2.8 | Completed Phase 8 auth-first UX and explicit demo-mode entry. |
| 3.0 | 2026-06-06 | complete | 2.9 | Closed the current MVP implementation plan with all tracked phases and stories done. |
| 3.1 | 2026-06-07 | active | 3.0 | Reopened the implementation plan for Phase 9 Investment Workspace Core and started account management. |
| 3.2 | 2026-06-07 | active | 3.1 | Completed Phase 9 Investment Workspace Core and left CSV import / sector-cache expansion deferred. |
| 3.3 | 2026-06-07 | active | 3.2 | Refined Phase 9 Investment Workspace UI to prioritize holdings and make account/holding setup controls collapsible. |
| 3.4 | 2026-06-07 | active | 3.3 | Renamed the user-facing Strategy Portfolio nav label to Strategies to distinguish it from Investment holdings. |
| 3.5 | 2026-06-07 | active | 3.4 | Added strategy watch-vs-position UI semantics and transaction-derived strategy allocation/performance fields. |
| 3.6 | 2026-06-07 | active | 3.5 | Clarified strategy price history as a selected-symbol chart and formatted price-axis values to two decimals. |
| 3.7 | 2026-06-07 | active | 3.6 | Reframed Strategies as paper portfolios with live quote execution, cash/share guardrails, and total strategy performance history. |
| 3.8 | 2026-06-07 | active | 3.7 | Added row-level watch buy/delete and owned sell actions in Strategies. |
| 3.9 | 2026-06-07 | active | 3.8 | Compact Strategies layout with header create action, right-side Add Symbol panel, visibility toggle, and denser symbol rows. |
