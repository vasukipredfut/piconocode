# Idayam ERP Backend - Architecture and Code Flow Diagrams

## Document Information

| Item | Details |
|------|---------|
| **Document Type** | Technical Architecture & Flow Documentation |
| **Version** | 1.0 |
| **Created Date** | April 2026 |
| **Last Updated** | April 2026 |
| **Audience** | Developers, Architects, DevOps Teams |
| **Purpose** | Understanding system architecture, request flow, and code execution paths |

---

## Table of Contents

1. [High-Level System Architecture](#high-level-system-architecture)
2. [Components Overview](#components-overview)
3. [API Request Flow (Complete Request Lifecycle)](#api-request-flow-complete-request-lifecycle)
4. [Authentication & Security Flow](#authentication--security-flow)
5. [Data Access & Database Flow](#data-access--database-flow)
6. [Module-Based Architecture](#module-based-architecture)
7. [Integration Points](#integration-points)
8. [Async Processing & Scheduling](#async-processing--scheduling)
9. [Caching Strategy](#caching-strategy)
10. [Error Handling Flow](#error-handling-flow)

---

## High-Level System Architecture

### Layered Architecture Diagram

```
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                          CLIENT LAYER                            ┃
┃  ┌───────────────┬──────────────────┬─────────────────────────┐  ┃
┃  │ Web Browser   │  Mobile App      │  Desktop App            │  ┃
┃  │ (Angular/     │  (Android/iOS)   │  (Windows/Mac/Linux)    │  ┃
┃  │  React/Vue)   │                  │                         │  ┃
┃  └───────────────┴──────────────────┴─────────────────────────┘  ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━┬━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
                         │
                    HTTP/HTTPS
                    REST API
                    (JSON)
                         │
                         ↓
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                   REQUEST PROCESSING LAYER                       ┃
┃  ┌─────────────────────────────────────────────────────────────┐ ┃
┃  │ 🛡️  FILTERS & INTERCEPTORS                                  │ ┃
┃  │  ├─ AuthTokenFilter (JWT validation)                        │ ┃
┃  │  ├─ ContentCachingFilter (Request/Response caching)         │ ┃
┃  │  ├─ ResponseLoggingInterceptor (Audit logging)              │ ┃
┃  │  └─ CORS Configuration (Cross-origin handling)              │ ┃
┃  └─────────────────────────────────────────────────────────────┘ ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━┬━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
                         │
                         ↓
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃               REST CONTROLLERS LAYER (106 Controllers)           ┃
┃  ┌──────────────┬──────────────┬──────────────┬──────────────┐  ┃
┃  │   Purchase   │   Inventory  │    Sales     │   Finance    │  ┃
┃  │  Controllers │  Controllers │ Controllers  │  Controllers │  ┃
┃  ├──────────────┼──────────────┼──────────────┼──────────────┤  ┃
┃  │     HR       │  Production  │  Operations  │    Other     │  ┃
┃  │ Controllers  │ Controllers  │ Controllers  │ Controllers  │  ┃
┃  └──────────────┴──────────────┴──────────────┴──────────────┘  ┃
┃                                                                  ┃
┃  Responsibilities:                                               ┃
┃  • Route incoming requests                                       ┃
┃  • Validate request parameters                                   ┃
┃  • Call appropriate service methods                              ┃
┃  • Serialize response objects                                    ┃
┃  • Handle HTTP status codes                                      ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━┬━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
                         │
                         ↓
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃          SERVICE LAYER (Business Logic - 100+ Services)          ┃
┃  ┌─────────────────────────────────────────────────────────────┐ ┃
┃  │  • Business rule validation & enforcement                    │ ┃
┃  │  • Data processing & transformation                          │ ┃
┃  │  • Cross-module integration logic                            │ ┃
┃  │  • Cache layer access                                        │ ┃
┃  │  • External service calls (Email, Storage, etc.)             │ ┃
┃  │  • Transactional operations coordination                     │ ┃
┃  └─────────────────────────────────────────────────────────────┘ ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━┬━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
                         │
        ┌────────────────┼────────────────┐
        ↓                ↓                ↓
┌──────────────────┐ ┌──────────────┐ ┌─────────────────┐
│ Cache Layer      │ │ File Storage │ │ Email Service   │
│ (Spring Cache)   │ │ (Local/      │ │ (SMTP)          │
│                  │ │  Network)    │ │                 │
└──────────────────┘ └──────────────┘ └─────────────────┘
        ↑                                      ↑
        └──────────────────┬───────────────────┘
                           │
                           ↓
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃            DATA ACCESS LAYER (400+ JPA Repositories)             ┃
┃  ┌─────────────────────────────────────────────────────────────┐ ┃
┃  │  • JPA/Hibernate ORM mapping                                 │ ┃
┃  │  • Custom SQL queries                                        │ ┃
┃  │  • Entity lifecycle management                               │ ┃
┃  │  • Transaction management                                    │ ┃
┃  │  • Batch operations                                          │ ┃
┃  └─────────────────────────────────────────────────────────────┘ ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━┬━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
                         │
              SQL Operations (INSERT/UPDATE/DELETE/SELECT)
                         │
                         ↓
┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
┃                   SQL SERVER DATABASE                             ┃
┃  ┌─────────────────────────────────────────────────────────────┐ ┃
┃  │  • Persistent data storage                                   │ ┃
┃  │  • Transactions & ACID compliance                            │ ┃
┃  │  • Indexes & query optimization                              │ ┃
┃  │  • Backup & recovery mechanisms                              │ ┃
┃  │  • 100+ entity tables                                        │ ┃
┃  └─────────────────────────────────────────────────────────────┘ ┃
┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
```

---

## Components Overview

### Core Components & Their Responsibilities

#### 1. **Filters & Interceptors**
Located in: `config/` package

```
AuthTokenFilter
├─ Intercepts all HTTP requests
├─ Validates JWT tokens from Authorization header
├─ Extracts user claims and roles
├─ Sets SecurityContext for authenticated users
└─ Returns 401 Unauthorized if token is invalid/expired

ContentCachingFilter
├─ Wraps HttpServletRequest/Response
├─ Enables reading request body multiple times
├─ Caches response for logging purposes
└─ Preserves original request/response data

ResponseLoggingInterceptor
├─ Logs all API responses
├─ Captures response time measurements
├─ Records HTTP status codes
├─ Enables audit trail creation
└─ Tracks API usage statistics

CorsConfiguration
├─ Allows cross-origin requests
├─ Configures allowed origins, methods, headers
├─ Enables credentials in cross-origin requests
└─ Prevents CORS-related security issues
```

#### 2. **Controllers (106 Total)**
Pattern: `[DomainName]Controller`

```
Example: PurchaseOrderController
├─ @RestController annotation
├─ @RequestMapping mapping
├─ POST: createPurchaseOrder()
├─ GET: getPurchaseOrder(id)
├─ PUT: updatePurchaseOrder()
├─ DELETE: deletePurchaseOrder()
├─ GET: getAllPurchaseOrders()
└─ Custom endpoints for business logic
```

#### 3. **Services (100+ Total)**
Pattern: `[DomainName]Service`

```
Example: PurchaseOrderService
├─ Constructor/field injection
├─ @Service annotation
├─ Auto-wired dependencies:
│  ├─ Repositories
│  ├─ Other Services
│  ├─ Configuration beans
│  └─ External providers
├─ Business logic methods
├─ @Transactional for data modifications
├─ Caching with @Cacheable, @CachePut
└─ Error handling & logging
```

#### 4. **Repositories (400+ Total)**
Pattern: `[Entity]Repository extends JpaRepository<T,ID>`

```
Example: PurchaseOrderRepository
├─ Extends: JpaRepository<PurchaseOrder, Long>
├─ Automatically provides:
│  ├─ save(entity)
│  ├─ findById(id)
│  ├─ findAll()
│  ├─ update()
│  ├─ delete(entity)
│  └─ deleteById(id)
├─ Custom query methods:
│  ├─ @Query native SQL queries
│  ├─ @Query JPQL queries
│  ├─ Derived query methods
│  └─ Stored procedure calls
└─ Pagination & Sorting support
```

---

## API Request Flow (Complete Request Lifecycle)

### Step-by-Step Request Processing

```
┌─────────────────────────────────────────────────────────────────┐
│ STEP 1: CLIENT INITIATES REQUEST                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Client Application                                              │
│  ↓                                                               │
│  HTTP Request Construction                                       │
│  ├─ Method: POST /api/purchaseorder                             │
│  ├─ Headers:                                                     │
│  │  ├─ Content-Type: application/json                           │
│  │  ├─ Authorization: Bearer eyJhbGciOiJIUzI1NiIs...           │
│  │  └─ Other headers                                             │
│  ├─ Body:                                                        │
│  │  {                                                             │
│  │    "vendorId": 123,                                          │
│  │    "amount": 50000,                                          │
│  │    "items": [...]                                            │
│  │  }                                                             │
│  └─ Network transmission                                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 2: REQUEST INTERCEPTED BY FILTERS                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  AuthTokenFilter                                                 │
│  ├─ Extract token from Authorization header                    │
│  ├─ Call JwtUtils.validateJwtToken(token)                      │
│  ├─ If valid:                                                   │
│  │  ├─ Extract user email from token                           │
│  │  ├─ Extract user roles from token                           │
│  │  ├─ Create Authentication object                            │
│  │  ├─ Set in SecurityContext                                  │
│  │  └─ Continue to next filter                                 │
│  └─ If invalid:                                                 │
│     ├─ Create error response                                    │
│     ├─ Set HTTP status 401                                      │
│     └─ Return immediately (skip remaining processing)           │
│                                                                  │
│  ContentCachingFilter                                            │
│  ├─ Cache request body (allows multiple reads)                 │
│  └─ Continue processing                                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 3: ROUTE TO APPROPRIATE CONTROLLER                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  DispatcherServlet (Spring internal)                            │
│  ├─ Match request URL to controller mapping                     │
│  ├─ Identify HTTP method                                         │
│  └─ Route to: PurchaseOrderController.createPurchaseOrder()     │
│                                                                  │
│  Controller Method Invocation                                    │
│  ├─ Spring deserializes JSON body to DTO object                │
│  ├─ Example: PurchaseOrderDTO dto                              │
│  ├─ @Valid annotation triggers validation                      │
│  └─ Validation annotations check:                              │
│     ├─ @NotNull, @NotEmpty                                     │
│     ├─ @Min, @Max constraints                                  │
│     ├─ @Pattern regex validation                               │
│     └─ Custom validators                                        │
│                                                                  │
│  If validation fails:                                            │
│  ├─ Spring throws MethodArgumentNotValidException               │
│  ├─ @ExceptionHandler catches exception                        │
│  └─ Returns 400 Bad Request with error details                 │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 4: CONTROLLER DELEGATES TO SERVICE                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  PurchaseOrderController.createPurchaseOrder(dto)              │
│  ├─ Extract user from SecurityContext                          │
│  ├─ Log request: "Creating purchase order from vendor: 123"    │
│  ├─ Check user permissions                                      │
│  ├─ Call: purchaseOrderService.createOrder(dto, user)          │
│  └─ Receive response: PurchaseOrderResponse                    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 5: SERVICE EXECUTES BUSINESS LOGIC                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  PurchaseOrderService.createOrder(dto, user)                   │
│  ├─ @Transactional(readOnly=false)                             │
│  │                                                               │
│  ├─ STEP 5.1: Validate Business Rules                          │
│  │  ├─ Check if vendor exists: vendorRepo.findById(123)        │
│  │  ├─ Check if vendor is active                               │
│  │  ├─ Validate purchase amount against limits                 │
│  │  ├─ Check inventory availability if required                │
│  │  └─ Verify user has permission to create orders             │
│  │                                                               │
│  ├─ STEP 5.2: Check Cache                                      │
│  │  ├─ Call: cacheManager.getCache("vendors").get(123)         │
│  │  ├─ If cache hit: Use cached vendor data                    │
│  │  ├─ If cache miss: Load from database                       │
│  │  └─ Upon load: Store in cache for future requests           │
│  │                                                               │
│  ├─ STEP 5.3: Perform Calculations                             │
│  │  ├─ Calculate tax (GST, VAT)                                │
│  │  ├─ Apply discounts                                          │
│  │  ├─ Calculate total amount                                   │
│  │  ├─ Calculate payment terms                                  │
│  │  └─ Generate order reference number                         │
│  │                                                               │
│  └─ Continue to STEP 6                                          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 6: DATA ACCESS LAYER OPERATIONS                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Still in Service (persists data in DB)                         │
│                                                                  │
│  STEP 6.1: Create Entity                                        │
│  ├─ Convert DTO to Entity: PurchaseOrder entity                │
│  ├─ Set all fields:                                             │
│  │  ├─ entity.setVendorId(dto.getVendorId())                   │
│  │  ├─ entity.setAmount(calculatedAmount)                      │
│  │  ├─ entity.setStatus("PENDING")                             │
│  │  └─ entity.setCreatedBy(user.getEmail())                    │
│  └─ Set audit fields:                                           │
│     ├─ entity.setCreatedDate(LocalDateTime.now())              │
│     └─ entity.setUpdatedDate(LocalDateTime.now())              │
│                                                                  │
│  STEP 6.2: Save to Database via Repository                     │
│  ├─ Call: purchaseOrderRepo.save(entity)                       │
│  ├─ Repository delegates to Hibernate/JPA                      │
│  ├─ Hibernate determines if INSERT or UPDATE                   │
│  ├─ Generates SQL: INSERT INTO purchase_orders (...)           │
│  ├─ Executes against SQL Server                                │
│  ├─ Returns saved entity with generated ID                     │
│  └─ Example return: PurchaseOrder{id: 1001, ...}               │
│                                                                  │
│  STEP 6.3: Save Related Entities                               │
│  ├─ Loop through line items from request                       │
│  ├─ For each item:                                              │
│  │  ├─ Create PurchaseOrderLineItem entity                     │
│  │  ├─ Link to parent PurchaseOrder                            │
│  │  ├─ Save: lineItemRepo.save(item)                           │
│  │  └─ INSERT INTO purchase_order_items (...)                  │
│  └─ Update inventory (if material items)                        │
│     ├─ Fetch product: productRepo.findById(itemProductId)      │
│     ├─ Update stock: product.setQtyOnOrder(qty)                │
│     └─ Save: productRepo.save(product)                         │
│                                                                  │
│  STEP 6.4: Transaction Completion                              │
│  ├─ All operations succeed: COMMIT                             │
│  ├─ If any operation fails: ROLLBACK all changes               │
│  └─ Return persisted entity to service                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 7: POST-PROCESSING & NOTIFICATIONS                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Back in Service.createOrder()                                  │
│                                                                  │
│  STEP 7.1: Update Cache                                        │
│  ├─ Add to cache: cache.put(purchaseOrderId, entity)           │
│  └─ Invalidate related caches if needed                        │
│                                                                  │
│  STEP 7.2: Send Notifications (Async/Queue)                    │
│  ├─ Emit event: OrderCreatedEvent(orderId, vendorId)           │
│  ├─ Email notification service picks up event                  │
│  └─ Sends email to:                                             │
│     ├─ Finance team                                             │
│     ├─ Vendor (if enabled)                                      │
│     └─ Approvers                                                │
│                                                                  │
│  STEP 7.3: Log Audit Trail                                     │
│  ├─ Insert into AuditLog table:                                │
│  │  ├─ userId: 45                                              │
│  │  ├─ action: "PURCHASE_ORDER_CREATED"                        │
│  │  ├─ entityId: 1001                                          │
│  │  ├─ changes: {...}                                          │
│  │  └─ timestamp: now()                                        │
│  └─ auditLogRepo.save(log)                                     │
│                                                                  │
│  STEP 7.4: Convert to Response DTO                             │
│  ├─ Map entity fields to response DTO                          │
│  ├─ Include calculated values                                   │
│  ├─ Filter sensitive data                                       │
│  └─ Create: PurchaseOrderResponse{...}                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 8: RESPONSE SERIALIZATION & RETURN TO CLIENT               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Controller Method Returns Response                             │
│                                                                  │
│  ResponseLoggingInterceptor                                      │
│  ├─ Capture response data                                       │
│  ├─ Log: "Response: 201 Created, OrderId: 1001"                │
│  ├─ Record response time: 245ms                                 │
│  └─ Store audit record                                          │
│                                                                  │
│  Spring Framework                                                │
│  ├─ Convert response object to JSON                             │
│  ├─ Jackson ObjectMapper serialization                          │
│  └─ Set HTTP response headers:                                  │
│     ├─ Content-Type: application/json                           │
│     ├─ Content-Length: 2048                                     │
│     └─ Other metadata headers                                   │
│                                                                  │
│  Response to Client                                              │
│  ├─ HTTP Status: 201 Created                                    │
│  ├─ Response Body:                                              │
│  │  {                                                            │
│  │    "success": true,                                         │
│  │    "message": "Purchase order created successfully",         │
│  │    "data": {                                                │
│  │      "purchaseOrderId": 1001,                              │
│  │      "vendorId": 123,                                      │
│  │      "amount": 55000,                                      │
│  │      "status": "PENDING",                                  │
│  │      "createdDate": "2026-04-21T10:30:00Z"                │
│  │    }                                                         │
│  │  }                                                            │
│  └─ Transmission to client                                      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│ STEP 9: CLIENT PROCESSES RESPONSE                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Client Application                                              │
│  ├─ Receives HTTP 201 status                                    │
│  ├─ Parses JSON response                                        │
│  ├─ Extracts purchaseOrderId: 1001                              │
│  ├─ Updates UI (hide form, show success message)                │
│  ├─ Refresh list of purchase orders                             │
│  └─ May navigate to order detail page                           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Authentication & Security Flow

### JWT Authentication Process

```
┌──────────────────────────────────────────────────┐
│ CLIENT: LOGIN REQUEST                            │
├──────────────────────────────────────────────────┤
│ POST /api/auth/login                             │
│ {                                                 │
│   "email": "user@company.com",                   │
│   "password": "securePassword123"                │
│ }                                                 │
└──────────────────────────────────────────────────┘
              ↓
┌──────────────────────────────────────────────────┐
│ SERVER: AUTHENTICATION PROCESS                   │
├──────────────────────────────────────────────────┤
│                                                  │
│ 1. Receive login request in AuthController      │
│ 2. Extract email & password from request        │
│ 3. Call AuthenticationManager.authenticate()    │
│                                                  │
│    Spring Security Flow:                         │
│    ├─ UserDetailsServiceImpl.loadUserByUsername()│
│    ├─ Fetch user from database                  │
│    ├─ BCryptPasswordEncoder.matches()            │
│    │  └─ Compare hashed password                 │
│    ├─ If match: Create Authentication object    │
│    └─ If mismatch: Throw BadCredentialsException│
│                                                  │
│ 4. If authentication succeeds:                   │
│    ├─ Generate JWT token using JwtUtils         │
│    ├─ Token contains:                           │
│    │  ├─ sub: user.email                        │
│    │  ├─ roles: [USER, MANAGER]                 │
│    │  ├─ iat: issued at (now)                   │
│    │  ├─ exp: expiration (now + 24h)            │
│    │  └─ HS256 signature                        │
│    │     (signed with JWT_SECRET_KEY)           │
│    └─ Generate refresh token (7 days)           │
│                                                  │
│ 5. Store refresh token in DB for later use      │
│ 6. Return response to client                    │
│                                                  │
└──────────────────────────────────────────────────┘
              ↓
┌──────────────────────────────────────────────────┐
│ CLIENT: RESPONSE RECEIVED                        │
├──────────────────────────────────────────────────┤
│ HTTP 200 OK                                      │
│ {                                                 │
│   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5...", │
│   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5...",│
│   "tokenType": "Bearer",                         │
│   "expiresIn": 86400                            │
│ }                                                 │
│                                                  │
│ Browser Storage:                                 │
│ • localStorage.setItem("accessToken", token)    │
│ • localStorage.setItem("refreshToken", token)   │
└──────────────────────────────────────────────────┘
              ↓
┌──────────────────────────────────────────────────┐
│ AUTHENTICATED REQUESTS                           │
├──────────────────────────────────────────────────┤
│ Every subsequent request includes:               │
│                                                  │
│ GET /api/purchaseorder/1001                      │
│ Headers:                                         │
│   Authorization: Bearer eyJhbGciOiJIUzI1NiIs... │
│                                                  │
└──────────────────────────────────────────────────┘
              ↓
┌──────────────────────────────────────────────────┐
│ SERVER: TOKEN VALIDATION (per request)          │
├──────────────────────────────────────────────────┤
│                                                  │
│ AuthTokenFilter.doFilterInternal():             │
│                                                  │
│ 1. Extract Authorization header                 │
│ 2. Check if header starts with "Bearer "        │
│ 3. Extract token from header                    │
│ 4. Call JwtUtils.validateJwtToken(token)        │
│                                                  │
│    Token Validation Steps:                       │
│    ├─ Parse JWT header, payload, signature      │
│    ├─ Verify signature using JWT_SECRET_KEY     │
│    ├─ Check if token is not expired             │
│    ├─ Extract user email & roles                │
│    ├─ Load user from UserDetailsServiceImpl      │
│    └─ If all OK: Create Authentication          │
│                                                  │
│ 5. Set Authentication in SecurityContext:       │
│    ├─ SecurityContextHolder.getContext()        │
│    ├─ .setAuthentication(authentication)        │
│    └─ Available throughout request lifecycle    │
│                                                  │
│ 6. Pass request to next filter                  │
│                                                  │
│ If validation fails:                             │
│ └─ AuthEntryPointJwt.commence()                 │
│    └─ Return 401 Unauthorized                  │
│                                                  │
└──────────────────────────────────────────────────┘
              ↓
┌──────────────────────────────────────────────────┐
│ AUTHORIZATION (Role-based Access Control)       │
├──────────────────────────────────────────────────┤
│                                                  │
│ Controller Methods (optional):                   │
│ @PreAuthorize("hasRole('ADMIN')")               │
│ public void deleteUser(Long userId) { ... }     │
│                                                  │
│ Or via HttpSecurity configuration:             │
│ .authorizeRequests()                            │
│   .antMatchers("/admin/**").hasRole("ADMIN")    │
│   .antMatchers("/api/**").authenticated()       │
│   .antMatchers("/swagger-ui.html").permitAll()  │
│                                                  │
│ Authorization Check:                             │
│ ├─ Retrieve roles from Authentication object    │
│ ├─ Compare against required roles                │
│ ├─ If match: Allow request                      │
│ └─ If no match: Return 403 Forbidden            │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

## Data Access & Database Flow

### Repository Query Execution

```
Service Layer Call:
vendor = vendorRepository.findById(123);

         ↓

Repository Method Resolution:
├─ Method: findById(123)
├─ Extends: JpaRepository<Vendor, Long>
├─ Spring Data resolves to Hibernate Query
└─ Translated to SQL

         ↓

Hibernate ORM Processing:
├─ Generate SQL: SELECT * FROM vendors WHERE id = 123
├─ Prepare SQL statement
├─ Execute against SQL Server connection pool
└─ Parse ResultSet

         ↓

SQL Server Execution:
├─ Receive SQL query
├─ Check query plan cache
├─ Parse & optimize query
├─ Execute from disk/memory
├─ Return matching rows
└─ Send ResultSet back

         ↓

Mapping to Entity:
├─ Retrieve ResultSet from SQL
├─ Read column values:
│  ├─ id = 123
│  ├─ name = "Supplier Corp"
│  ├─ email = "contact@supplier.com"
│  ├─ active = true
│  └─ other fields...
├─ Instantiate Vendor entity
├─ Set entity fields via reflection
└─ Load relationships (lazy/eager)

         ↓

Result:
vendor = Vendor{
  id: 123,
  name: "Supplier Corp",
  email: "contact@supplier.com",
  active: true
}
```

### Custom Query with @Query Annotation

```
Repository:
@Query("SELECT new com.idayam.dto.VendorSummaryDTO(" +
       "v.id, v.name, COUNT(po.id)) " +
       "FROM Vendor v LEFT JOIN PurchaseOrder po ON v.id = po.vendorId " +
       "WHERE v.active = true " +
       "GROUP BY v.id, v.name")
List<VendorSummaryDTO> getActiveVendorsSummary();

         ↓

Service calls:
List<VendorSummaryDTO> summary = vendorRepository.getActiveVendorsSummary();

         ↓

Execution:
1. Spring Data processes @Query annotation
2. Parse JPQL query
3. Convert to SQL:
   SELECT v.id, v.name, COUNT(po.id)
   FROM vendors v
   LEFT JOIN purchase_orders po ON v.id = po.vendor_id
   WHERE v.active = 1
   GROUP BY v.id, v.name

4. Execute against SQL Server
5. Map results to VendorSummaryDTO constructor
6. Return List<VendorSummaryDTO>
```

---

## Module-Based Architecture

### Business Module Organization

```
Purchase Module:
├── Controllers:
│   ├─ PurchaseOrderController
│   ├─ PurchaseBillApproveController
│   └─ PurchaseQualityInspectionScoreController
├── Services:
│   ├─ PurchaseOrderService
│   ├─ PurchaseBillApproveService
│   └─ QualityInspectionService
└── Repositories:
    ├─ PurchaseOrderRepository
    ├─ PurchaseOrderItemsRepository
    ├─ PurchaseBillApproveRepository
    └─ SupplierEvaluationRepository

Inventory Module:
├── Controllers:
│   ├─ MaterialRequestController
│   ├─ MaterialIssueController
│   ├─ StockLedgerController
│   └─ BinDetailsController
├── Services:
│   ├─ MaterialRequestService
│   ├─ MaterialIssueService
│   ├─ StockLedgerService
│   └─ BinStockLockService
└── Repositories:
    ├─ MaterialRequestRepository
    ├─ MaterialIssueRepository
    ├─ StockLedgerRepository
    └─ BinDetailsRepository

Sales Module:
├── Controllers:
│   ├─ SalesController
│   ├─ SalesReturnController
│   ├─ DispatchController
│   └─ DpatchDespatchController
├── Services:
│   ├─ SalesService
│   ├─ SalesReturnService
│   ├─ DispatchService
│   └─ DpatchDespatchService
└── Repositories:
    ├─ SalesRepository
    ├─ SalesReturnRepository
    ├─ DispatchRepository
    └─ DpatchDespatchRepository

Finance Module:
├── Controllers:
│   ├─ VoucherController
│   ├─ JournalController
│   ├─ ExpenseController
│   └─ ViewAccountsLedgerController
├── Services:
│   ├─ VoucherService
│   ├─ JournalService
│   ├─ ExpenseService
│   └─ ViewAccountsLedgerService
└── Repositories:
    ├─ VoucherRepository
    ├─ JournalRepository
    ├─ ExpenseRepository
    └─ ViewAccountsLedgerRepository

HR Module:
├── Controllers:
│   ├─ AttendanceController
│   ├─ SalaryController
│   ├─ LeaveController
│   ├─ TrainingController
│   └─ MedicalCheckupController
├── Services:
│   ├─ AttendanceService
│   ├─ SalaryService
│   ├─ LeaveDetailsService
│   ├─ TrainingService
│   └─ MedicalCheckupService
└── Repositories:
    ├─ AttendanceRepository
    ├─ SalaryRepository
    ├─ LeaveDetailsRepository
    └─ TrainingRepository

Production Module:
├── Controllers:
│   ├─ ProductionEntryController
│   ├─ BlendingAdviceController
│   ├─ JaggeryInspectionController
│   └─ PMScheduleController
├── Services:
│   ├─ ProductionEntryService
│   ├─ BlendingAdviceService
│   ├─ JaggeryInspectionService
│   └─ PMScheduleService
└── Repositories:
    ├─ ProductionEntryRepository
    ├─ BlendingAdviceRepository
    ├─ JaggeryInspectionRepository
    └─ PMScheduleRepository
```

---

## Integration Points

### Cross-Module Dependencies

```
Sales → Inventory:
├─ When creating sales order
├─ Check stock availability
├─ Update inventory levels
└─ Create ledger entries

Purchase → Inventory:
├─ Receiving goods
├─ Update bin details
├─ Update reorder levels
└─ Update stock aging

Production → Inventory:
├─ Fetch raw materials
├─ Issue materials
├─ Update production status
├─ Receive finished goods

Finance → All Modules:
├─ Create voucher entries
├─ Generate expense records
├─ Update ledger accounts
├─ Generate financial reports

HR → Payroll:
├─ Attendance affects salary
├─ Leave creates vouchers
├─ Deductions from salary
└─ Generate pay slips

Notifications → All Modules:
├─ Order confirmation emails
├─ Stock alerts
├─ Approval notifications
├─ Payment reminders
└─ Error notifications
```

---

## Async Processing & Scheduling

### Scheduled Tasks Configuration

```
IdayamApplication.java:
@SpringBootApplication
@EnableScheduling    ← Enable @Scheduled tasks
@EnableAsync         ← Enable @Async methods
public class IdayamApplication { ... }

         ↓

Scheduled Services:
├─ LeaveSchedulerService:
│  @Scheduled(cron = "0 0 1 1 * ?")  // Monthly on 1st
│  public void processLeaveCarryOver() { ... }
│
├─ SalarySchedulerService:
│  @Scheduled(cron = "0 0 0 30 * ?")  // 30th of month
│  public void generateSalaryRun() { ... }
│
├─ DayEndClosingService:
│  @Scheduled(cron = "0 0 23 * * ?")  // Daily at 11 PM
│  public void performDayEnd() { ... }
│
└─ EmailNotificationService:
   @Scheduled(fixedDelay = 30000)    // Every 30 seconds
   public void sendPendingEmails() { ... }

         ↓

Background Processing:
├─ Email Sending (Async):
│  @Async
│  public void sendOrderConfirmationEmail(Order order) {
│    // Email sent in separate thread
│  }
│
├─ File Generation:
│  @Async
│  public void generateReportPDF(ReportCriteria criteria) {
│    // PDF created asynchronously
│  }
│
└─ Data Export:
   @Async
   public void exportToExcel(List<Data> items) {
     // Export in background
   }
```

---

## Caching Strategy

### Cache Configuration

```
Service Layer Caching:

PurchaseOrderService:
├─ @Cacheable("purchaseOrders")
│  public PurchaseOrder getById(Long id) {
│    // First call: Query DB, then cache
│    // Subsequent calls: Return from cache
│  }
│
├─ @CachePut("purchaseOrders", key = "#result.id")
│  public PurchaseOrder update(PurchaseOrder po) {
│    // Always execute method, then update cache
│  }
│
└─ @CacheEvict("purchaseOrders", allEntries = true)
   public void clearAllCache() {
     // Remove all entries from purchaseOrders cache
   }

Cache Types:
├─ In-Memory Cache:
│  ├─ Fast (Nanoseconds)
│  ├─ Limited by heap memory
│  └─ Lost on restart
│
└─ Redis Cache (Optional):
   ├─ Faster than DB
   ├─ Shared across instances
   └─ Survives application restart

Cache Invalidation Strategy:
├─ Time-based: TTL (Time To Live)
├─ Event-based: Clear on data change
├─ Manual: @CacheEvict annotation
└─ Conditional: Custom cache logic
```

---

## Error Handling Flow

### Exception Handling Architecture

```
Controller Method Execution:

try {
  purchaseOrderService.createOrder(dto);
} 
catch (Exception ex) {
  // Handled by @ExceptionHandler
}

         ↓

Exception Caught:
├─ Validation Exception
│  ├─ MethodArgumentNotValidException
│  ├─ ConstraintViolationException
│  └─ Return 400 Bad Request
│
├─ Authentication Exception
│  ├─ AuthenticationException
│  ├─ TokenExpiredException
│  └─ Return 401 Unauthorized
│
├─ Authorization Exception
│  ├─ AccessDeniedException
│  └─ Return 403 Forbidden
│
├─ Resource Not Found
│  ├─ EntityNotFoundException
│  ├─ Vendor not found
│  └─ Return 404 Not Found
│
├─ Business Logic Exception
│  ├─ InsufficientInventoryException
│  ├─ Invalid order amount
│  └─ Return 400/409 Conflict
│
└─ System Exception
   ├─ NullPointerException
   ├─ Database connection error
   ├─ File I/O error
   └─ Return 500 Internal Server Error

         ↓

Global Exception Handler:
@ControllerAdvice
public class GlobalExceptionHandler {
  
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(ex) {
    // Build error details
    // Log error
    // Return error response
  }
  
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneric(ex) {
    // Generic error handling
  }
}

         ↓

Error Response Structure:
{
  "success": false,
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    {
      "field": "vendorId",
      "issue": "Vendor must not be null"
    }
  ],
  "timestamp": "2026-04-21T10:30:00Z",
  "path": "/api/purchaseorder"
}

         ↓

Error Logging:
├─ Log to console
├─ Log to file (logs/application.log)
├─ Log to database (ExceptionLogRepository)
└─ Alert monitoring system if critical
```

---

## Summary of Architectural Patterns

| Pattern | Usage | Example |
|---------|-------|---------|
| **Controller to Service** | Request handling to business logic delegation | OrderController → OrderService |
| **Service to Repository** | Business logic to data access | OrderService → OrderRepository |
| **Caching** | Improve read performance | @Cacheable on findById() |
| **Transactions** | Data consistency | @Transactional on create/update |
| **Async Processing** | Non-blocking operations | @Async for email sending |
| **Scheduled Tasks** | Periodic operations | @Scheduled for day-end closing |
| **Exception Handling** | Consistent error responses | @ControllerAdvice |
| **Security** | Authentication & authorization | JWT + Spring Security |
| **Logging & Auditing** | Tracking & compliance | AuditLogRepository |
| **Multi-Tenancy** | Data isolation | Branch-based filtering |

---

## Architecture Evolution Considerations

### For Future Scalability

1. **Microservices Migration**
   - Split monolith by business domains
   - Use Spring Cloud for service discovery
   - Implement API Gateway
   - Use message queues (RabbitMQ/Kafka) for async communication

2. **Caching Enhancement**
   - Implement distributed cache (Redis)
   - Cache warming strategies
   - Cache invalidation optimization

3. **API Evolution**
   - Versioning (/v1/, /v2/)
   - GraphQL endpoint alongside REST
   - API rate limiting
   - Token-based API authentication

4. **Performance Optimization**
   - Database query optimization
   - Read replicas for reporting
   - Elasticsearch for full-text search
   - CDN for static content

5. **Observability**
   - Distributed tracing (OpenTelemetry)
   - Centralized logging (ELK Stack)
   - Metrics collection (Prometheus/Grafana)
   - Health check endpoints

---

## Related Documentation

- [QUICK_START_GUIDE.md](QUICK_START_GUIDE.md) - Getting started
- [DEVELOPER_SETUP_GUIDE.md](DEVELOPER_SETUP_GUIDE.md) - Development environment
- [CONTROLLERS_OVERVIEW.md](CONTROLLERS_OVERVIEW.md) - Controller modules overview
- [API_ENDPOINTS_REFERENCE.md](API_ENDPOINTS_REFERENCE.md) - API endpoint details
- [HANDOVER_DOCUMENT.md](HANDOVER_DOCUMENT.md) - Deployment & operations

---

**Document Version:** 1.0  
**Last Updated:** April 21, 2026  
**Maintained By:** Development Team
