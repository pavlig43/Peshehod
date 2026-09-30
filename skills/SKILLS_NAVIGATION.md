# Skills Navigation

This file is the agent-readable navigation index for the shared skills collection.
It applies across projects and must not contain project-specific decisions.

## Skills

### App Marketing / Store Listing

- `skills/aso/custom-product-pages` — plans alternate App Store product pages
  with audience-specific promotional text, screenshots, and preview videos for
  paid campaigns or partner links. Read this skill when different traffic
  sources need different store-page messaging. Verify current Apple limits,
  routing options, and review behavior before implementation.

- `skills/aso/in-app-events` — produces the copy, artwork brief, dates, and
  submission plan for an App Store In-App Event. Read this skill when promoting
  a temporary challenge, competition, live event, premiere, or major update.
  **Needs correction before use:** fix its event-count and notification claims
  against current Apple documentation.

- `skills/aso/keyword-research` — expands and prioritizes App Store search terms
  using relevance, real demand data, current rankings, and competitor gaps. Read
  this skill before rewriting store metadata. Its opportunity formula is only a
  sorting aid; do not invent volume or difficulty values when data is unavailable.

- `skills/aso/localization` — plans localized store metadata, market-specific
  keyword research, translated screenshot copy, and cultural adaptation. Read
  this skill when expanding an app listing to another language or country.

- `skills/aso/metadata-optimization` — writes and length-checks App Store and
  Google Play titles, subtitles, keyword fields, short descriptions, and full
  descriptions from an existing keyword strategy. Read this skill when preparing
  store copy for a new listing or metadata update.

- `skills/aso/screenshot-optimization` — converts an app's audience, benefits,
  and screens into an ordered screenshot plan, overlay copy, and designer brief.
  Read this skill when creating or revising App Store or Google Play listing
  screenshots. Do not rely on its unsupported user-attention percentages.

- `skills/aso/seasonal-aso` — plans temporary keyword, promotional-text, and
  creative changes around a relevant holiday, season, or short-lived trend, with
  dates for reverting to evergreen metadata. Read this skill for a seasonal store
  campaign. Replace its Appeeky calls with an available source of keyword data.

- `skills/aso/aso-audit` — reviews an App Store or Google Play listing across
  metadata, search terms, creatives, ratings, reviews, and conversion elements,
  then orders the proposed changes. Read this skill when diagnosing weak store
  visibility or conversion. Treat its weighted score as an internal checklist,
  not an objective ASO measurement.

- `skills/aso/category-positioning` — compares plausible App Store categories
  or Google Play categories and tags using product fit, audience, and competitive
  pressure. Read this skill when choosing or changing a store category. **Needs
  correction before use:** verify claims about ranking resets, re-indexing time,
  chart thresholds, and editorial effects against current platform information.

- `skills/aso/competitor-analysis` — compares an app with selected competitors
  across listing copy, keywords, creatives, reviews, pricing, and features. Read
  this skill when looking for concrete positioning, keyword, or product-page gaps.

- `skills/aso/competitor-tracking` — defines a repeatable comparison of
  competitors' metadata, screenshots, ratings, reviews, prices, and rankings.
  Read this skill when monitoring changes over time. **Needs correction before
  use:** replace its Appeeky-specific collection calls with an available source.

- `skills/aso/ab-test-store-listing` — plans tests of store icons,
  screenshots, and preview videos, including hypotheses, variants, duration,
  and interpretation of results. Read this skill when comparing product-page
  creatives in App Store Connect or Google Play Console. Treat its example
  uplift ranges and sample-size shortcuts as rough illustrations, not evidence.

- `skills/aso/app-icon-optimization` — audits an app icon, develops testable
  alternatives, and produces a concrete brief for an icon designer. Read this
  skill when an app icon is hard to recognize, blends into its category, or
  needs variants for a store-listing experiment.

- `skills/aso/app-launch` — turns a planned app release date into a preparation,
  launch-day, and post-launch checklist covering the store listing, testing,
  analytics, support, and promotion. Read this skill when coordinating a new
  mobile-app launch or a major release; adapt its fixed timeline to the project.

- `skills/aso/app-preview-video` — creates a shot-by-shot script and production
  checklist for App Store preview videos and Google Play promo videos. Read this
  skill when planning, recording, or revising a store-listing video. Verify
  current platform specifications and submission rules before production.

### App Marketing / Routing

- `skills/aso/aso-router` — routes an app-marketing request to the relevant
  specialist skill in the ASO collection. Read this skill when the request spans
  several areas or the appropriate ASO skill is unclear. **Needs correction
  after this collection review:** update its paths and remove routes to skills
  selected for deletion.

### App Marketing / Paid Acquisition

- `skills/aso/apple-search-ads` — structures Apple Search Ads campaigns,
  separates keyword groups, reviews search terms, and proposes bid or budget
  changes from actual campaign results. Read this skill when setting up or
  improving Apple Search Ads. Verify its example bids, benchmarks, audience
  claims, and platform features before use.

- `skills/aso/attribution-setup` — maps how paid installs and revenue should be
  attributed across iOS, Android, ad networks, MMPs, and deep links. Read this
  skill when implementing or debugging install attribution. **Needs substantial
  correction before use:** its SKAN, AdAttributionKit, SDK, and privacy guidance
  is dated and must be rebuilt from current official documentation.

- `skills/aso/ua-campaign` — builds a paid app-acquisition plan across Apple
  Search Ads, Meta, Google, TikTok, and other channels from the available budget,
  markets, LTV, and campaign results. Read this skill when launching or revising
  install campaigns. Update platform names and features, and derive bids and
  targets from current account data rather than its example ranges.

- `skills/aso/web-to-app-funnel` — designs the path from a website or web payment
  to app installation, account recognition, deep-linked activation, and funnel
  measurement. Read this skill when users discover or pay on the web before
  entering the mobile app. Verify current Apple and Google external-payment rules
  for the relevant store and country before applying any compliance advice.

### App Monetization and Onboarding

- `skills/aso/monetization-strategy` — compares subscription, one-time purchase,
  in-app purchase, advertising, and hybrid models, then outlines pricing and
  where to present the offer. Read this skill when selecting or revising an app's
  revenue model. Do not reuse its generic prices or conversion ranges without
  product-specific evidence.

- `skills/aso/onboarding-optimization` — maps the path from first launch to the
  first useful action, identifies avoidable screens and permission requests, and
  proposes a shorter flow. Read this skill when new users abandon onboarding or
  fail to reach activation. **Needs correction before use:** remove universal
  permission-screen rules and unsupported benchmark claims.

- `skills/aso/paywall-optimization` — diagnoses where an app's payment funnel
  loses users, reviews the current paywall, and proposes one-variable tests of
  its copy, plans, pricing display, or timing. Read this skill when trial starts
  or purchases are weak. **Needs correction before use:** verify store-policy
  requirements and replace generic healthy ranges with product-specific data.

- `skills/aso/subscription-lifecycle` — reviews trial conversion, renewals,
  voluntary and failed-payment churn, cancellation feedback, and subscriber
  return campaigns. Read this skill when diagnosing a subscription business
  after the paywall. **Needs substantial correction before use:** verify billing
  retry timing, grace periods, cancellation capabilities, and win-back behavior
  against current StoreKit and Google Play Billing documentation.

### App Retention, Ratings, and Reviews

- `skills/aso/rating-prompt-strategy` — selects useful moments and eligibility
  conditions for showing the native iOS or Android rating prompt and outlines a
  recovery process after a rating decline. Read this skill when implementing or
  revising an app's request-for-rating flow.

- `skills/aso/retention-optimization` — uses retention data and observed user
  behavior to locate where users stop returning and to propose focused changes
  to activation, engagement, and re-engagement. Read this skill when Day 1,
  Day 7, or Day 30 retention is weak. Use the app's own cohort data rather than
  treating its generic benchmark table as a target.

- `skills/aso/review-management` — groups app reviews by recurring problem or
  praise, extracts product signals, and drafts specific developer responses.
  Read this skill when ratings fall, negative reviews accumulate, or competitor
  reviews need to be mined for unmet needs.

### App Marketing / Creators

- `skills/aso/creator-ugc-marketing` — produces a creator outreach plan, a
  concrete content brief, usage-rights requirements, and a way to measure the
  resulting installs. Read this skill when commissioning influencer or UGC
  videos for an app. Treat its prices and claimed advertising improvements as
  illustrations, not dependable benchmarks.

### App Marketing / PR and Referrals

- `skills/aso/press-and-pr` — turns an app launch or update into a press angle,
  journalist pitch, press release, press-kit checklist, and outreach schedule.
  Read this skill when seeking media coverage for an app. Verify that suggested
  publications, contacts, and platform tactics are still current.

- `skills/aso/referral-program` — designs an in-app referral program, including
  reward economics, invite links, qualification rules, analytics events, fraud
  controls, and launch checks. Read this skill when users should be able to invite
  others in exchange for product benefits, credit, or another reward.

### App Analytics

- `skills/aso/app-analytics` — builds an in-app event-tracking plan, measurement
  funnel, and dashboard outline for acquisition, activation, retention, and
  revenue. Read this skill when deciding what an app should record or when
  diagnosing missing analytics. Treat its broad benchmark ranges as examples,
  not universal targets.

- `skills/aso/crash-analytics` — turns Crashlytics or store crash data into a
  prioritized repair queue based on affected users and the importance of the
  broken flow. Read this skill when triaging mobile crashes or planning a staged
  release. **Needs correction before use:** update Firebase integration examples,
  remove obsolete Bitcode guidance, and verify its fixed thresholds.

### Android / Google Play ASO

- `skills/aso/android-aso` — reviews and drafts Google Play titles, short and
  full descriptions, creatives, localization, ratings work, and Store Listing
  Experiments. Read this skill when improving an Android store listing. Verify
  current Play Console limits, policies, and ranking claims before applying it.

### iOS / App Clips

- `skills/aso/app-clips` — helps plan the user flow, invocation URLs,
  App Store Connect setup, handoff to the full app, and measurement for an
  App Clip that runs without installing the full iOS app. Read this skill when
  designing or implementing an App Clip. **Needs correction before use:** its
  stored size limits and other platform restrictions are outdated and must be
  checked against current Apple documentation.

### Android / Performance

- `skills/android/performance/r8-analyzer` — analyzes Android R8 configuration
  and ProGuard/keep rules, helping identify overly broad or redundant rules.
  Read this skill when preparing optimized release builds, reducing app size,
  updating AGP or dependencies, or investigating R8-related release issues.

### Android / Google Play Billing

- `skills/android/play/play-billing-library-version-upgrade` — upgrades an
  Android project's Google Play Billing Library integration from a legacy
  version to the latest stable version, including API migrations, dependency
  and SDK alignment, and build verification. Read this skill when updating
  Play Billing, responding to a version deprecation deadline, or modernizing
  an older in-app purchase or subscription implementation.

### Android / System UI

- `skills/android/system/edge-to-edge` — migrates Jetpack Compose interfaces
  to adaptive edge-to-edge layouts and helps troubleshoot system-bar, IME,
  list, FAB, and full-screen dialog insets. Read this skill when raising an
  Android app's target SDK, adopting edge-to-edge rendering, or fixing content
  obscured by status bars, navigation bars, display cutouts, or the keyboard.
  Caution: its recommended API is outdated: `ComponentActivity.enableEdgeToEdge()`
  is deprecated in favor of `WindowCompat.enableEdgeToEdge(window)`, while the
  skill still bases its guidance on the former API distinction.

### Kotlin Multiplatform / Dependency Injection

- `skills/metro-di/SKILL 2.md` — provides a project-independent starting point
  for configuring and modifying Metro dependency injection in Kotlin and KMP
  projects, including graph placement, bindings, aggregation, and platform
  source sets. Read this draft when a task uses Zac Sweers Metro, but verify and
  correct its API details against the current official Metro documentation
  before applying it; it has not yet been technically updated.

### Kotlin Multiplatform / Decompose and MVIKotlin

- `skills/decompose-mvikotlin/decompose-component` — structures Decompose
  components and covers `ComponentContext` delegation, observable `Value`
  state, lifecycle callbacks, state preservation, retained instances, back
  handling, and preview or test doubles. Read this skill when creating or
  changing a Decompose component, deciding how component state should survive
  configuration changes or process death, or implementing component-level
  lifecycle and back-button behavior.

- `skills/decompose-mvikotlin/decompose-navigation` — selects and implements
  Decompose navigation models including `ChildStack`, `ChildSlot`,
  `ChildPages`, `ChildPanels`, and multiple navigation trees, with guidance on
  configuration serialization, deep links, result delivery, and stack
  operations. Read this skill when designing screen, dialog, pager, tab, or
  adaptive master-detail navigation with Decompose.

- `skills/decompose-mvikotlin/decompose-compose` — connects Decompose
  components and navigation state to Compose Multiplatform UI, including
  `subscribeAsState`, child rendering, animations, predictive back, desktop
  lifecycle integration, and previews. Read this skill when implementing the
  Compose presentation layer for Decompose components or troubleshooting
  state observation, child rendering, and back handling in Compose.

- `skills/decompose-mvikotlin/mvikotlin-code` — implements and reviews
  MVIKotlin stores integrated with Decompose, coroutine executors, retained
  stores, labels, state-to-model mapping, dependency injection, logging, time
  travel, and focused tests. Read this skill when working in a repository that
  follows its documented MVIKotlin + Decompose + Metro architecture. Its
  version, paths, package names, file layout, and DI conventions are
  repository-specific and must be checked against the target project before
  applying them.

### Kotlin / Language and Coroutines

- `skills/kotlin/kotlin-control-flow` — reshapes Kotlin branching with subject
  `when`, guard conditions, exhaustive closed-domain handling, smart casts, and
  early returns. Read this skill when writing or reviewing complex `if`/`when`
  logic; confirm that the project's Kotlin version supports guard conditions.

- `skills/kotlin/kotlin-coroutines-structured-concurrency` — reviews coroutine
  scope ownership, construction-time launches, fire-and-forget APIs,
  cancellation handling, and `runBlocking` boundaries. Read this skill when a
  class stores a `CoroutineScope`, launches hidden background work, or catches
  broad exceptions around suspending calls. Apply its scope rules with the
  documented exceptions for genuine lifecycle owners and UI state holders.

- `skills/kotlin/kotlin-flow-state-event-modeling` — selects among `StateFlow`,
  `SharedFlow`, channel-backed flows, and cold `Flow`, and reviews `stateIn`,
  sharing policy, atomic state updates, and sentinel initial values. Read this
  skill when designing state or one-shot event APIs. Remember that channel
  delivery can still lose an element if its receiving collector is cancelled.

- `skills/kotlin/kotlin-functions` — chooses between member, top-level,
  extension, factory, and service functions according to semantic ownership.
  Read this skill when an operation is attached to a primitive, collection,
  framework, or third-party type. Treat its rejection of library-type
  extensions as a strict architectural heuristic rather than a Kotlin rule.

- `skills/kotlin/kotlin-types-value-class` — chooses between primitives,
  type aliases, value classes, and data classes while checking boxing,
  serialization, equality, Compose stability, and framework boundaries. Read
  this skill when introducing or refactoring single-field domain wrappers;
  verify Java interop explicitly because value-class APIs are mangled by
  default.

### Compose / UI and Component Design

- `skills/compose-multiplatform/compose-animations` — selects Compose animation
  APIs for visibility, individual or coordinated values, content replacement,
  size changes, and gesture-driven motion. Read this skill when implementing or
  reviewing motion in a Compose interface.

- `skills/compose-multiplatform/compose-focus-navigation` — handles initial and
  restored focus, directional navigation, key events, and focus testing for
  keyboard, D-pad, TV, and desktop interfaces. Read this skill when Compose UI
  must work without touch input.

- `skills/compose-multiplatform/compose-modifier-and-layout-style` — guides
  composable modifier parameters, modifier ordering and construction, root
  layout responsibilities, conditional containers, and measurement-dependent
  layout. Read this skill when designing or reviewing Compose layout APIs.

- `skills/compose-multiplatform/compose-multiplatform-adaptive-design` — designs
  adaptive Compose Multiplatform interfaces for phones, tablets, foldables,
  desktop windows, and connected displays. Read this skill when choosing window
  size policies, navigation chrome, canonical pane layouts, or resize tests.

- `skills/compose-multiplatform/compose-slot-api-pattern` — designs reusable
  composables with content slots, optional regions, layout scopes, and defaults.
  Read this skill when a component's visual regions vary between callers or its
  API is accumulating content flags and primitive presentation parameters.

### Compose / State, Effects, and Architecture

- `skills/compose-multiplatform/compose-side-effects` — selects and reviews
  `LaunchedEffect`, `DisposableEffect`, `SideEffect`, `rememberCoroutineScope`,
  `rememberUpdatedState`, and `snapshotFlow`. Read this skill when imperative or
  suspending work must follow the Compose lifecycle.

- `skills/compose-multiplatform/compose-state-authoring` — covers local Compose
  state, snapshot-aware collections, composition-time state writes, and
  `@ReadOnlyComposable`. Read this skill when authoring or reviewing remembered
  state inside composables.

- `skills/compose-multiplatform/compose-state-hoisting` — decides whether UI
  state belongs locally, at a common composable owner, in a plain state holder,
  or in a screen-level state holder. Read this skill when state ownership or UI
  logic placement is unclear.

- `skills/compose-multiplatform/compose-state-holder-ui-split` — separates
  ViewModel, component, flow, navigation, and effect wiring from plain
  state-driven Compose rendering. Read this skill when a screen is difficult to
  preview, test, or reuse because layout and application wiring are mixed.

### Compose / Performance

- `skills/compose-multiplatform/compose-recomposition-performance` — routes
  recomposition investigations toward stability, deferred state reads, or
  cross-phase state writes. Read this skill when the source of excessive Compose
  recomposition is not yet clear.

- `skills/compose-multiplatform/compose-stability-diagnostics` — interprets
  Compose compiler reports, strong skipping, parameter comparison, immutable
  collections, and call-site churn. Read this skill when unstable parameters or
  failed skipping are suspected.

- `skills/compose-multiplatform/compose-state-deferred-reads` — moves frequent
  state reads into layout or draw phases and diagnoses backwards writes between
  Compose phases. Read this skill when scrolling, animation, gestures, or shared
  measurements cause unnecessary recomposition.

### Compose / Testing

- `skills/compose-multiplatform/compose-ui-testing-patterns` — chooses between
  state-driven UI tests, semantics assertions, screenshot tests, focus or key
  tests, interaction-state tests, and integration tests. Read this skill when
  planning or reviewing Compose UI test coverage.

### Web Interface Design / Review

- `skills/design/better/better-interface` — coordinates one consolidated review
  of accessibility, layout, writing, typography, color, and visual polish. Read
  this skill when auditing a complete web screen, flow, or feature. Before use,
  remove its unconditional dependency on the project-specific
  `design-app-system`; anchor the review in the target project's own design
  system instead.

- `skills/design/better/better-accessibility` — reviews and improves semantic
  HTML, keyboard and focus behavior, forms, screen-reader output, target sizes,
  reduced motion, and zoom behavior. Read this skill when building interactive
  components or investigating accessibility defects. Distinguish its broader UX
  recommendations from normative WCAG requirements.

- `skills/design/better/better-layout` — structures web interfaces through
  grouping, alignment, reading order, responsive breakpoints, safe areas, RTL,
  and localization resilience. Read this skill when arranging a page or when a
  layout breaks across sizes, directions, or longer translated content.

- `skills/design/better/better-writing` — writes and reviews concise interface
  copy for controls, forms, errors, confirmations, settings, onboarding, and
  empty states. Read this skill when user-facing wording is unclear,
  inconsistent, difficult to translate, or does not explain recovery.

- `skills/design/better/better-typography` — covers web-font selection and
  loading, type scales, OpenType features, line length, wrapping, truncation,
  mixed-direction text, and text accessibility. Read this skill when choosing
  or diagnosing how interface text renders. Treat font-smoothing and fixed
  sizing advice as contextual design choices rather than universal rules.

- `skills/design/better/better-colors` — works with OKLCH conversion, palette
  construction, gamut limits, semantic color tokens, appearance variants, and
  rendered-pair contrast. Read this skill when creating or reviewing a web color
  system. Use APCA and lightness thresholds as design aids; use the applicable
  WCAG method for compliance decisions.

- `skills/design/better/better-ui` — refines web-interface surfaces, radii,
  shadows, icons, press states, transitions, and small visual interactions. Read
  this skill when an otherwise functional interface lacks consistency or polish.
  Adapt its exact scale, blur, duration, and spring values to the project's
  established visual and motion language.

### Web Interface Design / Motion

- `skills/design/motion/apple` — translates Apple's principles for direct
  manipulation, velocity handoff, momentum, interruptible springs, soft
  boundaries, materials, and reduced-motion behavior to web interfaces. Read
  this skill when designing gesture-driven drawers, sheets, draggable elements,
  or other physical interactions; verify current browser support for the web
  equivalents it proposes.

- `skills/design/motion/improve` — surveys motion across a codebase, prioritizes
  existing problems, and writes self-contained implementation plans without
  changing source code. Read this skill when a project needs a motion roadmap
  rather than a review of one diff. Its performance rules are conservative:
  confirm actual profiling evidence before replacing Motion transform
  shorthands or declaring dropped-frame risk.

- `skills/design/motion/opportunities` — searches for static state changes that
  would genuinely benefit from motion and records the candidates it deliberately
  rejects. Read this skill when an interface feels abrupt or lifeless but should
  not be animated indiscriminately. Treat frequency cutoffs and exact timings as
  starting heuristics, not automatic prohibitions.

- `skills/design/motion/review` — reviews animation changes in a diff for
  purpose, frequency, easing, duration, origin, interruptibility, performance,
  and reduced-motion handling. Read this skill for a focused motion code review.
  Its block criteria express a deliberately strict house style; apply them in
  the product context and verify performance claims by measurement.

- `skills/design/motion/vocabulary` — maps a visual description of an animation
  to a concise established term and distinguishes nearby alternatives. Read this
  skill when the user knows the effect they want but not the name needed to
  discuss, search for, or specify it.
