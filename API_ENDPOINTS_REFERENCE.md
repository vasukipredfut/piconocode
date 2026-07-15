# Idayam ERP Backend - API Endpoints Reference

## Quick Navigation

### Authentication
- [User Authentication](#user-authentication)
- [Token Management](#token-management)

### Human Resources (HR)
- [User Management](#user-management)
- [Attendance](#attendance)
- [Leave Management](#leave-management)
- [Shift Management](#shift-management)
- [Salary & Wages](#salary--wages)

### Inventory & Stock
- [Stock Management](#stock-management)
- [Material Management](#material-management)
- [Warehouse Management](#warehouse-management)

### Procurement & Purchasing
- [Purchase Orders](#purchase-orders)
- [Vendor Management](#vendor-management)
- [Supplier Management](#supplier-management)

### Sales & Orders
- [Sales Orders](#sales-orders)
- [Sales Transactions](#sales-transactions)

### Financial & Accounting
- [Vouchers](#vouchers)
- [Bills & Invoices](#bills--invoices)
- [Journal Entries](#journal-entries)

### Production
- [Production Entry](#production-entry)
- [Quality Control](#quality-control)
- [Inspections](#inspections)

### Administration
- [Master Data](#master-data)
- [Common Operations](#common-operations)
- [File Management](#file-management)

---

## Authentication

### User Authentication

#### Login
```
POST /api/login
Content-Type: application/json

Request:
{
    "username": "user@company.com",
    "password": "password123"
}

Response: 200 OK
{
    "id": 1,
    "username": "user@company.com",
    "email": "user@company.com",
    "roles": ["USER", "MANAGER"],
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer"
}

Error Responses:
- 401 Unauthorized: Invalid credentials
- 400 Bad Request: Missing required fields
```

#### Register New User
```
POST /api/register
Content-Type: application/json

Request:
{
    "username": "newuser",
    "email": "newuser@company.com",
    "password": "SecurePassword123!",
    "firstName": "John",
    "lastName": "Doe"
}

Response: 201 Created
{
    "message": "User registered successfully",
    "userId": 10
}
```

#### Change Password
```
PUT /api/user/changePassword
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "oldPassword": "currentPassword",
    "newPassword": "newSecurePassword123!",
    "confirmPassword": "newSecurePassword123!"
}

Response: 200 OK
{
    "message": "Password changed successfully"
}

Error: 400 Bad Request - Passwords don't match
Error: 401 Unauthorized - Old password incorrect
```

### Token Management

#### Refresh Token
```
POST /api/refreshToken
Content-Type: application/json

Request:
{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}

Response: 200 OK
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer"
}

Error: 401 Unauthorized - Invalid refresh token
```

#### Logout
```
POST /api/logout
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}

Response: 200 OK
{
    "message": "Logged out successfully"
}
```

---

## User Management

### List All Users
```
GET /api/user?page=0&size=20&sort=username,asc
Authorization: Bearer {token}

Query Parameters:
- page: Page number (0-indexed) - Default: 0
- size: Items per page - Default: 20
- sort: Sort by field - Default: id,desc

Response: 200 OK
{
    "content": [
        {
            "id": 1,
            "username": "admin",
            "email": "admin@company.com",
            "firstName": "Admin",
            "lastName": "User",
            "active": true,
            "createdDate": "2025-01-01T10:00:00",
            "roles": [
                {
                    "id": 1,
                    "roleName": "ADMIN"
                }
            ]
        }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "currentPage": 0,
    "hasNext": true
}
```

### Get User by ID
```
GET /api/user/{userId}
Authorization: Bearer {token}

Response: 200 OK
{
    "id": 1,
    "username": "admin",
    "email": "admin@company.com",
    "firstName": "Admin",
    "lastName": "User",
    "active": true,
    "createdDate": "2025-01-01T10:00:00",
    "designation": {
        "id": 1,
        "designationName": "Manager"
    },
    "department": {
        "id": 1,
        "departmentName": "IT"
    },
    "branch": {
        "id": 1,
        "branchName": "Head Office"
    },
    "roles": [
        {
            "id": 1,
            "roleName": "ADMIN"
        }
    ]
}

Error: 404 Not Found
```

### Update User
```
PUT /api/user/{userId}
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@company.com",
    "designationId": 5,
    "departmentId": 3,
    "branchId": 2,
    "active": true
}

Response: 200 OK
{
    "message": "User updated successfully",
    "userId": 1
}

Error: 404 Not Found - User doesn't exist
Error: 400 Bad Request - Invalid input
```

### Assign Roles to User
```
PUT /api/user/{userId}/roles
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "roleIds": [1, 2, 3]
}

Response: 200 OK
{
    "message": "Roles assigned successfully"
}
```

### Deactivate User
```
DELETE /api/user/{userId}
Authorization: Bearer {token}

Response: 200 OK
{
    "message": "User deactivated successfully"
}

Error: 404 Not Found
```

---

## HR Module

### Attendance

#### Mark Attendance
```
POST /api/attendance
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "employeeId": 5,
    "punchIn": "2026-04-21T08:30:00",
    "punchOut": "2026-04-21T17:30:00",
    "status": "PRESENT",
    "remarks": "Regular attendance"
}

Response: 201 Created
{
    "attendanceId": 100,
    "message": "Attendance recorded"
}

Error: 400 Bad Request - Invalid employee
```

#### Get Attendance List
```
GET /api/attendance?employeeId={id}&date={date}&page=0&size=20
Authorization: Bearer {token}

Query Parameters:
- employeeId: Filter by employee
- date: Filter by specific date (YYYY-MM-DD)
- page: Page number
- size: Items per page

Response: 200 OK
{
    "content": [
        {
            "attendanceId": 100,
            "employee": {
                "id": 5,
                "name": "John Doe"
            },
            "punchIn": "2026-04-21T08:30:00",
            "punchOut": "2026-04-21T17:30:00",
            "workingHours": 9.0,
            "status": "PRESENT",
            "remarks": "Regular attendance",
            "createdDate": "2026-04-21T08:30:00"
        }
    ],
    "totalElements": 150,
    "totalPages": 8
}
```

### Leave Management

#### Apply Leave
```
POST /api/leave
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "leaveTypeId": 1,
    "startDate": "2026-05-01",
    "endDate": "2026-05-03",
    "reasonForLeave": "Personal work",
    "numberOfDays": 3,
    "applierId": 10
}

Response: 201 Created
{
    "leaveId": 50,
    "message": "Leave application submitted",
    "status": "PENDING_APPROVAL"
}

Error: 400 Bad Request - Invalid dates or overlapping leave
```

#### Get Leave Applications
```
GET /api/leave?employeeId={id}&status={status}&page=0&size=20
Authorization: Bearer {token}

Query Parameters:
- employeeId: Filter by employee
- status: PENDING, APPROVED, REJECTED, CANCELLED
- page: Page number
- size: Items per page

Response: 200 OK
{
    "content": [
        {
            "leaveId": 50,
            "employee": {
                "id": 10,
                "name": "Jane Smith"
            },
            "leaveType": {
                "id": 1,
                "typeName": "Casual Leave"
            },
            "startDate": "2026-05-01",
            "endDate": "2026-05-03",
            "numberOfDays": 3,
            "status": "APPROVED",
            "reasonForLeave": "Personal work",
            "appliedDate": "2026-04-20",
            "approvedDate": "2026-04-21",
            "approvedBy": "Manager Name"
        }
    ],
    "totalElements": 20,
    "totalPages": 1
}
```

#### Approve/Reject Leave
```
PUT /api/leave/{leaveId}/approve
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "status": "APPROVED",
    "remarks": "Approved as per request"
}

Response: 200 OK
{
    "message": "Leave application processed"
}

Error: 404 Not Found
Error: 400 Bad Request - Invalid status
```

### Shift Management

#### Create Shift
```
POST /api/shift
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "shiftName": "Morning Shift",
    "shiftCode": "MORNING",
    "startTime": "08:00:00",
    "endTime": "16:00:00",
    "duration": 8,
    "breakTime": 1,
    "workingHours": 7,
    "isActive": true
}

Response: 201 Created
{
    "shiftId": 5,
    "message": "Shift created successfully"
}
```

#### List Shifts
```
GET /api/shift?page=0&size=20
Authorization: Bearer {token}

Response: 200 OK
{
    "content": [
        {
            "shiftId": 1,
            "shiftName": "Morning Shift",
            "shiftCode": "MORNING",
            "startTime": "08:00:00",
            "endTime": "16:00:00",
            "duration": 8,
            "breakTime": 1,
            "workingHours": 7,
            "isActive": true
        }
    ],
    "totalElements": 5,
    "totalPages": 1
}
```

### Salary Management

#### Process Salary
```
POST /api/salary
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "employeeId": 10,
    "salaryMonth": "2026-04",
    "basicSalary": 50000,
    "allowances": 10000,
    "deductions": 5000,
    "netSalary": 55000,
    "status": "DRAFT"
}

Response: 201 Created
{
    "salaryId": 100,
    "message": "Salary record created"
}
```

#### Get Salary Details
```
GET /api/salary/{salaryId}
Authorization: Bearer {token}

Response: 200 OK
{
    "salaryId": 100,
    "employee": {
        "id": 10,
        "name": "John Doe"
    },
    "salaryMonth": "2026-04",
    "basicSalary": 50000,
    "allowances": 10000,
    "deductions": 5000,
    "netSalary": 55000,
    "status": "APPROVED",
    "createdDate": "2026-04-01",
    "processedDate": "2026-04-21"
}
```

---

## Inventory & Stock

### Stock Management

#### Get Stock Levels
```
GET /api/stock?page=0&size=20&warehouseId={id}
Authorization: Bearer {token}

Query Parameters:
- warehouseId: Filter by warehouse
- productId: Filter by product
- page: Page number
- size: Items per page

Response: 200 OK
{
    "content": [
        {
            "stockId": 1,
            "product": {
                "id": 100,
                "productCode": "PROD001",
                "productName": "Product Name"
            },
            "warehouse": {
                "id": 5,
                "warehouseName": "Main Warehouse"
            },
            "quantity": 500,
            "reorderLevel": 100,
            "reorderQuantity": 200,
            "lastUpdated": "2026-04-21T10:00:00"
        }
    ],
    "totalElements": 150
}
```

#### Update Stock
```
PUT /api/stock/{stockId}
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "quantity": 600,
    "remarks": "Stock count verification"
}

Response: 200 OK
{
    "message": "Stock updated successfully"
}
```

### Material Request

#### Create Material Request
```
POST /api/materialRequest
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "departmentId": 3,
    "requestDate": "2026-04-21",
    "items": [
        {
            "productId": 100,
            "quantity": 50,
            "unit": "KG",
            "remarks": "Required for production"
        }
    ],
    "purpose": "Production"
}

Response: 201 Created
{
    "requestId": 200,
    "message": "Material request created",
    "status": "PENDING_APPROVAL"
}
```

#### Get Material Requests
```
GET /api/materialRequest?departmentId={id}&status={status}&page=0&size=20
Authorization: Bearer {token}

Response: 200 OK
{
    "content": [
        {
            "requestId": 200,
            "department": {
                "id": 3,
                "departmentName": "Production"
            },
            "requestDate": "2026-04-21",
            "status": "APPROVED",
            "items": [
                {
                    "itemId": 1,
                    "product": {
                        "id": 100,
                        "productName": "Product Name"
                    },
                    "quantity": 50,
                    "unit": "KG",
                    "remarks": "Required for production"
                }
            ],
            "totalValue": 5000,
            "createdBy": "User Name",
            "createdDate": "2026-04-21"
        }
    ],
    "totalElements": 50
}
```

---

## Procurement

### Purchase Orders

#### Create Purchase Order
```
POST /api/purchaseOrder
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "vendorId": 10,
    "poNumber": "PO-2026-001",
    "poDate": "2026-04-21",
    "expectedDeliveryDate": "2026-05-21",
    "items": [
        {
            "productId": 100,
            "quantity": 1000,
            "unit": "KG",
            "unitPrice": 10.5,
            "totalAmount": 10500
        }
    ],
    "terms": "Net 30",
    "remarks": "Standard order"
}

Response: 201 Created
{
    "poId": 50,
    "poNumber": "PO-2026-001",
    "message": "Purchase order created"
}

Error: 400 Bad Request - Invalid vendor or items
```

#### Get Purchase Orders
```
GET /api/purchaseOrder?vendorId={id}&status={status}&page=0&size=20
Authorization: Bearer {token}

Query Parameters:
- vendorId: Filter by vendor
- status: DRAFT, CONFIRMED, RECEIVED, CANCELLED
- page: Page number
- size: Items per page

Response: 200 OK
{
    "content": [
        {
            "poId": 50,
            "poNumber": "PO-2026-001",
            "vendor": {
                "id": 10,
                "vendorName": "Supplier Inc."
            },
            "poDate": "2026-04-21",
            "expectedDeliveryDate": "2026-05-21",
            "status": "CONFIRMED",
            "items": [
                {
                    "itemId": 1,
                    "product": {
                        "id": 100,
                        "productName": "Product Name"
                    },
                    "quantity": 1000,
                    "unit": "KG",
                    "unitPrice": 10.5,
                    "totalAmount": 10500
                }
            ],
            "totalValue": 10500,
            "createdDate": "2026-04-21"
        }
    ],
    "totalElements": 30
}
```

#### Update PO Status
```
PUT /api/purchaseOrder/{poId}/status
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "status": "CONFIRMED"
}

Response: 200 OK
{
    "message": "Purchase order status updated"
}
```

### Vendor Management

#### Create Vendor
```
POST /api/vendor
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "vendorName": "ABC Supplies Ltd.",
    "vendorCode": "VENDOR001",
    "email": "contact@abcsupplies.com",
    "phone": "+91-9999999999",
    "address": "123 Business Park",
    "city": "Chennai",
    "state": "Tamil Nadu",
    "zipCode": "600001",
    "country": "India",
    "gstNumber": "33AABCT1234F1Z0",
    "paymentTerms": "Net 30",
    "isActive": true
}

Response: 201 Created
{
    "vendorId": 10,
    "message": "Vendor created successfully"
}
```

#### Get Vendors
```
GET /api/vendor?page=0&size=20&isActive=true
Authorization: Bearer {token}

Response: 200 OK
{
    "content": [
        {
            "vendorId": 10,
            "vendorName": "ABC Supplies Ltd.",
            "vendorCode": "VENDOR001",
            "email": "contact@abcsupplies.com",
            "phone": "+91-9999999999",
            "address": "123 Business Park",
            "city": "Chennai",
            "state": "Tamil Nadu",
            "zipCode": "600001",
            "gstNumber": "33AABCT1234F1Z0",
            "paymentTerms": "Net 30",
            "isActive": true,
            "createdDate": "2026-04-01"
        }
    ],
    "totalElements": 25
}
```

---

## Sales & Orders

### Sales Orders

#### Create Sales Order
```
POST /api/order
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "customerId": 5,
    "orderNumber": "ORD-2026-001",
    "orderDate": "2026-04-21",
    "deliveryDate": "2026-05-21",
    "items": [
        {
            "productId": 100,
            "quantity": 100,
            "unit": "KG",
            "unitPrice": 15.0,
            "totalAmount": 1500
        }
    ],
    "discountPercent": 5,
    "taxRate": 18,
    "totalValue": 1710,
    "remarks": "Urgent order"
}

Response: 201 Created
{
    "orderId": 100,
    "orderNumber": "ORD-2026-001",
    "message": "Sales order created"
}
```

#### Get Sales Orders
```
GET /api/order?customerId={id}&status={status}&page=0&size=20
Authorization: Bearer {token}

Response: 200 OK
{
    "content": [
        {
            "orderId": 100,
            "orderNumber": "ORD-2026-001",
            "customer": {
                "id": 5,
                "customerName": "Customer Name"
            },
            "orderDate": "2026-04-21",
            "deliveryDate": "2026-05-21",
            "status": "CONFIRMED",
            "items": [...],
            "totalValue": 1710,
            "createdDate": "2026-04-21"
        }
    ],
    "totalElements": 50
}
```

---

## Financial & Accounting

### Vouchers

#### Create Payment Voucher
```
POST /api/voucher
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
    "voucherNumber": "PV-2026-001",
    "voucherDate": "2026-04-21",
    "voucherType": "PAYMENT",
    "paymentMode": "CHEQUE",
    "amount": 50000,
    "description": "Payment for PO-2026-001",
    "vendorId": 10,
    "remarks": "Cheque #12345"
}

Response: 201 Created
{
    "voucherId": 200,
    "message": "Voucher created successfully"
}
```

#### Get Vouchers
```
GET /api/voucher?voucherType={type}&page=0&size=20
Authorization: Bearer {token}

Response: 200 OK
{
    "content": [
        {
            "voucherId": 200,
            "voucherNumber": "PV-2026-001",
            "voucherDate": "2026-04-21",
            "voucherType": "PAYMENT",
            "paymentMode": "CHEQUE",
            "amount": 50000,
            "description": "Payment for PO-2026-001",
            "status": "DRAFT",
            "createdDate": "2026-04-21"
        }
    ],
    "totalElements": 100
}
```

---

## Common Response Codes

### Success Responses
- **200 OK** - Request successful
- **201 Created** - Resource created successfully
- **204 No Content** - Request successful, no response body

### Client Error Responses
- **400 Bad Request** - Invalid input or malformed request
- **401 Unauthorized** - Missing or invalid authentication
- **403 Forbidden** - Authenticated but not authorized
- **404 Not Found** - Resource not found
- **409 Conflict** - Duplicate record or business rule violation
- **422 Unprocessable Entity** - Validation failed

### Server Error Responses
- **500 Internal Server Error** - Server error
- **501 Not Implemented** - Endpoint not available
- **503 Service Unavailable** - Server temporarily unavailable

### Error Response Format
```json
{
    "timestamp": "2026-04-21T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid input provided",
    "path": "/api/user"
}
```

---

## Pagination

All list endpoints support pagination with these query parameters:

```
GET /api/resource?page=0&size=20&sort=id,desc

Parameters:
- page: Zero-indexed page number (default: 0)
- size: Items per page (default: 20, max: 100)
- sort: Sort criteria (field,direction - e.g., "name,asc")

Response includes:
{
    "content": [...],           // Array of items
    "totalElements": 150,       // Total records
    "totalPages": 8,            // Total pages
    "currentPage": 0,           // Current page
    "pageSize": 20,             // Items per page
    "hasNext": true,            // Has more pages
    "hasPrevious": false        // Has previous pages
}
```

---

## Authentication Header

All endpoints (except login/register) require:

```
Authorization: Bearer {accessToken}

Example:
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTY5NzAwMDAwMCwiZXhwIjoxNjk3MDg2NDAwfQ.signature...
```

---

## API Testing Tools

### Using curl
```bash
# GET request
curl -H "Authorization: Bearer {token}" \
     http://localhost:8080/api/user

# POST request
curl -X POST \
     -H "Authorization: Bearer {token}" \
     -H "Content-Type: application/json" \
     -d '{"name":"value"}' \
     http://localhost:8080/api/resource
```

### Using Postman
1. Import collection from Swagger: GET http://localhost:8080/v2/api-docs
2. Set Authorization: Type = Bearer Token, Token = {accessToken}
3. Set Content-Type = application/json

### Using Swagger UI
- Access: http://localhost:8080/swagger-ui.html
- Click "Authorize" button
- Enter token value
- Try endpoints directly in UI

---

## Rate Limiting & Throttling

Currently no rate limiting is implemented. Consider adding for production:

```properties
# Suggested configuration
spring.servlet.http.max-request-count=1000
spring.servlet.http.max-requests-per-second=100
```

---

## Deprecation Policy

When APIs are deprecated:
1. Announced 1 month in advance
2. Marked with `@Deprecated` annotation
3. Suggested replacement provided in documentation
4. Removed after 3 months of deprecation

---

## For Complete Endpoint List

Access Swagger documentation at runtime:
```
http://localhost:8080/swagger-ui.html
```

This provides an interactive interface with all 400+ endpoints, request/response models, and try-it-out functionality.

---

## Next Steps

1. Review specific controller documentation: [CONTROLLER_DETAILED_DOCUMENTATION.md]
2. Check setup guide: [DEVELOPER_SETUP_GUIDE.md]
3. Access live API docs: http://localhost:8080/swagger-ui.html
4. Read handover document: [HANDOVER_DOCUMENT.md]
