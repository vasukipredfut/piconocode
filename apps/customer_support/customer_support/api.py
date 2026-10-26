import frappe

@frappe.whitelist(allow_guest=True)
def create_issue():
    try:
        customer_name = frappe.form_dict.get("customer_name")
        mobile = frappe.form_dict.get("mobile")
        subject = frappe.form_dict.get("subject")
        description = frappe.form_dict.get("description")

        if not customer_name or not mobile or not subject or not description:
            return {"status": "error", "message": "All fields are required"}

        customer = frappe.db.exists("Customer", {"customer_name": customer_name})

        if not customer:
            return {"status": "error", "message": "Customer not found. Please contact admin."}

        issue = frappe.get_doc({
            "doctype": "Issue",
            "subject": subject,
            "customer": customer,   # ✅ Link customer properly
            "description": f"""
Customer Name : {customer_name}
Mobile : {mobile}

Issue :
{description}
            """
        })
        issue.insert(ignore_permissions=True)
        frappe.db.commit()

        return {
            "status": "success",
            "message": f"Issue Created Successfully : {issue.name}"
        }

    except Exception as e:
        frappe.log_error(frappe.get_traceback(), "Customer Support Error")
        return {"status": "error", "message": str(e)}


@frappe.whitelist(allow_guest=True)
def get_customer_issues():
    customer_name = frappe.form_dict.get("customer_name")  # ✅ Filter by customer
    mobile = frappe.form_dict.get("mobile")

    if not customer_name:
        return []

    issues = frappe.get_all(
        "Issue",
        filters={"customer": customer_name},   # ✅ Filter properly
        fields=["name", "subject", "status", "creation"],
        order_by="creation desc"
    )
    return issues