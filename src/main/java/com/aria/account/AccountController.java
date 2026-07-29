package com.aria.account;

import com.aria.auth.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Account Preference screen: Profile Information (backed by the existing
 * employee_details row for the logged-in user) + User Preferences (theme,
 * colors, button style, font size — new table, user_ui_preference, V142).
 * Both halves are self-service: every endpoint acts on the current JWT's
 * own user id, never a path/query-supplied one.
 */
@RestController
@RequestMapping("/api/account")
@Tag(name = "Account Preferences")
public class AccountController {

    private final JdbcTemplate jdbc;
    private final String storageDir;

    public AccountController(JdbcTemplate jdbc,
                              @Value("${earms.document-storage:/tmp/earms-docs}") String storageDir) {
        this.jdbc = jdbc;
        this.storageDir = storageDir;
        new File(storageDir, "avatars").mkdirs();
    }

    public static class ProfileUpdateRequest {
        public String employeename;
        public String email;
        public String phoneno;
    }

    public static class PreferencesUpdateRequest {
        public String themeMode;
        public String primaryColor;
        public String secondaryColor;
        public String appColor;
        public String buttonStyle;
        public String fontSize;
    }

    // ---- Profile ---------------------------------------------------------

    @GetMapping("/profile")
    @Operation(summary = "Load the logged-in user's profile.")
    public Map<String, Object> getProfile() {
        Long userId = currentUserId();
        return jdbc.queryForMap(
                "SELECT e.id, e.username, e.employeename, e.email, e.phoneno, e.photo, r.name AS role_name " +
                "FROM employee_details e LEFT JOIN role r ON r.id = e.roles_id " +
                "WHERE e.id = ?",
                userId);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update the logged-in user's editable profile fields (name/email/phone).")
    public Map<String, Object> updateProfile(@RequestBody ProfileUpdateRequest req) {
        Long userId = currentUserId();
        jdbc.update(
                "UPDATE employee_details SET employeename = ?, email = ?, phoneno = ? WHERE id = ?",
                req.employeename, req.email, req.phoneno, userId);
        return getProfile();
    }

    @PostMapping(value = "/profile/photo", consumes = "multipart/form-data")
    @Operation(summary = "Upload/replace the logged-in user's profile photo.")
    public Map<String, Object> uploadPhoto(@RequestParam("file") MultipartFile file) {
        Long userId = currentUserId();
        try {
            if (file == null || file.isEmpty()) return Map.of("ok", false, "error", "no file");

            String safeName = (file.getOriginalFilename() == null ? "photo.bin"
                    : file.getOriginalFilename().replaceAll("[^A-Za-z0-9._-]", "_"));
            String storedName = "user-" + userId + "-" + System.currentTimeMillis() + "-" + safeName;
            File target = new File(new File(storageDir, "avatars"), storedName);
            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(file.getBytes());
            }

            String url = "/uploads/avatars/" + storedName;
            jdbc.update("UPDATE employee_details SET photo = ? WHERE id = ?", url, userId);
            return Map.of("ok", true, "url", url);
        } catch (Exception e) {
            return Map.of("ok", false, "error", e.getMessage());
        }
    }

    // ---- Preferences -------------------------------------------------------

    @GetMapping("/preferences")
    @Operation(summary = "Load the logged-in user's UI preferences (defaults if never saved).")
    public Map<String, Object> getPreferences() {
        Long userId = currentUserId();
        try {
            return jdbc.queryForMap(
                    "SELECT theme_mode, primary_color, secondary_color, app_color, button_style, font_size " +
                    "FROM user_ui_preference WHERE user_id = ?",
                    userId);
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> defaults = new HashMap<>();
            defaults.put("theme_mode", "LIGHT");
            defaults.put("primary_color", null);
            defaults.put("secondary_color", null);
            defaults.put("app_color", null);
            defaults.put("button_style", "PILL");
            defaults.put("font_size", "MEDIUM");
            return defaults;
        }
    }

    @PutMapping("/preferences")
    @Operation(summary = "Save (create or update) the logged-in user's UI preferences.")
    public Map<String, Object> updatePreferences(@RequestBody PreferencesUpdateRequest req) {
        Long userId = currentUserId();
        jdbc.update(
                "INSERT INTO user_ui_preference " +
                "(user_id, theme_mode, primary_color, secondary_color, app_color, button_style, font_size) " +
                "VALUES (?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE theme_mode=VALUES(theme_mode), primary_color=VALUES(primary_color), " +
                "secondary_color=VALUES(secondary_color), app_color=VALUES(app_color), " +
                "button_style=VALUES(button_style), font_size=VALUES(font_size)",
                userId, req.themeMode, req.primaryColor, req.secondaryColor, req.appColor,
                req.buttonStyle, req.fontSize);
        return getPreferences();
    }

    // ---- Helpers -----------------------------------------------------------

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthUserDetails udi) return udi.getId();
        return null;
    }
}
