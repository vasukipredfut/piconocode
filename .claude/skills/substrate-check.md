---
description: Audit the current change (uncommitted files, staged files, or a specific branch) against the EARMS substrate-first discipline. Catches anti-patterns from docs/architecture/CODE-REVIEW-develope-2026-07-11.md BEFORE they land in a PR. Use before every commit.
---

# Substrate check

Run this **before every commit** or before opening a PR. It applies the 10 anti-pattern checks from the substrate-first policy.

## What to check

Walk the changed files (staged + unstaged + files touched on the current branch since `main`) and produce a report of findings, scored by severity.

### 🔴 CRITICAL — reject the commit

1. **New `@Entity` class** — every new Java file annotated `@Entity` is a strategy violation UNLESS it's under `com.idayam.m0.*` (platform substrate itself). Business entities live in `entity_registry` + `entity_field_registry`, not `@Entity`.

2. **New `JpaRepository<>` interface** — same rule. If a file extends `JpaRepository`, `CrudRepository`, or `Repository`, it must be under `com.idayam.m0.*`.

3. **New per-entity controller** — any file matching `*Controller.java` that maps a `/api/m<N>/...` route for a specific business object. The only allowed controllers are:
   - Under `com.idayam.m0.*` (platform)
   - Under `com.idayam.m11.*` (integration adapters)
   - Government-form endpoints under `com.idayam.m6.gst.*` or `com.idayam.m7.govforms.*`
   Everything else should go through `GenericEntityController` at `/api/m0/entities/{code}`.

4. **Modified baseline migration** — any change to `src/main/resources/db/migration/V*.sql` where `V*` was already tagged as PUBLISHED (i.e. present on `main`). Flyway records checksums; modifying V3_2 breaks every DB that already ran it. Ship a NEW `Vn` migration instead.

5. **Migration file collision** — two files with the same `Vn` prefix. Grep `git log --all --format="" --name-only | grep V<N>__ | sort -u` before picking a version number.

### 🟠 HIGH — flag for review

6. **Commented-out code block > 20 lines** — either delete it (git remembers) or explain in a comment WHY it's kept.

7. **Hard-coded IP address in `application.properties`, `application-*.properties`, or `setupProxy.js`** — anything other than `127.0.0.1` is a red flag. Environment-specific config goes in `application-local.properties` (git-ignored) or via env-var override.

8. **Binary file inside `src/main/resources/db/migration/`** — `.zip`, `.jar`, `.gz`, `.dump`, `.png` — none of them belong there.

9. **Per-entity DTO** — any `*Dto.java`, `*Request.java`, `*Response.java`, `*Summary.java`, `*List.java` class that describes a single entity's wire shape. The datasource-query service already returns `{content, totalElements}` in a well-known shape.

10. **Typo in a public class name** — check every new/renamed class name against a spellchecker + against common English words. `WeightmnetEntry` / `ParkingRequsetDto` / `parkingHistoryList` (lowercase-first-letter) all fail this check.

## Output format

Produce a markdown table with columns:

| # | Severity | Rule | File | Line | Finding | Suggested fix |
|---|---|---|---|---|---|---|

Then a summary line:

- **CRITICAL**: `<n>` — commit **should be rejected** until fixed
- **HIGH**: `<n>` — flag before merging
- **CLEAN**: no findings — safe to commit

## How to run

Ask the user to confirm the diff scope first:

- "Staged files only" — use `git diff --cached --name-only`
- "All changes on this branch since main" — use `git diff --name-only main..HEAD` + `git diff --name-only main..HEAD --diff-filter=A` for new files
- "Uncommitted (staged + unstaged)" — `git diff --name-only HEAD` + `git ls-files --others --exclude-standard`

For each file, `git diff HEAD -- <path>` to see the change, then apply the 10 rules above.

## What NOT to flag

- Changes to `com.idayam.m0.*` are ALLOWED for all 10 rules — that IS the substrate.
- Changes to `com.idayam.m11.adapter.*` are ALLOWED for rule 3 — adapters are the sanctioned integration layer.
- Changes to migrations V78 onwards are ALLOWED for rule 4 — those are still on the working branch.
- Test files (`*Test.java`, `*.spec.js`, `*.test.js`) are ALLOWED for rule 6 — tests explain their comments.

## Extend the check with SonarQube rules

Beyond the 10 substrate anti-patterns, apply these Sonar rules to every changed Java file. Any hit → add to the finding list (severity HIGH unless noted).

| Sonar rule | Check | Threshold |
|---|---|---|
| **S125** | Commented-out code | 0 blocks > 5 lines |
| **S1192** | Duplicated string literals | Same string literal used ≥ 3 times → extract constant |
| **S3776** | Cognitive complexity per method | ≤ 15 |
| **S138** | Method length | ≤ 80 lines |
| **S1854** | Unused local variables | 0 |
| **S107** | Method parameter count | ≤ 5; use record for more |
| **S110** | Class inheritance depth | ≤ 5 levels |
| **S106** | `System.out` / `System.err` usage | 0; use `Logger` |
| **S2699** | Test methods missing assertions | 0 |
| **S1128** | Unused imports | 0 |
| **S1118** | Utility classes with public constructor | 0 (all utility constructors must be private) |
| **SM01** | Table without `tenant_id` for business data | flag if applicable |
| **SM03** | `INSERT` without `WHERE NOT EXISTS` guard in migrations | 0 |
| **SM07** | `entity_registry` row without matching `entity_field_registry` rows | 0 |

Every finding above lands in the same table format as the 10 substrate anti-patterns. Threshold: any CRITICAL blocks; HIGH is flagged for review.

## Engineering principles cross-check

Also apply these questions to every changed file:

- **Single responsibility?** Does this class/method do more than one thing? If yes, extract.
- **Testable?** Can this be exercised by a unit test without spinning up Spring context? If no, refactor collaborators to interfaces.
- **Fail fast?** Are inputs guarded at the top of each method?
- **Idempotent?** Can this migration/probe/handler run twice safely?
- **YAGNI?** Any field, method, or parameter that's not exercised by production code paths?

Full standards + rule catalogue: `docs/architecture/ENGINEERING-STANDARDS.md`

## Coverage requirements

Every changed file below must have coverage after this PR:

- **New Java class** → matching `<ClassName>Test.java` under `src/test/java/<same-package>/`. Minimum: 1 happy-path test + 1 edge case.
- **Modified Java class** → the change must be covered by an existing test, or add one.
- **New migration `Vn__*.sql`** → verify via `M0PageSchemaInitialiser` probe fires + a Playwright API test hits any new registered entity/page.
- **Coverage matrix must move by at least +1 line** — run `scripts/coverage-matrix.py` and confirm the new element is counted.

## Reference

- Full policy document with worked examples: `docs/architecture/CODE-REVIEW-develope-2026-07-11.md`
- Full engineering standards: `docs/architecture/ENGINEERING-STANDARDS.md`
- Coverage matrix generator: `scripts/coverage-matrix.py`
