# Idayam ERP Backend - Quick Start Guide

## 60-Minute Startup Guide

Get the Idayam ERP Backend running on your machine in under 60 minutes.

---

## Prerequisites (5 minutes)

Ensure you have installed:

```bash
# Check if installed
java -version      # Java 11 or higher
mvn -version       # Maven 3.6 or higher
git --version      # (optional, for cloning)
```

**Download if missing:**
- Java 11+: [openjdk.java.net](https://openjdk.java.net/install/)
- Maven: [maven.apache.org](https://maven.apache.org/download.cgi)
- SQL Server: [PostgreSQL 12+](https://www.postgresql.org/download/)

---

## Step 1: Clone & Prepare (5 minutes)

```bash
# Navigate to your projects folder
cd /path/to/projects

# Clone repository
git clone <your-repo-url>
cd earmsbackend

# Verify structure
ls -la src/
# Should show: main/ resources/ test/
```

---

## Step 2: Database Setup (10 minutes)

### Option A: PostgreSQL (Recommended)

```sql
-- Connect to SQL Server as admin
-- Run this script:

CREATE DATABASE idayam_erp;

CREATE USER idayam_user WITH PASSWORD = 'ComplexPassword123!';
ALTER ROLE idayam_user SET client_encoding TO 'utf8';
ALTER ROLE idayam_user SET default_transaction_isolation TO 'read committed';
GRANT ALL PRIVILEGES ON DATABASE idayam_erp TO idayam_user;

-- Verify
SELECT datname FROM pg_database WHERE datname = 'idayam_erp';
```

### Option B: H2 Database (Testing Only)

Skip database setup and use H2 in-memory database in `application-dev.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
```

---

## Step 3: Configuration (5 minutes)

Create `src/main/resources/application.properties`:

```properties
# ========== SERVER ==========
server.port=8080
server.servlet.context-path=/

# ========== DATABASE ==========
# For PostgreSQL (Option A):
spring.datasource.url=jdbc:postgresql://localhost:5432/idayam_erp
spring.datasource.username=idayam_user
spring.datasource.password=ComplexPassword123!
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# For H2 (Option B - testing):
# spring.datasource.url=jdbc:h2:mem:testdb
# spring.datasource.driverClassName=org.h2.Driver
# spring.datasource.username=sa
# spring.datasource.password

# ========== JPA ==========
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# ========== LOGGING ==========
logging.level.root=INFO
logging.level.com.idayam=DEBUG

# ========== JWT ==========
app.jwtSecret=your_very_long_secret_key_with_minimum_32_characters_recommended_is_64
app.jwtExpirationMs=86400000

# ========== FILE UPLOAD ==========
spring.servlet.multipart.max-file-size=1024MB

# ========== ACTIVE PROFILES ==========
spring.profiles.active=dev
```

---

## Step 4: Build Project (10 minutes)

```bash
# Navigate to project root
cd earmsbackend

# Download dependencies and build
mvn clean install

# This will:
# - Download all dependencies
# - Compile code
# - Run tests (if any)
# - Create JAR file

# Should see: BUILD SUCCESS
```

**Troubleshooting Build:**
```bash
# If build fails due to tests
mvn clean install -DskipTests

# If dependency issues
mvn dependency:tree  # Check dependencies
rm -rf ~/.m2/repository  # Clear Maven cache
mvn clean install  # Retry
```

---

## Step 5: Run Application (5 minutes)

### Option A: Maven (Recommended for Development)

```bash
# Run directly
mvn spring-boot:run

# Application starts on http://localhost:8080
# You'll see: "Started IdayamApplication in X.XXX seconds"
```

### Option B: JAR Execution

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/earmsbackend-*.jar

# Run on different port
java -Dserver.port=9090 -jar target/earmsbackend-*.jar
```

### Option C: IDE (IntelliJ/Eclipse)

**IntelliJ IDEA:**
1. File → Open → Select project root
2. Right-click `IdayamApplication.java`
3. Select "Run 'IdayamApplication'"

**Eclipse:**
1. File → Import → Maven → Existing Maven Projects
2. Right-click project → Run As → Java Application
3. Or: Run → Run Configurations → Create new → Java Application

---

## Step 6: Verify Installation (5 minutes)

### Health Check

```bash
# Check if application is running
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### Access API Documentation

```
Open browser: http://localhost:8080/swagger-ui.html
```

You'll see interactive API documentation with all endpoints.

### First Login

```bash
# Default credentials (if initialized):
# Username: admin
# Password: admin123

# Or create new user via API
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@company.com",
    "password": "TestPassword123!"
  }'
```

---

## Common Commands

### Start Application
```bash
mvn spring-boot:run
```

### Stop Application
```
Ctrl + C  # in terminal
```

### Build JAR
```bash
mvn clean package
```

### Run Tests
```bash
mvn test
```

### Skip Tests During Build
```bash
mvn clean package -DskipTests
```

### View Dependencies
```bash
mvn dependency:tree
```

### Clean Build
```bash
mvn clean
```

---

## Project Structure Quick Reference

```
earmsbackend/
├── src/
│   └── main/
│       ├── java/com/idayam/
│       │   ├── IdayamApplication.java     ← Main class
│       │   ├── controller/                ← API endpoints (106 files)
│       │   ├── service/                   ← Business logic
│       │   ├── dao/                       ← Database queries
│       │   ├── model/                     ← Entity classes
│       │   ├── dto/                       ← Data Transfer Objects
│       │   ├── config/                    ← Security, JWT, CORS
│       │   └── utils/                     ← Utilities
│       └── resources/
│           └── application.properties     ← Configuration (create this)
│
├── pom.xml                                ← Maven dependencies
├── target/                                ← Built artifacts
└── README.md                              ← Project info

Documentation Files:
├── CONTROLLERS_OVERVIEW.md              ← All 106 controllers
├── CONTROLLER_DETAILED_DOCUMENTATION.md ← Detailed endpoints
├── API_ENDPOINTS_REFERENCE.md           ← Quick API reference
├── DEVELOPER_SETUP_GUIDE.md             ← Complete setup
├── HANDOVER_DOCUMENT.md                 ← Production support
└── QUICK_START_GUIDE.md                 ← This file
```

---

## Key Files to Know

| File | Purpose |
|------|---------|
| `IdayamApplication.java` | Application entry point |
| `SwaggerConfig.java` | API documentation config |
| `WebSecurityConfig.java` | Security configuration |
| `JwtUtils.java` | JWT token handling |
| `application.properties` | Database, port, logging config |
| `pom.xml` | Dependencies and build config |

---

## API Quick Examples

### Login
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'

# Response contains: accessToken, refreshToken
```

### List Users
```bash
curl -H "Authorization: Bearer <your_access_token>" \
  http://localhost:8080/api/user?page=0&size=20
```

### Create Branch
```bash
curl -X POST http://localhost:8080/api/saveBranch \
  -H "Authorization: Bearer <your_access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "branchName": "Chennai Branch",
    "branchCode": "CHN",
    "city": "Chennai",
    "state": "Tamil Nadu"
  }'
```

---

## Troubleshooting Quick Fixes

### Port 8080 Already in Use
```bash
# Find process using port 8080
# Windows: netstat -ano | findstr :8080
# Linux: lsof -i :8080

# Change port in application.properties
server.port=8081
```

### Database Connection Error
```bash
# Check connection string in application.properties
# Verify PostgreSQL is running
# Verify username/password
# Test connection:
sqlcmd -S localhost -U appuser -P ComplexPassword123! -d IDAYAM_ERP
```

### Java Version Wrong
```bash
# Check Java version
java -version

# If not 11+, update JAVA_HOME:
# Windows: set JAVA_HOME=C:\Program Files\Java\jdk-11
# Linux: export JAVA_HOME=/usr/lib/jvm/java-11
```

### Build Fails with Tests
```bash
# Skip tests
mvn clean install -DskipTests
```

### Swagger UI not Loading
```bash
# Verify app is running
curl http://localhost:8080/swagger-ui.html

# Check logs for errors
# Ensure SwaggerConfig.java exists and is configured
```

---

## What's Included

### 106 REST Controllers for:
- ✅ User authentication and authorization
- ✅ HR and attendance management
- ✅ Inventory and stock management
- ✅ Purchase orders and procurement
- ✅ Sales orders and transactions
- ✅ Financial accounting
- ✅ Production and quality control
- ✅ Facility and asset management
- ✅ Reports and analytics
- ✅ Administrative operations

### Key Technologies:
- ✅ Spring Boot 2.6+
- ✅ Spring Security + JWT authentication
- ✅ Hibernate JPA
- ✅ PostgreSQL database
- ✅ Swagger 2.0 API documentation
- ✅ Maven build tool
- ✅ Java 11+

### Database Support:
- ✅ PostgreSQL 12+
- ✅ H2 (testing)
- ✅ MySQL/MariaDB (with config changes)

---

## Next Steps After Startup

1. **Explore API Documentation**
   - Visit: http://localhost:8080/swagger-ui.html
   - Try out endpoints in Swagger UI

2. **Read Detailed Documentation**
   - `CONTROLLERS_OVERVIEW.md` - Overview of all 106 controllers
   - `API_ENDPOINTS_REFERENCE.md` - Complete API reference
   - `CONTROLLER_DETAILED_DOCUMENTATION.md` - Detailed endpoint docs

3. **Understand Architecture**
   - `DEVELOPER_SETUP_GUIDE.md` - Development environment and best practices

4. **Production Deployment**
   - `HANDOVER_DOCUMENT.md` - Deployment, monitoring, maintenance

5. **Create Sample Data**
   - Create test users via API
   - Create sample branches, departments, products
   - Test workflows and transactions

---

## Common Development Tasks

### Add New Endpoint
1. Create method in Controller: `@PostMapping("/resource")`
2. Implement business logic in Service
3. Add DAO method if needed
4. Test via Swagger UI

### Debug Application
```bash
# Enable debug logging
logging.level.com.idayam=DEBUG

# View detailed logs
tail -f logs/application.log  # Linux/Mac
type logs\application.log     # Windows
```

### Run Specific Test
```bash
mvn test -Dtest=UserControllerTest
```

### Generate JavaDoc
```bash
mvn javadoc:javadoc
# Output in: target/site/apidocs/
```

---

## Performance Tips

1. **Development:**
   - Use H2 in-memory database for faster testing
   - Set `spring.jpa.show-sql=false` for better performance

2. **Production:**
   - Use PostgreSQL with proper indexes
   - Set appropriate JVM heap size: `-Xmx4g -Xms2g`
   - Enable query caching
   - Use connection pooling

3. **Logging:**
   - Set production log level to `INFO`
   - Rotate logs daily
   - Monitor log file size

---

## IDE Shortcuts

### IntelliJ IDEA
- `Ctrl+Shift+R` - Run `IdayamApplication`
- `Ctrl+Shift+D` - Debug
- `Ctrl+Alt+L` - Format code
- `Ctrl+/` - Comment code
- `Ctrl+H` - Find and replace

### Eclipse
- `Ctrl+F11` - Run as Java Application
- `F11` - Debug
- `Ctrl+Shift+F` - Format
- `Ctrl+/` - Comment
- `Ctrl+H` - Find/Replace

### VS Code
- `F5` - Start debugging
- `Ctrl+Shift+P` - Command palette
- `Ctrl+` ` - Terminal
- `Alt+Shift+F` - Format

---

## Getting Help

### Resources
- 📚 **Spring Boot Docs:** https://spring.io/projects/spring-boot
- 🔐 **Spring Security:** https://spring.io/projects/spring-security
- 🗄️ **Hibernate:** https://hibernate.org/
- 🗄️ **PostgreSQL:** https://www.postgresql.org/docs/

### In This Project
- 💡 See `DEVELOPER_SETUP_GUIDE.md` for detailed setup
- 📖 See `CONTROLLER_DETAILED_DOCUMENTATION.md` for all endpoints
- 🚀 See `HANDOVER_DOCUMENT.md` for production issues

### From Team
- 💬 Ask development team for technical questions
- 📋 Check API documentation at http://localhost:8080/swagger-ui.html
- 🔍 Review existing controller code for patterns

---

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| "Connection refused" | Start PostgreSQL / Check credentials |
| "Port already in use" | Change server.port in application.properties |
| "Class not found" | Run `mvn clean install` |
| "No database selected" | Create database and update connection string |
| "Authentication failed" | Check JWT secret in application.properties |
| "Swagger not loading" | Restart application / Clear browser cache |

---

## Estimated Development Skills

**Beginners:** 2-3 hours to understand setup and run first API call
**Intermediate:** 1 hour to setup and develop custom endpoints
**Advanced:** 30 minutes for full setup and customization

---

## Success Indicators

✅ Application starts without errors
✅ http://localhost:8080/swagger-ui.html loads
✅ Can call `/api/login` endpoint
✅ Can view user list after login
✅ No database errors in logs

---

## What's Next?

After successful startup:

1. **Explore the codebase** - Review controller and service classes
2. **Try the API** - Use Swagger UI to test endpoints
3. **Read documentation** - Review included .md files
4. **Create test data** - Add users, branches, products
5. **Build custom features** - Follow existing patterns
6. **Deploy to production** - Follow handover document

---

## Quick Reference Commands

```bash
# Build and run in one command
mvn clean package -DskipTests && java -jar target/*.jar

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# Run multiple commands
mvn clean && mvn install && mvn spring-boot:run

# Check application status
curl http://localhost:8080/actuator/health

# View all beans
curl http://localhost:8080/actuator/beans | grep beanName

# Check environment properties
curl http://localhost:8080/actuator/env
```

---

**Congratulations!** Your Idayam ERP Backend is now running! 🎉

**Total time:** Should be under 60 minutes
**Questions?** Refer to other documentation files or contact development team

---

**Document Version:** 1.0  
**Created:** April 2026  
**Last Updated:** April 2026
