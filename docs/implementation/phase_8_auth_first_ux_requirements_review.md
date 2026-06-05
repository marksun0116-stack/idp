---
review_id: RR-008
phase: "Phase 8 — Auth-First UX"
status: done
reviewed_on: 2026-06-05
linked_plan: PLAN-investor-development-platform-001
linked_story: US-015
---

# Phase 8 Auth-First UX Requirements Review

## Goal

Make authentication the default application entry point while preserving an explicit demo path for local development and evaluation.

## Requirements

- The browser app must start on a login/register screen when no issued token or demo-mode flag exists.
- Successful login must persist the backend-issued bearer token and load the investor workspace.
- Registration must create a local account, then sign the user into the workspace.
- Demo access must be explicit through a visible "Continue as demo user" action, not an implicit fallback.
- Logout or exiting demo must clear workspace state and return to the login/register screen.
- The authenticated workspace must show whether it is using a real token or demo mode.
- Demo mode may keep using the existing development bearer-subject compatibility path.

## Acceptance Criteria

- Opening `http://localhost:3000` with no local auth state shows the auth screen.
- Login and register actions remain connected to `CONR-user-api-001` endpoints.
- The dashboard and private APIs load only after login or explicit demo entry.
- Logout from the workspace returns to the auth screen.
- Settings and sidebar copy distinguish authenticated identity from demo identity.

## Risks And Constraints

- The development bearer fallback remains available for demo mode so existing local smoke scripts are not broken.
- The UI must not remove the Phase 7 backend token compatibility work.
- Existing owner-scoped API behavior is unchanged.

## Outcome

Requirements accepted and implemented in US-015.
