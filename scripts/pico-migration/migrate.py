#!/usr/bin/env python3
"""ERPNext (Spokes / PICO) -> Aria data migration.

Reads the ERPNext MariaDB directly and writes the Aria MariaDB (tables from V382-V389) for ONE
Aria tenant. Every row carries erp_ref = the ERPNext document name, so a run is an upsert:
re-running, or running with --since for the cut-over delta, updates rows instead of duplicating.

    python migrate.py --config migrate.ini --dry-run          # everything in one transaction, rolled back
    python migrate.py --config migrate.ini                    # full load
    python migrate.py --config migrate.ini --since "2026-11-28 18:00"   # delta (modified >= since)
    python migrate.py --config migrate.ini --phases people,leave        # selected phases
    python migrate.py --config migrate.ini --reconcile        # source vs target counts / sums

See MAPPING.md for the field mapping and CUTOVER.md for the runbook. Needs: pip install pymysql
"""
import argparse
import configparser
import hashlib
import json
import logging
import os
import shutil
import sys
from collections import defaultdict
from datetime import datetime

import transforms as T

try:
    import pymysql
    from pymysql.cursors import DictCursor
except ImportError:  # transforms / tests do not need it
    pymysql = None

log = logging.getLogger("migrate")

PHASES = ["masters", "people", "salary", "attendance", "leave", "payroll", "expense", "crm", "service", "files"]


# =============================================================== database helpers

class Db:
    def __init__(self, cfg, autocommit):
        if pymysql is None:
            sys.exit("pymysql is required: pip install pymysql")
        self.conn = pymysql.connect(host=cfg["host"], port=int(cfg.get("port", 3306)), user=cfg["user"],
                                    password=cfg["password"], database=cfg["database"], charset="utf8mb4",
                                    cursorclass=DictCursor, autocommit=autocommit)
        self.schema = cfg["database"]
        self._cols = {}

    def q(self, sql, params=()):
        with self.conn.cursor() as c:
            c.execute(sql, params)
            return list(c.fetchall())

    def one(self, sql, params=()):
        rows = self.q(sql, params)
        return rows[0] if rows else None

    def x(self, sql, params=()):
        with self.conn.cursor() as c:
            c.execute(sql, params)
            return c.lastrowid

    def columns(self, table):
        if table not in self._cols:
            self._cols[table] = {r["COLUMN_NAME"] for r in self.q(
                "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=%s AND TABLE_NAME=%s", (self.schema, table))}
        return self._cols[table]

    def has_table(self, table):
        return bool(self.columns(table))


class Source(Db):
    """ERPNext side. Selects only columns that exist (custom fields vary per site) and applies the
    company filter and the --since delta where the table has those columns."""

    def __init__(self, cfg, company, since):
        super().__init__(cfg, autocommit=True)
        self.company, self.since = company, since

    def rows(self, doctype, cols, where="1=1", params=(), company=True, delta=True, order="name"):
        table = "tab" + doctype
        have = self.columns(table)
        if not have:
            log.warning("source table %s not found -- skipped", table)
            return []
        present = [c for c in cols if c in have]
        sql = "SELECT " + ", ".join(f"`{c}`" for c in present) + f" FROM `{table}` WHERE ({where})"
        p = list(params)
        if company and self.company and "company" in have:
            sql += " AND company=%s"
            p.append(self.company)
        if delta and self.since and "modified" in have:
            sql += " AND modified >= %s"
            p.append(self.since)
        if order and order.split()[0] in have:
            sql += " ORDER BY " + order
        out = self.q(sql, p)
        for r in out:
            for c in cols:
                r.setdefault(c, None)
        return out


class Target(Db):
    """Aria side, one tenant. upsert() keys on (tenant_id, erp_ref) or a natural key."""

    def __init__(self, cfg, tenant_id):
        super().__init__(cfg, autocommit=False)
        self.tenant = tenant_id
        self.stats = defaultdict(lambda: defaultdict(int))
        self._wf = {}

    def upsert(self, table, key, values, insert_only=None):
        """key: dict of identifying columns (e.g. {'erp_ref': 'HR-EMP-0001'}); values: columns to
        write. insert_only: columns set only when the row is new (numbers, codes). Returns id."""
        have = self.columns(table)
        unknown = [c for c in list(key) + list(values) + list(insert_only or {}) if c not in have]
        if unknown:
            raise RuntimeError(f"{table}: unknown columns {unknown} -- is the V382-V389 schema applied?")
        where = " AND ".join(f"`{k}`=%s" for k in key) + " AND tenant_id=%s"
        row = self.one(f"SELECT id FROM `{table}` WHERE {where}", tuple(key.values()) + (self.tenant,))
        if row:
            if values:
                sets = ", ".join(f"`{k}`=%s" for k in values)
                self.x(f"UPDATE `{table}` SET {sets} WHERE id=%s", tuple(values.values()) + (row["id"],))
            self.stats[table]["updated"] += 1
            return row["id"]
        cols = {"tenant_id": self.tenant, **key, **(insert_only or {}), **values}
        try:
            rid = self.x(f"INSERT INTO `{table}` (" + ", ".join(f"`{c}`" for c in cols) + ") VALUES (" +
                         ", ".join(["%s"] * len(cols)) + ")", tuple(cols.values()))
        except pymysql.err.IntegrityError as e:
            # Duplicate business key / broken FK in the source: MariaDB rolls back only this
            # statement, so the row is reported and the load continues.
            self.stats[table]["rejected"] += 1
            log.warning("%s %s rejected: %s", table, key, e.args[1] if len(e.args) > 1 else e)
            return None
        self.stats[table]["inserted"] += 1
        return rid

    def replace_children(self, table, parent_col, parent_id, rows):
        self.x(f"DELETE FROM `{table}` WHERE tenant_id=%s AND `{parent_col}`=%s", (self.tenant, parent_id))
        for r in rows:
            cols = {"tenant_id": self.tenant, parent_col: parent_id, **r}
            self.x(f"INSERT INTO `{table}` (" + ", ".join(f"`{c}`" for c in cols) + ") VALUES (" +
                   ", ".join(["%s"] * len(cols)) + ")", tuple(cols.values()))
        self.stats[table]["inserted"] += len(rows)

    def ids(self, table, col="erp_ref"):
        return {r[col]: r["id"] for r in self.q(f"SELECT id, `{col}` FROM `{table}` WHERE tenant_id=%s AND `{col}` IS NOT NULL", (self.tenant,))}

    def setting(self, key, value):
        if value is None:
            return
        self.x("INSERT INTO m0_tenant_setting (tenant_id, setting_key, setting_value) VALUES (%s,%s,%s) "
               "ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)", (self.tenant, key, str(value)))

    # -------- workflow instances (what WorkflowEngineService.startInstance writes)

    def _definition(self, code):
        if code not in self._wf:
            d = self.one("SELECT tenant_id, version, body_json FROM workflow_definition WHERE code=%s AND status='PUBLISHED' "
                         "AND tenant_id IN (%s,-1) ORDER BY tenant_id DESC, version DESC LIMIT 1", (code, self.tenant))
            if not d:
                raise RuntimeError(f"workflow {code} not published -- apply V382-V388 first")
            body = json.loads(d["body_json"])
            states = {s["code"]: s for s in body["states"]}
            self._wf[code] = (d["tenant_id"], d["version"], states)
        return self._wf[code]

    def sync_instance(self, table, row_id, code, state, business_ref, column="workflow_instance_id"):
        """Keep a workflow_instance in step with a migrated row: open states get / keep an OPEN
        instance at that state; terminal states close an existing one (history needs none)."""
        owner, version, states = self._definition(code)
        if state not in states:
            raise RuntimeError(f"{code} has no state {state}")
        terminal = bool(states[state].get("terminal"))
        role = states[state].get("assignedRole")
        cur = self.one(f"SELECT `{column}` AS wi FROM `{table}` WHERE id=%s", (row_id,))
        wi = cur and cur["wi"]
        if wi:
            self.x("UPDATE workflow_instance SET current_state=%s, assigned_role=%s, status=%s WHERE id=%s AND tenant_id=%s",
                   (state, None if terminal else role, "CLOSED" if terminal else "OPEN", wi, self.tenant))
            return wi
        if terminal:
            return None
        wi = self.x("INSERT INTO workflow_instance (tenant_id, workflow_code, workflow_version, workflow_owner_tenant_id, current_state, "
                    "status, business_ref, submitted_data_json, assigned_role) VALUES (%s,%s,%s,%s,%s,'OPEN',%s,'{}',%s)",
                    (self.tenant, code, version, owner, state, business_ref, role))
        self.x(f"UPDATE `{table}` SET `{column}`=%s WHERE id=%s", (wi, row_id))
        self.stats["workflow_instance"]["inserted"] += 1
        return wi


# =============================================================== run context

class Ctx:
    def __init__(self, src, tgt, cfg):
        self.src, self.tgt, self.cfg = src, tgt, cfg
        self.emp = {}            # ERPNext Employee name -> m7_employee.id
        self.emp_by_user = {}    # ERPNext user id (email) -> m7_employee.id
        self.emp_by_name = {}    # employee_name -> m7_employee.id (unique names only)
        self.sales_person = {}   # Sales Person name -> m7_employee.id
        self.shift_code = {}     # Shift Type name -> m7_shift_type.shift_code
        self.leave_code = {}     # Leave Type name -> m7_leave_type.type_code
        self.expense_code = {}   # Expense Claim Type name -> m7_expense_claim_type.type_code
        self.warnings = []

    def warn(self, msg):
        self.warnings.append(msg)
        log.warning(msg)

    def load_maps(self):
        t = self.tgt
        self.emp = t.ids("m7_employee")
        for r in t.q("SELECT e.id, e.full_name, u.username FROM m7_employee e LEFT JOIN employee_details u ON u.id=e.user_id WHERE e.tenant_id=%s", (t.tenant,)):
            if r["username"]:
                self.emp_by_user[r["username"].lower()] = r["id"]
            self.emp_by_name.setdefault(r["full_name"], []).append(r["id"])
        self.shift_code = {r["erp_ref"]: r["shift_code"] for r in t.q("SELECT erp_ref, shift_code FROM m7_shift_type WHERE tenant_id=%s AND erp_ref IS NOT NULL", (t.tenant,))}
        self.leave_code = {r["erp_ref"]: r["type_code"] for r in t.q("SELECT erp_ref, type_code FROM m7_leave_type WHERE tenant_id=%s AND erp_ref IS NOT NULL", (t.tenant,))}
        self.expense_code = {r["erp_ref"]: r["type_code"] for r in t.q("SELECT erp_ref, type_code FROM m7_expense_claim_type WHERE tenant_id=%s AND erp_ref IS NOT NULL", (t.tenant,))}
        self.load_sales_persons()

    def load_sales_persons(self):
        """Sales Person name -> employee; re-run after the people phase (first run: no employees yet)."""
        for sp in self.src.rows("Sales Person", ["name", "employee", "enabled"], company=False, delta=False):
            if sp["employee"] in self.emp:
                self.sales_person[sp["name"]] = self.emp[sp["employee"]]

    def emp_by_full_name(self, name):
        ids = self.emp_by_name.get(name) or []
        return ids[0] if len(ids) == 1 else None


# =============================================================== phases

def phase_masters(c):
    s, t = c.src, c.tgt
    # Holiday lists + holidays
    for hl in s.rows("Holiday List", ["name", "holiday_list_name", "from_date", "to_date", "weekly_off"], company=False):
        hid = t.upsert("m7_holiday_list", {"erp_ref": hl["name"]},
                       {"list_name": T.clip(hl["holiday_list_name"] or hl["name"], 128), "from_date": hl["from_date"],
                        "to_date": hl["to_date"], "weekly_off": T.weekly_off(hl["weekly_off"]), "is_active": 1})
        if not hid:
            continue
        days = s.rows("Holiday", ["holiday_date", "description", "weekly_off"], "parent=%s", (hl["name"],), company=False, delta=False, order="holiday_date")
        # Weekly offs are computed from weekly_off in Aria; only real holidays are rows.
        t.replace_children("m7_holiday", "holiday_list_id", hid,
                           [{"holiday_date": d["holiday_date"], "description": T.clip(T.strip_html(d["description"]) or "Holiday", 255)}
                            for d in days if not T.truthy(d["weekly_off"])])
    # Shift types (+ HR Attendance Settings: OT / half-day rules per company + shift)
    rules = {r["shift_type"]: r for r in s.rows("HR Attendance Settings", ["shift_type", "full_day_before_time", "ot_start_time",
                                                                          "ot_multiplier", "holiday_ot_multiplier", "minimum_ot_minutes"], delta=False)}
    taken = {r["shift_code"] for r in t.q("SELECT shift_code FROM m7_shift_type WHERE tenant_id=%s", (t.tenant,))}
    for st in s.rows("Shift Type", ["name", "start_time", "end_time"], company=False):
        r = rules.get(st["name"], {})
        existing = c.shift_code.get(st["name"])
        code = existing or T.slug_code(st["name"], 8, taken)
        t.upsert("m7_shift_type", {"erp_ref": st["name"]},
                 {"shift_name": T.clip(st["name"], 64), "start_time": st["start_time"], "end_time": st["end_time"],
                  "full_day_before_time": r.get("full_day_before_time"), "ot_start_time": r.get("ot_start_time"),
                  "min_ot_minutes": int(r.get("minimum_ot_minutes") or 0), "ot_multiplier": r.get("ot_multiplier") or 1,
                  "holiday_ot_multiplier": r.get("holiday_ot_multiplier") or 1, "is_active": 1},
                 insert_only={"shift_code": code})
        c.shift_code[st["name"]] = code
    # Leave types
    taken = {r["type_code"] for r in t.q("SELECT type_code FROM m7_leave_type WHERE tenant_id=%s", (t.tenant,))}
    for lt in s.rows("Leave Type", ["name", "leave_type_name", "max_leaves_allowed", "is_lwp", "is_compensatory",
                                    "include_holiday", "allow_negative"], company=False):
        code = c.leave_code.get(lt["name"]) or T.slug_code(lt["name"], 16, taken)
        t.upsert("m7_leave_type", {"erp_ref": lt["name"]},
                 {"type_name": T.clip(lt["leave_type_name"] or lt["name"], 64), "max_days_per_year": lt["max_leaves_allowed"] or None,
                  "is_lwp": int(T.truthy(lt["is_lwp"])), "is_compensatory": int(T.truthy(lt["is_compensatory"])),
                  "include_holidays": int(T.truthy(lt["include_holiday"])), "allow_negative": int(T.truthy(lt["allow_negative"])), "is_active": 1},
                 insert_only={"type_code": code})
        c.leave_code[lt["name"]] = code
    # Expense claim types (GL accounts are ERPNext account names, not Aria codes -> set manually)
    taken = {r["type_code"] for r in t.q("SELECT type_code FROM m7_expense_claim_type WHERE tenant_id=%s", (t.tenant,))}
    for et in s.rows("Expense Claim Type", ["name", "expense_type"], company=False):
        code = c.expense_code.get(et["name"]) or T.slug_code(et["name"], 32, taken)
        t.upsert("m7_expense_claim_type", {"erp_ref": et["name"]}, {"type_name": T.clip(et["expense_type"] or et["name"], 128), "is_active": 1},
                 insert_only={"type_code": code})
        c.expense_code[et["name"]] = code
    # Announcements (custom DocType "Notifications")
    for n in s.rows("Notifications", ["name", "title", "description", "is_active", "creation"]):
        t.upsert("m7_announcement", {"erp_ref": n["name"]},
                 {"title": T.clip(n["title"] or n["name"], 200), "body": T.strip_html(n["description"]) or "-",
                  "is_active": int(T.truthy(n["is_active"]))})
    # Branding logo (file is copied by the files phase)
    if c.cfg.get("erp_company"):
        b = s.one("SELECT logo FROM `tabCompany Branding` WHERE company=%s", (c.cfg["erp_company"],)) if s.has_table("tabCompany Branding") else None
        if b and b["logo"]:
            t.setting("branding.logo_url", b["logo"])


def phase_people(c):
    s, t = c.src, c.tgt
    role_ids = {r["name"]: r["id"] for r in t.q("SELECT id, name FROM role")}
    role_map = json.loads(c.cfg.get("role_map") or "{}") or {
        "System Manager": "TenantAdmin", "HR Manager": "Manager", "HR User": "Manager", "Sales Manager": "Sales",
        "Sales User": "Sales", "Maintenance Manager": "Service", "Maintenance User": "Service"}
    priority = ["TenantAdmin", "Manager", "Sales", "Service", "Employee"]
    has_role = defaultdict(set)
    if s.has_table("tabHas Role"):
        for r in s.q("SELECT parent, role FROM `tabHas Role` WHERE parenttype='User'"):
            if r["role"] in role_map:
                has_role[r["parent"].lower()].add(role_map[r["role"]])

    cols = ["name", "employee_number", "employee_name", "first_name", "gender", "date_of_birth", "date_of_joining", "relieving_date",
            "status", "department", "designation", "cell_number", "company_email", "personal_email", "current_address",
            "permanent_address", "person_to_be_contacted", "emergency_phone_number", "relation", "bank_name", "bank_ac_no",
            "ifsc_code", "pan_number", "user_id", "image", "blood_group", "marital_status", "attendance_device_id"]
    taken_codes = {r["emp_code"] for r in t.q("SELECT emp_code FROM m7_employee WHERE tenant_id=%s", (t.tenant,))}
    for e in s.rows("Employee", cols):
        # Login (employee_details): username = ERPNext user id. ERPNext password hashes cannot be
        # carried over, so the password is unusable until the user runs /api/auth/password-reset.
        user_id = None
        if e["user_id"]:
            uname = e["user_id"].strip().lower()
            roles = has_role.get(uname, set())
            role = next((p for p in priority if p in roles), "Employee")
            u = t.one("SELECT id FROM employee_details WHERE username=%s", (uname,))
            if u:
                user_id = u["id"]
                t.x("UPDATE employee_details SET employeename=%s, email=%s, phoneno=%s, roles_id=%s, tenant_id=%s WHERE id=%s",
                    (e["employee_name"], uname, T.clip(e["cell_number"], 255), role_ids.get(role), t.tenant, user_id))
            else:
                user_id = t.x("INSERT INTO employee_details (username, employeename, email, phoneno, password, roles_id, status, tenant_id) "
                              "VALUES (%s,%s,%s,%s,'!reset-required',%s,'A',%s)",
                              (uname, e["employee_name"], uname, T.clip(e["cell_number"], 255), role_ids.get(role), t.tenant))
                t.stats["employee_details"]["inserted"] += 1
            if role not in role_ids:
                c.warn(f"role {role} missing in Aria (apply V389) for {uname}")
        code = T.clip(e["employee_number"] or e["name"], 32)
        status = T.employee_status(e["status"])
        eid = t.upsert("m7_employee", {"erp_ref": e["name"]}, {
            "full_name": T.clip(e["employee_name"] or e["first_name"], 128), "gender": T.clip(e["gender"], 8), "dob": e["date_of_birth"],
            "date_of_joining": e["date_of_joining"], "separation_date": e["relieving_date"], "status": status,
            "department": T.clip(e["department"], 64), "designation": T.clip(e["designation"], 64),
            "mobile_primary": T.clip(e["cell_number"], 16), "email_official": T.clip(e["company_email"], 96),
            "email_personal": T.clip(e["personal_email"], 96), "current_address": e["current_address"], "permanent_address": e["permanent_address"],
            "emergency_contact_name": T.clip(e["person_to_be_contacted"], 96), "emergency_contact_phone": T.clip(e["emergency_phone_number"], 16),
            "emergency_contact_relation": T.clip(e["relation"], 32), "bank_name": T.clip(e["bank_name"], 64),
            "bank_account_no": T.clip(e["bank_ac_no"], 32), "bank_ifsc": T.clip(e["ifsc_code"], 16), "pan_no": T.clip(e["pan_number"], 16),
            "blood_group": T.clip(e["blood_group"], 4), "marital_status": T.clip(e["marital_status"], 16),
            "photo_url": T.clip(e["image"], 256), "user_id": user_id},
            insert_only={"emp_code": code if code not in taken_codes else T.slug_code(code, 32, taken_codes)})
        taken_codes.add(code)
        if not eid:
            continue
        c.emp[e["name"]] = eid
        if e["user_id"]:
            c.emp_by_user[e["user_id"].strip().lower()] = eid
        c.emp_by_name.setdefault(e["employee_name"], [])
        if eid not in c.emp_by_name[e["employee_name"]]:
            c.emp_by_name[e["employee_name"]].append(eid)

    # Second pass: links between employees and to masters (all employees exist now)
    lists = t.ids("m7_holiday_list")
    link_cols = ["name", "reports_to", "leave_approver", "expense_approver", "holiday_list", "default_shift"]
    for e in s.rows("Employee", link_cols):
        eid = c.emp.get(e["name"])
        if not eid:
            continue
        t.x("UPDATE m7_employee SET reports_to_emp_id=%s, leave_approver_id=%s, expense_approver_id=%s, holiday_list_id=%s, "
            "shift_code=%s WHERE id=%s",
            (c.emp.get(e["reports_to"]), c.emp_by_user.get((e["leave_approver"] or "").lower()),
             c.emp_by_user.get((e["expense_approver"] or "").lower()), lists.get(e["holiday_list"]),
             c.shift_code.get(e["default_shift"]), eid))
        if e["leave_approver"] and not c.emp_by_user.get(e["leave_approver"].lower()):
            c.warn(f"{e['name']}: leave approver {e['leave_approver']} is not a migrated employee")
    # Sales persons
    c.load_sales_persons()
    for sp_name, eid in c.sales_person.items():
        t.x("UPDATE m7_employee SET is_sales_person=1 WHERE id=%s", (eid,))
    # Shift assignments
    for sa in s.rows("Shift Assignment", ["name", "employee", "shift_type", "start_date", "end_date", "status", "docstatus"], "docstatus=1"):
        if sa["employee"] in c.emp and sa["shift_type"] in c.shift_code:
            t.upsert("m7_shift_assignment", {"employee_id": c.emp[sa["employee"]], "start_date": sa["start_date"],
                                             "shift_code": c.shift_code[sa["shift_type"]]},
                     {"end_date": sa["end_date"], "status": "ACTIVE" if sa["status"] == "Active" else "INACTIVE"})
    # Lead owner (company -> user) and lead cities (custom DocType "Lead City": name, company, sales_person)
    lo = s.rows("Lead Owner", ["lead_owner"], delta=False)
    if lo and lo[0]["lead_owner"] and c.emp_by_user.get(lo[0]["lead_owner"].lower()):
        t.setting("crm.lead_owner_emp_id", c.emp_by_user[lo[0]["lead_owner"].lower()])
    for lc in s.rows("Lead City", ["name", "sales_person"]):
        t.upsert("m8_lead_city", {"erp_ref": lc["name"]}, {"city_name": T.clip(lc["name"], 64),
                                                           "sales_person_emp_id": c.sales_person.get(lc["sales_person"]), "is_active": 1})


def phase_salary(c):
    """Latest submitted Salary Structure Assignment per employee -> salary card. ERPNext structures
    compute components by formula; Aria cards hold monthly amounts, so `base` goes to basic_monthly
    (what ERPNext's OT rate used) and every card is flagged for HR to split into DA / HRA / ..."""
    s, t = c.src, c.tgt
    latest = {}
    for a in s.rows("Salary Structure Assignment", ["name", "employee", "from_date", "base", "variable", "salary_structure"],
                    "docstatus=1", order="from_date"):
        latest[a["employee"]] = a
    for emp, a in latest.items():
        if emp not in c.emp:
            continue
        base = T.money(a["base"])
        t.upsert("m7_employee_salary", {"erp_ref": a["name"]},
                 {"emp_id": c.emp[emp], "effective_from": a["from_date"], "basic_monthly": base,
                  "ctc_annual": base * 12 + T.money(a["variable"]),
                  "notes": T.clip(f"Migrated from ERPNext structure '{a['salary_structure']}' (base {base}). REVIEW: split into DA / HRA / allowances.", 1000)})


def phase_attendance(c):
    s, t = c.src, c.tgt
    for a in s.rows("Attendance", ["name", "employee", "attendance_date", "status", "shift", "in_time", "out_time", "working_hours"],
                    "docstatus=1", order="attendance_date"):
        eid, st = c.emp.get(a["employee"]), T.attendance_status(a["status"])
        if not eid or not st:
            continue
        t.upsert("m7_attendance", {"employee_id": eid, "date_of": a["attendance_date"]},
                 {"status": st, "shift_code": c.shift_code.get(a["shift"]),
                  "in_time": a["in_time"].time() if a["in_time"] else None, "out_time": a["out_time"].time() if a["out_time"] else None,
                  "worked_hours": a["working_hours"] or None, "source": "IMPORT"})
    for ch in s.rows("Employee Checkin", ["name", "employee", "log_type", "time", "latitude", "longitude", "device_id",
                                          "punch_device", "attach_image"], company=False, order="time"):
        eid = c.emp.get(ch["employee"])
        if not eid or ch["log_type"] not in ("IN", "OUT"):
            continue
        t.upsert("m7_employee_checkin", {"erp_ref": ch["name"]},
                 {"employee_id": eid, "log_type": ch["log_type"], "punch_time": ch["time"], "latitude": ch["latitude"],
                  "longitude": ch["longitude"], "punch_device": T.clip(ch["punch_device"] or ch["device_id"], 64),
                  "image_url": T.clip(ch["attach_image"], 512), "source": "IMPORT"})
    for ot in s.rows("OverTime Entry", ["name", "employee", "attendance_date", "shift", "ot_hours", "ot_amount", "is_holiday", "status"]):
        eid = c.emp.get(ot["employee"])
        if eid:
            t.upsert("m7_overtime_entry", {"erp_ref": ot["name"]},
                     {"employee_id": eid, "attendance_date": ot["attendance_date"], "shift_code": c.shift_code.get(ot["shift"]),
                      "ot_hours": ot["ot_hours"] or 0, "ot_amount": T.money(ot["ot_amount"]), "is_holiday": int(T.truthy(ot["is_holiday"])),
                      "status": T.overtime_status(ot["status"])})


def phase_leave(c):
    s, t = c.src, c.tgt
    for al in s.rows("Leave Allocation", ["name", "employee", "leave_type", "from_date", "to_date", "total_leaves_allocated",
                                          "new_leaves_allocated", "description"], "docstatus=1"):
        if al["employee"] in c.emp and al["leave_type"] in c.leave_code:
            t.upsert("m7_leave_allocation", {"erp_ref": al["name"]},
                     {"employee_id": c.emp[al["employee"]], "leave_kind": c.leave_code[al["leave_type"]], "from_date": al["from_date"],
                      "to_date": al["to_date"], "allocated_days": al["total_leaves_allocated"] or al["new_leaves_allocated"] or 0,
                      "notes": T.clip(al["description"], 255)})
    cols = ["name", "employee", "leave_type", "from_date", "to_date", "half_day", "half_day_date", "which_half", "total_leave_days",
            "description", "leave_approver", "status", "docstatus", "posting_date", "cancellation_reason", "requested_half_day",
            "requested_which_half", "modified"]
    for la in s.rows("Leave Application", cols, order="from_date"):
        eid = c.emp.get(la["employee"])
        if not eid or la["leave_type"] not in c.leave_code:
            c.warn(f"leave {la['name']}: employee or leave type not migrated -- skipped")
            continue
        status, cancel = T.leave_status(la["status"], la["docstatus"])
        half = T.truthy(la["half_day"])
        lid = t.upsert("m7_leave_request", {"erp_ref": la["name"]},
                       {"employee_id": eid, "leave_kind": c.leave_code[la["leave_type"]], "start_date": la["from_date"], "end_date": la["to_date"],
                        "days": la["total_leave_days"] or (0.5 if half else 1), "reason": T.clip(la["description"], 256), "status": status,
                        "current_step": status, "is_half_day": int(half), "which_half": T.which_half(la["which_half"]) if half else None,
                        "approver_emp_id": c.emp_by_user.get((la["leave_approver"] or "").lower()) or c.tgt.one(
                            "SELECT leave_approver_id AS a FROM m7_employee WHERE id=%s", (eid,))["a"],
                        "cancel_status": cancel, "cancel_reason": T.clip(la["cancellation_reason"], 512),
                        "revised_half_day": int(T.truthy(la["requested_half_day"])),
                        "revised_which_half": T.which_half(la["requested_which_half"]),
                        "decided_at": la["modified"] if status in ("APPROVED", "REJECTED", "CANCELLED") else None},
                       insert_only={"lr_number": T.clip(la["name"], 32)})
        if not lid:
            continue
        # The engine's leave workflow knows PENDING / APPROVED / REJECTED / WITHDRAWN; CANCELLED is
        # set by the cancellation flow, so a cancelled leave keeps the leave instance at APPROVED.
        t.sync_instance("m7_leave_request", lid, "M7.LeaveRequest", "APPROVED" if status == "CANCELLED" else status, f"LeaveRequest#{lid}")
        if cancel:
            t.sync_instance("m7_leave_request", lid, "M7.LeaveCancellation", cancel, f"LeaveRequest#{lid}", column="cancel_instance_id")


def phase_payroll(c):
    """Submitted Salary Slips -> m7_payroll_run (PAID) + lines + m7_payslip (PAID, HTML snapshot), so
    employees see their history in pico.my-salary. One slip per employee and month."""
    s, t = c.src, c.tgt
    company = c.cfg.get("company_display_name") or c.cfg.get("erp_company") or ""
    for sl in s.rows("Salary Slip", ["name", "employee", "start_date", "end_date", "posting_date", "payment_days", "leave_without_pay",
                                     "gross_pay", "total_deduction", "net_pay", "status", "docstatus"], "docstatus=1", order="start_date"):
        eid = c.emp.get(sl["employee"])
        if not eid:
            continue
        period = T.period_of(sl["start_date"])
        dup = t.one("SELECT erp_ref FROM m7_payroll_run WHERE tenant_id=%s AND employee_id=%s AND payroll_period=%s", (t.tenant, eid, period))
        if dup and dup["erp_ref"] != sl["name"]:
            c.warn(f"salary slip {sl['name']}: {period} already loaded from {dup['erp_ref']} -- skipped")
            continue
        details = s.rows("Salary Detail", ["salary_component", "abbr", "amount", "parentfield", "statistical_component", "do_not_include_in_total"],
                         "parent=%s AND parenttype='Salary Slip'", (sl["name"],), company=False, delta=False, order="idx")
        lines, earn, ded = [], [], []
        by = defaultdict(lambda: T.money(0))
        for d in details:
            if T.truthy(d["statistical_component"]) or T.truthy(d["do_not_include_in_total"]):
                continue
            kind = "EARNING" if d["parentfield"] == "earnings" else "DEDUCTION"
            code = T.component_code(d["abbr"], d["salary_component"])
            lines.append({"emp_id": eid, "component_code": code, "component_type": kind, "amount": T.money(d["amount"]),
                          "formula_used": T.clip(d["salary_component"], 256)})
            (earn if kind == "EARNING" else ded).append({"component": d["salary_component"], "amount": d["amount"]})
            by[code] += T.money(d["amount"])
        gross, total_ded, net = T.money(sl["gross_pay"]), T.money(sl["total_deduction"]), T.money(sl["net_pay"])
        rid = t.upsert("m7_payroll_run", {"erp_ref": sl["name"]},
                       {"payroll_period": period, "employee_id": eid, "worked_days": sl["payment_days"], "basic": by.get("B", by.get("BASIC", T.money(0))),
                        "hra": by.get("HRA", T.money(0)), "gross": gross, "net_pay": net, "total_deduct": total_ded,
                        "payable_days": sl["payment_days"], "lop_days": sl["leave_without_pay"], "status": "PAID", "current_step": "PAID",
                        "paid_at": sl["posting_date"]})
        if not rid:
            continue
        t.replace_children("m7_payroll_line", "run_id", rid, lines)
        emp = t.one("SELECT emp_code, full_name FROM m7_employee WHERE id=%s", (eid,))
        t.upsert("m7_payslip", {"emp_id": eid, "payroll_period": period},
                 {"status": "PAID", "gross": gross, "total_deduct": total_ded, "net_pay": net, "generated_at": sl["posting_date"],
                  "generated_by": "ERPNext migration", "html_snapshot": T.payslip_html(company, emp, period, sl, earn, ded)})


def phase_expense(c):
    s, t = c.src, c.tgt
    for ec in s.rows("Expense Claim", ["name", "employee", "expense_approver", "approval_status", "status", "is_paid", "docstatus",
                                       "posting_date", "total_claimed_amount", "total_sanctioned_amount", "remark", "modified"], order="posting_date"):
        eid = c.emp.get(ec["employee"])
        if not eid:
            continue
        status = T.expense_status(ec["approval_status"], ec["status"], ec["is_paid"], ec["docstatus"])
        cid = t.upsert("m7_expense_claim", {"erp_ref": ec["name"]},
                       {"employee_id": eid, "approver_emp_id": c.emp_by_user.get((ec["expense_approver"] or "").lower()),
                        "posting_date": ec["posting_date"], "total_claimed": T.money(ec["total_claimed_amount"]),
                        "total_sanctioned": T.money(ec["total_sanctioned_amount"]) if status in ("APPROVED", "PAID") else None,
                        "remarks": T.clip(ec["remark"], 512), "status": status, "current_step": status,
                        "decided_at": ec["modified"] if status != "SUBMITTED" else None, "paid_at": ec["modified"] if status == "PAID" else None},
                       insert_only={"claim_number": T.clip(ec["name"], 32)})
        if not cid:
            continue
        rows = []
        for d in s.rows("Expense Claim Detail", ["expense_date", "expense_type", "description", "amount", "sanctioned_amount", "receipt"],
                        "parent=%s", (ec["name"],), company=False, delta=False, order="idx"):
            if T.money(d["amount"]) <= 0:
                continue
            code = c.expense_code.get(d["expense_type"])
            if not code:
                c.warn(f"expense {ec['name']}: type {d['expense_type']} not migrated -- line skipped")
                continue
            rows.append({"expense_date": d["expense_date"] or ec["posting_date"], "type_code": code, "description": T.clip(T.strip_html(d["description"]), 512),
                         "amount": T.money(d["amount"]), "sanctioned_amount": T.money(d["sanctioned_amount"]) if status in ("APPROVED", "PAID") else None,
                         "receipt_url": T.clip(d["receipt"], 512)})
        t.replace_children("m7_expense_claim_line", "claim_id", cid, rows)
        t.sync_instance("m7_expense_claim", cid, "M7.ExpenseClaim", status, f"ExpenseClaim#{cid}")


def phase_crm(c):
    s, t = c.src, c.tgt
    gst = c.cfg.get("default_gst_pct", "18")
    for it in s.rows("Item", ["name", "item_code", "item_name", "item_group", "brand", "stock_uom", "standard_rate", "valuation_rate",
                              "disabled", "custom_machine_id", "description"], company=False):
        t.upsert("m5_sku_master", {"sku_code": T.clip(it["item_code"] or it["name"], 64)},
                 {"brand": T.clip(it["brand"] or c.cfg.get("default_brand", "PICO"), 64),
                  "product_family": T.clip(it["item_group"] or it["item_name"] or "Item", 64),
                  "offer_variant": T.clip(it["item_name"], 64), "uom_primary": T.clip(it["stock_uom"] or "Nos", 16),
                  "sale_price": T.money(it["standard_rate"]) if it["standard_rate"] else None,
                  "purchase_price": T.money(it["valuation_rate"]) if it["valuation_rate"] else None,
                  "machine_id": T.clip(it["custom_machine_id"], 64), "status": "INACTIVE" if T.truthy(it["disabled"]) else "ACTIVE"},
                 insert_only={"carton_weight_kg": 0, "gst_rate_pct": gst, "batch_tracked": 0})
    # Customers
    taken = {r["customer_code"] for r in t.q("SELECT customer_code FROM m8_customer WHERE tenant_id=%s", (t.tenant,))}
    existing = t.ids("m8_customer")
    for cu in s.rows("Customer", ["name", "customer_name", "customer_type", "territory", "mobile_no", "email_id", "tax_id", "disabled",
                                  "lead_name", "custom_city_town", "custom_sales_person", "primary_address"], company=False):
        code = None if cu["name"] in existing else T.slug_code(cu["name"], 32, taken)
        t.upsert("m8_customer", {"erp_ref": cu["name"]},
                 {"customer_name": T.clip(cu["customer_name"] or cu["name"], 256), "customer_type": T.customer_type(cu["customer_type"]),
                  "mobile_no": T.clip(cu["mobile_no"], 32), "email_id": T.clip(cu["email_id"], 128), "gstin": T.clip(cu["tax_id"], 16),
                  "address": T.clip(T.strip_html(cu["primary_address"]), 512), "territory": T.clip(cu["territory"], 64),
                  "city_town": T.clip(cu["custom_city_town"], 64), "sales_person_emp_id": c.sales_person.get(cu["custom_sales_person"]),
                  "is_active": 0 if T.truthy(cu["disabled"]) else 1},
                 insert_only={"customer_code": code, "source": "IMPORT"} if code else None)
    customers = t.ids("m8_customer")
    # Leads
    for ld in s.rows("Lead", ["name", "lead_name", "company_name", "email_id", "mobile_no", "phone", "status", "qualification_status",
                              "source", "territory", "city", "city_town", "sales_person", "lead_owner", "lead_created_by", "customer", "creation", "modified"]):
        stage = T.lead_stage(ld["status"])
        lid = t.upsert("m8_lead", {"erp_ref": ld["name"]},
                       {"lead_name": T.clip(ld["lead_name"] or ld["company_name"] or ld["name"], 256), "contact_name": T.clip(ld["lead_name"], 128),
                        "company_name": T.clip(ld["company_name"], 256), "contact_phone": T.clip(ld["mobile_no"] or ld["phone"], 32),
                        "contact_email": T.clip(ld["email_id"], 128), "source": T.clip(ld["source"], 32), "territory": T.clip(ld["territory"], 64),
                        "city_town": T.clip(ld["city_town"] or ld["city"], 64), "sales_person_emp_id": c.sales_person.get(ld["sales_person"]),
                        "owner_id": c.emp_by_user.get((ld["lead_owner"] or "").lower()), "created_by_emp_id": c.emp_by_full_name(ld["lead_created_by"]),
                        "qualification_status": T.qualification(ld["qualification_status"]), "stage": stage, "status": stage, "current_step": stage,
                        "customer_id": customers.get(ld["customer"]), "closed_at": ld["modified"] if stage in ("WON", "LOST") else None},
                       insert_only={"lead_number": T.clip(ld["name"], 32)})
        if not lid:
            continue
        t.sync_instance("m8_lead", lid, "M8.Lead", stage, f"Lead#{lid}")
    leads = t.ids("m8_lead")
    for cu in s.rows("Customer", ["name", "lead_name"], "lead_name IS NOT NULL AND lead_name<>''", company=False):
        if cu["name"] in customers and cu["lead_name"] in leads:
            t.x("UPDATE m8_customer SET lead_id=%s WHERE id=%s AND lead_id IS NULL", (leads[cu["lead_name"]], customers[cu["name"]]))
            t.x("UPDATE m8_lead SET customer_id=%s WHERE id=%s", (customers[cu["name"]], leads[cu["lead_name"]]))
    # Quotations
    skus = {r["sku_code"] for r in t.q("SELECT sku_code FROM m5_sku_master WHERE tenant_id=%s", (t.tenant,))}
    for qt in s.rows("Quotation", ["name", "quotation_to", "party_name", "customer_name", "transaction_date", "valid_till", "net_total",
                                   "total_taxes_and_charges", "grand_total", "status", "docstatus", "quotation_created_by", "modified"],
                     "docstatus<2", order="transaction_date"):
        status = T.quote_status(qt["status"], qt["docstatus"])
        to_customer = qt["quotation_to"] == "Customer"
        qid = t.upsert("m8_quote", {"erp_ref": qt["name"]},
                       {"quote_to": "CUSTOMER" if to_customer else "LEAD", "customer_id": customers.get(qt["party_name"]) if to_customer else None,
                        "lead_id": None if to_customer else leads.get(qt["party_name"]),
                        "customer_name": T.clip(qt["customer_name"] or qt["party_name"], 256), "quote_date": qt["transaction_date"],
                        "valid_until": qt["valid_till"], "subtotal": T.money(qt["net_total"]), "tax_total": T.money(qt["total_taxes_and_charges"]),
                        "total_value": T.money(qt["grand_total"]), "status": status, "current_step": status,
                        "created_by_emp_id": c.emp_by_full_name(qt["quotation_created_by"]),
                        "decided_at": qt["modified"] if status in ("ACCEPTED", "REJECTED", "EXPIRED") else None},
                       insert_only={"quote_number": T.clip(qt["name"], 32)})
        if not qid:
            continue
        lines = []
        for li in s.rows("Quotation Item", ["item_code", "item_name", "qty", "uom", "rate", "amount"], "parent=%s", (qt["name"],),
                         company=False, delta=False, order="idx"):
            if li["item_code"] not in skus or not li["qty"] or li["qty"] <= 0:
                c.warn(f"quotation {qt['name']}: item {li['item_code']} not migrated or zero qty -- line skipped")
                continue
            lines.append({"sku_code": li["item_code"], "item_name": T.clip(li["item_name"], 256), "qty": li["qty"], "uom": T.clip(li["uom"], 16),
                          "rate": T.money(li["rate"]), "gst_rate_pct": 0, "amount": T.money(li["amount"]), "tax_amount": 0})
        t.replace_children("m8_quote_line", "quote_id", qid, lines)
        t.sync_instance("m8_quote", qid, "M8.Quote", status, f"Quote#{qid}")


def phase_service(c):
    s, t = c.src, c.tgt
    customers = t.ids("m8_customer")
    for ir in s.rows("Installation Report", ["name", "customer", "employee", "address", "contact_person_name", "contact_number", "email",
                                            "uw", "amc", "ow", "o", "doc_no", "date", "call_receipt_date", "nature_of_call", "call_date_from",
                                            "to", "machine_type", "machine_model", "brief_of_primary_communication_with_or_by_customer",
                                            "pre_work_observation", "action_summary", "remarks_by_customer", "work_done_nature",
                                            "system_operation", "suggestions_for_trouble", "purpose_reason", "verified_otp",
                                            "otp_verified_on", "docstatus", "creation"], order="date"):
        eid, cid = c.emp.get(ir["employee"]), customers.get(ir["customer"])
        if not eid or not cid:
            c.warn(f"installation report {ir['name']}: engineer or customer not migrated -- skipped")
            continue
        status = T.install_status(ir["verified_otp"], ir["docstatus"])
        # The OTP fields are NOT migrated: ERPNext kept the code in plain text. Open reports need a resend.
        rid = t.upsert("m9_installation_report", {"erp_ref": ir["name"]},
                       {"doc_no": T.clip(ir["doc_no"], 64), "report_date": ir["date"] or ir["creation"].date(), "engineer_emp_id": eid, "customer_id": cid,
                        "address": T.clip(ir["address"], 512), "contact_person_name": T.clip(ir["contact_person_name"], 128),
                        "contact_number": T.clip(ir["contact_number"], 32), "email": T.clip(ir["email"], 128),
                        "warranty_kind": T.warranty_kind(ir["uw"], ir["amc"], ir["ow"], ir["o"]), "call_receipt_date": ir["call_receipt_date"],
                        "nature_of_call": T.clip(ir["nature_of_call"], 140), "call_date_from": ir["call_date_from"], "call_date_to": ir["to"],
                        "machine_type": T.clip(ir["machine_type"], 140), "machine_model": T.clip(ir["machine_model"], 140),
                        "primary_communication": ir["brief_of_primary_communication_with_or_by_customer"], "pre_work_observation": ir["pre_work_observation"],
                        "action_summary": ir["action_summary"], "remarks_by_customer": ir["remarks_by_customer"],
                        "work_done_nature": int(T.truthy(ir["work_done_nature"])), "system_operation": int(T.truthy(ir["system_operation"])),
                        "suggestions_for_trouble": int(T.truthy(ir["suggestions_for_trouble"])), "purpose_reason": int(T.truthy(ir["purpose_reason"])),
                        "status": status, "current_step": status,
                        "otp_verified_at": ir["otp_verified_on"] if status == "VERIFIED" else None,
                        "otp_sent_to": "ERPNext (migrated)" if status == "VERIFIED" else None,
                        "closed_at": ir["otp_verified_on"] if status == "VERIFIED" else None},
                       insert_only={"report_number": T.clip(ir["name"], 32)})
        if not rid:
            continue
        t.sync_instance("m9_installation_report", rid, "M9.InstallationReport", status, f"InstallationReport#{rid}")
    for iss in s.rows("Issue", ["name", "subject", "customer", "customer_name", "raised_by", "status", "priority", "issue_type", "description",
                                "resolution_details", "opening_date", "creation", "modified"], order="creation"):
        status = T.issue_status(iss["status"])
        cust = customers.get(iss["customer"])
        tid = t.upsert("m9_service_ticket", {"erp_ref": iss["name"]},
                       {"customer_name": T.clip(iss["customer_name"] or iss["customer"] or "Unknown", 256), "customer_id": cust,
                        "subject": T.clip(iss["subject"], 256), "description": T.strip_html(iss["description"]),
                        "issue_kind": T.clip(iss["issue_type"], 32), "priority": T.issue_priority(iss["priority"]),
                        "contact_email": T.clip(iss["raised_by"], 128), "source": "IMPORT", "reported_at": iss["creation"],
                        "resolution": T.strip_html(iss["resolution_details"]), "status": status, "current_step": status,
                        "closed_at": iss["modified"] if status == "CLOSED" else None},
                       insert_only={"ticket_no": T.clip(iss["name"], 32)})
        if not tid:
            continue
        comments = [{"author_emp_id": c.emp_by_user.get((cm["owner"] or "").lower()), "author_name": T.clip(cm["comment_by"] or cm["owner"] or "ERPNext", 128),
                     "comment": T.strip_html(cm["content"]), "is_internal": 1, "created_at": cm["creation"]}
                    for cm in s.rows("Comment", ["owner", "comment_by", "content", "creation", "comment_type"],
                                     "reference_doctype='Issue' AND reference_name=%s AND comment_type='Comment'", (iss["name"],),
                                     company=False, delta=False, order="creation") if T.strip_html(cm["content"])]
        t.replace_children("m9_ticket_comment", "ticket_id", tid, comments)
        t.sync_instance("m9_service_ticket", tid, "M9.ServiceTicket", status, f"ServiceTicket#{tid}")


# Columns that may still hold an ERPNext /files/... URL after the other phases.
FILE_COLUMNS = [("m7_employee", "photo_url"), ("m7_employee_checkin", "image_url"), ("m7_expense_claim_line", "receipt_url")]


def phase_files(c):
    """Copy ERPNext files referenced by migrated rows into Aria's upload dir and rewrite the URLs.
    Only jpg / png / webp / pdf (the UploadGuard allow-list, by magic bytes) are copied; anything
    else is cleared and reported. Needs erp_files_dir (sites/<site>) and aria_uploads_dir."""
    t = c.tgt
    src_dir, dst_dir = c.cfg.get("erp_files_dir"), c.cfg.get("aria_uploads_dir")
    if not src_dir or not dst_dir:
        c.warn("files phase: erp_files_dir / aria_uploads_dir not configured -- skipped")
        return
    os.makedirs(os.path.join(dst_dir, "erp"), exist_ok=True)

    def move(url):
        ref = T.erp_file_path(url)
        if not ref:
            return url if not str(url).startswith(("/files/", "/private/files/")) else None
        path = os.path.join(src_dir, "private" if ref[0] == "private" else "public", "files", ref[1])
        if not os.path.isfile(path):
            c.warn(f"file not found: {path}")
            return None
        with open(path, "rb") as f:
            head = f.read(12)
        kind = T.sniff(head)
        if not kind:
            c.warn(f"file type not allowed (only jpg/png/webp/pdf): {path}")
            return None
        with open(path, "rb") as f:
            h = hashlib.sha1(f.read()).hexdigest()[:20]
        name = f"erp-{h}.{kind}"
        if not c.cfg.get("dry_run"):
            shutil.copyfile(path, os.path.join(dst_dir, "erp", name))
        t.stats["files"]["copied"] += 1
        return f"/uploads/erp/{name}"

    for table, col in FILE_COLUMNS:
        for r in t.q(f"SELECT id, `{col}` AS u FROM `{table}` WHERE tenant_id=%s AND (`{col}` LIKE '/files/%%' OR `{col}` LIKE '/private/files/%%' OR `{col}` LIKE 'http%%/files/%%')", (t.tenant,)):
            t.x(f"UPDATE `{table}` SET `{col}`=%s WHERE id=%s", (move(r["u"]), r["id"]))
    for r in t.q("SELECT setting_value AS u FROM m0_tenant_setting WHERE tenant_id=%s AND setting_key='branding.logo_url' AND setting_value LIKE '%%/files/%%'", (t.tenant,)):
        t.setting("branding.logo_url", move(r["u"]))
    t.x("UPDATE employee_details u JOIN m7_employee e ON e.user_id=u.id SET u.photo=e.photo_url WHERE e.tenant_id=%s AND e.photo_url LIKE '/uploads/erp/%%'", (t.tenant,))


PHASE_FN = {"masters": phase_masters, "people": phase_people, "salary": phase_salary, "attendance": phase_attendance, "leave": phase_leave,
            "payroll": phase_payroll, "expense": phase_expense, "crm": phase_crm, "service": phase_service, "files": phase_files}


# =============================================================== reconciliation

RECON = [
    # (label, ERPNext SQL (company-filtered with %(c)s), Aria SQL (tenant %(t)s))
    ("employees", "SELECT COUNT(*) n FROM tabEmployee WHERE company=%(c)s", "SELECT COUNT(*) n FROM m7_employee WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL"),
    ("checkins", "SELECT COUNT(*) n FROM `tabEmployee Checkin` ec JOIN tabEmployee e ON e.name=ec.employee WHERE e.company=%(c)s AND ec.log_type IN ('IN','OUT')",
     "SELECT COUNT(*) n FROM m7_employee_checkin WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL"),
    ("attendance (submitted)", "SELECT COUNT(*) n FROM tabAttendance WHERE company=%(c)s AND docstatus=1",
     "SELECT COUNT(*) n FROM m7_attendance WHERE tenant_id=%(t)s AND source='IMPORT'"),
    ("leave applications", "SELECT COUNT(*) n FROM `tabLeave Application` WHERE company=%(c)s", "SELECT COUNT(*) n FROM m7_leave_request WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL"),
    ("leave days approved", "SELECT COALESCE(SUM(total_leave_days),0) n FROM `tabLeave Application` WHERE company=%(c)s AND docstatus=1 AND status='Approved'",
     "SELECT COALESCE(SUM(days),0) n FROM m7_leave_request WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL AND status='APPROVED' AND cancel_status IS NULL"),
    ("salary slips", "SELECT COUNT(*) n FROM `tabSalary Slip` WHERE company=%(c)s AND docstatus=1", "SELECT COUNT(*) n FROM m7_payroll_run WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL"),
    ("net pay total", "SELECT COALESCE(SUM(net_pay),0) n FROM `tabSalary Slip` WHERE company=%(c)s AND docstatus=1",
     "SELECT COALESCE(SUM(net_pay),0) n FROM m7_payroll_run WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL"),
    ("expense claims", "SELECT COUNT(*) n FROM `tabExpense Claim` WHERE company=%(c)s", "SELECT COUNT(*) n FROM m7_expense_claim WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL"),
    ("expense claimed total", "SELECT COALESCE(SUM(total_claimed_amount),0) n FROM `tabExpense Claim` WHERE company=%(c)s",
     "SELECT COALESCE(SUM(total_claimed),0) n FROM m7_expense_claim WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL"),
    ("leads", "SELECT COUNT(*) n FROM tabLead WHERE company=%(c)s", "SELECT COUNT(*) n FROM m8_lead WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL"),
    ("customers", "SELECT COUNT(*) n FROM tabCustomer", "SELECT COUNT(*) n FROM m8_customer WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL"),
    ("quotations (not cancelled)", "SELECT COUNT(*) n FROM tabQuotation WHERE company=%(c)s AND docstatus<2", "SELECT COUNT(*) n FROM m8_quote WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL"),
    ("installation reports", "SELECT COUNT(*) n FROM `tabInstallation Report` WHERE company=%(c)s", "SELECT COUNT(*) n FROM m9_installation_report WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL"),
    ("issues", "SELECT COUNT(*) n FROM tabIssue WHERE company=%(c)s", "SELECT COUNT(*) n FROM m9_service_ticket WHERE tenant_id=%(t)s AND erp_ref IS NOT NULL"),
]


def reconcile(src, tgt, company):
    print(f"{'check':32} {'ERPNext':>16} {'Aria':>16}  result")
    bad = 0
    for label, s_sql, t_sql in RECON:
        try:
            a = src.one(s_sql, {"c": company})["n"]
        except Exception as e:  # table missing on this site
            a = f"n/a ({type(e).__name__})"
        b = tgt.one(t_sql, {"t": tgt.tenant})["n"]
        ok = str(a) == str(b)
        bad += 0 if ok else 1
        print(f"{label:32} {str(a):>16} {str(b):>16}  {'OK' if ok else 'DIFF'}")
    print("\nDIFF rows are expected where the script skips data on purpose (see the warnings of the load run and MAPPING.md).")
    return bad


# =============================================================== main

def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--config", required=True)
    ap.add_argument("--phases", default=",".join(PHASES))
    ap.add_argument("--since", help="delta load: only source rows with modified >= this timestamp")
    ap.add_argument("--dry-run", action="store_true", help="run every phase in one transaction and roll it back")
    ap.add_argument("--reconcile", action="store_true")
    args = ap.parse_args()

    ini = configparser.ConfigParser()
    ini.read(args.config)
    cfg = dict(ini["migration"])
    cfg["dry_run"] = args.dry_run
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s",
                        handlers=[logging.StreamHandler(), logging.FileHandler(cfg.get("log_file", "migrate.log"), encoding="utf-8")])

    src = Source(dict(ini["erpnext"]), cfg.get("erp_company"), args.since)
    tgt = Target(dict(ini["aria"]), int(cfg["aria_tenant_id"]))
    if not tgt.one("SELECT id FROM tenant WHERE id=%s AND status='ACTIVE'", (tgt.tenant,)):
        sys.exit(f"Aria tenant {tgt.tenant} not found / not ACTIVE")
    if args.reconcile:
        sys.exit(1 if reconcile(src, tgt, cfg.get("erp_company")) else 0)

    phases = [p.strip() for p in args.phases.split(",") if p.strip()]
    unknown = [p for p in phases if p not in PHASE_FN]
    if unknown:
        sys.exit(f"unknown phases {unknown}; known: {PHASES}")
    ctx = Ctx(src, tgt, cfg)
    ctx.load_maps()
    started = datetime.now()
    try:
        for p in [p for p in PHASES if p in phases]:  # always in dependency order
            log.info("== phase %s", p)
            PHASE_FN[p](ctx)
            if not args.dry_run:
                tgt.conn.commit()
    except Exception:
        tgt.conn.rollback()
        log.exception("phase failed -- its changes were rolled back (earlier phases stay committed; re-run is safe)")
        raise
    if args.dry_run:
        tgt.conn.rollback()
        log.info("DRY RUN -- everything rolled back")

    log.info("== summary (%s, %s)", "dry run" if args.dry_run else "committed", datetime.now() - started)
    for table, st in sorted(tgt.stats.items()):
        log.info("  %-28s %s", table, dict(st))
    log.info("  warnings: %d (see %s)", len(ctx.warnings), cfg.get("log_file", "migrate.log"))


if __name__ == "__main__":
    main()
