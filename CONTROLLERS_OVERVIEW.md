# Idayam ERP Backend - Controllers Overview

## Project Information
- **Application Name:** Idayam ERP System
- **Framework:** Spring Boot
- **Documentation Date:** April 2026
- **Base Path:** `/api`
- **Authentication:** JWT Token-based

## Table of Contents
1. [Project Structure](#project-structure)
2. [Controller Categories](#controller-categories)
3. [Key Features](#key-features)
4. [Controller Index](#controller-index)

## Project Structure

```
src/
├── main/
│   ├── java/com/idayam/
│   │   ├── IdayamApplication.java (Main Application Class)
│   │   ├── SwaggerConfig.java (API Documentation)
│   │   ├── config/
│   │   │   ├── AuthEntryPointJwt.java
│   │   │   ├── AuthTokenFilter.java
│   │   │   ├── JwtUtils.java
│   │   │   ├── WebSecurityConfig.java
│   │   │   ├── CorsConfiguration.java
│   │   │   └── ... (Other configurations)
│   │   ├── controller/ (106 REST Controllers)
│   │   ├── service/ (Business Logic Layer)
│   │   ├── dao/ (Data Access Layer - Repositories)
│   │   ├── model/ (Entity Models/JPA)
│   │   ├── dto/ (Data Transfer Objects)
│   │   └── utils/ (Utility Classes)
│   └── resources/
│       ├── application.properties
│       └── logging.properties
└── test/
    └── java/com/idayam/
```

## Key Features

### Security
- **Authentication:** JWT (JSON Web Token)
- **Filter:** `AuthTokenFilter` - Validates JWT tokens on each request
- **Entry Point:** `AuthEntryPointJwt` - Handles unauthorized access
- **CORS:** Configured for cross-origin requests

### Logging & Monitoring
- **ContentCachingFilter:** Logs request/response content
- **ResponseLoggingInterceptor:** Captures HTTP responses

### File Upload
- **Max Upload Size:** 1GB (configurable)
- **Handler:** `FileStorageController` - Manages file uploads

### API Documentation
- **Swagger 2.0** enabled for auto-generated API docs
- **Endpoint:** `/swagger-ui.html`

## Controller Categories

### 1. Authentication & User Management
- **UserController** - User login, registration, password management
- **ModulePrivilegeController** - Role and permission management
- **MailController** - Email operations

### 2. Human Resources & Attendance
- **AttendanceController** - Employee attendance tracking
- **LeaveController** - Leave application and management
- **SalaryController** - Salary processing
- **TrainingController** - Employee training management
- **MedicalCheckupController** - Medical checkup records
- **ShiftAllotmentController** - Shift assignments
- **ShiftsController** - Shift definitions
- **DutyController** - Duty assignments
- **DutyUnplannedController** - Unplanned duty assignments
- **WelfareController** - Employee welfare programs
- **WelfareRegistrationController** - Welfare registrations
- **FingerPrintController** - Biometric attendance integration

### 3. Inventory & Stock Management
- **BinDetailsController** - Bin/storage location management
- **BinStockController** - Stock levels in bins
- **GoDownController** - Warehouse management
- **GoDownSummaryController** - Warehouse summary reports
- **StockLedgerController** - Stock movement ledger
- **MaterialFlowGDCController** - Material flow tracking
- **ProductMasterController** - Product master data
- **MaterialWithInBranchReceiptController** - Intra-branch receipt
- **WithInBranchDistributionController** - Intra-branch distribution
- **WithInBranchReceiveController** - Intra-branch receive

### 4. Purchase Order & Procurement
- **PurchaseOrderController** - PO creation and management
- **PurchaseBillApproveController** - Bill approval
- **AdvancePaymentForPurchaseOrderController** - Advance payments
- **SupplierEvaluationController** - Supplier performance tracking
- **VendorController** - Vendor master data
- **VendorSisterConcernController** - Sister concerns management
- **VendorSisterConcernMapController** - Sister concern mapping

### 5. Sales & Orders
- **OrderController** - Sales order management
- **SalesController** - Sales transaction processing
- **SalesReturnController** - Sales return handling
- **SalesProductFilmController** - Product film information
- **SalesProductLabelController** - Product label information
- **HoSalesController** - Head office sales coordination

### 6. Material Management & Requests
- **MaterialRequestController** - Material request submission
- **MaterialIssueController** - Material issue processing
- **MaterialReturnController** - Material return handling
- **InternalMaterialIssueController** - Internal material issue
- **OtherBranchIssueController** - Issue to other branches
- **OtherBranchReceiptController** - Receipt from other branches
- **IntraBranchReceiptController** - Intra-branch receipts
- **DeliteStockMoveRequestController** - Stock movement requests

### 7. Production & Manufacturing
- **ProductionEntryController** - Production entry/completion
- **BlendingAdviceController** - Blending instructions
- **JaggeryInspectionController** - Jaggery quality inspection
- **AdminInspectionController** - Administrative inspections
- **InwardInspectionEntryController** - Inward inspection
- **QcInProcessController** - QC during production
- **QualityInspectionController** - Quality checks
- **PMScheduleController** - Preventive maintenance schedule
- **ProductDistruptanciesController** - Production disruptions

### 8. Financial & Accounting
- **VoucherController** - Voucher management
- **CustomVoucherController** - Custom voucher templates
- **JournalController** - Journal entries
- **CreditNoteController** - Credit note management
- **BillController** - Bill management
- **CostExpenseController** - Cost and expense tracking
- **ExpenseController** - General expenses
- **ExpenseCategoryController** - Expense categories
- **ViewAccountsLedgerController** - General ledger view
- **OtherDeductionController** - Deductions management
- **EpayMasterController** - E-Pay master data
- **DepayMasterController** - Deduction payment

### 9. Reports & Analytics
- **ReportController** - General reports
- **ProcessReportController** - Process-specific reports
- **QtyReportController** - Quantity reports
- **KpiController** - KPI tracking and reporting

### 10. Configuration & Masters
- **MasterController** - Master data management
- **AttributesController** - Attribute configuration
- **PAttributesController** - Product attributes
- **SpecificationController** - Specification configuration
- **HsnController** - HSN code master
- **PanelMasterController** - Panel configuration
- **HoOfficeConfigureController** - HO office configuration
- **DayEndClosingController** - Day-end closure operations
- **ActionController** - Action configuration and tracking

### 11. Employee Compensation
- **CwagesController** - Casual wages
- **CWagesSalaryController** - Casual wages salary
- **CompentencyMatrixController** - Competency framework
- **SkillGradeLevelController** - Skill levels and grades
- **AwardController** - Employee awards

### 12. Utilities & Administration
- **CommonController** - Common operations (Branch, Department, Lot management)
- **FileStorageController** - File upload/download
- **MigrationController** - Data migration utilities
- **PageController** - Static page management
- **VerifyController** - Data verification utilities
- **WorkFlowController** - Workflow management

### 13. Facility & Asset Management
- **MachineController** - Machine master and maintenance
- **VehicleMovementController** - Vehicle movement tracking
- **VehicleRequestController** - Vehicle request/booking
- **TransportController** - Transport management
- **TransportDriverController** - Driver management
- **TransportTripController** - Trip management
- **TransportTripExpenseController** - Trip expenses
- **FumigationController** - Fumigation/pest control
- **RodentMonitorController** - Rodent monitoring
- **WindmillController** - Windmill management
- **FactoryToHoController** - Factory to HO coordination

### 14. Food & Canteen Management
- **CanteenMenuController** - Menu management
- **FoodItemController** - Food item master

### 15. Systems & Integration
- **ASRSController** - Automated Storage/Retrieval System
- **EBServiceController** - E-Business service integration
- **EbGeneratorController** - EB report generation
- **SendExternalController** - External system integration
- **PdfController** - PDF generation and reporting

### 16. Process & Operations
- **DispatchController** - Dispatch management
- **DpatchDeliveryController** - Delivery tracking
- **DpatchDespatchController** - Despatch operations
- **DwlogController** - Warehouse log operations
- **InterviewController** - Interview management
- **ProposalController** - Proposal management
- **ProposalParamInspectionScoreController** - Inspection scoring
- **ScrapController** - Scrap management
- **SplitController** - Split operations
- **BankDetailsController** - Bank account management
- **BankReptController** - Bank reconciliation
- **TsLapController** - TS LAP tracking
- **No2ProductsController** - No.2 products management
- **EmastDailyController** - Daily entry master
- **PurchaseQualityInspectionScoreController** - Quality scoring

## Controller Index (106 Total)

| # | Controller Name | Module | Purpose |
|---|---|---|---|
| 1 | ActionController | Configuration | Action configuration and tracking |
| 2 | AdminInspectionController | Production | Administrative inspection |
| 3 | AdvancePaymentForPurchaseOrderController | Procurement | PO advance payments |
| 4 | ASRSController | Systems | Automated storage system |
| 5 | AttendanceController | HR | Attendance tracking |
| 6 | AttributesController | Master Data | General attributes |
| 7 | AwardController | HR | Employee awards |
| 8 | BankDetailsController | Finance | Bank account management |
| 9 | BankReptController | Finance | Bank reconciliation |
| 10 | BillController | Finance | Bill management |
| 11 | BinDetailsController | Inventory | Bin/storage location |
| 12 | BinStockController | Inventory | Bin stock levels |
| 13 | BlendingAdviceController | Production | Blending instructions |
| 14 | CanteenMenuController | Admin | Canteen menu |
| 15 | CommonController | Admin | Common operations |
| 16 | CompentencyMatrixController | HR | Competency framework |
| 17 | CostExpenseController | Finance | Cost tracking |
| 18 | CreditNoteController | Finance | Credit notes |
| 19 | CustomVoucherController | Finance | Custom vouchers |
| 20 | CwagesController | HR | Casual wages |
| 21 | CWagesSalaryController | HR | Casual wage salary |
| 22 | DayEndClosingController | Operations | Day-end closure |
| 23 | DeliteStockMoveRequestController | Inventory | Stock movement |
| 24 | DepayMasterController | Finance | Deduction payment |
| 25 | DispatchController | Operations | Dispatch management |
| 26 | DpatchDeliveryController | Operations | Delivery tracking |
| 27 | DpatchDespatchController | Operations | Despatch operations |
| 28 | DutyController | HR | Duty assignments |
| 29 | DutyUnplannedController | HR | Unplanned duties |
| 30 | DwlogController | Warehouse | Warehouse logs |
| 31 | EbGeneratorController | Systems | EB report generation |
| 32 | EBServiceController | Systems | E-Business service |
| 33 | EmastDailyController | Operations | Daily master entry |
| 34 | EpayMasterController | Finance | E-Pay master |
| 35 | ExpenseCategoryController | Finance | Expense categories |
| 36 | ExpenseController | Finance | Expenses |
| 37 | FactoryToHoController | Operations | Factory-HO coordination |
| 38 | FileStorageController | Admin | File management |
| 39 | FingerPrintController | HR | Biometric attendance |
| 40 | FoodItemController | Admin | Food items |
| 41 | FumigationController | Facility | Fumigation |
| 42 | GoDownController | Inventory | Warehouse management |
| 43 | GoDownSummaryController | Inventory | Warehouse summary |
| 44 | HolidayController | HR | Holiday management |
| 45 | HoOfficeConfigureController | Admin | HO configuration |
| 46 | HoSalesController | Sales | HO sales |
| 47 | HsnController | Master Data | HSN codes |
| 48 | InternalMaterialIssueController | Inventory | Internal issue |
| 49 | InterviewController | HR | Interview management |
| 50 | IntraBranchReceiptController | Inventory | Intra-branch receipt |
| 51 | InwardInspectionEntryController | Production | Inward inspection |
| 52 | JaggeryInspectionController | Production | Jaggery inspection |
| 53 | JournalController | Finance | Journal entries |
| 54 | KpiController | Analytics | KPI tracking |
| 55 | LeaveController | HR | Leave management |
| 56 | MachineController | Facility | Machine management |
| 57 | MailController | Admin | Email operations |
| 58 | MasterController | Master Data | Master data |
| 59 | MaterialFlowGDCController | Inventory | Material flow |
| 60 | MaterialIssueController | Inventory | Material issue |
| 61 | MaterialRequestController | Inventory | Material request |
| 62 | MaterialReturnController | Inventory | Material return |
| 63 | MaterialWithInBranchReceiptController | Inventory | With-in branch receipt |
| 64 | MedicalCheckupController | HR | Medical checkup |
| 65 | MigrationController | Admin | Data migration |
| 66 | ModulePrivilegeController | Security | Module privileges |
| 67 | No2ProductsController | Operations | No.2 products |
| 68 | OrderController | Sales | Sales orders |
| 69 | OtherBranchIssueController | Inventory | Branch issue |
| 70 | OtherBranchReceiptController | Inventory | Branch receipt |
| 71 | OtherDeductionController | Finance | Deductions |
| 72 | PageController | Admin | Page management |
| 73 | PanelMasterController | Master Data | Panel configuration |
| 74 | PAttributesController | Master Data | Product attributes |
| 75 | PdfController | Systems | PDF generation |
| 76 | PMScheduleController | Facility | PM scheduling |
| 77 | ProcessReportController | Analytics | Process reports |
| 78 | ProductDistruptanciesController | Production | Production disruptions |
| 79 | ProductionEntryController | Production | Production entry |
| 80 | ProductMasterController | Master Data | Product master |
| 81 | ProposalController | Operations | Proposals |
| 82 | ProposalParamInspectionScoreController | Production | Inspection scoring |
| 83 | PurchaseBillApproveController | Procurement | PO bill approval |
| 84 | PurchaseOrderController | Procurement | Purchase orders |
| 85 | PurchaseQualityInspectionScoreController | Quality | Purchase quality |
| 86 | QcInProcessController | Production | In-process QC |
| 87 | QtyReportController | Analytics | Quantity reports |
| 88 | ReportController | Analytics | General reports |
| 89 | RodentMonitorController | Facility | Rodent monitoring |
| 90 | SalaryController | HR | Salary management |
| 91 | SalesController | Sales | Sales transactions |
| 92 | SalesProductFilmController | Sales | Product films |
| 93 | SalesProductLabelController | Sales | Product labels |
| 94 | SalesReturnController | Sales | Sales returns |
| 95 | ScrapController | Operations | Scrap management |
| 96 | SendExternalController | Systems | External integration |
| 97 | ShiftAllotmentController | HR | Shift assignments |
| 98 | ShiftsController | HR | Shift management |
| 99 | SkillGradeLevelController | HR | Skill grades |
| 100 | SpecificationController | Master Data | Specifications |
| 101 | SplitController | Operations | Split operations |
| 102 | StockLedgerController | Inventory | Stock ledger |
| 103 | SupplierEvaluationController | Procurement | Supplier evaluation |
| 104 | TrainingController | HR | Training management |
| 105 | TransportController | Facility | Transport management |
| 106+ | (40+ more) | Various | See detailed documentation |

---

## Connection Points

### Authentication Flow
```
Client Request
    ↓
AuthTokenFilter (validates JWT token)
    ↓
Controller (processes request)
    ↓
Service layer (business logic)
    ↓
DAO/Repository (database access)
    ↓
Response
```

### Data Flow
```
DTO (Client Input)
    ↓
Controller (request mapping)
    ↓
Service (business logic)
    ↓
Model/Entity (database mapping)
    ↓
DAO/Repository (CRUD operations)
    ↓
Database
```

---

## Next Steps
- See `DEVELOPER_SETUP_GUIDE.md` for setup and configuration
- See `CONTROLLER_DETAILED_DOCUMENTATION.md` for detailed controller information
- See `API_ENDPOINTS_REFERENCE.md` for complete API endpoint listing
- See `HANDOVER_DOCUMENT.md` for deployment and maintenance procedures
