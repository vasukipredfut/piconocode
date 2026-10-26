import frappe

from frappe.utils import (
    getdate
)

from datetime import datetime


@frappe.whitelist()
def process_attendance_for_date(attendance_date=None):

    attendance_date = getdate(attendance_date)

    settings_list = frappe.get_all(
        "HR Attendance Settings",
        fields=[
            "name",
            "company",
            "shift_type",
            "full_day_before_time",
            "ot_start_time",
            "ot_multiplier",
            "holiday_ot_multiplier",
            "minimum_ot_minutes"
        ]
    )

    for settings in settings_list:

        process_company_attendance(
            settings,
            attendance_date
        )

    frappe.db.commit()

    return {
        "status": "success",
        "message": "Attendance processed successfully"
    }


def process_company_attendance(settings, attendance_date):

    employees = frappe.get_all(
        "Employee",
        filters={
            "status": "Active",
            "company": settings.company
        },
        fields=[
            "name",
            "employee_name",
            "company",
            "holiday_list",
            "default_shift"
        ]
    )

    for emp in employees:

        process_single_employee(
            emp,
            settings,
            attendance_date
        )


def process_single_employee(emp, settings, attendance_date):

    existing_attendance = frappe.db.exists(
        "Attendance",
        {
            "employee": emp.name,
            "attendance_date": attendance_date
        }
    )

    if existing_attendance:
        return

    start_datetime = f"{attendance_date} 00:00:00"
    end_datetime = f"{attendance_date} 23:59:59"

    checkins = frappe.get_all(
        "Employee Checkin",
        filters={
            "employee": emp.name,
            "time": ["between", [start_datetime, end_datetime]]
        },
        fields=[
            "name",
            "time",
            "log_type"
        ],
        order_by="time asc"
    )

    if not checkins:
        return

    first_in = None
    last_out = None

    for row in checkins:

        if row.log_type == "IN" and not first_in:
            first_in = row.time

        if row.log_type == "OUT":
            last_out = row.time

    if not first_in:
        return

    # ---------------------------------------------------
    # SHIFT CHECK
    # ---------------------------------------------------

    employee_shift = get_employee_shift(
        emp.name,
        attendance_date
    )

    # ---------------------------------------------------
    # ATTENDANCE STATUS
    # ---------------------------------------------------

    attendance_status = "Present"


    full_day_before_time = (
        datetime.min + settings.full_day_before_time
    ).time()

    allowed_in_datetime = datetime.combine(
        attendance_date,
        full_day_before_time
    )

    if first_in > allowed_in_datetime:
        attendance_status = "Half Day"

    # ---------------------------------------------------
    # HOLIDAY CHECK
    # ---------------------------------------------------

    is_holiday = check_holiday(
        emp.holiday_list,
        attendance_date
    )

    if is_holiday:
        attendance_status = "Present"

    # ---------------------------------------------------
    # CREATE ATTENDANCE
    # ---------------------------------------------------

    attendance_doc = frappe.get_doc({
        "doctype": "Attendance",
        "employee": emp.name,
        "attendance_date": attendance_date,
        "company": emp.company,
        "shift": employee_shift,
        "status": attendance_status
    })

    attendance_doc.insert(ignore_permissions=True)

    attendance_doc.submit()

    # ---------------------------------------------------
    # OVERTIME
    # ---------------------------------------------------

    if last_out:

        ot_start_time = (
            datetime.min + settings.ot_start_time
        ).time()

        ot_start_datetime = datetime.combine(
            attendance_date,
            ot_start_time
        )

        if last_out > ot_start_datetime:

            ot_minutes = (
                last_out - ot_start_datetime
            ).total_seconds() / 60

            if ot_minutes >= settings.minimum_ot_minutes:

                ot_hours = ot_minutes / 60

                hourly_salary = get_employee_hourly_salary(
                    emp.name
                )

                multiplier = (
                    settings.holiday_ot_multiplier
                    if is_holiday
                    else settings.ot_multiplier
                )

                ot_amount = (
                    ot_hours
                    * hourly_salary
                    * multiplier
                )

                create_ot_entry(
                    emp,
                    attendance_date,
                    employee_shift,
                    ot_hours,
                    ot_amount,
                    is_holiday
                )


def create_ot_entry(
    emp,
    attendance_date,
    employee_shift,
    ot_hours,
    ot_amount,
    is_holiday
):

    existing_ot = frappe.db.exists(
        "OverTime Entry",
        {
            "employee": emp.name,
            "attendance_date": attendance_date
        }
    )

    if existing_ot:
        return

    doc = frappe.get_doc({
        "doctype": "OverTime Entry",
        "employee": emp.name,
        "employee_name": emp.employee_name,
        "company": emp.company,
        "shift_type": employee_shift,
        "attendance_date": attendance_date,
        "ot_hours": round(ot_hours, 2),
        "ot_amount": round(ot_amount, 2),
        "is_holiday": is_holiday
    })

    doc.insert(ignore_permissions=True)

    doc.submit()


def check_holiday(holiday_list, attendance_date):

    if not holiday_list:
        return False

    holiday = frappe.db.exists(
        "Holiday",
        {
            "parent": holiday_list,
            "holiday_date": attendance_date
        }
    )

    return True if holiday else False


def get_employee_hourly_salary(employee):

    base_salary = frappe.db.get_value(
        "Salary Structure Assignment",
        {
            "employee": employee,
            "docstatus": 1
        },
        "base"
    ) or 0

    daily_salary = base_salary / 30

    hourly_salary = daily_salary / 8

    return hourly_salary


def get_employee_shift(employee, attendance_date):

    shift_assignment = frappe.db.get_value(
        "Shift Assignment",
        {
            "employee": employee,
            "start_date": ["<=", attendance_date],
            "docstatus": 1,
            "status": "Active"
        },
        "shift_type",
        order_by="start_date desc"
    )

    if shift_assignment:
        return shift_assignment

    default_shift = frappe.db.get_value(
        "Employee",
        employee,
        "default_shift"
    )

    return default_shift