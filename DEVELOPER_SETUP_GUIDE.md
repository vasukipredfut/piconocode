# Idayam ERP Backend - Comprehensive Developer Setup Guide

**Version**: 1.0 | **Last Updated**: April 22, 2026 | **Java 11+** | **Spring Boot 2.6+** | **PostgreSQL 12+**

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Detailed Environment Setup](#detailed-environment-setup)
4. [Database Setup & Migration](#database-setup--migration)
5. [Build & Run](#build--run)
6. [IDE Configuration](#ide-configuration)
7. [API Development Guide](#api-development-guide)
8. [Testing & Debugging](#testing--debugging)
9. [Project Architecture](#project-architecture)
10. [Code Standards & Conventions](#code-standards--conventions)
11. [Performance Optimization](#performance-optimization)
12. [Security Best Practices](#security-best-practices)
13. [CI/CD & Deployment](#cicd--deployment)
14. [Troubleshooting](#troubleshooting)
15. [Advanced Topics](#advanced-topics)

---

## Quick Start (5 Minutes)

For experienced developers, quick setup:

```bash
# 1. Clone repository
git clone <repo-url> && cd earmsbackend

# 2. Verify prerequisites
java -version      # Must be 11+
mvn --version      # Must be 3.6+
psql --version     # Must be 12+

# 3. Setup database (use sql_migrations folder)
cd sql_migrations && ./quick-setup.sh

# 4. Build project
mvn clean install

# 5. Run application
mvn spring-boot:run

# Application runs at: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

---

## Prerequisites

### System Requirements
- **Java:** JDK 11 or higher
- **Maven:** 3.6.0 or higher
- **Database:** PostgreSQL 12+ or higher
- **Memory:** 4GB minimum (8GB recommended)
- **Disk Space:** 2GB for application and dependencies

### Required Software
```bash
# Verify Java installation
java -version
# Expected: Java 11+

# Verify Maven installation
mvn --version
# Expected: Maven 3.6.0+

# Optional: Git for version control
git --version
```

### IDE Requirements
- **IntelliJ IDEA** 2021+ OR
- **Eclipse** 2021-06+ OR
- **VS Code** with Spring Boot Extension Pack

---

## Project Setup

### Step 1: Clone Repository
```bash
# Clone the repository
git clone <repository-url>
cd earmsbackend

# Verify structure
ls -la src/
# Should show: main/ resources/ test/
```

### Step 2: Maven Dependencies
```bash
# Clean and install dependencies
mvn clean install

# Skip tests during installation (optional)
mvn clean install -DskipTests

# Check for dependency issues
mvn dependency:tree
```

### Step 3: Database Driver
Ensure PostgreSQL JDBC driver is in dependencies:
```xml
<!-- In pom.xml -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## Detailed Environment Setup

### Advanced application.properties Configuration

For development with full feature support:

```properties
# ========== SERVER CONFIGURATION ==========
server.port=8080
server.servlet.context-path=/
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.force=true
server.error.include-message=always
server.error.include-binding-errors=always

# ========== DATABASE CONNECTION POOL ==========
spring.datasource.url=jdbc:postgresql://localhost:5432/idayam_erp
spring.datasource.username=idayam_user
spring.datasource.password=ComplexPassword123!
spring.datasource.driver-class-name=org.postgresql.Driver

# HikariCP Connection Pool (highly optimized)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.leak-detection-threshold=60000

# ========== JPA & HIBERNATE CONFIGURATION ==========
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL10Dialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# ========== QUERY OPTIMIZATION ==========
spring.jpa.properties.hibernate.generate_statistics=false
spring.jpa.properties.hibernate.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory

# ========== LOGGING CONFIGURATION ==========
logging.level.root=INFO
logging.level.com.idayam=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.config=classpath:logging.properties
logging.file.name=logs/application.log
logging.file.max-size=10MB
logging.file.max-history=30

# ========== JWT SECURITY CONFIGURATION ==========
app.jwtSecret=your_super_secret_key_must_be_at_least_32_characters_long_for_security
app.jwtExpirationMs=86400000
app.jwtRefreshExpirationMs=604800000

# ========== FILE UPLOAD CONFIGURATION ==========
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=1024MB
spring.servlet.multipart.max-request-size=1024MB
spring.servlet.multipart.file-size-threshold=1MB

# ========== SWAGGER/SPRINGFOX CONFIGURATION ==========
springfox.documentation.swagger-ui.enabled=true
springfox.documentation.swagger-ui.path=/swagger-ui/index.html
springfox.documentation.swagger.v2.path=/api-docs
springfox.documentation.swagger2.v2.host=localhost:8080
springfox.documentation.swagger-ui.tagsSorter=alpha
springfox.documentation.swagger-ui.operationsSorter=alpha

# ========== JACKSON JSON CONFIGURATION ==========
spring.jackson.default-property-inclusion=non_null
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.serialization.indent-output=true
spring.jackson.time-zone=UTC
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss

# ========== CORS CONFIGURATION ==========
# Configure in WebSecurityConfig or application.properties
# Handled in CorsConfiguration.java

# ========== ACTIVE PROFILE ==========
spring.profiles.active=dev

# ========== ADDITIONAL FEATURES ==========
# Enable actuator endpoints
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=when-authorized
```

### Development Profile (application-dev.properties)

```properties
# Development specific settings
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
logging.level.com.idayam=DEBUG
logging.level.org.springframework.web=DEBUG
spring.h2.console.enabled=false
server.error.include-stacktrace=always
```

### Production Profile (application-prod.properties)

```properties
# Production specific settings
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
logging.level.com.idayam=WARN
logging.level.org.springframework.web=WARN
logging.level.org.springframework.security=WARN

# Performance optimization
server.compression.enabled=true
server.compression.min-response-size=1024
server.compression.excluded-media-types=image/png,image/jpeg

# Security
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict
```

### Enhanced Logging Configuration

Create `src/main/resources/logback-spring.xml` for better logging control:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Define properties -->
    <property name="LOG_FILE" value="${LOG_FILE:-${LOG_PATH:-${LOG_TEMP:-${java.io.tmpdir:-/tmp}}/spring.log}}"/>
    
    <!-- Console appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- File appender -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
    </appender>
    
    <!-- Logger configuration -->
    <logger name="com.idayam" level="DEBUG"/>
    <logger name="org.springframework.web" level="INFO"/>
    <logger name="org.springframework.security" level="DEBUG"/>
    <logger name="org.hibernate.SQL" level="DEBUG"/>
    <logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="TRACE"/>
    
    <!-- Root logger -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

---

## Database Setup & Migration

### Step 1: Application Properties

Create `src/main/resources/application.properties`:

```properties
# ========== SERVER CONFIGURATION ==========
server.port=8080
server.servlet.context-path=/
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.force=true

# ========== DATABASE CONFIGURATION ==========
spring.datasource.url=jdbc:postgresql://localhost:5432/idayam_erp
spring.datasource.username=idayam_user
spring.datasource.password=secure_password_here
spring.datasource.driver-class-name=org.postgresql.Driver

# ========== JPA CONFIGURATION ==========
spring.jpa.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# ========== LOGGING CONFIGURATION ==========
logging.level.root=INFO
logging.level.com.idayam=DEBUG
logging.config=classpath:logging.properties

# ========== JWT CONFIGURATION ==========
app.jwtSecret=your_secret_key_min_32_characters_long
app.jwtExpirationMs=86400000

# ========== FILE UPLOAD CONFIGURATION ==========
spring.servlet.multipart.max-file-size=1024MB
spring.servlet.multipart.max-request-size=1024MB

# ========== SWAGGER DOCUMENTATION ==========
springfox.documentation.swagger2.v2.host=localhost:8080
springfox.documentation.swagger-ui.enabled=true

# ========== ACTIVE PROFILE ==========
spring.profiles.active=dev
```

### Step 2: Logging Properties

Create `src/main/resources/logging.properties`:

```properties
# Logging pattern
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Log levels
logging.level.com.idayam=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.hibernate=WARN
logging.level.org.springframework.security=INFO

# File logging
logging.file.name=logs/application.log
logging.file.max-size=10MB
logging.file.max-history=10
```

### Step 3: Environment-Specific Configuration

Create profiles for different environments:

**application-dev.properties:**
```properties
spring.jpa.hibernate.ddl-auto=validate
logging.level.com.idayam=DEBUG
spring.h2.console.enabled=false
```

**application-prod.properties:**
```properties
spring.jpa.hibernate.ddl-auto=validate
logging.level.com.idayam=INFO
server.compression.enabled=true
server.compression.min-response-size=1024
```

---

## Database Setup

### Step 1: PostgreSQL Connection

```bash
# Test connection
psql -h localhost -U idayam_user -d idayam_erp

# Or install PostgreSQL client if needed
sudo apt-get install postgresql-client  # For Ubuntu/Debian
brew install postgresql  # For macOS
```

### Step 2: Create Database

```sql
-- Connect as admin
sudo -u postgres psql

-- Create database
CREATE DATABASE idayam_erp;

-- Create user
CREATE USER idayam_user WITH PASSWORD 'secure_password_here';

-- Grant privileges
ALTER ROLE idayam_user SET client_encoding TO 'utf8';
ALTER ROLE idayam_user SET default_transaction_isolation TO 'read committed';
GRANT ALL PRIVILEGES ON DATABASE idayam_erp TO idayam_user;

-- Connect to database
\c idayam_erp idayam_user

-- Create schema
ALTER DATABASE IDAYAM_ERP SET TRUSTWORTHY ON
GO
```

### Step 3: Create Required Tables

The application uses Hibernate JPA for ORM. Tables are typically auto-created via entity classes:

**Key Tables (auto-generated from entities):**
- `users` - User accounts
- `roles` - Role definitions
- `permissions` - Module permissions
- `refresh_tokens` - JWT refresh tokens
- `branches` - Organization branches
- `departments` - Departments
- `designations` - Job designations
- And 100+ business entity tables

### Step 4: Initial Data

Create a SQL script for initial data:

```sql
USE IDAYAM_ERP
GO

-- Insert roles
INSERT INTO roles (role_name) VALUES ('ADMIN'), ('USER'), ('MANAGER')
GO

-- Insert default admin user
-- Password hashing done via application during user creation
INSERT INTO users (username, email, active, created_date)
VALUES ('admin', 'admin@idayam.com', 1, GETDATE())
GO
```

### Database Connection Verification

```bash
# Test connection with psql
psql -h localhost -p 5432 -U idayam_user -d idayam_erp -c "SELECT 1;"

# View current database
SELECT current_database();

# List all tables
\dt

# Get table structure
\d users
```

### Running Database Migrations

```bash
# Linux/Mac: Use provided migration script
cd sql_migrations
chmod +x run-migrations.sh
./run-migrations.sh

# Or with quick setup
chmod +x quick-setup.sh
./quick-setup.sh

# Windows: Use batch script
cd sql_migrations
run-migrations.bat
```

### Verifying Migration Success

After running migrations, verify in psql:

```sql
-- Check all tables created
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = 'public';
-- Should return 20+

-- Check users table
SELECT COUNT(*) FROM users;

-- Check default roles
SELECT * FROM roles;

-- Check permissions count
SELECT COUNT(*) FROM permissions;
```

### Creating Development Test Data

```sql
-- Create test user for development
INSERT INTO users (username, password, email, first_name, last_name, active)
VALUES ('devuser', 'hashed_password_here', 'dev@idayam.com', 'Dev', 'User', true);

-- Assign test role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'devuser' AND r.name = 'USER';
```

---

## Build & Run

### Maven Build Options

```bash
# Development build with debug info
mvn clean package -Pdev -X

# Production optimized build
mvn clean package -Pprod

# Quick build (skip tests)
mvn clean package -DskipTests

# Build with specific Java version
mvn clean package -source 11 -target 11

# Build with additional maven options
mvn clean package -T 1C  # Parallel build

# Check for security vulnerabilities
mvn org.owasp:dependency-check-maven:check
```

### Running Application

```bash
# Standard Spring Boot run
mvn spring-boot:run

# Run with active profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Run with custom port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"

# Run JAR directly
java -jar target/idayam-0.0.1-SNAPSHOT.jar

# Run JAR with custom configuration
java -Dspring.config.location=file:./config/application.properties \
     -Dspring.profiles.active=prod \
     -jar target/idayam-0.0.1-SNAPSHOT.jar

# Run with JVM debugging enabled (port 5005)
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

### Verify Application is Running

```bash
# Test application health
curl http://localhost:8080/actuator/health

# Check Swagger documentation
curl http://localhost:8080/swagger-ui.html

# Login and get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"ComplexPassword123!"}'

# Use token to access protected endpoint
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/employees
```

### Docker Deployment

```dockerfile
# Multi-stage build Dockerfile
FROM maven:3.8.1-openjdk-11 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-Xmx1g", \
  "-Xms512m", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-jar", "app.jar"]
```

Build and run Docker container:

```bash
# Build image
docker build -t idayam-erp:latest .

# Run container with database link
docker run -d \
  --name idayam-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://idayam-db:5432/idayam_erp \
  -e SPRING_DATASOURCE_USERNAME=idayam_user \
  -e SPRING_DATASOURCE_PASSWORD=ComplexPassword123! \
  -e JAVA_OPTS="-Xmx1g -Xms512m" \
  idayam-erp:latest

# View logs
docker logs -f idayam-app

# Stop container
docker stop idayam-app
docker rm idayam-app
```

---

## IDE Configuration

### IntelliJ IDEA Professional Setup

```xml
<!-- File: .idea/runConfigurations/IdayamApp.xml -->
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="IdayamApp" type="SpringBootApplicationConfigurationType">
    <module name="idayam" />
    <option name="SPRING_BOOT_MAIN_CLASS" value="com.idayam.IdayamApplication" />
    <option name="VM_PARAMETERS" value="-Dspring.profiles.active=dev -Xmx1g -Xms512m" />
    <option name="PROGRAM_PARAMETERS" value="" />
    <envs>
      <env name="SPRING_DATASOURCE_PASSWORD" value="ComplexPassword123!" />
    </envs>
    <method v="2" />
  </configuration>
</component>
```

Advanced IntelliJ configuration:

```
1. File → Settings → Build, Execution, Deployment
   → Compiler → Java Compiler
   → Set target bytecode version to 11

2. Enable incremental compilation:
   → Build, Execution, Deployment → Compiler → Share build process heap

3. Debug configuration:
   → Run → Edit Configurations
   → Add debug breakpoints
   → Run → Debug 'IdayamApp'

4. Code analysis:
   → Analyze → Run Inspection by Name
   → Search "Spring" for Spring-specific inspections
```

### Eclipse Advanced Setup

Create Maven launch configuration:

```
Run → External Tools → External Tools Configurations
- Location: ${project_loc:earmsbackend}/../mvn
- Working Directory: ${project_loc:earmsbackend}
- Arguments: clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### VS Code Development Setup

Create `.vscode/tasks.json`:

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Maven Build",
      "type": "shell",
      "command": "mvn",
      "args": ["clean", "package", "-DskipTests"],
      "group": {
        "kind": "build",
        "isDefault": true
      },
      "problemMatcher": [],
      "presentation": {
        "echo": true,
        "reveal": "always",
        "panel": "new"
      }
    },
    {
      "label": "Spring Boot Run",
      "type": "shell",
      "command": "mvn",
      "args": ["spring-boot:run", "-Dspring-boot.run.arguments=\"--spring.profiles.active=dev\""],
      "group": {
        "kind": "test",
        "isDefault": true
      }
    }
  ]
}
```

---

## API Development Guide

### REST Endpoint Structure

```java
@RestController
@RequestMapping("/api/v1/employees")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    /**
     * Get all employees with pagination and filtering
     * @param page Zero-based page number
     * @param size Records per page
     * @param sortBy Field to sort by
     * @param direction Sort direction (ASC/DESC)
     * @return Page of employees
     */
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_EMPLOYEES')")
    @ApiOperation(value = "Get all employees", response = EmployeeDTO.class)
    @ApiResponses({
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden")
    })
    public ResponseEntity<?> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Page<EmployeeDTO> employees = employeeService.getAllEmployees(
            PageRequest.of(page, size, 
                Sort.Direction.fromString(direction), sortBy));
        
        return ResponseEntity.ok(employees);
    }
    
    /**
     * Create new employee
     * @param employeeDTO Employee data
     * @return Created employee
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_EMPLOYEES')")
    @ApiOperation(value = "Create new employee")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO created = employeeService.createEmployee(employeeDTO);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(created);
    }
}
```

### DTO Best Practices

```java
// EmployeeDTO - Data Transfer Object
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("Employee Data Transfer Object")
public class EmployeeDTO {
    
    @ApiModelProperty("Employee ID (auto-generated)")
    private Long id;
    
    @ApiModelProperty("Employee code (unique)")
    @NotBlank(message = "Employee code is required")
    private String employeeCode;
    
    @ApiModelProperty("First name")
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    private String firstName;
    
    @ApiModelProperty("Email address")
    @Email(message = "Valid email required")
    private String email;
    
    @ApiModelProperty("Department ID")
    @NotNull(message = "Department is required")
    private Long departmentId;
    
    // Getters and setters automatically generated by Lombok
}
```

### Service Layer Pattern

```java
@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final EmailService emailService;
    
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
            .map(employeeMapper::toDTO);
    }
    
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        // Validate
        if (employeeRepository.existsByEmployeeCode(employeeDTO.getEmployeeCode())) {
            throw new BusinessException("Employee code already exists");
        }
        
        // Convert and save
        Employee employee = employeeMapper.toEntity(employeeDTO);
        Employee saved = employeeRepository.save(employee);
        
        // Send welcome email
        emailService.sendWelcomeEmail(saved.getEmail(), saved.getFirstName());
        
        return employeeMapper.toDTO(saved);
    }
}
```

### Custom Exception Handling

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ValidationErrorResponse("Validation failed", errors));
    }
}
```

---

## Testing & Debugging

### Unit Testing with JUnit 5 & Mockito

```java
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    
    @Mock
    private EmployeeRepository employeeRepository;
    
    @InjectMocks
    private EmployeeService employeeService;
    
    @Test
    void testCreateEmployee_Success() {
        // Arrange
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeCode("EMP001");
        employeeDTO.setFirstName("John");
        
        Employee employee = new Employee();
        employee.setId(1L);
        
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.save(any())).thenReturn(employee);
        
        // Act
        EmployeeDTO result = employeeService.createEmployee(employeeDTO);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(employeeRepository, times(1)).save(any());
    }
}
```

### Integration Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EmployeeControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @BeforeEach
    void setup() {
        employeeRepository.deleteAll();
    }
    
    @Test
    void testGetAllEmployees() {
        // Given
        Employee employee = new Employee();
        employee.setEmployeeCode("EMP001");
        employeeRepository.save(employee);
        
        // When
        ResponseEntity<Page> response = restTemplate.getForEntity(
            "/api/v1/employees", Page.class);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
```

### Remote Debugging

```bash
# Start application with debug enabled on port 5005
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
     -jar target/idayam-0.0.1-SNAPSHOT.jar

# In IDE (IntelliJ):
# Run → Edit Configurations → Add Debug Configuration → Remote
# Set Host: localhost, Port: 5005
# Press Debug button
```

### Logging Best Practices

```java
@Slf4j
public class EmployeeService {
    
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        log.info("Creating new employee with code: {}", employeeDTO.getEmployeeCode());
        
        try {
            // ... business logic
            log.debug("Employee validation passed");
            
            Employee saved = employeeRepository.save(employee);
            log.info("Employee created successfully with ID: {}", saved.getId());
            return employeeMapper.toDTO(saved);
            
        } catch (Exception ex) {
            log.error("Error creating employee", ex);
            throw new BusinessException("Failed to create employee", ex);
        }
    }
}
```

---

## Project Architecture

### 5-Layer Architecture Diagram

```
┌─────────────────────────────────────────┐
│        Controller Layer (REST APIs)      │
│  - HTTP Request/Response Handling        │
│  - Parameter Validation                  │
│  - Authentication Check                  │
└──────────┬──────────────────────────────┘
           │
┌──────────▼──────────────────────────────┐
│    Service Layer (Business Logic)        │
│  - Business Rules                        │
│  - Data Transformation                   │
│  - Transaction Management                │
└──────────┬──────────────────────────────┘
           │
┌──────────▼──────────────────────────────┐
│    DAO/Repository Layer (Data Access)    │
│  - JPA Repositories                      │
│  - Custom Query Methods                  │
│  - Entity Persistence                    │
└──────────┬──────────────────────────────┘
           │
┌──────────▼──────────────────────────────┐
│     Database Layer (PostgreSQL)          │
│  - Persistent Data Storage               │
│  - Transactions                          │
│  - Constraints & Relationships           │
└─────────────────────────────────────────┘
```

### Module Organization

```
src/main/java/com/idayam/
├── IdayamApplication.java           # Main entry point
├── config/                           # Configuration classes
│   ├── SecurityConfig.java
│   ├── JwtUtils.java
│   ├── SwaggerConfig.java
│   ├── CorsConfiguration.java
│   └── WebSecurityConfig.java
├── controller/                       # REST Controllers (106 controllers)
│   ├── ActionController.java
│   ├── EmployeeController.java
│   ├── ProcurementController.java
│   └── [others...]
├── service/                          # Business Logic (Service classes)
│   ├── EmployeeService.java
│   ├── ProcurementService.java
│   └── [others...]
├── dao/                              # Data Access Objects (Repositories)
│   ├── EmployeeRepository.java
│   ├── ProcurementRepository.java
│   └── [others...]
├── model/                            # JPA Entity Classes (100+ entities)
│   ├── Employee.java
│   ├── ProcurementOrder.java
│   └── [others...]
├── dto/                              # Data Transfer Objects
│   ├── EmployeeDTO.java
│   ├── ProcurementDTO.java
│   └── [others...]
├── utils/                            # Utility classes
│   ├── Constants.java
│   ├── Validators.java
│   └── DateUtils.java
└── sqlserver/                        # SQL Server specific code
    ├── dao/
    └── model/
```

---

## Code Standards & Conventions

### Java Naming Conventions

```java
// Classes - PascalCase
public class EmployeeService { }
public class EmployeeDTO { }

// Interfaces - PascalCase (often with "I" prefix or "-able" suffix)
public interface IEmployeeService { }
public interface Auditable { }

// Methods - camelCase
public void createEmployee() { }
public List<Employee> getActiveEmployees() { }

// Constants - UPPER_SNAKE_CASE
public static final String EMPLOYEE_CODE_PREFIX = "EMP-";
public static final int MAX_EMPLOYEES = 10000;

// Variables - camelCase
private String employeeName;
private int departmentId;

// Booleans - "is" or "has" prefix
private boolean isActive;
private boolean hasExperience;
```

### Annotation Usage

```java
// Always use Lombok annotations
@Data                    // Generates getters, setters, toString, equals, hashCode
@Builder                 // Builder pattern
@NoArgsConstructor       // No-arg constructor
@AllArgsConstructor      // All-arg constructor
@RequiredArgsConstructor // Constructor for @NonNull fields
@Slf4j                   // Logger injection

// Spring annotations
@Component       // Generic component
@Service         // Business logic
@Repository      // Data access
@Controller      // REST Controller
@RestController  // REST with @ResponseBody

// Validation annotations
@NotNull, @NotEmpty, @NotBlank
@Email, @Size, @Min, @Max
@Pattern(regexp = "...")
```

### Comment Guidelines

```java
// Use JavaDoc for public methods
/**
 * Creates a new employee in the system.
 * 
 * @param employeeDTO The employee data
 * @return The created employee DTO with generated ID
 * @throws BusinessException if employee code already exists
 * @throws ValidationException if validation fails
 */
public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) { }

// Use line comments for complex logic
// Calculate salary after deducting taxes and insurance
double netSalary = grossSalary - tax - insurance;
```

### Error Handling

```java
// Create custom exceptions
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Use appropriate exception types
public void updateEmployee(Long id, EmployeeDTO dto) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Employee not found with id: " + id));
    
    if (employee.isInactive()) {
        throw new BusinessException("Cannot update inactive employee");
    }
}
```

---

## Performance Optimization

### Database Query Optimization

```java
// ❌ BAD - N+1 Query Problem
List<Employee> employees = employeeRepository.findAll();
for (Employee emp : employees) {
    emp.getDepartment().getName();  // Triggers separate query for each employee
}

// ✅ GOOD - Use Fetch Join
@Query("SELECT e FROM Employee e " +
       "JOIN FETCH e.department d " +
       "WHERE e.active = true")
List<Employee> findAllWithDepartment();

// ✅ GOOD - Use @EntityGraph
@EntityGraph(attributePaths = {"department", "designation", "branch"})
@Query("SELECT e FROM Employee e WHERE e.active = true")
List<Employee> findAllWithRelations();
```

### Connection Pool Optimization

```properties
# Optimal HikariCP configuration
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000

# Batch processing
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
```

### Caching Strategy

```java
@Service
@RequiredArgsConstructor
public class DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    private final CacheManager cacheManager;
    
    @Cacheable(value = "departments", key = "#id")
    public DepartmentDTO getDepartment(Long id) {
        return departmentRepository.findById(id)
            .map(this::toDTOsyntax)
            .orElseThrow();
    }
    
    @CachePut(value = "departments", key = "#result.id")
    public DepartmentDTO updateDepartment(DepartmentDTO dto) {
        // Update logic
        return dto;
    }
    
    @CacheEvict(value = "departments", allEntries = true)
    public void evictAllDepartments() {
        // Called when departments change significantly
    }
}
```

---

## Security Best Practices

### JWT Authentication Implementation

```java
@Component
public class JwtUtils {
    
    @Value("${app.jwtSecret}")
    private String jwtSecret;
    
    @Value("${app.jwtExpirationMs}")
    private long jwtExpirationMs;
    
    public String generateJwtToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
    
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature");
        }
        return false;
    }
}
```

### Password Security

```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // 12 rounds
    }
    
    // Usage in service
    @Service
    public class UserService {
        @Autowired
        private PasswordEncoder passwordEncoder;
        
        public void registerUser(String username, String password) {
            String encodedPassword = passwordEncoder.encode(password);
            // Save user with encoded password
        }
    }
}
```

### CORS Configuration

```java
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://yourdomain.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

---

## CI/CD & Deployment

### GitHub Actions Workflow

Create `.github/workflows/build-deploy.yml`:

```yaml
name: Build and Deploy

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  build:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:12
        env:
          POSTGRES_DB: idayam_test
          POSTGRES_USER: test_user
          POSTGRES_PASSWORD: test_pass
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
        distribution: 'adopt'
    
    - name: Build with Maven
      run: mvn clean package
    
    - name: Run Tests
      run: mvn test
    
    - name: SonarQube Scan
      uses: SonarSource/sonarcloud-github-action@master
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
    
    - name: Build Docker Image
      run: docker build -t idayam-erp:latest .
    
    - name: Push to Registry
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker push ${{ secrets.DOCKER_REGISTRY }}/idayam-erp:latest
```

### Deployment to Production

```bash
#!/bin/bash
# deploy.sh - Production deployment script

# Build application
mvn clean package -Pprod

# Stop running container
docker stop idayam-app-prod || true
docker rm idayam-app-prod || true

# Pull latest image
docker pull registry.example.com/idayam-erp:latest

# Run new container
docker run -d \
  --name idayam-app-prod \
  -p 8080:8080 \
  --restart unless-stopped \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db.example.com:5432/idayam_erp \
  -e SPRING_DATASOURCE_USERNAME=${DB_USER} \
  -e SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD} \
  -v /data/logs:/app/logs \
  registry.example.com/idayam-erp:latest

# Check health
sleep 10
curl http://localhost:8080/actuator/health
```

---

## Troubleshooting

### Common Development Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Port 8080 in use | Another app using port | Change port in application.properties or kill process using lsof/netstat |
| Database connection refused | PostgreSQL not running | Start PostgreSQL service or check credentials |
| Maven build fails | Dependency conflict | Run `mvn dependency:tree` and resolve conflicts |
| Tests fail in IDE but pass on CLI | IDE cache | Invalidate IDE caches and restart |
| Hot reload not working | DevTools not configured | Add spring-boot-devtools dependency |

### Debug Mode Tips

```bash
# Enable Spring Boot debug output
mvn spring-boot:run -Ddebug

# Enable SQL logging
# Set in application.properties:
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Performance Diagnostics

```bash
# Monitor thread usage
jps -l  # List running JVMs
jthreadm PID

# Heap dump analysis
jheapdump PID dump.hprof

# Monitor garbage collection
jstat -gc PID 1000  # Update every 1 second
```

---

## Advanced Topics

### Advanced Caching with Redis (Optional)

```xml
<!-- Add Redis dependency to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```properties
# Redis configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.database=0
spring.cache.type=redis
```

### Async Processing with @Async

```java
@Service
public class NotificationService {
    
    @Async
    public void sendEmailAsync(String email, String subject, String body) {
        // Email sending happens in background thread
        // Does not block the main request
    }
}
```

### Scheduled Jobs with @Scheduled

```java
@Service
public class MaintenanceScheduler {
    
    @Scheduled(fixedRate = 60000)  // Every 60 seconds
    public void cleanupOldRecords() {
        // Cleanup logic
    }
    
    @Scheduled(cron = "0 2 * * *")  // Daily at 2 AM
    public void generateDailyReports() {
        // Report generation
    }
}
```

### Custom Validation Annotations

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface PhoneNumber {
    String message() default "Invalid phone number format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.matches("[0-9\\-\\+]{10,}");
    }
}
```

---

## Additional Resources

### Documentation
- [Spring Boot Official Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Spring Data JPA Reference](https://spring.io/projects/spring-data-jpa)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

### Tools
- **IntelliJ IDEA**: https://www.jetbrains.com/idea/
- **Maven**: https://maven.apache.org/
- **PostgreSQL**: https://www.postgresql.org/download/
- **Postman**: https://www.postman.com/
- **SonarQube**: https://www.sonarqube.org/

### Useful Commands

```bash
# Project analysis
mvn clean compile

# Generate test coverage report
mvn clean test jacoco:report

# Check for dependency vulnerabilities
mvn org.owasp:dependency-check-maven:check

# Format code according to standards
mvn spotless:apply

# Generate project documentation
mvn site
```

---

**Last Updated**: April 22, 2026
**Maintained By**: Idayam Development Team
**Review Date**: July 22, 2026

---

### Issue: Java Version Mismatch
```bash
# Solution 1: Check Java version
java -version

# Solution 2: Set JAVA_HOME
# Windows:
set JAVA_HOME=C:\Program Files\Java\jdk-11

# Linux/Mac:
export JAVA_HOME=/usr/libexec/java_home -v 11
```

### Issue: Maven Dependencies Not Downloaded
```bash
# Clear local Maven cache
mvn clean

# Remove .m2 folder and retry
rm -rf ~/.m2/repository
mvn clean install
```

### Issue: Database Connection Error
```properties
# Check connection parameters in application.properties
# PostgreSQL Connection:
# - Default port: 5432
# - Ensure PostgreSQL service is running
# - psql connection string: psql -h localhost -U idayam_user -d idayam_erp
# - Enable TCP/IP

# Test connection:
# sqlcmd -S SERVER -U USER -P PASSWORD -d DATABASE
```

### Issue: Port 8080 Already in Use
```bash
# Find process using port
# Windows: netstat -ano | findstr :8080
# Linux: lsof -i :8080

# Change port in application.properties
server.port=8081
```

### Issue: JWT Token Related
```properties
# Ensure JWT secret is set and at least 32 characters
app.jwtSecret=your_long_secret_key_with_sufficient_length_for_security

# Increase expiration if needed (milliseconds)
app.jwtExpirationMs=86400000
```

### Issue: File Upload Size Exceeded
```properties
# Increase max file size in application.properties
spring.servlet.multipart.max-file-size=2048MB
spring.servlet.multipart.max-request-size=2048MB
```

---

## Project Structure Details

### Layer Architecture

```
Controller Layer
    ├── Handles HTTP requests
    ├── Request validation
    ├── Response formatting
    └── Authentication check

        ↓

Service Layer
    ├── Business logic
    ├── Calculations
    ├── Data processing
    └── Transaction management

        ↓

DAO Layer (Repositories)
    ├── Database queries
    ├── Entity persistence
    └── Query building

        ↓

Database Layer
    ├── PostgreSQL Database
    └── Data storage
```

### Key Directories

| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/idayam` | Main Java source code |
| `src/main/java/com/idayam/controller` | REST Controllers (106 files) |
| `src/main/java/com/idayam/service` | Business logic services |
| `src/main/java/com/idayam/dao` | Repository interfaces |
| `src/main/java/com/idayam/model` | JPA Entity classes |
| `src/main/java/com/idayam/dto` | Data Transfer Objects |
| `src/main/java/com/idayam/config` | Security and framework config |
| `src/main/java/com/idayam/utils` | Utility classes |
| `src/main/resources` | Application properties and templates |
| `src/test` | Unit and integration tests |

### Naming Conventions

**Controllers:** `{Feature}Controller.java`
- Example: `UserController`, `OrderController`
- Pattern: `@RestController` with `@RequestMapping("/api")`

**Services:** `{Feature}Service.java`
- Business logic implementation
- Marked with `@Service`

**Repositories (DAO):** `{Feature}Repository.java`
- Extend `JpaRepository<Entity, ID>`
- Marked with `@Repository`

**Models (Entities):** `{Feature}.java`
- JPA entity classes
- Marked with `@Entity` and `@Table`

**DTOs:** `{Feature}Dto.java`
- Data transfer objects for API
- Used for request/response payloads

---

## Best Practices

### 1. Error Handling
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<?> handleException(Exception e) {
    ErrorResponse error = new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        e.getMessage(),
        LocalDateTime.now()
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
}
```

### 2. Logging
```java
private static final Logger logger = LoggerFactory.getLogger(YourController.class);

logger.info("Processing request: {}", requestId);
logger.error("Error occurred: ", exception);
```

### 3. Transaction Management
```java
@Service
@Transactional
public class YourService {
    // Methods automatically wrapped in transactions
}
```

### 4. Dependency Injection
```java
@Autowired
private YourRepository repository;
// OR
private final YourRepository repository;

public YourService(YourRepository repository) {
    this.repository = repository;  // Constructor injection preferred
}
```

---

## Knowledge Resources

- **Spring Boot Documentation:** https://spring.io/projects/spring-boot
- **Spring Data JPA:** https://spring.io/projects/spring-data-jpa
- **Spring Security:** https://spring.io/projects/spring-security
- **Maven Documentation:** https://maven.apache.org/
- **PostgreSQL Documentation:** https://www.postgresql.org/docs/

---

## Next Steps

1. Set up your IDE (IntelliJ, Eclipse, or VS Code)
2. Configure `application.properties` with your database
3. Run `mvn clean install` to download dependencies
4. Start application with `mvn spring-boot:run`
5. Access API docs at `http://localhost:8080/swagger-ui.html`
6. Read `CONTROLLER_DETAILED_DOCUMENTATION.md` for API details
