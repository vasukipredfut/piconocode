# Idayam ERP - System Navigation & Basic Operations

## Complete Guide to Using the System Interface

This manual covers all the common operations and interface elements used throughout the Idayam ERP system.

---

## 1. System Interface Overview

### Main Screen Layout

```
┌─────────────────────────────────────────────────────────────┐
│  Logo  │ Main Menu (Collapsed)  │        [Search]  ⚙️ Profile │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│ ◀ Home > Employees > Edit [Breadcrumb Navigation]            │
│                                                               │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                    Main Content Area                  │  │
│  │                                                       │  │
│  │  Employee Information Form or List View              │  │
│  │                                                       │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                               │
│                                     [Cancel]  [Save]         │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

### Screen Components

| Component | Function |
|-----------|----------|
| **Header** | Application name, search, user menu, settings |
| **Sidebar Menu** | Navigation to all modules |
| **Breadcrumb** | Shows current location and navigation path |
| **Main Content** | Form, list, or report view |
| **Action Buttons** | Save, cancel, delete, print, export |
| **Status Bar** | Displays messages and notifications |

---

## 2. Menu System

### Expanding/Collapsing Menu

1. **Click hamburger icon** (☰) to toggle menu
2. Menu expands to show all options
3. Click again to collapse menu
4. Click on any menu item to navigate

### Menu Organization

```
├── Dashboard
│   └── Quick overview and key metrics
│
├── Human Resources (HR)
│   ├── Employees (Employee master data)
│   ├── Attendance (Daily attendance tracking)
│   ├── Leave Management (Leave applications and approvals)
│   └── Payroll (Salary processing)
│
├── Inventory
│   ├── Products (Product catalog)
│   ├── Stock Management (Stock levels and movements)
│   ├── Warehouses (Warehouse management)
│   └── Stock Movements (Stock transaction history)
│
├── Procurement
│   ├── Purchase Orders (PO creation and tracking)
│   ├── Vendors (Vendor master data)
│   ├── Purchase Requisitions (PR creation)
│   └── Goods Receipt (GRN creation)
│
├── Sales
│   ├── Sales Orders (Order creation and management)
│   ├── Customers (Customer master data)
│   ├── Deliveries (Delivery tracking)
│   └── Sales Returns (Return management)
│
├── Finance
│   ├── Vouchers (Voucher creation and approval)
│   ├── Journal Entries (Manual journal entries)
│   ├── Accounts (Chart of accounts)
│   └── Financial Reports (Reports and analysis)
│
├── Reports
│   ├── Sales Reports
│   ├── Inventory Reports
│   ├── Financial Reports
│   └── HR Reports
│
└── Settings
    ├── My Profile (User profile)
    ├── Change Password (Password management)
    ├── My Permissions (View assigned permissions)
    └── System Settings (Admin only)
```

### Breadcrumb Navigation

Located at the top of the content area:
- Shows your current location in the system
- Example: `Home > Procurement > Purchase Orders > Create`
- Click any breadcrumb to navigate to that section
- Last breadcrumb shows current page

---

## 3. List View Operations

### Understanding List Views

List views display all records in a table format with filters, search, and action options.

```
┌─────────────────────────────────────────────────────────────┐
│ Purchase Orders                                 [+ New]      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│ Search: [Search PO...]  ▼                                   │
│                                                              │
│ Filters: Status [All ▼]  Vendor [All ▼]  Date Range ▼    │
│         [Apply Filters]  [Clear]                           │
│                                                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│ □ PO#    │ Vendor      │ Amount  │ Status    │ Date       │
│   ────   │ ──────────  │ ────── │ ──────   │ ──────     │
│ ☐ PO001  │ ABC Vendors │ $5,000 │ Approved │ 01/04/24   │
│ ☐ PO002  │ XYZ Traders │ $8,500 │ Pending  │ 02/04/24   │
│ ☐ PO003  │ ABC Vendors │ $3,200 │ Draft    │ 03/04/24   │
│                                                              │
│ Showing 1-3 of 125 records                                  │
│ Records per page: [10 ▼]  [◄ Prev]  [1] [2] [3]  [Next ►] │
│                                                              │
│ [Export to Excel]  [Export to PDF]  [Print]                │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Search Function

**Simple Search**:
1. Enter search term in **Search** field
2. Results filter automatically as you type
3. Searches across all visible columns

**Advanced Search**:
1. Click **[Advanced Search]** button (if available)
2. Set multiple criteria
3. Click **[Search]** to apply

### Filtering Records

1. Click **Filter dropdown** (funnel icon or field name)
2. Select filter criteria:
   - By status (Active, Inactive, Draft, Approved)
   - By date range (From date - To date)
   - By department, vendor, or other categories
3. Click **[Apply Filters]**
4. Click **[Clear]** to remove filters

### Sorting Records

1. Click **column header** to sort ascending
2. Click again to sort descending
3. Arrow (↑↓) indicates sort direction
4. Multi-column sort available (Shift + Click)

### Pagination (Page Navigation)

- **Records per page**: Select from dropdown (10, 25, 50, 100)
- **Page buttons**: Click [1], [2], [3]... to navigate
- **Next/Previous**: Use [◄] and [►] buttons
- **Record count**: Shows "Showing 1-10 of 500 records"

### Bulk Actions

1. **Check boxes** next to records to select
2. Use **Select All** checkbox to select all on page
3. Choose action from **Bulk Actions** menu:
   - Delete (multiple records)
   - Change Status
   - Assign to User
   - Generate Report
4. Click **[Apply]** to execute

---

## 4. Form & Data Entry

### Understanding Forms

Forms are used to create and edit records with multiple fields.

```
┌──────────────────────────────────────────────────┐
│ Create New Employee                        [x]   │
├──────────────────────────────────────────────────┤
│                                                  │
│ Personal Information                             │
│                                                  │
│ First Name *:      [____________]                │
│ Last Name *:       [____________]                │
│ Email Address *:   [____________]                │
│ Phone Number:      [____________]                │
│                                                  │
│ Employment Information                           │
│                                                  │
│ Employee Code *:   [____________]                │
│ Department *:      [Select Dept ▼]               │
│ Designation *:     [Select Desig ▼]              │
│ Join Date *:       [DD/MM/YYYY] 📅              │
│ Salary:            [____________]                │
│                                                  │
│ [ ] Active  [ ] Send Welcome Email               │
│                                                  │
│                                                  │
│          [Cancel]  [Save]  [Save & New]         │
│                                                  │
└──────────────────────────────────────────────────┘
```

### Field Types

| Field Type | Interaction | Example |
|-----------|-------------|---------|
| **Text** | Type text directly | Name, Code, Email |
| **Number** | Type numbers only | Amount, Quantity |
| **Date** | Click calendar icon or type | 01/04/2024 |
| **Dropdown** | Click to expand options | Department, Status |
| **Checkbox** | Click to toggle | Active, Send Email |
| **Radio** | Click to select one | Gender, Type |
| **Textarea** | Type multi-line text | Description, Notes |
| **Attachment** | Click to upload file | Document, Image |

### Required vs Optional Fields

- **Required fields** marked with red ***** asterisk
  - **Clarification**: These MUST be filled before you can save
  - **Error message**: "This field is required" appears if you try to save without filling it
  - **How to identify**: Look for the red asterisk (*)
  
- **Optional fields** can be left blank
  - **Clarification**: You can save the form without filling these
  - **When to use**: Fill only if applicable to your situation
  - **Example**: Middle name is often optional

### Form Validation & Error Messages

**What is validation?**
- System checks that data entered is in correct format
- Example: Email must be in format name@domain.com

**Common validation errors**:

| Error Message | Meaning | Solution |
|---------------|---------|----------|
| "This field is required" | You left a required field empty | Fill in all fields marked with * |
| "Invalid email format" | Email doesn't look like real email | Check format: name@domain.com |
| "This value already exists" | You entered a duplicate | Use a unique value (codes must be unique) |
| "Please enter valid number" | You typed letters in a number field | Delete and type numbers only |
| "Date must be in future" | You entered a past date when future needed | Click calendar and select correct date |
| "Field cannot exceed 50 characters" | You typed too much text | Delete some characters |

**How to fix validation errors**:
1. Read the error message carefully
2. Locate the field with red border (error field)
3. Correct the entry based on error message
4. Click Save again

### Form Buttons Explained

- **[Save]**: Saves the form and closes it. Use this when done entering data.
- **[Save & New]**: Saves form and opens a new empty form. Use when entering many records.
- **[Cancel]**: Closes form without saving. Any unsaved changes are lost.
- **[Save & Continue]**: Saves and keeps form open for more edits.

**Important**: 
- Unsaved changes are LOST if you close without clicking Save
- Browser won't warn you, so be careful
- If you accidentally close, you'll have to re-enter the data

### Data Entry Tips

1. **Tab key**: Move to next field
2. **Shift + Tab**: Move to previous field
3. **Enter**: Submit form (if enabled)
4. **Escape**: Cancel and close form
5. **Ctrl + S**: Save form quickly

### Date Selection

1. Click **Calendar icon** next to date field
2. Select month and year from header
3. Click **day** to select date
4. Or type manually: `DD/MM/YYYY` or `01/04/2024`

### Dropdown Selection

1. Click on **dropdown field**
2. Select from list
3. Or type to search/filter options
4. Press **Enter** or click to confirm

### File Upload

1. Click on **Upload area** or **[Choose File]** button
2. Select file from your computer
3. File name appears when selected
4. Click **[Upload]** to confirm
5. Multiple files: Some forms allow multiple uploads

---

## 5. Buttons & Actions

### Standard Button Actions

| Button | Function |
|--------|----------|
| **[Save]** | Save current form and stay on form |
| **[Save & Close]** | Save and close form |
| **[Save & New]** | Save and open blank form for next entry |
| **[Cancel]** | Discard changes and close form |
| **[Delete]** | Delete current record (with confirmation) |
| **[Edit]** | Switch form to edit mode |
| **[View]** | Display record in read-only view |
| **[Print]** | Print current record |
| **[Export]** | Export to Excel/PDF |
| **[Approve]** | Approve transaction (if authorized) |
| **[Reject]** | Reject transaction with comment |
| **[Submit]** | Submit for approval |
| **[+New]** | Create new record |
| **[Search]** | Open search/filter panel |
| **[Reset]** | Clear form and start over |

### Dynamic Buttons

Some buttons appear only in certain conditions:
- **[Approve]**: Only for pending records
- **[Reject]**: Only for pending records
- **[Edit]**: Only in view mode
- **[Delete]**: Only if permitted by role

---

## 6. Common Dialog Boxes

### Confirmation Dialogs

```
┌────────────────────────────────┐
│        Confirm Action          │
├────────────────────────────────┤
│                                │
│  Are you sure you want to      │
│  delete this record?           │
│                                │
│  This action cannot be undone. │
│                                │
│        [No]  [Yes, Delete]    │
│                                │
└────────────────────────────────┘
```

**Actions requiring confirmation**:
- Delete record
- Approve transaction
- Reject application
- Cancel order

### Success Message

```
┌────────────────────────────────┐
│  ✓ Record saved successfully   │
│  Purchase Order #PO001 created │
└────────────────────────────────┘
```

### Error Message

```
┌────────────────────────────────┐
│  ✗ Error saving record         │
│  Please fill all required      │
│  fields and try again.         │
│                                │
│          [Close]  [Retry]      │
│                                │
└────────────────────────────────┘
```

### Validation Messages

- **Red text** below field: Field validation error
- **Orange warning**: Important information
- **Blue info**: Helpful information

---

## 7. Notifications & Messages

### Message Types

| Type | Color | Meaning |
|------|-------|---------|
| **Success** | Green ✓ | Action completed successfully |
| **Error** | Red ✗ | Action failed, error occurred |
| **Warning** | Orange ⚠ | Important attention needed |
| **Info** | Blue ℹ | Informational message |

### Notification Panel

1. **Bell icon** in top-right corner
2. Shows recent notifications
3. Click notification to navigate to related record
4. Mark as read by clicking
5. Clear old notifications

### In-Form Messages

- **Field error**: Red text below field
- **Required field**: Marked with red *
- **Success**: Green checkmark appears
- **Loading**: Spinner while processing

---

## 8. Report Viewing

### Report Types

1. **List Reports**: Tabular data with filters
2. **Summary Reports**: Statistical summaries
3. **Detailed Reports**: Comprehensive information
4. **Chart Reports**: Visual graphs and dashboards

### Report Operations

```
┌──────────────────────────────────────────┐
│ Sales Report - Q1 2024                   │
├──────────────────────────────────────────┤
│ Filters: Region [All ▼]  Month [All ▼]   │
│ [Apply Filters]  [Clear]  [Refresh]      │
│                                          │
│ Chart View │ Table View │ Summary        │
│                                          │
│ [Print]  [Export]  [Email]  [Schedule]  │
│                                          │
│ Sales by Region                          │
│ ┌─────────────────────────────────────┐ │
│ │  Region A    ████████ 40%           │ │
│ │  Region B    ██████ 30%             │ │
│ │  Region C    ████ 20%               │ │
│ │  Region D    ██ 10%                 │ │
│ └─────────────────────────────────────┘ │
│                                          │
│ Monthly Sales Trend                      │
│ [Line graph showing trend]               │
│                                          │
└──────────────────────────────────────────┘
```

### Exporting Reports

1. Click **[Export]** button
2. Choose format:
   - Excel (.xlsx)
   - PDF (.pdf)
   - CSV (.csv)
3. Click **[Download]**
4. File downloads to your computer

### Scheduling Reports

1. Click **[Schedule]** button
2. Set frequency:
   - Daily
   - Weekly
   - Monthly
3. Select **recipients**
4. Choose **format** and **delivery method**
5. Click **[Schedule]**

---

## 9. User Profile & Settings

### Accessing Your Profile

1. Click **Profile icon** (top-right corner)
2. Select **My Profile**
3. View and edit your information

### Editing Profile

1. Click **[Edit]** button
2. Modify allowed fields:
   - First Name
   - Last Name
   - Email
   - Phone
   - Photo/Avatar
3. Click **[Save]**

### Changing Password

1. Go to **Settings** → **Change Password**
2. Enter **Current Password**
3. Enter **New Password** (min 12 characters)
4. Confirm **New Password**
5. Click **[Change Password]**

### Managing Preferences

1. Go to **Settings** → **Preferences**
2. Set:
   - **Language**: English, Spanish, Hindi
   - **Date Format**: DD/MM/YYYY or MM/DD/YYYY
   - **Time Zone**: Select your timezone
   - **Records per page**: Default page size
   - **Theme**: Light or Dark mode
3. Click **[Save Preferences]**

---

## 10. Search & Find Features

### Global Search

1. Press **Ctrl + F** or click search icon
2. Type search term
3. Results appear in dropdown
4. Click result to navigate

### Advanced Search

1. Go to specific module
2. Click **[Advanced Search]**
3. Set multiple criteria
4. Click **[Search]**

### Saved Searches

1. Create a search with filters
2. Click **[Save Search]**
3. Name your search
4. Click **[Save]**
5. Access saved searches from dropdown

---

## 11. Printing & Exporting

### Printing Records

1. Open the record
2. Click **[Print]** button
3. Select **printer** or **Print to PDF**
4. Adjust settings if needed
5. Click **[Print]**

### Exporting Data

**Export from list view**:
1. Apply filters if desired
2. Click **[Export]** button
3. Select format: Excel, PDF, CSV
4. Click **[Export]**
5. File downloads automatically

**Export from report**:
1. View report
2. Click **[Export]**
3. Choose format
4. Click **[Download]**

---

## 12. Best Practices for Navigation

### Navigation Tips

1. **Use menu**: Always navigate through menu, not browser back button
2. **Bookmark pages**: Bookmark frequently used modules
3. **Use search**: Quick find with global search (Ctrl+F)
4. **Follow breadcrumbs**: Use breadcrumb to navigate back
5. **Save frequently**: Don't lose work by refreshing unexpectedly

### Performance Tips

1. **Close unused tabs**: Reduces browser memory usage
2. **Clear cache**: Menu > Settings > Clear Cache
3. **Use latest browser**: Works best on updated browsers
4. **Check internet**: Ensure stable connection
5. **Avoid simultaneous editing**: Don't edit same record twice

### Error Prevention

1. **Verify before saving**: Review all data before clicking Save
2. **Read error messages**: Understand what went wrong
3. **Use required fields**: Don't skip mandatory fields
4. **Check permissions**: Verify you can perform action
5. **Confirm destructive actions**: Always confirm deletions

---

## Quick Reference Guide

### Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Search | Ctrl + F |
| Save | Ctrl + S |
| New Record | Ctrl + N |
| Close Dialog | Escape |
| Tab to Next | Tab |
| Tab to Previous | Shift + Tab |

### Common Navigation Paths

| Task | Path |
|------|------|
| View Employees | Menu > HR > Employees |
| Create PO | Menu > Procurement > Purchase Orders > [+ New] |
| View Orders | Menu > Sales > Sales Orders |
| Check Stock | Menu > Inventory > Stock Management |
| Process Payroll | Menu > HR > Payroll |

---

**Document Version**: 1.0
**Last Updated**: April 22, 2026
**Next Review**: April 22, 2027
