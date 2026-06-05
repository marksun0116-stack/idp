---
title: "Phase 5 Public Profile Requirements Review"
status: approved
owner: "Investor Development Platform Team"
last_updated: 2026-06-05
version: "0.1"
---

# Phase 5 Public Profile Requirements Review

## Scope

Phase 5 adds public investor profiles and approved reputation signals. Public profile data must be explicitly configured by the owner and must not expose private decisions, review notes, private strategy portfolios, or private tracked symbols.

## Included

- Owner-scoped public profile upsert.
- Owner-scoped public profile read for the current authenticated user.
- Public profile read by handle.
- Public profile reputation metrics controlled by `publishedMetricIds`.
- Published strategies limited to strategy portfolios with `public` visibility.
- Public strategy symbols limited to symbols with `public` visibility.

## Deferred

- Profile image upload.
- Follower/community discovery.
- Reputation ranking.
- Moderation workflow.
- Production authentication and handle reservation.

## Decisions

| Topic | Decision | Rationale |
| --- | --- | --- |
| Publish model | Profiles are public only after the owner creates a public profile. | Avoids accidental public presence. |
| Metrics | `dqs`, `researchDiscipline`, `riskManagement`, and `strategyConsistency` are the first allowed metric ids. | Keeps reputation surfaces understandable and avoids leaking behavioral detail. |
| Public strategies | Profile responses include only public strategy portfolios. | Aligns with private-data visibility invariant. |
| Private symbols | Public profile strategy summaries count only public tracked symbols. | Prevents private watchlist leakage. |

## Acceptance Criteria

- Owner can create or update a public profile with handle, display name, bio, and approved metric ids.
- Public read by handle returns approved metrics and public strategies only.
- Public read returns `404` when a handle has not been published.
- Private strategy portfolios do not appear in public profile responses.
- Private tracked symbols do not appear in public strategy counts.

## Change Log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-05 | approved | - | Initial Phase 5 public profile requirements review. |
