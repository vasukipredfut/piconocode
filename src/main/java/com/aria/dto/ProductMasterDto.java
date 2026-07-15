package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.aria.model.ProductMaster;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductMasterDto {

	private Long id;

	private String productName;

	private String productCode;

	private String productType;

	private Long departmentId;

	private String departmentName;

	private String maintain;

	private String description;

	private String unit;
	private String lotValues;
	private String addLotValues;
	private String removeLotValues;

	private String altunit;

	private String where1;

	private String unit1;

	private String where2;

	private String unit2;

	private String billOfMaterial;

	private String byeProduct;

	private String productCategory;

	private String packingType;

	private String picture;

	private String pictureName;

	private int pictureSize;

	private String pictureType;

	private String pictureWidth;

	private String pictureHeight;

	private String gweight;

	private String search;

	private String rawMaterial;

	private String inward;

	private String purpose;

	private String workInst;

	private String avgqty1;

	private String avgqty2;

	private String avgqty3;

	private String pofreq;

	private String minimumStockLevelQuantity;

	private String taskCategory;

	private String cluster;

	private String allowAdd;

	private String contractPurchase;

	private String tcid;

	private String finall;

	private String recycle;

	private int vat;

	private int cst;

	private String libraryFlag;

	private String hsnRefId;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private Boolean blending;

	private Long purchaseResponsibilityPlant;
	private String purchaseResponsibilityPlantName;

	private String pcategory;
	private String productCategoryType;
	private Boolean stockLedger;

	private List<Long> productMappingCategory;

	private Integer pieceQuantity;

	private Boolean splitConfig;
	private String splitConfigbased;
	private String standingOrder;
	private String standingOrderAuth;
	private Long palletQuantity;
	private Double ProductWeight;
	private String asrsProductType;



	public ProductMasterDto(ProductMaster product) {
		this.id = product.getId();
		this.productName = product.getProductName();
		this.productType = product.getProductType();
		if (product.getDepartment() != null) {
			this.departmentId = product.getDepartment().getId();
			this.departmentName = product.getDepartment().getName();
		}
		this.maintain = product.getMaintain();
		this.description = product.getDescription();
		this.unit = product.getUnit();
		this.altunit = product.getAltunit();
		this.where1 = product.getWhere1();
		this.unit1 = product.getUnit1();
		this.where2 = product.getWhere2();
		this.unit2 = product.getUnit2();
		this.billOfMaterial = product.getBillOfMaterial();
		this.byeProduct = product.getByeProduct();
		this.productType = product.getProductType();
		this.productCategory = product.getProductCategory();
		this.packingType = product.getPackingType();
		this.picture = product.getPicture();
		this.pictureName = product.getProductName();
		this.pictureSize = product.getPicturesize();
		this.pictureType = product.getPicturetype();
		this.pictureWidth = product.getPicturewidth();
		this.pictureHeight = product.getPictureheight();
		this.gweight = product.getGweight();
		this.search = product.getSearch();
		this.rawMaterial = product.getRawMaterial();
		this.inward = product.getInward();
		this.purpose = product.getPurpose();
		this.workInst = product.getWorkinst();
		this.avgqty1 = product.getAvgqty1();
		this.avgqty2 = product.getAvgqty2();
		this.avgqty3 = product.getAvgqty3();
		this.pofreq = product.getPofreq();
		this.minimumStockLevelQuantity = product.getMinimumStockLevelQuantity();
		this.taskCategory = product.getTaskCategory();
		this.cluster = product.getCluster();
		this.allowAdd = product.getAllowAdd();
		this.contractPurchase = product.getContractPurchase();
		this.tcid = product.getTcid();
		this.finall = product.getFinall();
		this.recycle = product.getRecycle();
		this.vat = product.getVat();
		this.cst = product.getCst();
		this.libraryFlag = product.getLibraryFlag();
		this.hsnRefId = product.getHsnRefId();
		this.createdBy = product.getCreatedBy();
		this.updatedBy = product.getUpdatedBy();
		this.created_date = product.getCreated_date();
		this.updated_date = product.getUpdated_date();
		this.blending = product.getBlending();
		this.pcategory = product.getPcategory();
		this.stockLedger = product.getStockLedger();
		this.pieceQuantity = product.getPieceQuantity();
		this.splitConfig = product.getSplit_config();
		this.splitConfigbased = product.getSplit_config_based();
		this.productCategoryType = product.getProduct_category_type();

		if (product.getPurchaseResponsibilityPlant() != null) {
			this.purchaseResponsibilityPlant = product.getPurchaseResponsibilityPlant().getId();
			this.purchaseResponsibilityPlantName = product.getPurchaseResponsibilityPlant().getBranchName();
		}

		this.standingOrder = product.getStanding_order();
		this.standingOrderAuth = product.getStanding_authorise();
		this.palletQuantity=product.getPalletQuantity();
		this.ProductWeight=product.getProductWeight();
		this.asrsProductType=product.getAsrsProductType();
	}
}
