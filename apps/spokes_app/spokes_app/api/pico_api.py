import frappe
import random
import requests
from frappe import _
import uuid
from datetime import datetime, timedelta
from frappe.utils import now_datetime, nowdate,getdate,get_datetime,flt
import calendar
from frappe.utils.password import update_password
import base64
import mimetypes
from frappe.utils.file_manager import save_file,get_file
from frappe.utils import cint
from frappe.email import sendmail
from frappe.utils.pdf import get_pdf
from io import BytesIO
from PIL import Image, UnidentifiedImageError

@frappe.whitelist(allow_guest=True)
def login():
    """
    User Login API
    Uses Administrator token for access,
    validates user credentials,
    returns user details + roles + role profile
    """
    try:
        email = frappe.form_dict.get("email")
        password = frappe.form_dict.get("password")

        if not email or not password:
            frappe.local.response["http_status_code"] = 400
            return {
                "status": "error",
                "message": "Email and password are required"
            }

        # Authenticate user
        login_manager = frappe.auth.LoginManager()
        login_manager.authenticate(user=email, pwd=password)
        login_manager.post_login()

        user = frappe.get_doc("User", email)

        token = str(uuid.uuid4())
        expiry = datetime.now() + timedelta(hours=8)


        frappe.db.set_value("User",email,{"api_token": token,"token_expiry": expiry},update_modified=False)

        # Fetch Employee Details using user_id
        employee = frappe.db.get_value(
            "Employee",
            {"user_id": email},
            ["name", "employee_number", "company"],
            as_dict=True
        )

        frappe.db.commit()


        return {
            "status": "success",
            "message": "Login successful",
            "token": token,
            "expires_at": expiry,
            "full_name": user.full_name,
            "email": user.name,
            "username": user.username,
            "role_profile_name": user.role_profile_name,
#            "roles": frappe.get_roles(user.name),

            "employee_id": employee.name if employee else None,
            "employee_number": employee.employee_number if employee else None,
            "company": employee.company if employee else None
            #"home_page": user.home_page or "/app"
        }

    except frappe.AuthenticationError:
        frappe.local.response["http_status_code"] = 401
        return {
            "status": "error",
            "message": "Invalid credentials"
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Pico Login API Error")
        frappe.local.response["http_status_code"] = 500
        return {
            "status": "error",
            "message": str(e)
        }


from datetime import datetime

def validate_token():
    token = frappe.get_request_header("X-Auth-Token")

    if not token:
        frappe.throw("Token missing", frappe.AuthenticationError)

    user = frappe.db.get_value(
        "User",
        {"api_token": token},
        ["name", "token_expiry"],
        as_dict=True
    )

    if not user:
        frappe.throw("Invalid token", frappe.AuthenticationError)

    from frappe.utils import now_datetime

    if user.token_expiry < now_datetime():
        frappe.throw("Token expired", frappe.AuthenticationError)

    return user.name


@frappe.whitelist(allow_guest=True)
def send_reset_otp():
    import random

    try:
        email = frappe.form_dict.get("email")

        if not email:
            return {"status": "error", "message": "Email is required"}

        if not frappe.db.exists("User", email):
            return {"status": "error", "message": "User not found"}

        # ✅ 4-digit OTP
        otp = str(random.randint(1000, 9999))

        expiry = now_datetime() + timedelta(minutes=10)

        # ✅ Direct DB update (NO modified change in UI)
        frappe.db.sql("""
            UPDATE `tabUser`
            SET verify_otp=%s, otp_expiry=%s, otp_verified=0
            WHERE name=%s
        """, (otp, expiry, email))

        frappe.db.commit()

        # 📧 Send mail
        frappe.sendmail(
            recipients=email,
            subject="Password Reset OTP",
            message=f"Your OTP is: <b>{otp}</b><br>Valid for 10 minutes.",
            now=True 
        )

        return {
            "status": "success",
            "message": "OTP sent successfully"
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Send OTP Error")
        return {"status": "error", "message": str(e)}

@frappe.whitelist(allow_guest=True)
def verify_reset_otp():
    try:
        email = frappe.form_dict.get("email")
        otp = frappe.form_dict.get("otp")

        if not email or not otp:
            return {"status": "error", "message": "Email and OTP required"}

        user = frappe.db.get_value(
            "User",
            email,
            ["verify_otp", "otp_expiry"],
            as_dict=True
        )

        if not user:
            return {"status": "error", "message": "User not found"}

        # ❌ OTP mismatch
        if str(user.verify_otp) != str(otp):
            return {"status": "error", "message": "Invalid OTP"}

        # ⏰ Expiry check
        if user.otp_expiry and frappe.utils.now_datetime() > user.otp_expiry:
            return {"status": "error", "message": "OTP expired"}

        # ✅ Mark verified (no modified change)
        frappe.db.sql("""
            UPDATE `tabUser`
            SET otp_verified=1
            WHERE name=%s
        """, (email,))

        frappe.db.commit()

        return {
            "status": "success",
            "message": "OTP verified"
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Verify OTP Error")
        return {"status": "error", "message": str(e)}

@frappe.whitelist(allow_guest=True)
def reset_password():
    try:
        email = frappe.form_dict.get("email")
        new_password = frappe.form_dict.get("new_password")

        if not email or not new_password:
            frappe.local.response["http_status_code"] = 400
            return {
                "status": "error",
                "message": "Email and new_password are required"
            }

        if not frappe.db.exists("User", email):
            frappe.local.response["http_status_code"] = 404
            return {
                "status": "error",
                "message": "User not found"
            }

        # 🔍 Check OTP verified
        otp_verified = frappe.db.get_value("User", email, "otp_verified")

        if not otp_verified:
            return {
                "status": "error",
                "message": "OTP not verified"
            }

        # ✅ Correct password update (hashed)
        frappe.utils.password.update_password(email, new_password)

        # 🧹 Clear OTP fields (NO modified change)
        frappe.db.sql("""
            UPDATE `tabUser`
            SET verify_otp=NULL,
                otp_expiry=NULL,
                otp_verified=0
            WHERE name=%s
        """, (email,))

        frappe.db.commit()

        return {
            "status": "success",
            "message": "Password updated successfully",
            "email": email
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Reset Password Error")
        frappe.local.response["http_status_code"] = 500
        return {
            "status": "error",
            "message": str(e)
        }



# @frappe.whitelist(allow_guest=True)
# def user_profile():
#     """
#     Get logged-in user's profile details from User + Employee
#     """
#     try:
#         #user_email = frappe.form_dict.get("email")
#         user_email = validate_token()

#         # Get User details
#         user = frappe.get_doc("User", user_email)

#         # Get Employee linked to user
#         employee = frappe.db.get_value(
#             "Employee",
#             {"user_id": user_email},
#             [
#                 "name",
#                 "gender",
#                 "date_of_birth",
#                 "cell_number",
#                 "employee_number",
#                 "designation",
#                 "person_to_be_contacted",
#                 "emergency_phone_number",
#                 "relation"
#             ],
#             as_dict=True
#         )

#         return {
#             "status": "success",
#             "data": {
#                 "email": user.name,
#                 "first_name": user.first_name,
#                 "country": user.country,
#                 "emergency_contact": employee.emergency_phone_number,
#                 "emergency_contact_name":employee.person_to_be_contacted,
#                 "relation":employee.relation,
#                 "role_profile_name": user.role_profile_name,
#                 "gender": employee.gender if employee else None,
#                 "birth_date": employee.date_of_birth if employee else None,
#                 "mobile_no": employee.cell_number if employee else user.mobile_no,
#                 "employee_number": employee.employee_number if employee else None,
#                 "designation": employee.designation if employee else None
#             }
#         }

#     except Exception as e:
#         frappe.log_error(frappe.get_traceback(), "Pico User Profile API Error")
#         frappe.local.response["http_status_code"] = 500
#         return {
#             "status": "error",
#             "message": str(e)
#         }

from io import BytesIO
from urllib.parse import unquote, urlsplit

import frappe
from PIL import Image, UnidentifiedImageError
from frappe.utils import get_url
from frappe.utils.file_manager import save_file


# Temporarily True while diagnosing errors.
# Set False after the API works.
PROFILE_DEBUG = True


def _profile_error(exc, title, rollback=False):
    if rollback:
        frappe.db.rollback()

    frappe.log_error(
        message=frappe.get_traceback(),
        title=title,
    )

    if isinstance(exc, frappe.AuthenticationError):
        status_code = 401
        message = "Invalid or expired token"
    elif isinstance(exc, frappe.PermissionError):
        status_code = 403
        message = "Access denied"
    elif isinstance(exc, frappe.ValidationError):
        status_code = 400
        message = str(exc)
    else:
        status_code = 500
        message = "Unable to process profile"

    frappe.local.response["http_status_code"] = status_code

    result = {
        "status": "error",
        "message": str(exc) if PROFILE_DEBUG else message,
    }

    if PROFILE_DEBUG:
        result["error_type"] = type(exc).__name__

    return result


def _get_profile_user():
    user_email = validate_token()

    if not user_email or user_email == "Guest":
        frappe.throw(
            "Authentication required",
            frappe.AuthenticationError,
        )

    user = frappe.get_doc("User", user_email)

    if not user.enabled:
        frappe.throw(
            "User is disabled",
            frappe.AuthenticationError,
        )

    return user


def _get_profile_employee(user_email):
    employee_name = frappe.db.get_value(
        "Employee",
        {"user_id": user_email},
        "name",
    )

    if not employee_name:
        return None

    return frappe.get_doc("Employee", employee_name)


def _profile_response_data(user, employee=None):
    image_path = user.get("profile_image")

    return {
        "email": user.name,
        "first_name": user.get("first_name"),
        "country": user.get("country"),
        "role_profile_name": user.get("role_profile_name"),
        "profile_image": image_path,
        "profile_image_url": (
            get_url(image_path)
            if image_path and image_path.startswith("/")
            else image_path
        ),
        "mobile_no": (
            employee.get("cell_number") if employee else None
        ) or user.get("mobile_no"),
        "gender": employee.get("gender") if employee else None,
        "birth_date": employee.get("date_of_birth") if employee else None,
        "employee_number": (
            employee.get("employee_number") if employee else None
        ),
        "designation": employee.get("designation") if employee else None,
        "person_to_be_contacted": (
            employee.get("person_to_be_contacted") if employee else None
        ),
        "emergency_phone_number": (
            employee.get("emergency_phone_number") if employee else None
        ),
        "emergency_contact": (
            employee.get("emergency_phone_number") if employee else None
        ),
        "emergency_contact_name": (
            employee.get("person_to_be_contacted") if employee else None
        ),
        "relation": employee.get("relation") if employee else None,
    }


def _validate_profile_image(content):
    if not content:
        frappe.throw("Image file is empty")

    if len(content) > 5 * 1024 * 1024:
        frappe.throw("Profile image must be 5 MB or smaller")

    try:
        with Image.open(BytesIO(content)) as image:
            image_format = (image.format or "").upper()
            image.verify()

    except (
        UnidentifiedImageError,
        OSError,
        ValueError,
        Image.DecompressionBombError,
    ):
        frappe.throw("Invalid or corrupted image file")

    extensions = {
        "JPEG": "jpg",
        "PNG": "png",
        "WEBP": "webp",
    }

    if image_format not in extensions:
        frappe.throw(
            f"Detected format: {image_format or 'Unknown'}. "
            "Only JPG, JPEG, PNG and WEBP are allowed."
        )

    return extensions[image_format]


def _existing_profile_image(value, user):
    """
    Reuse an existing file only when attached to this User.
    Does not download arbitrary URLs.
    """
    parsed = urlsplit(value.strip())

    if parsed.scheme and parsed.scheme not in ("http", "https"):
        frappe.throw("Invalid image URL")

    if parsed.netloc:
        site = urlsplit(get_url())

        if (
            parsed.scheme != site.scheme
            or parsed.netloc != site.netloc
        ):
            frappe.throw(
                "Use a file URL from this site, or a relative /files/ path"
            )

    if parsed.query or parsed.fragment:
        frappe.throw("Use a file URL without query parameters or fragments")

    candidates = list(dict.fromkeys([
        parsed.path,
        unquote(parsed.path),
    ]))

    for file_url in candidates:
        if not file_url.startswith(("/files/", "/private/files/")):
            continue

        file_name = frappe.db.get_value(
            "File",
            {
                "file_url": file_url,
                "attached_to_doctype": "User",
                "attached_to_name": user.name,
            },
            "name",
        )

        if file_name:
            file_doc = frappe.get_doc("File", file_name)
            _validate_profile_image(file_doc.get_content())
            return file_doc.file_url

    frappe.throw(
        "This image is not attached to your User. "
        "Upload a local image file instead."
    )


@frappe.whitelist(allow_guest=True)
def user_profile():
    """Get profile details using the validated X-Auth-Token."""
    try:
        user = _get_profile_user()
        employee = _get_profile_employee(user.name)

        return {
            "status": "success",
            "data": _profile_response_data(user, employee),
        }

    except Exception as exc:
        return _profile_error(
            exc,
            "User Profile API Error",
        )


@frappe.whitelist(allow_guest=False, methods=["POST"])
def update_user_profile():
    try:
        user = _get_profile_user()
        employee = _get_profile_employee(user.name)

        if not employee:
            frappe.local.response["http_status_code"] = 404
            return {
                "status": "error",
                "message": "Employee not found",
            }

        if not user.meta.has_field("profile_image"):
            frappe.throw(
                "Field profile_image does not exist in User DocType"
            )

        form = frappe.form_dict

        # Update User fields.
        for fieldname in ("first_name", "mobile_no", "country"):
            value = form.get(fieldname)
            if value:
                user.set(fieldname, value)

        # Update Employee fields.
        employee_fields = {
            "gender": "gender",
            "birth_date": "date_of_birth",
            "designation": "designation",
            "mobile_no": "cell_number",
            "person_to_be_contacted": "person_to_be_contacted",
            "emergency_phone_number": "emergency_phone_number",
            "relation": "relation",
        }

        for request_field, employee_field in employee_fields.items():
            value = form.get(request_field)
            if value:
                employee.set(employee_field, value)

        # Accept either multipart file field name.
        uploaded_image = frappe.request.files.get("profile_image")

        if uploaded_image is None:
            uploaded_image = frappe.request.files.get("file")

        if uploaded_image is not None:
            if not uploaded_image.filename:
                frappe.throw("Please select an image")

            content = uploaded_image.read()

            if not content:
                frappe.throw("Uploaded image is empty")

            if len(content) > 5 * 1024 * 1024:
                frappe.throw("Image must be 5 MB or smaller")

            try:
                with Image.open(BytesIO(content)) as image:
                    image_format = (image.format or "").upper()
                    image.verify()

            except (
                UnidentifiedImageError,
                OSError,
                ValueError,
                Image.DecompressionBombError,
            ):
                frappe.throw("Invalid or corrupted image")

            # Both JPG and JPEG are identified as JPEG by Pillow.
            allowed_formats = {
                "JPEG": "jpg",
                "PNG": "png",
                "WEBP": "webp",
            }

            if image_format not in allowed_formats:
                frappe.throw(
                    f"Detected format: {image_format or 'Unknown'}. "
                    "Upload a JPG, JPEG, PNG or WEBP image."
                )

            extension = allowed_formats[image_format]
            filename = (
                f"profile-{frappe.generate_hash(length=12)}.{extension}"
            )

            saved_file = save_file(
                fname=filename,
                content=content,
                dt="User",
                dn=user.name,
                is_private=1,
            )

            user.set("profile_image", saved_file.file_url)

        elif form.get("profile_image") or form.get("file"):
            frappe.throw(
                "A file path or URL was sent as text. "
                "Select a file in the UI or use curl "
                '-F "profile_image=@/actual/local/image.jpg".'
            )

        user.save(ignore_permissions=True)
        employee.save(ignore_permissions=True)

        user.reload()
        employee.reload()

        data = _profile_response_data(user, employee)

        frappe.db.commit()

        return {
            "status": "success",
            "message": "Profile updated successfully",
            "data": data,
        }

    except Exception as exc:
        return _profile_error(
            exc,
            "Update User Profile API Error",
            rollback=True,
        )


@frappe.whitelist(allow_guest=False)
def company_branding():
    """Return branding for the company linked to the authenticated user."""
    try:
        user_email = validate_token()

        if not user_email or user_email == "Guest":
            frappe.local.response["http_status_code"] = 401
            return {
                "status": "error",
                "message": "Authentication required",
            }

        user = frappe.get_doc("User", user_email)

        if not user.enabled:
            frappe.local.response["http_status_code"] = 403
            return {
                "status": "error",
                "message": "User is disabled",
            }

        company = user.get("custom_company")

        if not company:
            frappe.local.response["http_status_code"] = 404
            return {
                "status": "error",
                "message": "Company is not configured for this user",
            }

        branding = frappe.db.get_value(
            "Company Branding",
            {"company": company},
            ["name", "company", "logo"],
            as_dict=True,
        )

        if not branding:
            frappe.local.response["http_status_code"] = 404
            return {
                "status": "error",
                "message": "Company branding not found",
            }

        logo = branding.get("logo")

        # Convert a relative file path into an absolute URL.
        logo_url = (
            get_url(logo) if logo and logo.startswith("/") else logo
        )

        return {
            "status": "success",
            "data": {
                "company": company,
                "logo": logo,
                "logo_url": logo_url,
            },
        }

    except frappe.AuthenticationError:
        frappe.local.response["http_status_code"] = 401
        return {
            "status": "error",
            "message": "Invalid or expired authentication",
        }

    except frappe.PermissionError:
        frappe.local.response["http_status_code"] = 403
        return {
            "status": "error",
            "message": "Access denied",
        }

    except Exception:
        frappe.log_error(
            frappe.get_traceback(),
            "Company Branding API Error",
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": "Unable to fetch company branding",
        }

@frappe.whitelist(allow_guest=True)
def refresh_token():
    try:
        token = frappe.get_request_header("X-Auth-Token")

        if not token:
            frappe.local.response["http_status_code"] = 401
            return {
                "status": "error",
                "message": "Token missing"
            }

        # Find user by token
        user = frappe.db.get_value(
            "User",
            {"api_token": token},
            ["name", "token_expiry"],
            as_dict=True
        )

        if not user:
            frappe.local.response["http_status_code"] = 401
            return {
                "status": "error",
                "message": "Invalid token"
            }

        # Check expiry
        if user.token_expiry < now_datetime():
            frappe.local.response["http_status_code"] = 401
            return {
                "status": "error",
                "message": "Token expired. Please login again."
            }

        # Generate new token
        new_token = str(uuid.uuid4())
        new_expiry = now_datetime() + timedelta(hours=8)

        # Update user
        user_doc = frappe.get_doc("User", user.name)
        user_doc.api_token = new_token
        user_doc.token_expiry = new_expiry
        user_doc.save(ignore_permissions=True)

        frappe.db.commit()

        return {
            "status": "success",
            "message": "Token refreshed successfully",
            "token": new_token,
            "expires_at": new_expiry
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Refresh Token Error")
        frappe.local.response["http_status_code"] = 500
        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def today_attendance():
    try:
        # Validate token → get user
        user_email = validate_token()

        # Get Employee linked to user
        employee = frappe.db.get_value(
            "Employee",
            {"user_id": user_email},
            ["name", "employee_name"],
            as_dict=True
        )

        if not employee:
            return {
                "status": "error",
                "message": "Employee not linked to this user"
            }

        # Get today's attendance
        attendance = frappe.db.get_value(
            "Attendance",
            {
                "employee": employee.name,
                "attendance_date": nowdate()
            },
            [
                "name",
                "status",
                "in_time",
                "out_time",
                "working_hours"
            ],
            as_dict=True
        )

        return {
            "status": "success",
            "data": {
                "employee": employee.name,
                "employee_name": employee.employee_name,
                "date": nowdate(),
                "attendance": attendance if attendance else "Not Marked"
            }
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Today Attendance API Error")
        return {
            "status": "error",
            "message": str(e)
        }


@frappe.whitelist(allow_guest=True)
def attendance_history():
    try:
        from frappe.utils import nowdate
        from datetime import datetime, timedelta
        import calendar
        import json

        user_email = validate_token()

        # ✅ FIX: Read body properly
        #data = frappe.request.get_json() or {}
        data = frappe.request.get_json(silent=True) or frappe.form_dict

        year = int(data.get("year")) if data.get("year") else datetime.now().year
        month_filter = int(data.get("month")) if data.get("month") else None

        today = datetime.strptime(nowdate(), "%Y-%m-%d")

        # Employee
        employee = frappe.db.get_value(
            "Employee",
            {"user_id": user_email},
            ["name", "employee_name", "holiday_list"],
            as_dict=True
        )

        if not employee:
            return {"status": "error", "message": "Employee not linked"}

        # Holidays
        holidays = frappe.get_all(
            "Holiday",
            filters={"parent": employee.holiday_list},
            fields=["holiday_date", "description"]
        )
        holiday_map = {str(h.holiday_date): h.description for h in holidays}

        response = []

        # ✅ Decide months to process
        if month_filter:
            months_to_process = [month_filter]
        else:
            if year == today.year:
                months_to_process = list(range(1, today.month + 1))
            else:
                months_to_process = list(range(1, 13))

        # Loop months
        for month in months_to_process:

            month_name = calendar.month_name[month]

            start_date = datetime(year, month, 1)
            last_day = calendar.monthrange(year, month)[1]
            end_date = datetime(year, month, last_day)

            # ✅ Limit to today only if current month & year
            if year == today.year and month == today.month:
                end_date = today

            # Attendance records
            attendance_records = frappe.get_all(
                "Attendance",
                filters={
                    "employee": employee.name,
                    "attendance_date": ["between", [start_date, end_date]]
                },
                fields=[
                    "attendance_date",
                    "status",
                    "in_time",
                    "out_time",
                    "working_hours"
                ]
            )

            att_map = {str(a.attendance_date): a for a in attendance_records}

            days_data = []
            current = start_date

            while current <= end_date:
                date_str = str(current.date())

                if date_str in att_map:
                    record = att_map[date_str]
                    days_data.append({
                        "date": date_str,
                        "status": record.status,
                        "in_time": record.in_time,
                        "out_time": record.out_time,
                        "working_hours": record.working_hours
                    })
                else:
                    if date_str in holiday_map:
                        days_data.append({
                            "date": date_str,
                            "status": "Holiday",
                            "description": holiday_map[date_str]
                        })
                    else:
                        days_data.append({
                            "date": date_str,
                            "status": "Absent"
                        })

                current += timedelta(days=1)

            response.append({
                "month": month,
                "month_name": month_name,
                "data": days_data
            })

        return {
            "status": "success",
            "year": year,
            "employee": employee.name,
            "attendance": response
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Attendance History Error")
        return {"status": "error", "message": str(e)}


@frappe.whitelist(allow_guest=True)
def holiday_calendar():
    try:
        # 🔐 Token validation (your custom method)
        user = validate_token()

        data = frappe.request.get_json(silent=True) or {}
        year = int(data.get("year", datetime.now().year))

        # 🔍 Get employee
        employee = frappe.db.get_value("Employee", {"user_id": user}, "name")
        if not employee:
            return {"status": "error", "message": "Employee not found"}

        # 📌 Get holiday list
        holiday_list = frappe.db.get_value("Employee", employee, "holiday_list")
        if not holiday_list:
            return {"status": "error", "message": "Holiday List not assigned"}

        # 📅 Get holidays
        holidays = frappe.get_all(
            "Holiday",
            filters={
                "parent": holiday_list,
                "holiday_date": ["between", [f"{year}-01-01", f"{year}-12-31"]]
            },
            fields=["holiday_date", "description"],
            order_by="holiday_date asc"
        )

        # 🗂 Group by month
        result = {}

        for h in holidays:
            month = h.holiday_date.strftime("%m")
            month_name = h.holiday_date.strftime("%B")

            if month not in result:
                result[month] = {
                    "month": int(month),
                    "month_name": month_name,
                    "holidays": []
                }

            result[month]["holidays"].append({
                "date": str(h.holiday_date),
                "description": h.description
            })

        return {
            "status": "success",
            "year": year,
            "holiday_list": holiday_list,
            "data": list(result.values())
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Holiday Calendar Error")
        return {"status": "error", "message": str(e)}


@frappe.whitelist(allow_guest=True)
def employee_checkin():
    try:
        data = frappe.request.get_json()

        employee_number = data.get("employee_number")
        log_type = data.get("log_type")  # IN / OUT
        time = data.get("time")
        latitude = data.get("latitude")
        longitude = data.get("longitude")
        status = data.get("status")
        punch_device = data.get("punch_device")
        image_data = data.get("image")  # base64 string

        # 🔴 Validation
        if not employee_number or not log_type:
            frappe.local.response["http_status_code"] = 400
            return {
                "status": "error",
                "message": "employee_number and log_type are required"
            }

        # 🔍 Get Employee
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["name", "employee_name", "user_id","company"],
            as_dict=True
        )

        if not employee:
            frappe.local.response["http_status_code"] = 404
            return {
                "status": "error",
                "message": "Employee not found"
            }

        # ⏰ Time handling
        checkin_time = get_datetime(time) if time else now_datetime()

        # 📅 Extract only date from checkin time
        checkin_date = checkin_time.date()

         # 🔍 Check whether approved leave exists for this date
        approved_leave = frappe.db.sql("""
            SELECT
                name,
                from_date,
                to_date,
                half_day,
                which_half
            FROM `tabLeave Application`
            WHERE employee = %s
            AND status = 'Approved'
            AND docstatus = 1
            AND %s BETWEEN from_date AND to_date
            LIMIT 1
        """, (employee.name, checkin_date), as_dict=True)

        # -----------------------------------
        # LEAVE VALIDATION
        # -----------------------------------
        if approved_leave:

            leave_doc = approved_leave[0]

            # Current hour from checkin time
            current_hour = checkin_time.hour

            # -----------------------------------
            # FULL DAY LEAVE
            # -----------------------------------
            if not leave_doc.half_day:

                frappe.local.response[
                    "http_status_code"
                ] = 409

                return {
                    "status": "error",
                    "message":
                        f"For Date {checkin_date}, "
                        f"leave application is already approved. "
                        f"Kindly cancel the leave application before checkin."
                }

            # -----------------------------------
            # HALF DAY LEAVE
            # -----------------------------------
            else:

                which_half = (
                    leave_doc.which_half or ""
                ).strip().lower()

                # -----------------------------------
                # FIRST HALF / BEFORE LUNCH
                # Employee allowed AFTER 2 PM
                # -----------------------------------
                if which_half in [
                    "first half",
                    "before lunch"
                ]:

                    if current_hour < 14:

                        frappe.local.response[
                            "http_status_code"
                        ] = 409

                        return {
                            "status": "error",
                            "message":
                                "You are on First Half Leave. "
                                "Check-in allowed only after 2:00 PM."
                        }

                # -----------------------------------
                # SECOND HALF / AFTER LUNCH
                # Employee allowed BEFORE 2 PM
                # -----------------------------------
                elif which_half in [
                    "second half",
                    "after lunch"
                ]:

                    if current_hour >= 14:

                        frappe.local.response[
                            "http_status_code"
                        ] = 409

                        return {
                            "status": "error",
                            "message":
                                "You are on Second Half Leave. "
                                "Check-in not allowed after 2:00 PM."
                        }

        # 📌 Create Checkin
        checkin = frappe.get_doc({
            "doctype": "Employee Checkin",
            "employee": employee.name,
            "employee_name": employee.employee_name,
            "log_type": log_type,
            "time": checkin_time,
            "employee_number": employee_number,
            "skip_auto_attendance":1,
            "latitude": latitude,
            "longitude": longitude,
            "status": status,
            "punch_device": punch_device,
            "user": employee.user_id,
            "custom_company":employee.company
        })

        checkin.insert(ignore_permissions=True)

        # 📸 Handle Image Upload
        if image_data:
            try:
                file_content = base64.b64decode(image_data)

                file_doc = save_file(
                    fname=f"{employee_number}_{log_type}.jpg",
                    content=file_content,
                    dt="Employee Checkin",
                    dn=checkin.name,
                    is_private=0
                )

                frappe.db.set_value("Employee Checkin",checkin.name,"attach_image",file_doc.file_url)

            except Exception as img_err:
                frappe.log_error(str(img_err), "Image Upload Error")

        frappe.db.commit()

        return {
            "status": "success",
            "message": "Checkin recorded",
            "employee": employee.name,
            "checkin_id": checkin.name
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Employee Checkin API Error")
        frappe.local.response["http_status_code"] = 500
        return {
            "status": "error",
            "message": str(e)
        }


@frappe.whitelist(allow_guest=True)
def get_employee_checkins():
    try:
        employee_number = frappe.form_dict.get("employee_number")
        from_date = frappe.form_dict.get("from_date")
        to_date = frappe.form_dict.get("to_date")

        if not employee_number:
            frappe.local.response["http_status_code"] = 400
            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # 🔍 Get Employee
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["name", "employee_name"],
            as_dict=True
        )

        if not employee:
            frappe.local.response["http_status_code"] = 404
            return {
                "status": "error",
                "message": "Employee not found"
            }

        # 📌 Filters
        filters = {
            "employee": employee.name
        }

        if from_date and to_date:
            filters["time"] = ["between", [from_date, to_date]]

        # 📊 Fetch Checkins
        checkins = frappe.db.get_all(
            "Employee Checkin",
            filters=filters,
            fields=[
                "name",
                "log_type",
                "time",
                "employee_number",
                "latitude",
                "longitude",
                "status",
                "punch_device"
            ],
            order_by="time desc"
        )

        return {
            "status": "success",
            "employee": employee.name,
            "employee_name": employee.employee_name,
            "count": len(checkins),
            "data": checkins
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Get Checkin API Error")
        frappe.local.response["http_status_code"] = 500
        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def employee_checkout():
    try:
        data = frappe.request.get_json()

        employee_number = data.get("employee_number")
        time = data.get("time")
        latitude = data.get("latitude")
        longitude = data.get("longitude")
        status = data.get("status")
        punch_device = data.get("punch_device")
        image_data = data.get("image")

        if not employee_number or not time:
            frappe.local.response["http_status_code"] = 400
            return {
                "status": "error",
                "message": "employee_number and time are required"
            }

        # 🔍 Get Employee
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["name", "employee_name", "user_id"],
            as_dict=True
        )

        if not employee:
            frappe.local.response["http_status_code"] = 404
            return {
                "status": "error",
                "message": "Employee not found"
            }

        # 📝 Create Checkin (OUT)
        checkin = frappe.get_doc({
            "doctype": "Employee Checkin",
            "employee": employee.name,
            "employee_name": employee.employee_name,
            "employee_number": employee_number,
            "skip_auto_attendance":1,
            "log_type": "OUT",
            "time": time,
            "latitude": latitude,
            "longitude": longitude,
            "status": status,
            "punch_device": punch_device
        })

        checkin.insert(ignore_permissions=True)

        # 📸 Image Upload
        if image_data:
            try:
                import base64
                from frappe.utils.file_manager import save_file

                file_content = base64.b64decode(image_data)

                file_doc = save_file(
                    fname=f"{employee_number}_OUT_{frappe.utils.now()}.jpg",
                    content=file_content,
                    dt="Employee Checkin",
                    dn=checkin.name,
                    is_private=0
                )

                frappe.db.set_value(
                    "Employee Checkin",
                    checkin.name,
                    "attach_image",
                    file_doc.file_url
                )

            except Exception as img_err:
                frappe.log_error(str(img_err), "Checkout Image Error")

        frappe.db.commit()

        return {
            "status": "success",
            "message": "Punch OUT recorded",
            "checkin_id": checkin.name,
            "employee": employee.name
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Checkout API Error")
        frappe.local.response["http_status_code"] = 500
        return {
            "status": "error",
            "message": str(e)
        }


@frappe.whitelist(allow_guest=False)
def get_employee_last_punch(employee_number, date=None):
    """
    Get employee last punch IN and OUT.
    
    Optional:
    - date (YYYY-MM-DD)
      If date is passed, returns that day's punch details.
      Otherwise returns latest punch details.
    """

    try:

        if not employee_number:
            return {
                "success": False,
                "message": "employee_number is required"
            }

        # Fetch Employee
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["name", "employee_name", "company"],
            as_dict=True
        )

        if not employee:
            return {
                "success": False,
                "message": "Employee not found"
            }

        filters = {
            "employee": employee.name
        }

        # Filter by date if provided
        if date:
            filters["time"] = ["between", [f"{date} 00:00:00", f"{date} 23:59:59"]]

        # Get Punch IN
        last_in = frappe.get_all(
            "Employee Checkin",
            filters={
                **filters,
                "log_type": "IN"
            },
            fields=[
                "name",
                "time",
                "log_type"
            ],
            order_by="time desc",
            limit=1
        )

        # Get Punch OUT
        last_out = frappe.get_all(
            "Employee Checkin",
            filters={
                **filters,
                "log_type": "OUT"
            },
            fields=[
                "name",
                "time",
                "log_type"
            ],
            order_by="time desc",
            limit=1
        )

        return {
            "success": True,
            "employee": employee.name,
            "employee_name": employee.employee_name,
            "employee_number": employee_number,
            "company": employee.company,
            "date": date if date else "latest",
            "last_punch_in": last_in[0] if last_in else None,
            "last_punch_out": last_out[0] if last_out else None
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Get Employee Last Punch API")

        return {
            "success": False,
            "message": str(e)
        }


import frappe
from frappe import _
from frappe.utils import getdate, nowdate

@frappe.whitelist(allow_guest=True)
def apply_leave():
    try:
        data = frappe.request.get_json()

        employee_number = data.get("employee_number")
        leave_type = data.get("leave_type")
        from_date = data.get("from_date")
        to_date = data.get("to_date")
        description = data.get("description")
        half_day = data.get("half_day", 0)

        # -----------------------------
        # Validate Required Fields
        # -----------------------------

        if not employee_number:
            frappe.local.response["http_status_code"] = 400
            return {
                "status": "error",
                "message": "employee_number is required"
            }

        if not leave_type or not from_date or not to_date:
            frappe.local.response["http_status_code"] = 400
            return {
                "status": "error",
                "message": "employee, leave_type, from_date, to_date are required"
            }


        # -------------------------------------------------
        # Get Employee using employee_number
        # -------------------------------------------------
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["name"],
        )


        # -----------------------------
        # Check Employee
        # -----------------------------
        if not frappe.db.exists("Employee", employee):
            frappe.local.response["http_status_code"] = 404
            return {
                "status": "error",
                "message": "Employee not found"
            }

        emp_doc = frappe.get_doc("Employee", employee)

        # -----------------------------
        # Get Leave Approver from Employee
        # -----------------------------
        leave_approver = emp_doc.leave_approver

        if not leave_approver:
            frappe.local.response["http_status_code"] = 400
            return {
                "status": "error",
                "message": "Leave approver not configured for employee"
            }

        # -----------------------------
        # Create Leave Application
        # -----------------------------
        leave_doc = frappe.get_doc({
            "doctype": "Leave Application",
            "employee": employee,
            "employee_name": emp_doc.employee_name,
            "company": emp_doc.company,
            "leave_type": leave_type,
            "from_date": from_date,
            "to_date": to_date,
            "description": description,
            "leave_approver": leave_approver,
            "posting_date": nowdate(),
            "status": "Open",
            "half_day": half_day
        })

        leave_doc.insert(ignore_permissions=True)

        frappe.db.commit()

        # -----------------------------
        # ERPNext Document URL
        # -----------------------------
        site_url = frappe.utils.get_url()

        document_url = (
            f"{site_url}/app/leave-application/{leave_doc.name}"
        )

        # -----------------------------
        # Send Mail to Leave Approver
        # -----------------------------
        frappe.sendmail(
            recipients=[leave_approver],
            subject=f"Leave Approval Request - {leave_doc.name}",
            message=f"""
                <p>Dear Approver,</p>

                <p>
                    Employee <b>{emp_doc.employee_name}</b>
                    has applied for leave.
                </p>

                <p>
                    <b>Leave Type:</b> {leave_type}<br>
                    <b>From:</b> {from_date}<br>
                    <b>To:</b> {to_date}
                </p>

                <p>
                    Click below to review:
                </p>

                <p>
                    <a href="{document_url}">
                        {leave_doc.name}
                    </a>
                </p>

                <p>Thanks</p>
            """
        )

        return {
            "status": "success",
            "message": "Leave application submitted successfully",
            "leave_application": leave_doc.name,
            "leave_approver": leave_approver
        }

    except Exception as e:
        frappe.log_error(
            frappe.get_traceback(),
            "Leave Application API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def approve_reject_leave():

    try:

        data = frappe.request.get_json(
            silent=True
        ) or {}

        leave_application = data.get(
            "leave_application"
        )

        action = data.get(
            "action"
        )

        remarks = data.get(
            "remarks"
        )

        # -----------------------------------
        # VALIDATE
        # -----------------------------------
        if not leave_application:

            frappe.local.response[
                "http_status_code"
            ] = 400

            return {
                "status": "error",
                "message":
                    "leave_application is required"
            }

        if not action:

            frappe.local.response[
                "http_status_code"
            ] = 400

            return {
                "status": "error",
                "message":
                    "action is required"
            }

        action = action.title()

        leave_doc = frappe.get_doc(
            "Leave Application",
            leave_application
        )

        # ===================================
        # APPROVED
        # ===================================
        if action == "Approved":

            leave_doc.status = "Approved"

            leave_doc.save(
                ignore_permissions=True
            )

            if leave_doc.docstatus == 0:

                leave_doc.submit()

            frappe.db.commit()

            return {
                "status": "success",
                "message":
                    "Leave Approved successfully",
                "leave_application":
                    leave_doc.name
            }

        # ===================================
        # REJECTED
        # ===================================
        elif action == "Rejected":

            leave_doc.status = "Rejected"

            leave_doc.save(
                ignore_permissions=True
            )

            frappe.db.commit()

            return {
                "status": "success",
                "message":
                    "Leave Rejected successfully"
            }

        # ===================================
        # CANCELLED
        # ===================================
        elif action == "Cancelled":

            # -----------------------------------
            # GET REQUESTED VALUES
            # -----------------------------------
            requested_half_day = cint(
                leave_doc.requested_half_day
            )

            requested_which_half = (
                leave_doc.requested_which_half
            )

            half_day_date = (
                leave_doc.half_day_date
                or leave_doc.from_date
            )

            # -----------------------------------
            # CANCEL ATTENDANCE
            # -----------------------------------
            attendance_list = frappe.get_all(
                "Attendance",
                filters={
                    "leave_application":
                        leave_doc.name
                },
                pluck="name"
            )

            for att in attendance_list:

                att_doc = frappe.get_doc(
                    "Attendance",
                    att
                )

                if att_doc.docstatus == 1:

                    att_doc.cancel()

            # -----------------------------------
            # UPDATE STATUS
            # -----------------------------------
            frappe.db.set_value(
                "Leave Application",
                leave_doc.name,
                "status",
                "Cancelled",
                update_modified=False
            )

            frappe.db.commit()

            # -----------------------------------
            # RELOAD LATEST DOC
            # -----------------------------------
            leave_doc = frappe.get_doc(
                "Leave Application",
                leave_doc.name
            )

            # -----------------------------------
            # TRIGGER HOOK
            # -----------------------------------
            leave_doc.run_method(
                "on_update_after_submit"
            )

            frappe.db.commit()

            # -----------------------------------
            # RELOAD FINAL STATUS
            # -----------------------------------
            leave_doc.reload()
            # -----------------------------------
            # CANCEL DOC
            # -----------------------------------
            if leave_doc.docstatus == 1:

                leave_doc.cancel()


            frappe.db.commit()

            return {
                "status": "success",
                "message":
                    "Leave Cancelled successfully",
                "revised_half_day_created":
                    requested_half_day
            }

        else:

            return {
                "status": "error",
                "message":
                    "Invalid action"
            }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Approve Reject Leave API Error"
        )

        frappe.local.response[
            "http_status_code"
        ] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def cancel_leave_application():

    try:

        # -----------------------------------
        # GET PARAMS
        # -----------------------------------
        leave_application = frappe.form_dict.get("leave_application")

        cancellation_reason = frappe.form_dict.get("cancellation_reason")

        revised_half_day = cint(
            frappe.form_dict.get("half_day") or 0
        )

        which_half = frappe.form_dict.get("which_half")

        cancellation_date = frappe.form_dict.get("cancellation_date")

        half_day_date = frappe.form_dict.get(
            "half_day_date"
        )

        # -----------------------------------
        # POST JSON
        # -----------------------------------
        if not leave_application:

            data = frappe.request.get_json(
                silent=True
            ) or {}

            leave_application = data.get(
                "leave_application"
            )

            cancellation_reason = data.get(
                "cancellation_reason"
            )

            revised_half_day = cint(
                data.get("half_day") or 0
            )

            which_half = data.get("which_half")

            cancellation_date = data.get(
                "cancellation_date"
            )

            half_day_date = data.get(
                "half_day_date"
           )

        # -----------------------------------
        # VALIDATE
        # -----------------------------------
        if not leave_application:

            frappe.local.response[
                "http_status_code"
            ] = 400

            return {
                "status": "error",
                "message":
                    "leave_application is required"
            }

        # -----------------------------------
        # GET LEAVE DOC
        # -----------------------------------
        leave_doc = frappe.get_doc(
            "Leave Application",
            leave_application
        )

        # -----------------------------------
        # ALREADY CANCELLED
        # -----------------------------------
        if leave_doc.status == "Cancelled":

            return {
                "status": "error",
                "message":
                    "Leave already cancelled"
            }


        leave_doc.db_set(
            "requested_half_day",
            cint(revised_half_day),
            update_modified=False
        )

        leave_doc.db_set(
            "requested_which_half",
            which_half,
            update_modified=False
        )

        leave_doc.db_set(
            "cancellation_reason",
            cancellation_reason,
            update_modified=False
        )

        leave_doc.db_set(
            "cancellate_date",
            cancellation_date
            or frappe.utils.today(),
            update_modified=False
        )

        leave_doc.db_set(
            "half_day_date",
             half_day_date,
             update_modified=False
        )


        frappe.db.commit()


        # -----------------------------------
        # CASE 1:
        # NOT APPROVED YET
        # JUST CANCEL
        # -----------------------------------
        if leave_doc.docstatus == 0:

            leave_doc.status = "Cancelled"

            leave_doc.save(
                ignore_permissions=True
            )

            frappe.db.commit()

            return {
                "status": "success",
                "message":
                    "Leave cancelled successfully"
            }

        # -----------------------------------
        # CASE 2:
        # APPROVED LEAVE
        # SEND MAIL TO APPROVER
        # -----------------------------------

        # -----------------------------------
        # HALF DAY REVISION REQUEST
        # -----------------------------------
        if revised_half_day == 1:

            subject = (
                f"Half Day Revision Request "
                f"- {leave_doc.name}"
            )

            message = f"""
                <p>Dear Approver,</p>

                <p>
                    Employee
                    <b>{leave_doc.employee_name}</b>
                    requested cancellation
                    of approved leave.
                </p>

                <p>
                    Instead of full cancellation,
                    employee requested
                    <b>Half Day Leave</b>.
                </p>

                <p>
                    <b>Date:</b>
                    {leave_doc.from_date}
                </p>

                <p>
                    <b>Which Half:</b>
                    {which_half}
                </p>

                <p>
                    <b>Reason:</b>
                    {cancellation_reason}
                </p>

                <p>
                    Please review below:
                </p>

                <p>
                    <a href="{frappe.utils.get_url()}/app/leave-application/{leave_doc.name}">
                        {leave_doc.name}
                    </a>
                </p>

                <p>Thanks</p>
            """

        else:

            # -----------------------------------
            # FULL CANCELLATION REQUEST
            # -----------------------------------
            subject = (
                f"Leave Cancellation Request "
                f"- {leave_doc.name}"
            )

            message = f"""
                <p>Dear Approver,</p>

                <p>
                    Employee
                    <b>{leave_doc.employee_name}</b>
                    requested cancellation
                    of approved leave.
                </p>

                <p>
                    <b>Leave:</b>
                    {leave_doc.name}
                </p>

                <p>
                    <b>Reason:</b>
                    {cancellation_reason}
                </p>

                <p>
                    Please review below:
                </p>

                <p>
                    <a href="{frappe.utils.get_url()}/app/leave-application/{leave_doc.name}">
                        {leave_doc.name}
                    </a>
                </p>

                <p>Thanks</p>
            """

        # -----------------------------------
        # SEND MAIL
        # -----------------------------------

        leave_doc.db_set(
            "status",
            "Cancellation Inprocess",
            update_modified=False
        )

        frappe.sendmail(
            recipients=[
                leave_doc.leave_approver
            ],
            subject=subject,
            message=message
        )

        frappe.db.commit()

        return {
            "status": "success",
            "message":
                "Cancellation request sent to approver",
            "half_day_requested":
                revised_half_day,
            "which_half":
                which_half
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Cancel Leave API Error"
        )

        frappe.local.response[
            "http_status_code"
        ] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def cancel_leave_applications():

    try:

        leave_application = frappe.form_dict.get("leave_application")

        if not leave_application:
            data = frappe.request.get_json(silent=True) or {}
            leave_application = data.get("leave_application")
            return {
                "status": "error",
                "message": "leave_application is required"
            }

        leave_doc = frappe.get_doc(
            "Leave Application",
            leave_application
        )

        # -----------------------------------
        # BEFORE APPROVAL
        # Draft/Open
        # -----------------------------------
        if leave_doc.docstatus == 0:

            leave_doc.status = "Cancelled"
            leave_doc.save(ignore_permissions=True)

            frappe.db.commit()

            return {
                "status": "success",
                "message": "Leave cancelled successfully"
            }

        # -----------------------------------
        # AFTER APPROVAL
        # Send approval mail for cancellation
        # -----------------------------------
        elif leave_doc.docstatus == 1:

            site_url = frappe.utils.get_url()

            document_url = (
                f"{site_url}/app/leave-application/{leave_doc.name}"
            )

            frappe.sendmail(
                recipients=[leave_doc.leave_approver],
                subject=f"Leave Cancellation Request - {leave_doc.name}",
                message=f"""
                    <p>Employee
                    <b>{leave_doc.employee_name}</b>
                    requested leave cancellation.</p>

                    <p>
                        Click below to review:
                    </p>

                    <p>
                        <a href="{document_url}">
                            {leave_doc.name}
                        </a>
                    </p>
                """
            )

            return {
                "status": "success",
                "message": "Cancellation request sent to approver"
            }

        return {
            "status": "error",
            "message": "Invalid leave state"
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Cancel Leave API Error"
        )

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def whos_on_leave():

    try:

        today = frappe.utils.today()

        # Get all approved leaves for today
        leave_list = frappe.get_all(
            "Leave Application",
            filters={
                "status": "Approved",
                "docstatus": 1,
                "from_date": ["<=", today],
                "to_date": [">=", today]
            },
            fields=[
                "name",
                "employee",
                "employee_name",
                "leave_type",
                "from_date",
                "to_date",
                "half_day",
                "description"
            ],
            order_by="employee_name asc"
        )

        employees_on_leave = []

        for leave in leave_list:

            employee_details = frappe.db.get_value(
                "Employee",
                leave.employee,
                [
                    "employee_number",
                    "department",
                    "designation",
                    "company",
                    "image"
                ],
                as_dict=True
            )

            employees_on_leave.append({
                "leave_application": leave.name,
                "employee": leave.employee,
                "employee_name": leave.employee_name,
                "employee_number": employee_details.employee_number if employee_details else "",
                "department": employee_details.department if employee_details else "",
                "designation": employee_details.designation if employee_details else "",
                "company": employee_details.company if employee_details else "",
                "employee_image": employee_details.image if employee_details else "",
                "leave_type": leave.leave_type,
                "from_date": leave.from_date,
                "to_date": leave.to_date,
                "half_day": leave.half_day,
                "reason": leave.description
            })

        return {
            "status": "success",
            "date": today,
            "count": len(employees_on_leave),
            "employees_on_leave": employees_on_leave
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Who's On Leave API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }


@frappe.whitelist(allow_guest=True)
def get_employee_leave_history():

    try:

        # -----------------------------------
        # Support GET Params
        # -----------------------------------
        employee_number = frappe.form_dict.get("employee_number")
        status = frappe.form_dict.get("status")
        leave_application = frappe.form_dict.get("leave_application")

        # -----------------------------------
        # Support POST JSON
        # -----------------------------------
        if not employee_number:

            data = frappe.request.get_json(silent=True) or {}

            employee_number = data.get("employee_number")
            status = data.get("status")
            leave_application = data.get("leave_application")

        # -----------------------------------
        # Validate
        # -----------------------------------
        if not employee_number:

            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # -----------------------------------
        # Get Employee
        # -----------------------------------
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            [
                "name",
                "employee_name",
                "department",
                "designation",
                "company"
            ],
            as_dict=True
        )

        if not employee:

            frappe.local.response["http_status_code"] = 404

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # -----------------------------------
        # Filters
        # -----------------------------------
        filters = {
            "employee": employee.name
        }

        if status:
            filters["status"] = status

        if leave_application:
            filters["name"] = leave_application

        # -----------------------------------
        # Get Leave Applications
        # -----------------------------------
        leave_list = frappe.get_all(
            "Leave Application",
            filters=filters,
            fields=[
                "name as id",
                "leave_type",
                "from_date",
                "to_date",
                "total_leave_days",
                "half_day",
                "description",
                "posting_date",
                "status",
                "docstatus",
                "creation",
                "modified",
                "leave_approver"
            ],
            order_by="creation desc"
        )

        return {
            "status": "success",
            "employee": {
                "employee_id": employee.name,
                "employee_number": employee_number,
                "employee_name": employee.employee_name,
                "department": employee.department,
                "designation": employee.designation,
                "company": employee.company
            },
            "count": len(leave_list),
            "leave_history": leave_list
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Employee Leave History API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }


@frappe.whitelist(allow_guest=True)
def get_employee_shift():
    """
    Get employee shift details using employee_number
    """

    try:
        employee_number = frappe.form_dict.get("employee_number")

        if not employee_number:
            data = frappe.request.get_json(silent=True) or {}
            employee_number = data.get("employee_number")


        if not employee_number:
            frappe.local.response["http_status_code"] = 400
            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # Get Employee from employee_number
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            [
                "name",
                "employee_name",
                "default_shift"
            ],
            as_dict=True
        )

        if not employee:
            frappe.local.response["http_status_code"] = 404
            return {
                "status": "error",
                "message": "Employee not found"
            }

        shift_details = {}

        if employee.default_shift:

            shift = frappe.db.get_value(
                "Shift Type",
                employee.default_shift,
                [
                    "name",
                    "start_time",
                    "end_time"
                ],
                as_dict=True
            )

            shift_details = shift or {}

        return {
            "status": "success",
            "employee": employee.name,
            "employee_name": employee.employee_name,
            "employee_number": employee_number,
            "shift": shift_details
        }

    except Exception as e:
        frappe.log_error(
            frappe.get_traceback(),
            "Get Employee Shift API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def get_leave_balance():
    """
    Get employee leave balance and used leave details
    """

    try:

        employee_number = frappe.form_dict.get("employee_number")
        year = frappe.form_dict.get("year") or frappe.utils.now_datetime().year

        # Support POST JSON body also
        if not employee_number:
            data = frappe.request.get_json(silent=True) or {}

            employee_number = data.get("employee_number")
            year = data.get("year") or frappe.utils.now_datetime().year

        if not employee_number:
            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # Get Employee
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            [
                "name",
                "employee_name"
            ],
            as_dict=True
        )

        if not employee:
            frappe.local.response["http_status_code"] = 404

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # Get Leave Allocations
        allocations = frappe.get_all(
            "Leave Allocation",
            filters={
                "employee": employee.name,
                "docstatus": 1
            },
            fields=[
                "name",
                "leave_type",
                "from_date",
                "to_date",
                "new_leaves_allocated",
                "total_leaves_allocated"
            ]
        )

        leave_summary = []

        for allocation in allocations:

            # Only current year allocations
            if str(year) not in str(allocation.from_date):
                continue

            # Used Leave
            used_leaves = frappe.get_all(
                "Leave Application",
                filters={
                    "employee": employee.name,
                    "leave_type": allocation.leave_type,
                    "status": "Approved",
                    "docstatus": 1
                },
                fields=[
                    "name",
                    "from_date",
                    "to_date",
                    "total_leave_days",
                    "description",
                    "status"
                ]
            )

            total_used = sum(
                float(d.total_leave_days)
                for d in used_leaves
            )

            allocated = (
                allocation.total_leaves_allocated
                or allocation.new_leaves_allocated
                or 0
            )

            balance = allocated - total_used

            leave_summary.append({
                "leave_type": allocation.leave_type,
                "allocated_leaves": allocated,
                "used_leaves": total_used,
                "balance_leaves": balance,
                "used_leave_details": used_leaves
            })

        return {
            "status": "success",
            "employee": employee.name,
            "employee_name": employee.employee_name,
            "employee_number": employee_number,
            "year": year,
            "leave_summary": leave_summary
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Get Leave Balance API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def get_leave_types():
    """
    Get all active leave types
    """

    try:

        leave_types = frappe.get_all(
            "Leave Type",
            filters={
                "company": "PICO Automation"
            },
            fields=[
                "name",
                "max_leaves_allowed",
                "is_lwp",
                "is_compensatory"
            ],
            order_by="name asc"
        )

        return {
            "status": "success",
            "count": len(leave_types),
            "leave_types": leave_types
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Get Leave Types API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }


@frappe.whitelist(allow_guest=True)
def get_pending_leaves_by_employee():
    """
    Get pending leave applications for employee
    """

    try:

        # Try GET params first
        employee_number = frappe.form_dict.get("employee_number")

        # If not found, try POST JSON body
        if not employee_number:
            data = frappe.request.get_json(silent=True) or {}
            employee_number = data.get("employee_number")

        if not employee_number:
            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # Get Employee
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            [
                "name",
                "employee_name"
            ],
            as_dict=True
        )

        if not employee:
            frappe.local.response["http_status_code"] = 404

            return {
                "status": "error",
                "message": "Employee not found"
            }

        pending_leaves = frappe.get_all(
            "Leave Application",
            filters={
                "employee": employee.name,
                "status": "Open"
            },
            fields=[
                "name",
                "leave_type",
                "from_date",
                "to_date",
                "total_leave_days",
                "half_day",
                "description",
                "posting_date",
                "status"
            ],
            order_by="creation desc"
        )

        return {
            "status": "success",
            "employee": employee.name,
            "employee_name": employee.employee_name,
            "employee_number": employee_number,
            "count": len(pending_leaves),
            "pending_leaves": pending_leaves
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Pending Leave Employee API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def get_pending_leaves_for_approver():

    try:

        employee_number = frappe.form_dict.get("employee_number")

        if not employee_number:
            data = frappe.request.get_json(silent=True) or {}
            employee_number = data.get("employee_number")

        if not employee_number:
            frappe.local.response["http_status_code"] = 400
            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # -----------------------------------
        # GET EMPLOYEE
        # -----------------------------------
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["name", "employee_name"],
            as_dict=True
        )

        if not employee:
            frappe.local.response["http_status_code"] = 404
            return {
                "status": "error",
                "message": "Employee not found"
            }

        # -----------------------------------
        # GET LEAVES WHERE HE IS APPROVER
        # -----------------------------------
        pending_leaves = frappe.get_all(
            "Leave Application",
            filters={
                "leave_approver": frappe.db.get_value("Employee", employee.name, "user_id"),
                "status": "Open"
            },
            fields=[
                "name",
                "employee",
                "employee_name",
                "leave_type",
                "from_date",
                "to_date",
                "total_leave_days",
                "half_day",
                "description",
                "posting_date",
                "status"
            ],
            order_by="creation desc"
        )

        return {
            "status": "success",
            "approver": employee.name,
            "approver_name": employee.employee_name,
            "count": len(pending_leaves),
            "pending_leaves": pending_leaves
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Pending Leave Approver API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=False)
def get_notifications(employee_number, name=None):
    """
    Get active notifications based on employee company.
    Optional filter by Notification document name.
    """

    try:
        if not employee_number:
            return {
                "success": False,
                "message": "employee_number is required"
            }

        # Fetch Employee using employee_number
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["name", "company"],
            as_dict=True
        )

        if not employee:
            return {
                "success": False,
                "message": "Employee not found"
            }

        company = employee.company

        # Filters
        filters = {
            "company": company,
            "is_active": 1
        }

        # Optional filter by Notification doc name
        if name:
            filters["name"] = name

        notifications = frappe.get_all(
            "Notifications",
            filters=filters,
            fields=[
                "name",
                "title",
                "company",
                "description",
                "is_active"
            ],
            order_by="creation desc"
        )

        return {
            "success": True,
            "employee": employee.name,
            "company": company,
            "count": len(notifications),
            "data": notifications
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Get Notifications API")

        return {
            "success": False,
            "message": str(e)
        }


@frappe.whitelist(allow_guest=False)
def delete_leave_application():
    """
    Delete Leave Application only if status is Open/Pending
    Approved or Rejected leave cannot be deleted
    """

    try:

        # Validate token
        user_email = validate_token()

        leave_application = frappe.form_dict.get("leave_application")

        if not leave_application:
            return {
                "status": "error",
                "message": "leave_application is required"
            }

        # Check Leave Application Exists
        if not frappe.db.exists("Leave Application", leave_application):
            return {
                "status": "error",
                "message": "Leave Application not found"
            }

        leave_doc = frappe.get_doc("Leave Application", leave_application)

        # Check linked employee user
        employee = frappe.db.get_value(
            "Employee",
            {"user_id": user_email},
            ["name", "employee_number"],
            as_dict=True
        )

        if not employee:
            return {
                "status": "error",
                "message": "Employee not found"
            }

        # Security check
        if leave_doc.employee != employee.name:
            return {
                "status": "error",
                "message": "You are not allowed to delete this leave application"
            }

        # Allow delete only if not approved/rejected
        if leave_doc.status in ["Approved", "Rejected"]:
            return {
                "status": "error",
                "message": f"Cannot delete leave application because it is already {leave_doc.status}"
            }

        # Delete document
        frappe.delete_doc("Leave Application", leave_application)

        frappe.db.commit()

        return {
            "status": "success",
            "message": "Leave Application deleted successfully",
            "leave_application": leave_application
        }

    except Exception as e:
        frappe.log_error(
            frappe.get_traceback(),
            "Delete Leave Application API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def get_salary_slips():

    try:

        # -----------------------------------
        # Support GET Params
        # -----------------------------------
        employee_number = frappe.form_dict.get("employee_number")
        salary_slip = frappe.form_dict.get("salary_slip")
        year = frappe.form_dict.get("year")

        # -----------------------------------
        # Support POST JSON
        # -----------------------------------
        if not employee_number:

            data = frappe.request.get_json(silent=True) or {}

            employee_number = data.get("employee_number")
            salary_slip = data.get("salary_slip")
            year = data.get("year")

        # -----------------------------------
        # Validate
        # -----------------------------------
        if not employee_number:

            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # -----------------------------------
        # Get Employee
        # -----------------------------------
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            [
                "name",
                "employee_name",
                "department",
                "designation",
                "company"
            ],
            as_dict=True
        )

        if not employee:

            frappe.local.response["http_status_code"] = 404

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # -----------------------------------
        # Filters
        # -----------------------------------
        filters = {
            "employee": employee.name,
            "docstatus": 1
        }

        if salary_slip:
            filters["name"] = salary_slip

        if year:
            filters["start_date"] = ["between", [f"{year}-01-01", f"{year}-12-31"]]

        # -----------------------------------
        # Get Salary Slips
        # -----------------------------------
        salary_slips = frappe.get_all(
            "Salary Slip",
            filters=filters,
            fields=[
                "name as id",
                "employee",
                "employee_name",
                "start_date",
                "end_date",
                "posting_date",
                "gross_pay",
                "net_pay",
                "total_deduction",
                "rounded_total",
                "status",
                "currency",
                "docstatus"
            ],
            order_by="start_date desc"
        )

        # -----------------------------------
        # Particular Salary Slip Details
        # -----------------------------------
        if salary_slip and salary_slips:

            slip_doc = frappe.get_doc(
                "Salary Slip",
                salary_slip
            )

            earnings = []

            for row in slip_doc.earnings:
                earnings.append({
                    "salary_component": row.salary_component,
                    "amount": row.amount
                })

            deductions = []

            for row in slip_doc.deductions:
                deductions.append({
                    "salary_component": row.salary_component,
                    "amount": row.amount
                })

            return {
                "status": "success",
                "employee": {
                    "employee_id": employee.name,
                    "employee_number": employee_number,
                    "employee_name": employee.employee_name,
                    "department": employee.department,
                    "designation": employee.designation,
                    "company": employee.company
                },
                "salary_slip": {
                    "id": slip_doc.name,
                    "salary_structure": slip_doc.salary_structure,
                    "payroll_frequency": slip_doc.payroll_frequency,
                    "start_date": slip_doc.start_date,
                    "end_date": slip_doc.end_date,
                    "payment_days": slip_doc.payment_days,
                    "leave_without_pay": slip_doc.leave_without_pay,
                    "gross_pay": slip_doc.gross_pay,
                    "total_deduction": slip_doc.total_deduction,
                    "net_pay": slip_doc.net_pay,
                    "rounded_total": slip_doc.rounded_total,
                    "currency": slip_doc.currency,
                    "status": slip_doc.status,
                    "earnings": earnings,
                    "deductions": deductions
                }
            }

        # -----------------------------------
        # List All Salary Slips
        # -----------------------------------
        return {
            "status": "success",
            "employee": {
                "employee_id": employee.name,
                "employee_number": employee_number,
                "employee_name": employee.employee_name,
                "department": employee.department,
                "designation": employee.designation,
                "company": employee.company
            },
            "count": len(salary_slips),
            "salary_slips": salary_slips
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Salary Slip API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def download_salary_slip_pdf():

    try:

        # -----------------------------------
        # GET Params
        # -----------------------------------
        salary_slip = frappe.form_dict.get("salary_slip")

        # -----------------------------------
        # POST JSON
        # -----------------------------------
        if not salary_slip:

            data = frappe.request.get_json(silent=True) or {}

            salary_slip = data.get("salary_slip")

        if not salary_slip:

            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "salary_slip is required"
            }

        # -----------------------------------
        # Get PDF
        # -----------------------------------
        pdf = frappe.get_print(
            "Salary Slip",
            salary_slip,
            as_pdf=True
        )

        frappe.local.response.filename = f"{salary_slip}.pdf"
        frappe.local.response.filecontent = pdf
        frappe.local.response.type = "pdf"

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Salary Slip PDF API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def get_employee_salary_profile():

    try:

        # -----------------------------------
        # GET Params
        # -----------------------------------
        employee_number = frappe.form_dict.get("employee_number")

        # -----------------------------------
        # POST JSON
        # -----------------------------------
        if not employee_number:

            data = frappe.request.get_json(silent=True) or {}

            employee_number = data.get("employee_number")

        # -----------------------------------
        # Validate
        # -----------------------------------
        if not employee_number:

            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # -----------------------------------
        # Employee Details
        # -----------------------------------
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            [
                "name",
                "employee_name",
                "employee_number",
                "company",
                "department",
                "designation",
                "branch",
                "date_of_joining",
                "reports_to",
                "leave_approver",
                "expense_approver",
                "holiday_list",
                "default_shift",
                "pan_number",
                "provident_fund_account",
                "bank_name",
                "bank_ac_no",
                "ifsc_code",
                "salary_mode",
                "ctc",
                "image",
                "user_id"
            ],
            as_dict=True
        )

        if not employee:

            frappe.local.response["http_status_code"] = 404

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # -----------------------------------
        # Reporting Manager Name
        # -----------------------------------
        reporting_manager = ""

        if employee.reports_to:

            reporting_manager = frappe.db.get_value(
                "Employee",
                employee.reports_to,
                "employee_name"
            )

        # -----------------------------------
        # Salary Structure
        # -----------------------------------
        salary_structure = frappe.db.get_value(
            "Salary Structure Assignment",
            {
                "employee": employee.name,
                "docstatus": 1
            },
            [
                "salary_structure",
                "base"
            ],
            as_dict=True,
            order_by="from_date desc"
        )

        # -----------------------------------
        # Response
        # -----------------------------------
        return {
            "status": "success",
            "employee_profile": {
                "employee_id": employee.name,
                "employee_name": employee.employee_name,
                "employee_number": employee.employee_number,
                "company": employee.company,
                "department": employee.department,
                "designation": employee.designation,
                "branch": employee.branch,
                "date_of_joining": employee.date_of_joining,
                "reporting_manager": reporting_manager,
                "leave_approver": employee.leave_approver,
                "expense_approver": employee.expense_approver,
                "holiday_list": employee.holiday_list,
                "default_shift": employee.default_shift,
                "pan_number": employee.pan_number,
                "provident_fund_account": employee.provident_fund_account,
                "bank_name": employee.bank_name,
                "bank_account_number": employee.bank_ac_no,
                "ifsc_code": employee.ifsc_code,
                "salary_mode": employee.salary_mode,
                "ctc": employee.ctc,
                "salary_structure": salary_structure.salary_structure if salary_structure else "",
                "base_salary": salary_structure.base if salary_structure else 0,
                "employee_image": employee.image,
                "user_id": employee.user_id
            }
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Employee Salary Profile API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }


@frappe.whitelist(allow_guest=True)
def get_employee_salary_structure():

    try:

        # -----------------------------------
        # GET Params
        # -----------------------------------
        employee_number = frappe.form_dict.get("employee_number")

        # -----------------------------------
        # POST JSON
        # -----------------------------------
        if not employee_number:

            data = frappe.request.get_json(silent=True) or {}

            employee_number = data.get("employee_number")

        # -----------------------------------
        # Validate
        # -----------------------------------
        if not employee_number:

            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # -----------------------------------
        # Employee
        # -----------------------------------
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            [
                "name",
                "employee_name",
                "company",
                "designation",
                "department"
            ],
            as_dict=True
        )

        if not employee:

            frappe.local.response["http_status_code"] = 404

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # -----------------------------------
        # Salary Structure Assignment
        # -----------------------------------
        assignment = frappe.db.get_value(
            "Salary Structure Assignment",
            {
                "employee": employee.name,
                "docstatus": 1
            },
            [
                "name",
                "salary_structure",
                "from_date",
                "base",
                "variable"
            ],
            as_dict=True,
            order_by="from_date desc"
        )

        if not assignment:

            return {
                "status": "error",
                "message": "Salary Structure not assigned"
            }

        # -----------------------------------
        # Salary Structure Document
        # -----------------------------------
        structure_doc = frappe.get_doc(
            "Salary Structure",
            assignment.salary_structure
        )

        earnings = []

        for row in structure_doc.earnings:

            earnings.append({
                "salary_component": row.salary_component,
                "abbr": row.abbr,
                "amount": row.amount,
                "condition": row.condition,
                "formula": row.formula
            })

        deductions = []

        for row in structure_doc.deductions:

            deductions.append({
                "salary_component": row.salary_component,
                "abbr": row.abbr,
                "amount": row.amount,
                "condition": row.condition,
                "formula": row.formula
            })

        # -----------------------------------
        # Response
        # -----------------------------------
        return {
            "status": "success",
            "employee": {
                "employee_id": employee.name,
                "employee_number": employee_number,
                "employee_name": employee.employee_name,
                "company": employee.company,
                "designation": employee.designation,
                "department": employee.department
            },
            "salary_structure": {
                "assignment_id": assignment.name,
                "salary_structure": assignment.salary_structure,
                "from_date": assignment.from_date,
                "base_salary": assignment.base,
                "variable_amount": assignment.variable,
                "currency": structure_doc.currency,
                "is_active": structure_doc.is_active,
                "payroll_frequency": structure_doc.payroll_frequency,
                "earnings": earnings,
                "deductions": deductions
            }
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Employee Salary Structure API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def get_employee_salary_components():

    try:

        # -----------------------------------
        # GET Params
        # -----------------------------------
        employee_number = frappe.form_dict.get("employee_number")

        # -----------------------------------
        # POST JSON
        # -----------------------------------
        if not employee_number:

            data = frappe.request.get_json(silent=True) or {}

            employee_number = data.get("employee_number")

        # -----------------------------------
        # Validate
        # -----------------------------------
        if not employee_number:

            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # -----------------------------------
        # Employee
        # -----------------------------------
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            [
                "name",
                "employee_name",
                "company",
                "designation",
                "department"
            ],
            as_dict=True
        )

        if not employee:

            frappe.local.response["http_status_code"] = 404

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # -----------------------------------
        # Salary Structure Assignment
        # -----------------------------------
        assignment = frappe.db.get_value(
            "Salary Structure Assignment",
            {
                "employee": employee.name,
                "docstatus": 1
            },
            [
                "salary_structure"
            ],
            as_dict=True,
            order_by="from_date desc"
        )

        if not assignment:

            return {
                "status": "error",
                "message": "Salary Structure not assigned"
            }

        # -----------------------------------
        # Salary Structure
        # -----------------------------------
        structure_doc = frappe.get_doc(
            "Salary Structure",
            assignment.salary_structure
        )

        salary_components = []

        # -----------------------------------
        # Earnings Components
        # -----------------------------------
        for row in structure_doc.earnings:

            component = frappe.db.get_value(
                "Salary Component",
                row.salary_component,
                [
                    "salary_component_abbr",
                    "type",
                    "depends_on_payment_days",
                    "is_tax_applicable",
                    "statistical_component",
                    "description"
                ],
                as_dict=True
            )

            salary_components.append({
                "component_type": "Earning",
                "salary_component": row.salary_component,
                "abbr": component.salary_component_abbr if component else "",
                "amount": row.amount,
                "formula": row.formula,
                "condition": row.condition,
                "depends_on_payment_days": component.depends_on_payment_days if component else 0,
                "is_tax_applicable": component.is_tax_applicable if component else 0,
                "statistical_component": component.statistical_component if component else 0,
                "description": component.description if component else ""
            })

        # -----------------------------------
        # Deduction Components
        # -----------------------------------
        for row in structure_doc.deductions:

            component = frappe.db.get_value(
                "Salary Component",
                row.salary_component,
                [
                    "salary_component_abbr",
                    "type",
                    "depends_on_payment_days",
                    "is_tax_applicable",
                    "statistical_component",
                    "description"
                ],
                as_dict=True
            )

            salary_components.append({
                "component_type": "Deduction",
                "salary_component": row.salary_component,
                "abbr": component.salary_component_abbr if component else "",
                "amount": row.amount,
                "formula": row.formula,
                "condition": row.condition,
                "depends_on_payment_days": component.depends_on_payment_days if component else 0,
                "is_tax_applicable": component.is_tax_applicable if component else 0,
                "statistical_component": component.statistical_component if component else 0,
                "description": component.description if component else ""
            })

        # -----------------------------------
        # Response
        # -----------------------------------
        return {
            "status": "success",
            "employee": {
                "employee_id": employee.name,
                "employee_number": employee_number,
                "employee_name": employee.employee_name,
                "company": employee.company,
                "designation": employee.designation,
                "department": employee.department
            },
            "salary_structure": assignment.salary_structure,
            "count": len(salary_components),
            "salary_components": salary_components
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Employee Salary Components API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def create_expense_claim():

    try:

        data = frappe.request.get_json(silent=True) or {}

        employee_number = data.get("employee_number")
        expense_date = data.get("expense_date")
        expense_type = data.get("expense_type")
        amount = flt(data.get("amount") or 0)
        description = data.get("description")
        payable_account = data.get("payable_account")

        # NEW FIELD
        receipt_base64 = data.get("receipt")

        # -----------------------------------
        # VALIDATION
        # -----------------------------------
        if not employee_number:
            return {"status": "error", "message": "employee_number is required"}

        if not expense_type:
            return {"status": "error", "message": "expense_type is required"}

        if amount <= 0:
            return {"status": "error", "message": "amount should be greater than 0"}

        # -----------------------------------
        # GET EMPLOYEE
        # -----------------------------------
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["name", "employee_name", "expense_approver", "company"],
            as_dict=True
        )

        if not employee:
            return {"status": "error", "message": "Employee not found"}

        if not employee.expense_approver:
            return {"status": "error", "message": "Expense Approver not configured"}

        company_cost_center = frappe.db.get_value(
            "Company",
            employee.company,
            "cost_center"
        )

        if not company_cost_center:
            return {
                "status": "error",
                "message": f"Cost Center not configured for company {employee.company}"
            }

        # -----------------------------------
        # CREATE EXPENSE CLAIM DOC
        # -----------------------------------
        expense_doc = frappe.get_doc({
            "doctype": "Expense Claim",
            "employee": employee.name,
            "employee_name": employee.employee_name,
            "company": employee.company,
            "expense_approver": employee.expense_approver,
            "approval_status": "Draft",
            "payable_account": payable_account,
            "expenses": []
        })

        # -----------------------------------
        # HANDLE RECEIPT
        # -----------------------------------
        receipt_file_url = None
        receipt_base64_return = None

        if receipt_base64:

            receipt_file_url, receipt_base64_return = save_base64_receipt(
                receipt_base64,
                employee.name
            )

        # -----------------------------------
        # APPEND CHILD ROW
        # -----------------------------------
        expense_doc.append("expenses", {
            "expense_date": expense_date,
            "expense_type": expense_type,
            "amount": amount,
            "description": description,
            "sanctioned_amount": amount,
            "cost_center": company_cost_center,

            # IMPORTANT FIELD IN CHILD TABLE
            "receipt": receipt_file_url
        })

        expense_doc.insert(ignore_permissions=True)
        frappe.db.commit()

        send_expense_approval_email(expense_doc.name)

        return {
            "status": "success",
            "message": "Expense Claim created successfully",
            "expense_claim": expense_doc.name,
            "approval_status": expense_doc.approval_status,
            "expense_approver": employee.expense_approver,

            # return preview base64
            "receipt_preview_base64": receipt_base64_return
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Create Expense Claim API Error")
        return {"status": "error", "message": str(e)}

def save_base64_receipt(base64_data, expense_claim_name):

    try:

        if not base64_data:
            return None, None

        # -----------------------------------
        # HANDLE BASE64 HEADER
        # -----------------------------------
        if "," in base64_data:
            header, encoded = base64_data.split(",", 1)
            mime_type = header.split(";")[0].replace("data:", "")
        else:
            encoded = base64_data
            mime_type = "application/octet-stream"

        # -----------------------------------
        # DETECT EXTENSION
        # -----------------------------------
        extension = mimetypes.guess_extension(mime_type)

        if not extension:
            extension = ".bin"

        file_content = base64.b64decode(encoded)

        file_name = f"{expense_claim_name}_receipt{extension}"

        # -----------------------------------
        # SAVE FILE IN FRAPPE
        # -----------------------------------
        saved_file = save_file(
            fname=file_name,
            content=file_content,
            dt="Expense Claim",
            dn=expense_claim_name,
            is_private=0
        )

        # -----------------------------------
        # CONVERT BACK TO BASE64 (FOR UI PREVIEW)
        # -----------------------------------
        encoded_preview = base64.b64encode(file_content).decode("utf-8")

        return saved_file.file_url, f"data:{mime_type};base64,{encoded_preview}"

    except Exception:
        frappe.log_error(frappe.get_traceback(), "Save Receipt Error")
        return None, None

def send_expense_approval_email(expense_claim_name):

    doc = frappe.get_doc("Expense Claim", expense_claim_name)

    # -----------------------------------
    # ERP LINK
    # -----------------------------------
    erp_link = f"http://198.204.236.82/app/expense-claim/{doc.name}"

    # -----------------------------------
    # ATTACHMENTS
    # -----------------------------------
    attachments = []

    for row in doc.expenses:

        if row.receipt:

            attachments.append({
                "file_url": row.receipt
            })

    # -----------------------------------
    # EMAIL
    # -----------------------------------
    sendmail(
        recipients=[doc.expense_approver],
        subject=f"Expense Approval Request - {doc.name}",
        message=f"""
        <p>Dear sir/madam,</p>

        <p>Expense claim is waiting for your approval.</p>


        <p><b>Employee:</b> {doc.employee_name}</p>
        <p><b>Company:</b> {doc.company}</p>
        <p><b>Total Amount:</b> {doc.total_claimed_amount}</p>


        <p>
            👉 <a href="{erp_link}" target="_blank">
                Click here to open Expense Claim in Web
            </a>
        </p>


        """,
        attachments=attachments
    )

@frappe.whitelist(allow_guest=True)
def approve_reject_expense_claim():

    try:

        data = frappe.request.get_json(
            silent=True
        ) or {}

        expense_claim = data.get(
            "expense_claim"
        )

        action = data.get(
            "action"
        )

        remarks = data.get(
            "remarks"
        )

        # -----------------------------------
        # VALIDATION
        # -----------------------------------
        if not expense_claim:

            return {
                "status": "error",
                "message":
                    "expense_claim is required"
            }

        if not action:

            return {
                "status": "error",
                "message":
                    "action is required"
            }

        action = action.title()

        expense_doc = frappe.get_doc(
            "Expense Claim",
            expense_claim
        )

        # ===================================
        # APPROVED
        # ===================================
        if action == "Approved":

            # -----------------------------------
            # SET APPROVAL STATUS
            # BEFORE SUBMIT
            # -----------------------------------
            expense_doc.approval_status = (
                "Approved"
            )

            # -----------------------------------
            # UPDATE SANCTIONED AMOUNT
            # -----------------------------------
            total_amount = 0

            for row in expense_doc.expenses:

                row.sanctioned_amount = (
                    row.amount
                )

                total_amount += flt(
                    row.amount
                )

            # -----------------------------------
            # TOTALS
            # -----------------------------------
            expense_doc.total_sanctioned_amount = (
                total_amount
            )

            expense_doc.grand_total = (
                total_amount
            )

            # -----------------------------------
            # SAVE BEFORE SUBMIT
            # -----------------------------------
            expense_doc.save(
                ignore_permissions=True
            )

            # -----------------------------------
            # SUBMIT
            # -----------------------------------
            if expense_doc.docstatus == 0:

                expense_doc.submit()

            # -----------------------------------
            # COMMENTS
            # -----------------------------------
            if remarks:

                expense_doc.add_comment(
                    "Comment",
                    remarks
                )

            frappe.db.commit()

            return {

                "status": "success",

                "message":
                    "Expense Claim Approved successfully",

                "expense_claim":
                    expense_doc.name,

                "approval_status":
                    expense_doc.approval_status,

                "docstatus":
                    expense_doc.docstatus,

                "grand_total":
                    expense_doc.grand_total
            }

        # ===================================
        # REJECTED
        # ===================================
        elif action == "Rejected":

            expense_doc.approval_status = (
                "Rejected"
            )

            expense_doc.save(
                ignore_permissions=True
            )

            if remarks:

                expense_doc.add_comment(
                    "Comment",
                    remarks
                )

            frappe.db.commit()

            return {

                "status": "success",

                "message":
                    "Expense Claim Rejected successfully",

                "expense_claim":
                    expense_doc.name,

                "approval_status":
                    expense_doc.approval_status
            }

        else:

            return {
                "status": "error",
                "message":
                    "Invalid action"
            }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Approve Reject Expense Claim API Error"
        )

        return {
            "status": "error",
            "message": str(e)
        }



@frappe.whitelist(allow_guest=True)
def get_employee_expenses():

    try:

        employee_number = frappe.form_dict.get(
            "employee_number"
        )

        expense_claim = frappe.form_dict.get(
            "expense_claim"
        )

        approval_status = frappe.form_dict.get(
            "approval_status"
        )

        # -----------------------------------
        # VALIDATE
        # -----------------------------------
        if not employee_number:

            frappe.local.response[
                "http_status_code"
            ] = 400

            return {
                "status": "error",
                "message":
                    "employee_number is required"
            }

        # -----------------------------------
        # GET EMPLOYEE
        # -----------------------------------
        employee = frappe.db.get_value(
            "Employee",
            {
                "employee_number":
                    employee_number
            },
            [
                "name",
                "employee_name",
                "company"
            ],
            as_dict=True
        )

        if not employee:

            frappe.local.response[
                "http_status_code"
            ] = 404

            return {
                "status": "error",
                "message":
                    "Employee not found"
            }

        # ===================================
        # SINGLE EXPENSE DETAIL
        # ===================================
        if expense_claim:

            expense_doc = frappe.get_doc(
                "Expense Claim",
                expense_claim
            )

            # if expense_doc.employee != employee.name:

            #     frappe.local.response[
            #         "http_status_code"
            #     ] = 403

            #     return {
            #         "status": "error",
            #         "message":
            #             "Unauthorized access"
            #     }

            expenses = []

            for row in expense_doc.expenses:

                expenses.append({
                    "expense_date":
                        row.expense_date,

                    "expense_type":
                        row.expense_type,

                    "description":
                        row.description,

                    "amount":
                        row.amount,

                    "sanctioned_amount":
                        row.sanctioned_amount,

                    "default_account":
                        row.default_account,

                    "cost_center":
                        row.cost_center
                })

            return {
                "status": "success",
                "expense_claim": {
                    "name":
                        expense_doc.name,

                    "employee":
                        expense_doc.employee,

                    "employee_name":
                        expense_doc.employee_name,

                    "company":
                        expense_doc.company,

                    "expense_approver":
                        expense_doc.expense_approver,

                    "approval_status":
                        expense_doc.approval_status,

                    "posting_date":
                        expense_doc.posting_date,

                    "payable_account":
                        expense_doc.payable_account,

                    "total_claimed_amount":
                        expense_doc.total_claimed_amount,

                    "total_sanctioned_amount":
                        expense_doc.total_sanctioned_amount,

                    "remark":
                        expense_doc.remark,

                    "expenses":
                        expenses
                }
            }

        # ===================================
        # EXPENSE LIST
        # ===================================
        filters = {
            "employee": employee.name
        }

        if approval_status:

            filters["approval_status"] = (
                approval_status
            )

        expense_list = frappe.get_all(
            "Expense Claim",
            filters=filters,
            fields=[
                "name",
                "posting_date",
                "approval_status",
                "total_claimed_amount",
                "total_sanctioned_amount",
                "expense_approver",
                "company",
                "creation"
            ],
            order_by="creation desc"
        )

        return {
            "status": "success",
            "employee": {
                "employee_id":
                    employee.name,

                "employee_number":
                    employee_number,

                "employee_name":
                    employee.employee_name,

                "company":
                    employee.company
            },

            "count":
                len(expense_list),

            "expenses":
                expense_list
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Get Employee Expenses API Error"
        )

        frappe.local.response[
            "http_status_code"
        ] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def get_company_expenses():

    try:

        employee_number = frappe.form_dict.get(
            "employee_number"
        )

        approval_status = frappe.form_dict.get(
            "approval_status"
        )

        # -----------------------------------
        # VALIDATE
        # -----------------------------------
        if not employee_number:

            frappe.local.response[
                "http_status_code"
            ] = 400

            return {
                "status": "error",
                "message":
                    "employee_number is required"
            }

        # -----------------------------------
        # GET EMPLOYEE
        # -----------------------------------
        employee = frappe.db.get_value(
            "Employee",
            {
                "employee_number":
                    employee_number
            },
            [
                "name",
                "employee_name",
                "company"
            ],
            as_dict=True
        )

        if not employee:

            frappe.local.response[
                "http_status_code"
            ] = 404

            return {
                "status": "error",
                "message":
                    "Employee not found"
            }

        # -----------------------------------
        # FILTERS
        # -----------------------------------
        filters = {
            "company": employee.company
        }

        if approval_status:

            filters["approval_status"] = (
                approval_status
            )

        # -----------------------------------
        # GET ALL COMPANY EXPENSES
        # -----------------------------------
        expense_list = frappe.get_all(
            "Expense Claim",
            filters=filters,
            fields=[
                "name",
                "employee",
                "employee_name",
                "company",
                "posting_date",
                "approval_status",
                "total_claimed_amount",
                "total_sanctioned_amount",
                "expense_approver",
                "creation"
            ],
            order_by="creation desc"
        )

        return {
            "status": "success",

            "company":
                employee.company,

            "requested_by": {
                "employee_id":
                    employee.name,

                "employee_number":
                    employee_number,

                "employee_name":
                    employee.employee_name
            },

            "count":
                len(expense_list),

            "expenses":
                expense_list
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Get Company Expenses API Error"
        )

        frappe.local.response[
            "http_status_code"
        ] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def get_company_expense_claim_types():

    try:

        employee_number = frappe.form_dict.get(
            "employee_number"
        )

        # -----------------------------------
        # VALIDATE
        # -----------------------------------
        if not employee_number:

            frappe.local.response[
                "http_status_code"
            ] = 400

            return {
                "status": "error",
                "message":
                    "employee_number is required"
            }

        # -----------------------------------
        # GET EMPLOYEE
        # -----------------------------------
        employee = frappe.db.get_value(
            "Employee",
            {
                "employee_number":
                    employee_number
            },
            [
                "name",
                "employee_name",
                "company"
            ],
            as_dict=True
        )

        if not employee:

            frappe.local.response[
                "http_status_code"
            ] = 404

            return {
                "status": "error",
                "message":
                    "Employee not found"
            }

        # -----------------------------------
        # GET EXPENSE CLAIM TYPES
        # -----------------------------------
        expense_types = frappe.get_all(
            "Expense Claim Type",
            fields=[
                "name"
            ],
            order_by="name asc"
        )

        final_data = []

        for exp in expense_types:

            expense_doc = frappe.get_doc(
                "Expense Claim Type",
                exp.name
            )

            company_accounts = []

            for row in expense_doc.accounts:

                if row.company == employee.company:

                    company_accounts.append({
                        "company":
                            row.company,

                        "default_account":
                            row.default_account
                    })

            # ONLY APPEND IF COMPANY MATCH EXISTS
            if company_accounts:

                final_data.append({
                    "expense_claim_type":
                        expense_doc.name,

                    "accounts":
                        company_accounts
                })

        return {
            "status": "success",

            "company":
                employee.company,

            "requested_by": {
                "employee_id":
                    employee.name,

                "employee_number":
                    employee_number,

                "employee_name":
                    employee.employee_name
            },

            "count":
                len(final_data),

            "expense_claim_types":
                final_data
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Get Company Expense Claim Types API Error"
        )

        frappe.local.response[
            "http_status_code"
        ] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def get_pending_expenses_for_approver():

    try:

        employee_number = frappe.form_dict.get("employee_number")

        if not employee_number:
            data = frappe.request.get_json(silent=True) or {}
            employee_number = data.get("employee_number")

        if not employee_number:
            frappe.local.response["http_status_code"] = 400
            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # -----------------------------------
        # GET EMPLOYEE
        # -----------------------------------
        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["name", "employee_name"],
            as_dict=True
        )

        if not employee:
            frappe.local.response["http_status_code"] = 404
            return {
                "status": "error",
                "message": "Employee not found"
            }

        # -----------------------------------
        # GET PENDING EXPENSES
        # -----------------------------------
        pending_expenses = frappe.get_all(
            "Expense Claim",
            filters={
                "expense_approver": frappe.db.get_value("Employee", employee.name, "user_id"),
                "approval_status": "Draft"
            },
            fields=[
                "name",
                "employee",
                "employee_name",
                "company",
                "posting_date",
                "total_claimed_amount",
                "total_sanctioned_amount",
                "approval_status"
            ],
            order_by="creation desc"
        )

        return {
            "status": "success",
            "approver": employee.name,
            "approver_name": employee.employee_name,
            "count": len(pending_expenses),
            "pending_expenses": pending_expenses
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Pending Expense Approver API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

import frappe
import json
from frappe.utils import nowdate

# =========================================================
# CREATE LEAD
# =========================================================

@frappe.whitelist(allow_guest=True)
def create_lead():

    try:

        data = json.loads(frappe.request.data)

        employee_number = data.get("employee_number")
        lead_name = data.get("lead_name")
        company_name = data.get("company_name")
        email_id = data.get("email_id")
        mobile_no = data.get("mobile_no")
        territory = data.get("territory")
        city_town = data.get("city_town")
        sales_person = data.get("sales_person")

        if not employee_number:

            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # =================================================
        # GET EMPLOYEE
        # =================================================

        employee = frappe.db.get_value(
            "Employee",
            {
                "employee_number": employee_number
            },
            [
                "name",
                "employee_name",
                "company",
                "user_id"
            ],
            as_dict=True
        )

        if not employee:

            frappe.local.response["http_status_code"] = 404

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # =================================================
        # AUTO FETCH SALES PERSON FROM LEAD CITY
        # ONLY IF UI DID NOT SEND sales_person
        # =================================================

        if not sales_person and city_town:

            sales_person = frappe.db.get_value(
                "Lead City",
                {
                    "name": city_town,
                    "company": employee.company
                },
                "sales_person"
            )

        # =================================================
        # VALIDATE SALES PERSON EXISTS
        # =================================================

        sales_person_employee = None

        if sales_person:

            sales_person_employee = frappe.db.get_value(
                "Sales Person",
                sales_person,
                "employee"
            )

            if not sales_person_employee:

                frappe.local.response["http_status_code"] = 404

                return {
                    "status": "error",
                    "message": f"Employee not linked in Sales Person: {sales_person}"
                }

        # =================================================
        # AUTO FETCH LEAD OWNER
        # =================================================

        lead_owner = frappe.db.get_value(
            "Lead Owner",
            {
                "company": employee.company
            },
            "lead_owner"
        )

        # =================================================
        # CREATE LEAD
        # =================================================

        lead = frappe.get_doc({
            "doctype": "Lead",
            "lead_name": lead_name,
            "company_name": company_name,
            "email_id": email_id,
            "mobile_no": mobile_no,
            "territory": territory,
            "city_town": city_town,
            "sales_person": sales_person,
            "lead_owner": lead_owner,
            "company": employee.company,
            "lead_created_by": employee.employee_name
        })

        lead.insert(ignore_permissions=True)

        frappe.db.commit()

        # =================================================
        # SEND MAIL TO LEAD OWNER
        # ONLY IF EMPLOYEE != LEAD OWNER
        # =================================================

        if (
            lead_owner and
            employee.user_id != lead_owner
        ):

            send_lead_notification_email(
                lead,
                recipient=lead_owner
            )

        # =================================================
        # SEND MAIL TO SALES PERSON
        # ONLY IF EMPLOYEE != SALES PERSON
        # =================================================

        if sales_person_employee:

            sales_person_user = frappe.db.get_value(
                "Employee",
                sales_person_employee,
                "user_id"
            )

            if (
                sales_person_user and
                sales_person_user != employee.user_id
            ):

                send_lead_notification_email(
                    lead,
                    recipient=sales_person_user
                )

        return {
            "status": "success",
            "message": "Lead created successfully",
            "lead": lead.name,
            "company": employee.company
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Create Lead API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }


# =========================================================
# SEND LEAD NOTIFICATION EMAIL
# =========================================================

def send_lead_notification_email(
    lead_doc,
    recipient
):

    try:

        if not recipient:
            return

        # ============================================
        # FETCH USER FULL NAME
        # ============================================

        user_full_name = frappe.db.get_value(
            "User",
            recipient,
            "full_name"
        ) or recipient

        subject = f"New Lead Generated - {lead_doc.name}"

        message = f"""
            <p>Dear {user_full_name},</p>

            <p>
                A new lead has been generated.
            </p>

            <table border="1" cellpadding="8" cellspacing="0">

                <tr>
                    <td><b>Lead ID</b></td>
                    <td>{lead_doc.name}</td>
                </tr>

                <tr>
                    <td><b>Lead Name</b></td>
                    <td>{lead_doc.lead_name}</td>
                </tr>

                <tr>
                    <td><b>Company Name</b></td>
                    <td>{lead_doc.company_name or ""}</td>
                </tr>

                <tr>
                    <td><b>Mobile Number</b></td>
                    <td>{lead_doc.mobile_no or ""}</td>
                </tr>

                <tr>
                    <td><b>Email</b></td>
                    <td>{lead_doc.email_id or ""}</td>
                </tr>

                <tr>
                    <td><b>Territory</b></td>
                    <td>{lead_doc.territory or ""}</td>
                </tr>

                <tr>
                    <td><b>City/Town</b></td>
                    <td>{lead_doc.city_town or ""}</td>
                </tr>

                <tr>
                    <td><b>Sales Person</b></td>
                    <td>{lead_doc.sales_person or ""}</td>
                </tr>

            </table>

            <br>

            <p>
                Please login to ERPNext for further follow-up.
            </p>

            <p>Thanks</p>
        """

        frappe.sendmail(
            recipients=[recipient],
            subject=subject,
            message=message
        )

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Lead Notification Mail Error"
        )

# =========================================================
# GET LEADS API
# =========================================================
@frappe.whitelist(allow_guest=True)
def get_leads():

    try:

        employee_number = frappe.form_dict.get("employee_number")
        lead_name = frappe.form_dict.get("lead_name")

        if not employee_number:

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # ==========================================
        # GET EMPLOYEE
        # ==========================================

        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            [
                "name",
                "employee_name",
                "company",
                "user_id"
            ],
            as_dict=True
        )

        if not employee:

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # ==========================================
        # GET ROLE PROFILE FROM USER
        # ==========================================

        role_profile = frappe.db.get_value(
            "User",
            employee.user_id,
            "role_profile_name"
        ) or ""

        # ==========================================
        # BASE FILTER
        # ==========================================

        filters = {
            "company": employee.company
        }

        # ==========================================
        # ROLE CHECK
        # ==========================================

        # SALES MANAGER -> ONLY HIS LEADS
        if role_profile == "Sales Manager":

            filters["sales_person"] = employee.employee_name

        # SUPER ADMIN -> ALL LEADS
        elif role_profile == "Super Admin":

            pass

        # OTHER USERS -> ACCESS DENIED
        else:

            return {
                "status": "error",
                "message": "You are not allowed to view leads"
            }

        # ==========================================
        # OPTIONAL FILTER
        # ==========================================

        if lead_name:
            filters["name"] = lead_name

        # ==========================================
        # GET LEADS
        # ==========================================

        leads = frappe.get_all(
            "Lead",
            filters=filters,
            fields=[
                "name",
                "lead_name",
                "company_name",
                "email_id",
                "mobile_no",
                "territory",
                "city_town",
                "sales_person",
                "status",
                "creation"
            ],
            order_by="creation desc"
        )

        return {
            "status": "success",
            "role_profile": role_profile,
            "total_leads": len(leads),
            "data": leads
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Get Leads API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

# =========================================================
# CONVERT LEAD TO CUSTOMER
# =========================================================

@frappe.whitelist(allow_guest=True)
def convert_lead_to_customer():

    try:

        data = json.loads(frappe.request.data)

        lead = data.get("lead")
        employee_number = data.get("employee_number")   # ✅ ADD THIS

        if not lead:
            return {"status": "error", "message": "lead is required"}

        if not employee_number:
            return {"status": "error", "message": "employee_number is required"}

        # ==========================================
        # GET EMPLOYEE
        # ==========================================

        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["name", "employee_name", "company"],
            as_dict=True
        )

        if not employee:
            return {"status": "error", "message": "Employee not found"}

        # ==========================================
        # GET LEAD DOC
        # ==========================================

        lead_doc = frappe.get_doc("Lead", lead)

        # ==========================================
        # CREATE CUSTOMER
        # ==========================================

        customer = frappe.get_doc({
            "doctype": "Customer",
            "customer_name": lead_doc.lead_name,
            "lead_name": lead_doc.name,
            "customer_type": "Company",
            "territory": lead_doc.territory,
            "mobile_no": lead_doc.mobile_no,
            "email_id": lead_doc.email_id,
            "company": lead_doc.company,
            "custom_city_town": lead_doc.city_town,
            "custom_sales_person": lead_doc.sales_person,
            "customer_created_by": employee.employee_name
        })

        customer.insert(ignore_permissions=True)

        # ==========================================
        # UPDATE LEAD STATUS
        # ==========================================

        frappe.db.set_value(
            "Lead",
            lead_doc.name,
            "status",
            "Converted"
        )

        frappe.db.commit()

        return {
            "status": "success",
            "message": "Lead converted successfully",
            "lead": lead_doc.name,
            "customer": customer.name,
            "company": lead_doc.company
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Convert Lead API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

# =========================================================
# CREATE QUOTATION FROM LEAD
# =========================================================

@frappe.whitelist(allow_guest=True)
def create_quotation_from_lead():

    try:

        data = json.loads(frappe.request.data)

        employee_number = data.get("employee_number")
        lead = data.get("lead")
        items = data.get("items")

        # ==========================================
        # VALIDATIONS
        # ==========================================

        if not employee_number:
            return {
                "status": "error",
                "message": "employee_number is required"
            }

        if not lead:
            return {
                "status": "error",
                "message": "lead is required"
            }

        if not items:
            return {
                "status": "error",
                "message": "items are required"
            }

        # ==========================================
        # GET EMPLOYEE
        # ==========================================

        employee = frappe.db.get_value(
            "Employee",
            {
                "employee_number": employee_number
            },
            [
                "name","employee_name",
                "company"
            ],
            as_dict=True
        )

        if not employee:

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # ==========================================
        # GET LEAD DOC
        # ==========================================

        lead_doc = frappe.get_doc("Lead", lead)

        # ==========================================
        # CREATE QUOTATION
        # ==========================================

        quotation = frappe.get_doc({
            "doctype": "Quotation",
            "quotation_to": "Lead",
            "party_name": lead_doc.name,
            "lead": lead_doc.name,
            "customer_name": lead_doc.lead_name,
            "company": employee.company,
            "quotation_created_by": employee.employee_name,
            "transaction_date": nowdate(),
            "territory": lead_doc.territory,
            "items": items
        })

        quotation.insert(ignore_permissions=True)

        # ==========================================
        # SUBMIT QUOTATION
        # ==========================================

        quotation.submit()

        # ==========================================
        # UPDATE LEAD STATUS
        # ==========================================

        frappe.db.set_value(
            "Lead",
            lead_doc.name,
            "status",
            "Quotation"
        )

        frappe.db.commit()

        # ==========================================
        # SEND EMAIL
        # ==========================================

        send_quotation_email(quotation)


        return {
            "status": "success",
            "message": "Quotation created from lead successfully",
            "lead": lead_doc.name,
            "quotation": quotation.name,
            "company": employee.company,
            "quotation_created_by": employee.employee_name,
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Create Quotation From Lead API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

# =========================================================
# CREATE QUOTATION API
# =========================================================

@frappe.whitelist(allow_guest=True)
def create_quotation():

    try:

        data = json.loads(frappe.request.data)

        employee_number = data.get("employee_number")
        customer = data.get("customer")
        items = data.get("items")

        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["employee_name","company"],
            as_dict=True
        )

        if not employee:

            return {
                "status": "error",
                "message": "Employee not found"
            }

        quotation = frappe.get_doc({
            "doctype": "Quotation",
            "quotation_to": "Customer",
            "party_name": customer,
            "company": employee.company,
            "quotation_created_by": employee.employee_name,
            "transaction_date": nowdate(),
            "items": items
        })

        quotation.insert(ignore_permissions=True)

        # ==========================================
        # SUBMIT QUOTATION
        # ==========================================

        quotation.submit()

        frappe.db.commit()

        # ==========================================
        # SEND EMAIL
        # ==========================================

        send_quotation_email(quotation)


        return {
            "status": "success",
            "quotation": quotation.name
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Create Quotation API Error"
        )

        return {
            "status": "error",
            "message": str(e)
        }


# =========================================================
# SEND QUOTATION EMAIL
# =========================================================

def send_quotation_email(quotation_doc):

    try:

        customer_email = ""

        # ==========================================
        # FETCH CUSTOMER EMAIL
        # ==========================================

        if quotation_doc.quotation_to == "Customer":

            customer_email = frappe.db.get_value(
                "Customer",
                quotation_doc.party_name,
                "email_id"
            )

        elif quotation_doc.quotation_to == "Lead":

            customer_email = frappe.db.get_value(
                "Lead",
                quotation_doc.party_name,
                "email_id"
            )

        if not customer_email:
            return

        # ==========================================
        # ERP DOCUMENT URL
        # ==========================================

        document_url = (
            frappe.utils.get_url()
            + f"/app/quotation/{quotation_doc.name}"
        )

        # ==========================================
        # EMAIL MESSAGE
        # ==========================================

        subject = f"Quotation Submitted - {quotation_doc.name}"

        message = f"""
            <p>Dear Customer,</p>

            <p>
                Your quotation has been submitted successfully.
            </p>

            <table border="1" cellpadding="8" cellspacing="0">

                <tr>
                    <td><b>Quotation</b></td>
                    <td>{quotation_doc.name}</td>
                </tr>

                <tr>
                    <td><b>Quotation To</b></td>
                   <td>{frappe.utils.formatdate(quotation_doc.transaction_date, "dd-MM-yyyy")}</td>
                </tr>

                <tr>
                    <td><b>Transaction Date</b></td>
                    <td>{quotation_doc.transaction_date}</td>
                </tr>

                <tr>
                    <td><b>Grand Total</b></td>
                    <td>{quotation_doc.grand_total}</td>
                </tr>


            </table>

            <br>

            <p>Thanks</p>
        """

        frappe.sendmail(
            recipients=[customer_email],
            subject=subject,
            message=message
        )

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Quotation Email Error"
        )

# =========================================================
# GET QUOTATIONS API
# =========================================================

@frappe.whitelist(allow_guest=True)
def get_quotations():

    try:

        employee_number = frappe.form_dict.get("employee_number")
        quotation = frappe.form_dict.get("quotation")

        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["company"],
            as_dict=True
        )

        if not employee:
            return {
                "status": "error",
                "message": "Employee not found"
            }

        filters = {
            "company": employee.company
        }

        if quotation:
            filters["name"] = quotation

        quotations = frappe.get_all(
            "Quotation",
            filters=filters,
            fields=[
                "name",
                "party_name",
                "transaction_date",
                "grand_total",
                "status"
            ],
            order_by="creation desc"
        )

        return {
            "status": "success",
            "data": quotations
        }

    except Exception as e:

        return {
            "status": "error",
            "message": str(e)
        }

# =========================================================
# GET MY LEADS (SALES PERSON BASED)
# =========================================================

# =========================================================
# GET CUSTOMERS API
# =========================================================

@frappe.whitelist(allow_guest=True)
def get_customers():

    try:

        employee_number = frappe.form_dict.get("employee_number")
        customer_name = frappe.form_dict.get("customer_name")

        # =================================================
        # VALIDATION
        # =================================================

        if not employee_number:

            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # =================================================
        # GET EMPLOYEE COMPANY
        # =================================================

        employee = frappe.db.get_value(
            "Employee",
            {
                "employee_number": employee_number
            },
            [
                "name",
                "company"
            ],
            as_dict=True
        )

        if not employee:

            frappe.local.response["http_status_code"] = 404

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # =================================================
        # FILTERS
        # =================================================

        filters = {
            "company": employee.company
        }

        # =================================================
        # PARTICULAR CUSTOMER FILTER
        # =================================================

        if customer_name:

            filters["customer_name"] = customer_name

        # =================================================
        # GET CUSTOMERS
        # =================================================

        customers = frappe.get_all(
            "Customer",
            filters=filters,
            fields=[
                "name",
                "customer_name",
                "customer_type",
                "territory",
                "mobile_no",
                "email_id",
                "company",
                "lead_name",
                "creation",
                "modified"
            ],
            order_by="creation desc"
        )

        return {
            "status": "success",
            "company": employee.company,
            "total_customers": len(customers),
            "data": customers
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Get Customers API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def get_my_leads():

    try:

        employee_number = frappe.form_dict.get("employee_number")
        lead_name = frappe.form_dict.get("lead_name")

        if not employee_number:

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            [
                "name",
                "employee_name",
                "company"
            ],
            as_dict=True
        )

        if not employee:

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # ==========================================
        # SALES PERSON NAME
        # ==========================================

        sales_person = employee.employee_name

        filters = {
            "company": employee.company,
            "sales_person": sales_person
        }

        # OPTIONAL FILTER
        if lead_name:
            filters["name"] = lead_name

        leads = frappe.get_all(
            "Lead",
            filters=filters,
            fields=[
                "name",
                "lead_name",
                "company_name",
                "email_id",
                "mobile_no",
                "territory",
                "city_town",
                "sales_person",
                "status",
                "creation"
            ],
            order_by="creation desc"
        )

        return {
            "status": "success",
            "sales_person": sales_person,
            "total_leads": len(leads),
            "data": leads
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Get My Leads API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

# =========================================================
# GET SALES PERSON LIST
# =========================================================

@frappe.whitelist(allow_guest=True)
def get_sales_persons():

    try:

        employee_number = frappe.form_dict.get("employee_number")

        if not employee_number:

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # ==========================================
        # GET EMPLOYEE COMPANY
        # ==========================================

        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            ["company"],
            as_dict=True
        )

        if not employee:

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # ==========================================
        # GET SALES PERSONS FROM EMPLOYEE
        # ==========================================

        sales_persons = frappe.get_all(
            "Employee",
            filters={
                "company": employee.company,
                "status": "Active"
            },
            fields=[
                "name",
                "employee_name",
                "employee_number",
                "designation"
            ],
            order_by="employee_name asc"
        )

        return {
            "status": "success",
            "company": employee.company,
            "total_sales_persons": len(sales_persons),
            "data": sales_persons
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Get Sales Persons API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def create_installation_report():

    try:

        data = frappe.request.get_json()

        # ==========================================
        # GET EMPLOYEE
        # ==========================================

        employee_number = data.get("employee_number")

        if not employee_number:

            return {
                "status": "error",
                "message": "Employee number is required"
            }

        employee = frappe.db.get_value(

            "Employee",

            {
                "employee_number": employee_number
            },

            [
                "name",
                "employee_name",
                "company"
            ],

            as_dict=True

        )

        if not employee:

            return {
                "status": "error",
                "message": "Employee not found"
            }

        company = employee.company

        # ==========================================
        # CHECK CUSTOMER
        # ==========================================

        customer_name_input = data.get(
            "customer_name"
        )

        customer = frappe.db.get_value(

            "Customer",

            {
                "customer_name": customer_name_input,
                "company": company
            },

            [
                "name",
                "customer_name"
            ],

            as_dict=True

        )

        if not customer:

            return {

                "status": "error",

                "message":

                    f"Customer '{customer_name_input}' not found"

            }

        # ==========================================
        # GENERATE OTP
        # ==========================================

        generated_otp = str(
            random.randint(1000, 9999)
        )

        # ==========================================
        # CREATE DOCUMENT
        # ==========================================

        doc = frappe.get_doc({

            "doctype": "Installation Report",

            "employee": employee.name,

            "employee_name":
                employee.employee_name,

            "customer":
                customer.name,

            "customer_name":
                customer.customer_name,

            "address":
                data.get("address"),

            "contact_person_name":
                data.get("contact_person_name"),

            "contact_number":
                data.get("contact_number"),

            "email":
                data.get("email"),

            "uw":
                data.get("uw", 0),

            "amc":
                data.get("amc", 0),

            "ow":
                data.get("ow", 0),

            "o":
                data.get("o", 0),

            "doc_no":
                data.get("doc_no"),

            "date":
                data.get("date"),

            "call_receipt_date":
                data.get("call_receipt_date"),

            "nature_of_call":
                data.get("nature_of_call"),

            "call_date_from":
                data.get("call_date_from"),

            "to":
                data.get("to"),

            "machine_type":
                data.get("machine_type"),

            "machine_model":
                data.get("machine_model"),

            "brief_of_primary_communication_with_or_by_customer":

                data.get(
                    "brief_of_primary_communication_with_or_by_customer"
                ),

            "pre_work_observation":

                data.get(
                    "pre_work_observation"
                ),

            "action_summary":

                data.get(
                    "action_summary"
                ),

            "remarks_by_customer":

                data.get(
                    "remarks_by_customer"
                ),

            "work_done_nature":

                data.get(
                    "work_done_nature",
                    0
                ),

            "system_operation":

                data.get(
                    "system_operation",
                    0
                ),

            "suggestions_for_trouble":

                data.get(
                    "suggestions_for_trouble",
                    0
                ),

            "purpose_reason":

                data.get(
                    "purpose_reason",
                    0
                ),

            # ======================================
            # OTP FIELDS
            # ======================================

            "otp":
                generated_otp,

            "otp_to_be_verified":
                generated_otp,

            "verified_otp":
                0,

            "otp_verified_on":
                None

        })

        # ==========================================
        # INSERT ONLY ONCE
        # ==========================================

        doc.insert(ignore_permissions=True)

        # ==========================================
        # SEND SMS
        # ==========================================

        # if doc.contact_number:

        #     send_otp_sms(

        #         doc.contact_number,

        #         generated_otp

        #     )

        # ==========================================
        # SEND EMAIL OTP
        # ==========================================

        if doc.email:

            send_otp_email(
                doc.email,
                generated_otp,
                doc.doc_no
            )


        frappe.db.commit()

        # ==========================================
        # RESPONSE
        # ==========================================

        return {

            "status": "success",

            "message":

                "Installation Report Created & OTP Sent",

            "name": doc.name,

            "otp_sent_to":
                doc.email

        }

    except Exception as e:

        frappe.log_error(

            frappe.get_traceback(),

            "Installation Report Create Error"

        )

        return {

            "status": "error",

            "message": str(e)

        }

# =========================================================
# EMAIL OTP FUNCTION
# =========================================================

def send_otp_email(email, otp, document_name):

    try:

        frappe.sendmail(

            recipients=[email],

            subject="PICO Automation - OTP Verification",

            message=f"""

                Dear Customer,

                Your OTP for Installation Report verification is:

                <h2>{otp}</h2>

                Document Number: {document_name}

                Please share this OTP with the service engineer to complete verification.

                Regards,
                PICO Automation

            """

        )

        frappe.log_error(

            title="EMAIL OTP SENT",

            message=f"OTP sent to {email}"

        )

        return True

    except Exception as e:

        frappe.log_error(

            frappe.get_traceback(),

            "EMAIL OTP ERROR"

        )

        return str(e)


def send_otp_sms(mobile, otp):

    try:

        url = "https://www.fast2sms.com/dev/bulkV2"

        payload = {

            "route": "q",

            "message":
                f"Your PICO Automation OTP is {otp}",

            "language": "english",

            "flash": 0,

            "numbers": mobile

        }

        headers = {

            "authorization":
                "VFlx5yu7PAD0oFganIQZeJTCe72ajQsYB5DcQn8HdPFeZQTHOzBL0Wpt3fHz"

        }

        response = requests.get(

            url,

            params=payload,

            headers=headers

        )

        response_data = response.json()

        frappe.log_error(

            title="FAST2SMS RESPONSE",

            message=str(response_data)

        )

        return response_data

    except Exception as e:

        frappe.log_error(

            frappe.get_traceback(),

            "OTP SMS Error"

        )

        return str(e)


@frappe.whitelist(allow_guest=True)
def verify_installation_report_otp():

    try:

        name = frappe.form_dict.get("name")

        otp = frappe.form_dict.get("otp")

        # ==========================================
        # VALIDATION
        # ==========================================

        if not name:

            return {
                "status": "error",
                "message": "Document name required"
            }

        if not otp:

            return {
                "status": "error",
                "message": "OTP required"
            }

        # ==========================================
        # GET DOCUMENT
        # ==========================================

        doc = frappe.get_doc(
            "Installation Report",
            name
        )

        # ==========================================
        # CHECK OTP
        # ==========================================

        if str(doc.otp_to_be_verified) != str(otp):

            return {

                "status": "error",

                "message": "OTP is not valid"

            }

        # ==========================================
        # OTP VERIFIED
        # ==========================================

        doc.verified_otp = 1

        doc.otp_verified_on = now_datetime()

        # Optional
        doc.otp = otp

        # ==========================================
        # AUTO SUBMIT DOCUMENT
        # ==========================================

        if doc.docstatus == 0:

            doc.submit()

        else:

            doc.save(ignore_permissions=True)

        frappe.db.commit()

        # ==========================================
        # SUCCESS RESPONSE
        # ==========================================

        return {

            "status": "success",

            "message":

                "OTP verified successfully and document submitted",

            "name": doc.name,

            "docstatus": doc.docstatus,

            "verified_otp": doc.verified_otp

        }

    except Exception as e:

        frappe.log_error(

            frappe.get_traceback(),

            "Verify Installation OTP Error"

        )

        return {

            "status": "error",

            "message": str(e)

        }

@frappe.whitelist(allow_guest=True)
def get_installation_report(name):

    try:

        data = frappe.db.get_value(

            "Installation Report",

            name,

            [

                "name",

                "customer_name",
                "address",
                "contact_person_name",
                "contact_number",
                "email",

                "uw",
                "amc",
                "ow",
                "o",

                "doc_no",
                "date",

                "call_receipt_date",
                "nature_of_call",

                "call_date_from",
                "to",

                "machine_type",
                "machine_model",

                "brief_of_primary_communication_with_or_by_customer",

                "pre_work_observation",

                "action_summary",

                "remarks_by_customer",

                "work_done_nature",

                "system_operation",

                "suggestions_for_trouble",

                "purpose_reason"

            ],

            as_dict=1

        )

        if not data:

            return {
                "status": "error",
                "message": "Installation Report not found"
            }

        return {
            "status": "success",
            "data": data
        }

    except Exception as e:

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def get_all_installation_reports():

    try:

        data = frappe.get_all(

            "Installation Report",

            fields=[

                "name",

                "customer_name",
                "address",
                "contact_person_name",
                "contact_number",
                "email",

                "uw",
                "amc",
                "ow",
                "o",

                "doc_no",
                "date",

                "call_receipt_date",
                "nature_of_call",

                "call_date_from",
                "to",

                "machine_type",
                "machine_model",

                "brief_of_primary_communication_with_or_by_customer",

                "pre_work_observation",

                "action_summary",

                "remarks_by_customer",

                "work_done_nature",

                "system_operation",

                "suggestions_for_trouble",

                "purpose_reason"

            ],

            order_by="creation desc"

        )

        return {
            "status": "success",
            "data": data
        }

    except Exception as e:

        return {
            "status": "error",
            "message": str(e)
        }

@frappe.whitelist(allow_guest=True)
def download_installation_report_pdf(name):

    try:

        html = frappe.get_print(

            "Installation Report",

            name,

            print_format="Installation Report"

        )

        pdf = get_pdf(html)

        frappe.local.response.filename = f"{name}.pdf"

        frappe.local.response.filecontent = pdf

        frappe.local.response.type = "download"

    except Exception as e:

        frappe.throw(str(e))

@frappe.whitelist(allow_guest=True)
def get_employee_installation_reports():

    try:

        employee_number = frappe.form_dict.get(
            "employee_number"
        )

        if not employee_number:

            return {
                "status": "error",
                "message": "Employee number is required"
            }

        # ==========================================
        # GET EMPLOYEE USING EMPLOYEE NUMBER
        # ==========================================

        employee = frappe.db.get_value(

            "Employee",

            {
                "employee_number": employee_number
            },

            [
                "name",
                "employee_name",
                "company"
            ],

            as_dict=True

        )

        if not employee:

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # ==========================================
        # ERP EMPLOYEE ID
        # ==========================================

        employee_id = employee.name

        # Example:
        # HR-PICO-EMP-00041

        # ==========================================
        # GET INSTALLATION REPORTS
        # ==========================================

        reports = frappe.get_all(

            "Installation Report",

            filters={

                "employee": employee_id

            },

            fields=[

                "name",

                "employee",

                "employee_name",

                "customer_name",

                "machine_type",

                "machine_model",

                "nature_of_call",

                "date",

                "creation"

            ],

            order_by="creation desc"

        )

        return {

            "status": "success",

            "employee_number": employee_number,

            "employee": employee_id,

            "employee_name": employee.employee_name,

            "company": employee.company,

            "count": len(reports),

            "data": reports

        }

    except Exception as e:

        frappe.log_error(

            frappe.get_traceback(),

            "Get Employee Installation Reports Error"

        )

        return {

            "status": "error",

            "message": str(e)

        }
@frappe.whitelist(allow_guest=True)
def get_items():

    try:

        employee_number = frappe.form_dict.get("employee_number")
        item_code = frappe.form_dict.get("item_code")

        if not employee_number:

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # ==========================================
        # GET EMPLOYEE COMPANY
        # ==========================================

        employee = frappe.db.get_value(
            "Employee",
            {"employee_number": employee_number},
            [
                "company"
            ],
            as_dict=True
        )

        if not employee:

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # ==========================================
        # ITEM FILTERS
        # ==========================================

        filters = {
            "custom_company": employee.company,
            "disabled": 0
        }

        # OPTIONAL ITEM FILTER
        if item_code:
            filters["item_code"] = item_code

        # ==========================================
        # GET ITEMS
        # ==========================================

        items = frappe.get_all(
            "Item",
            filters=filters,
            fields=[
                "item_code",
                "item_name",
                "custom_machine_id",
                "stock_uom",
                "valuation_rate"
            ],
            order_by="item_name asc"
        )

        final_data = []

        for item in items:

            # ==========================================
            # ITEM TAX
            # ==========================================

            taxes = frappe.get_all(
                "Item Tax",
                filters={
                    "parent": item.item_code
                },
                fields=[
                    "item_tax_template"
                ]
            )

            final_data.append({
                "item_code": item.item_code,
                "item_name": item.item_name,
                "machine_id": item.custom_machine_id,
                "uom": item.stock_uom,
                "item_price": item.valuation_rate or 0,
                "item_tax": taxes if taxes else []
            })

        return {
            "status": "success",
            "company": employee.company,
            "total_items": len(final_data),
            "data": final_data
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Get Items API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

# =========================================================
# UPDATE LEAD QUALIFICATION STATUS API
# =========================================================
# =========================================================
# UPDATE LEAD QUALIFICATION STATUS API
# =========================================================

@frappe.whitelist(allow_guest=True)
def update_lead_qualification_status():

    try:

        data = json.loads(frappe.request.data)

        lead = data.get("lead")
        qualification_status = data.get("qualification_status")

        # =================================================
        # VALIDATION
        # =================================================

        if not lead:

            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "lead is required"
            }

        if not qualification_status:

            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "qualification_status is required"
            }

        # =================================================
        # CHECK LEAD EXISTS
        # =================================================

        if not frappe.db.exists("Lead", lead):

            frappe.local.response["http_status_code"] = 404

            return {
                "status": "error",
                "message": "Lead not found"
            }

        # =================================================
        # GET CURRENT STATUS
        # =================================================

        current_status = frappe.db.get_value(
            "Lead",
            lead,
            "status"
        )

        # =================================================
        # UPDATE QUALIFICATION STATUS
        # =================================================

        lead_doc = frappe.get_doc("Lead", lead)

        lead_doc.qualification_status = qualification_status

        # SAVE WITHOUT CHANGING STATUS
        lead_doc.save(ignore_permissions=True)

        # =================================================
        # RESTORE ORIGINAL STATUS
        # =================================================

        frappe.db.set_value(
            "Lead",
            lead,
            "status",
            current_status,
            update_modified=False
        )

        frappe.db.commit()

        return {
            "status": "success",
            "message": "Qualification status updated successfully",
            "lead": lead,
            "previous_status": current_status,
            "qualification_status": qualification_status
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Update Lead Qualification Status API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return {
            "status": "error",
            "message": str(e)
        }

# =========================================================
# GET ALL LEAD TRACKING HISTORY
# =========================================================

@frappe.whitelist(allow_guest=True)
def get_all_lead_tracking_history():

    try:

        employee_number = frappe.form_dict.get("employee_number")
        lead = frappe.form_dict.get("lead")
        status = frappe.form_dict.get("status")
        from_date = frappe.form_dict.get("from_date")
        to_date = frappe.form_dict.get("to_date")

        if not employee_number:

            frappe.local.response["http_status_code"] = 400

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # =================================================
        # GET EMPLOYEE DETAILS
        # =================================================

        employee = frappe.db.get_value(
            "Employee",
            {
                "employee_number": employee_number
            },
            [
                "name",
                "employee_name",
                "company",
                "user_id"
            ],
            as_dict=True
        )

        if not employee:

            frappe.local.response["http_status_code"] = 404

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # =================================================
        # GET ROLE PROFILE FROM USER
        # =================================================

        role_profile_name = frappe.db.get_value(
            "User",
            employee.user_id,
            "role_profile_name"
        ) or ""

        role_profile_name_lower = role_profile_name.lower()



        # =================================================
        # DEFAULT FILTER
        # =================================================

        filters = {
            "company": employee.company
        }

        # PARTICULAR LEAD FILTER
        if lead:
            filters["name"] = lead

        # STATUS FILTER
        if status:
            filters["status"] = status

        # =================================================
        # ADMIN CAN SEE ALL LEADS
        # =================================================

        if "admin" not in role_profile_name_lower:

            # =================================================
            # SALES PERSON CAN SEE ONLY THEIR LEADS
            # =================================================

            filters["sales_person"] = employee.employee_name

        # =================================================
        # DATE FILTER
        # =================================================

        if from_date and to_date:
            filters["creation"] = ["between", [from_date, to_date]]

        elif from_date:
            filters["creation"] = [">=", from_date]

        elif to_date:
            filters["creation"] = ["<=", to_date]


        # =================================================
        # GET LEADS
        # =================================================

        leads = frappe.get_all(
            "Lead",
            filters=filters,
            fields=[
                "name",
                "lead_name",
                "company_name",
                "lead_created_by",
                "owner",
                "creation",
                "sales_person",
                "status",
                "qualification_status",
                "territory",
                "city_town"
            ],
            order_by="creation desc"
        )

        final_data = []

        # =================================================
        # LOOP LEADS
        # =================================================

        for lead_doc in leads:

            tracking_history = {
                "lead": lead_doc.name,
                "lead_name": lead_doc.lead_name,
                "company_name": lead_doc.company_name,
                "created_by": lead_doc.owner,
                "creation_time": lead_doc.creation,
                "sales_person": lead_doc.sales_person,
                "lead_created_by": lead_doc.lead_created_by,
                "lead_status": lead_doc.status,
                "qualification_status": lead_doc.qualification_status,
                "territory": lead_doc.territory,
                "city_town": lead_doc.city_town,
                "converted_to_customer": False,
                "customer": None,
                "quotation_created": False,
                "quotation_flow": []
            }

            # =================================================
            # CUSTOMER CHECK
            # =================================================

            customer = frappe.db.get_value(
                "Customer",
                {
                    "lead_name": lead_doc.name
                },
                [
                    "name",
                    "customer_name",
                    "creation",
                    "customer_created_by"
                ],
                as_dict=True
            )

            if customer:

                tracking_history["converted_to_customer"] = True

                tracking_history["customer"] = {
                    "customer_id": customer.name,
                    "customer_name": customer.customer_name,
                    "created_on": customer.creation,
                    "customer_created_by": customer.customer_created_by if customer else None
                }

            quotation_data = []

            # =================================================
            # DIRECT LEAD → QUOTATION
            # =================================================

            direct_quotes = frappe.get_all(
                "Quotation",
                filters={
                    "party_name": lead_doc.name
                },
                fields=[
                    "name",
                    "status",
                    "docstatus",
                    "creation",
                    "transaction_date",
                    "grand_total",
                    "quotation_created_by"
                ]
            )

            for q in direct_quotes:

                quotation_data.append({
                    "quotation": q.name,
                    "flow": "Lead → Quotation",
                    "quotation_status": q.status,
                    "docstatus": q.docstatus,
                    "created_on": q.creation,
                    "transaction_date": q.transaction_date,
                    "grand_total": q.grand_total,
                    "quotation_created_by": q.quotation_created_by
                })

            # =================================================
            # LEAD → CUSTOMER → QUOTATION
            # =================================================

            if customer:

                customer_quotes = frappe.get_all(
                    "Quotation",
                    filters={
                        "party_name": customer.name
                    },
                    fields=[
                        "name",
                        "status",
                        "docstatus",
                        "creation",
                        "transaction_date",
                        "grand_total"
                    ]
                )

                for q in customer_quotes:

                    quotation_data.append({
                        "quotation": q.name,
                        "flow": "Lead → Customer → Quotation",
                        "quotation_status": q.status,
                        "docstatus": q.docstatus,
                        "created_on": q.creation,
                        "transaction_date": q.transaction_date,
                        "grand_total": q.grand_total
                    })

            if quotation_data:

                tracking_history["quotation_created"] = True
                tracking_history["quotation_flow"] = quotation_data

            final_data.append(tracking_history)

        return final_data

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Get All Lead Tracking History API Error"
        )

        frappe.local.response["http_status_code"] = 500

        return [{
            "status": "error",
            "message": str(e)
        }]

# =========================================================
# GET ISSUES API
# =========================================================

from bs4 import BeautifulSoup

@frappe.whitelist(allow_guest=True)
def get_issues():

    try:

        employee_number = frappe.form_dict.get("employee_number")
        issue = frappe.form_dict.get("issue")
        customer = frappe.form_dict.get("customer")

        if not employee_number:

            return {
                "status": "error",
                "message": "employee_number is required"
            }

        # =================================================
        # FETCH EMPLOYEE COMPANY
        # =================================================

        employee = frappe.db.get_value(
            "Employee",
            {
                "employee_number": employee_number
            },
            [
                "name",
                "company"
            ],
            as_dict=True
        )

        if not employee:

            return {
                "status": "error",
                "message": "Employee not found"
            }

        # =================================================
        # ISSUE FILTER
        # =================================================

        filters = {
            "company": employee.company
        }

        # ---------------------------------------------
        # FILTER BY ISSUE
        # ---------------------------------------------

        if issue:
            filters["name"] = issue

        # ---------------------------------------------
        # FILTER BY CUSTOMER
        # ---------------------------------------------

        if customer:
            filters["customer"] = customer

        # =================================================
        # GET ISSUES
        # =================================================

        issues = frappe.get_all(
            "Issue",
            filters=filters,
            fields=[
                "name",
                "subject",
                "customer",
                "status",
                "priority",
                "issue_type",
                "description",
                "contact",
                "email_account",
                "creation",
                "modified"
            ],
            order_by="creation desc"
        )

        final_data = []

        # =================================================
        # LOOP ISSUES
        # =================================================

        for issue_doc in issues:

            # =============================================
            # REMOVE HTML FROM DESCRIPTION
            # =============================================

            clean_description = ""

            if issue_doc.description:

                clean_description = BeautifulSoup(
                    issue_doc.description,
                    "html.parser"
                ).get_text(separator=" ").strip()

            # =============================================
            # GET COMMENTS + ACTIVITY
            # =============================================

            comments = frappe.get_all(
                "Comment",
                filters={
                    "reference_doctype": "Issue",
                    "reference_name": issue_doc.name
                },
                fields=[
                    "name",
                    "comment_type",
                    "content",
                    "owner",
                    "creation"
                ],
                order_by="creation asc"
            )

            activity_data = []

            for c in comments:

                clean_comment = ""

                if c.content:

                    clean_comment = BeautifulSoup(
                        c.content,
                        "html.parser"
                    ).get_text(separator=" ").strip()

                activity_data.append({
                    "comment_id": c.name,
                    "comment_type": c.comment_type,
                    "comment": clean_comment,
                    "comment_by": c.owner,
                    "comment_time": c.creation
                })

            final_data.append({
                "issue": issue_doc.name,
                "subject": issue_doc.subject,
                "customer": issue_doc.customer,
                "status": issue_doc.status,
                "priority": issue_doc.priority,
                "issue_type": issue_doc.issue_type,
                "description": clean_description,
                "contact": issue_doc.contact,
                "email_account": issue_doc.email_account,
                "creation": issue_doc.creation,
                "modified": issue_doc.modified,
                "comments_and_activity": activity_data
            })

        return {
            "status": "success",
            "company": employee.company,
            "total_issues": len(final_data),
            "data": final_data
        }

    except Exception as e:

        frappe.log_error(
            frappe.get_traceback(),
            "Get Issues API Error"
        )

        return {
            "status": "error",
            "message": str(e)
        }