# Idayam ERP - SQL Migration Scripts Guide

## Overview

This directory contains all SQL migration scripts and utilities for setting up the Idayam ERP PostgreSQL database. The scripts are organized in execution order and include automated setup tools.

## Directory Structure

```
sql_migrations/
├── 01-schema.sql           # Database schema and extensions setup
├── 02-core-tables.sql      # Core system tables (users, roles, permissions)
├── 03-business-tables.sql  # Business entity tables (employees, products, orders, etc.)
├── 04-indexes.sql          # Performance optimization indexes
├── 05-seed-data.sql        # Initial reference and sample data
├── run-migrations.sh        # Linux/macOS migration runner (bash)
├── run-migrations.bat       # Windows migration runner (batch)
├── quick-setup.sh           # Interactive one-command setup for Linux/macOS
└── README.md               # This file
```

## Quick Start

### Option 1: Fully Automated Setup (Recommended)

#### On Linux/macOS:
```bash
cd sql_migrations
chmod +x quick-setup.sh
./quick-setup.sh
```

This script will:
1. Check PostgreSQL installation
2. Prompt for database configuration
3. Create database and user
4. Run all migrations automatically
5. Display connection details

#### On Windows:
If you have PostgreSQL installed with psql accessible, you can adapt the quick-setup.sh to batch commands, or use the manual approach below.

### Option 2: Step-by-Step Manual Setup

#### Step 1: Create Database and User
```bash
psql -U postgres -d postgres

# In psql prompt:
CREATE DATABASE idayam_erp ENCODING 'UTF8' LC_COLLATE 'en_US.UTF-8' LC_CTYPE 'en_US.UTF-8';
CREATE USER idayam_user WITH PASSWORD 'ComplexPassword123!';
GRANT ALL PRIVILEGES ON DATABASE idayam_erp TO idayam_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO idayam_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO idayam_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO idayam_user;
\q
```

#### Step 2: Run Migrations
```bash
# Linux/macOS
chmod +x run-migrations.sh
./run-migrations.sh

# Windows (from PowerShell or Command Prompt)
run-migrations.bat
```

Or execute migrations manually:
```bash
psql -U idayam_user -d idayam_erp -f 01-schema.sql
psql -U idayam_user -d idayam_erp -f 02-core-tables.sql
psql -U idayam_user -d idayam_erp -f 03-business-tables.sql
psql -U idayam_user -d idayam_erp -f 04-indexes.sql
psql -U idayam_user -d idayam_erp -f 05-seed-data.sql
```

## Migration Scripts Description

### 01-schema.sql
**Purpose**: Initialize PostgreSQL extensions and base schema

**Creates**:
- UUID extension (`uuid-ossp`)
- Cryptography extension (`pgcrypto`)
- Public schema
- Audit log table for tracking changes

**Size**: ~50 lines
**Execution Time**: < 1 second
**Dependencies**: None (runs as superuser)

### 02-core-tables.sql
**Purpose**: Create core authentication and authorization tables

**Creates**:
- `users` - User accounts with authentication info
- `roles` - System roles (ADMIN, MANAGER, USER, VIEWER)
- `permissions` - Individual permission definitions
- `user_roles` - User to role mappings
- `role_permissions` - Role to permission mappings
- `refresh_tokens` - JWT refresh tokens

**Indexes**: 9 indexes for fast lookups
**Size**: ~150 lines
**Execution Time**: 1-2 seconds
**Dependencies**: 01-schema.sql

### 03-business-tables.sql
**Purpose**: Create business entity tables for core operations

**Creates**:
- **Master Data**: branches, departments, designations
- **HR**: employees, attendance, leaves
- **Inventory**: products, vendors, stock
- **Orders**: purchase_orders, sales_orders with line items

**Total Tables**: 12 core tables
**Indexes**: 20+ indexes for performance
**Size**: ~350 lines
**Execution Time**: 2-3 seconds
**Dependencies**: 02-core-tables.sql

**Important Relationships**:
- Employees → Users (optional, for system login)
- Purchase Orders → Vendors
- Sales Orders → Customers (implicit)
- Stock → Products
- Attendance → Employees

### 04-indexes.sql
**Purpose**: Create additional performance optimization indexes

**Indexes Created**: 13 composite and specialized indexes

**Types**:
- Unique constraints (username, email, codes)
- Lookup indexes (active status, dates)
- Foreign key optimization
- Full-text search preparation
- Composite indexes for common queries

**Execution Time**: 1-2 seconds
**Dependencies**: 03-business-tables.sql

**Usage Impact**:
- Improves query performance by 5-10x for common queries
- Increases write time by ~5-10%
- Adds ~2-5% to storage

### 05-seed-data.sql
**Purpose**: Insert initial reference data and sample records

**Data Inserted**:
- **Security**: 4 default roles, 30+ permissions
- **Organization**: 5 branches, 5 departments, 7 designations
- **Users**: 3 sample users (admin, manager, user) with role assignments
- **Products**: 5 sample products
- **Vendors**: 4 sample vendors
- **Inventory**: 3 stock records

**Size**: ~500 lines
**Execution Time**: 2-3 seconds
**Dependencies**: 02-core-tables.sql, 03-business-tables.sql

**Important Notes**:
- Passwords are default (MUST be changed in production)
- Contact information is sample data (MUST be updated)
- On CONFLICT clauses prevent duplicate key errors if re-run

## Database Configuration

### Connection Details

**Database Name**: `idayam_erp`
**Default Port**: `5432`
**Superuser**: `postgres` (created during PostgreSQL installation)
**Application User**: `idayam_user`
**Default Password**: `ComplexPassword123!` (CHANGE IN PRODUCTION)

### Spring Boot Configuration

Add to `application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/idayam_erp
spring.datasource.username=idayam_user
spring.datasource.password=ComplexPassword123!
spring.datasource.driver-class-name=org.postgresql.Driver

# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# JPA Configuration
spring.jpa.database=POSTGRESQL
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQL10Dialect
```

### Production Configuration

For production environments:
```properties
# Enable SSL
spring.datasource.url=jdbc:postgresql://db-server.example.com:5432/idayam_erp?sslmode=require
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.connection-timeout=10000
spring.datasource.hikari.idle-timeout=900000
```

## Security Best Practices

### Password Policy
- Minimum 12 characters
- Mix of uppercase, lowercase, numbers, and special characters
- Change default passwords immediately after setup
- Use strong, unique passwords for each environment

### User Permissions
```sql
-- View current permissions
SELECT u.username, r.name as role, p.name as permission
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
LEFT JOIN role_permissions rp ON r.id = rp.role_id
LEFT JOIN permissions p ON rp.permission_id = p.id
ORDER BY u.username, r.name, p.name;
```

### Database Backup

#### Automated Backup (Linux/macOS)
```bash
#!/bin/bash
BACKUP_DIR="/backups/idayam_erp"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

mkdir -p $BACKUP_DIR

pg_dump -U idayam_user -h localhost idayam_erp > \
  "$BACKUP_DIR/idayam_erp_backup_$TIMESTAMP.sql"

# Keep only last 30 days
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete

echo "Backup completed: $BACKUP_DIR/idayam_erp_backup_$TIMESTAMP.sql"
```

#### Manual Backup
```bash
pg_dump -U idayam_user -d idayam_erp -f backup.sql
```

#### Restore Backup
```bash
psql -U idayam_user -d idayam_erp -f backup.sql
```

## Troubleshooting

### Connection Issues

**Problem**: "psql: FATAL:  role "postgres" does not exist"
```bash
# Solution: Check PostgreSQL installation
sudo -u postgres psql -d postgres
```

**Problem**: "ERROR: database "idayam_erp" already exists"
```sql
-- Solution: Drop existing database
DROP DATABASE IF EXISTS idayam_erp;
-- Then re-run migrations
```

**Problem**: "permission denied for database"
```sql
-- Solution: Grant privileges to user
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO idayam_user;
GRANT ALL PRIVILEGES ON DATABASE idayam_erp TO idayam_user;
```

### Migration Failures

**Problem**: "ERROR: relation already exists"
- Database was partially initialized
- Re-run migrations with fresh database
- Or modify migration scripts to use `IF NOT EXISTS` clauses

**Problem**: "FK constraint violation"
- Parent table records missing
- Check seed-data.sql is executed after all table creation scripts
- Verify foreign key relationships in schema

### Data Issues

**Problem**: "No default data in tables"
```sql
-- Verify seed data was inserted
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM roles;
SELECT COUNT(*) FROM permissions;
```

**Problem**: "User login fails after setup"
```sql
-- Check user credentials
SELECT username, email, active FROM users WHERE username = 'admin';

-- Reset password (development only)
UPDATE users 
SET password = crypt('NewPassword123!', gen_salt('bf', 4))
WHERE username = 'admin';
```

## Verification Checklist

After running migrations, verify:

```sql
-- 1. Check table creation
SELECT COUNT(*) as table_count FROM information_schema.tables 
WHERE table_schema = 'public' AND table_type = 'BASE TABLE';
-- Expected: ~20+ tables

-- 2. Check core tables
\dt users roles permissions branches employees products

-- 3. Verify indexes
SELECT COUNT(*) as index_count FROM pg_indexes 
WHERE schemaname = 'public';
-- Expected: ~40+ indexes

-- 4. Check seed data
SELECT COUNT(*) FROM users;          -- Expected: 3+
SELECT COUNT(*) FROM roles;          -- Expected: 4
SELECT COUNT(*) FROM permissions;    -- Expected: 30+
SELECT COUNT(*) FROM branches;       -- Expected: 5
SELECT COUNT(*) FROM departments;    -- Expected: 5+
SELECT COUNT(*) FROM products;       -- Expected: 5
SELECT COUNT(*) FROM vendors;        -- Expected: 4

-- 5. Verify foreign keys
SELECT constraint_name FROM information_schema.table_constraints 
WHERE constraint_type = 'FOREIGN KEY' 
AND table_schema = 'public';
-- Expected: 15+ foreign keys
```

## Advanced Operations

### Reorder Indexes by Performance
```sql
-- Reindex all indexes on a table
REINDEX TABLE users;
REINDEX TABLE employees;
REINDEX TABLE products;

-- Full database reindex
REINDEX DATABASE idayam_erp;
```

### Analyze Query Performance
```sql
-- Enable query analysis
EXPLAIN ANALYZE
SELECT e.*, d.department_name, b.branch_name
FROM employees e
JOIN departments d ON e.department_id = d.id
JOIN branches b ON e.branch_id = b.id
WHERE e.active = true;
```

### Manage Audit Logs
```sql
-- View audit log activity
SELECT * FROM audit_log ORDER BY changed_at DESC LIMIT 100;

-- Archive old audit logs
DELETE FROM audit_log WHERE changed_at < NOW() - INTERVAL '90 days';

-- Get audit stats by table
SELECT table_name, operation, COUNT(*) as count
FROM audit_log
GROUP BY table_name, operation
ORDER BY count DESC;
```

## Performance Tuning

### Check Table Sizes
```sql
SELECT schemaname, tablename, 
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Analyze All Tables
```sql
ANALYZE;
```

### Vacuum Database
```sql
VACUUM FULL;
```

## Support and Documentation

For detailed information:
- PostgreSQL Documentation: https://www.postgresql.org/docs/
- Spring Boot Database Configuration: https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.data
- Database Design Reference: See ../DATABASE_SETUP_GUIDE.md

## Migration Rollback

To rollback to a previous state:

```bash
# Full database reset (WARNING: Deletes all data)
psql -U postgres -d postgres << EOF
DROP DATABASE IF EXISTS idayam_erp;
DROP USER IF EXISTS idayam_user;
EOF

# Then re-run setup:
./quick-setup.sh
```

## Next Steps

1. **Verify Connection**: Test from Spring Boot application
2. **Enable Backups**: Set up automated daily backups
3. **Monitor Performance**: Use monitoring tools for query performance
4. **Plan Scaling**: Prepare for horizontal scaling if needed
5. **Document Changes**: Track any schema modifications

---

**Last Updated**: April 22, 2026
**Version**: 1.0
**PostgreSQL Compatibility**: 12.0+
