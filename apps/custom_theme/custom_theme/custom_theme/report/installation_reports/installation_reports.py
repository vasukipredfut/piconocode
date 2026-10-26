import frappe


def execute(filters=None):
	filters = filters or {}
	columns = get_columns()
	data = get_data(filters)
	return columns, data


def get_columns():
	return [
		{"label": "Doc No", "fieldname": "doc_no", "fieldtype": "Data", "width": 110},
		{"label": "Date", "fieldname": "date", "fieldtype": "Date", "width": 100},
		{"label": "Employee", "fieldname": "employee", "fieldtype": "Link", "options": "Employee", "width": 120},
		{"label": "Employee Name", "fieldname": "employee_name", "fieldtype": "Data", "width": 150},
		{"label": "Customer", "fieldname": "customer", "fieldtype": "Link", "options": "Customer", "width": 140},
		{"label": "Customer Name", "fieldname": "customer_name", "fieldtype": "Data", "width": 150},
		{"label": "Nature Of Call", "fieldname": "nature_of_call", "fieldtype": "Data", "width": 140},
		{"label": "Machine Type", "fieldname": "machine_type", "fieldtype": "Data", "width": 120},
		{"label": "Machine Model", "fieldname": "machine_model", "fieldtype": "Data", "width": 120},
		{"label": "Company", "fieldname": "company", "fieldtype": "Link", "options": "Company", "width": 140},
	]


def get_data(filters):
	conditions = ["1=1"]
	values = {}

	if filters.get("from_date"):
		conditions.append("ir.date >= %(from_date)s")
		values["from_date"] = filters["from_date"]
	if filters.get("to_date"):
		conditions.append("ir.date <= %(to_date)s")
		values["to_date"] = filters["to_date"]
	if filters.get("employee"):
		conditions.append("ir.employee = %(employee)s")
		values["employee"] = filters["employee"]
	if filters.get("customer"):
		conditions.append("ir.customer = %(customer)s")
		values["customer"] = filters["customer"]
	if filters.get("company"):
		conditions.append("ir.company = %(company)s")
		values["company"] = filters["company"]

	where_clause = " AND ".join(conditions)

	return frappe.db.sql(
		f"""
		SELECT
			ir.doc_no, ir.date, ir.employee, ir.employee_name, ir.customer, ir.customer_name,
			ir.nature_of_call, ir.machine_type, ir.machine_model, ir.company
		FROM `tabInstallation Report` ir
		WHERE {where_clause}
		ORDER BY ir.date DESC
		""",
		values,
		as_dict=1,
	)
