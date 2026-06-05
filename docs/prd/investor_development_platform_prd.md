---
prd_id: PRD-investor-development-platform-001
title: "Investor Development Platform"
status: draft
owner: "Investor Development Platform Team"
product_manager: "TBD"
engineering_lead: "TBD"
last_updated: 2026-06-05
version: "0.4"
linked_feature_specs:
  - FEAT-investor-development-platform-001
related_constraints:
  - CON-private-research-default-001
  - CON-public-strategy-auditability-001
  - CON-decision-review-cadence-001
  - CON-investment-non-advice-001
  - CON-historical-data-range-001
  - CON-technical-signals-001
related_invariants:
  - INV-decision-record-integrity-001
  - INV-review-schedule-integrity-001
  - INV-private-data-visibility-001
  - INV-public-strategy-history-001
  - INV-dqs-explainability-001
  - INV-strategy-market-data-scope-001
related_decisions: []
related_contracts:
  - CONR-decision-record-api-001
  - CONR-decision-review-api-001
  - CONR-behavioral-analytics-api-001
  - CONR-dqs-api-001
  - CONR-strategy-portfolio-api-001
  - CONR-public-profile-api-001
  - CONR-historical-chart-api-001
  - CONR-insights-api-001
slices:
  - investor-workspace
---

# Investor Development Platform PRD

## 1. Summary

### Problem

Self-directed investors lack a structured learning loop for investment decisions. Existing tools emphasize returns, portfolio tracking, or social commentary, but they do not help investors capture decision context, evaluate process quality, or learn from outcomes.

### Proposed solution

Build an Investor Development Platform that helps investors document investment decisions, research evidence, risk factors, and outcomes. The platform will provide structured decision journals, review engines, behavioral analytics, and public strategy portfolios that support continuous improvement and credible investing reputation.

### Why now

Retail investment activity and self-directed investor sophistication are rising. Investors increasingly demand tools for accountability, discipline, and reproducible improvement rather than just performance metrics. This platform addresses a gap between data-oriented investing tools and behavior-driven investor development.

## 2. Goals and non-goals

| Goals | Non-goals |
| --- | --- |
| Enable structured investment decision-making and review | Replace broker execution or portfolio management platforms |
| Capture decision context, evidence, risk, and learning | Be a market prediction engine |
| Provide metrics and analytics that separate decision quality from outcome quality | Be a generic social network |
| Support public strategy portfolios and credibility for creators | Provide full financial advice |
| Launch a minimum viable workspace with research, decision journaling, and review | Build an exhaustive investing platform in the first release |

## 3. Users and stakeholders

### Primary users

- Self-directed investors
  - Age 25-45
  - Household income $100k-$300k+
  - Portfolio size $25k-$500k
  - Active research behavior and desire to improve decision quality

### Secondary users

- Investment content creators
  - YouTube creators, Substack authors, X/Twitter analysts, Discord leaders
  - Seek transparent track records and public credibility

### Stakeholders

- Product management
- Engineering
- Design
- Growth/marketing
- Early adopter investors

## 4. User journeys and stories

- US-001: As a self-directed investor, I want to create a structured investment decision record so I can capture my thesis, evidence, risks, and exit criteria. (Trace: FEAT-investor-development-platform-001)
- US-002: As an investor, I want to review my decision after a defined period so I can compare my thesis to the outcome and learn what worked. (Trace: FEAT-investor-development-platform-001)
- US-003: As a creator, I want to publish an immutable strategy portfolio with a documented decision history so I can demonstrate credibility. (Trace: FEAT-investor-development-platform-001)
- US-004: As an investor, I want to see behavioral analytics like FOMO and risk discipline so I can understand patterns in my investing behavior. (Trace: FEAT-investor-development-platform-001)
- US-005: As an investor reviewing a strategy portfolio, I want quotes, charts, and indicators for the portfolio's symbols so I can evaluate the strategy context without leaving the portfolio. (Trace: FEAT-investor-development-platform-001)
- US-006: As an investor, I want to add a stock to a strategy portfolio for tracking without buying it so the portfolio can act as a private watchlist before any transaction. (Trace: FEAT-investor-development-platform-001)

## 5. Functional requirements

- FR-001: The platform must allow users to create and manage decision records with thesis, evidence, risk factors, confidence level, time horizon, and exit criteria.
- FR-002: The platform must support research workspaces with notes, watchlists, and structured signal validation.
- FR-003: The platform must automatically trigger decision reviews at configurable intervals (30, 90, 180 days, 1 year).
- FR-004: The platform must compute and display decision quality metrics separate from outcome-based performance metrics.
- FR-005: The platform must support public strategy portfolios with immutable transaction history and performance tracking.
- FR-006: The platform must surface behavioral analytics that quantify discipline, loss aversion, and research completeness.
- FR-007: Strategy portfolios must include portfolio-scoped quote rows, historical charts, and technical indicator views for symbols in the strategy.
- FR-008: Strategy portfolios must support tracked-only symbols that do not create positions, transactions, or performance impact until a buy/sell action is recorded.

## 6. Non-functional requirements

- NFR-001: The platform must be reliable enough for daily investment research and decision review workflows.
- NFR-002: The platform must protect user data and not expose private research, positions, or decision records without explicit user consent.
- NFR-003: The platform must be responsive enough that core decision journal and review operations complete in under 2 seconds for typical usage.
- NFR-004: The platform should be extensible to add new analytics and reputation signals in later phases.

## 7. Data and privacy

- The platform will collect and store user research notes, decision records, investment thesis data, and portfolio/position metadata.
- Private investment, tracked symbols, and research data must remain confidential unless the user chooses to publish a strategy portfolio, tracked symbol, or specific decision.
- Public visibility is limited to published strategy portfolios and optional public profile/reputation data.

## 8. Integrations and dependencies

- Data feeds for market prices, reference data, historical performance, and technical indicator inputs used by watchlists and strategy portfolios.
- Authentication and user account management.
- Optional portfolio/holding import integration for strategy portfolio tracking.
- AI research assistant capabilities may be added after the initial MVP.

## 9. Edge cases, errors, failure behavior

- If decision review data is unavailable, the platform must preserve the original decision record and allow manual review entry.
- If market data is delayed, the platform must show the last known price and indicate data freshness.
- If a public strategy portfolio is deleted or unpublished, the platform must preserve the integrity of historical published decisions in the user’s archive.
- If a tracked-only symbol is removed from a strategy portfolio, no transaction history is modified because no buy/sell action occurred.

## 10. Success metrics and definition of done

- Initial release includes a working research workspace, decision journal, review engine, and basic behavioral analytics.
- Success metrics for the MVP:
  - 1,000 users
  - 10,000 decisions recorded
  - 70% of active users creating decision records
  - 50% review completion rate for review-triggered decisions
- Definition of done: MVP is available to users, basic workflows operate end-to-end, and the PRD is linked to the feature primitive.

## 11. Rollout and migration

### Phase 1: Investor Workspace
- Launch core research workspace, watchlists, and decision journaling.
- Validate early user adoption and behavior.

### Phase 2: Review and analytics
- Add decision review cadence and behavior scoring.
- Introduce decision quality metrics and learnings.

### Phase 3: Strategy portfolios and reputation
- Add public strategy portfolios, strategy-scoped quote/chart/indicator research surfaces, shareable credibility signals, and creator-facing features.

## 12. Open questions and decisions

- What is the minimum set of public reputation metrics to launch with the strategy portfolio feature?
- Should the initial MVP include AI research assistance or defer it to a later phase?
- What specific constraints should govern which decision data is published publicly?

## 13. Risks and mitigations

| Risk | Impact | Mitigation | Owner |
| --- | --- | --- | --- |
| Users do not adopt a structured decision workflow | Low initial retention | Focus on simplicity and clear onboarding for decision journaling | Product |
| Outcome bias remains the dominant user mindset | Product misunderstanding | Surface decision quality metrics and educational guidance | Marketing/Product |
| Sensitive investor data is exposed | Reputational and legal risk | Enforce strict privacy defaults and explicit publish controls | Engineering |
| Too much scope in MVP | Delayed launch | Keep initial release focused on core research, decisions, and reviews | Product |

## 14. Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-04 | draft | — | Initial PRD drafted from the Investor Development Platform brief. |
| 0.2 | 2026-06-05 | draft | 0.1 | Linked PRD metadata to IDP concept constraints, invariants, and contracts. |
| 0.3 | 2026-06-05 | draft | 0.2 | Added strategy portfolio quote, chart, and indicator scope. |
| 0.4 | 2026-06-05 | draft | 0.3 | Added tracked-only strategy portfolio symbols as portfolio-native private watchlists. |
