#!/bin/bash
# ============================================
# Idayam ERP - Quick PostgreSQL Database Setup
# Script: quick-setup.sh
# Date: April 22, 2026
# Description: One-command database initialization
# ============================================

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}"
echo "=================================================="
echo "Idayam ERP - Quick PostgreSQL Database Setup"
echo "=================================================="
echo -e "${NC}"

# Step 1: Check PostgreSQL installation
echo -e "${YELLOW}Step 1: Checking PostgreSQL installation...${NC}"
if ! command -v psql &> /dev/null; then
    echo -e "${RED}✗ PostgreSQL client not found${NC}"
    echo "Please install PostgreSQL from: https://www.postgresql.org/download/"
    exit 1
fi
echo -e "${GREEN}✓ PostgreSQL client found${NC}"

# Step 2: Get configuration
echo -e "${YELLOW}\nStep 2: Database Configuration${NC}"
read -p "PostgreSQL Host (default: localhost): " DB_HOST
DB_HOST=${DB_HOST:-localhost}
read -p "PostgreSQL Port (default: 5432): " DB_PORT
DB_PORT=${DB_PORT:-5432}
read -p "Database Name (default: idayam_erp): " DB_NAME
DB_NAME=${DB_NAME:-idayam_erp}
read -p "Database Owner (default: idayam_user): " DB_OWNER
DB_OWNER=${DB_OWNER:-idayam_user}
read -sp "PostgreSQL Admin Password: " ADMIN_PASSWORD
echo

# Step 3: Test connection
echo -e "${YELLOW}\nStep 3: Testing connection to PostgreSQL...${NC}"
if PGPASSWORD=$ADMIN_PASSWORD psql -h $DB_HOST -p $DB_PORT -U postgres -d postgres -c "SELECT 1;" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Connected to PostgreSQL${NC}"
else
    echo -e "${RED}✗ Failed to connect to PostgreSQL${NC}"
    echo "Please check:"
    echo "  1. PostgreSQL service is running"
    echo "  2. Correct host and port"
    echo "  3. Valid admin password"
    exit 1
fi

# Step 4: Create database
echo -e "${YELLOW}\nStep 4: Creating database: $DB_NAME${NC}"
PGPASSWORD=$ADMIN_PASSWORD psql -h $DB_HOST -p $DB_PORT -U postgres -d postgres << EOF
CREATE DATABASE $DB_NAME ENCODING 'UTF8' LC_COLLATE 'en_US.UTF-8' LC_CTYPE 'en_US.UTF-8';
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Database created${NC}"
else
    echo -e "${YELLOW}ℹ Database might already exist${NC}"
fi

# Step 5: Create user
echo -e "${YELLOW}\nStep 5: Creating database user: $DB_OWNER${NC}"
read -sp "Set password for $DB_OWNER: " DB_PASSWORD
echo

PGPASSWORD=$ADMIN_PASSWORD psql -h $DB_HOST -p $DB_PORT -U postgres -d postgres << EOF
CREATE ROLE $DB_OWNER WITH LOGIN PASSWORD '$DB_PASSWORD' CREATEDB CREATEROLE;
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ User created${NC}"
else
    echo -e "${YELLOW}ℹ User might already exist${NC}"
fi

# Step 6: Grant privileges
echo -e "${YELLOW}\nStep 6: Setting up privileges...${NC}"
PGPASSWORD=$ADMIN_PASSWORD psql -h $DB_HOST -p $DB_PORT -U postgres -d postgres << EOF
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO $DB_OWNER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO $DB_OWNER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO $DB_OWNER;
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_OWNER;
GRANT ALL PRIVILEGES ON SCHEMA public TO $DB_OWNER;
EOF

echo -e "${GREEN}✓ Privileges configured${NC}"

# Step 7: Run migrations
echo -e "${YELLOW}\nStep 7: Running database migrations...${NC}"
MIGRATIONS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ -d "$MIGRATIONS_DIR" ]; then
    export PGPASSWORD=$DB_PASSWORD
    
    for migration in 01-schema.sql 02-core-tables.sql 03-business-tables.sql 04-indexes.sql 05-seed-data.sql; do
        migration_file="$MIGRATIONS_DIR/$migration"
        
        if [ -f "$migration_file" ]; then
            echo -n "  - Executing $migration... "
            if psql -h $DB_HOST -p $DB_PORT -U $DB_OWNER -d $DB_NAME -f "$migration_file" > /dev/null 2>&1; then
                echo -e "${GREEN}✓${NC}"
            else
                echo -e "${RED}✗${NC}"
                exit 1
            fi
        fi
    done
else
    echo -e "${RED}✗ Migrations directory not found${NC}"
    exit 1
fi

# Step 8: Verify setup
echo -e "${YELLOW}\nStep 8: Verifying setup...${NC}"
export PGPASSWORD=$DB_PASSWORD
TABLE_COUNT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_OWNER -d $DB_NAME -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';")
echo -e "${GREEN}✓ Database setup complete!${NC}"
echo -e "  Tables created: $TABLE_COUNT"

# Step 9: Display configuration
echo -e "${BLUE}"
echo "=================================================="
echo "Setup Complete - Save these connection details:"
echo "=================================================="
echo -e "${NC}"
echo "Database Name:     $DB_NAME"
echo "Host:              $DB_HOST"
echo "Port:              $DB_PORT"
echo "Username:          $DB_OWNER"
echo "Password:          ••••••••"
echo ""
echo "JDBC Connection String:"
echo "jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
echo ""
echo "Application Properties:"
echo "spring.datasource.url=jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
echo "spring.datasource.username=$DB_OWNER"
echo "spring.datasource.password=$DB_PASSWORD"
echo ""
echo -e "${YELLOW}Important:${NC}"
echo "  1. Store the password securely"
echo "  2. Update application.properties with above details"
echo "  3. Change admin password in production"
echo "  4. Create regular database backups"
echo "  5. Enable SSL for production connections"
echo ""
