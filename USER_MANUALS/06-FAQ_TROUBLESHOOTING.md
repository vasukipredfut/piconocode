# Idayam ERP - FAQ & Troubleshooting Guide

## Frequently Asked Questions and Solutions

This guide answers common questions and provides solutions to frequent issues.

---

## 1. Login & Access Questions

### Q: I forgot my password. How do I reset it?

**A:** 
1. Click **"Forgot Password?"** on login screen
   - **Location**: Below the password field
   - **Note**: Only appears on login page
   
2. Enter your **email address**
   - **Clarification**: Must be the email registered in your account
   - **If unsure**: Contact your System Administrator to verify email
   
3. Click **[Send Reset Link]**
   - **What happens**: System sends email with password reset link
   - **Where**: Check your email inbox
   - **Spam folder**: Check if email doesn't appear in inbox
   
4. You'll receive an email with reset link
   - **From**: noreply@idayam-erp.com (or similar)
   - **Subject**: "Password Reset Request"
   - **Content**: Contains link to reset password
   
5. Click link in email
   - **Important**: Link only works once
   - **Expires**: 24 hours after sending
   - **If expired**: Request new reset link
   
6. Create **new password**
   - **Requirements** (MUST meet ALL):
     - ✓ Minimum 12 characters
     - ✓ At least 1 UPPERCASE letter (A-Z)
     - ✓ At least 1 lowercase letter (a-z)
     - ✓ At least 1 number (0-9)
     - ✓ At least 1 special character (!@#$%^&*)
   - **Example GOOD**: MyNewPassword@2024!
   - **Example BAD**: password123 (no uppercase, special char)
   
7. Click **[Reset Password]**
   - **Result**: Password updated successfully
   
8. Use new password to login
   - **Note**: Old password no longer works

**Password Reset - Troubleshooting**:
| Problem | Cause | Solution |
|---------|-------|----------|
| No email received | Email address wrong or typed wrong | Check email carefully, verify with admin |
| Link expired (>24hrs) | Didn't click within 24 hours | Request new reset link |
| Reset fails | Password doesn't meet requirements | Use strong password with all requirements |
| Still can't login | Email address issue | Contact System Administrator |

**Important Security Notes**:
- Never share password reset links with others
- Don't use same password as another account
- Change password immediately if you suspect it's compromised

### Q: How do I change my password after login?

**A:**
1. Go to **Settings** > **Change Password**
   - **Path**: Click Settings icon (gear ⚙️) → Select "Change Password"
   - **Alternative**: User Profile → Change Password
   
2. Enter **Current Password**
   - **Clarification**: Your existing/old password
   - **Why needed**: Confirms you are authorized to change password
   - **Security**: Prevents unauthorized password changes if someone has access to your unlocked computer
   
3. Enter **New Password** (min 12 characters, mixed case, numbers, symbols)
   - **Requirements**:
     - ✓ Minimum 12 characters (longer = more secure)
     - ✓ At least 1 UPPERCASE letter (A-Z)
     - ✓ At least 1 lowercase letter (a-z)
     - ✓ At least 1 number (0-9)
     - ✓ At least 1 special character (!@#$%^&*)
     - ✗ Cannot be same as current password
     - ✗ Cannot be same as previous 3 passwords
   - **Examples**:
     - ✓ GOOD: SecurePass@2024#Ver2
     - ✗ BAD: password123 (no uppercase, symbols)
     - ✗ BAD: Pass123 (only 7 characters)
   
4. Confirm **New Password**
   - **Clarification**: Re-enter same password to confirm
   - **Why needed**: Prevents typos that would lock you out
   - **Error if mismatch**: "Passwords do not match"
   
5. Click **[Change Password]**
   - **Result**: "Password changed successfully"
   - **Effect**: Changes immediately
   - **Next login**: Use new password

**Password Change - Tips**:
- Do NOT include your username in the password (e.g., if username is john.smith, don't use "john" in password)
- Do NOT share your password with anyone (IT won't ask for it)
- Save in secure location (password manager like 1Password, LastPass, or Keeper)
- Change every 90 days for security
- If you suspect password is compromised: Change immediately and contact IT

**Password Change - Common Errors**:
| Error | Cause | Fix |
|-------|-------|-----|
| "Current password is incorrect" | Typed old password wrong | Check CAPS LOCK, try again |
| "Passwords do not match" | New and confirm don't match | Re-type, ensure no typos |
| "Password does not meet requirements" | Missing required elements | Add uppercase, number, symbol |
| "Cannot reuse previous password" | Used same as before | Use completely different password |

### Q: I can't access a particular module. What should I do?

**A:**
**First, check if module appears in menu**:
- If module name is NOT in left menu at all:
  - You don't have permission to access it
  
- If module name appears but is grayed out (disabled):
  - You have permission but feature is disabled
  - Contact administrator

**Steps to request access**:
1. Identify which module you need
   - Example: "HR" or "Procurement"
   
2. Note what you're trying to do
   - Example: "Create new employee records"
   - Example: "Approve purchase orders"
   
3. Contact your **System Administrator** or **Department Manager**
   - Provide:
     - Your full name / username
     - Name of module you need
     - What specific actions you want to perform
     - Why you need access (department, role, project)
   
4. Administrator will:
   - Review your role and responsibilities
   - Assign appropriate permissions
   - Grant access (usually within 1 business day)
   
5. You'll receive notification when access granted
   - Module will appear in menu
   - Buttons will be enabled
   - Can now perform actions

**Permission Levels** (what you might get access to):
- **View only**: Can read data, but cannot make changes
- **Create**: Can add new records
- **Edit**: Can modify existing records
- **Delete**: Can remove records (restricted to important roles)
- **Approve**: Can approve pending items (manager permissions)
- **Report**: Can view and generate reports
- **Admin**: Full control of module

### Q: How do I check what permissions I have?

**A:**
1. Go to **Settings** > **My Permissions**
   - **Path**: Click Settings (⚙️) icon → Click "My Permissions"
   - **Alternative**: Profile → My Permissions
   
2. View all permissions assigned to your role
   - **Format**: Usually organized as checklist or table
   - **Shows**: ✓ = Have permission, ✗ or blank = No permission
   
3. Organized by module:
   - **HR**: Can create/edit/delete employees, manage attendance, process payroll, etc.
   - **Inventory**: Can manage products, stock, warehouses
   - **Procurement**: Can create POs, manage vendors, track receipts
   - **Sales**: Can create orders, process deliveries
   - **Finance**: Can create vouchers, view financial reports
   - **Reports**: Can access various reports
   - **Settings**: Can modify system configuration (admin only)

**Understanding Your Permissions**:
- **Module permission**: Access to entire module
- **Sub-module permission**: Access to specific feature within module
- **Action permission**: Specific actions (Create, Edit, Delete, Approve)

**If you see permissions you need but can't use**:
- Permission may be role-based (applied by role, not individual)
- Role applies standardized permission set to all users in that role
- Cannot have permissions others with same role don't have
- Contact administrator to review your role

**Common Permission Levels by Role**:
| Role | HR | Inventory | Procurement | Reports |
|------|-----|-----------|-------------|---------|
| **Admin** | Full | Full | Full | Full |
| **Manager** | View/Edit | View | Approve POs | View |
| **Staff** | View Own | View | Create PR | Limited |
| **Viewer** | View Only | View Only | View Only | View Only |

### Q: Why does the system keep logging me out?

**A:**
**Common causes and solutions**:

1. **Session Inactivity Timeout** (Most Common)
   - **What is it**: System automatically logs you out if you don't do anything for 30 minutes
   - **Why**: Security feature to protect account if you leave computer unattended
   - **Solution**: 
     - Keep working or stay active
     - Save work frequently
     - If taking break, lock your computer (Windows + L on Windows, Cmd + Control + Q on Mac)
   - **What you see**: "Your session has expired, please login again"
   - **What you lose**: Any unsaved form data is lost
   
2. **Multiple Login Sessions**
   - **What is it**: You logged in from another device/browser
   - **Why**: System forces out previous sessions for security
   - **Solution**:
     - Logout from one device before logging in from another
     - Or accept new session logout and login again
   - **What you see**: "You have been logged out. Another session started from different location."
   
3. **Browser Issues**
   - **What is it**: Browser cache/cookies cleared or expired
   - **Why**: Browser memory can get corrupted
   - **Solution**:
     - Clear browser cache (Ctrl + Shift + Delete on Windows)
     - Clear cookies
     - Close browser completely and reopen
     - Try different browser (Chrome, Firefox, Safari, Edge)
   
4. **Network Connection Lost**
   - **What is it**: Internet connection dropped during session
   - **Why**: Server lost connection with your browser
   - **Solution**:
     - Reconnect to internet
     - Login again
     - Use stable internet connection

**How to Prevent Unexpected Logouts**:
- **Save Work Frequently**: Ctrl + S or Click Save button
- **Don't Leave Computer Unattended**: Lock screen when away
- **Keep Session Active**: Make a click or keystroke every 20 minutes during activity
- **Close Browser Properly**: Don't force quit or restart computer while logged in
- **Use One Session**: Login from one device at a time

**What to Do When Logged Out**:
1. Click **[Login Again]** or return to login page
2. Enter username and password
3. Click **[Login]**
4. Return to where you were (may need to navigate back)
5. **Data**: Any unsaved changes are lost
   - Saved data: Still there
   - Unsaved form data: Lost

---

## 2. Navigation & Interface Questions

### Q: How do I get back to previous page?

**A:**
Use **breadcrumb navigation** at top of page:
- Click any breadcrumb item to go back
- Example: Home > Employees > Edit
- Avoid using browser back button as it may lose data

### Q: How do I search for records across the system?

**A:**
**Method 1: Global Search**
1. Press **Ctrl + F** or click search icon
2. Type search term
3. Click on result to navigate

**Method 2: Module Search**
1. Go to specific module
2. Use **Search** field at top
3. Or use **Advanced Search** for multiple criteria

### Q: The system is very slow. What can I do?

**A:**
1. **Clear browser cache**:
   - Settings > Clear Cache
2. **Close unnecessary tabs**: Reduces memory usage
3. **Try different browser**: Chrome is recommended
4. **Check internet connection**: Ensure stable connection
5. **Contact IT**: If persistent, report to IT department

### Q: How do I change the date or time format?

**A:**
1. Go to **Settings** > **Preferences**
2. Set **Date Format**:
   - DD/MM/YYYY or MM/DD/YYYY
3. Set **Time Zone**: Select your timezone
4. Click **[Save Preferences]**

---

## 3. Data Entry & Form Questions

### Q: What does the * (asterisk) mean next to a field?

**A:**
- Red **\*** means **Required Field**
- Must fill this field before saving
- System won't allow save if required fields empty
- Check form for all red asterisks before clicking Save

### Q: How do I select from a dropdown that has many options?

**A:**
1. Click **dropdown field**
2. Type to **filter options** (starts searching automatically)
3. Click desired option
4. Or scroll and click option

**Tip**: Type first few letters of option to quickly narrow down

### Q: How do I upload files or documents?

**A:**
1. Click **Upload** area or **[Choose File]** button
2. Select file from computer
3. File name appears in field
4. Click **[Upload]** to confirm
5. Or save form - file uploads with form

**Supported formats**: PDF, Excel, Word, Images (JPG, PNG), CSV

### Q: How do I enter dates? What format do I use?

**A:**
**Option 1: Calendar picker**
- Click **calendar icon** next to date field
- Select month/year from header
- Click day to select

**Option 2: Type directly**
- Format: **DD/MM/YYYY**
- Example: 25/04/2024

**Option 3: Type and let system parse**
- Just type: 25/4/24 or 4-25-24
- System recognizes and formats automatically

### Q: What happens if I leave a form and don't save?

**A:**
1. System detects unsaved changes
2. Shows **"Unsaved changes"** warning
3. If you navigate away:
   - Click **[Save]** to save changes
   - Click **[Discard]** to lose changes
4. **Tip**: Save frequently (Ctrl+S) to avoid losing data

### Q: How do I select multiple options from a list?

**A:**
- Check **checkboxes** next to items
- Use **Select All** checkbox to select all on page
- For bulk actions, select items and click **[Bulk Actions]**

---

## 4. Reporting & Export Questions

### Q: How do I export data to Excel?

**A:**
1. Go to list view (e.g., Employee list)
2. Apply filters if needed
3. Click **[Export]** button
4. Select **Excel (.xlsx)** format
5. Click **[Download]**
6. File downloads to your computer

**Note**: Exported data includes all columns shown on screen

### Q: Can I export to PDF format?

**A:**
Yes, most reports support PDF:
1. Click **[Export]** button
2. Select **PDF** format
3. Click **[Download]**

Or use **[Print]** button and print to PDF

### Q: How do I schedule a report to run automatically?

**A:**
1. Open report
2. Click **[Schedule]** button
3. Set **Frequency**:
   - Daily
   - Weekly (select day)
   - Monthly (select date)
4. Select **Recipients** (email addresses)
5. Choose **Format**: Excel, PDF
6. Click **[Schedule]**

Report automatically emailed to recipients on schedule

### Q: How do I apply filters to a report?

**A:**
1. Open report
2. Use **Filter** fields at top:
   - Department: Select from dropdown
   - Date Range: Select from/to dates
   - Status: Select status
3. Click **[Apply Filters]**
4. Or click **[Advanced Search]** for complex filters

### Q: Can I save my custom filter for reuse?

**A:**
Yes, for some filters:
1. Set up filter criteria
2. Click **[Save Filter]**
3. Name your filter
4. Click **[Save]**
5. Next time, select from **Saved Filters** dropdown

---

## 5. Module-Specific Questions

### HR Module

**Q: How is salary calculated?**

**A:**
Salary = Basic Salary + Allowances (HRA, DA, Conveyance, etc.) - Deductions (PF, Tax, Insurance, etc.)

**Attendance Impact**:
- Present: 100% salary
- Absent: Full day salary deducted
- Half Day: Half day salary deducted
- Leave (Approved): No deduction

### Q: Why can't I mark attendance for a past month?

**A:**
- System locks past month attendance
- Use **Bulk Upload** feature instead:
  1. Go to Attendance
  2. Click **[Bulk Upload]**
  3. Download Excel template
  4. Fill attendance data
  5. Upload completed file

### Q: How do I view my leave balance?

**A:**
1. Go to **HR** > **Leave Management**
2. Click **[My Leave Balance]**
3. Shows for each leave type:
   - Total entitled
   - Used this year
   - Remaining balance

### Q: What if a leave application is rejected?

**A:**
- You'll receive notification with rejection reason
- Reapply with different dates
- Contact your manager to discuss

---

### Inventory Module

**Q: What is the difference between "In Stock" and "Free Stock"?**

**A:**
- **In Stock**: Total quantity available
- **Free Stock**: In Stock - Reserved items (allocated to orders)
- If In Stock = 100 and Reserved = 30
- Free Stock = 70

### Q: How do I handle stock that's damaged or expired?

**A:**
1. Go to **Inventory** > **Stock Management**
2. Click **[Physical Stock Take]**
3. Enter actual count (excluding damaged)
4. Click **[Post Adjustment]**
5. Or manually:
   1. Go to **Stock Movements** > **[Stock Adjustment]**
   2. Enter damaged/expired quantity
   3. Reason: Damage, Expiry, etc.
   4. Click **[Post]**

### Q: Why can't I reduce stock below zero?

**A:**
- System prevents negative stock
- Current stock: 10 units
- You cannot issue 15 units
- **Solution**: Receive stock first, then issue

---

### Procurement Module

**Q: What's the difference between PR and PO?**

**A:**
- **PR (Purchase Requisition)**: Internal request for purchase
  - Created by employee requesting goods
  - Needs approval from manager
  - Used to control what should be purchased

- **PO (Purchase Order)**: External order to vendor
  - Created by procurement
  - Sent to vendor
  - Legally binding contract

**Workflow**: PR → Approved PR → Convert to PO → Send to Vendor

### Q: Can I edit a PO after sending it to vendor?

**A:**
- **Before Goods Receipt**: Yes, can edit most fields
- **After Goods Receipt**: Cannot edit quantities
- If vendor needs to know of changes, send email notification

### Q: Why is my invoice amount different from PO amount?

**A:**
Common reasons:
- **Quantity Difference**: Less or more received
- **Price Change**: Vendor charged different price
- **Taxes**: Additional taxes added
- **Freight**: Shipping charges added
- **Discount**: Discount applied

**Resolution**: Compare GRN (actual received) with Invoice

---

## 6. Troubleshooting Common Issues

### Issue: Cannot save form - "Required fields" error

**Solution**:
1. Check for **red \*** next to fields
2. Hover over error fields for message
3. Fill all required fields
4. Try saving again

### Issue: Page shows "Page not found" or error 404

**Solution**:
1. Refresh page (F5)
2. Clear browser cache
3. Try again
4. If persistent, contact IT

### Issue: Data is not updated immediately after saving

**Solution**:
1. **Refresh page** (F5) to reload data
2. Clear browser cache (Settings > Clear Cache)
3. Try from different browser
4. Contact IT if issue persists

### Issue: Dropdown field not showing all options

**Solution**:
1. Scroll in dropdown to see more options
2. Type to search/filter options
3. Click refresh button if available
4. Deactivated items may not show (check with admin)

### Issue: Report showing "No data" even when records exist

**Solution**:
1. Check **filters** are not too restrictive
2. Click **[Clear Filters]** to remove all filters
3. Run report again
4. Check date range filter

### Issue: Cannot delete a record - "Cannot delete" error

**Solution**:
- Record may be referenced by other records
- Example: Cannot delete employee if they have attendance records
- Solution: Contact administrator if deletion is critical

### Issue: Excel export shows special characters as gibberish

**Solution**:
1. Open Excel file
2. Select all data (Ctrl+A)
3. Set encoding to **UTF-8**
4. Special characters should display correctly

### Issue: Attached file cannot be downloaded

**Solution**:
1. File may have been deleted
2. Re-upload the file
3. Or contact administrator

### Issue: Email notification not received

**Solution**:
1. Check **email address** is correct
2. Check **spam/junk folder**
3. Ask IT to verify email configuration
4. Manual notification option available

---

## 7. Performance & System Questions

### Q: What browser is recommended?

**A:**
- **Chrome**: Best performance
- **Firefox**: Good performance
- **Safari**: Good performance
- **Edge**: Good performance
- **Internet Explorer**: Not recommended (outdated)

### Q: How often should I clear browser cache?

**A:**
- Clear cache **weekly** if using system daily
- Or if experiencing display issues
- Go to **Settings** > **Clear Cache**

### Q: What's the maximum file size I can upload?

**A:**
- Maximum: **10 MB per file**
- Larger files must be split
- Or compress before uploading

### Q: Can I access the system from mobile/tablet?

**A:**
- Yes, system is responsive
- Works on tablets and mobile devices
- Not all features optimized for small screens
- Recommended: Tablet or larger screen

### Q: Is there an offline mode?

**A:**
- No offline mode available
- System requires constant internet connection
- Cannot function without connection

---

## 8. Security & Compliance Questions

### Q: How often should I change my password?

**A:**
- Change every **90 days** minimum
- Change immediately if:
  - You suspect breach
  - You shared password (shouldn't happen!)
  - Someone asks for it (never share!)

### Q: What should I do if I suspect my account is hacked?

**A:**
1. **Change password immediately**
2. **Logout from all sessions**
3. **Contact IT/Admin** with:
   - When you noticed issue
   - What unusual activity occurred
   - Any sensitive data accessed

### Q: Will my data be backed up?

**A:**
- Yes, daily backups are performed
- Backed up to secure location
- You can request data recovery if needed

### Q: Who can see my salary information?

**A:**
- Your own salary: Only you (and HR)
- Other salaries: Only authorized persons (Manager, Finance, Admin)
- System logs who accesses sensitive data

### Q: How long is data retained?

**A:**
- Active data: Retained indefinitely
- Inactive employee data: Retained per legal requirement (7 years typically)
- Deleted data: Backup retention follows same policy

---

## 9. Common Error Messages

### "Access Denied"
- You don't have permission for this action
- **Solution**: Contact admin to get permission

### "Record not found"
- Record may have been deleted
- You may not have permission to view
- **Solution**: Check record exists or ask admin

### "Duplicate record"
- Record with same code/identifier exists
- **Solution**: Use unique code or edit existing record

### "Database connection error"
- Server temporarily unavailable
- **Solution**: Wait and refresh, or contact IT

### "File size exceeds limit"
- File larger than 10 MB
- **Solution**: Compress file or split into smaller files

### "Invalid date format"
- Date entered in wrong format
- **Solution**: Use DD/MM/YYYY format

### "Concurrent edit conflict"
- Multiple people editing same record
- **Solution**: Refresh to get latest version

### "Session expired"
- Logged out due to inactivity
- **Solution**: Login again

---

## 10. Getting Additional Help

### Support Contacts

| Channel | Contact | Response Time |
|---------|---------|----------------|
| **Email** | support@idayam.com | 24 hours |
| **Chat** | In-app chat (bottom-right) | 1-2 hours |
| **Phone** | +91-XXXX-XXXX-XXXX | 2-4 hours |
| **Knowledge Base** | https://help.idayam.com | Self-service |

### Information to Provide

When contacting support, provide:
1. **Your username and email**
2. **What you're trying to do** (detailed steps)
3. **Error message** (exact text)
4. **When it occurred** (date and time)
5. **Browser and version** (Chrome, Firefox, etc.)
6. **Screenshots** (if applicable)

### Escalation Process

- **Level 1**: Support team troubleshoot
- **Level 2**: Escalate if not resolved in 24 hours
- **Level 3**: Senior technical team involvement
- **Critical**: Immediate escalation

---

## 11. User Documentation

### Available Manuals

1. **Getting Started Guide**: Login and initial setup
2. **System Navigation**: Interface and navigation guide
3. **HR Module Manual**: Employee and payroll management
4. **Inventory Module Manual**: Product and stock management
5. **Procurement Module Manual**: Purchasing and vendors
6. **Sales Module Manual**: Orders and customers
7. **Finance Module Manual**: Vouchers and reports
8. **FAQ & Troubleshooting**: This document

### Where to Find Documents

- **In System**: Help menu > Documentation
- **External**: https://help.idayam.com/documentation
- **Offline**: Download PDF versions from Portal

---

## 12. Useful Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Ctrl + F | Open search |
| Ctrl + S | Save current form |
| Ctrl + N | Create new record |
| Ctrl + E | Edit current record |
| Ctrl + P | Print current record |
| Ctrl + C | Copy text |
| Ctrl + V | Paste text |
| Escape | Close dialog/form |
| Tab | Move to next field |
| Shift + Tab | Move to previous field |
| Enter | Submit form |
| F5 | Refresh page |

---

## Quick Reference Checklist

### When You Get Stuck

- [ ] Check if you have required permissions
- [ ] Verify all required fields are filled (red *)
- [ ] Check error message carefully
- [ ] Try refreshing the page
- [ ] Clear browser cache
- [ ] Try different browser
- [ ] Logout and login again
- [ ] Contact support with details

### Information to Save

- [ ] Your login credentials (kept secure!)
- [ ] Support contact numbers
- [ ] Your role and permissions
- [ ] Browser version
- [ ] System documentation links
- [ ] IT contact for emergency issues

---

**Document Version**: 1.0
**Last Updated**: April 22, 2026
**Next Review**: April 22, 2027

---

**Still need help?**
- Email: support@idayam.com
- Phone: +91-XXXX-XXXX-XXXX
- Chat: Click chat icon at bottom-right of screen
- Knowledge Base: https://help.idayam.com
