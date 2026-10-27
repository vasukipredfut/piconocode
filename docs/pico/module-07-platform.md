# Module 7: Reports, Branding, Profile, Notifications, Password reset, WhatsApp

Requires Modules 1–6 (`TenantMailService.sendNow` from Module 6's version of the Module 2 file).

| File | Goes to (Aria repo) |
|---|---|
| `V388__pico_reports_branding_notifications.sql` | `src/main/resources/db/migration/` (renumber if V388 is taken) |
| `java/com/aria/action/handler/PicoMyProfileActionHandler.java` | `src/main/java/com/aria/action/handler/` |
| `java/com/aria/action/handler/PicoMyNotificationsActionHandler.java` | same |
| `java/com/aria/action/handler/PicoWhatsappAttendanceReportActionHandler.java` | same |
| `java/com/aria/account/PasswordResetController.java` | `src/main/java/com/aria/account/` |
| `java/com/aria/integration/adapter/TwilioWhatsappAdapter.java` | `src/main/java/com/aria/integration/adapter/` |
| `java/com/aria/hr/attendance/PicoAttendanceDigest.java` | `src/main/java/com/aria/hr/attendance/` |
| `java/com/aria/utils/UploadGuard.java` | `src/main/java/com/aria/utils/` |
| `uploads-hardening.patch` | apply to `AccountController`, `DocumentUploadController`, `CorsConfiguration` |

## Where each ERPNext piece went

| ERPNext | Aria |
|---|---|
| `custom_theme` reports: Customer, Employee Checkin, Expense Claim, Installation | already list pages: `pico:customer-list`, `pico:checkin-list`, `pico:expense-claim-list`, `pico:installation-report-list` (Modules 1, 4, 5, 6) |
| Lead Report, Leave Application Report | new `pico:lead-report`, `pico:leave-report` |
| Theme Preference + My Preferences page | **already in Aria**: `GET/PUT /api/account/preferences` (`user_ui_preference`: theme mode, primary / secondary / app colour, button style, font size) |
| Company Branding (logo) | tenant settings `company_name` (existing) + `branding.logo_url` (new) → `pico.my-profile {"op":"branding"}` |
| `user_profile`, `update_user_profile` | `pico.my-profile` (get / update) + existing `POST /api/account/profile/photo` |
| `login`, `validate_token`, `refresh_token` | Aria JWT: `POST /api/auth/signin`, `/api/auth/refreshToken` (no change) |
| `send_reset_otp`, `verify_reset_otp`, `reset_password` | `POST /api/auth/password-reset/request` and `/confirm` |
| `get_notifications` ("Notifications" DocType) | `m7_announcement` (`pico:announcement-list`) + `pico.my-notifications` (also pending-approval counts) |
| `attendance_report.send_attendance_report_to_whatsapp` | `TWILIO_WHATSAPP` connector + `PicoAttendanceDigest` (00:30 daily, yesterday) + `pico.whatsapp-attendance-report` |
| `user.py` (users restricted to their company) | Aria tenants already isolate companies |
| desk CSS / JS, sidebar / workspace patches | dropped (Aria's own frontend) |
| `api/fuel_attendant.py` ("Fuel Attendar") | **not migrated**: not wired in hooks, its DocType is not in the repo; looks unrelated to PICO. Confirm with the client |

## Security fixes in this module

1. **Password reset.**
   - **ERPNext:** a 4-digit code with unlimited tries, then `reset_password` trusted a per-email "verified" flag. Brute-force the code and you can reset anyone's password.
   - **Aria:**
     - 6 digits, hashed, 15-minute expiry, 5 tries.
     - The password is set **in the same call** that proves the code.
     - The same reply whether or not the account exists, and the mail is sent off the request thread (no timing difference).
     - Max 3 requests per account per hour and 10 calls per IP per 10 minutes.
2. **Profile update.** ERPNext let employees overwrite their own designation, gender and DOB. Aria allows only contact / emergency fields and the user's **own** uploaded avatar URL.
3. **Uploads (existing Aria bug):** `/uploads/**` is public and served from the app's origin, while the photo and document-vault endpoints stored **any** file type. An uploaded `.html` / `.svg` runs as the app (stored XSS). This also affects Module 1 selfies and Module 4 receipts. `UploadGuard` allows only JPG / PNG / WEBP (+ PDF for the vault) by magic bytes and renames the stored file; the patch also adds `nosniff`.
4. **WhatsApp.**
   - ERPNext used the Twilio sandbox and a recipient number in source.
   - One variant published a PDF of everyone's attendance as a **public** file.
   - Aria sends text only, to configured numbers, with Twilio credentials in the integration secret store.

## Mobile app: old → new calls

| ERPNext `pico_api.*` | Aria |
|---|---|
| `send_reset_otp` | `POST /api/auth/password-reset/request` `{"username":".."}` |
| `verify_reset_otp` + `reset_password` | `POST /api/auth/password-reset/confirm` `{"username":"..","otp":"123456","new_password":".."}` (one call) |
| `user_profile` | `pico.my-profile` `{"op":"get"}` |
| `update_user_profile` | photo: `POST /api/account/profile/photo` (multipart `file`) → `url`; then `pico.my-profile` `{"op":"update","mobile_primary":"..","emergency_contact_name":"..","photo_url":"<url>"}` |
| `company_branding` | `pico.my-profile` `{"op":"branding"}` |
| `get_notifications` | `pico.my-notifications` `{}`; one notice: `{"announcement_id":3}` |
| *(theme)* | `GET` / `PUT /api/account/preferences` |

## Setup after deploy
1. Upload the company logo (document vault) and set `branding.logo_url` in `m0_tenant_setting`.
2. WhatsApp (optional):
   - Create a `TWILIO_WHATSAPP` integration instance with config `{"accountSid":"AC…","from":"whatsapp:+…"}` and secret `authToken`, then set it ACTIVE. The Twilio sandbox only reaches numbers that joined it; production needs an approved WhatsApp sender.
   - Set `whatsapp.attendance_report_enabled=true` and `whatsapp.attendance_report_to=+9198…,+9197…`.
3. Load ERPNext "Notifications" rows into `m7_announcement` (`erp_ref`).
4. The Twilio credentials ERPNext read from `site_config` (`twilio_sid` / `twilio_token`): rotate them if that site config was ever shared.

## Verified
- All 12 JSON literals in the SQL parse.
- Modules 1–7 compile together against Aria's `target/classes`. Modules 1–6 still compile alone.
- `UploadGuard.sniff` accepts JPG / PNG / WEBP / PDF and rejects HTML and SVG.
- `storedName` strips path characters.
- The OTP hash binds the row id.
- The email / mobile masks behave as expected (ad-hoc checks run in the scratchpad).

## Not verified
- The SQL has not been run.
- The password-reset endpoints, Twilio sending, the 00:30 digest and the upload patch have not run against a live backend.
