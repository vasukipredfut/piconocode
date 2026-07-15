# Idayam ERP - Getting Started Guide

## Welcome to Idayam ERP

This guide will help you get started with the Idayam ERP system, covering login, initial setup, and basic navigation.

---

## 1. System Access & Login

### Accessing the System

1. **Open your web browser** (Chrome, Firefox, Safari, or Edge)
2. **Enter the system URL**: `http://your-server-address:8080/idayam`
3. **Press Enter** to navigate to the login page

### Login Credentials

When you first access Idayam ERP, you'll see the login screen:

```
┌──────────────────────────────┐
│   Idayam ERP System          │
│   Login                      │
├──────────────────────────────┤
│ Username: [_______________]  │
│ Password: [_______________]  │
│                              │
│    [Login]  [Forgot Pass]   │
└──────────────────────────────┘
```

**Default Credentials** (Change after first login):
- **Admin User**: 
  - Username: `admin`
  - Password: `ComplexPassword123!`
- **Manager User**:
  - Username: `manager`
  - Password: `ComplexPassword123!`
- **Regular User**:
  - Username: `user`
  - Password: `ComplexPassword123!`

### Steps to Login

1. Enter your **Username** in the first field
   - **Clarification**: Username is case-sensitive. If you're unsure of your username, contact your System Administrator
   - **Example**: If your username is "john.smith", entering "John.Smith" will fail
   
2. Enter your **Password** in the second field
   - **Clarification**: Password is also case-sensitive
   - **Note**: Password field will show dots (●●●●●●) for security
   - **If you forgot**: Click **[Forgot Password]** link to reset
   
3. Click the **[Login]** button
   - **Wait for**: Page may take 2-3 seconds to process
   - **Success indicator**: You'll see a brief loading spinner
   - **After successful login**: You'll be redirected to the Dashboard
   
4. **What happens after login**:
   - Dashboard loads with personalized data based on your role
   - Your name appears in top-right corner
   - All modules you have access to appear in the left menu
   - Any pending tasks or notifications appear on dashboard

### Login Troubleshooting

**"Invalid username or password" error**:
- Check spelling of username (case-sensitive)
- Verify CAPS LOCK is not on
- Count the password characters carefully
- Try the username without any leading/trailing spaces
- Contact your System Administrator if you still can't login

**"Account locked" message**:
- This happens after 5 failed login attempts
- Wait 30 minutes before trying again
- Or contact your System Administrator to unlock

**"Session expired" message**:
- Your session timed out (usually after 30 minutes of inactivity)
- Simply login again - no data is lost
- This is a security feature to protect your account

**"Page won't load" or "Connection error"**:
- Check your internet connection
- Verify the server URL is correct
- Try a different browser
- Contact your IT support if problem persists

**Important**: Change your password immediately after first login for security reasons. Use a strong password with at least 12 characters including uppercase, lowercase, numbers, and symbols.

---

## 2. First-Time Login Steps

### Step 1: Password Change (Recommended)
After successful login, you may be prompted to change your password:

1. Go to **Settings** → **Change Password**
   - **Location**: Settings icon in top-right corner next to your profile name
   
2. Enter **Current Password**
   - **Clarification**: This must be your current/old password
   - **Why required**: This confirms you are authorized to change the password
   
3. Enter **New Password** 
   - **Requirements**:
     - Minimum 12 characters (longer is more secure)
     - At least 1 UPPERCASE letter (A-Z)
     - At least 1 lowercase letter (a-z)
     - At least 1 number (0-9)
     - At least 1 special character (!@#$%^&*)
   - **Example of STRONG password**: MyCompany@2024#Secure
   - **Example of WEAK password**: password123 (no uppercase, special chars)
   
4. Confirm **New Password**
   - **Clarification**: Re-enter the same password to confirm
   - **Why required**: To prevent typos that would lock you out
   
5. Click **[Save]**
   - **What happens next**: 
     - Password is updated immediately
     - You'll see success message: "Password changed successfully"
     - Your next login will require the NEW password

**Password Change Tips**:
- Do NOT include your username in the password
- Do NOT use the same password you used before
- Do NOT share your password with anyone
- Write down new password in a secure location (password manager)

### Step 2: Update Profile Information
Complete your user profile:

1. Click your **Profile Icon** (top-right corner)
   - **Clarification**: Shows your initials or avatar
   - **Location**: In the header bar, far right
   
2. Select **My Profile**
   - **Alternative**: You can also go to Settings → Profile
   
3. Update the following fields:
   - **First Name** * (required)
     - Clarification: Enter your given name as it appears in company records
     - Used in: Reports, notifications, and system greetings
     
   - **Last Name** * (required)
     - Clarification: Your family name
     - Used in: Formal reports and documents
     
   - **Email Address** * (required)
     - Clarification: Should be your official company email
     - Important: Notifications and password reset emails go here
     - Must be unique (no two users can have the same email)
     
   - **Phone Number**
     - Format: Country code + number (e.g., +1-555-123-4567)
     - Used for: System notifications (if enabled)
     
   - **Department** (if applicable)
     - Clarification: Select from dropdown list
     - Used for: Permission assignments and reporting structure
     - If your department is not in the list: Contact System Administrator
     
4. Click **[Save Changes]**
   - **What happens next**: 
     - Profile is updated immediately
     - Success message: "Profile updated successfully"
     - You'll see your updated name in the next page refresh

**Profile Tips**:
- Your profile information is used for notifications and reports
- Ensure email is correct so you receive password resets
- Keep department accurate for proper permission assignments
- You cannot change your Username after account creation (contact admin if needed)

### Step 3: Review Your Permissions
Check what modules and features you have access to:

1. Go to **Settings** → **My Permissions**
   - **Clarification**: This shows your role and what you can do
   
2. Review the list of permissions assigned to your role
   - **What you'll see**:
     - Module names (e.g., HR, Inventory, Procurement)
     - Actions you can perform (e.g., Create, Edit, Delete, View)
     - Read-only access vs. full access indicators
   
   - **Understanding permission types**:
     - **View**: Can only see the data (cannot make changes)
     - **Create**: Can add new records
     - **Edit**: Can modify existing records
     - **Delete**: Can remove records (usually restricted)
     - **Report**: Can view and generate reports
   
   - **Example**:
     - ✓ HR Module - View, Create, Edit
     - ✓ Inventory - View only
     - ✗ Finance - No access
     
3. **What if you don't have permission you need?**
   - **Expected behavior**: When you try to access a feature you don't have permission for:
     - Button appears grayed out (disabled)
     - Or you see "Access Denied" message
     - Module may not appear in left menu
   
   - **How to request access**:
     - Contact your Department Manager
     - Or contact your System Administrator
     - They will submit access request form
     - Access is usually granted within 1 business day

**Important Notes**:
- Permissions are based on your Role (Admin, Manager, User, Viewer)
- Your System Administrator controls who gets what permissions
- Permissions are designed to protect sensitive data
- Never share your login credentials to access restricted features

---

## 3. Dashboard Overview

### Dashboard Components

After login, you'll see the main Dashboard with:

```
┌─────────────────────────────────────────────────────┐
│ Idayam ERP                              [Settings] [Logout] │
├─────────────────────────────────────────────────────┤
│                                                      │
│  Welcome, John Smith!                               │
│                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────┐ │
│  │ Total Sales  │  │ Inventory    │  │Pending   │ │
│  │ $125,450     │  │ $45,000      │  │ Orders:5 │ │
│  └──────────────┘  └──────────────┘  └──────────┘ │
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │ Recent Activities                             │  │
│  │ • Order #2024-001 created - 2 hours ago      │  │
│  │ • Stock adjusted for Product ABC - Today     │  │
│  │ • Purchase Order approved - Yesterday        │  │
│  └──────────────────────────────────────────────┘  │
│                                                      │
└─────────────────────────────────────────────────────┘
```

**Key Dashboard Elements**:
- **Quick Stats**: Shows key metrics and KPIs
- **Quick Access**: Shortcuts to frequently used features
- **Recent Activities**: Shows recent transactions and updates
- **Notifications**: Alerts and important messages

---

## 4. Main Navigation Menu

The main menu provides access to all modules:

### Menu Structure

```
Menu
├── Dashboard
├── Human Resources (HR)
│   ├── Employees
│   ├── Attendance
│   ├── Leave Management
│   └── Payroll
├── Inventory
│   ├── Products
│   ├── Stock Management
│   ├── Warehouses
│   └── Stock Movements
├── Procurement
│   ├── Purchase Orders
│   ├── Vendors
│   ├── Purchase Requisitions
│   └── Goods Receipt
├── Sales
│   ├── Sales Orders
│   ├── Customers
│   ├── Deliveries
│   └── Sales Returns
├── Finance
│   ├── Vouchers
│   ├── Journal Entries
│   ├── Accounts
│   └── Reports
├── Reports
│   ├── Sales Reports
│   ├── Inventory Reports
│   ├── Financial Reports
│   └── HR Reports
├── Settings
│   ├── My Profile
│   ├── Change Password
│   ├── My Permissions
│   └── System Settings (Admin only)
└── Logout
```

### Navigating Modules

1. **Click** on a menu item to expand it
2. **Click** on a sub-item to access that feature
3. **Use breadcrumbs** at the top to navigate back

---

## 5. Common Features Across All Screens

### List View
- **Search Box**: Find records by name, code, or ID
- **Filters**: Filter by status, date range, department, etc.
- **Sort**: Click column headers to sort
- **Pagination**: Navigate through pages (10, 25, 50 records per page)
- **Export**: Export data to Excel or CSV
- **Actions**: Edit, Delete, or View details

### Form/Edit View
- **Save Button**: Save changes
- **Cancel Button**: Discard changes and go back
- **Delete Button**: Delete the record
- **Print Button**: Print the record
- **History Button**: View change history

### Search & Filter
1. Enter **search term** in the search box
2. Select **filter criteria** from dropdown filters
3. Click **[Apply Filters]** button
4. Results update automatically

---

## 6. Basic Operations

### Creating a New Record

1. Navigate to the desired module (e.g., Employees)
2. Click **[+ New]** or **[Create New]** button
3. Fill in **required fields** (marked with *)
4. Enter **optional information** as needed
5. Click **[Save]** to create the record
6. A confirmation message will appear

### Editing an Existing Record

1. Navigate to the list of records
2. Click the record you want to edit
3. Click **[Edit]** button
4. Modify the desired fields
5. Click **[Save]** to update
6. Changes are saved immediately

### Deleting a Record

1. Navigate to the record
2. Click **[Delete]** button
3. Confirm deletion when prompted
4. Record is permanently deleted

### Viewing Record Details

1. Click on the record name or **[View]** button
2. See all details in read-only mode
3. Use **[Edit]** to make changes
4. Use **[Print]** to print the record

---

## 7. User Roles & Permissions

### Role Types

**Admin (Administrator)**
- Full system access
- Can create, edit, delete all records
- Can manage users and permissions
- Can access system settings

**Manager**
- Can view and create records in assigned departments
- Can approve orders and transactions
- Cannot access system settings
- Limited user management

**User (Regular Employee)**
- Can view and create assigned records
- Cannot delete most records
- Cannot approve transactions
- Limited to their department

**Viewer (Read-Only)**
- Can only view records
- Cannot create, edit, or delete
- Cannot approve transactions
- Read-only access to reports

### Permission Levels

Different modules have different permission levels:
- **View**: Read-only access to records
- **Create**: Can create new records
- **Edit**: Can modify existing records
- **Delete**: Can delete records
- **Approve**: Can approve transactions
- **Export**: Can export data

---

## 8. Keyboard Shortcuts

### Common Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl + S` | Save current form |
| `Ctrl + N` | Create new record |
| `Ctrl + E` | Edit current record |
| `Ctrl + F` | Open search |
| `Ctrl + P` | Print current record |
| `Escape` | Cancel/Close dialog |
| `Tab` | Move to next field |
| `Shift + Tab` | Move to previous field |
| `Enter` | Submit form |
| `Ctrl + Z` | Undo (where applicable) |

---

## 9. Getting Help

### Built-in Help Features

1. **Hover over fields**: Tooltips explain field purposes
2. **[?] Help buttons**: Click on help icons for field explanations
3. **Help menu**: Access guides and documentation
4. **Search help**: Search for topics in the help system

### Support Contact

If you need assistance:
- **Email**: support@idayam.com
- **Phone**: +91-XXXX-XXXX-XXXX
- **Chat**: Click the **Chat** icon in the bottom-right corner
- **Knowledge Base**: Visit https://help.idayam.com

### Reporting Issues

If you encounter a problem:
1. Note the **error message** (if any)
2. Note the **time** the issue occurred
3. Note the **steps** you were performing
4. **Contact support** with this information

---

## 10. Best Practices

### Security Best Practices

1. **Never share** your login credentials with anyone
2. **Change your password** regularly (every 90 days recommended)
3. **Log out** when leaving your desk unattended
4. **Use strong passwords**: Mix of uppercase, lowercase, numbers, symbols
5. **Report suspicious activity** immediately to your admin

### Data Entry Best Practices

1. **Double-check entries** before saving
2. **Use proper formatting** for dates and phone numbers
3. **Complete required fields** before saving
4. **Review data** before approving transactions
5. **Keep information updated** for accurate reporting

### System Best Practices

1. **Save frequently** while working on forms
2. **Use browser back button** sparingly - use menu navigation instead
3. **Clear browser cache** if you see outdated information
4. **Report bugs** to system administrator
5. **Keep your browser updated** for better compatibility

---

## 11. Frequently Asked Questions

**Q: I forgot my password. What do I do?**
A: Click "Forgot Password?" on the login screen, enter your email, and follow the instructions to reset.

**Q: Why can't I edit a record?**
A: You may not have edit permissions for that record type. Contact your administrator.

**Q: How do I export data to Excel?**
A: In list views, look for the **[Export]** button and select the format.

**Q: What should I do if the system is slow?**
A: Clear your browser cache, close other tabs, or try a different browser.

**Q: Can I access the system from mobile devices?**
A: Yes, the system is responsive and works on tablets and mobile devices.

---

## Quick Reference Checklist

- [ ] Login successful with your credentials
- [ ] Changed your initial password
- [ ] Updated your profile information
- [ ] Reviewed your assigned permissions
- [ ] Familiar with dashboard layout
- [ ] Can navigate the main menu
- [ ] Know how to search and filter
- [ ] Understand create, edit, delete operations
- [ ] Know who to contact for support
- [ ] Bookmarked the system URL

---

**Document Version**: 1.0
**Last Updated**: April 22, 2026
**Next Review**: April 22, 2027
