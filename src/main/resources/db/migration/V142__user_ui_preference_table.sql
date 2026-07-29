-- Per direct request: Account Preference screen (Profile Information + User
-- Preferences — theme, colors, button style, font size). Profile fields
-- (Full Name/Email/Phone) already exist on employee_details
-- (employeename/email/phoneno) — no new table needed for that half. The
-- theme/color/style half has no home anywhere yet, so it gets its own
-- one-row-per-user table.
CREATE TABLE IF NOT EXISTS user_ui_preference (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  theme_mode VARCHAR(16) NOT NULL DEFAULT 'LIGHT',
  primary_color VARCHAR(16) DEFAULT NULL,
  secondary_color VARCHAR(16) DEFAULT NULL,
  app_color VARCHAR(16) DEFAULT NULL,
  button_style VARCHAR(16) NOT NULL DEFAULT 'PILL',
  font_size VARCHAR(16) NOT NULL DEFAULT 'MEDIUM',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_user_ui_pref_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
