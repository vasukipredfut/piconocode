-- Account Preference screen: background image upload (stored as a file
-- under earms.document-storage/backgrounds, same pattern as the profile
-- photo). Nullable — most users never set one, falls back to the app default.
ALTER TABLE user_ui_preference
  ADD COLUMN background_image VARCHAR(255) DEFAULT NULL AFTER font_size;
