import frappe


def execute(filters=None):
	filters = filters or {}
	columns = get_columns()
	data = get_data(filters)
	return columns, data


def get_columns():
	return [
		{"label": "Employee", "fieldname": "employee", "fieldtype": "Link", "options": "Employee", "width": 120},
		{"label": "Employee Name", "fieldname": "employee_name", "fieldtype": "Data", "width": 150},
		{"label": "Expense Type", "fieldname": "expense_type", "fieldtype": "Data", "width": 160},
		{"label": "Posting Date", "fieldname": "posting_date", "fieldtype": "Date", "width": 100},
		{"label": "Status", "fieldname": "status", "fieldtype": "Data", "width": 100},
		{"label": "Grand Total", "fieldname": "grand_total", "fieldtype": "Currency", "width": 120},
		{"label": "Total Claimed Amount", "fieldname": "total_claimed_amount", "fieldtype": "Currency", "width": 140},
		{"label": "Company", "fieldname": "company", "fieldtype": "Link", "options": "Company", "width": 140},
	]


def get_data(filters):
	conditions = ["1=1"]
	values = {}

	if filters.get("from_date"):
		conditions.append("ec.posting_date >= %(from_date)s")
		values["from_date"] = filters["from_date"]
	if filters.get("to_date"):
		conditions.append("ec.posting_date <= %(to_date)s")
		values["to_date"] = filters["to_date"]
	if filters.get("employee"):
		conditions.append("ec.employee = %(employee)s")
		values["employee"] = filters["employee"]
	if filters.get("company"):
		conditions.append("ec.company = %(company)s")
		values["company"] = filters["company"]
	if filters.get("status"):
		conditions.append("ec.status = %(status)s")
		values["status"] = filters["status"]

	where_clause = " AND ".join(conditions)

	# "expense_type" lives on the Expense Claim Detail child table
	# ("expenses"), not on Expense Claim itself — one claim can have several
	# line items, each with its own type, so this stays one row per claim
	# (matching the original doctype list it replaced) by rolling up every
	# distinct type on that claim into one comma-separated value rather than
	# fanning out into one row per line item.
	return frappe.db.sql(
		f"""
		SELECT
			ec.employee, ec.employee_name,
			GROUP_CONCAT(DISTINCT ecd.expense_type SEPARATOR ', ') AS expense_type,
			ec.posting_date, ec.status, ec.grand_total, ec.total_claimed_amount, ec.company
		FROM `tabExpense Claim` ec
		LEFT JOIN `tabExpense Claim Detail` ecd ON ecd.parent = ec.name
		WHERE {where_clause}
		GROUP BY ec.name
		ORDER BY ec.posting_date DESC
		""",
		values,
		as_dict=1,
	)
