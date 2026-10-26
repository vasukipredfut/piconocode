import frappe
from frappe.core.doctype.user.user import User

class CustomUser(User):
    def validate(self):
        super().validate()
        self.restrict_users_by_company()
    
    def restrict_users_by_company(self):
        if frappe.session.user == "Administrator":
            return  # Allow full access to Administrator
        
        # Get the company of the logged-in user
        user_company = frappe.get_value("Employee", {"user_id": frappe.session.user}, "company")
        
        if not user_company:
            frappe.throw("You are not linked to any company. Please contact the administrator.")

        # Restrict visibility to users linked to the same company
        allowed_users = frappe.get_all("Employee", filters={"company": user_company}, pluck="user_id")

        # Modify query conditions
        self.meta.get("permissions")[0]["user_permission"] = {
            "User": ["name", "in", allowed_users]
        }
