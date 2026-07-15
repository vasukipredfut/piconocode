# Idayam ERP - Procurement Module User Manual

## Complete Guide to Purchase Orders & Vendor Management

This manual covers all procurement operations including purchase orders, vendor management, and goods receipt.

---

## 1. Procurement Module Overview

### Procurement Features

The Procurement module manages all purchasing and vendor-related operations:

```
Procurement Module
├── Purchase Orders
│   ├── Create PO
│   ├── Send to Vendor
│   ├── Track PO Status
│   └── PO History
│
├── Vendors Management
│   ├── Vendor Master
│   ├── Vendor Details
│   ├── Vendor Performance
│   └── Vendor Documents
│
├── Purchase Requisitions
│   ├── Create PR
│   ├── Convert to PO
│   └── PR Status Tracking
│
└── Goods Receipt & Returns
    ├── Receive Goods
    ├── Inspection & QC
    ├── Returns Management
    └── GRN Tracking
```

---

## 2. Vendor Management

### Vendor Master Setup

Each vendor record contains:

**Basic Information**:
- Vendor Code (unique)
- Vendor Name
- Contact Person
- Email, Phone

**Address Information**:
- City, State, Postal Code
- Country

**Commercial Terms**:
- Payment terms
- Delivery terms
- Discount terms

**Status**:
- Active/Inactive
- Approval status

### Creating New Vendor

1. Go to **Procurement** > **Vendors**
   - **Path**: Click "Procurement" in left menu → Click "Vendors"
   
2. Click **[+ New Vendor]**
   - **Location**: Top right corner, green button
   - **Result**: New form opens for vendor data entry

3. Fill **Vendor Information**:
   - **Vendor Code*** (required): Unique identifier
     - **Clarification**: System identifier for the vendor
     - **Format**: 6-8 characters (e.g., VEND001, SUP-ABC-001)
     - **Important**: Must be unique - no two vendors can share code
     - **Tip**: Use meaningful codes (e.g., ABC-INC for ABC Incorporated)
     
   - **Vendor Name*** (required): Full company name
     - **Clarification**: Official business name
     - **Examples**: 
       - "ABC Trading Company Ltd."
       - "XYZ Manufacturing Inc."
     - **Tip**: Match legal registration name exactly
     - **Used for**: Purchase orders, invoices, reports
     
   - **Contact Person*** (required): Primary contact name
     - **Clarification**: Person to contact at vendor company
     - **Examples**: "Mr. John Smith", "Ms. Sarah Johnson"
     - **Why important**: Used for communication and correspondence
     
   - **Email*** (required): Contact email address
     - **Format**: name@company.com
     - **Used for**: PO delivery, notifications, communication
     - **Tip**: Get official vendor email, not personal email
     
   - **Phone*** (required): Contact phone number
     - **Format**: +1-555-123-4567 or (555) 123-4567
     - **Used for**: Quick communication if urgent
     - **Tip**: Get direct line if possible

4. Fill **Address**:
   - **Address Line**: Street address
     - **Example**: "123 Industrial Park Road"
     
   - **City**, **State**: Location details
     - **Example**: City: Mumbai, State: Maharashtra
     
   - **Postal Code**: Zip code
     - **Example**: 400001
     
   - **Country**: Country location
     - **Important**: Used for customs, shipping calculations
     - **Select from dropdown**

5. Fill **Commercial Terms**:
   - **Payment Terms**: When and how you pay
     - **Clarification**: Defines payment period after invoice
     - **Common options**:
       - **NET 30**: Pay within 30 days of invoice date
       - **NET 60**: Pay within 60 days (longer credit period)
       - **NET 90**: Pay within 90 days (extended credit)
       - **COD (Cash on Delivery)**: Pay when goods arrive
       - **ADVANCE**: Pay before goods shipped
     - **Why important**: Affects cash flow and working capital
     - **Example**:
       - Invoice date: 01-Apr-2024
       - NET 30 means: Pay by 01-May-2024
     - **Tips**:
       - Longer terms improve cash flow
       - But may mean higher prices
       - Balance between cash flow and cost
     
   - **Delivery Lead Time**: Days from order to delivery
     - **Clarification**: How long vendor takes to deliver
     - **Examples**:
       - 3 days: Fast delivery (local vendor)
       - 7 days: Standard delivery
       - 14 days: Overseas/slow delivery
     - **Why important**: Used in reorder calculation
     - **Formula**: Reorder Level = (Daily Sales × Lead Time) + Safety Stock
     
   - **Discount %** (optional): Discount if applicable
     - **Clarification**: Percentage discount on purchases
     - **Examples**: 0%, 5%, 10%, 15%
     - **When applies**: Usually on large orders or bulk purchases
     - **Note**: Vendor may offer different discounts for different scenarios

6. **Vendor Rating** (if system allows):
   - Initial rating: Not rated until orders placed
   - Rating updates after purchases based on performance
   
7. Check **[Active]** to enable vendor
   - **Clarification**: Active vendors appear in selection lists
   - **Inactive**: Vendors hidden (for discontinued suppliers)
   
8. Click **[Save]**
   - **What happens**: 
     - Vendor created, success message shown
     - Vendor now available for use in Purchase Orders
     - Can now assign to purchase requisitions

**Vendor Creation - Important Notes**:
- **Why Payment Terms matter**: Affects cash flow
  - NET 30 = You have 30 days to pay after invoice
  - Must plan invoices and cash carefully
  
- **Why Delivery Lead Time matters**: Affects stock planning
  - If vendor takes 14 days, must order well in advance
  - Short lead time = order last minute if needed
  - Long lead time = must forecast demand further ahead

- **Vendor Management Tips**:
  - Keep vendor contacts updated
  - Monitor vendor performance (on-time delivery, quality)
  - Compare terms among multiple vendors
  - Negotiate better terms for large volumes

### Viewing Vendor Details

1. Go to **Procurement** > **Vendors**
2. Search or filter vendors
3. Click vendor name to view
4. See complete vendor profile
5. View transaction history with vendor

### Editing Vendor Information

1. Open vendor record
2. Click **[Edit]**
3. Update fields as needed
4. Click **[Save]**

### Vendor Rating & Performance

**Track Vendor Performance**:

1. Open vendor record
2. View **Performance Metrics**:
   - On-time delivery %
   - Quality score
   - Price competitiveness
   - Order fulfillment rate
3. Rating: Excellent, Good, Average, Poor

**Deactivate Vendor**:

1. If vendor no longer used
2. Click **[Edit]**
3. Uncheck **[Active]**
4. Click **[Save]**
5. Vendor won't appear in selection lists

---

## 3. Purchase Requisitions (PR)

### Creating Purchase Requisition

**PR is Internal Request for Purchase**

1. Go to **Procurement** > **Purchase Requisitions**
2. Click **[+ New PR]**
3. Fill **PR Header**:
   - **PR Number**: Auto-generated
   - **PR Date**: Today's date
   - **Required By Date**: When goods needed
   - **Department**: Requesting department
   - **Requested By**: Your name (auto-filled)
4. Add **PR Items**:
   - Click **[+ Add Item]**
   - **Product**: Select product
   - **Quantity**: Required quantity
   - **Unit**: Auto-filled
   - **Estimated Cost**: Auto-filled from product master
   - Repeat for multiple items
5. Add **Notes**: Any special instructions
6. Click **[Save]** to draft
7. Click **[Submit]** to send for approval

### Approving Purchase Requisition

**Manager/Approver Role**:

1. Go to **Procurement** > **Purchase Requisitions**
2. Filter: **[Status: Pending Approval]**
3. Review PR details:
   - Items requested
   - Quantities
   - Estimated costs
   - Department and business case
4. **To Approve**:
   - Click **[Approve]**
   - Click **[Confirm]**
5. **To Reject**:
   - Click **[Reject]**
   - Enter rejection reason
   - Click **[Confirm]**

### Converting PR to Purchase Order

1. Open approved PR
2. Click **[Convert to PO]**
3. System shows PR items
4. Select **Vendor** from dropdown
5. Vendor details auto-populate:
   - Address
   - Payment terms
   - Delivery terms
6. Review and adjust:
   - Unit prices (from vendor quotes)
   - Delivery date
   - Any special terms
7. Click **[Create PO]**
8. PO created and linked to PR

---

## 4. Purchase Orders (PO)

### Understanding PO Status

| Status | Meaning |
|--------|---------|
| **Draft** | Created, not yet sent to vendor |
| **Sent** | Sent to vendor, awaiting confirmation |
| **Confirmed** | Vendor confirmed, order placed |
| **Partially Received** | Some goods received |
| **Received** | All goods received |
| **Invoiced** | Invoice received from vendor |
| **Paid** | Payment made to vendor |
| **Closed** | PO completed |
| **Cancelled** | PO cancelled |

### Creating Purchase Order

**Option 1: From Purchase Requisition** (Already covered above)

**Option 2: Direct PO Creation**

1. Go to **Procurement** > **Purchase Orders**
2. Click **[+ New PO]**
3. Fill **PO Header**:
   - **PO Number**: Auto-generated
   - **Vendor** *: Select vendor
   - **Delivery Address**: Auto-filled or modify
   - **PO Date**: Today
   - **Expected Delivery**: Estimated date
   - **Payment Terms**: NET 30, NET 60, etc.
4. Add **PO Items**:
   - Click **[+ Add Item]**
   - **Product** *: Select product
   - **Quantity** *: Order quantity
   - **Unit Price** *: Price per unit
   - **Line Total**: Auto-calculated
   - Repeat for multiple items
5. **Order Total**: Auto-calculated
6. Add **Notes**: Special instructions
7. Click **[Save]** to draft
8. Click **[Send to Vendor]** to send PO

### Sending PO to Vendor

1. Open PO in **Draft** status
2. Click **[Send to Vendor]**
3. Enter vendor **Email** (auto-filled)
4. Add optional **Message** to vendor
5. Click **[Send]**
6. Status changes to **Sent**
7. Vendor receives email with PO details

### Tracking PO Status

1. Go to **Procurement** > **Purchase Orders**
2. View all POs with status:
   - Green: On track
   - Orange: At risk (delayed)
   - Red: Critical (very delayed)
3. Click PO to see:
   - Full details
   - Expected vs actual delivery
   - Goods received so far
   - Invoice status
   - Payment status

### Modifying PO

**Before Goods Receipt**:

1. Open PO
2. Click **[Edit]**
3. Can modify:
   - Delivery date
   - Quantities (if not received)
   - Unit prices
   - Payment terms
4. Click **[Save]**
5. If already sent to vendor, notify them of changes

**After Goods Received**:

Cannot modify received quantities. Can only:
- Update payment terms
- Add notes

### Closing PO

1. Open PO
2. When all goods received and invoiced
3. Click **[Close PO]**
4. Status becomes **Closed**
5. No further changes allowed

### Cancelling PO

1. Open PO in **Draft** or **Sent** status
2. Click **[Cancel PO]**
3. Enter **Cancellation Reason**
4. Click **[Confirm Cancel]**
5. Status becomes **Cancelled**

---

## 5. Goods Receipt & Inspection

### Receiving Goods from Vendor

**When goods arrive from vendor**:

1. Go to **Procurement** > **Goods Receipt** (or **Inventory** > **Stock Movements**)
2. Click **[+ Receive Goods]** or **[Goods Receipt Note]**
3. Enter **PO Number** or select from list
4. System shows:
   - PO details
   - Items expected
   - Quantities ordered
5. For each item:
   - Enter **Quantity Received**
   - If less than ordered, note **Short Quantity**
   - If more received, note **Over Quantity**
6. Enter **Receipt Date**
7. **Inspection**: Check for:
   - Correct items received
   - Correct quantities
   - No damage
   - Quality acceptable
8. Enter **Comments** about inspection
9. Click **[Post Receipt]** or **[Create GRN]** (Goods Receipt Note)
10. System updates:
    - Inventory stock increases
    - PO status updates
    - GRN created for records

### Handling Damaged/Defective Goods

**If goods received are damaged**:

1. During receipt, note in **Comments**
2. Reject those items during receipt
3. Create **Return Note** immediately
4. Notify vendor
5. Arrange return/replacement

**Return Process**:

1. Go to **Procurement** > **Returns**
2. Click **[+ New Return]**
3. Reference the **GRN** (Goods Receipt Note)
4. List items being returned:
   - Product name
   - Quantity
   - Reason: Defective, Damaged, Wrong Item, etc.
5. Authorize return
6. Send to vendor
7. Track return status

### GRN (Goods Receipt Note)

**Document created when goods received**:

Contains:
- PO reference
- Items received
- Quantities
- Condition
- Receipt date
- Inspector name

**Uses**:
- Confirm goods were received
- Match with invoice
- Process payment
- Audit trail

---

## 6. Vendor Invoice & Payment

### Processing Vendor Invoice

1. Go to **Procurement** > **Vendor Invoices**
2. Click **[+ New Invoice]** or
3. Upload **Invoice from Vendor**:
   - Enter invoice number
   - Enter invoice date
   - Attach invoice document
4. System shows GRNs matching invoice:
   - Match GRN with invoice items
   - Verify quantities match
   - Verify amounts match
5. Click **[Match GRN to Invoice]**
6. If discrepancy:
   - Note in comments
   - Follow exception handling
7. Click **[Approve Invoice]**
8. Status becomes **Approved**

### Payment Processing

1. Go to **Finance** > **Payments** or **Procurement** > **Vendor Invoices**
2. View **Invoices Due for Payment**
3. Select invoices to pay
4. Click **[Process Payment]** or **[Create Payment Voucher]**
5. Payment details auto-populated
6. Choose payment method:
   - Bank transfer
   - Cheque
   - Cash
7. Click **[Process]**
8. Payment recorded and confirmed

---

## 7. Purchase Reports

### Available Reports

**Order Related**:
- **Open POs**: Active POs awaiting delivery
- **Overdue POs**: POs past delivery date
- **PO by Vendor**: All POs from each vendor
- **PO Performance**: Delivery vs expected

**Vendor Related**:
- **Vendor List**: All vendors
- **Vendor Performance**: On-time delivery, quality scores
- **Vendor Spend**: Total purchase amount per vendor
- **Vendor Comparison**: Compare vendor performance

**Financial Reports**:
- **Purchase Summary**: Total spending by category
- **Vendor Payment**: Invoices and payment status
- **Outstanding Payables**: Unpaid invoices

### Generating Report

1. Go to **Reports** > **Procurement Reports**
2. Select **Report Type**
3. Set filters:
   - Date range
   - Vendor
   - Category
   - Status
4. Click **[Generate]**
5. View, export, or print

---

## 8. Common Procurement Tasks

### Task: Create PO for Low Stock Item

1. Go to **Inventory** > **Stock Management**
2. Identify low-stock product
3. Click **[Reorder]**
4. System opens **New PO**
5. Select vendor
6. Confirm quantity
7. Send to vendor

### Task: Expedite Delayed PO

1. Open delayed PO
2. Click **[Expedite]**
3. Enter urgent delivery date
4. Add urgent note
5. Email sent to vendor
6. System tracks expedited status

### Task: Compare Vendor Quotes

1. Create PR
2. Submit to multiple vendors for quotes
3. Go to **Procurement** > **RFQ** (Request for Quote)
4. Create RFQ from PR
5. Send to multiple vendors
6. Receive and compare quotes
7. Select best quote
8. Create PO from selected quote

### Task: Set Up Vendor Rating

1. Go to **Procurement** > **Vendors**
2. Open vendor
3. Click **[Ratings]** or **[Performance]**
4. Set rating criteria:
   - On-time delivery score
   - Quality score
   - Price competitiveness
   - Communication quality
5. Overall rating auto-calculated
6. Use ratings for vendor selection

---

## 9. Best Practices

### Purchase Process Best Practices

1. **Always Create PR**: Use PR process for control
2. **Get Quotes**: Compare prices from multiple vendors
3. **Verify Before Payment**: Match GRN with invoice
4. **Document Everything**: Keep copies of all documents
5. **Review Regularly**: Check delivery performance

### Vendor Management Best Practices

1. **Maintain Master Data**: Keep vendor info updated
2. **Regular Reviews**: Evaluate vendor performance
3. **Diversify Vendors**: Don't depend on single vendor
4. **Build Relationships**: Maintain good vendor relations
5. **Negotiate Terms**: Try to get better payment/delivery terms

### Procurement Control Best Practices

1. **Approval Workflow**: Follow authorization levels
2. **Segregation of Duties**: Different people for PR, PO, receipt, invoice, payment
3. **Exception Handling**: Document and resolve discrepancies
4. **Audit Trail**: Keep full history of POs
5. **Cost Control**: Monitor and control spending

---

## 10. Troubleshooting

### Common Issues

**Issue**: Vendor not appearing in dropdown
**Solution**: Vendor may be inactive. Check and activate vendor.

**Issue**: Cannot create PO for selected vendor
**Solution**: Verify vendor is marked as active for purchasing.

**Issue**: GRN quantity doesn't match PO quantity
**Solution**: This is acceptable if partial receipt. System allows partial receipts.

**Issue**: Invoice amount doesn't match PO amount
**Solution**: May be due to price changes or quantity differences. Review invoice against GRN.

---

## Quick Reference

### Navigation Paths

| Task | Path |
|------|------|
| Create Vendor | Procurement > Vendors > [+ New] |
| Create PR | Procurement > Purchase Requisitions > [+ New] |
| Create PO | Procurement > Purchase Orders > [+ New] |
| Receive Goods | Inventory > Stock Movements > [Goods Receipt] |
| Process Invoice | Procurement > Vendor Invoices > [+ New] |

### Key Statuses

- **Draft**: Created, not sent
- **Sent**: Sent to vendor
- **Confirmed**: Vendor confirmed
- **Partially Received**: Some items received
- **Received**: All items received
- **Invoiced**: Invoice received
- **Closed**: Complete and done

---

**Document Version**: 1.0
**Last Updated**: April 22, 2026
**Next Review**: April 22, 2027
