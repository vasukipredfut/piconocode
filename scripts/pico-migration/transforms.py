"""Pure ERPNext -> Aria value mappings used by migrate.py (no database access; see test_transforms.py)."""
import html
import re
from datetime import date, datetime
from decimal import ROUND_HALF_UP, Decimal

# ---------------------------------------------------------------- generic

def slug_code(name, max_len, taken):
    """Upper-case code of at most max_len chars from an ERPNext name, unique against `taken` (a set, updated)."""
    base = re.sub(r"[^A-Z0-9]+", "_", (name or "").upper()).strip("_") or "X"
    code = base[:max_len]
    n = 2
    while code in taken:
        suffix = "_" + str(n)
        code = base[: max_len - len(suffix)] + suffix
        n += 1
    taken.add(code)
    return code


def clip(value, max_len):
    if value is None:
        return None
    s = str(value).strip()
    return s[:max_len] if s else None


def truthy(v):
    if isinstance(v, (bytes, bytearray)):
        return any(v)
    if isinstance(v, str):
        return v.strip().lower() in ("1", "true", "yes")
    return bool(v)


def last10(mobile):
    d = re.sub(r"\D", "", mobile or "")
    return d[-10:] if len(d) >= 10 else None


def money(v):
    """2-decimal amount, rounded half-up like the Aria Java code (Decimal's default is half-even)."""
    return Decimal(str(v or 0)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


def period_of(d):
    """YYYY-MM of a date / datetime / 'YYYY-MM-DD' string."""
    if isinstance(d, (date, datetime)):
        return d.strftime("%Y-%m")
    return str(d)[:7]


# ---------------------------------------------------------------- HR

def employee_status(status):
    return {"Active": "ACTIVE", "Inactive": "INACTIVE", "Suspended": "INACTIVE", "Left": "LEFT"}.get(status or "", "ACTIVE")


def attendance_status(status):
    return {"Present": "PRESENT", "Absent": "ABSENT", "Half Day": "HALF_DAY", "On Leave": "ON_LEAVE",
            "Work From Home": "WFH"}.get(status or "")


WEEKDAYS = ("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")


def weekly_off(value):
    v = (value or "").strip().upper()
    return v if v in WEEKDAYS else None


def leave_status(status, docstatus):
    """(status, cancel_status) for m7_leave_request."""
    if docstatus == 2 or status == "Cancelled":
        return "CANCELLED", "CANCELLED" if docstatus == 1 else None
    if status == "Rejected":
        return "REJECTED", None
    if status == "Cancellation Inprocess":
        return "APPROVED", "REQUESTED"
    if status == "Approved":
        return ("APPROVED", None) if docstatus == 1 else ("PENDING", None)
    return "PENDING", None


def which_half(value):
    v = (value or "").strip().lower()
    if v in ("first half", "before lunch"):
        return "FIRST_HALF"
    if v in ("second half", "after lunch"):
        return "SECOND_HALF"
    return None


def overtime_status(status):
    return "CANCELLED" if (status or "").strip().lower() in ("rejected", "cancelled") else "OPEN"


def expense_status(approval_status, status, is_paid, docstatus):
    if docstatus == 2:
        return "WITHDRAWN"
    if approval_status == "Rejected":
        return "REJECTED"
    if approval_status == "Approved":
        return "PAID" if truthy(is_paid) or status == "Paid" else "APPROVED"
    return "SUBMITTED"


def component_code(abbr, component):
    """m7_payroll_line.component_code (<= 32) from Salary Detail abbr, else salary_component."""
    return slug_code(abbr or component, 32, set())


# ---------------------------------------------------------------- CRM

def lead_stage(status):
    s = (status or "").strip().lower()
    if s in ("replied",):
        return "CONTACTED"
    if s in ("opportunity", "interested"):
        return "QUALIFIED"
    if s == "quotation":
        return "QUOTED"
    if s == "converted":
        return "WON"
    if s in ("do not contact", "lost quotation"):
        return "LOST"
    return "NEW"


def qualification(value):
    return {"unqualified": "UNQUALIFIED", "in process": "IN_PROCESS", "qualified": "QUALIFIED"}.get(
        (value or "").strip().lower(), "UNQUALIFIED")


def quote_status(status, docstatus):
    if docstatus == 0:
        return "DRAFT"
    if docstatus == 2:
        return "REJECTED"
    s = (status or "").strip().lower()
    if s == "ordered":
        return "ACCEPTED"
    if s == "lost":
        return "REJECTED"
    if s == "expired":
        return "EXPIRED"
    return "SENT"


def customer_type(value):
    return "INDIVIDUAL" if (value or "").strip().lower() == "individual" else "COMPANY"


# ---------------------------------------------------------------- service

def issue_status(status):
    s = (status or "").strip().lower()
    return {"open": "OPEN", "replied": "AWAITING_CUSTOMER", "on hold": "AWAITING_CUSTOMER", "paused": "AWAITING_CUSTOMER",
            "resolved": "RESOLVED", "closed": "CLOSED"}.get(s, "OPEN")


def issue_priority(priority):
    return {"urgent": "P1", "high": "P2", "medium": "P3", "low": "P4"}.get((priority or "").strip().lower(), "P3")


def install_status(verified_otp, docstatus):
    if docstatus == 2:
        return "CANCELLED"
    return "VERIFIED" if truthy(verified_otp) else "OTP_PENDING"


def warranty_kind(uw, amc, ow, o):
    for flag, kind in ((uw, "UW"), (amc, "AMC"), (ow, "OW"), (o, "O")):
        if truthy(flag):
            return kind
    return None


def strip_html(text):
    """ERPNext Issue / Comment bodies are HTML; Aria stores plain text."""
    if not text:
        return text
    t = re.sub(r"(?i)<br\s*/?>|</p>|</div>", "\n", str(text))
    t = re.sub(r"<[^>]+>", "", t)
    return re.sub(r"\n{3,}", "\n\n", html.unescape(t)).strip()


# ---------------------------------------------------------------- files

def sniff(head):
    """Same allow-list as Aria's UploadGuard: jpg / png / webp / pdf by magic bytes, else None."""
    if head[:3] == b"\xff\xd8\xff":
        return "jpg"
    if head[:4] == b"\x89PNG":
        return "png"
    if head[:4] == b"RIFF" and head[8:12] == b"WEBP":
        return "webp"
    if head[:5] == b"%PDF-":
        return "pdf"
    return None


def erp_file_path(url):
    """('public'|'private', file name) for an ERPNext file URL, or None if it is not one."""
    if not url:
        return None
    m = re.match(r"^(?:https?://[^/]+)?/(private/)?files/([^?#]+)$", str(url).strip())
    if not m:
        return None
    name = m.group(2)
    if "/" in name or "\\" in name or name in (".", ".."):
        return None
    return ("private" if m.group(1) else "public", name)


# ---------------------------------------------------------------- payslip snapshot

def payslip_html(company, emp, period, slip, earnings, deductions):
    """Minimal printable payslip for migrated history (Aria's PayslipController renders live ones)."""
    e = lambda v: html.escape("" if v is None else str(v))
    rows = lambda lines: "".join(f"<tr><td>{e(l['component'])}</td><td style='text-align:right'>{money(l['amount']):,.2f}</td></tr>" for l in lines)
    return ("<!DOCTYPE html><html><head><meta charset='utf-8'><title>Payslip</title></head><body style='font-family:Arial;font-size:12px'>"
            f"<h2>{e(company)}</h2><p>Payslip for <b>{e(period)}</b> (migrated from ERPNext {e(slip.get('name'))})</p>"
            f"<p><b>Employee:</b> {e(emp.get('emp_code'))} {e(emp.get('full_name'))}<br><b>Payment days:</b> {e(slip.get('payment_days'))}"
            f" &nbsp; <b>LWP:</b> {e(slip.get('leave_without_pay'))}</p>"
            "<table border='1' cellpadding='4' cellspacing='0' width='100%'><tr><th>Earnings</th><th>Amount</th></tr>"
            f"{rows(earnings)}<tr><td><b>Gross</b></td><td style='text-align:right'><b>{money(slip.get('gross_pay')):,.2f}</b></td></tr></table><br>"
            "<table border='1' cellpadding='4' cellspacing='0' width='100%'><tr><th>Deductions</th><th>Amount</th></tr>"
            f"{rows(deductions)}<tr><td><b>Total Deductions</b></td><td style='text-align:right'><b>{money(slip.get('total_deduction')):,.2f}</b></td></tr></table>"
            f"<h3>Net Pay: {money(slip.get('net_pay')):,.2f}</h3></body></html>")
