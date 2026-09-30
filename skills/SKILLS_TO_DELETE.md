# Skills to Delete

This file lists skills that were reviewed and explicitly selected for removal.
Do not add a skill until that decision has been made with the user.

## Skills

- `skills/aso/app-marketing-context` — adds a mandatory context document and
  questionnaire instead of solving an app-marketing task directly.

- `skills/aso/app-rejection-recovery` — relies on stored, fast-changing App
  Store and Google Play policy guidance without requiring current official
  verification, making it unreliable for rejection and appeal decisions.

- `skills/aso/app-store-featured` — largely speculative advice about Apple's
  editorial preferences, presented as concrete selection criteria.

- `skills/aso/asc-metrics` — requires an unavailable paid Appeeky connection and
  references integration documentation that is missing from this collection.

- `skills/aso/market-movers` — cannot collect the chart history it analyzes
  without unavailable Appeeky MCP commands.

- `skills/aso/market-pulse` — its market briefing depends entirely on unavailable
  Appeeky feeds for chart movements, keyword trends, featuring, and new releases.

- `skills/design/app-system` — hard-codes Claude and Anthropic colors,
  typography, components, and brand assumptions into a supposedly general
  application design system, so it can contaminate unrelated projects.

- `skills/design/polish/emil-kowalski` — substantially duplicates the retained
  UI-polish and motion skills, repeats their most categorical technical claims,
  and adds a mandatory promotional response without providing a distinct
  workflow.

- `skills/hindsight-docs` — documentation for the Hindsight memory system, which
  is not used and is not expected to be used in future projects.

- `skills/implement-issue` — requires an unavailable set of Superpowers workflow
  skills and explicitly forbids any standalone fallback when they are missing.

- `skills/miniapp-procedural-audio` — depends on a project-specific Kotlin audio
  API, presets, tests, and documentation that are unavailable in this shared
  collection, leaving no useful standalone workflow.

- `skills/puzzle-game-creation` — is tightly coupled to the Logica MiniApp
  scaffold, contributor protocol, host APIs, and the separately rejected
  `miniapp-procedural-audio` skill, so it is not a reliable standalone workflow
  for this shared collection.
