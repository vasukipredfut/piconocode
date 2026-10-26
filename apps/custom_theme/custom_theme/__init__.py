__version__ = "0.0.1"



def _patch_desk_app_logo():
	import frappe.boot

	original_get_app_logo = frappe.boot.get_app_logo

	def get_app_logo():
		from custom_theme.utils import get_company_logo

		return get_company_logo() or original_get_app_logo()

	frappe.boot.get_app_logo = get_app_logo


_patch_desk_app_logo()
