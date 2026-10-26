import razorpay
import frappe
from frappe.utils import now
from frappe import _

#from erpnext.hr.doctype.salary_slip.salary_slip import make_salary_slip

RAZORPAY_KEY_ID = "rzp_test_7cRAtabhzPGWWG"
RAZORPAY_KEY_SECRET = "E9vR7uptLZuL9GnQliZ7OBf5"

@frappe.whitelist()
def transfer_salary(docname):
    doc = frappe.get_doc("Salary Payment Processor", docname)
    client = razorpay.Client(auth=(RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET))

    for row in doc.employee_salary_table:
        if row.status == "Pending":
            try:
                # Step 1: Create RazorpayX contact
                contact = client.contact.create({
                    "name": row.employee_name,
                    "type": "employee",
                    "reference_id": row.employee,
                    "email": f"vasuki.r@predfut.com",
                    "contact": "8248699739"
                })

                # Step 2: Create fund account
                fund_account = client.fund_account.create({
                    "contact_id": contact["id"],
                    "account_type": "bank_account",
                    "bank_account": {
                        "name": row.employee_name,
                        "ifsc": row.ifsc_code,
                        "account_number": row.bank_account_no
                    }
                })

                # Step 3: Make payout
                payout = client.payout.create({
                    "account_number": "2323230032525252",  # Test account
                    "fund_account_id": fund_account["id"],
                    "amount": int(float(row.amount_to_pay) * 100),
                    "currency": "INR",
                    "mode": "IMPS",
                    "purpose": "salary",
                    "queue_if_low_balance": True,
                    "reference_id": row.employee,
                    "narration": "Salary Payment"
                })

                # Step 4: Mark as paid
                row.status = "Paid"

                # Step 5: Create Salary Slip (VERY IMPORTANT PART)
                payroll_entries = frappe.get_all("Payroll Entry", filters={
                    "docstatus": 1,  # Submitted
                    "company": doc.company,  # optional filter
                    "posting_date": ["like", f"{doc.payroll_year}-{str(doc.payroll_month).zfill(2)}%"]
                })

                if payroll_entries:
                    salary_slip = make_salary_slip(
                        employee=row.employee,
                        payroll_entry=payroll_entries[0].name
                    )
                    salary_slip.insert()
                    salary_slip.submit()
                    frappe.db.commit()
                else:
                    frappe.msgprint(f"No matching Payroll Entry for {row.employee} to create Salary Slip.")

            except Exception as e:
                frappe.log_error(f"Error processing salary for {row.employee}: {str(e)}")

    doc.save()

import frappe
from datetime import datetime

@frappe.whitelist()
def fetch_employee_data(docname):
    doc = frappe.get_doc("Salary Payment Processor", docname)
    doc.employees = []

    # Ensure you have a payroll_month field (e.g., '2025-06-01')
    payroll_month = frappe.utils.getdate(doc.payroll_month)
    month_start = payroll_month.replace(day=1)
    next_month = (month_start.replace(day=28) + frappe.utils.datetime.timedelta(days=4)).replace(day=1)

    # Fetch payroll entries only for that month
    payroll_entries = frappe.get_all("Payroll Entry",
        filters={
            "start_date": (">=", month_start),
            "end_date": ("<", next_month),
            "docstatus": 1
        },
        fields=["name", "posting_date"]
    )

    for entry in payroll_entries:
        # Get the employees from the Payroll Entry
        employees = frappe.get_all("Payroll Employee Detail",
            filters={"parent": entry.name},
            fields=["employee", "employee_name", "net_pay"]
        )

        for emp in employees:
            doc.append("employees", {
                "employee": emp.employee,
                "employee_name": emp.employee_name,
                "amount": emp.net_pay,
                "payroll_entry": entry.name
            })

    doc.save()
    return True

