import frappe
from frappe.utils import cint


def on_update(doc, method):

    # -----------------------------------
    # APPROVED
    # -----------------------------------
    if (
        doc.status == "Approved"
        and doc.docstatus == 0
    ):

        doc.submit()

        send_employee_mail(
            doc,
            "Approved"
        )

    # -----------------------------------
    # REJECTED
    # -----------------------------------
    elif (
        doc.status == "Rejected"
        and doc.docstatus == 0
    ):

        send_employee_mail(
            doc,
            "Rejected"
        )


def on_update_after_submit(doc, method):

    # -----------------------------------
    # PREVENT RECURSION
    # -----------------------------------
    if frappe.flags.in_leave_cancel:
        return

    # -----------------------------------
    # ONLY WHEN CANCELLED
    # -----------------------------------
    if (
        doc.status == "Cancelled"
        and doc.docstatus == 1
    ):

        frappe.flags.in_leave_cancel = True

        try:

            # -----------------------------------
            # CANCEL ATTENDANCE
            # -----------------------------------
            attendance_list = frappe.get_all(
                "Attendance",
                filters={
                    "leave_application": doc.name
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
            # STORE VALUES
            # -----------------------------------
            requested_half_day = cint(
                doc.requested_half_day or 0
            )

            requested_which_half = (
                doc.requested_which_half
            )

            half_day_date = (
                doc.half_day_date
                or doc.from_date
            )

            # -----------------------------------
            # CANCEL ORIGINAL DOC
            # -----------------------------------
            if doc.docstatus == 1:

                latest_doc = frappe.get_doc(
                    "Leave Application",
                    doc.name
                )

                latest_doc.cancel()

            # -----------------------------------
            # CREATE HALF DAY REVISED LEAVE
            # -----------------------------------
            if requested_half_day == 1:

                revised_doc = frappe.new_doc(
                    "Leave Application"
                )

                revised_doc.employee = doc.employee

                revised_doc.employee_name = (
                    doc.employee_name
                )

                revised_doc.company = (
                    doc.company
                )

                revised_doc.leave_type = (
                    doc.leave_type
                )

                revised_doc.from_date = (
                    half_day_date
                )

                revised_doc.to_date = (
                    half_day_date
                )

                revised_doc.half_day = 1

                revised_doc.half_day_date = (
                    half_day_date
                )

                revised_doc.which_half = (
                    requested_which_half
                )

                revised_doc.description = (
                    f"{doc.description or ''}"
                    f"\n\nRevised Half Day Leave"
                )

                revised_doc.leave_approver = (
                    doc.leave_approver
                )

                revised_doc.status = "Approved"

                revised_doc.insert(
                    ignore_permissions=True
                )

                revised_doc.submit()

                # -----------------------------------
                # MAIL
                # -----------------------------------
                employee_email = (
                    frappe.db.get_value(
                        "Employee",
                        doc.employee,
                        "user_id"
                    )
                )

                if employee_email:

                    frappe.sendmail(
                        recipients=[
                            employee_email
                        ],
                        subject=
                            "Half Day Leave Approved",
                        message=f"""
                            <p>
                                Dear
                                {doc.employee_name},
                            </p>

                            <p>
                                Your leave
                                <b>{doc.name}</b>
                                was cancelled.
                            </p>

                            <p>
                                Revised Half Day
                                Leave Approved.
                            </p>

                            <p>
                                <b>Date:</b>
                                {half_day_date}
                            </p>

                            <p>
                                <b>Which Half:</b>
                                {requested_which_half}
                            </p>

                            <p>
                                <b>New Leave:</b>
                                {revised_doc.name}
                            </p>
                        """
                    )

            else:

                send_employee_mail(
                    doc,
                    "Cancelled"
                )

        finally:

            frappe.flags.in_leave_cancel = False


def send_employee_mail(doc, status):

    employee_email = frappe.db.get_value(
        "Employee",
        doc.employee,
        "user_id"
    )

    if not employee_email:
        return

    frappe.sendmail(
        recipients=[employee_email],
        subject=f"Leave Application {status}",
        message=f"""
            <p>
                Dear {doc.employee_name},
            </p>

            <p>
                Your leave application
                <b>{doc.name}</b>
                has been
                <b>{status}</b>.
            </p>

            <p>
                <b>From Date:</b>
                {doc.from_date}
                <br>

                <b>To Date:</b>
                {doc.to_date}
            </p>

            <p>
                Thanks
            </p>
        """
    )
