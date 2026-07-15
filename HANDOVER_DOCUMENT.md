# Idayam ERP Backend - Handover Document

## Document Control

| Item | Details |
|------|---------|
| Document Type | Handover/Maintenance Documentation |
| Version | 1.0 |
| Created Date | April 2026 |
| Last Updated | April 2026 |
| Audience | Development, Operations, Support Teams |
| Status | Active |

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [System Architecture](#system-architecture)
3. [Technology Stack](#technology-stack)
4. [Deployment Architecture](#deployment-architecture)
5. [Installation & Deployment](#installation--deployment)
6. [Database Management](#database-management)
7. [Security & Authentication](#security--authentication)
8. [Monitoring & Logging](#monitoring--logging)
9. [Backup & Recovery](#backup--recovery)
10. [Troubleshooting Guide](#troubleshooting-guide)
11. [Maintenance Procedures](#maintenance-procedures)
12. [Performance Tuning](#performance-tuning)
13. [Disaster Recovery](#disaster-recovery)
14. [Contact & Escalation](#contact--escalation)

---

## Executive Summary

### System Overview
The Idayam ERP Backend is a comprehensive Spring Boot-based enterprise resource planning system designed for manufacturing and distribution operations. The system provides:

- **106 REST Controllers** managing various business processes
- **Multi-layer architecture** ensuring separation of concerns
- **JWT-based authentication** for secure API access
- **PostgreSQL database integration** for persistent data storage
- **Comprehensive API documentation** via Swagger 2.0

### Key Metrics
- **Controllers:** 106+
- **API Endpoints:** 400+ (estimated)
- **Database Tables:** 100+ (entity models)
- **Annual Uptime SLA:** 99.5% target
- **Max Concurrent Users:** Configurable (typically 100+)

### User Base
- **Financial Users:** Accounting, Finance teams
- **Operations Users:** Production, warehouse, logistics
- **Admin Users:** System administrators (3-5)
- **Managers:** Department/section heads (20-30)
- **Field Users:** Attendants, inspectors (50+)

---

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Client Layer                        │
│           (Web/Mobile/Desktop Applications)             │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP/HTTPS
                         ↓
┌─────────────────────────────────────────────────────────┐
│                   API Gateway (Optional)                │
│              (Reverse Proxy, Load Balancer)             │
└────────────────────────┬────────────────────────────────┘
                         │
                         ↓
┌─────────────────────────────────────────────────────────┐
│              Spring Boot Application Layer              │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │           Controller Layer (106 Controllers)      │  │
│  │  • Request handling & routing                     │  │
│  │  • Authentication validation                      │  │
│  │  • Response serialization                         │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │            Service Layer (Business Logic)        │  │
│  │  • Core business logic implementation            │  │
│  │  • Data validation & processing                  │  │
│  │  • Integration logic                             │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │          DAO Layer (JPA Repositories)            │  │
│  │  • Database query operations                     │  │
│  │  • Entity management                             │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │             Security & Filters                   │  │
│  │  • JWT Token validation                         │  │
│  │  • CORS handling                                 │  │
│  │  • Request/Response logging                      │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
└────────────────────────┬────────────────────────────────┘
                         │
         ┌───────────────┴───────────────┐
         ↓                               ↓
┌─────────────────────┐       ┌──────────────────────┐
│   PostgreSQL      │       │  File Storage        │
│                     │       │  (Local/Network)     │
│  • Data persistence │       │  • PDF reports       │
│  • Transactions     │       │  • File uploads      │
│  • Backup/restore   │       │  • Attachments       │
└─────────────────────┘       └──────────────────────┘
```

### Microservices Readiness
The current monolithic architecture can be refactored into microservices:
- **Auth Service:** User management
- **Inventory Service:** Stock and material management
- **Finance Service:** Accounting and billing
- **HR Service:** Employee and payroll management
- **Production Service:** Manufacturing operations

---

## Technology Stack

### Core Technologies

| Layer | Technology | Version |
|-------|-----------|---------|
| **Framework** | Spring Boot | 2.6+ |
| **Security** | Spring Security + JWT | Latest |
| **ORM** | Hibernate/JPA | 5.6+ |
| **API Docs** | Swagger 2.0/Springfox | 2.9+ |
| **Database** | PostgreSQL | 12+ |
| **Build Tool** | Maven | 3.6+ |
| **Language** | Java | 11+ |

### Dependencies Summary

```xml
<!-- Core Spring Boot -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security

<!-- Database -->
postgresql-jdbc
org.hibernate

<!-- API Documentation -->
springfox-swagger2
springfox-swagger-ui

<!-- JWT -->
jjwt (JSON Web Token library)

<!-- File Upload -->
commons-fileupload
commons-io

<!-- Logging -->
logback
slf4j

<!-- Testing -->
spring-boot-starter-test
junit
mockito
```

---

## Deployment Architecture

### Development Environment
```
Developer Workstation
    ├── JDK 11+
    ├── Maven 3.6+
    ├── PostgreSQL (local or network)
    ├── IDE (IntelliJ/Eclipse/VS Code)
    └── Git client
```

### Staging Environment
```
Application Server
├── JDK 11+
├── PostgreSQL 12+
├── Application Logs Directory
├── File Upload Directory
├── Backup Directory
└── Tomcat/Embedded Server (port 8080)

Network
├── Load Balancer (optional)
├── Reverse Proxy (Nginx/Apache)
└── Firewall rules for 8080, 1433
```

### Production Environment
```
┌─────────────────────────────────────────┐
│          Load Balancer / Proxy           │ :443 (HTTPS)
└────────────┬────────────────────────────┘
             │
      ┌──────┴──────┐
      │             │
      ↓             ↓
┌──────────┐   ┌──────────┐
│ App Srv1 │   │ App Srv2 │  (Multiple instances for HA)
│ :8080    │   │ :8080    │
└────┬─────┘   └────┬─────┘
     └────┬─────────┘
          │
          ↓
    ┌───────────────┐
    │  DB Cluster   │     (PostgreSQL with replication)
    │  (Primary)    │
    └───────┬───────┘
            │
            ↓
    ┌───────────────┐
    │  DB Cluster   │     (PostgreSQL secondary)
    │  (Secondary)  │
    └───────────────┘

Monitoring:
├── Application Health Checks
├── Database Monitoring
├── Server Resource Monitoring
├── Log Aggregation
└── Alert System
```

### Required Infrastructure

| Component | Specification | Notes |
|-----------|---|---|
| **Web Server** | 2+ cores, 4GB+ RAM | Scales with users |
| **Database Server** | 4+ cores, 8GB+ RAM | Dedicated PostgreSQL |
| **Storage** | 100GB+ | Database files + logs + backups |
| **Network** | Gigabit LAN | Minimum 100 Mbps recommended |
| **Backup Storage** | External NAS/Cloud | Daily automated backups |
| **SSL Certificate** | Valid HTTPS cert | For production |

---

## Installation & Deployment

### Pre-Deployment Checklist

- [ ] Database server is operational
- [ ] Database created with proper permissions
- [ ] PostgreSQL JDBC driver available
- [ ] Java 11+ installed on target server
- [ ] Firewall rules configured (port 8080, 1433)
- [ ] Sufficient disk space (100GB recommended)
- [ ] Network connectivity verified
- [ ] SSL certificate prepared (production)
- [ ] File upload directory created and permissions set
- [ ] Backup directory configured
- [ ] Monitoring tools installed

### Step-by-Step Deployment

#### Step 1: Prepare Server
```bash
# Create application directory
mkdir -p /opt/earmsbackend
cd /opt/earmsbackend

# Create subdirectories
mkdir -p logs uploads backups config

# Set permissions
chmod 755 /opt/earmsbackend
chmod 777 /opt/earmsbackend/logs
chmod 777 /opt/earmsbackend/uploads
chmod 777 /opt/earmsbackend/backups
```

#### Step 2: Database Setup
```sql
-- On PostgreSQL
CREATE DATABASE IDAYAM_ERP
GO

USE IDAYAM_ERP
GO

-- Create service account
CREATE LOGIN idayam_user WITH PASSWORD = 'StrongPassword123!';
CREATE USER idayam_user FOR LOGIN idayam_user;

-- Grant permissions
EXEC sp_addrolemember 'db_owner', 'idayam_user';
GO
```

#### Step 3: Deploy Application
```bash
# Copy JAR file
scp target/earmsbackend-1.0.0.jar user@app-server:/opt/earmsbackend/

# Create systemd service file
sudo nano /etc/systemd/system/earmsbackend.service
```

#### Step 4: Create Systemd Service

Create `/etc/systemd/system/earmsbackend.service`:

```ini
[Unit]
Description=Idayam ERP Backend Service
After=network.target

[Service]
Type=simple
User=earmsbackend
WorkingDirectory=/opt/earmsbackend
ExecStart=/usr/bin/java -Xmx4g -Xms2g \
    -Dspring.config.location=file:/opt/earmsbackend/config/application.properties \
    -jar /opt/earmsbackend/earmsbackend-1.0.0.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

#### Step 5: Start Service
```bash
# Enable service
sudo systemctl enable earmsbackend

# Start service
sudo systemctl start earmsbackend

# Check status
sudo systemctl status earmsbackend

# View logs
sudo journalctl -u earmsbackend -f
```

#### Step 6: Verify Deployment
```bash
# Check application is running
curl http://localhost:8080/swagger-ui.html

# Check health endpoint
curl http://localhost:8080/actuator/health
```

### Rollback Procedure
```bash
# Stop current version
sudo systemctl stop earmsbackend

# Restore previous JAR
cp /opt/earmsbackend/backups/earmsbackend-previous.jar /opt/earmsbackend/

# Restore database (if needed)
# Use backup files from backups directory

# Start service
sudo systemctl start earmsbackend
```

---

## Database Management

### Database Schema Overview

**Core Tables:**
- `users` - System users
- `roles` - Role definitions
- `permissions` - Module permissions
- `refresh_tokens` - JWT refresh tokens

**Business Tables:**
- `branches` - Organization branches
- `departments` - Departments
- `designations` - Job positions
- `employees` - Employee master
- `(100+ other business entity tables)`

### Backup Strategy

#### Daily Backup
```sql
-- Backup script (run daily at 2 AM)
BACKUP DATABASE [IDAYAM_ERP]
TO DISK = N'/backups/IDAYAM_ERP_Daily_20260421.bak'
WITH NOFORMAT, NOINIT, NAME = N'IDAYAM_ERP-Full Database Backup',
     SKIP, NOREWIND, NOUNLOAD, STATS = 10
GO
```

#### Transaction Log Backup
```sql
-- Run every 15 minutes for point-in-time recovery
BACKUP LOG [IDAYAM_ERP]
TO DISK = N'/backups/IDAYAM_ERP_Log_20260421_1400.trn'
WITH NOFORMAT, NOINIT, NAME = N'IDAYAM_ERP-Transaction Log Backup',
     NOREWIND, NOUNLOAD, STATS = 10
GO
```

#### Retention Policy
- Daily full backups: Keep 7 days
- Transaction logs: Keep 14 days
- Weekly full backups: Keep 4 weeks
- Monthly full backups: Keep 1 year

### Recovery Procedures

#### Full Database Recovery
```sql
-- Restore from full backup
RESTORE DATABASE [IDAYAM_ERP]
FROM DISK = N'/backups/IDAYAM_ERP_Daily_20260420.bak'
WITH FILE = 1, NORECOVERY

-- Restore transaction logs
RESTORE LOG [IDAYAM_ERP]
FROM DISK = N'/backups/IDAYAM_ERP_Log_20260421_1400.trn'
WITH FILE = 1, RECOVERY

-- Verify
DBCC CHECKDB (IDAYAM_ERP)
GO
```

---

## Security & Authentication

### Authentication Flow

```
Client                          Server
  │                               │
  ├──────── Login Request ────────→
  │   (username, password)        │
  │                               │
  │   ← ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─←
  │   JWT Access Token            │
  │   + Refresh Token             │
  │                               │
  ├─ Request with JWT Token ─────→
  │   (Authorization: Bearer XXX) │
  │                               │
  │   ← ─ ─ Response ─ ─ ─ ─ ─ ←
  │         (with data)           │
  │                               │
  │ (If token expires)            │
  ├─ Refresh Token Request ──────→
  │                               │
  │   ← ─ ─ New JWT Token ──── ←
  │                               │
  └───────────────────────────────┘
```

### JWT Configuration
```properties
# In application.properties
app.jwtSecret=your_very_long_secret_key_with_at_least_32_characters_recommended_to_use_random_string
app.jwtExpirationMs=86400000  # 24 hours
app.jwtRefreshExpirationMs=604800000  # 7 days
```

### Security Best Practices

1. **HTTPS Only in Production**
   - Enable SSL/TLS
   - Use valid certificates
   - Force HTTPS redirect

2. **JWT Secret Management**
   - Keep secret in secure storage (environment variables, vault)
   - Never hardcode in source
   - Rotate periodically
   - Use strong random generation

3. **CORS Configuration**
   - Whitelist only trusted origins
   - Specify allowed methods (GET, POST, PUT, DELETE)
   - Limit credentials access

4. **Password Security**
   - Enforce strong password policy
   - Use bcrypt/argon2 for hashing
   - Implement account lockout after failed attempts

5. **SQL Injection Prevention**
   - Use parameterized queries (JPA handles this)
   - Never concatenate SQL strings
   - Validate input on server side

### Authentication Endpoints

```
POST /api/auth/login
  Request: {"username": "user", "password": "pass"}
  Response: {"accessToken": "JWT", "refreshToken": "REFRESH"}

POST /api/auth/refresh
  Request: {"refreshToken": "REFRESH"}
  Response: {"accessToken": "JWT"}

POST /api/auth/logout
  Request: {"refreshToken": "REFRESH"}
  Response: {"message": "success"}
```

---

## Monitoring & Logging

### Logging Configuration

**Log Levels:**
- `ERROR` - Critical failures requiring immediate action
- `WARN` - Potential issues that should be investigated
- `INFO` - Normal application flow (business transactions)
- `DEBUG` - Detailed diagnostic information

**Log Files:**
- Application logs: `/opt/earmsbackend/logs/application.log`
- Size rotation: 10MB per file
- Retention: 10 files (max 100MB)

### Monitoring Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Application info
curl http://localhost:8080/actuator/info
```

### Performance Metrics to Monitor

| Metric | Threshold | Action |
|--------|-----------|--------|
| **CPU Usage** | > 80% | Investigate slow queries, scale up |
| **Memory Usage** | > 85% | Increase heap size, optimize code |
| **Database Connections** | > 80% of pool | Increase connection pool size |
| **API Response Time** | > 5 seconds | Profile endpoints, optimize DB |
| **Error Rate** | > 1% | Review application logs |
| **Disk Space** | < 10% free | Archive old logs, cleanup uploads |

### Logging Best Practices

1. **Use Appropriate Log Levels**
   ```java
   logger.debug("Processing order: {}", orderId);  // Development
   logger.info("Order submitted by {}", userId);    // Business event
   logger.warn("High memory usage: {}%", usage);   // Warning condition
   logger.error("Failed to process payment", ex);   // Error condition
   ```

2. **Structured Logging**
   ```java
   logger.info("User login", Map.of(
       "userId", user.getId(),
       "username", user.getUsername(),
       "timestamp", LocalDateTime.now()
   ));
   ```

3. **Log Sensitive Data Carefully**
   - Never log passwords
   - Mask credit card numbers
   - Mask personal identifiable information (PII)

---

## Backup & Recovery

### Backup Schedule

```
Daily Schedule:
  2:00 AM  - Full database backup
  Every 15 min - Transaction log backup
  
Weekly Schedule:
  Sunday 2:00 AM - Full backup (weekly retention)
  
Monthly Schedule:
  1st of month 2:00 AM - Full backup (yearly retention)
```

### Backup Verification
```bash
# Monthly verify all backups
RESTORE VERIFYONLY FROM DISK = N'/backups/IDAYAM_ERP_Daily_20260401.bak'

# Check integrity
DBCC CHECKDB (IDAYAM_ERP)
```

### Recovery Time Objectives (RTO/RPO)

| Scenario | RTO | RPO | Procedure |
|----------|-----|-----|-----------|
| **Minor Data Loss** | 1 hour | 15 min | Point-in-time recovery |
| **Database Corruption** | 2 hours | 24 hr | Full restore from backup |
| **Server Failure** | 4 hours | 24 hr | Restore to new server |
| **Total Loss** | 8 hours | 24 hr | Use secondary datacenter |

---

## Troubleshooting Guide

### Application Won't Start

**Symptom:** Application fails on startup

**Diagnosis:**
```bash
# Check logs
tail -100 /opt/earmsbackend/logs/application.log

# Check if port is in use
lsof -i :8080

# Test database connection
sqlcmd -S db-server -U user -P pass -d IDAYAM_ERP
```

**Solutions:**
1. Database connection failed → Verify credentials, network connectivity
2. Port already in use → Change port or kill existing process
3. Insufficient memory → Increase JVM heap size
4. Missing properties → Check application.properties file

### High Memory Usage

**Symptom:** Application memory grows continuously

**Solutions:**
```bash
# Check memory usage
jps -l -v  # Shows Java processes and heap size

# Increase heap
java -Xmx8g -Xms4g -jar earmsbackend.jar

# Add to systemd service:
# Environment="JAVA_OPTS=-Xmx8g -Xms4g"
```

### Slow API Responses

**Diagnosis:**
1. Check application logs for slow queries
2. Monitor database queries
3. Review network latency

**Solutions:**
```bash
# Enable query logging
# In application.properties:
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Add database indexes
# Check execution plans for slow queries
```

### Database Connection Issues

**Check:**
```bash
# Is database server running?
nmap -p 1433 db-server

# Test SQL connection
sqlcmd -S db-server -U user -P pass

# Check firewall
iptables -L -n | grep 1433
```

---

## Maintenance Procedures

### Database Maintenance (Weekly)

```sql
-- Update statistics
EXEC sp_updatestats

-- Rebuild fragmented indexes (> 30% fragmentation)
DBCC SHOWCONTIG (tablename) WITH TABLERESULTS

-- Defragment indexes
ALTER INDEX ALL ON [tablename] REORGANIZE

-- Shrink transaction log (if needed)
BACKUP LOG [IDAYAM_ERP] WITH TRUNCATE_ONLY
DBCC SHRINKFILE (IDAYAM_ERP_log, 100)
```

### Application Patching

1. **Test Updates** - In staging environment first
2. **Backup Database** - Before any update
3. **Review Changelog** - Check for breaking changes
4. **Update Dependencies** - `mvn clean install`
5. **Rebuild JAR** - `mvn clean package`
6. **Deploy** - Follow deployment procedure
7. **Verify** - Run smoke tests
8. **Monitor** - Watch logs and metrics

### Certificate Renewal (Monthly Check)

```bash
# Check certificate expiration
openssl s_client -connect hostname:443 -showcerts

# Renew if within 30 days of expiration
# Update in reverse proxy (Nginx/Apache)
# Restart services
```

---

## Performance Tuning

### JVM Tuning

```bash
# Recommended heap allocation
-Xmx4g        # Maximum heap size
-Xms2g        # Initial heap size
-XX:+UseG1GC  # Use G1 Garbage Collector (Java 9+)
-XX:MaxGCPauseMillis=200

# Full command:
java -Xmx4g -Xms2g -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar earmsbackend-1.0.0.jar
```

### Database Connection Pool

```properties
# In application.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

### Entity Eager Loading

```java
// Avoid N+1 query problem
// Use fetch = FetchType.LAZY for relationships
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;

// Use @EntityGraph for critical queries
@EntityGraph(attributePaths = {"user", "role"})
@Query("SELECT a FROM Action a WHERE a.id = ?1")
Action findActionWithDetails(Long id);
```

---

## Disaster Recovery

### Business Continuity Plan

### RTO/RPO Summary
- **Critical Systems:** RTO 1hr, RPO 15 min
- **Important Systems:** RTO 4 hrs, RPO 1 hr
- **Non-Critical:** RTO 24 hrs, RPO 24 hrs

### Recovery Procedures

**Scenario 1: Database Corruption**
1. Identify corruption with `DBCC CHECKDB`
2. Restore from last good backup
3. Apply transaction logs to current time
4. Verify data integrity
5. Notify affected users

**Scenario 2: Complete Server Loss**
1. Provision new server (same specs)
2. Install OS and software dependencies
3. Restore database from backup
4. Deploy application
5. Update DNS/Load balancer to point to new server

**Scenario 3: Whole Datacenter Failure**
1. Activate secondary datacenter
2. Failover database replication
3. Update application configs
4. Verify all services operational
5. Monitor for issues

---

## Contact & Escalation

### Support Contacts

| Role | Name | Email | Phone | Available |
|------|------|-------|-------|-----------|
| **System Admin** | [Name] | [email] | [phone] | 24/7 |
| **Database Admin** | [Name] | [email] | [phone] | 24/7 |
| **Development Lead** | [Name] | [email] | [phone] | Office hours |
| **IT Manager** | [Name] | [email] | [phone] | Office hours |

### Escalation Matrix

```
Severity 1 (Critical - System Down)
  ↓
Immediate: Notify System Admin + DBA + Dev Lead
Contact: All support contacts simultaneously
Target Response: 15 minutes
Target Resolution: 4 hours

Severity 2 (High - Major Function Broken)
  ↓
Within 1 hour: Notify System Admin + DBA
Contact: Primary support followed by secondary
Target Response: 1 hour
Target Resolution: 4-8 hours

Severity 3 (Medium - Some Functions Affected)
  ↓
Within 4 hours: Notify System Admin
Contact: Primary support
Target Response: 4 hours
Target Resolution: 1-2 days

Severity 4 (Low - Minor Issues)
  ↓
Within 24 hours: Email System Admin
Contact: Via ticket system
Target Response: 24 hours
Target Resolution: 5 business days
```

### Knowledge Base

- **Internal Wiki:** [Your wiki URL]
- **Documentation:** [Your repo/doc location]
- **API Documentation:** http://[server]:8080/swagger-ui.html
- **Issue Tracker:** [GitHub Issues/Jira]
- **Change Log:** [Location]

### Additional Resources

- **Spring Boot Docs:** https://spring.io/projects/spring-boot
- **Spring Data JPA:** https://spring.io/projects/spring-data-jpa
- **PostgreSQL Docs:** https://www.postgresql.org/docs/
- **Maven Docs:** https://maven.apache.org/

---

## Appendix

### A. Sample Environment Variables

```bash
# .env file for Docker/Kubernetes
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-server:5432/idayam_erp
SPRING_DATASOURCE_USERNAME=idayam_user
SPRING_DATASOURCE_PASSWORD=SecurePassword123!
APP_JWTSECRET=VeryLongSecretKeyWith32CharactersMinimumRequired
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS=-Xmx4g -Xms2g
```

### B. Useful SQL Queries

```sql
-- Check database size
SELECT 
    DB_NAME() as DatabaseName,
    (SUM(size) * 8.0 / 1024 / 1024) as SizeMB
FROM sys.database_files
GROUP BY DB_NAME()

-- Check active connections
SELECT COUNT(*) as ActiveConnections
FROM sys.dm_exec_sessions
WHERE database_id = DB_ID('IDAYAM_ERP')

-- Find slow running queries
SELECT TOP 10
    creation_time,
    last_execution_time,
    execution_count,
    total_elapsed_time / execution_count as avg_elapsed_time,
    query_hash,
    query_plan_hash
FROM sys.dm_exec_query_stats
ORDER BY total_elapsed_time DESC
```

### C. Emergency Procedures

**If System is Down (< 15 seconds):**
1. Check application health: `curl http://localhost:8080/actuator/health`
2. Check database connectivity
3. Restart application: `sudo systemctl restart earmsbackend`

**If Database is Down:**
1. Check PostgreSQL service: `systemctl status postgresql` or `brew services list`
2. Verify TCP/IP is enabled
3. Check disk space and memory
4. If crashed, rollback to last transaction log backup

**If Under Attack (DDoS):**
1. Block suspicious IPs at firewall
2. Enable rate limiting in reverse proxy
3. Contact ISP if external DDoS
4. Scale infrastructure if load-based

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | April 2026 | Dev Team | Initial handover document |

---

**End of Handover Document**

For questions or clarifications, contact the System Administration team.
