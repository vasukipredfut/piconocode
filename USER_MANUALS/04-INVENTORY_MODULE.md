# Idayam ERP - Inventory Management User Manual

## Complete Guide to Product & Stock Management

This manual covers all inventory operations including product management, stock tracking, warehouses, and stock movements.

---

## 1. Inventory Module Overview

### Inventory Module Features

The Inventory module manages all product and stock-related operations:

```
Inventory Module
├── Products Management
│   ├── Product Master
│   ├── Product Categories
│   ├── Product Pricing
│   └── Product Documents
│
├── Stock Management
│   ├── Stock Levels
│   ├── Stock Allocation
│   ├── Reorder Management
│   └── Stock Valuation
│
├── Warehouse Management
│   ├── Warehouse Setup
│   ├── Storage Bins
│   ├── Warehouse Transfers
│   └── Warehouse Capacity
│
└── Stock Movements
    ├── Stock In (Purchases)
    ├── Stock Out (Sales)
    ├── Stock Adjustment
    ├── Inventory Transfer
    └── Movement History
```

### Accessing Inventory Module

1. Click **Inventory** in main menu
2. Select desired sub-module:
   - **Products**
   - **Stock Management**
   - **Warehouses**
   - **Stock Movements**

---

## 2. Product Management

### Product Master Setup

Each product contains:

**Basic Information**:
- Product Code (unique identifier)
- Product Name
- Product Category
- Unit of Measurement

**Pricing Information**:
- Unit Price (selling price)
- Cost Price (purchase cost)
- Discount (if applicable)

**Stock Information**:
- Reorder Level (minimum stock)
- Reorder Quantity
- Current Stock Level

**Status**:
- Active/Inactive
- Discontinued status

### Creating New Product

1. Go to **Inventory** > **Products**
   - **Path**: Click "Inventory" in left menu → Click "Products"
   
2. Click **[+ New Product]**
   - **Location**: Top right corner, green button
   - **Result**: New form opens for product data entry

3. Fill **Product Information**:
   - **Product Code*** (required): Unique identifier
     - **Clarification**: System identifier for the product
     - **Format**: Usually 6-8 characters (e.g., PROD001, SKU-ABC-123)
     - **Important**: Must be unique - no two products can have same code
     - **Rule**: Cannot contain special characters (!, @, #, etc.)
     - **Error if duplicate**: "Product code already exists"
     - **Tip**: Use meaningful codes that indicate product type (e.g., SHIRT-001, WIDGET-A)
     
   - **Product Name*** (required): Full descriptive name
     - **Clarification**: Name displayed in reports and lists
     - **Examples**: 
       - "Cotton T-Shirt, Size M, Blue"
       - "Industrial Pump, 2HP, 220V"
     - **Best practice**: Include size, color, specifications if applicable
     - **Why important**: Helps users quickly identify product
     
   - **Category*** (required): Product classification
     - **Clarification**: Groups similar products together
     - **Examples**: Electronics, Clothing, Raw Materials, Components
     - **How to select**: 
       - Click dropdown to see available categories
       - Select matching category
       - If not found, can create new category (click "Add New")
     - **Why important**: Used for filtering, reporting, and organization
     
   - **Unit*** (required): Measurement unit
     - **Clarification**: How product is measured/sold
     - **Common units**:
       - Pieces (PCS) - for individual items
       - Kilograms (KG) - for weight
       - Liters (L) - for volume/liquids
       - Meters (M) - for length
       - Box, Dozen, Bag, etc.
     - **Important**: Must be consistent (e.g., always sell in Kg, not mix Kg and lbs)
     
4. Fill **Pricing**:
   - **Unit Price*** (required): Selling price to customer
     - **Clarification**: Price per unit (e.g., per piece, per Kg)
     - **Examples**: $5.00, $125.50
     - **Used for**: Calculating sale revenue, customer invoices
     - **Tip**: Must be higher than cost price to make profit
     
   - **Cost Price**: Purchase cost from supplier
     - **Clarification**: How much you pay to buy this product
     - **Examples**: $3.00, $75.00
     - **Used for**: Calculating profit margin, inventory valuation
     - **Tip**: Used in profit calculations: Profit = Selling Price - Cost Price
     
   - **Tax** (optional): Tax percentage if applicable
     - **Clarification**: VAT or GST rate for this product
     - **Examples**: 0%, 5%, 12%, 18%
     - **Note**: System automatically calculates tax on sales

5. Fill **Stock Parameters**:
   - **Reorder Level*** (required): Minimum stock to maintain
     - **Clarification**: When stock falls below this, warning appears
     - **Example**: If set to 50, system warns when stock < 50
     - **Why important**: Prevents running out of stock
     - **How to set**: Based on sales volume and lead time
       - Fast-moving product: 100+ units
       - Slow-moving product: 10-20 units
     
   - **Reorder Quantity**: Amount to order when stock is low
     - **Clarification**: How many units to purchase when stock hits reorder level
     - **Example**: 
       - Reorder Level: 50
       - Reorder Quantity: 500
       - When stock drops to 50, system suggests order of 500 more
     - **Why important**: Ensures you always have enough stock
     - **How to set**: Based on:
       - Supplier lead time (how long to get delivery)
       - Average sales per day
       - Storage capacity
     
6. **Status**: Check **[Active]** to enable product
   - **Clarification**: Active products appear in all lists and sales
   - **Inactive**: Products are hidden (useful for discontinued items)
   
7. Click **[Save]**
   - **What happens**: 
     - Product created, success message: "Product created successfully"
     - Redirects to product details page
     - Product now available for use in stock movements and sales
     
**Creating Product - Common Mistakes**:
| Mistake | Problem | Fix |
|---------|---------|-----|
| Duplicate product code | Cannot save, error shown | Use unique code (add -01, -02, etc.) |
| Empty required fields | Cannot save, fields highlighted | Fill all fields marked with * |
| Unit Price = 0 | Product appears free in reports | Enter correct selling price |
| Reorder Level too high | System constantly shows low stock alerts | Adjust based on actual sales volume |
| Typo in product name | Hard to find in future searches | Re-edit and correct name

### Product Categories

**Categories Help Organize Products**:
- Raw Materials
- Finished Goods
- Work in Progress
- Consumables
- Others

**Create New Category**:
1. Go to **Inventory** > **Products** > **Categories**
2. Click **[+ New Category]**
3. Enter **Category Name**
4. Enter **Description**
5. Click **[Save]**

### Viewing Product Details

1. Go to **Inventory** > **Products**
2. Search or filter products
3. Click product name to view details
4. See all information in read-only mode
5. Click **[Edit]** to modify

### Editing Product

1. Open product
2. Click **[Edit]**
3. Modify fields:
   - Pricing can be changed
   - Reorder levels adjustable
   - Status can be toggled
4. Click **[Save]**
5. Changes apply immediately

### Product Reports

**Available Reports**:
1. **Product Inventory**: All products with stock levels
2. **Low Stock Alert**: Products below reorder level
3. **Stock Valuation**: Value of inventory
4. **Product Sales**: Sales quantity by product
5. **Movement History**: Complete stock history

---

## 3. Stock Management

### Understanding Stock

**Stock Levels**:
- **In Stock**: Quantity available
- **Reserved**: Allocated to orders
- **Free Stock**: Available for use (In Stock - Reserved)
- **On Order**: Quantity being received

### Checking Stock Levels

**Inventory Dashboard**:
1. Go to **Inventory** > **Stock Management**
2. View all products with:
   - Current stock quantity
   - Reorder level
   - Stock status (OK, Low, Critical)
   - Last movement date
3. Click product for detailed view

**Stock Detail View**:
1. Click on specific product
2. See:
   - Total stock across all warehouses
   - Stock by warehouse/bin
   - Recent movements
   - Upcoming receipts
   - Pending orders

### Reorder Management

**Low Stock Alert**:
- System alerts when stock falls below reorder level
- Appears in **Inventory Dashboard**
- Email notification sent to responsible person

**Automatic Purchase Order**:
1. Go to **Inventory** > **Stock Management**
2. Filter: **[Show Low Stock Only]**
3. For each low-stock item:
   - Click **[Create PO]**
   - System creates Purchase Order
   - Quantity = Reorder Quantity
   - Send to procurement

**Manual Reorder**:
1. Open product
2. Click **[Reorder]**
3. Enter quantity (defaults to reorder qty)
4. Select vendor
5. Click **[Create Purchase Order]**

### Stock Adjustment

**Stock Take / Physical Verification**:

1. Go to **Inventory** > **Stock Management**
2. Click **[Physical Stock Take]**
3. Select **Warehouse** and **Date Range**
4. System shows expected stock
5. Manually count physical stock
6. Enter **Actual Quantity** for each product
7. System calculates **Variance** (Expected - Actual)
8. Click **[Review]** to verify
9. Click **[Post Adjustment]** to update system

**Adjustment Reasons**:
- Inventory loss
- Damage
- Obsolescence
- Count error
- Theft

### Stock by Warehouse

**Multi-Warehouse Management**:

1. Go to **Inventory** > **Warehouses**
2. View all warehouses with stock totals
3. Click warehouse to see:
   - All products stored
   - Stock in bins
   - Warehouse capacity usage
   - Recent transfers

**Transfer Stock Between Warehouses**:

1. Go to **Inventory** > **Stock Movements** > **[Warehouse Transfer]**
2. Click **[+ New Transfer]**
3. Fill:
   - **From Warehouse** *: Source warehouse
   - **To Warehouse** *: Destination warehouse
   - **Product** *: Select product
   - **Quantity** *: Amount to transfer
   - **Reason**: Transfer reason (if needed)
4. Click **[Review]**
5. Click **[Post Transfer]**
6. Stock updated in both warehouses

---

## 4. Warehouse Management

### Warehouse Structure

**Warehouse Components**:
- Warehouse location
- Storage bins/racks
- Capacity and current usage
- Products stored

### Setting Up Warehouse

1. Go to **Inventory** > **Warehouses**
2. Click **[+ New Warehouse]**
3. Enter **Warehouse Details**:
   - **Warehouse Name** *
   - **Location**: Address and details
   - **Manager**: Responsible person
   - **Capacity**: Total storage capacity
   - **Status**: Active/Inactive
4. Click **[Save]**

### Creating Storage Bins

Bins organize products within warehouse:

1. Open Warehouse
2. Click **[+ Add Bin]**
3. Enter:
   - **Bin Code**: Unique identifier
   - **Bin Location**: Physical location
   - **Capacity**: Max quantity
   - **Product Type**: Type of products stored
4. Click **[Save]**

### Warehouse Transfers

**Types of Transfers**:
- Between warehouses
- Within warehouse (different bins)
- From warehouse to site

**Record Transfer**:
1. Go to **Stock Movements** > **[Warehouse Transfer]**
2. Click **[+ New]**
3. Select **From Warehouse** and **To Warehouse**
4. Enter products and quantities
5. Click **[Post]**
6. Transfer recorded in history

### Warehouse Reports

**Available Reports**:
1. **Warehouse Stock**: All products in each warehouse
2. **Warehouse Capacity**: Utilization report
3. **Stock Location**: Which product in which bin
4. **Warehouse Movements**: Transfer history
5. **Warehouse Valuation**: Warehouse-wise inventory value

---

## 5. Stock Movements

### Types of Stock Movements

| Movement Type | Direction | Trigger |
|---------------|-----------|---------|
| **Purchase Receipt** | In | Goods receipt from vendor |
| **Sales Order Fulfillment** | Out | Fulfilling sales order |
| **Stock Return** | In | Return from customer |
| **Stock Adjustment** | In/Out | Physical count adjustment |
| **Warehouse Transfer** | In/Out | Moving between warehouses |
| **Sample Issue** | Out | Samples for testing |
| **Scrap/Loss** | Out | Damaged or expired goods |

### Recording Stock In (Purchase)

**When Purchase Order is Received**:

1. Go to **Inventory** > **Stock Movements** > **[Goods Receipt]**
2. Click **[+ Receive Goods]**
3. Enter **PO Number** or select from list
4. System shows expected items and quantities
5. Enter **Received Quantity** for each item
6. Check items for:
   - Quality
   - Quantity match
   - No damage
7. Enter **Receipt Date**
8. Add **Notes** if needed
9. Click **[Post Receipt]**
10. Stock updated in system

### Recording Stock Out (Sales)

**When Fulfilling Sales Order**:

1. Go to **Inventory** > **Stock Movements**
2. Click **[+ Issue Stock]**
3. Enter **Sales Order Number**
4. System shows items to be shipped
5. Verify quantity in stock
6. If stock insufficient:
   - Partial fulfillment or
   - Wait for new stock arrival
7. Enter **Issue Date**
8. Click **[Post Issue]**
9. Stock decremented from warehouse

### Stock Return Management

**Customer Returns Stock**:

1. Go to **Inventory** > **Stock Movements** > **[Stock Return]**
2. Click **[+ New Return]**
3. Enter **Original Sales Order** reference
4. List items being returned
5. Inspect returned goods:
   - Condition
   - Quantity verification
6. Determine **Return Reason**:
   - Defective
   - Wrong item
   - Customer requested
   - Expired
7. Click **[Accept Return]**
8. Stock added back to inventory
9. If defective, may be marked for scrap

### Stock Movement History

**Tracking Stock Movement**:

1. Go to **Inventory** > **Stock Movements** > **[History]**
2. View all movements:
   - Product name
   - Movement type
   - Quantity
   - Date
   - Warehouse
   - Reference (PO/SO number)
3. Filter by:
   - Product
   - Date range
   - Movement type
   - Warehouse

---

## 6. Stock Valuation

### Stock Valuation Methods

**FIFO (First In First Out)**:
- First received stock issued first
- Preferred method

**LIFO (Last In First Out)**:
- Last received stock issued first

**Weighted Average**:
- Average cost of all stock

### Stock Valuation Report

**Generate Report**:

1. Go to **Reports** > **Inventory Reports** > **Stock Valuation**
2. Select **Valuation Date**
3. System calculates:
   - **Quantity**: Physical stock qty
   - **Cost**: Cost price per unit
   - **Total Value**: Qty × Cost
   - **Summary**: Total inventory value
4. By warehouse or product
5. Export to Excel

**Uses**:
- Balance sheet preparation
- Insurance coverage determination
- Financial reporting
- Performance analysis

---

## 7. Common Inventory Tasks

### Task: Receive New Stock from Vendor

1. Go to **Inventory** > **Stock Movements** > **[Goods Receipt]**
2. Enter PO number
3. System lists expected items
4. Enter received quantities
5. Click **[Post Receipt]**
6. Stock automatically updated

### Task: Fulfill Sales Order

1. Go to **Inventory** > **Stock Movements** > **[Issue Stock]**
2. Enter Sales Order number
3. Verify quantities in stock
4. Click **[Post Issue]**
5. Reduces stock quantity

### Task: Adjust Inventory

1. Go to **Inventory** > **Stock Management** > **[Physical Stock Take]**
2. Count actual stock
3. Enter actual quantities
4. Click **[Post Adjustment]**
5. System updates to actual qty

### Task: Transfer Stock to Another Warehouse

1. Go to **Inventory** > **Stock Movements** > **[Warehouse Transfer]**
2. Select from and to warehouses
3. Enter product and quantity
4. Click **[Post Transfer]**
5. Updates both warehouse stocks

### Task: Set Reorder Level

1. Open product
2. Click **[Edit]**
3. Update **Reorder Level** and **Reorder Quantity**
4. Click **[Save]**
5. System alerts when stock falls below level

---

## 8. Inventory Reports

### Key Reports

**Stock Related**:
- **Current Stock**: All products with current levels
- **Stock by Warehouse**: Stock location details
- **Low Stock Alert**: Products below reorder level
- **Stock Aging**: Old stock identification

**Movement Related**:
- **Stock In Report**: All purchases received
- **Stock Out Report**: All stock issues
- **Stock Transfers**: Warehouse transfers
- **Return Report**: Customer returns

**Financial Reports**:
- **Stock Valuation**: Total inventory value
- **Stock Movement Value**: Cost of movements
- **Inventory Variance**: Discrepancies

### Generating Reports

1. Go to **Reports** > **Inventory Reports**
2. Select **Report Type**
3. Set filters:
   - Date range
   - Product/Category
   - Warehouse
   - Status
4. Click **[Generate]**
5. View, export, or print

---

## 9. Inventory Best Practices

### Stock Management Best Practices

1. **Regular Stock Verification**: Conduct physical stock takes quarterly
2. **Accurate Reorder Levels**: Based on demand patterns
3. **First-In-First-Out**: Rotate stock to prevent aging
4. **Proper Storage**: Store products according to type
5. **Timely Recording**: Record movements immediately

### Warehouse Best Practices

1. **Organized Storage**: Clear bin identification
2. **Capacity Planning**: Don't exceed warehouse capacity
3. **Safety**: Follow safety protocols
4. **Access Control**: Restrict warehouse access
5. **Regular Maintenance**: Keep warehouse clean

### Inventory Control Best Practices

1. **Cycle Counts**: Regular counting of specific items
2. **ABC Analysis**: Focus on high-value items
3. **Obsolescence Management**: Identify and scrap old stock
4. **Lead Time Monitoring**: Track vendor delivery times
5. **Demand Forecasting**: Predict future demand

---

## 10. Troubleshooting

### Common Issues

**Issue**: Stock quantity doesn't match physical count
**Solution**: Conduct physical stock take and post adjustment

**Issue**: Cannot reduce stock below zero
**Solution**: Stock available is insufficient, receive stock first

**Issue**: Warehouse capacity exceeded
**Solution**: Transfer stock to another warehouse or expand capacity

**Issue**: Reorder alerts not appearing
**Solution**: Verify reorder level is set correctly

---

## Quick Reference

### Navigation Paths

| Task | Path |
|------|------|
| View Stock Levels | Inventory > Stock Management |
| Create Product | Inventory > Products > [+ New] |
| Receive Stock | Inventory > Stock Movements > [Goods Receipt] |
| Issue Stock | Inventory > Stock Movements > [Issue Stock] |
| Transfer Warehouse | Inventory > Stock Movements > [Transfer] |
| Physical Stock Take | Inventory > Stock Management > [Stock Take] |

### Key Fields

| Field | Purpose |
|-------|---------|
| Product Code | Unique identifier |
| Reorder Level | Minimum stock threshold |
| Unit Price | Selling price |
| Cost Price | Purchase cost |
| Quantity | Current stock quantity |

---

**Document Version**: 1.0
**Last Updated**: April 22, 2026
**Next Review**: April 22, 2027
