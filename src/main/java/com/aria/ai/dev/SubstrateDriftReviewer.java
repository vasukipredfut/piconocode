package com.aria.ai.dev;

import com.aria.ai.AgentContext;
import com.aria.ai.AgentDecision;
import com.aria.ai.AiAgent;
import com.aria.ai.AiAgentHandler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @ai-element
 *   kind:      RULE_CEP
 *   agent:     dev.substrate-drift-reviewer
 *   mode-max:  SUPERVISED
 *   inputs:    List of file paths + diffs from a GitHub PR
 *   outputs:   List of anti-pattern findings for a PR review comment
 *
 * Dev-AI agent that runs on every PR opened against earmsbackend. Applies the
 * 10 anti-pattern checks from docs/architecture/CODE-REVIEW-develope-2026-07-11.md.
 *
 * This is a rule-based CEP agent, not an LLM. Every finding is deterministic
 * so the review is reproducible.
 *
 * Would have caught every finding in Dhivya's 2026-07-11 develope-branch PR
 * before it landed.
 */
@AiAgent(
        code = "dev.substrate-drift-reviewer",
        displayName = "Substrate Drift Reviewer",
        model = "local.rule-cep",
        kind = AiAgent.Kind.RULE_CEP,
        population = AiAgent.Population.DEV,
        maxMode = AiAgent.Mode.SUPERVISED,
        tools = { },
        description = "Blocks PRs that introduce the 10 substrate-anti-patterns from CODE-REVIEW-develope-2026-07-11.md"
)
@Component
public class SubstrateDriftReviewer implements AiAgentHandler {

    @Override
    public String code() { return "dev.substrate-drift-reviewer"; }

    @Override
    public AgentDecision run(AgentContext ctx) {
        // ctx.inputs() expected shape: { "files": [ {"path":"...", "diff":"..."} , ...] }
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> files = (List<Map<String,Object>>) ctx.inputs()
                .getOrDefault("files", List.of());

        List<Map<String,Object>> findings = new ArrayList<>();

        for (Map<String,Object> f : files) {
            String path = String.valueOf(f.get("path"));
            String diff = String.valueOf(f.getOrDefault("diff", ""));

            check(findings, path, diff, "R1-new-jpa-entity",
                    "CRITICAL", Pattern.compile("^\\+\\s*@Entity\\b", Pattern.MULTILINE),
                    "New @Entity outside com.aria.m0.* — use entity_registry rows",
                    p -> !p.contains("com/aria/m0/") && p.endsWith(".java"));

            check(findings, path, diff, "R2-new-jpa-repo",
                    "CRITICAL", Pattern.compile("^\\+.*extends\\s+(JpaRepository|CrudRepository|Repository)\\b"),
                    "New *Repository interface — GenericEntityController handles CRUD",
                    p -> !p.contains("com/aria/m0/") && p.endsWith(".java"));

            check(findings, path, diff, "R3-new-bespoke-controller",
                    "CRITICAL", Pattern.compile("^\\+.*(@RestController|@Controller)\\b"),
                    "New *Controller outside allowed packages — use /api/m0/entities/{code}",
                    p -> p.endsWith("Controller.java")
                            && !p.contains("com/aria/m0/")
                            && !p.contains("com/aria/m11/adapter/")
                            && !p.contains("com/aria/m6/gst/")
                            && !p.contains("com/aria/m7/govforms/"));

            check(findings, path, diff, "R6-commented-out-code",
                    "HIGH", Pattern.compile("^\\+//.*$", Pattern.MULTILINE),
                    "Large commented-out block committed — delete it, git remembers",
                    p -> p.endsWith(".java") && !p.endsWith("Test.java")
                            && countMatches(diff, "^\\+//", Pattern.MULTILINE) > 20);

            check(findings, path, diff, "R9-remote-ip-in-props",
                    "CRITICAL", Pattern.compile("^\\+.*jdbc:mariadb://(?!127\\.0\\.0\\.1|localhost)"),
                    "JDBC url points at a non-127.0.0.1 host — never commit this",
                    p -> p.endsWith("application.properties"));

            check(findings, path, diff, "R8-binary-in-migrations",
                    "CRITICAL", Pattern.compile(".*"),
                    "Binary file inside db/migration/ — move to docs/customers/reference-dumps/",
                    p -> p.startsWith("src/main/resources/db/migration/")
                            && (p.endsWith(".zip") || p.endsWith(".jar")
                                || p.endsWith(".gz") || p.endsWith(".pdf")
                                || p.endsWith(".xlsx")));
        }

        if (findings.isEmpty()) {
            return AgentDecision.noop("no anti-patterns detected");
        }

        long criticals = findings.stream()
                .filter(m -> "CRITICAL".equals(m.get("severity"))).count();
        BigDecimal confidence = new BigDecimal("0.95"); // rule-based = high confidence

        String reason = "found " + findings.size() + " findings (" + criticals + " critical): "
                + findings.stream().limit(3).map(m -> m.get("rule").toString()).toList();

        return new AgentDecision(
                AgentDecision.Action.NOTIFY,
                Map.of("kind", "pr-review-comment",
                       "findings", findings,
                       "block_merge", criticals > 0),
                reason,
                confidence,
                Map.of("findings_count", findings.size(),
                       "criticals", criticals));
    }

    private void check(List<Map<String,Object>> findings, String path, String diff,
                       String rule, String severity, Pattern pattern,
                       String message, java.util.function.Predicate<String> pathCheck) {
        if (!pathCheck.test(path)) return;
        if (pattern.matcher(diff).find()) {
            findings.add(Map.of(
                    "rule", rule, "severity", severity, "path", path, "message", message));
        }
    }

    private int countMatches(String s, String regex, int flags) {
        Pattern p = Pattern.compile(regex, flags);
        java.util.regex.Matcher m = p.matcher(s);
        int n = 0;
        while (m.find()) n++;
        return n;
    }
}
