# Idayam ERP - HR Module User Manual

## Complete Guide to Human Resources Management

This manual covers all HR operations including employee management, attendance, leaves, and payroll.

---

## 1. HR Module Overview

### HR Module Features

The HR module manages all employee-related operations:

```
HR Module
├── Employees Management
│   ├── Employee Records
│   ├── Personal Information
│   ├── Employment Details
│   └── Document Management
│
├── Attendance Management
│   ├── Daily Attendance
│   ├── Attendance Reports
│   ├── Leave Tracking
│   └── Shift Management
│
├── Leave Management
│   ├── Leave Applications
│   ├── Leave Approvals
│   ├── Leave Balance
│   └── Leave Types
│
└── Payroll
    ├── Salary Structure
    ├── Payroll Processing
    ├── Salary Slips
    └── Attendance-based Deduction
```

### Accessing HR Module

1. Click **HR** in main menu
2. Select desired sub-module:
   - **Employees**
   - **Attendance**
   - **Leave Management**
   - **Payroll**

---

## 2. Employee Management

### Employee Record Structure

Each employee record contains:

**Personal Information**:
- First Name, Last Name
- Email, Phone Number
- Date of Birth, Gender
- Address, City, State

**Employment Information**:
- Employee Code (unique)
- Department, Designation
- Branch Assignment
- Join Date, Salary

**Documents**:
- Resume, Certificates
- ID Proof, Address Proof
- Employment Agreement

### Creating New Employee

1. Go to **HR** > **Employees**
   - **Path**: Click "HR" in left menu → Click "Employees"
   
2. Click **[+ New Employee]**
   - **Location**: Top right corner, green button
   - **Result**: New form opens for data entry

3. Fill **Personal Information**:
   - **First Name*** (required): Employee's given name
     - Example: John
     - Clarification: This is the first part of their name
     
   - **Last Name*** (required): Employee's family name
     - Example: Smith
     
   - **Email*** (required): Official company email
     - Format: firstname.lastname@company.com
     - Important: System uses this for notifications
     - Must be unique (no two employees with same email)
     
   - **Phone Number**: Contact number
     - Format: +1-555-123-4567 or (555) 123-4567
     - Optional but recommended
     
   - **Gender**: Select from dropdown
     - Options: Male, Female, Other, Prefer not to say
     
   - **Date of Birth**: Click calendar to select
     - Format: DD/MM/YYYY
     - Used for: Age calculation, anniversary tracking

4. Fill **Employment Information**:
   - **Employee Code*** (required): Unique identifier
     - **Clarification**: Each employee needs a unique code
     - **Auto-generated**: Leave blank to auto-generate (recommended)
       - System generates: EMP-001, EMP-002, etc.
     - **Manual entry**: Enter custom code if needed
       - Example: EMP-JOHN-001
       - Rule: Must be unique, no duplicates allowed
       - Error message if duplicate: "This employee code already exists"
       
   - **Department*** (required): Select from dropdown
     - Examples: Sales, HR, IT, Operations, Finance
     - Clarification: Where the employee will work
     - Used for: Reporting, salary processing, access control
     
   - **Designation*** (required): Job title
     - Examples: Manager, Executive, Analyst, Coordinator
     - Clarification: The employee's position/role
     
   - **Branch*** (required): Office location
     - Examples: Head Office, Branch A, Branch B
     - Clarification: Physical location where employee works
     
   - **Join Date*** (required): When employee starts
     - Format: DD/MM/YYYY
     - Important: Cannot be a future date
     - Used for: Salary calculation, anniversary records
     
   - **Salary**: Monthly salary amount
     - Format: Numeric value (e.g., 50000)
     - Optional: Can be updated later
     - Used for: Payroll processing, reports

5. **Optional**: Upload documents
   - Click **[Choose Files]** or drag-drop
   - Supported types: PDF, DOC, JPG, PNG
   - Maximum file size: 5MB per file
   - Types to upload: Resume, Certificates, ID, Address Proof

6. **Activation Status**:
   - Check **[Active]** checkbox to mark employee as active
   - Clarification: Active employees appear in all lists
   - Inactive employees are archived (hidden from lists)

7. Click **[Save]**
   - **What happens**: Employee record created, success message shown
   - **Next step**: You can now assign roles, permissions, or training

### Viewing Employee Details

1. Navigate to **HR** > **Employees**
2. **Search** or **filter** to find employee
3. Click employee name to view details
4. Information displayed in read-only mode
5. Click **[Edit]** to modify

### Editing Employee Information

1. Open employee record
2. Click **[Edit]** button
3. Modify desired fields
4. Click **[Save]** to update
5. Changes reflected immediately

### Deactivating Employee

1. Open employee record
2. Click **[Edit]**
3. Uncheck **[Active]** checkbox
4. Enter **Deactivation Date**
5. Enter **Reason for Deactivation**
6. Click **[Save]**
7. Employee no longer appears in active lists

### Employee Reports

**Available Reports**:
1. **Employee Directory**: List of all employees
2. **Department-wise**: Employees by department
3. **Designation-wise**: Employees by position
4. **Branch-wise**: Employees by location
5. **Joining Report**: Employees by join date range
6. **Salary Analysis**: Salary statistics by department

**Generate Report**:
1. Go to **Reports** > **HR Reports**
2. Select report type
3. Set filters (Date range, Department, etc.)
4. Click **[Generate]**
5. View, print, or export report

---

## 3. Attendance Management

### Attendance Types

| Status | Code | Meaning | Salary Impact | When to Use |
|--------|------|---------|---------------|-------------|
| **Present** | P | Employee worked full day | No deduction | Employee came and worked full 8 hours |
| **Absent** | A | Employee didn't come | Full day deduction (100%) | Employee didn't come or notify |
| **Half Day** | HD | Employee came for half day | Half day deduction (50%) | Employee came but left early or arrived late |
| **Leave** | L | On approved leave | No deduction if approved | Employee took approved vacation/sick leave |
| **Holiday** | H | Public holiday | No deduction | National/company holiday (office closed) |

**Clarifications**:
- **Present (P)**: Normal working day - no salary impact
- **Absent (A)**: Employee not present without leave approval - salary deducted
- **Half Day (HD)**: Employee present for part of day (≤4 hours worked)
  - Partial salary deduction
  - Requires employee to work at least 4 hours to mark as present
  - If worked less than 4 hours, mark as half day
- **Leave (L)**: Employee took approved leave (vacation, sick, etc.)
  - Only if leave was pre-approved
  - Salary NOT deducted (covered by leave policy)
- **Holiday (H)**: Office closed for public/company holiday
  - Automatically marked for all employees
  - No salary deduction
  - No individual approval needed

**Important Notes**:
- One attendance record per employee per day
- Cannot mark same day twice
- If need to correct, click [Edit] and update
- System calculates salary deductions automatically based on status

### Recording Attendance

**Method 1: Manual Entry**

1. Go to **HR** > **Attendance**
   - **Path**: HR menu → Click "Attendance"
   
2. Click **[+ Mark Attendance]**
   - **Location**: Top right corner, green button
   
3. Select **Date**
   - **Clarification**: Click calendar to choose date
   - **Note**: Can mark current day or past days (usually up to 30 days back)
   - **Cannot**: Mark future dates
   
4. Select **Employee** or **Department**
   - **Option A - Single Employee**: 
     - Click dropdown and type employee name
     - Select from list
     - Used for: Marking one employee at a time
   - **Option B - Entire Department**:
     - Click "Mark for Department" option
     - Select department
     - Marks all active employees in that department
     - Used for: Marking entire team at once
     
5. Choose **Status**: Present, Absent, Half Day, Leave, Holiday
   - **Clarification**: Select what happened that day
   
6. Enter **Check-in Time** and **Check-out Time** (if Present)
   - **Format**: HH:MM (24-hour format)
   - **Example**: 09:00 (for 9 AM), 17:30 (for 5:30 PM)
   - **Important**: Check-out time must be after check-in time
   - **System calculates**: Working hours automatically
   
7. **Calculate Working Hours** (auto-calculated)
   - **Clarification**: System automatically calculates based on times entered
   - **Example**: 09:00 to 17:30 = 8.5 hours
   - **Important**: This affects salary deduction calculations
   
8. Add **Notes** (optional)
   - **Use for**: Comments like "Arrived late due to traffic", "Left early for meeting"
   - **Visible to**: Manager and HR team
   
9. Click **[Save]**
   - **What happens**: Attendance recorded, success message shown

**Method 2: Bulk Upload**

1. Go to **HR** > **Attendance**
2. Click **[Bulk Upload]**
   - **When to use**: Marking attendance for 10+ employees at once
   
3. Download **Excel template**
   - **Contains columns**:
     - Employee Code
     - Date
     - Status
     - Check-in Time
     - Check-out Time
     - Notes
     
4. Fill attendance data in Excel:
   - Row 1: Headers (already filled)
   - Row 2+: Enter data for each employee
   - **Example**:
     - EMP-001, 01/04/2024, P, 09:00, 17:30, -
     - EMP-002, 01/04/2024, A, -, -, Sick leave (unapproved)
     
5. Upload completed file
   - **Supported format**: .xlsx (Excel 2007+)
   - **Maximum size**: 5MB, max 1000 rows
   
6. Verify data in preview
   - **Check for**: Errors, invalid codes, duplicate entries
   - **Fix errors**: Delete rows and re-upload if needed
   
7. Click **[Confirm Upload]**
   - **System**: Processes all rows and saves attendance records
   - **What you see**: "Successfully uploaded 50 attendance records"

### Attendance Report

1. Go to **Reports** > **HR Reports** > **Attendance Report**
2. Set filters:
   - **Date Range**: From date - To date
   - **Department**: Select department
   - **Employee**: Select specific employee (optional)
3. Click **[Generate Report]**
4. View report with:
   - Daily status for each employee
   - Total present, absent, leave days
   - Working hours
5. **Export** to Excel or **Print**

---

## 4. Leave Management

### Leave Types

| Leave Type | Purpose | Maximum Days |
|-----------|---------|--------------|
| **Casual Leave (CL)** | Personal reasons | 12/year |
| **Sick Leave (SL)** | Medical reasons | 8/year |
| **Annual Leave (AL)** | Vacation | 21/year |
| **Unpaid Leave (UL)** | Without pay | As per policy |
| **Maternity** | Post-pregnancy | 180 days |
| **Paternity** | Newborn care | 15 days |

### Applying for Leave

**Employee Applies for Leave**:

1. Go to **HR** > **Leave Management**
2. Click **[Apply for Leave]**
3. Fill **Leave Details**:
   - **Leave Type** *: Select from dropdown
   - **From Date** *: First day of leave
   - **To Date** *: Last day of leave
   - **Number of Days**: Auto-calculated
   - **Reason** *: Reason for leave
   - **Attach Documents**: Upload medical certificate if needed
4. Click **[Submit]**
5. Application sent to manager for approval

### Leave Application Status

**Pending**: Waiting for approval
**Approved**: Manager approved, leave confirmed
**Rejected**: Manager rejected application
**Cancelled**: Employee cancelled application

### Leave Approval (Manager)

1. Go to **HR** > **Leave Management** > **Leave Approvals**
2. View pending leave applications
3. For each application:
   - Click application to view details
   - Review **Reason** and **Attachments**
   - Check employee's **Leave Balance**
4. **To Approve**:
   - Click **[Approve]**
   - Add comment (optional)
   - Click **[Confirm Approval]**
5. **To Reject**:
   - Click **[Reject]**
   - Enter **Rejection Reason**
   - Click **[Confirm Rejection]**
6. Employee notified automatically

### Leave Balance

**View Leave Balance**:
1. Go to **HR** > **Leave Management** > **My Leave Balance**
2. Shows balance for each leave type:
   - Total entitled: Annual allocation
   - Used: Days already taken
   - Remaining: Available balance
   - Pending: Under approval
3. Update balance annually on hire anniversary

**Leave Balance Report**:
1. Go to **Reports** > **HR Reports** > **Leave Balance Report**
2. Shows all employees' leave balances
3. Filter by department or employee
4. Export to Excel

---

## 5. Payroll Management

### Payroll Structure

**Components**:
- **Basic Salary**: Fixed monthly salary
- **HRA** (House Rent Allowance): Housing assistance
- **DA** (Dearness Allowance): Inflation adjustment
- **Conveyance**: Travel allowance
- **Other Allowances**: Performance, skill-based
- **Deductions**: PF, Tax, Insurance, Others

### Processing Payroll

**Step 1: Verify Attendance**

1. Ensure all **Attendance** is marked for the month
2. Go to **HR** > **Attendance Report**
3. Verify all employees have attendance entries
4. Missing attendance defaults to Absent (salary deducted)

**Step 2: Process Payroll**

1. Go to **HR** > **Payroll**
2. Click **[+ Process Payroll]**
3. Select **Month and Year**
4. Select **Department** or **All Employees**
5. System auto-calculates based on:
   - Attendance records
   - Salary structure
   - Configured deductions
6. Click **[Preview]** to verify calculations
7. Click **[Process]** to generate payroll
8. Salary slips generated for all employees

**Step 3: Review Payroll**

1. Go to **HR** > **Payroll** > **Monthly Payroll**
2. View payroll for month:
   - Employee names
   - Gross salary
   - Deductions
   - Net salary
   - Attendance count
3. Click **employee name** to view detailed breakdown

**Step 4: Approve Payroll**

1. Review payroll accuracy
2. Click **[Approve Payroll]**
3. System updates employee records
4. Salary slips can be generated for distribution

### Salary Slip

**Generate Salary Slip**:

1. Go to **HR** > **Payroll** > **Salary Slips**
2. Select **Month and Year**
3. Click **[Generate Slips]** or select individual employee
4. System creates PDF salary slips

**Salary Slip Contents**:
- Employee name, code, designation
- Pay period (month/year)
- Earnings:
  - Basic Salary
  - HRA, DA, Allowances
  - Total Earnings
- Deductions:
  - PF (Provident Fund)
  - TDS (Tax Deducted at Source)
  - Insurance, Other Deductions
  - Total Deductions
- **Net Salary**: Earnings - Deductions

**Distribute Salary Slips**:
1. Go to **Salary Slips** list
2. Select employee
3. Click **[Email Slip]**
4. System sends PDF to employee email
5. Or employee logs in to view slip

### Payroll Reports

**Available Reports**:
1. **Monthly Payroll Report**: All employees for month
2. **Salary Analysis**: Salary statistics
3. **Deduction Summary**: Total deductions by type
4. **Attendance Impact**: How attendance affected salary
5. **Employee Salary**: Individual employee salary history

**Generate Payroll Report**:
1. Go to **Reports** > **HR Reports** > **Payroll Report**
2. Select **Report Type**
3. Set **Month and Year**
4. Apply filters (Department, Designation, etc.)
5. Click **[Generate]**
6. Export to Excel or Print

---

## 6. Common HR Tasks

### Task: Employee Transfer

1. Open employee record
2. Click **[Edit]**
3. Change **Department** and/or **Designation**
4. Update **Salary** if applicable
5. Enter **Effective Date**
6. Click **[Save]**
7. Update recorded in system

### Task: Salary Increment

1. Open employee record
2. Click **[Salary Increment]**
3. Enter **New Salary** amount
4. Enter **Effective Date**
5. Add **Reason** (e.g., Promotion, Annual Increment)
6. Click **[Process Increment]**
7. Payroll automatically uses new salary next month

### Task: Leave Policy Update

1. Go to **HR** > **Leave Management** > **Leave Policies**
2. Click **[Edit Policy]**
3. Update leave types and allocations:
   - Casual Leave: 12 days/year
   - Sick Leave: 8 days/year
   - Annual Leave: 21 days/year
4. Set **Effective Date**
5. Click **[Save]**
6. Applies to all future leave year allocations

### Task: Attendance Exception

1. Open **Attendance** record
2. If attendance marked incorrectly:
   - Click **[Edit]**
   - Correct the status
   - Click **[Save]**
3. Notify HR for payroll adjustment if deduction occurred

---

## 7. HR Reports & Analytics

### Dashboard Metrics

**HR Dashboard** shows:
- Total employees (Active/Inactive)
- New joiners this month
- Employees on leave today
- Pending leave approvals
- Payroll status

### Key Reports

**Employee Related**:
- Employee Directory
- Employee Joining Report
- Attrition Report
- Designation-wise Distribution

**Attendance Related**:
- Daily Attendance Report
- Monthly Attendance Summary
- Absent Employees Report
- Attendance Trends

**Leave Related**:
- Leave Balance Report
- Leave Usage Report
- Pending Leave Approvals
- Leave Trends

**Payroll Related**:
- Monthly Payroll Summary
- Salary Analysis
- Deduction Details
- Payroll Trends

---

## 8. HR Best Practices

### Data Entry Best Practices

1. **Employee Code**: Use unique, meaningful codes
2. **Date Format**: Consistent DD/MM/YYYY format
3. **Salary Entry**: Include all components
4. **Department**: Ensure correct assignment
5. **Documentation**: Keep all documents updated

### Attendance Best Practices

1. **Timely Recording**: Mark attendance daily
2. **Accurate Status**: Use correct leave/absence codes
3. **Regular Audits**: Review attendance regularly
4. **Document Exceptions**: Note special cases
5. **Employee Verification**: Confirm with employee if unsure

### Leave Management Best Practices

1. **Clear Policy**: Communicate leave policies
2. **Balance Tracking**: Monitor leave balances
3. **Documentation**: Require proper documentation for sick leave
4. **Approval Process**: Timely approval/rejection
5. **Planning**: Consider employee coverage in approvals

### Payroll Best Practices

1. **Verification**: Double-check before processing
2. **Timeliness**: Process payroll on schedule
3. **Documentation**: Keep records for compliance
4. **Confidentiality**: Salary information is confidential
5. **Approval Workflow**: Follow approval process

---

## 9. Troubleshooting

### Common Issues

**Issue**: Cannot mark attendance for past month
**Solution**: Use bulk upload feature or contact HR admin

**Issue**: Leave application not visible after submission
**Solution**: Refresh page, check in "My Applications" > "Submitted"

**Issue**: Salary incorrect in payroll
**Solution**: Verify attendance was correctly marked, check salary structure

**Issue**: Employee cannot view salary slip
**Solution**: Check if payroll was approved, verify employee email is correct

---

## 10. Quick Reference

### Navigation Paths

| Task | Path |
|------|------|
| Add New Employee | HR > Employees > [+ New] |
| Mark Attendance | HR > Attendance > [+ Mark] |
| Apply for Leave | HR > Leave Management > [Apply] |
| View Leave Balance | HR > Leave Management > [My Balance] |
| Process Payroll | HR > Payroll > [+ Process] |
| View Salary Slip | HR > Payroll > Salary Slips |

### Important Dates

- Leave allocation: Annual hire anniversary
- Payroll processing: Last day of month
- Payroll approval: By 25th of month
- Salary slips distribution: By 30th of month

---

**Document Version**: 1.0
**Last Updated**: April 22, 2026
**Next Review**: April 22, 2027
