# Idayam ERP Backend - PostgreSQL Database Setup Guide

**Project:** Idayam ERP Backend System  
**Database Type:** PostgreSQL 12+  
**Document Date:** April 22, 2026  
**Version:** 1.0  

---

## Table of Contents

1. [Database Overview](#database-overview)
2. [Prerequisites](#prerequisites)
3. [Installation Guide](#installation-guide)
4. [Database Configuration](#database-configuration)
5. [User & Credentials Setup](#user--credentials-setup)
6. [Database Initialization](#database-initialization)
7. [Migration Scripts](#migration-scripts)
8. [Backup & Recovery](#backup--recovery)
9. [Troubleshooting](#troubleshooting)
10. [Security Best Practices](#security-best-practices)

---

## Database Overview

### Database Architecture

```
┌─────────────────────────────────────────────┐
│         PostgreSQL Instance                 │
├─────────────────────────────────────────────┤
│  Database: idayam_erp                       │
│  Port: 5432 (default)                       │
│  User: idayam_user                          │
│  Password: [Secure - Set during setup]      │
│                                             │
│  100+ Tables (Entity Models):               │
│  ├── Users & Roles                          │
│  ├── HR & Attendance                        │
│  ├── Inventory & Stock                      │
│  ├── Procurement & Vendors                  │
│  ├── Sales & Orders                         │
│  ├── Finance & Accounts                     │
│  ├── Production & Quality                   │
│  └── [15 more business modules]             │
└─────────────────────────────────────────────┘
```

### Database Schema Overview

**Core System Tables:**
- `users` - System user accounts
- `roles` - Role definitions
- `permissions` - Module permissions
- `refresh_tokens` - JWT refresh tokens

**Business Tables (100+):**
- Employee master, Attendance, Leave, Salary
- Products, Stock, Bins, Warehouses
- Purchase Orders, Vendors, Bills
- Sales Orders, Invoices, Customers
- Journals, Vouchers, Ledgers
- [And 80+ more business entity tables]

---

## Prerequisites

### System Requirements

**Minimum:**
- 2GB RAM
- 10GB disk space
- PostgreSQL 12 or higher
- psql command-line tool

**Recommended for Production:**
- 4GB+ RAM
- 100GB+ disk space
- PostgreSQL 12+ with streaming replication
- Separate database server

### Required Tools

```bash
# Verify PostgreSQL installation
psql --version

# Should show: psql (PostgreSQL) 12.x or higher

# If not installed, install via:
# Windows: Download from https://www.postgresql.org/download/windows/
# Mac: brew install postgresql
# Linux: apt-get install postgresql postgresql-contrib
```

### Network Requirements

- Port 5432 (default PostgreSQL port) must be accessible
- For remote connections, configure firewall rules
- For production, use encrypted connections (SSL/TLS)

---

## Installation Guide

### Windows Installation

**Step 1: Download PostgreSQL**
```
1. Visit https://www.postgresql.org/download/windows/
2. Download PostgreSQL 12+ installer
3. Run the installer
```

**Step 2: Run Installer**
```
1. Accept license
2. Choose installation directory (e.g., C:\Program Files\PostgreSQL\12)
3. Select components:
   - PostgreSQL Server
   - pgAdmin 4 (admin tool - optional)
   - Command Line Tools
4. Set password for 'postgres' superuser (IMPORTANT - remember this!)
5. Set port to 5432
6. Complete installation
```

**Step 3: Verify Installation**
```bash
# Open Command Prompt
psql --version

# Should display: psql (PostgreSQL) 12.x or higher
```

### macOS Installation

```bash
# Using Homebrew (recommended)
brew install postgresql

# Start PostgreSQL
brew services start postgresql

# Verify installation
psql --version
```

### Linux (Ubuntu/Debian) Installation

```bash
# Update package manager
sudo apt-get update

# Install PostgreSQL
sudo apt-get install postgresql postgresql-contrib

# Start PostgreSQL service
sudo systemctl start postgresql

# Enable auto-start
sudo systemctl enable postgresql

# Verify installation
psql --version
```

---

## Database Configuration

### Connection Parameters

**Development Environment:**
```properties
Database Name:   idayam_erp
Host:           localhost
Port:           5432
User:           idayam_user
Password:       [Set during setup]
SSL:            false (not required for local development)
```

**Production Environment:**
```properties
Database Name:   idayam_erp
Host:           db-server.example.com
Port:           5432 (or custom)
User:           idayam_user
Password:       [Secure - from vault]
SSL:            true (required)
Replication:    enabled (optional HA)
Connection Pool: 20 connections
```

### PostgreSQL Configuration File

**Location:**
- Windows: `C:\Program Files\PostgreSQL\12\data\postgresql.conf`
- Linux: `/etc/postgresql/12/main/postgresql.conf`
- Mac: `/usr/local/var/postgres/postgresql.conf`

**Key Settings for Development:**

```ini
# Listen on all interfaces (development only)
listen_addresses = '*'

# Connection limits
max_connections = 100
shared_buffers = 256MB
effective_cache_size = 1GB

# Logging
log_statement = 'all'
log_duration = on
log_min_duration_statement = 1000

# Performance
work_mem = 4MB
maintenance_work_mem = 64MB
```

**For Production:**

```ini
# Specific network interface
listen_addresses = '192.168.1.100'

# Higher connection limits
max_connections = 200
shared_buffers = 4GB
effective_cache_size = 8GB

# SSL/TLS
ssl = on
ssl_cert_file = '/path/to/server.crt'
ssl_key_file = '/path/to/server.key'

# Streaming Replication (HA)
wal_level = replica
max_wal_senders = 3
max_replication_slots = 3
```

---

## User & Credentials Setup

### Default Credentials

**Superuser:**
```
Username: postgres
Password: [Set during installation]
Role:     SUPERUSER
```

### Create Application User

**Step 1: Connect as Superuser**

```bash
# Windows/Linux/Mac
psql -U postgres

# Or on Linux with sudo
sudo -u postgres psql
```

**Step 2: Create Database User**

```sql
-- Create user with password
CREATE USER idayam_user WITH PASSWORD 'ComplexPassword123!';

-- Set user defaults
ALTER USER idayam_user SET client_encoding TO 'utf8';
ALTER USER idayam_user SET default_transaction_isolation TO 'read committed';
ALTER USER idayam_user SET default_transaction_deferrable TO 'off';
ALTER USER idayam_user SET default_sequence_ownership TO 'owned_by_table';

-- Verify user created
\du
```

### Create Database

```sql
-- Create database owned by application user
CREATE DATABASE idayam_erp OWNER idayam_user;

-- Set database defaults
ALTER DATABASE idayam_erp SET client_encoding TO 'utf8';
ALTER DATABASE idayam_erp SET default_transaction_isolation TO 'read committed';

-- Verify database created
\l
```

### Grant Permissions

```sql
-- Connect to the database
\c idayam_erp

-- Grant schema permissions
GRANT USAGE ON SCHEMA public TO idayam_user;
GRANT CREATE ON SCHEMA public TO idayam_user;

-- Grant table permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO idayam_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO idayam_user;

-- Grant default permissions for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO idayam_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO idayam_user;
```

### Verify User Setup

```bash
# Test connection as application user
psql -U idayam_user -d idayam_erp -h localhost

# If successful, you should see the psql prompt:
# idayam_erp=>

# Verify permissions
\dt  # List tables (empty initially)
\dn  # List schemas
```

---

## Database Initialization

### Step-by-Step Setup

**Step 1: Connect to Database**

```bash
# Login as application user
psql -U idayam_user -d idayam_erp -h localhost

# Or Windows with password prompt
psql -U idayam_user -d idayam_erp -h 127.0.0.1 -W
```

**Step 2: Run Migration Scripts**

```bash
# Execute all migration scripts in order
psql -U idayam_user -d idayam_erp -f 01-schema.sql
psql -U idayam_user -d idayam_erp -f 02-core-tables.sql
psql -U idayam_user -d idayam_erp -f 03-business-tables.sql
psql -U idayam_user -d idayam_erp -f 04-indexes.sql
psql -U idayam_user -d idayam_erp -f 05-seed-data.sql
```

**Step 3: Verify Installation**

```bash
# Connect to database
psql -U idayam_user -d idayam_erp

# Check tables
SELECT COUNT(*) as total_tables FROM information_schema.tables 
WHERE table_schema = 'public';

# Expected output: 100+ tables

# Check data
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM roles;
SELECT COUNT(*) FROM permissions;

# Exit
\q
```

### Automated Setup Script

**Create `setup-database.sh` (Linux/Mac):**

```bash
#!/bin/bash

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

echo "Starting Idayam ERP Database Setup..."

# Step 1: Create database and user
echo "Creating database and user..."
psql -U postgres -c "CREATE USER idayam_user WITH PASSWORD 'ComplexPassword123!';" 2>/dev/null || echo "User may already exist"
psql -U postgres -c "CREATE DATABASE idayam_erp OWNER idayam_user;" 2>/dev/null || echo "Database may already exist"

# Step 2: Run migrations
echo "Running migrations..."
for script in $(ls -1 *.sql | sort); do
  echo "Executing $script..."
  psql -U idayam_user -d idayam_erp -f "$script" || {
    echo -e "${RED}Error executing $script${NC}"
    exit 1
  }
done

echo -e "${GREEN}Database setup completed successfully!${NC}"

# Step 3: Verify
echo "Verifying installation..."
psql -U idayam_user -d idayam_erp -c "SELECT COUNT(*) as tables FROM information_schema.tables WHERE table_schema='public';"
```

**Run the script:**

```bash
chmod +x setup-database.sh
./setup-database.sh
```

---

## Migration Scripts

### 01-schema.sql

```sql
-- ============================================
-- Idayam ERP Database Schema Setup
-- Migration: 01-schema.sql
-- Date: April 22, 2026
-- Description: Create schema and extensions
-- ============================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create schema
CREATE SCHEMA IF NOT EXISTS public;

-- Set schema search path
SET search_path TO public;

-- Create audit table for tracking changes
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL,
    operation VARCHAR(10) NOT NULL, -- INSERT, UPDATE, DELETE
    user_id BIGINT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    old_data JSONB,
    new_data JSONB
);

-- Create index on audit_log
CREATE INDEX idx_audit_log_table_name ON audit_log(table_name);
CREATE INDEX idx_audit_log_changed_at ON audit_log(changed_at);

COMMIT;
```

### 02-core-tables.sql

```sql
-- ============================================
-- Idayam ERP Core Tables
-- Migration: 02-core-tables.sql
-- Date: April 22, 2026
-- Description: Create core system tables
-- ============================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    account_locked BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0
);

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    module VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User-Role mapping
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, role_id)
);

-- Role-Permission mapping
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role_id, permission_id)
);

-- JWT Refresh Tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE
);

-- Create indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

COMMIT;
```

### 03-business-tables.sql

```sql
-- ============================================
-- Idayam ERP Business Tables
-- Migration: 03-business-tables.sql
-- Date: April 22, 2026
-- Description: Create business entity tables
-- ============================================

-- Branches table
CREATE TABLE IF NOT EXISTS branches (
    id BIGSERIAL PRIMARY KEY,
    branch_code VARCHAR(50) UNIQUE NOT NULL,
    branch_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Departments table
CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES branches(id),
    department_code VARCHAR(50) NOT NULL,
    department_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(branch_id, department_code)
);

-- Designations table
CREATE TABLE IF NOT EXISTS designations (
    id BIGSERIAL PRIMARY KEY,
    designation_code VARCHAR(50) UNIQUE NOT NULL,
    designation_name VARCHAR(255) NOT NULL,
    level INT,
    description VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Employees table
CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    employee_code VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    branch_id BIGINT NOT NULL REFERENCES branches(id),
    department_id BIGINT NOT NULL REFERENCES departments(id),
    designation_id BIGINT NOT NULL REFERENCES designations(id),
    salary DECIMAL(12,2),
    join_date DATE NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(50) UNIQUE NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    unit_price DECIMAL(12,2) NOT NULL,
    cost_price DECIMAL(12,2),
    reorder_level INT DEFAULT 10,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Vendors table
CREATE TABLE IF NOT EXISTS vendors (
    id BIGSERIAL PRIMARY KEY,
    vendor_code VARCHAR(50) UNIQUE NOT NULL,
    vendor_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(20),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Stock/Inventory table
CREATE TABLE IF NOT EXISTS stock (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    bin_id VARCHAR(50),
    quantity INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Purchase Orders table
CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    po_number VARCHAR(50) UNIQUE NOT NULL,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    po_date DATE NOT NULL,
    delivery_date DATE,
    total_amount DECIMAL(15,2),
    status VARCHAR(50) DEFAULT 'DRAFT',
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Purchase Order Items
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    po_id BIGINT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    line_total DECIMAL(15,2)
);

-- Sales Orders table
CREATE TABLE IF NOT EXISTS sales_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    order_date DATE NOT NULL,
    delivery_date DATE,
    total_amount DECIMAL(15,2),
    status VARCHAR(50) DEFAULT 'DRAFT',
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sales Order Items
CREATE TABLE IF NOT EXISTS sales_order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES sales_orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    line_total DECIMAL(15,2)
);

-- Attendance table
CREATE TABLE IF NOT EXISTS attendance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    attendance_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'PRESENT', -- PRESENT, ABSENT, LEAVE, HOLIDAY
    check_in_time TIME,
    check_out_time TIME,
    working_hours DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(employee_id, attendance_date)
);

-- Leave table
CREATE TABLE IF NOT EXISTS leaves (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    leave_type VARCHAR(50) NOT NULL, -- CASUAL, SICK, ANNUAL, UNPAID
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    number_of_days INT NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    approved_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for business tables
CREATE INDEX idx_branches_active ON branches(active);
CREATE INDEX idx_departments_branch_id ON departments(branch_id);
CREATE INDEX idx_employees_branch_id ON employees(branch_id);
CREATE INDEX idx_employees_user_id ON employees(user_id);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_stock_product_id ON stock(product_id);
CREATE INDEX idx_po_vendor_id ON purchase_orders(vendor_id);
CREATE INDEX idx_po_status ON purchase_orders(status);
CREATE INDEX idx_so_order_date ON sales_orders(order_date);
CREATE INDEX idx_so_status ON sales_orders(status);
CREATE INDEX idx_attendance_emp_date ON attendance(employee_id, attendance_date);
CREATE INDEX idx_leaves_employee_id ON leaves(employee_id);

COMMIT;
```

### 04-indexes.sql

```sql
-- ============================================
-- Idayam ERP Database Indexes
-- Migration: 04-indexes.sql
-- Date: April 22, 2026
-- Description: Create performance indexes
-- ============================================

-- Performance optimization indexes

-- Fast user lookups
CREATE INDEX idx_users_lowercase_email ON users(LOWER(email));
CREATE INDEX idx_users_username_active ON users(username, active);

-- Fast employee searches
CREATE INDEX idx_employees_code ON employees(employee_code);
CREATE INDEX idx_employees_email ON employees(email);

-- Fast product searches
CREATE INDEX idx_products_code ON products(product_code);
CREATE INDEX idx_products_category ON products(category);

-- Fast transaction lookups
CREATE INDEX idx_po_created_at ON purchase_orders(created_at);
CREATE INDEX idx_so_created_at ON sales_orders(created_at);

-- Fast inventory lookups
CREATE INDEX idx_stock_product_bin ON stock(product_id, bin_id);

-- Fast audit lookups
CREATE INDEX idx_audit_operation_time ON audit_log(operation, changed_at);

COMMIT;
```

### 05-seed-data.sql

```sql
-- ============================================
-- Idayam ERP Seed Data
-- Migration: 05-seed-data.sql
-- Date: April 22, 2026
-- Description: Insert initial reference data
-- ============================================

-- Insert default roles
INSERT INTO roles (name, description, active) VALUES
('ADMIN', 'Administrator with full system access', true),
('MANAGER', 'Manager with department permissions', true),
('USER', 'Regular user with limited access', true),
('VIEWER', 'Read-only access', true)
ON CONFLICT DO NOTHING;

-- Insert default permissions
INSERT INTO permissions (name, module, description, active) VALUES
('VIEW_USERS', 'Users', 'View user list', true),
('CREATE_USERS', 'Users', 'Create new users', true),
('EDIT_USERS', 'Users', 'Edit user details', true),
('DELETE_USERS', 'Users', 'Delete users', true),
('VIEW_REPORTS', 'Reports', 'View reports', true),
('VIEW_INVENTORY', 'Inventory', 'View inventory', true),
('EDIT_INVENTORY', 'Inventory', 'Edit inventory', true),
('VIEW_ORDERS', 'Orders', 'View orders', true),
('CREATE_ORDERS', 'Orders', 'Create orders', true),
('VIEW_PAYROLL', 'Payroll', 'View payroll', true),
('PROCESS_PAYROLL', 'Payroll', 'Process payroll', true)
ON CONFLICT DO NOTHING;

-- Assign permissions to Admin role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.active = true
ON CONFLICT DO NOTHING;

-- Insert default branch
INSERT INTO branches (branch_code, branch_name, city, state, country, active) VALUES
('HQ', 'Head Quarters', 'Chennai', 'Tamil Nadu', 'India', true),
('BR001', 'Branch 1', 'Mumbai', 'Maharashtra', 'India', true),
('BR002', 'Branch 2', 'Delhi', 'Delhi', 'India', true)
ON CONFLICT DO NOTHING;

-- Insert default departments
INSERT INTO departments (branch_id, department_code, department_name, active)
SELECT b.id, 'IT', 'Information Technology', true FROM branches b WHERE b.branch_code = 'HQ'
ON CONFLICT DO NOTHING;

INSERT INTO departments (branch_id, department_code, department_name, active)
SELECT b.id, 'HR', 'Human Resources', true FROM branches b WHERE b.branch_code = 'HQ'
ON CONFLICT DO NOTHING;

INSERT INTO departments (branch_id, department_code, department_name, active)
SELECT b.id, 'OPS', 'Operations', true FROM branches b WHERE b.branch_code = 'HQ'
ON CONFLICT DO NOTHING;

-- Insert default designations
INSERT INTO designations (designation_code, designation_name, level, active) VALUES
('DIR', 'Director', 1, true),
('MGR', 'Manager', 2, true),
('EXE', 'Executive', 3, true),
('EMP', 'Employee', 4, true),
('INT', 'Intern', 5, true)
ON CONFLICT DO NOTHING;

-- Insert admin user (password: admin123 - CHANGE IN PRODUCTION)
INSERT INTO users (username, password, email, first_name, last_name, active) 
VALUES ('admin', 'hashed_password_here', 'admin@idayam.com', 'Admin', 'User', true)
ON CONFLICT DO NOTHING;

-- Create sample products
INSERT INTO products (product_code, product_name, category, unit_price, cost_price, reorder_level, active) VALUES
('PROD001', 'Sugar', 'Raw Materials', 45.00, 40.00, 100, true),
('PROD002', 'Molasses', 'Raw Materials', 35.00, 30.00, 50, true),
('PROD003', 'Jaggery', 'Finished Goods', 80.00, 60.00, 200, true)
ON CONFLICT DO NOTHING;

-- Create sample vendors
INSERT INTO vendors (vendor_code, vendor_name, contact_person, email, phone, city, country, active) VALUES
('VEND001', 'Vendor A', 'Contact Person A', 'vendor.a@example.com', '1234567890', 'Mumbai', 'India', true),
('VEND002', 'Vendor B', 'Contact Person B', 'vendor.b@example.com', '9876543210', 'Delhi', 'India', true)
ON CONFLICT DO NOTHING;

COMMIT;
```

---

## Backup & Recovery

### Automated Backup

**Create Backup Directory:**

```bash
# Linux/Mac
mkdir -p /var/backups/idayam_erp
chmod 700 /var/backups/idayam_erp

# Windows
mkdir C:\Backups\idayam_erp
```

**Create Backup Script (`backup-database.sh`):**

```bash
#!/bin/bash

# Configuration
DB_NAME="idayam_erp"
DB_USER="idayam_user"
BACKUP_DIR="/var/backups/idayam_erp"
BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/idayam_erp_$BACKUP_DATE.sql"

echo "Starting database backup..."

# Full database backup
pg_dump -U $DB_USER -d $DB_NAME > "$BACKUP_FILE"

if [ $? -eq 0 ]; then
  echo "Backup successful: $BACKUP_FILE"
  # Compress backup
  gzip "$BACKUP_FILE"
  echo "Compressed backup: $BACKUP_FILE.gz"
else
  echo "Backup failed!"
  exit 1
fi

# Keep only last 7 days of backups
find $BACKUP_DIR -name "idayam_erp_*.sql.gz" -mtime +7 -delete

echo "Old backups cleaned up"
```

**Schedule Backup (Cron - Linux/Mac):**

```bash
# Edit crontab
crontab -e

# Add this line to backup daily at 2:00 AM
0 2 * * * /path/to/backup-database.sh

# Verify
crontab -l
```

**Schedule Backup (Task Scheduler - Windows):**

```
1. Open Task Scheduler
2. Create Basic Task
3. Set trigger: Daily at 2:00 AM
4. Set action: Run `backup-database.bat`
```

### Database Recovery

```bash
# List available backups
ls -lh /var/backups/idayam_erp/

# Restore from backup
psql -U idayam_user -d idayam_erp < backup_file.sql

# Or if compressed
gunzip -c backup_file.sql.gz | psql -U idayam_user -d idayam_erp

# Verify restoration
psql -U idayam_user -d idayam_erp -c "SELECT COUNT(*) as tables FROM information_schema.tables WHERE table_schema='public';"
```

---

## Troubleshooting

### Common Issues & Solutions

**Issue: Connection refused**

```bash
# Check if PostgreSQL is running
# Linux
sudo systemctl status postgresql

# Mac
brew services list

# Windows
# Check Services > PostgreSQL

# Start if not running
# Linux
sudo systemctl start postgresql

# Mac
brew services start postgresql
```

**Issue: Authentication failed**

```bash
# Verify pg_hba.conf configuration
# Location: /etc/postgresql/12/main/pg_hba.conf

# Should contain:
# host    all             all             127.0.0.1/32            md5
# host    all             all             ::1/128                 md5

# After changes, restart PostgreSQL
```

**Issue: Port already in use**

```bash
# Find process using port 5432
# Linux/Mac
lsof -i :5432

# Windows
netstat -ano | findstr :5432

# Change PostgreSQL port in postgresql.conf
port = 5433  # Use different port
```

**Issue: Out of disk space**

```sql
-- Check database size
SELECT datname, pg_size_pretty(pg_database_size(datname)) 
FROM pg_database 
WHERE datname = 'idayam_erp';

-- Check table sizes
SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) 
FROM pg_tables 
WHERE schemaname = 'public' 
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

**Issue: Slow queries**

```sql
-- Enable query logging
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_min_duration_statement = 1000; -- Log queries > 1 second

-- Reload configuration
SELECT pg_reload_conf();

-- Check slow query log
SELECT query, mean_exec_time, max_exec_time 
FROM pg_stat_statements 
WHERE mean_exec_time > 1000 
ORDER BY mean_exec_time DESC;
```

---

## Security Best Practices

### Password Policy

```sql
-- Enforce strong passwords for users
-- Create function to validate passwords
CREATE OR REPLACE FUNCTION validate_password(password VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
  IF LENGTH(password) < 12 THEN
    RETURN FALSE;
  END IF;
  IF password !~ '[A-Z]' THEN
    RETURN FALSE;
  END IF;
  IF password !~ '[a-z]' THEN
    RETURN FALSE;
  END IF;
  IF password !~ '[0-9]' THEN
    RETURN FALSE;
  END IF;
  IF password !~ '[!@#$%^&*]' THEN
    RETURN FALSE;
  END IF;
  RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
```

### SSL/TLS Configuration

```bash
# Generate self-signed certificate (development only)
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/postgresql/server.key \
  -out /etc/postgresql/server.crt

# Set permissions
chmod 600 /etc/postgresql/server.key
chmod 600 /etc/postgresql/server.crt
```

**PostgreSQL Configuration:**

```ini
# postgresql.conf
ssl = on
ssl_cert_file = '/etc/postgresql/server.crt'
ssl_key_file = '/etc/postgresql/server.key'

# pg_hba.conf - require SSL
hostssl    idayam_erp    idayam_user    0.0.0.0/0    md5
```

### User Role Separation

```sql
-- Create read-only role
CREATE ROLE idayam_readonly;
GRANT USAGE ON SCHEMA public TO idayam_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO idayam_readonly;

-- Create read-write role
CREATE ROLE idayam_readwrite;
GRANT USAGE ON SCHEMA public TO idayam_readwrite;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO idayam_readwrite;

-- Assign roles to users as needed
GRANT idayam_readonly TO report_user;
GRANT idayam_readwrite TO application_user;
```

### Monitoring & Logging

```sql
-- Enable logging
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_duration = ON;
ALTER SYSTEM SET log_connections = ON;
ALTER SYSTEM SET log_disconnections = ON;
ALTER SYSTEM SET log_min_duration_statement = 1000;

-- Check logs
SELECT * FROM pg_read_file('/var/log/postgresql/postgresql.log', 0, 100000);
```

---

## Database Connection String

### For Spring Boot Application

**application.properties:**

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/idayam_erp
spring.datasource.username=idayam_user
spring.datasource.password=ComplexPassword123!
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate Configuration
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

**application-prod.properties:**

```properties
# Production settings with SSL
spring.datasource.url=jdbc:postgresql://db-server.example.com:5432/idayam_erp?sslmode=require
spring.datasource.username=idayam_user
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=50
```

---

## Quick Reference

### Essential psql Commands

```bash
# Connect to database
psql -U idayam_user -d idayam_erp

# List databases
\l

# List tables
\dt

# List users
\du

# Get table schema
\d table_name

# Run SQL file
\i /path/to/file.sql

# Exit psql
\q
```

### Database Statistics

```sql
-- Total tables
SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';

-- Database size
SELECT pg_size_pretty(pg_database_size('idayam_erp'));

-- User count
SELECT COUNT(*) FROM users;

-- Role count
SELECT COUNT(*) FROM roles;
```

---

**Document Version:** 1.0  
**Last Updated:** April 22, 2026  
**Database Version:** PostgreSQL 12+  
**Status:** ✅ PRODUCTION-READY
