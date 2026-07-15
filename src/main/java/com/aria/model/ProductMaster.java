package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.aria.dto.HoProductDto;
import com.aria.dto.ProductMasterDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "products_master")
public class ProductMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String productName;

	@Column(name = "productcode")
	private String productCode;

	@Column(name = "category")
	private String productType;

	@ManyToOne(optional = false)
	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumn(name = "dept_id", referencedColumnName = "id", nullable = true)
	private DepartmentDetails department;

	@Column
	private String maintain;

	@Column
	private String description;

	@Column
	private String unit;

	@Column
	private String altunit;

	@Column
	private String where1; // main value

	@Column
	private String unit1;

	@Column
	private String where2; // alt value

	@Column
	private String unit2;

	@Column(name = "bom")
	private String billOfMaterial;

	@Column(name = "bye")
	private String byeProduct;

	@Column(name = "productcategory")
	private String productCategory;

	@Column(name = "packingtype")
	private String packingType;

	@Column
	private String picture;

	@Column
	private String picturename;

	@Column
	private int picturesize;

	@Column
	private String picturetype;

	@Column
	private String picturewidth;

	@Column
	private String pictureheight;

	@Column
	private String gweight;

	@Column
	private String search;

	@Column(name = "rawmaterial")
	private String rawMaterial;

	@Column
	private String inward;

	@Column
	private String purpose;

	@Column
	private String workinst;

	@Column
	private String avgqty1;

	@Column
	private String avgqty2;

	@Column
	private String avgqty3;

	@Column
	private String pofreq;

	@Column(name = "minoq")
	private String minimumStockLevelQuantity;

	@Column(name = "taskcategory")
	private String taskCategory;

	@Column
	private String cluster;

	@Column(name = "allowadd")
	private String allowAdd;

	@Column(name = "contractpurchase")
	private String contractPurchase;

	@Column
	private String tcid;

	@Column(name = "final")
	private String finall;

	@Column
	private String recycle;

	@Column
	private int vat;

	@Column
	private int cst;

	@Column(name = "library_flag")
	private String libraryFlag;

	@Column(name = "hsn_Ref_Id")
	private String hsnRefId;

	@Column(updatable = false, name = "createdby")
	private Long createdBy;

	@Column(name = "updatedby")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@Column
	private Boolean blending;

	@OneToOne
	@JoinColumn(name = "purchase_responsibility_plant", referencedColumnName = "id")
	private Branch purchaseResponsibilityPlant;

	@Column(name = "pcategory")
	private String pcategory;

	@Column(name = "stock_ledger")
	private Boolean stockLedger;

	@Column(name = "ho_status")
	private String hoStatus;

	@Column(name = "send_ft_to_ho_object")
	private String send_ft_to_ho_object;

	@Column(name = "piece_quantity")
	private Integer pieceQuantity;

	@Column
	private Boolean split_config;

	@Column
	private String split_config_based;

	@Column(updatable = false)
	private String product_category_type;

	@Column
	private String standing_order;

	@Column
	private String standing_authorise;
	
//	20-08-2025 created by dhivya(ASRS related column changes), discussed by subhash sir
	@Column(name = "pallet_quantity")
	private Long palletQuantity;
	
	@Column(name = "product_weight")
	private Double ProductWeight;
	
	@Column(name = "asrs_product_type")
	private String asrsProductType;


	public ProductMaster(ProductMasterDto product) {
		this.id = product.getId();
		this.productName = product.getProductName();
		this.productType = product.getProductType();
		if (product.getDepartmentId() != null && product.getDepartmentId() != 0) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(product.getDepartmentId());
			this.department = departmentDetails;
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
		this.productCategory = product.getProductCategory();
		this.packingType = product.getPackingType();
		this.picture = product.getPicture();
		this.picturename = product.getDepartmentName();
		this.picturesize = product.getPictureSize();
		this.picturetype = product.getPictureType();
		this.picturewidth = product.getPictureWidth();
		this.pictureheight = product.getPictureHeight();
		this.gweight = product.getGweight();
		this.search = product.getSearch();
		this.rawMaterial = product.getRawMaterial();
		this.inward = product.getInward();
		this.purpose = product.getPurpose();
		this.workinst = product.getWorkInst();
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
		if (product.getPurchaseResponsibilityPlant() != null && product.getPurchaseResponsibilityPlant() != 0) {
			Branch branch = new Branch();
			branch.setId(product.getPurchaseResponsibilityPlant());
			this.purchaseResponsibilityPlant = branch;
		}
		this.pcategory = product.getPcategory();
		this.stockLedger = product.getStockLedger();
		this.pieceQuantity = product.getPieceQuantity();
		this.split_config = product.getSplitConfig();
		this.split_config_based = product.getSplitConfigbased();
		this.product_category_type = product.getProductCategoryType();
		this.standing_order = product.getStandingOrder();
		this.standing_authorise = product.getStandingOrderAuth();
		this.palletQuantity=product.getPalletQuantity();
		this.ProductWeight=product.getProductWeight();
		this.asrsProductType=product.getAsrsProductType();
	}

	public ProductMaster(HoProductDto product) {
		this.id = product.getId();
		this.productName = product.getProductName();
		this.productType = product.getProductType();
		if (product.getDepartmentId() != null && product.getDepartmentId() != 0) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(product.getDepartmentId());
			this.department = departmentDetails;
		}
		this.maintain = product.getMaintain();
		this.description = product.getDescription();
		this.unit = product.getMainUnit();
		this.altunit = product.getAltunit();
		this.where1 = product.getMainValue();
		this.unit1 = product.getMainUnit1();
		this.where2 = product.getAltValue();
		this.unit2 = product.getAltUnit1();
		this.billOfMaterial = product.getBillOfMaterial();
		this.byeProduct = product.getByeProduct();
		this.productCategory = product.getProductCategory();
		this.packingType = product.getPackingType();
		this.picture = product.getPicture();
		this.picturename = product.getDepartmentName();
		this.gweight = product.getGrossWeight();
		this.search = product.getNikeNameSearch();
		this.rawMaterial = product.getRawMaterial();
		this.inward = product.getInward();
		this.purpose = product.getPurpose();
		this.workinst = product.getWorkInst();
		this.avgqty1 = product.getPeakMonthAvgQty1();
		this.avgqty2 = product.getPeakMonthAvgQty2();
		this.avgqty3 = product.getPeakMonthAvgQty3();
		this.pofreq = product.getStockInDays();
		this.minimumStockLevelQuantity = product.getMinimumStockLevelQuantity();
		this.taskCategory = product.getTaskCategory();
		this.allowAdd = product.getAllowProcessIntakeIfNoQcReport();
		this.contractPurchase = product.getContractPurchase();
		this.tcid = product.getTcid();
		this.recycle = product.getRecycle();
		this.product_category_type = product.getProductCategoryType();
		if (product.getPurchaseResponsibilityPlant() != null && product.getPurchaseResponsibilityPlant() != 0) {
			Branch branch = new Branch();
			branch.setId(product.getPurchaseResponsibilityPlant());
			this.purchaseResponsibilityPlant = branch;
		}
	}
}
