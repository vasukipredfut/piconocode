# Idayam ERP Backend - Detailed Controller Documentation

## Overview

This document provides comprehensive details about all 106 REST Controllers in the Idayam ERP Backend system. Each controller is documented with:
- Purpose and responsibilities
- Base endpoint path
- Main HTTP methods
- Key endpoints
- Request/Response examples
- Authentication requirements
- Error handling

---

## Table of Contents

[1. Authentication & User Management](#1-authentication--user-management)
[2. HR & Attendance](#2-hr--attendance)
[3. Inventory & Stock](#3-inventory--stock)
[4. Procurement](#4-procurement)
[5. Sales](#5-sales)
[6. Finance & Accounting](#6-finance--accounting)
[7. Production](#7-production)
[8. Facility & Assets](#8-facility--assets)
[9. Administration & Configuration](#9-administration--configuration)
[10. Reports & Analytics](#10-reports--analytics)

---

## 1. Authentication & User Management

### UserController
**Base Path:** `/api` (root level)
**Purpose:** User authentication, registration, profile management, and JWT token handling

**Key Endpoints:**
```
POST   /login                      - User login
POST   /register                   - User registration
POST   /refreshToken              - Refresh JWT token
POST   /logout                     - User logout
GET    /api/user                   - List all users (paginated)
GET    /api/user/{id}              - Get user details
PUT    /api/user/{id}              - Update user profile
PUT    /api/user/{id}/roles        - Assign roles
DELETE /api/user/{id}              - Deactivate user
```

**Request Example - Login:**
```json
{
    "username": "admin@company.com",
    "password": "SecurePassword123"
}
```

**Response Example:**
```json
{
    "id": 1,
    "username": "admin@company.com",
    "email": "admin@company.com",
    "firstName": "Admin",
    "lastName": "User",
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "roles": ["ADMIN", "USER"]
}
```

**Authentication:** 
- Login endpoint: None required
- Other endpoints: JWT Bearer token required

---

### ModulePrivilegeController
**Base Path:** `/api`
**Purpose:** Manage role-based access control, module permissions, and privilege assignments

**Key Endpoints:**
```
POST   /api/modulePrivilege           - Create privilege
GET    /api/modulePrivilege           - List privileges
GET    /api/modulePrivilege/{id}      - Get privilege details
PUT    /api/modulePrivilege/{id}      - Update privilege
DELETE /api/modulePrivilege/{id}      - Delete privilege
GET    /api/role                      - List all roles
POST   /api/role                      - Create new role
PUT    /api/role/{id}/permissions     - Assign permissions to role
```

**Key Features:**
- Module-level permission management
- Role hierarchy support
- Permission inheritance
- Audit trail for privilege changes

---

### MailController
**Base Path:** `/api`
**Purpose:** Send emails and manage email notifications

**Key Endpoints:**
```
POST   /api/mail/send               - Send email
POST   /api/mail/sendBulk           - Send bulk emails
GET    /api/mail/template           - List email templates
POST   /api/mail/template           - Create email template
```

**Supported Email Types:**
- Leave notifications
- Purchase order confirmations
- Sales order updates
- Payment reminders
- System alerts

---

## 2. HR & Attendance

### AttendanceController
**Base Path:** `/api`
**Purpose:** Employee attendance tracking, punch in/out, and attendance reports

**Key Endpoints:**
```
POST   /api/attendance              - Mark attendance
GET    /api/attendance              - Get attendance records
GET    /api/attendance/{id}         - Get specific attendance
PUT    /api/attendance/{id}         - Update attendance
DELETE /api/attendance/{id}         - Delete attendance record
GET    /api/attendance/report       - Attendance report
```

**Features:**
- Daily punch in/out tracking
- Working hours calculation
- Late arrival detection
- Early departure tracking
- Monthly attendance summary
- Integration with biometric systems

---

### LeaveController
**Base Path:** `/api`
**Purpose:** Leave application, approval workflow, and leave balance tracking

**Key Endpoints:**
```
POST   /api/leave                   - Apply for leave
GET    /api/leave                   - Get leave applications
GET    /api/leave/{id}              - Get leave details
PUT    /api/leave/{id}/approve      - Approve leave
PUT    /api/leave/{id}/reject       - Reject leave
GET    /api/leave/balance           - Get leave balance
GET    /api/leave/report            - Leave report
```

**Leave Types Supported:**
- Casual Leave
- Sick Leave
- Annual Leave
- Maternity Leave
- Paternity Leave
- Unpaid Leave
- Custom leave types

**Workflow:**
1. Employee applies → Status: PENDING
2. Manager approves/rejects → Status: APPROVED/REJECTED
3. System updates leave balance
4. Attendance auto-marked

---

### SalaryController
**Base Path:** `/api`
**Purpose:** Salary processing, payroll calculations, and salary slip generation

**Key Endpoints:**
```
POST   /api/salary                  - Create salary record
GET    /api/salary                  - Get salary records
GET    /api/salary/{id}             - Get salary details
PUT    /api/salary/{id}             - Update salary
GET    /api/salary/{id}/slip        - Generate salary slip
GET    /api/salary/report           - Salary report
POST   /api/salary/process          - Process payroll
```

**Features:**
- Basic salary, allowances, deductions
- Statutory deductions (PF, ESI, TDS)
- Overtime calculations
- Bonus and incentives
- Salary slip generation
- Monthly payroll processing
- Year-end settlement

---

### ShiftsController
**Base Path:** `/api`
**Purpose:** Shift definitions and configurations

**Key Endpoints:**
```
POST   /api/shift                   - Create shift
GET    /api/shift                   - List shifts
GET    /api/shift/{id}              - Get shift details
PUT    /api/shift/{id}              - Update shift
DELETE /api/shift/{id}              - Delete shift
```

**Shift Parameters:**
- Shift name and code
- Start and end time
- Break duration
- Working hours calculation
- Active/inactive status

---

### ShiftAllotmentController
**Base Path:** `/api`
**Purpose:** Assign shifts to employees

**Key Endpoints:**
```
POST   /api/shiftAllotment          - Assign shift to employee
GET    /api/shiftAllotment          - Get assignments
PUT    /api/shiftAllotment/{id}     - Update assignment
DELETE /api/shiftAllotment/{id}     - Cancel assignment
GET    /api/shiftAllotment/roster   - Get shift roster
```

---

### DutyController
**Base Path:** `/api`
**Purpose:** Plan and assign employee duties

**Key Endpoints:**
```
POST   /api/duty                    - Create duty
GET    /api/duty                    - List duties
PUT    /api/duty/{id}               - Update duty
DELETE /api/duty/{id}               - Delete duty
GET    /api/duty/roster             - Duty roster
```

---

### DutyUnplannedController
**Base Path:** `/api`
**Purpose:** Handle unplanned duty assignments and emergency duty allocation

**Key Endpoints:**
```
POST   /api/unplannedDuty           - Assign unplanned duty
GET    /api/unplannedDuty           - Get unplanned duties
PUT    /api/unplannedDuty/{id}      - Update unplanned duty
DELETE /api/unplannedDuty/{id}      - Cancel unplanned duty
```

---

### TrainingController
**Base Path:** `/api`
**Purpose:** Employee training and skill development programs

**Key Endpoints:**
```
POST   /api/training                - Create training program
GET    /api/training                - List training programs
POST   /api/training/{id}/enroll    - Enroll employee
GET    /api/training/{id}/attendees - Get attendees
PUT    /api/training/{id}/complete  - Mark complete
```

---

### CompentencyMatrixController
**Base Path:** `/api`
**Purpose:** Define and track employee competencies

**Key Endpoints:**
```
POST   /api/competencyMatrix        - Create competency
GET    /api/competencyMatrix        - List competencies
PUT    /api/competencyMatrix/{id}   - Update competency
GET    /api/employee/{id}/competency - Get employee competencies
```

---

### SkillGradeLevelController
**Base Path:** `/api`
**Purpose:** Define skill levels and grades

**Key Endpoints:**
```
POST   /api/skillGrade              - Create skill grade
GET    /api/skillGrade              - List skill grades
PUT    /api/skillGrade/{id}         - Update skill grade
```

---

### MedicalCheckupController
**Base Path:** `/api`
**Purpose:** Track employee medical checkups and health records

**Key Endpoints:**
```
POST   /api/medicalCheckup          - Create checkup record
GET    /api/medicalCheckup          - Get checkup records
GET    /api/medicalCheckup/{id}     - Get checkup details
PUT    /api/medicalCheckup/{id}     - Update checkup record
```

---

### WelfareController & WelfareRegistrationController
**Base Path:** `/api`
**Purpose:** Employee welfare programs and benefits management

**Key Endpoints:**
```
POST   /api/welfare                 - Create welfare program
GET    /api/welfare                 - List programs
POST   /api/welfare/{id}/register   - Register employee
GET    /api/welfareRegistration     - Get registrations
```

---

### FingerPrintController
**Base Path:** `/api`
**Purpose:** Integration with biometric/fingerprint attendance systems

**Key Endpoints:**
```
POST   /api/fingerprint/sync        - Sync biometric data
GET    /api/fingerprint/data        - Get fingerprint records
POST   /api/fingerprint/import      - Import from device
```

---

### CwagesController & CWagesSalaryController
**Base Path:** `/api`
**Purpose:** Casual wage workers and their salary management

**Key Endpoints:**
```
POST   /api/cwages                  - Create casual wage worker
GET    /api/cwages                  - List casual workers
POST   /api/cwages/{id}/salary      - Process casual wage
GET    /api/cwagesSalary            - Get wage records
```

---

### AwardController
**Base Path:** `/api`
**Purpose:** Employee awards and recognition

**Key Endpoints:**
```
POST   /api/award                   - Create award
GET    /api/award                   - List awards
POST   /api/award/{id}/recipient    - Assign to employee
GET    /api/award/recipient         - Get recipients
```

---

## 3. Inventory & Stock

### BinDetailsController
**Base Path:** `/api`
**Purpose:** Warehouse bin/storage location management

**Key Endpoints:**
```
POST   /api/binDetails              - Create bin
GET    /api/binDetails              - List bins
GET    /api/binDetails/{id}         - Get bin details
PUT    /api/binDetails/{id}         - Update bin
DELETE /api/binDetails/{id}         - Delete bin
GET    /api/binDetails/warehouse    - Get bins in warehouse
```

**Bin Information:**
- Bin code and location
- Capacity and dimensions
- Current stock
- Warehouse assignment

---

### BinStockController
**Base Path:** `/api`
**Purpose:** Track stock levels in individual bins

**Key Endpoints:**
```
GET    /api/binStock                - Get stock levels
GET    /api/binStock/{binId}        - Get bin stock
PUT    /api/binStock/{binId}        - Update stock
POST   /api/binStock/transfer       - Transfer between bins
```

---

### GoDownController & GoDownSummaryController
**Base Path:** `/api`
**Purpose:** Warehouse/godown management and summary reports

**Key Endpoints:**
```
POST   /api/godown                  - Create warehouse
GET    /api/godown                  - List warehouses
GET    /api/godown/{id}             - Get warehouse details
PUT    /api/godown/{id}             - Update warehouse
GET    /api/godownSummary           - Get warehouse summary
```

**Warehouse Tracking:**
- Total capacity and usage
- Inventory levels
- Stock movement
- Storage assignments

---

### StockLedgerController
**Base Path:** `/api`
**Purpose:** Complete stock movement history and ledger

**Key Endpoints:**
```
GET    /api/stockLedger             - Get stock ledger
GET    /api/stockLedger/{productId} - Product stock history
POST   /api/stockLedger/report      - Generate stock report
GET    /api/stockLedger/fifo        - Calculate FIFO cost
GET    /api/stockLedger/lifo        - Calculate LIFO cost
```

**Features:**
- Stock movement tracking
- FIFO/LIFO calculations
- Variance analysis
- Stock aging reports

---

### MaterialRequestController
**Base Path:** `/api`
**Purpose:** Material request submission and approval workflow

**Key Endpoints:**
```
POST   /api/materialRequest         - Create material request
GET    /api/materialRequest         - List requests
GET    /api/materialRequest/{id}    - Get request details
PUT    /api/materialRequest/{id}/approve - Approve
PUT    /api/materialRequest/{id}/reject  - Reject
POST   /api/materialRequest/{id}/issue   - Issue materials
```

**Workflow:**
1. Create request → DRAFT
2. Submit → PENDING
3. Manager approves → APPROVED
4. Store issues materials → ISSUED
5. Complete → CLOSED

---

### MaterialIssueController
**Base Path:** `/api`
**Purpose:** Issue materials from warehouse to departments

**Key Endpoints:**
```
POST   /api/materialIssue           - Create issue
GET    /api/materialIssue           - Get issues
GET    /api/materialIssue/{id}      - Get issue details
POST   /api/materialIssue/{id}/confirm - Confirm receipt
PUT    /api/materialIssue/{id}/return  - Handle return
```

---

### MaterialReturnController
**Base Path:** `/api`
**Purpose:** Handle material returns and reverse issuance

**Key Endpoints:**
```
POST   /api/materialReturn          - Create return
GET    /api/materialReturn          - List returns
POST   /api/materialReturn/{id}/accept  - Accept return
POST   /api/materialReturn/{id}/reject  - Reject return
```

---

### InternalMaterialIssueController
**Base Path:** `/api`
**Purpose:** Material movement between internal departments

**Key Endpoints:**
```
POST   /api/internalMaterialIssue   - Create inter-dept issue
GET    /api/internalMaterialIssue   - List issues
```

---

### OtherBranchIssueController & OtherBranchReceiptController
**Base Path:** `/api`
**Purpose:** Material movement between different branches

**Key Endpoints:**
```
POST   /api/branchIssue             - Issue to branch
GET    /api/branchIssue             - List issues
POST   /api/branchReceipt           - Receive from branch
GET    /api/branchReceipt           - List receipts
```

---

### WithInBranchDistributionController & MaterialWithInBranchReceiptController
**Base Path:** `/api`
**Purpose:** Within-branch material distribution and receipts

**Key Endpoints:**
```
POST   /api/withinBranchDistribution  - Create distribution
GET    /api/withinBranchDistribution  - List distributions
POST   /api/withinBranchReceipt       - Create receipt
GET    /api/withinBranchReceipt       - List receipts
```

---

### IntraBranchReceiptController
**Base Path:** `/api`
**Purpose:** Intra-branch receipt and movement tracking

**Key Endpoints:**
```
POST   /api/intraBranchReceipt      - Create receipt
GET    /api/intraBranchReceipt      - List receipts
```

---

### DeliteStockMoveRequestController
**Base Path:** `/api`
**Purpose:** Stock movement requests and tracking

**Key Endpoints:**
```
POST   /api/stockMoveRequest        - Create request
GET    /api/stockMoveRequest        - List requests
PUT    /api/stockMoveRequest/{id}   - Update status
```

---

### MaterialFlowGDCController
**Base Path:** `/api`
**Purpose:** Material flow tracking in warehouses

**Key Endpoints:**
```
GET    /api/materialFlow            - Get material flow data
GET    /api/materialFlow/report     - Flow analysis report
```

---

## 4. Procurement

### PurchaseOrderController
**Base Path:** `/api`
**Purpose:** Purchase order creation, management, and tracking

**Key Endpoints:**
```
POST   /api/purchaseOrder           - Create PO
GET    /api/purchaseOrder           - List POs
GET    /api/purchaseOrder/{id}      - Get PO details
PUT    /api/purchaseOrder/{id}      - Update PO
PUT    /api/purchaseOrder/{id}/confirm - Confirm PO
PUT    /api/purchaseOrder/{id}/receive - Receive goods
DELETE /api/purchaseOrder/{id}      - Cancel PO
```

**PO Status:**
- DRAFT
- CONFIRMED
- PARTIALLY_RECEIVED
- RECEIVED
- INVOICED
- CANCELLED

---

### PurchaseBillApproveController
**Base Path:** `/api`
**Purpose:** Approve purchase bills and invoices

**Key Endpoints:**
```
GET    /api/purchaseBill            - List bills
GET    /api/purchaseBill/{id}       - Get bill details
PUT    /api/purchaseBill/{id}/approve - Approve bill
PUT    /api/purchaseBill/{id}/reject  - Reject bill
POST   /api/purchaseBill/{id}/payment - Create payment
```

---

### AdvancePaymentForPurchaseOrderController
**Base Path:** `/api`
**Purpose:** Handle advance payments against purchase orders

**Key Endpoints:**
```
POST   /api/advancePayment          - Create advance payment
GET    /api/advancePayment          - List advance payments
PUT    /api/advancePayment/{id}/adjust - Adjust against bill
GET    /api/advancePayment/report   - Advance analysis
```

---

### VendorController
**Base Path:** `/api`
**Purpose:** Vendor master data and management

**Key Endpoints:**
```
POST   /api/vendor                  - Create vendor
GET    /api/vendor                  - List vendors
GET    /api/vendor/{id}             - Get vendor details
PUT    /api/vendor/{id}             - Update vendor
DELETE /api/vendor/{id}             - Deactivate vendor
GET    /api/vendor/{id}/po-history  - PO history
GET    /api/vendor/{id}/payment-history - Payment history
```

**Vendor Information:**
- Vendor name, code, contact
- Payment terms
- GST/Tax ID
- Bank details
- Performance rating

---

### SupplierEvaluationController
**Base Path:** `/api`
**Purpose:** Evaluate and track supplier performance

**Key Endpoints:**
```
POST   /api/supplierEvaluation      - Create evaluation
GET    /api/supplierEvaluation      - List evaluations
PUT    /api/supplierEvaluation/{id} - Update evaluation
GET    /api/supplierEvaluation/report - Performance report
```

**Evaluation Parameters:**
- Quality score
- Delivery performance
- Compliance
- Price competitiveness
- Overall rating

---

### VendorSisterConcernController & VendorSisterConcernMapController
**Base Path:** `/api`
**Purpose:** Manage sister concerns and vendor relationships

**Key Endpoints:**
```
POST   /api/vendorSisterConcern     - Create sister concern
GET    /api/vendorSisterConcern     - List sister concerns
POST   /api/vendorMap               - Map vendor relationship
GET    /api/vendorMap               - Get mappings
```

---

## 5. Sales

### OrderController
**Base Path:** `/api`
**Purpose:** Sales order creation and management

**Key Endpoints:**
```
POST   /api/order                   - Create sales order
GET    /api/order                   - List orders
GET    /api/order/{id}              - Get order details
PUT    /api/order/{id}              - Update order
PUT    /api/order/{id}/confirm      - Confirm order
PUT    /api/order/{id}/dispatch     - Dispatch order
DELETE /api/order/{id}              - Cancel order
```

---

### SalesController
**Base Path:** `/api`
**Purpose:** Sales transaction processing

**Key Endpoints:**
```
POST   /api/sales                   - Create sales
GET    /api/sales                   - List sales
GET    /api/sales/{id}              - Get sales details
PUT    /api/sales/{id}              - Update sales
POST   /api/sales/{id}/invoice      - Generate invoice
```

---

### SalesReturnController
**Base Path:** `/api`
**Purpose:** Handle sales returns and reverse transactions

**Key Endpoints:**
```
POST   /api/salesReturn             - Create return
GET    /api/salesReturn             - List returns
PUT    /api/salesReturn/{id}/accept - Accept return
POST   /api/salesReturn/{id}/credit - Issue credit note
```

---

### SalesProductFilmController & SalesProductLabelController
**Base Path:** `/api`
**Purpose:** Manage product films and labels for sales

**Key Endpoints:**
```
POST   /api/productFilm             - Create film
GET    /api/productFilm             - List films
POST   /api/productLabel            - Create label
GET    /api/productLabel            - List labels
```

---

### HoSalesController
**Base Path:** `/api`
**Purpose:** Head office sales coordination

**Key Endpoints:**
```
GET    /api/hoSales/report          - Sales report
POST   /api/hoSales/consolidate     - Consolidate sales
```

---

## 6. Finance & Accounting

### VoucherController
**Base Path:** `/api`
**Purpose:** Voucher management (payment, receipt, journal)

**Key Endpoints:**
```
POST   /api/voucher                 - Create voucher
GET    /api/voucher                 - List vouchers
GET    /api/voucher/{id}            - Get voucher details
PUT    /api/voucher/{id}            - Update voucher
POST   /api/voucher/{id}/approve    - Approve voucher
POST   /api/voucher/{id}/post       - Post to ledger
DELETE /api/voucher/{id}            - Delete voucher
```

**Voucher Types:**
- Payment Voucher (PV)
- Receipt Voucher (RV)
- Journal Voucher (JV)
- Contra Voucher (CV)

---

### CustomVoucherController
**Base Path:** `/api`
**Purpose:** Create and manage custom voucher templates

**Key Endpoints:**
```
POST   /api/customVoucher           - Create custom voucher
GET    /api/customVoucher           - List custom vouchers
POST   /api/customVoucher/{id}/template - Create template
```

---

### JournalController
**Base Path:** `/api`
**Purpose:** Journal entry recording and management

**Key Endpoints:**
```
POST   /api/journal                 - Create journal entry
GET    /api/journal                 - List entries
GET    /api/journal/{id}            - Get entry details
PUT    /api/journal/{id}            - Update entry
DELETE /api/journal/{id}            - Delete entry
POST   /api/journal/{id}/post       - Post to ledger
```

---

### CreditNoteController
**Base Path:** `/api`
**Purpose:** Create and manage credit notes

**Key Endpoints:**
```
POST   /api/creditNote              - Create credit note
GET    /api/creditNote              - List credit notes
GET    /api/creditNote/{id}         - Get note details
PUT    /api/creditNote/{id}         - Update note
POST   /api/creditNote/{id}/apply   - Apply to invoice
```

---

### BillController
**Base Path:** `/api`
**Purpose:** Bill creation, tracking, and payment

**Key Endpoints:**
```
POST   /api/bill                    - Create bill
GET    /api/bill                    - List bills
GET    /api/bill/{id}               - Get bill details
PUT    /api/bill/{id}               - Update bill
POST   /api/bill/{id}/payment       - Record payment
GET    /api/bill/aging              - Aging report
```

---

### CostExpenseController
**Base Path:** `/api`
**Purpose:** Track costs and expenses

**Key Endpoints:**
```
POST   /api/costExpense             - Create cost
GET    /api/costExpense             - List costs
PUT    /api/costExpense/{id}        - Update cost
GET    /api/costExpense/report      - Cost analysis
```

---

### ExpenseController & ExpenseCategoryController
**Base Path:** `/api`
**Purpose:** Record and categorize expenses

**Key Endpoints:**
```
POST   /api/expense                 - Create expense
GET    /api/expense                 - List expenses
POST   /api/expenseCategory         - Create category
GET    /api/expenseCategory         - List categories
GET    /api/expense/report          - Expense report
```

---

### ViewAccountsLedgerController
**Base Path:** `/api`
**Purpose:** View general ledger accounts

**Key Endpoints:**
```
GET    /api/ledger                  - List ledgers
GET    /api/ledger/{accountCode}    - Get ledger details
GET    /api/ledger/{id}/postings    - Get postings
GET    /api/ledger/report/trial-balance - Trial balance
GET    /api/ledger/report/profit-loss   - P&L statement
GET    /api/ledger/report/balance-sheet - Balance sheet
```

---

### OtherDeductionController
**Base Path:** `/api`
**Purpose:** Manage additional deductions

**Key Endpoints:**
```
POST   /api/otherDeduction          - Create deduction
GET    /api/otherDeduction          - List deductions
PUT    /api/otherDeduction/{id}     - Update deduction
```

---

### EpayMasterController & DepayMasterController
**Base Path:** `/api`
**Purpose:** E-Pay and deduction payment management

**Key Endpoints:**
```
POST   /api/epayMaster              - Create e-pay
GET    /api/epayMaster              - List e-pay
POST   /api/depayMaster             - Create deduction payment
GET    /api/depayMaster             - List deduction payments
```

---

### BankDetailsController & BankReptController
**Base Path:** `/api`
**Purpose:** Bank account management and reconciliation

**Key Endpoints:**
```
POST   /api/bankDetails             - Create bank account
GET    /api/bankDetails             - List accounts
PUT    /api/bankDetails/{id}        - Update account
POST   /api/bankReconciliation      - Create reconciliation
GET    /api/bankReconciliation      - List reconciliations
PUT    /api/bankReconciliation/{id}/confirm - Confirm
```

---

## 7. Production

### ProductionEntryController
**Base Path:** `/api`
**Purpose:** Record production completion and output

**Key Endpoints:**
```
POST   /api/productionEntry         - Create entry
GET    /api/productionEntry         - List entries
PUT    /api/productionEntry/{id}    - Update entry
POST   /api/productionEntry/{id}/complete - Complete
```

---

### BlendingAdviceController
**Base Path:** `/api`
**Purpose:** Issue blending instructions and recipes

**Key Endpoints:**
```
POST   /api/blendingAdvice          - Create advice
GET    /api/blendingAdvice          - List advice
GET    /api/blendingAdvice/{id}     - Get details
```

---

### JaggeryInspectionController
**Base Path:** `/api`
**Purpose:** Jaggery quality inspection and testing

**Key Endpoints:**
```
POST   /api/jaggeryInspection       - Create inspection
GET    /api/jaggeryInspection       - List inspections
POST   /api/jaggeryInspection/{id}/approve - Approve
POST   /api/jaggeryInspection/{id}/reject - Reject
```

---

### AdminInspectionController
**Base Path:** `/api`
**Purpose:** Administrative inspection and quality control

**Key Endpoints:**
```
POST   /api/adminInspection         - Create inspection
GET    /api/adminInspection         - List inspections
```

---

### InwardInspectionEntryController
**Base Path:** `/api`
**Purpose:** Inspect incoming goods and materials

**Key Endpoints:**
```
POST   /api/inwardInspection        - Create inspection
GET    /api/inwardInspection        - List inspections
POST   /api/inwardInspection/{id}/approve - Approve
POST   /api/inwardInspection/{id}/reject - Reject (return)
```

---

### QcInProcessController
**Base Path:** `/api`
**Purpose:** Quality control during production process

**Key Endpoints:**
```
POST   /api/qcInProcess             - Create QC check
GET    /api/qcInProcess             - List QC checks
POST   /api/qcInProcess/{id}/pass   - Pass QC
POST   /api/qcInProcess/{id}/fail   - Fail QC
```

---

### ProductDistruptanciesController
**Base Path:** `/api`
**Purpose:** Track production disruptions and downtime

**Key Endpoints:**
```
POST   /api/disruption              - Create disruption record
GET    /api/disruption              - List disruptions
POST   /api/disruption/{id}/resolve - Resolve disruption
GET    /api/disruption/report       - Disruption report
```

---

### PMScheduleController
**Base Path:** `/api`
**Purpose:** Preventive maintenance scheduling

**Key Endpoints:**
```
POST   /api/pmSchedule              - Create PM schedule
GET    /api/pmSchedule              - List schedules
POST   /api/pmSchedule/{id}/complete - Mark complete
GET    /api/pmSchedule/overdue      - Overdue maintenance
```

---

### ProposalController & ProposalParamInspectionScoreController
**Base Path:** `/api`
**Purpose:** Production proposals and inspection scoring

**Key Endpoints:**
```
POST   /api/proposal                - Create proposal
GET    /api/proposal                - List proposals
POST   /api/proposal/{id}/score     - Add inspection score
```

---

### PurchaseQualityInspectionScoreController
**Base Path:** `/api`
**Purpose:** Quality inspection scoring for purchases

**Key Endpoints:**
```
POST   /api/purchaseQI              - Create QI score
GET    /api/purchaseQI              - List scores
PUT    /api/purchaseQI/{id}         - Update score
```

---

## 8. Facility & Assets

### MachineController
**Base Path:** `/api`
**Purpose:** Machine master and maintenance tracking

**Key Endpoints:**
```
POST   /api/machine                 - Create machine record
GET    /api/machine                 - List machines
GET    /api/machine/{id}            - Get machine details
PUT    /api/machine/{id}            - Update machine
POST   /api/machine/{id}/maintenance - Record maintenance
GET    /api/machine/{id}/maintenance-history - History
```

---

### VehicleMovementController
**Base Path:** `/api`
**Purpose:** Track vehicle movements and locations

**Key Endpoints:**
```
POST   /api/vehicleMovement         - Create movement record
GET    /api/vehicleMovement         - List movements
GET    /api/vehicleMovement/report  - Movement report
```

---

### VehicleRequestController
**Base Path:** `/api`
**Purpose:** Request and allocate vehicles

**Key Endpoints:**
```
POST   /api/vehicleRequest          - Create request
GET    /api/vehicleRequest          - List requests
PUT    /api/vehicleRequest/{id}/approve - Approve
PUT    /api/vehicleRequest/{id}/allocate - Allocate vehicle
PUT    /api/vehicleRequest/{id}/complete - Complete
```

---

### TransportController
**Base Path:** `/api`
**Purpose:** Main transport management and operations

**Key Endpoints:**
```
POST   /api/transport               - Create transport
GET    /api/transport               - List transports
PUT    /api/transport/{id}          - Update transport
GET    /api/transport/cost-analysis - Cost analysis
```

---

### TransportDriverController
**Base Path:** `/api`
**Purpose:** Driver management and assignment

**Key Endpoints:**
```
POST   /api/driver                  - Create driver record
GET    /api/driver                  - List drivers
POST   /api/driver/{id}/assign      - Assign to trip
```

---

### TransportTripController
**Base Path:** `/api`
**Purpose:** Manage transportation trips

**Key Endpoints:**
```
POST   /api/trip                    - Create trip
GET    /api/trip                    - List trips
PUT    /api/trip/{id}/start         - Start trip
PUT    /api/trip/{id}/complete      - Complete trip
GET    /api/trip/{id}/details       - Trip details
```

---

### TransportTripExpenseController
**Base Path:** `/api`
**Purpose:** Track trip expenses

**Key Endpoints:**
```
POST   /api/tripExpense             - Create expense
GET    /api/tripExpense             - List expenses
GET    /api/tripExpense/report      - Expense report
```

---

### FumigationController & RodentMonitorController
**Base Path:** `/api`
**Purpose:** Pest control and monitoring

**Key Endpoints:**
```
POST   /api/fumigation              - Create fumigation record
GET    /api/fumigation              - List fumigations
POST   /api/rodentMonitor           - Create monitor record
GET    /api/rodentMonitor           - List monitoring
```

---

### WindmillController
**Base Path:** `/api`
**Purpose:** Windmill/renewable energy management

**Key Endpoints:**
```
POST   /api/windmill                - Create windmill record
GET    /api/windmill                - List windmills
GET    /api/windmill/{id}/output    - Output tracking
```

---

### FactoryToHoController
**Base Path:** `/api`
**Purpose:** Factory to head office coordination

**Key Endpoints:**
```
GET    /api/factoryToHo/report      - Factory-HO report
POST   /api/factoryToHo/handover    - Create handover
```

---

## 9. Administration & Configuration

### CommonController
**Base Path:** `/api`
**Purpose:** Common operations - branch, department, lot management

**Key Endpoints:**
```
POST   /api/saveBranch              - Create branch
GET    /api/branch/{id}             - Get branch details
PUT    /api/updateBranch            - Update branch
DELETE /api/deleteBranch/{id}       - Delete branch
GET    /api/branchDetailList        - List branches

POST   /api/saveDepartment          - Create department
GET    /api/department/{id}         - Get dept details
PUT    /api/updateDepartment        - Update department

POST   /api/lot                     - Create lot
GET    /api/lot                     - List lots
GET    /api/lot/{id}/values         - Get lot values
```

---

### MasterController
**Base Path:** `/api`
**Purpose:** Master data management

**Key Endpoints:**
```
GET    /api/master/list             - List master data
POST   /api/master                  - Create master
PUT    /api/master/{id}             - Update master
DELETE /api/master/{id}             - Delete master
```

---

### AttributesController & PAttributesController
**Base Path:** `/api`
**Purpose:** Product and general attribute management

**Key Endpoints:**
```
POST   /api/attribute               - Create attribute
GET    /api/attribute               - List attributes
POST   /api/productAttribute        - Create product attribute
GET    /api/productAttribute        - List product attributes
```

---

### SpecificationController
**Base Path:** `/api`
**Purpose:** Product specification definitions

**Key Endpoints:**
```
POST   /api/specification           - Create specification
GET    /api/specification           - List specifications
PUT    /api/specification/{id}      - Update specification
```

---

### ProductMasterController
**Base Path:** `/api`
**Purpose:** Product master data management

**Key Endpoints:**
```
POST   /api/product                 - Create product
GET    /api/product                 - List products
GET    /api/product/{id}            - Get product details
PUT    /api/product/{id}            - Update product
GET    /api/product/{id}/attributes - Get attributes
GET    /api/product/code/{code}     - Search by code
```

---

### HsnController
**Base Path:** `/api`
**Purpose:** HSN code management for taxation

**Key Endpoints:**
```
POST   /api/hsn                     - Create HSN code
GET    /api/hsn                     - List HSN codes
GET    /api/hsn/{code}              - Get HSN details
```

---

### PanelMasterController
**Base Path:** `/api`
**Purpose:** Panel configuration and management

**Key Endpoints:**
```
POST   /api/panel                   - Create panel
GET    /api/panel                   - List panels
PUT    /api/panel/{id}              - Update panel
```

---

### HoOfficeConfigureController
**Base Path:** `/api`
**Purpose:** Head office configuration

**Key Endpoints:**
```
GET    /api/hoConfig                - Get HO config
PUT    /api/hoConfig                - Update HO config
POST   /api/hoConfig/parameter      - Set parameter
```

---

### DayEndClosingController
**Base Path:** `/api`
**Purpose:** Day-end closure operations

**Key Endpoints:**
```
POST   /api/dayEndClosing           - Create day-end
GET    /api/dayEndClosing           - List day-ends
PUT    /api/dayEndClosing/{id}/finalize - Finalize
GET    /api/dayEndClosing/status    - Check status
```

---

### ActionController
**Base Path:** `/api`
**Purpose:** Action configuration and tracking

**Key Endpoints:**
```
POST   /api/action                  - Create action
GET    /api/action                  - List actions
POST   /api/actionConfiguration     - Create config
GET    /api/actionConfiguration     - List configs
```

---

### FileStorageController
**Base Path:** `/api`
**Purpose:** File upload and storage management

**Key Endpoints:**
```
POST   /api/file/upload             - Upload file
GET    /api/file/{fileId}           - Download file
DELETE /api/file/{fileId}           - Delete file
GET    /api/file/list               - List files
```

**Supported File Types:**
- PDF documents
- Excel files (.xlsx, .xls)
- CSV files
- Image files (.jpg, .png)
- Text files

**Max Upload Size:** 1GB (configurable)

---

### MigrationController
**Base Path:** `/api`
**Purpose:** Data migration utilities

**Key Endpoints:**
```
POST   /api/migration/import        - Import data
POST   /api/migration/export        - Export data
GET    /api/migration/status        - Check status
```

---

### PageController
**Base Path:** `/api`
**Purpose:** Static page and content management

**Key Endpoints:**
```
POST   /api/page                    - Create page
GET    /api/page                    - List pages
GET    /api/page/{slug}             - Get page content
PUT    /api/page/{id}               - Update page
```

---

### VerifyController
**Base Path:** `/api`
**Purpose:** Data verification utilities

**Key Endpoints:**
```
POST   /api/verify/data             - Verify data integrity
GET    /api/verify/report           - Verification report
POST   /api/verify/reconcile        - Reconcile data
```

---

### WorkFlowController
**Base Path:** `/api`
**Purpose:** Workflow management and approval processes

**Key Endpoints:**
```
POST   /api/workflow                - Create workflow
GET    /api/workflow                - List workflows
POST   /api/workflow/{id}/step      - Create workflow step
PUT    /api/workflow/{id}/approve   - Approve in workflow
```

---

### InterviewController
**Base Path:** `/api`
**Purpose:** Interview management and scheduling

**Key Endpoints:**
```
POST   /api/interview               - Create interview
GET    /api/interview               - List interviews
PUT    /api/interview/{id}/schedule - Schedule interview
PUT    /api/interview/{id}/complete - Mark complete
POST   /api/interview/{id}/feedback - Add feedback
```

---

### No2ProductsController & ScrapController
**Base Path:** `/api`
**Purpose:** No.2 products and scrap management

**Key Endpoints:**
```
POST   /api/no2Products             - Create no.2 product
GET    /api/no2Products             - List products
POST   /api/scrap                   - Create scrap record
GET    /api/scrap                   - List scrap
POST   /api/scrap/{id}/sell         - Sell scrap
```

---

### SplitController
**Base Path:** `/api`
**Purpose:** Split operations and lot management

**Key Endpoints:**
```
POST   /api/split                   - Create split
GET    /api/split                   - List splits
POST   /api/split/{id}/complete     - Complete split
```

---

### EmastDailyController
**Base Path:** `/api`
**Purpose:** Daily entry master

**Key Endpoints:**
```
POST   /api/emastDaily              - Create daily entry
GET    /api/emastDaily              - List entries
GET    /api/emastDaily/report       - Daily report
```

---

## 10. Reports & Analytics

### ReportController
**Base Path:** `/api`
**Purpose:** General report generation

**Key Endpoints:**
```
GET    /api/report/sales            - Sales report
GET    /api/report/inventory        - Inventory report
GET    /api/report/purchase         - Purchase report
GET    /api/report/finance          - Finance report
GET    /api/report/custom           - Custom report builder
POST   /api/report/export           - Export report
```

---

### ProcessReportController
**Base Path:** `/api`
**Purpose:** Process-specific reports

**Key Endpoints:**
```
GET    /api/processReport/production - Production report
GET    /api/processReport/quality    - Quality report
GET    /api/processReport/supply     - Supply chain report
```

---

### QtyReportController
**Base Path:** `/api`
**Purpose:** Quantity and stock reports

**Key Endpoints:**
```
GET    /api/qtyReport/stock         - Stock quantity report
GET    /api/qtyReport/movement      - Movement report
GET    /api/qtyReport/variance      - Variance report
```

---

### KpiController
**Base Path:** `/api`
**Purpose:** KPI tracking and dashboard reporting

**Key Endpoints:**
```
GET    /api/kpi/dashboard           - KPI dashboard
GET    /api/kpi/{kpiId}             - Get KPI details
POST   /api/kpi/{kpiId}/update      - Update KPI value
GET    /api/kpi/trend               - KPI trend analysis
```

**Key KPIs Tracked:**
- Revenue and profit margins
- Inventory turnover
- Production efficiency
- On-time delivery %
- Employee productivity
- Cost per unit
- Quality metrics (defects %)
- Customer satisfaction

---

### PdfController
**Base Path:** `/api`
**Purpose:** PDF generation and reporting

**Key Endpoints:**
```
POST   /api/pdf/generate            - Generate PDF
GET    /api/pdf/{documentId}        - Get PDF
POST   /api/pdf/email               - Email PDF
```

**PDF Generate Types:**
- Sales invoices
- Purchase orders
- Salary slips
- Stock reports
- Financial statements
- Custom documents

---

### Other Specialized Controllers

**ASRSController**
- Automated Storage/Retrieval System integration
- Inventory management with automated picking

**EBServiceController & EbGeneratorController**
- E-Business services
- EB (Electricity Board) report generation

**SendExternalController**
- External system and API integration
- Data synchronization with external systems

**CanteenMenuController & FoodItemController**
- Canteen operations
- Menu management
- Food item master

---

## Core Service Architecture

All controllers utilize underlying services:

```
Controller ← → Service ← → Repository/DAO ← → Database
```

### Standard HTTP Methods

| Method | Operation | Typical Endpoint |
|--------|-----------|------------------|
| POST | Create | /api/resource |
| GET | Read | /api/resource, /api/resource/{id} |
| PUT | Update | /api/resource/{id} |
| DELETE | Delete | /api/resource/{id} |
| PATCH | Partial Update | /api/resource/{id} |

### Common Response Patterns

**Success Response (200/201):**
```json
{
    "id": 1,
    "data": {...},
    "message": "Operation successful",
    "timestamp": "2026-04-21T10:00:00",
    "success": true
}
```

**Error Response (4xx/5xx):**
```json
{
    "timestamp": "2026-04-21T10:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "errors": [
        {
            "field": "email",
            "message": "Invalid email format"
        }
    ]
}
```

---

## Authentication & Authorization

### JWT Token Structure
```
Header: {
    "alg": "HS512",
    "typ": "JWT"
}

Payload: {
    "sub": "username",
    "iat": 1697000000,  // issued at
    "exp": 1697086400,  // expiration
    "roles": ["ADMIN", "USER"],
    "userId": 1
}

Signature: HMACSHA512(base64(header) + "." + base64(payload), secret)
```

### Role-Based Access Control (RBAC)
- Admin: Full system access
- Manager: Department/section level access
- User: Own data and common operations
- Guest: Read-only access

---

## Entity Relationships

**User Relationships:**
- User → Role (Many-to-Many)
- User → Department (Many-to-One)
- User → Designation (Many-to-One)
- User → Branch (Many-to-One)

**Product Relationships:**
- Product → Category (Many-to-One)
- Product → Attributes (Many-to-Many)
- Product → Specification (One-to-One)

**Transaction Relationships:**
- PurchaseOrder → Vendor (Many-to-One)
- PurchaseOrder → PurchaseOrderItems (One-to-Many)
- SalesOrder → Customer (Many-to-One)
- SalesOrder → SalesOrderItems (One-to-Many)

---

## Next Steps

For more detailed information on specific controllers, please refer to:
1. **API_ENDPOINTS_REFERENCE.md** - Complete endpoint listing
2. **DEVELOPER_SETUP_GUIDE.md** - Development environment setup
3. **HANDOVER_DOCUMENT.md** - Production support and maintenance
4. **Swagger UI** - Interactive API documentation at http://localhost:8080/swagger-ui.html

For questions about specific functionality, consult the corresponding controller's service and DAO classes.

---

**Document Version:** 1.0  
**Last Updated:** April 2026  
**For Support:** Contact development team
