package com.aria.dto;

import com.aria.model.Branch;
import com.aria.model.DepartmentDetails;
import com.aria.model.ProductMaster;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class HoProductDto {

    private Long id;

    private String productName;

    private String productCode;

    private String productType;

    private Long departmentId;

    private String departmentName;

    private String maintain;

    private String description;

    private String mainUnit;

    private String altunit;

    private String mainValue;

    private String mainUnit1;

    private String altValue;

    private String altUnit1;

    private String billOfMaterial;

    private String byeProduct;

    private String productCategory;

    private String packingType;

    private String picture;

    private String pictureName;


    private String picturePath;

    private String grossWeight;

    private String nikeNameSearch;

    private String rawMaterial;

    private String inward;

    private String purpose;

    private String workInst;

    private String peakMonthAvgQty1;

    private String peakMonthAvgQty2;

    private String peakMonthAvgQty3;

    private String stockInDays;

    private String minimumStockLevelQuantity;

    private String taskCategory;

    private String allowProcessIntakeIfNoQcReport;

    private String contractPurchase;

    private String tcid;

    private String recycle;

    private Long purchaseResponsibilityPlant;

    private String purchaseResponsibilityPlantName;
    private String productCategoryType;

    public HoProductDto(ProductMaster product) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.productType = product.getProductType();
        if (product.getDepartment() != null) {
            this.departmentId = product.getDepartment().getId();
            this.departmentName = product.getDepartment().getName();
        }
        this.maintain = product.getMaintain();
        this.description = product.getDescription();
        this.mainUnit = product.getUnit();
        this.altunit = product.getAltunit();
        this.mainValue = product.getWhere1();
        this.mainUnit1 = product.getUnit1();
        this.altValue = product.getWhere2();
        this.altUnit1 = product.getUnit2();
        this.billOfMaterial = product.getBillOfMaterial();
        this.byeProduct = product.getByeProduct();
        this.productCategory = product.getProductCategory();
        this.packingType = product.getPackingType();
        this.picture = product.getPicture();
        this.pictureName = product.getPicturename();
        this.grossWeight = product.getGweight();
        this.nikeNameSearch = product.getSearch();
        this.rawMaterial = product.getRawMaterial();
        this.inward = product.getInward();
        this.purpose = product.getPurpose();
        this.workInst = product.getWorkinst();
        this.peakMonthAvgQty1 = product.getAvgqty1();
        this.peakMonthAvgQty2 = product.getAvgqty2();
        this.peakMonthAvgQty3 = product.getAvgqty3();
        this.stockInDays = product.getPofreq();
        this.minimumStockLevelQuantity = product.getMinimumStockLevelQuantity();
        this.taskCategory = product.getTaskCategory();
        this.allowProcessIntakeIfNoQcReport = product.getAllowAdd();
        this.contractPurchase = product.getContractPurchase();
        this.tcid = product.getTcid();
        this.recycle = product.getRecycle();
        this.productCategoryType = product.getProduct_category_type();
        if (product.getPurchaseResponsibilityPlant() != null) {
            this.purchaseResponsibilityPlant = product.getPurchaseResponsibilityPlant().getId();
            this.purchaseResponsibilityPlantName = product.getPurchaseResponsibilityPlant().getBranchName();
        }
    }
}
