package com.aria.account;

import com.aria.auth.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private final PasswordEncoder passwordEncoder;

    public AccountController(JdbcTemplate jdbc,
                              @Value("${earms.document-storage:/tmp/earms-docs}") String storageDir,
                              PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.storageDir = storageDir;
        this.passwordEncoder = passwordEncoder;
        new File(storageDir, "avatars").mkdirs();
        new File(storageDir, "backgrounds").mkdirs();
    }

    public static class ProfileUpdateRequest {
        public String employeename;
        public String email;
        public String phoneno;
    }

    public static class ChangePasswordRequest {
        public String oldPassword;
        public String newPassword;
    }

    /**
     * Bound via {@code @ModelAttribute} from the multipart request — Spring
     * fills {@code file} from the file part and the rest from form fields.
     * Every field is optional; {@link #updatePreferences} only overwrites
     * whichever ones actually arrived. Add new preference fields here (plus
     * the matching DB column + COALESCE clause) rather than growing a long
     * {@code @RequestParam} method signature.
     */
    public static class PreferencesUpdateRequest {
        public String themeMode;
        public String primaryColor;
        public String secondaryColor;
        public String appColor;
        public String buttonStyle;
        public String fontSize;
        public MultipartFile file;
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

    /**
     * Self-service password change: the current password must match before
     * the new one is written. Uses the same {@code employee_details.password}
     * / {@code PasswordEncoder} bean as login (see
     * {@code com.aria.entity.GenericEntityController#applyDefaultPasswordIfNeeded}
     * for the create-time counterpart of this) — the new value is always
     * hashed, never stored in plain text.
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change the logged-in user's own password (old password required to verify identity).")
    public Map<String, Object> changePassword(@RequestBody ChangePasswordRequest req) {
        Long userId = currentUserId();
        if (req.newPassword == null || req.newPassword.isBlank()) {
            return Map.of("ok", false, "error", "New password is required");
        }
        String currentHash;
        try {
            currentHash = jdbc.queryForObject(
                    "SELECT password FROM employee_details WHERE id = ?", String.class, userId);
        } catch (EmptyResultDataAccessException e) {
            return Map.of("ok", false, "error", "User not found");
        }
        if (currentHash == null || !passwordEncoder.matches(
                req.oldPassword == null ? "" : req.oldPassword, currentHash)) {
            return Map.of("ok", false, "error", "Old password is incorrect");
        }
        jdbc.update("UPDATE employee_details SET password = ? WHERE id = ?",
                passwordEncoder.encode(req.newPassword), userId);
        return Map.of("ok", true);
    }

    // ---- Preferences -------------------------------------------------------

    @GetMapping("/preferences")
    @Operation(summary = "Load the logged-in user's UI preferences (defaults if never saved).")
    public Map<String, Object> getPreferences() {
        Long userId = currentUserId();
        try {
            return jdbc.queryForMap(
                    "SELECT theme_mode, primary_color, secondary_color, app_color, button_style, font_size, background_image " +
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
            defaults.put("background_image", null);
            return defaults;
        }
    }

    /**
     * Single call for the whole Preferences form: theme/color/style fields
     * plus an optional new background image, all in one multipart request
     * (a background image can only travel as a file part, so this can't be
     * plain JSON). {@code file} is optional — omit it to save the other
     * fields without touching whatever background image is already set.
     */
    @PutMapping(value = "/preferences", consumes = "multipart/form-data")
    @Operation(summary = "Save (create or update) the logged-in user's UI preferences, optionally replacing the background image.")
    public Map<String, Object> updatePreferences(@ModelAttribute PreferencesUpdateRequest req) throws IOException {
        Long userId = currentUserId();

        String backgroundUrl = null;
        if (req.file != null && !req.file.isEmpty()) {
            String safeName = (req.file.getOriginalFilename() == null ? "background.bin"
                    : req.file.getOriginalFilename().replaceAll("[^A-Za-z0-9._-]", "_"));
            String storedName = "user-" + userId + "-" + System.currentTimeMillis() + "-" + safeName;
            File target = new File(new File(storageDir, "backgrounds"), storedName);
            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(req.file.getBytes());
            }
            backgroundUrl = "/uploads/backgrounds/" + storedName;
        }

        // COALESCE(?, existing/default) on every field — so a request that only
        // carries a subset (e.g. just the background image file, or just the
        // theme) leaves the rest of an existing row untouched instead of
        // nulling out NOT NULL columns (theme_mode/button_style/font_size).
        jdbc.update(
                "INSERT INTO user_ui_preference " +
                "(user_id, theme_mode, primary_color, secondary_color, app_color, button_style, font_size, background_image) " +
                "VALUES (?, COALESCE(?, 'LIGHT'), ?, ?, ?, COALESCE(?, 'PILL'), COALESCE(?, 'MEDIUM'), ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "theme_mode = COALESCE(?, theme_mode), " +
                "primary_color = COALESCE(?, primary_color), " +
                "secondary_color = COALESCE(?, secondary_color), " +
                "app_color = COALESCE(?, app_color), " +
                "button_style = COALESCE(?, button_style), " +
                "font_size = COALESCE(?, font_size), " +
                "background_image = COALESCE(?, background_image)",
                userId, req.themeMode, req.primaryColor, req.secondaryColor, req.appColor, req.buttonStyle, req.fontSize, backgroundUrl,
                req.themeMode, req.primaryColor, req.secondaryColor, req.appColor, req.buttonStyle, req.fontSize, backgroundUrl);
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
