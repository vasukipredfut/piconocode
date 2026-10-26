import frappe
from frappe.utils.pdf import get_pdf
from frappe.utils.file_manager import save_file
from frappe.utils import get_url
from frappe.utils.data import nowdate
import base64
import requests
from frappe.utils import today as get_today

@frappe.whitelist()
def send_attendance_pdf_to_whatsapp():
    # 1. Get today's attendance data
    today = nowdate()
    report_data = frappe.db.sql("""
        SELECT
            att.employee,
            emp.employee_name,
            emp.attendance_device_id,
            att.shift,
            att.in_time,
            att.out_time,
            att.working_hours,
            att.status
        FROM `tabAttendance` att
        JOIN `tabEmployee` emp ON att.employee = emp.name
        WHERE att.attendance_date = %s
        ORDER BY emp.attendance_device_id ASC
    """, (today,), as_dict=True)

    if not report_data:
        return "No attendance data for today."

    # 2. Prepare HTML table
    html = "<h3>Today's Attendance Report</h3><table border='1' style='border-collapse:collapse;'>"
    html += "<tr><th>Emp ID</th><th>Name</th><th>E.Code</th><th>Shift</th><th>In</th><th>Out</th><th>Hours</th><th>Status</th></tr>"
    for row in report_data:
        html += f"<tr><td>{row.employee}</td><td>{row.employee_name}</td><td>{row.attendance_device_id}</td>" \
                f"<td>{row.shift}</td><td>{row.in_time}</td><td>{row.out_time}</td>" \
                f"<td>{row.working_hours}</td><td>{row.status}</td></tr>"
    html += "</table>"

    # 3. Generate PDF from HTML
    pdf_bytes = get_pdf(html)

    # 4. Save file to File doctype and get public URL
    filename = f"Attendance_Report_{today}.pdf"
    date_str = get_today()
    #saved_file = save_file(filename, pdf_bytes, "Home", date_str, is_private=0)
    saved_file = save_file(filename, pdf_bytes, None, None, is_private=0)

    file_url = get_url(saved_file.file_url)

    # 5. Send WhatsApp message via Twilio API
    twilio_sid = frappe.conf.twilio_sid
    twilio_token = frappe.conf.twilio_token
    from_whatsapp = 'whatsapp:+16812215834'
    to_whatsapp = 'whatsapp:+918248699739'

    message_data = {
        'messaging_product': 'whatsapp',
        'to': to_whatsapp,
        'type': 'document',
        'document': {
            'link': file_url,
            'filename': filename
        }
    }

    response = requests.post(
        f'https://api.twilio.com/2010-04-01/Accounts/{twilio_sid}/Messages.json',
        auth=(twilio_sid, twilio_token),
        data={
            'From': from_whatsapp,
            'To': to_whatsapp,
            'Body': f"📋 Today's Attendance Report\n\nDownload: {file_url}"
        }
    )

    if response.status_code == 201 or response.status_code == 200:
        return "Report sent via WhatsApp successfully!"
    else:
        return f"Failed to send message: {response.text}"



import frappe
from frappe.utils.pdf import get_pdf
from frappe.utils.file_manager import save_file
from frappe.utils import get_url
from frappe.utils.data import nowdate
import requests

@frappe.whitelist()
def send_attendance_report_to_whatsapp_old():
    today = nowdate()

    report_data = frappe.db.sql("""
        SELECT
            att.employee,
            emp.employee_name,
            emp.attendance_device_id,
            att.shift,
            att.in_time,
            att.out_time,
            att.working_hours,
            att.status
        FROM `tabAttendance` att
        JOIN `tabEmployee` emp ON att.employee = emp.name
        WHERE att.attendance_date = %s
        ORDER BY emp.attendance_device_id ASC
    """, (today,), as_dict=True)

    if not report_data:
        return "No attendance data found for today."

    html = "<h3>Today's Attendance Report</h3><table border='1' style='border-collapse:collapse;'>"
    html += "<tr><th>Emp ID</th><th>Name</th><th>E.Code</th><th>Shift</th><th>In</th><th>Out</th><th>Hours</th><th>Status</th></tr>"
    for row in report_data:
        html += f"<tr><td>{row.employee}</td><td>{row.employee_name}</td><td>{row.attendance_device_id}</td><td>{row.shift}</td><td>{row.in_time}</td><td>{row.out_time}</td><td>{row.working_hours}</td><td>{row.status}</td></tr>"
    html += "</table>"

    pdf_bytes = get_pdf(html)
    filename = f"Attendance_Report_{today}.pdf"
    date_str = get_today()
    #saved_file = save_file(filename, pdf_bytes, "Home", date_str, is_private=0)
    saved_file = save_file(filename, pdf_bytes, None, None, is_private=0)

    file_url = get_url(saved_file.file_url)

    # Twilio credentials
    twilio_sid = frappe.conf.twilio_sid
    twilio_token = frappe.conf.twilio_token
    from_whatsapp = "whatsapp:+14155238886"
    to_whatsapp = "whatsapp:+918248699739"

    response = requests.post(
        f"https://api.twilio.com/2010-04-01/Accounts/{twilio_sid}/Messages.json",
        auth=(twilio_sid, twilio_token),
        data={
            "From": from_whatsapp,
            "To": to_whatsapp,
            "Body": f"📋 Today's Attendance Report\nDownload PDF: {file_url}"
        }
    )

    if response.status_code in [200, 201]:
        return "✅ Message sent via WhatsApp!"
    else:
        return f"❌ Failed: {response.status_code} - {response.text}"


import frappe
from frappe.utils.data import nowdate
import requests

@frappe.whitelist()
def send_attendance_report_to_whatsapp():
    today = nowdate()

    report_data = frappe.db.sql("""
        SELECT
            att.employee,
            emp.employee_name,
            emp.attendance_device_id,
            att.shift,
            att.in_time,
            att.out_time,
            att.working_hours,
            att.status
        FROM `tabAttendance` att
        JOIN `tabEmployee` emp ON att.employee = emp.name
        WHERE att.attendance_date = %s
        ORDER BY emp.attendance_device_id ASC
    """, (today,), as_dict=True)

    if not report_data:
        return "No attendance data found for today."

    # Format message text
    message = f"📋 *Attendance Report - {today}*\n\n"
    message += "*EmpID | Name | Code | Shift | In | Out | Hrs | Status*\n"
    message += "--------------------------------------------------\n"

    for row in report_data:
        message += f"{row.employee} | {row.employee_name} | {row.attendance_device_id} | {row.shift or '-'} | {row.in_time or '-'} | {row.out_time or '-'} | {row.working_hours or '-'} | {row.status}\n"

    # Twilio credentials
    twilio_sid = frappe.conf.twilio_sid
    twilio_token = frappe.conf.twilio_token
    from_whatsapp = "whatsapp:+14155238886"  # This is Twilio sandbox number
    to_whatsapp = "whatsapp:+918248699739"   # Must be joined to sandbox

    response = requests.post(
        f"https://api.twilio.com/2010-04-01/Accounts/{twilio_sid}/Messages.json",
        auth=(twilio_sid, twilio_token),
        data={
            "From": from_whatsapp,
            "To": to_whatsapp,
            "Body": message
        }
    )

    if response.status_code in [200, 201]:
        return "✅ Message sent via WhatsApp!"
    else:
        return f"❌ Failed: {response.status_code} - {response.text}"
