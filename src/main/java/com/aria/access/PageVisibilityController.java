package com.aria.access;

import com.aria.security.TenantContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Self-service page-visibility lookup — "which pages can the currently
 * logged-in user see in navigation". Resolves the caller's role from the JWT
 * authority (a numeric role id, per {@code com.aria.auth.AuthUserDetails})
 * and applies the same allow-list rule as
 * {@code EmployeeAccessMatrixController#roleVisibility}: a role with zero
 * rows in {@code role_page_visibility} is unrestricted (sees every
 * published page — today's behaviour, unchanged); a role with any rows
 * there is restricted to exactly those pages. SuperAdmin always sees
 * everything, same convention as the rest of this package.
 *
 * <p>Open to any authenticated user (unlike EmployeeAccessMatrixController,
 * which is Admin-only) — every user needs to know their own visible pages.
 */
@RestController
@RequestMapping("/api/m0/nav")
public class PageVisibilityController {

    private static final String SUPER_ROLE = "SuperAdmin";

    private final JdbcTemplate jdbc;

    public PageVisibilityController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/visible-pages")
    public ResponseEntity<?> visiblePages() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false, "error", "Not authenticated"));
        }

        Long roleId = null;
        String roleName = null;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String s = ga.getAuthority();
            if (s == null) continue;
            try {
                roleId = Long.parseLong(s.trim());
                roleName = loadRoleName(roleId);
            } catch (NumberFormatException nfe) {
                roleName = s;
                roleId = loadRoleId(s);
            }
            break;
        }

        List<Map<String, Object>> pages = jdbc.queryForList(
                "SELECT code, title, category FROM page_definition WHERE tenant_id IN (?, -1) AND status = 'PUBLISHED'",
                TenantContext.get());

        boolean unrestricted = roleId == null || SUPER_ROLE.equalsIgnoreCase(roleName);
        Set<String> allowed = Set.of();
        if (!unrestricted) {
            allowed = new HashSet<>(jdbc.queryForList(
                    "SELECT page_code FROM role_page_visibility WHERE tenant_id IN (?, -1) AND role_id = ?",
                    String.class, TenantContext.get(), roleId));
            if (allowed.isEmpty()) unrestricted = true;
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> page : pages) {
            String pageCode = (String) page.get("code");
            if (!unrestricted && !allowed.contains(pageCode)) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("pageCode", pageCode);
            row.put("title", page.get("title"));
            row.put("category", page.get("category"));
            out.add(row);
        }
        out.sort(Comparator
                .comparing((Map<String, Object> s) -> (String) s.get("category"), Comparator.nullsLast(String::compareTo))
                .thenComparing(s -> (String) s.get("title"), Comparator.nullsLast(String::compareTo)));

        return ResponseEntity.ok(Map.of("ok", true, "roleId", roleId, "roleName", roleName,
                "restricted", !unrestricted, "pages", out));
    }

    private String loadRoleName(Long id) {
        try {
            return jdbc.queryForObject("SELECT name FROM role WHERE id=?", String.class, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Long loadRoleId(String name) {
        try {
            return jdbc.queryForObject("SELECT id FROM role WHERE name=?", Long.class, name);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
