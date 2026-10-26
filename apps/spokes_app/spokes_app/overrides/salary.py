import frappe


def add_overtime_in_salary_slip(doc, method):

    ot_entries = frappe.get_all(
        "OverTime Entry",
        filters={
            "employee": doc.employee,
            "company": doc.company,
            "attendance_date": [
                "between",
                [doc.start_date, doc.end_date]
            ],
            "status": ["!=", "Rejected"]
        },
        fields=[
            "ot_amount"
        ]
    )

    total_ot = sum(
        x.ot_amount for x in ot_entries
    )

    if total_ot > 0:

        already_added = False

        for row in doc.earnings:

            if row.salary_component == "Overtime":
                already_added = True

        if not already_added:

            doc.append(
                "earnings",
                {
                    "salary_component": "Overtime",
                    "amount": total_ot
                }
            )

        # -----------------------------------
        # SALARY WITH OT
        # -----------------------------------

        doc.salary_with_ot = (
            doc.net_pay + total_ot
        )

