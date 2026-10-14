package com.aria.workflow.engine;

import com.aria.security.TenantContext;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * After every page submission, records WHO submitted it (the logged-in user's id, taken from the JWT principal,
 * never from the form) on that page's own table, for this workflow instance.
 *
 * <p>Which table(s) a page writes to comes from {@code page_submitter_map} (page_code -> table / key column /
 * user column) so a new page is wired up by inserting a row (plus the column) -- no code change. The engine calls
 * this once, AFTER all transition listeners ran, so the page's row is guaranteed to exist already.
 *
 * <p>{@code aria:weighment-capture} is submitted up to three times per instance (first / second / third weight,
 * often by different officers): its map row's user column is the pattern {@code w#_by_id}, with {@code #} replaced by
 * 1/2/3 from the submitted {@code weight_seq} (FIRST/SECOND/THIRD), falling back to the state the vehicle was in.
 * A resubmission of the same page overwrites with the latest submitter.
 *
 * <p>Failures are logged, never propagated -- a stamp can never fail a transition.
 */
@Component
public class PageSubmitterStamper {

    private static final Logger log = LoggerFactory.getLogger(PageSubmitterStamper.class);
    private static final Pattern SAFE_IDENT = Pattern.compile("^[A-Za-z0-9_]+$");
    private static final Set<String> FIRST_WEIGHT_FROM = Set.of("QC_PRELOAD_APPROVED", "RELOAD_REQUIRED", "QC_APPROVED", "SUPERVISOR_ALLOCATED", "DRAFT");
    private static final Set<String> SECOND_WEIGHT_FROM = Set.of("QC_SAMPLE_APPROVED", "UNLOADING_WEIGHMENT", "RELOAD_LOADING", "WEIGHMENT_1", "UNLOADING_DONE", "LOADING_UNLOADING_IN_PROGRESS");
    private static final Set<String> THIRD_WEIGHT_FROM = Set.of("DISPATCH_VERIFIED", "BILLING_DONE", "QC_FINAL_CHECKED", "QTY_RECONCILED");

    private final JdbcTemplate jdbc;

    public PageSubmitterStamper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void stamp(WorkflowInstance inst, String previousState, String pageCode, Long userId, Map<String, Object> formData) {
        if (inst == null || pageCode == null || userId == null) return;
        try {
            List<Map<String, Object>> targets = jdbc.queryForList(
                    "SELECT table_name, key_column, user_column, name_column, contact_column FROM page_submitter_map WHERE page_code = ?", pageCode);
            for (Map<String, Object> t : targets) {
                String table = (String) t.get("table_name");
                String keyColumn = (String) t.get("key_column");
                String userColumn = (String) t.get("user_column");
                String nameColumn = (String) t.get("name_column");
                String contactColumn = (String) t.get("contact_column");
                boolean weighment = contactColumn != null;
                String seq = null;
                if ((userColumn != null && userColumn.contains("#")) || (nameColumn != null && nameColumn.contains("#"))
                        || (contactColumn != null && contactColumn.contains("#"))) {
                    seq = weightSeq(formData, previousState);
                    if (seq == null) {
                        log.debug("page submitter stamp: {} has no weight sequence for instance={} -- skipped", pageCode, inst.getId());
                        continue;
                    }
                }
                userColumn = fill(userColumn, seq);
                nameColumn = fill(nameColumn, seq);
                contactColumn = fill(contactColumn, seq);
                if (!safe(table) || !safe(keyColumn) || !safe(userColumn)
                        || (nameColumn != null && !safe(nameColumn)) || (contactColumn != null && !safe(contactColumn))) {
                    log.warn("page submitter stamp: unsafe identifier in page_submitter_map for {}", pageCode);
                    continue;
                }

                // login user (JWT) always; the person the form names (editable field) when the page sent one.
                // The weighment page's existing 'Weighment Officer Name' is that person when no performed_by_name came.
                String performed = text(formData, "performed_by_name", "performedByName");
                if (performed == null && weighment) performed = text(formData, "weight_officer", "weightOfficer");
                String contact = weighment ? text(formData, "weight_officer_contact", "weightOfficerContact") : null;

                StringBuilder set = new StringBuilder(userColumn).append(" = ?");
                List<Object> args = new java.util.ArrayList<>();
                args.add(userId);
                if (performed != null && nameColumn != null) { set.append(", ").append(nameColumn).append(" = ?"); args.add(performed); }
                if (contact != null) { set.append(", ").append(contactColumn).append(" = ?"); args.add(contact); }
                args.add(TenantContext.get());
                args.add(inst.getId());
                int n = jdbc.update("UPDATE " + table + " SET " + set + " WHERE tenant_id = ? AND " + keyColumn + " = ?", args.toArray());
                if (n == 0) {
                    log.debug("page submitter stamp: {}.{} has no row yet for instance={}", table, userColumn, inst.getId());
                }
            }
        } catch (Exception e) {
            log.warn("page submitter stamp failed for instance={} page={}: {}", inst.getId(), pageCode, e.getMessage());
        }
    }

    private static String fill(String column, String seq) {
        return column == null ? null : (seq == null ? column : column.replace("#", seq));
    }

    private static String text(Map<String, Object> m, String... keys) {
        if (m == null) return null;
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null && !v.toString().isBlank()) {
                String s = v.toString().trim();
                return s.length() > 128 ? s.substring(0, 128) : s;
            }
        }
        return null;
    }

    /** "1" / "2" / "3" from the submitted weight_seq, else from the state the vehicle was in; null when unknown. */
    private static String weightSeq(Map<String, Object> formData, String previousState) {
        Object seq = formData == null ? null : formData.get("weight_seq");
        if (seq != null) {
            switch (seq.toString().trim().toUpperCase()) {
                case "FIRST": return "1";
                case "SECOND": return "2";
                case "THIRD": return "3";
                default: break;
            }
        }
        if (previousState == null) return null;
        if (FIRST_WEIGHT_FROM.contains(previousState)) return "1";
        if (SECOND_WEIGHT_FROM.contains(previousState)) return "2";
        if (THIRD_WEIGHT_FROM.contains(previousState)) return "3";
        return null;
    }

    private static boolean safe(String ident) {
        return ident != null && SAFE_IDENT.matcher(ident).matches();
    }
}
