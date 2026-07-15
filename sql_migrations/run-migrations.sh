#!/bin/bash
# ============================================
# Idayam ERP Database Migration Runner
# Script: run-migrations.sh
# Date: April 22, 2026
# Description: Automated database migration execution
# ============================================

set -e  # Exit on error

# Configuration
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-idayam_erp}"
DB_USER="${DB_USER:-postgres}"
MIGRATIONS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
print_error() {
    echo -e "${RED}ERROR: $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Check if psql is available
if ! command -v psql &> /dev/null; then
    print_error "psql not found. Please install PostgreSQL client tools."
    exit 1
fi

# Confirm before running
echo "=========================================="
echo "Idayam ERP Database Migration Runner"
echo "=========================================="
print_info "Target Database: $DB_NAME"
print_info "Host: $DB_HOST"
print_info "Port: $DB_PORT"
print_info "User: $DB_USER"
print_info "Migrations Directory: $MIGRATIONS_DIR"
echo "=========================================="
echo ""

read -p "Do you want to proceed with migrations? (yes/no): " confirm
if [[ "$confirm" != "yes" ]]; then
    print_error "Migration cancelled by user."
    exit 0
fi

echo ""
print_info "Starting database migrations..."
echo ""

# Array of migration files in order
migrations=(
    "01-schema.sql"
    "02-core-tables.sql"
    "03-business-tables.sql"
    "04-indexes.sql"
    "05-seed-data.sql"
)

# Execute each migration
for migration in "${migrations[@]}"; do
    migration_file="$MIGRATIONS_DIR/$migration"
    
    if [[ ! -f "$migration_file" ]]; then
        print_error "Migration file not found: $migration_file"
        exit 1
    fi
    
    print_info "Executing: $migration"
    
    if PGPASSWORD=$DB_PASSWORD psql \
        -h "$DB_HOST" \
        -p "$DB_PORT" \
        -U "$DB_USER" \
        -d "$DB_NAME" \
        -f "$migration_file" 2>&1; then
        print_success "Completed: $migration"
    else
        print_error "Failed to execute: $migration"
        exit 1
    fi
    
    echo ""
done

# Verify installation
print_info "Verifying installation..."
PGPASSWORD=$DB_PASSWORD psql \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    -c "SELECT 
        'users' as table_name, COUNT(*) as row_count FROM users
    UNION ALL
    SELECT 'roles', COUNT(*) FROM roles
    UNION ALL
    SELECT 'permissions', COUNT(*) FROM permissions
    UNION ALL
    SELECT 'branches', COUNT(*) FROM branches;"

echo ""
print_success "All migrations completed successfully!"
print_success "Database is ready for application use."
echo ""
print_info "Next steps:"
echo "  1. Update application.properties with database connection details"
echo "  2. Test connection from Spring Boot application"
echo "  3. Verify all tables and data using database UI tool"
echo "  4. Create backups before going to production"
