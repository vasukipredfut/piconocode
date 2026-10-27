package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.CurrentUser;
import com.aria.security.TenantContext;
import com.aria.tenant.TenantSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * pico.my-profile -- replaces ERPNext user_profile, update_user_profile and company_branding.
 * <ul>
 *   <li>{@code op=get}      -- login account (employee_details) + employee master (m7_employee) + role</li>
 *   <li>{@code op=update}   -- the caller's own contact / emergency fields and photo_url. HR-owned
 *       fields (designation, gender, date of birth, bank, PAN) are not editable here -- ERPNext let
 *       an employee overwrite their own designation through this API.</li>
 *   <li>{@code op=branding} -- tenant company_name + branding.logo_url (ERPNext Company Branding)</li>
 * </ul>
 * The photo itself is uploaded with POST /api/account/profile/photo (apply UploadGuard first, see
 * module README); pass the returned url as {@code photo_url}. Theme: /api/account/preferences.
 */
@Component
public class PicoMyProfileActionHandler implements ActionHandler {

    public static final String CODE = "pico.my-profile";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private TenantSettings settings;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> a = inv.getArgs() == null ? Map.of() : inv.getArgs();
        String op = PicoSelf.str(a.get("op"));
        if ("branding".equals(op)) {
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("company", settings.get("company_name", null));
            b.put("tagline", settings.get("company_tagline", null));
            b.put("logo_url", settings.get("branding.logo_url", null));
            return ActionResult.builder().ok(true).data(b).build();
        }
        Long userId = CurrentUser.id();
        if (userId == null) return ActionResult.error("Authentication required");
        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if ("update".equals(op)) {
            if (emp == null) return ActionResult.error("Employee not found");
            List<String> sets = new ArrayList<>();
            List<Object> params = new ArrayList<>();
            field(a, "mobile_primary", 16, "\\+?[0-9 ]{7,16}", sets, params);
            field(a, "email_personal", 96, "[^@\\s]+@[^@\\s]+\\.[^@\\s]+", sets, params);
            field(a, "emergency_contact_name", 96, null, sets, params);
            field(a, "emergency_contact_phone", 16, "\\+?[0-9 ]{7,16}", sets, params);
            field(a, "emergency_contact_relation", 32, null, sets, params);
            String photo = PicoSelf.str(a.get("photo_url"));
            if (photo != null && !photo.isEmpty()) {
                // Only this user's own avatar upload: no external URLs, no other people's files, no path tricks.
                if (!photo.matches("/uploads/avatars/user-" + userId + "-[A-Za-z0-9._-]+\\.(jpg|jpeg|png|webp)")) {
                    throw new IllegalArgumentException("photo_url must be an image uploaded through /api/account/profile/photo");
                }
                sets.add("photo_url=?");
                params.add(photo);
                jdbc.update("UPDATE employee_details SET photo=? WHERE id=?", photo, userId);
            }
            if (sets.isEmpty()) throw new IllegalArgumentException("Nothing to update");
            params.add(TenantContext.get());
            params.add(emp.get("id"));
            jdbc.update("UPDATE m7_employee SET " + String.join(", ", sets) + " WHERE tenant_id=? AND id=?", params.toArray());
            if (a.get("mobile_primary") != null) {
                jdbc.update("UPDATE employee_details SET phoneno=? WHERE id=?", PicoSelf.str(a.get("mobile_primary")), userId);
            }
        } else if (op != null && !"get".equals(op)) {
            throw new IllegalArgumentException("op must be get, update or branding");
        }

        Map<String, Object> login = jdbc.queryForMap(
                "SELECT u.id, u.username, u.employeename, u.email, u.phoneno, u.photo, r.name AS role_name " +
                        "FROM employee_details u LEFT JOIN role r ON r.id = u.roles_id WHERE u.id=?", userId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("login", login);
        if (emp != null) {
            data.put("employee", jdbc.queryForMap(
                    "SELECT e.id, e.emp_code, e.full_name, e.gender, e.dob, e.designation, e.department, e.work_location, " +
                            "e.date_of_joining, e.mobile_primary, e.email_official, e.email_personal, e.photo_url, " +
                            "e.emergency_contact_name, e.emergency_contact_phone, e.emergency_contact_relation, " +
                            "m.full_name AS reporting_manager FROM m7_employee e " +
                            "LEFT JOIN m7_employee m ON m.tenant_id = e.tenant_id AND m.id = e.reports_to_emp_id " +
                            "WHERE e.tenant_id=? AND e.id=?", TenantContext.get(), emp.get("id")));
        }
        return ActionResult.builder().ok(true).message("update".equals(op) ? "Profile updated successfully" : null).data(data).build();
    }

    private static void field(Map<String, Object> a, String key, int max, String pattern, List<String> sets, List<Object> params) {
        if (!a.containsKey(key)) return;
        String v = PicoSelf.str(a.get(key));
        if (v != null && v.isEmpty()) v = null;
        if (v != null && v.length() > max) throw new IllegalArgumentException(key + " must be at most " + max + " characters");
        if (v != null && pattern != null && !v.matches(pattern)) throw new IllegalArgumentException(key + " is not valid");
        sets.add(key + "=?");
        params.add(v);
    }
}
