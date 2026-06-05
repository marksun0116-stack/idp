---
title: "Phase 5A Investor Workspace UI Requirements Review"
status: approved
owner: "Investor Development Platform Team"
last_updated: 2026-06-05
version: "0.1"
---

# Phase 5A Investor Workspace UI Requirements Review

## Scope

Phase 5A creates the first browser-visible investor workspace on top of the completed private APIs from Phases 1-4. The goal is a usable local UI, not the final public/community dashboard.

## Included

- React web frontend served through Docker Compose.
- A private investor identity control backed by the development bearer-subject auth filter.
- Decision journal create/list/detail workflow.
- Review queue summary.
- DQS and behavioral analytics summary panels.
- Strategy portfolio create/list/detail workflow.
- Strategy tracked-symbol add flow that supports watchlist-like tracking without buying.
- Strategy-scoped quote, chart/history, and indicator placeholder surfaces.

## Deferred

- Public profile and reputation UX.
- Community discovery.
- Production authentication.
- Real market data provider integration.
- Full dashboard polish and advanced filtering.

## Decisions

| Topic | Decision | Rationale |
| --- | --- | --- |
| Frontend stack | React + Vite. | Matches KFS tech-stack decision and keeps local iteration fast. |
| Local serving | Docker Compose serves the built frontend through Nginx and proxies `/api` to the backend. | Avoids browser CORS issues and makes `docker compose up --build` enough for local UI access. |
| Auth | UI stores a local investor id and sends `Authorization: Bearer <id>`. | Matches the current backend development auth model. |
| Strategy listing | Add an owner-scoped `GET /api/strategies` endpoint. | A UI cannot reliably reload strategy state after refresh without a list API. |

## Acceptance Criteria

- Opening the frontend URL shows an investor workspace instead of Spring Boot's Whitelabel page.
- User can create a decision and immediately see it in the journal.
- User can see review, DQS, and behavioral analytics summaries for the current investor id.
- User can create a strategy, select it, add tracked symbols, and see strategy quote/history/indicator sections.
- Docker Compose starts `postgres`, `backend`, and `frontend` together.

## Parallelization

- Phase 5 public-profile backend work can proceed in parallel after the private workspace UI is verified.
- Final dashboard integration can reuse Phase 5A components after public profile requirements are approved.

## Change Log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-05 | approved | - | Initial requirements review for the first browser-visible workspace UI. |
