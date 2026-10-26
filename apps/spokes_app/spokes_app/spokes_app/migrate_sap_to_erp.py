import pymysql
import frappe

# SAP MySQL Database Configuration
SAP_DB_CONFIG = {
    "host": "localhost",
    "user": "root",
    "password": "admin@123",
    "database": "sap_dummy"
}

def fetch_sap_data():
    """Fetch customer data from the dummy SAP database"""
    conn = pymysql.connect(**SAP_DB_CONFIG)
    cursor = conn.cursor()
    cursor.execute("SELECT customer_code, customer_name, customer_group, territory FROM kna1")
    sap_data = cursor.fetchall()
    conn.close()
    return sap_data

def migrate_customers():
    """Migrate SAP customer data to ERPNext"""
    sap_data = fetch_sap_data()
    
    for customer in sap_data:
        customer_code, customer_name, customer_group, territory = customer

        # Check if customer already exists in ERPNext
        if not frappe.db.exists("Customer", customer_name):
            customer_doc = frappe.get_doc({
                "doctype": "Customer",
                "customer_name": customer_name,
                "customer_group": customer_group,
                "territory": territory,
                "customer_type": "Company",  # Adjust if needed
                "customer_code": customer_code
            })
            customer_doc.insert(ignore_permissions=True)
            frappe.db.commit()
            print(f"Customer {customer_name} migrated successfully.")
        else:
            print(f"Customer {customer_name} already exists.")

# Run the migration manually for testing
if __name__ == "__main__":
    migrate_customers()
