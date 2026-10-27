"""Offline tests for the ERPNext -> Aria value mappings:  python -m unittest test_transforms -v"""
import unittest
from datetime import date, datetime
from decimal import Decimal

import transforms as T


class Generic(unittest.TestCase):
    def test_slug_code_is_unique_and_bounded(self):
        taken = set()
        self.assertEqual(T.slug_code("General Shift", 8, taken), "GENERAL_")
        self.assertEqual(T.slug_code("General Shift 2", 8, taken), "GENERA_2")
        self.assertEqual(T.slug_code("", 8, taken), "X")
        self.assertTrue(all(len(c) <= 8 for c in taken))

    def test_clip_and_truthy(self):
        self.assertIsNone(T.clip("   ", 5))
        self.assertEqual(T.clip(" abcdef ", 3), "abc")
        self.assertTrue(T.truthy(1) and T.truthy("1") and T.truthy(b"\x01"))
        self.assertFalse(T.truthy(0) or T.truthy("0") or T.truthy(None) or T.truthy(b"\x00"))

    def test_money_and_period(self):
        self.assertEqual(T.money(12.345), Decimal("12.35"))
        self.assertEqual(T.money(None), Decimal("0.00"))
        self.assertEqual(T.period_of(date(2026, 3, 1)), "2026-03")
        self.assertEqual(T.period_of(datetime(2026, 11, 30, 10, 0)), "2026-11")
        self.assertEqual(T.period_of("2026-07-15"), "2026-07")
        self.assertEqual(T.last10("+91 98765-43210"), "9876543210")
        self.assertIsNone(T.last10("12345"))


class Hr(unittest.TestCase):
    def test_leave_status(self):
        self.assertEqual(T.leave_status("Open", 0), ("PENDING", None))
        self.assertEqual(T.leave_status("Approved", 1), ("APPROVED", None))
        self.assertEqual(T.leave_status("Approved", 0), ("PENDING", None))  # approved but never submitted
        self.assertEqual(T.leave_status("Rejected", 0), ("REJECTED", None))
        self.assertEqual(T.leave_status("Cancellation Inprocess", 1), ("APPROVED", "REQUESTED"))
        self.assertEqual(T.leave_status("Cancelled", 1), ("CANCELLED", "CANCELLED"))
        self.assertEqual(T.leave_status("Approved", 2), ("CANCELLED", None))

    def test_misc_hr(self):
        self.assertEqual(T.which_half("Before Lunch"), "FIRST_HALF")
        self.assertEqual(T.which_half("second half"), "SECOND_HALF")
        self.assertIsNone(T.which_half(None))
        self.assertEqual(T.attendance_status("Work From Home"), "WFH")
        self.assertIsNone(T.attendance_status("Weird"))
        self.assertEqual(T.employee_status("Left"), "LEFT")
        self.assertEqual(T.weekly_off("Sunday"), "SUNDAY")
        self.assertIsNone(T.weekly_off("Funday"))
        self.assertEqual(T.overtime_status("Rejected"), "CANCELLED")
        self.assertEqual(T.overtime_status(None), "OPEN")

    def test_expense_status(self):
        self.assertEqual(T.expense_status("Draft", "Draft", 0, 0), "SUBMITTED")
        self.assertEqual(T.expense_status("Approved", "Unpaid", 0, 1), "APPROVED")
        self.assertEqual(T.expense_status("Approved", "Paid", 0, 1), "PAID")
        self.assertEqual(T.expense_status("Approved", "Unpaid", 1, 1), "PAID")
        self.assertEqual(T.expense_status("Rejected", "Rejected", 0, 0), "REJECTED")
        self.assertEqual(T.expense_status("Approved", "Cancelled", 0, 2), "WITHDRAWN")

    def test_component_code(self):
        self.assertEqual(T.component_code("B", "Basic"), "B")
        self.assertEqual(T.component_code(None, "House Rent Allowance"), "HOUSE_RENT_ALLOWANCE")


class Crm(unittest.TestCase):
    def test_lead_and_quote(self):
        self.assertEqual(T.lead_stage("Lead"), "NEW")
        self.assertEqual(T.lead_stage("Open"), "NEW")
        self.assertEqual(T.lead_stage("Replied"), "CONTACTED")
        self.assertEqual(T.lead_stage("Opportunity"), "QUALIFIED")
        self.assertEqual(T.lead_stage("Quotation"), "QUOTED")
        self.assertEqual(T.lead_stage("Converted"), "WON")
        self.assertEqual(T.lead_stage("Do Not Contact"), "LOST")
        self.assertEqual(T.qualification("In Process"), "IN_PROCESS")
        self.assertEqual(T.qualification(None), "UNQUALIFIED")
        self.assertEqual(T.quote_status("Draft", 0), "DRAFT")
        self.assertEqual(T.quote_status("Open", 1), "SENT")
        self.assertEqual(T.quote_status("Ordered", 1), "ACCEPTED")
        self.assertEqual(T.quote_status("Lost", 1), "REJECTED")
        self.assertEqual(T.quote_status("Expired", 1), "EXPIRED")
        self.assertEqual(T.customer_type("Individual"), "INDIVIDUAL")
        self.assertEqual(T.customer_type("Company"), "COMPANY")


class Service(unittest.TestCase):
    def test_service_maps(self):
        self.assertEqual(T.issue_status("Replied"), "AWAITING_CUSTOMER")
        self.assertEqual(T.issue_status("Closed"), "CLOSED")
        self.assertEqual(T.issue_priority("Urgent"), "P1")
        self.assertEqual(T.issue_priority(None), "P3")
        self.assertEqual(T.install_status(1, 1), "VERIFIED")
        self.assertEqual(T.install_status(0, 0), "OTP_PENDING")
        self.assertEqual(T.install_status(1, 2), "CANCELLED")
        self.assertEqual(T.warranty_kind(0, 1, 0, 0), "AMC")
        self.assertIsNone(T.warranty_kind(0, 0, 0, 0))

    def test_strip_html(self):
        self.assertEqual(T.strip_html("<div>Line 1<br>Line &amp; 2</div><p>x</p>"), "Line 1\nLine & 2\nx")
        self.assertEqual(T.strip_html("<p>a</p><p></p><p></p><p>b</p>"), "a\n\nb")
        self.assertIsNone(T.strip_html(None))


class Files(unittest.TestCase):
    def test_sniff(self):
        self.assertEqual(T.sniff(b"\xff\xd8\xff\xe0abc"), "jpg")
        self.assertEqual(T.sniff(b"\x89PNG\r\n\x1a\n"), "png")
        self.assertEqual(T.sniff(b"RIFF\x00\x00\x00\x00WEBP"), "webp")
        self.assertEqual(T.sniff(b"%PDF-1.4"), "pdf")
        self.assertIsNone(T.sniff(b"<svg xmlns"))
        self.assertIsNone(T.sniff(b"<html>"))

    def test_erp_file_path(self):
        self.assertEqual(T.erp_file_path("/files/a b.jpg"), ("public", "a b.jpg"))
        self.assertEqual(T.erp_file_path("https://erp.x.com/private/files/r.pdf"), ("private", "r.pdf"))
        self.assertIsNone(T.erp_file_path("/files/../../etc/passwd"))
        self.assertIsNone(T.erp_file_path("/files/sub/x.jpg"))
        self.assertIsNone(T.erp_file_path("https://cdn.example.com/x.jpg"))
        self.assertIsNone(T.erp_file_path(None))

    def test_payslip_html_escapes(self):
        h = T.payslip_html("PICO <Auto>", {"emp_code": "E1", "full_name": "A&B"}, "2026-10",
                           {"name": "SAL-1", "gross_pay": 1000, "total_deduction": 100, "net_pay": 900, "payment_days": 30, "leave_without_pay": 0},
                           [{"component": "Basic", "amount": 1000}], [{"component": "PF", "amount": 100}])
        self.assertIn("PICO &lt;Auto&gt;", h)
        self.assertIn("A&amp;B", h)
        self.assertIn("900.00", h)


if __name__ == "__main__":
    unittest.main()
