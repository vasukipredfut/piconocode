# Idayam ERP Backend - Architecture Diagrams

## 1. System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│  ┌──────────────────┬──────────────────┬──────────────────┐    │
│  │  Web Browser     │  Mobile App      │  Desktop App     │    │
│  └────────┬─────────┴────────┬─────────┴────────┬─────────┘    │
└───────────┼────────────────────┼──────────────────┼──────────────┘
            │ HTTPS/HTTP         │                  │
┌───────────▼────────────────────▼──────────────────▼──────────────┐
│                   API GATEWAY / LOAD BALANCER                    │
│  • Request routing    • Rate limiting    • SSL termination       │
│  • Load distribution  • Request logging  • Compression           │
└───────────┬────────────────────────────────────────────┬─────────┘
            │                                            │
┌───────────▼────────────────────────────────────────────▼─────────┐
│                 SPRING BOOT APPLICATION (PORT 8080)              │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │          PRESENTATION LAYER (Controllers)                 │  │
│  │                                                            │  │
│  │  • ActionController         • VoucherController           │  │
│  │  • AttendanceController     • OrderController            │  │
│  │  • InventoryController      • ProductionController       │  │
│  │  • [106 Total REST Controllers]                          │  │
│  │                                                            │  │
│  │  API Endpoints: 400+ endpoints                            │  │
│  │  HTTP Methods: GET, POST, PUT, DELETE, PATCH             │  │
│  └────────────────────────────────────────────────────────────┘  │
│                            ▲                                      │
│                            │ Service injection                    │
│  ┌────────────────────────┴────────────────────────────────────┐  │
│  │         SERVICE LAYER (Business Logic)                    │  │
│  │                                                            │  │
│  │  • UserService              • InventoryService            │  │
│  │  • OrderService             • PaymentService              │  │
│  │  • ReportService            • ValidationService           │  │
│  │  • [Custom business logic]   • CacheService               │  │
│  │                                                            │  │
│  │  Responsibilities:                                        │  │
│  │  - Business rule validation  - Data transformation        │  │
│  │  - Transaction management    - Cross-cutting concerns     │  │
│  └────────────────────────────────────────────────────────────┘  │
│                            ▲                                      │
│                            │ Repository injection                 │
│  ┌────────────────────────┴────────────────────────────────────┐  │
│  │     DATA ACCESS LAYER (DAO / JPA Repositories)            │  │
│  │                                                            │  │
│  │  • UserRepository           • OrderRepository             │  │
│  │  • ProductRepository        • InventoryRepository         │  │
│  │  • [Spring Data JPA Queries]                              │  │
│  │  • Custom SQL queries (when needed)                       │  │
│  │                                                            │  │
│  │  Features:                                                │  │
│  │  - CRUD operations          - Query optimization          │  │
│  │  - Lazy loading             - Caching                     │  │
│  └────────────────────────────────────────────────────────────┘  │
│                            ▲                                      │
│  ┌────────────────────────┴────────────────────────────────────┐  │
│  │           SECURITY & FILTER LAYER                         │  │
│  │                                                            │  │
│  │  • AuthTokenFilter           (JWT validation)             │  │
│  │  • CorsFilter                (Cross-origin requests)      │  │
│  │  • ContentCachingFilter       (Request/Response logging)   │  │
│  │  • SecurityConfig            (Auth configuration)         │  │
│  │                                                            │  │
│  │  Security Features:                                       │  │
│  │  ✓ JWT token validation      ✓ Password hashing (bcrypt)  │  │
│  │  ✓ Role-based access control ✓ Rate limiting ready       │  │
│  │  ✓ CORS properly configured  ✓ Input validation           │  │
│  └────────────────────────────────────────────────────────────┘  │
│                                                                   │
└───────────┬────────────────────────────────────────────┬─────────┘
            │                                            │
┌───────────▼────────────────────┐      ┌───────────────▼────────┐
│     PRIMARY DATABASE           │      │    FILE STORAGE        │
│   (PostgreSQL 12+)             │      │                        │
│                                │      │  • Local filesystem    │
│  • 100+ tables                 │      │  • Network share       │
│  • Indexes optimized           │      │  • Cloud storage       │
│  • Foreign key constraints     │      │  • CDN                 │
│  • Triggers & views            │      │                        │
│  • Connection pooling          │      │  Stores:               │
│  • Transaction support         │      │  • PDF reports         │
│                                │      │  • File uploads        │
│  Data Types:                   │      │  • Email attachments   │
│  • Users & Roles              │      │  • Images & media      │
│  • Business entities          │      │                        │
│  • Transactional data         │      └────────────────────────┘
│  • Audit logs                 │
│  • Reference data             │
└───────────┬────────────────────┘
            │
┌───────────▼────────────────────┐
│    BACKUP & RECOVERY           │
│                                │
│  • Daily full backups          │
│  • Hourly transaction logs     │
│  • Point-in-time recovery      │
│  • Offsite backup storage      │
└────────────────────────────────┘
```

---

## 2. Request Processing Flow

```
HTTP Request
    │
    ▼
┌─────────────────────────────────────┐
│  Servlet Filter Chain               │
├─────────────────────────────────────┤
│  1. CorsFilter                      │
│     - Check allowed origins         │
│     - Add CORS headers              │
│  2. ContentCachingFilter            │
│     - Cache request/response        │
│     - Log request body              │
│  3. AuthTokenFilter                 │
│     - Extract JWT token             │
│     - Validate signature            │
│     - Populate security context     │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  DispatcherServlet                  │
│  - Route request to controller      │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Controller (ActionController, etc) │
├─────────────────────────────────────┤
│  @RequestMapping("/api/action")     │
│  @PostMapping("/save")              │
│                                     │
│  1. Extract request data            │
│  2. Validate input (JSR-303)        │
│  3. Call service layer              │
│  4. Return response                 │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Service Layer                      │
├─────────────────────────────────────┤
│  ActionService                      │
│                                     │
│  1. Apply business rules            │
│  2. Perform calculations            │
│  3. Manage transactions             │
│  4. Call DAO/Repository             │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  DAO/Repository Layer (JPA)         │
├─────────────────────────────────────┤
│  ActionRepository extends           │
│  JpaRepository<Action, Long>        │
│                                     │
│  1. Build query                     │
│  2. Execute database operation      │
│  3. Map results to entities         │
│  4. Handle transactions             │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Database (PostgreSQL)              │
├─────────────────────────────────────┤
│  Execute SQL:                       │
│  - SELECT, INSERT, UPDATE, DELETE   │
│  - Maintain ACID properties         │
│  - Apply constraints                │
└──────────────┬──────────────────────┘
               │
               ▼
        Response Data
               │
               ▼
┌─────────────────────────────────────┐
│  Hibernate Mapping                  │
│  - Map ResultSet to Entity objects  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Service Layer                      │
│  - Process result                   │
│  - Prepare response DTO             │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Controller                         │
│  - Wrap in ResponseEntity           │
│  - Add appropriate HTTP status      │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Response Handling                  │
├─────────────────────────────────────┤
│  1. Content negotiation (JSON)      │
│  2. Serialize response              │
│  3. Add response headers            │
│  4. ContentCachingFilter logs       │
└──────────────┬──────────────────────┘
               │
               ▼
        HTTP Response
         (200, 201, 400, 401, 500, etc)
```

---

## 3. Authentication & Security Flow

```
┌────────────────────────┐
│   Client Application   │
└────────────┬───────────┘
             │
             ▼
    ┌─────────────────────────┐
    │  POST /api/auth/login   │
    │  {username, password}   │
    └────────────┬────────────┘
                 │
                 ▼
    ┌─────────────────────────────────┐
    │  UserController.login()         │
    └────────────┬────────────────────┘
                 │
                 ▼
    ┌─────────────────────────────────┐
    │  UserService.authenticate()     │
    │  1. Find user by username       │
    │  2. Match password (bcrypt)     │
    │  3. Check user status (active)  │
    └────────────┬────────────────────┘
                 │
                 ▼
    ┌─────────────────────────────────┐
    │  JwtUtils.generateToken()       │
    │  1. Create JWT payload:         │
    │     - userId                    │
    │     - username                  │
    │     - roles                     │
    │     - permissions               │
    │  2. Sign with secret key        │
    │  3. Add expiration (24h)        │
    └────────────┬────────────────────┘
                 │
                 ▼
    ┌─────────────────────────────────┐
    │  Return JWT Token               │
    │  {accessToken, refreshToken}    │
    └────────────┬────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────────┐
    │  Client stores tokens (secure storage) │
    └────────────┬───────────────────────────┘
                 │
                 ▼
    ┌──────────────────────────────────────────┐
    │  Subsequent API Request                  │
    │  GET /api/user?page=0&size=10           │
    │  Header: Authorization: Bearer [JWT]    │
    └────────────┬─────────────────────────────┘
                 │
                 ▼
    ┌──────────────────────────────────────┐
    │  AuthTokenFilter.doFilterInternal()  │
    │  1. Extract token from header        │
    │  2. Validate token signature         │
    │  3. Check expiration                 │
    │  4. Extract user claims              │
    │  5. Build authentication object      │
    │  6. Set SecurityContextHolder        │
    └────────────┬─────────────────────────┘
                 │
         ┌───────┴───────┐
         │               │
         ▼               ▼
    ┌────────┐      ┌──────────────┐
    │ Valid  │      │ Invalid      │
    └───┬────┘      └──┬───────────┘
        │              │
        ▼              ▼
    ┌────────────┐  ┌──────────────────┐
    │ Continue   │  │ Return 401       │
    │ Request    │  │ Unauthorized     │
    └────┬───────┘  └──────────────────┘
         │
         ▼
    ┌──────────────────────────┐
    │  Controller receives     │
    │  authenticated request   │
    └────────┬─────────────────┘
             │
             ▼
    ┌──────────────────────────┐
    │  WebSecurityConfig       │
    │  - Check role/permission │
    │  - Authorize request     │
    │  (@PreAuthorize)         │
    └────────┬─────────────────┘
             │
     ┌───────┴──────────┐
     │                  │
     ▼                  ▼
┌────────────┐   ┌────────────────┐
│ Authorized │   │ Forbidden      │
└───┬────────┘   └────┬───────────┘
    │                 │
    ▼                 ▼
┌──────────────┐  ┌──────────┐
│ Execute      │  │ Return 403
│ business     │  │ Forbidden
│ logic        │  └──────────┘
└───┬──────────┘
    │
    ▼
Response returned to client
```

---

## 4. Database Entity Relationship (Simplified)

```
┌──────────────────┐
│      USERS       │
├──────────────────┤
│ id (PK)          │
│ username (UQ)    │
│ password         │
│ email            │
│ active           │
│ created_date     │
└────────┬─────────┘
         │ 1..N
         ├─────────────────────┐
         │                     │
         ▼ 1..N                ▼ 1..N
    ┌────────────────┐    ┌────────────────┐
    │  USER_ROLE     │    │ EMPLOYEE       │
    ├────────────────┤    ├────────────────┤
    │ id (PK)        │    │ id (PK)        │
    │ user_id (FK)   │    │ user_id (FK)   │
    │ role_id (FK)   │    │ emp_name       │
    │ assigned_date  │    │ department_id  │
    └────────┬───────┘    │ salary         │
             │            │ join_date      │
             │ N..1       └────────┬───────┘
             │                     │ 1..N
             ▼                     ▼
        ┌─────────┐           ┌────────────┐
        │  ROLES  │           │ ATTENDANCE │
        ├─────────┤           ├────────────┤
        │ id (PK) │           │ id (PK)    │
        │ name    │           │ emp_id(FK) │
        │ active  │           │ date       │
        └─────────┘           │ status     │
                              └────────────┘

┌──────────────────┐
│    PRODUCTS      │
├──────────────────┤
│ id (PK)          │
│ code (UQ)        │
│ name             │
│ category_id      │
│ price            │
│ stock            │
└────────┬─────────┘
         │ 1..N
         ├───────────────────────────┐
         │                           │
         ▼ N..1                      ▼ N..1
    ┌───────────────┐          ┌──────────────┐
    │    ORDERS     │          │    STOCK     │
    ├───────────────┤          ├──────────────┤
    │ id (PK)       │          │ id (PK)      │
    │ order_no      │          │ product_id   │
    │ product_id    │          │ bin_id       │
    │ quantity      │          │ quantity     │
    │ order_date    │          │ last_updated │
    │ user_id       │          └──────────────┘
    └───────────────┘

Key:
PK = Primary Key
FK = Foreign Key
UQ = Unique
1..N = One to Many
N..1 = Many to One
```

---

## 5. Deployment Architecture

### 5.1 Development Environment
```
┌─────────────────────────────────┐
│   Developer Workstation         │
├─────────────────────────────────┤
│ IDE (IntelliJ/Eclipse/VS Code)  │
│ JDK 11+                         │
│ Maven 3.6+                      │
│ Git client                      │
│ PostgreSQL (Local or Docker)    │
│ Postman/Insomnia (API testing)  │
└─────────────────────────────────┘
```

### 5.2 Staging Environment
```
┌──────────────────────────┐
│   Staging Server         │
├──────────────────────────┤
│ CentOS/Ubuntu Linux      │
│ JDK 11+                  │
│ PostgreSQL 12+           │
│ Application JAR          │
│ Systemd service          │
│ Nginx reverse proxy      │
│ Let's Encrypt SSL cert   │
└──────────────────────────┘
```

### 5.3 Production Environment (HA)
```
                ┌──────────────────┐
                │  Load Balancer   │
                │  (Nginx/HAProxy) │
                │ :443 (HTTPS)     │
                └────────┬─────────┘
                         │
          ┌──────────────┴──────────────┐
          │                             │
          ▼                             ▼
    ┌──────────────┐            ┌──────────────┐
    │  App Server1 │            │  App Server2 │
    │              │            │              │
    │ CentOS/Linux │            │ CentOS/Linux │
    │ JDK 11+      │            │ JDK 11+      │
    │ Tomcat       │            │ Tomcat       │
    │ :8080        │            │ :8080        │
    │ Application  │            │ Application  │
    └───────┬──────┘            └────────┬─────┘
            │                            │
            └───────────┬────────────────┘
                        │
          ┌─────────────┴─────────────┐
          │                           │
          ▼                           ▼
    ┌──────────────┐        ┌──────────────────┐
    │ PostgreSQL   │        │ PostgreSQL       │
    │ Primary      │        │ Secondary        │
    │ (Streaming   │◄──────►│ (Hot Standby)    │
    │  Replication)│        │                  │
    └──────────────┘        └──────────────────┘
          │
          ▼
    ┌──────────────┐
    │  Backup      │
    │  Storage     │
    │  (Daily)     │
    └──────────────┘
```

---

## 6. Module Dependencies

```
USER AUTHENTICATION & SECURITY
├── ModulePrivilegeController (permissions)
├── AuthTokenFilter (JWT validation)
└── WebSecurityConfig (security setup)

HUMAN RESOURCES MODULE
├── AttendanceController
├── LeaveController
├── SalaryController
├── TrainingController
├── [depends on] UserController (employee master)
└── [depends on] DepartmentController (organization structure)

INVENTORY MANAGEMENT
├── StockLedgerController (central)
├── BinStockController
├── MaterialRequestController
├── MaterialIssueController
├── [depends on] ProductMasterController
└── [depends on] GoDownController (warehouse)

PROCUREMENT
├── PurchaseOrderController
├── VendorController
├── PurchaseBillApproveController
├── [depends on] ProductMasterController
└── [depends on] StockLedgerController

SALES
├── OrderController
├── SalesController
├── SalesReturnController
├── [depends on] ProductMasterController
├── [depends on] StockLedgerController
└── [depends on] UserController (sales rep)

FINANCIAL
├── VoucherController
├── JournalController
├── BillController
├── [depends on] All transaction modules
└── ViewAccountsLedgerController (reporting)

PRODUCTION
├── ProductionEntryController
├── QualityInspectionController
├── BlendingAdviceController
├── [depends on] StockLedgerController
├── [depends on] OrderController
└── [depends on] ProductMasterController

REPORTS & ANALYTICS
├── ReportController
├── ProcessReportController
├── KpiController
└── [depends on] All other modules
```

---

## 7. Data Flow for Purchase Order

```
┌──────────────────────────┐
│ Vendor initiates PO      │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│ POST /api/purchaseOrder  │
│ {product, quantity, vendor}
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────────┐
│ PurchaseOrderController      │
│ - Validate request           │
│ - Check stock levels         │
│ - Validate vendor            │
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ PurchaseOrderService         │
│ - Calculate total amount     │
│ - Apply business rules       │
│ - Start transaction          │
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ Save PurchaseOrder entity    │
│ (PurchaseOrderRepository)    │
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ Database INSERT operation    │
│ CREATE ROW in PO table       │
│ COMMIT transaction           │
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ Send email notification      │
│ (async - MessageService)     │
└────────────┬─────────────────┘
             │
             ▼
┌──────────────────────────────┐
│ Return response to client    │
│ {poId, status, amount}       │
└────────────┬─────────────────┘
             │
             ▼
    ┌────────────────┐
    │ Display in UI  │
    │ to user        │
    └────────────────┘
```

---

## 8. Cache Strategy

```
┌─────────────────────────────┐
│ Request comes in            │
└──────────────┬──────────────┘
               │
               ▼
    ┌──────────────────────┐
    │ Check Cache          │
    │ (Redis/In-Memory)    │
    └──────────┬───────────┘
               │
        ┌──────┴──────┐
        │             │
    Cache Hit      Cache Miss
        │             │
        ▼             ▼
    ┌────────┐   ┌────────────┐
    │ Return │   │ Query      │
    │ cached │   │ Database   │
    │ data   │   │            │
    └────────┘   └──────┬─────┘
                        │
                        ▼
                ┌────────────────┐
                │ Store in cache │
                │ (TTL: configurable)
                └────────┬───────┘
                         │
                         ▼
                    ┌────────┐
                    │ Return │
                    │ data   │
                    └────────┘
```

---

**END OF ARCHITECTURE DIAGRAMS**
