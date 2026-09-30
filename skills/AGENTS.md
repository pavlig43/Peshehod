# Working Context

We are reviewing a shared, project-independent collection of agent skills one skill at a time.

The agent provides a very concise review and a recommendation. The user makes the
final keep/remove decision. Do not treat the agent's recommendation as a
decision, and do not update `SKILLS_NAVIGATION.md` or `SKILLS_TO_DELETE.md` until
the user explicitly states the decision.

When skills are reviewed in a batch, the user may name only the skills that
should be removed or otherwise treated as exceptions. Every reviewed skill in
that batch that the user does not name for removal is considered approved to
keep and must be added to `SKILLS_NAVIGATION.md`.

The primary question for every review is whether the skill can provide practical value in any realistic future project as part of this shared, project-independent collection. A skill does not need to be useful in the current project or used frequently. "This may be useful someday" is a valid reason to keep it when the future use case is realistic and the instructions are sufficiently reliable. Do not remove a skill merely because there is no current need for it.

Every review has only two possible outcomes: keep or remove. Remove a skill only when it has no meaningful use case, adds no practical value beyond skills already kept, depends on unavailable infrastructure without a useful standalone workflow, or is too unreliable or unsafe to use as written.

For each skill, determine:

- what the skill actually does;
- when it is useful during a project's lifecycle;
- whether it should be kept or removed;
- whether its instructions are technically reliable enough for that intended use. Check technical details only as far as needed to judge the skill's practical value and safety; do not turn every review into an exhaustive audit.

Evaluate future usefulness, not only whether the current project already contains enough code to run the skill.

Default to a very concise, practical review. Read enough to understand the skill's purpose,
workflow, and likely value, but do not turn every review into a full technical audit,
fact-check every reference, or enumerate minor defects unless the user explicitly asks.
The number of files in a skill does not by itself justify a longer review. Focus the
answer on what the skill does, when it is useful, and the keep/remove decision.

## Expected review format

Keep each skill assessment short: state what the skill does, its main practical
caveat, and the keep/remove recommendation. Do not replace these per-skill
conclusions with an extended audit of the whole directory. When reviewing several
skills together, give one concise numbered item per skill. Mention only the
limitation that materially affects the recommendation; do not list every technical
defect found during reading.

Examples:

1. `release-checklist` — builds a practical sequence of checks before publishing
   an app. Some suggested timelines are too rigid, but the workflow remains useful.
   **Recommend keeping it.**
2. `legacy-api-recovery` — gives repair instructions for an obsolete platform API.
   Its central technical assumptions are no longer valid, so following it can
   produce incorrect changes. **Recommend removing it.**

Read English skill sources only. Do not open paths or links marked `ru` or `skills-ru`. Communicate with the user in Russian.

`SKILLS_NAVIGATION.md` is an agent-facing discovery map, not a review log or a
decision register. Add only reviewed and agreed skill locations, what each skill
helps with, and the situations in which another agent should open it. Do not put
keep/remove decisions or review commentary in this file. Update
`SKILLS_TO_DELETE.md` only after an explicit removal decision.
