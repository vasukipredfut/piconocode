package com.aria.archunit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rule T — tenant scoping is structural, not a convention.
 *
 * <h2>Why a build-time rule</h2>
 * Tenant scoping was previously enforced by a hand-maintained allowlist naming
 * 5 tables out of the 146 that carry a {@code tenant_id}. Everything it omitted
 * leaked — not by decision, but because nobody remembered to add it. That is the
 * defining property of the bug class: <b>it fails open and it fails silently.</b>
 * An unscoped query returns more rows than it should, which looks like data, not
 * like an error. Nothing throws. No test goes red.
 *
 * <p>Replacing the allowlist with {@link com.aria.security.TenantScope} (which
 * derives membership from the schema) removes the "remember to add the table"
 * failure. This test removes the "remember to add the predicate" failure — the
 * one that is still, otherwise, a human promise.
 *
 * <h2>What is banned</h2>
 * <ol>
 *   <li><b>Client-supplied tenancy.</b> Reading a tenant id out of a request
 *       body/param is always wrong: it lets a caller name someone else's firm.
 *       Tenancy comes from the authenticated context, full stop.</li>
 *   <li><b>Re-introducing a hardcoded tenant allowlist.</b> The schema is the
 *       source of truth.</li>
 *   <li><b>A literal tenant id in SQL.</b> {@code tenant_id = 1} pins every
 *       deployment to one firm and is invisible until a second firm exists.</li>
 * </ol>
 *
 * <p>Source-text based rather than bytecode: these are properties of the SQL
 * strings, which ArchUnit's type graph cannot see.
 */
class TenantScopeArchTest {

    private static final Path SRC = Paths.get("src/main/java/com/aria");

    /**
     * Reading tenancy from CALLER-supplied input.
     *
     * <p>Deliberately narrow: it targets request-shaped identifiers
     * ({@code req}, {@code body}, {@code payload}, …) and the binding
     * annotations. Reading {@code tenant_id} off a row fetched from the DB is
     * legitimate and must not trip this — e.g. AiAgentDispatcher reads the
     * agent's own tenant from its {@code ai_agent} row.
     *
     * <p>A rule that cries wolf gets disabled, and a disabled rule protects
     * nothing. Precision is the point.
     */
    private static final Pattern CLIENT_TENANT = Pattern.compile(
            "(?i)\\b(req|request|body|payload|input|inputs|params|dto)\\s*\\.\\s*"
          + "(get|getOrDefault)\\s*\\(\\s*\"tenant_?id\"|"
          + "@RequestParam[^)]*\"tenant_?id\"|"
          + "@PathVariable[^)]*\"tenant_?id\"");

    /**
     * A literal tenant id baked into SQL — read or write.
     *
     * <p>Requires a quote on the line, so this matches SQL string literals and
     * not constant declarations like {@code DEFAULT_TENANT_ID = 1L}.
     *
     * <p>The {@code VALUES (1,} arm exists because the first version of this rule
     * only looked for {@code tenant_id = 1} and therefore missed
     * {@code INSERT INTO m5_shift_handover (tenant_id, ...) VALUES (1,?,?...)} —
     * a hardcoded tenant on the WRITE side, which is strictly worse than on a
     * read: it stamps another firm's id onto new rows.
     */
    private static final Pattern LITERAL_TENANT_SQL = Pattern.compile(
            "(?i)\"[^\"]*\\btenant_id\\s*=\\s*[0-9]+|"
          + "(?i)\\(\\s*tenant_id\\s*,[^\"]*\"[^\"]*VALUES\\s*\\(\\s*[0-9]+\\s*,");

    /** A hardcoded set of "these tables are tenant-scoped". */
    private static final Pattern TENANT_ALLOWLIST = Pattern.compile(
            "TENANT_SCOPED\\s*=\\s*Set\\.of");

    @Test
    @DisplayName("Rule T1: tenancy never comes from client input")
    void tenantNeverFromClient() throws IOException {
        List<String> hits = scan(CLIENT_TENANT);
        assertThat(hits)
                .describedAs("Tenant id must come from TenantContext (the JWT), never from the "
                        + "request. A caller who can name a tenant id can read another firm's "
                        + "data. Offenders:\n  " + String.join("\n  ", hits))
                .isEmpty();
    }

    @Test
    @DisplayName("Rule T2: no hardcoded tenant-scoped table allowlist")
    void noAllowlist() throws IOException {
        List<String> hits = scan(TENANT_ALLOWLIST);
        assertThat(hits)
                .describedAs("Tenant-scoped tables are derived from the schema by TenantScope. "
                        + "An allowlist silently unscopes every table it forgets. Offenders:\n  "
                        + String.join("\n  ", hits))
                .isEmpty();
    }

    @Test
    @DisplayName("Rule T3: no literal tenant id in SQL")
    void noLiteralTenantInSql() throws IOException {
        List<String> hits = scan(LITERAL_TENANT_SQL);
        assertThat(hits)
                .describedAs("A literal tenant id pins the deployment to one firm and is "
                        + "invisible until a second firm exists. Bind TenantContext.get() "
                        + "instead. Offenders:\n  " + String.join("\n  ", hits))
                .isEmpty();
    }

    /** Returns "file:line  text" for every match, skipping comments. */
    private List<String> scan(Pattern p) throws IOException {
        List<String> hits = new ArrayList<>();
        try (Stream<Path> files = Files.walk(SRC)) {
            for (Path f : files.filter(x -> x.toString().endsWith(".java")).toList()) {
                // TenantScope documents the banned patterns in its own javadoc.
                if (f.getFileName().toString().equals("TenantScope.java")) continue;
                List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    String trimmed = line.trim();
                    if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) continue;
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        hits.add(SRC.relativize(f) + ":" + (i + 1) + "  " + trimmed);
                    }
                }
            }
        }
        return hits;
    }
}
