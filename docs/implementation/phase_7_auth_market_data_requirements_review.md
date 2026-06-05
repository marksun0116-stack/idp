---
title: "Phase 7 Auth and Market Data Requirements Review"
status: approved
owner: "Investor Development Platform Team"
last_updated: 2026-06-05
version: "0.1"
---

# Phase 7 Auth and Market Data Requirements Review

## Scope

Phase 7 hardens two MVP shortcuts: development-only bearer identity and placeholder strategy market data. The goal is a local, production-shaped implementation that works in Docker while preserving graceful fallback behavior.

## Included

- User registration, login, and current-user endpoints.
- Opaque bearer tokens persisted by the backend.
- Password hashing with Spring Security's BCrypt encoder.
- Frontend login/register controls that store the returned bearer token.
- Strategy quotes backed by Yahoo Finance chart data where available.
- Strategy history backed by Yahoo Finance chart data where available.
- Strategy indicators derived from historical closes with RSI and trend verdicts.
- Graceful market-data unavailable responses rather than failing the strategy page.

## Deferred

- JWT signing/rotation.
- Email verification and password reset.
- Third-party OAuth.
- Persistent local historical cache.
- Paid/official market-data provider integration.

## Compatibility Decision

Unknown bearer tokens remain accepted as development subjects. Real tokens issued by `/api/users/login` resolve to the registered username. This keeps earlier local data and tests usable while introducing real auth flows.

## Acceptance Criteria

- User can register and log in from API and UI.
- Authenticated `/api/users/me` returns the current registered user for issued tokens.
- Existing bearer-subject development flow still works locally.
- Strategy quote/history endpoints attempt real market data for scoped strategy symbols.
- Indicator endpoint returns an RSI/trend verdict from history when data is available.
- Market-data provider failure does not crash the API.

## Change Log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-05 | approved | - | Initial Phase 7 requirements review for auth and real market data. |
