package com.aria.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dto.QualityInspectionDTO;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "purchase_quality_inspection_score")
public class QualityInspectionScore {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "purchase_quality_inspection_score_id")
	private Long purchaseQualityInspectionScoreId;

	@Column(name = "trn_number")
	private String trnNumber;

	@Column(name = "item_id")
	private Long itemId;

	@Column(name = "item_type")
	private Long itemType;

	@Column(name = "item_name")
	private String itemName;

	@Column(name = "po_id")
	private Long poId;

	@Column(name = "po_number")
	private String poNumber;

	@Column(name = "bill_ref_id")
	private Long billRefId;

	@Column(name = "bill_number")
	private String billNumber;

	@Column(name = "bill_date")
	private Date billDate;

	@Column(name = "due_date")
	private Date dueDate;

	@Column(name = "delivery_delay")
	private String deliveryDelay;

	@Column(name = "vendor_ref_id")
	private Long vendorRefId;

	@Column(name = "bill_amount")
	private Integer billAmount;

	@Column(name = "inward_by")
	private Long inwardBy;

	@Column(name = "inward_date")
	private Date inwardDate;

	@Column(name = "po_qty")
	private Double poQty;

	@Column(name = "supplied_qty")
	private Double suppliedQty;

	@Column(name = "pending_qty")
	private Double pendingQty;

	@Column(name = "number_off_items")
	private Double numberOffItems;

	@Column(name = "delivery_score")
	private Double deliveryScore;

	@Column(name = "loyality_score")
	private Double loyaltyScore;

	@Column(name = "qc_score")
	private Double qcScore;

	@Column(name = "total_qc_score")
	private Double totalQcScore;

	@Column(name = "total_score")
	private Double totalScore;

	@Column(name = "approve_by_id")
	private Long approveById;

	@Column(name = "email_id")
	private String emailId;

	@Column(name = "email_status_flag")
	private String emailStatusFlag;

	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "active_flag")
	private Integer activeFlag;

	@Column(name = "created_by_id")
	private Long createdById;

	@Column(name = "department")
	private Integer department;

	@Column(name = "company")
	private Long company;

	@Column(name = "branch")
	private Long branch;

	@Column(name = "date")
	private Date date;

	@Column(name = "box_compression")
	private String boxCompression;

	@Column
	private String box_compression_path_value;

	public QualityInspectionScore(QualityInspectionDTO model) {
		DateUtil dateUtil = new DateUtil();
		this.purchaseQualityInspectionScoreId = model.getPurchaseQualityInspectionScoreId();
		this.trnNumber = model.getTrnNumber();
		this.itemId = model.getItemId();
		this.itemName = model.getItemName();
		this.itemType = model.getItemType();
		this.poId = model.getPoId();
		this.poNumber = model.getPoNumber();
		this.billRefId = model.getBillRefId();
		this.billNumber = model.getBillNumber();
		this.deliveryDelay = model.getDeliveryDelay();
		if (model.getBillDate() != null) {
			this.billDate = dateUtil.getStringTODateWk(model.getBillDate());
		}

		if (model.getDueDate() != null) {
			this.dueDate = dateUtil.getStringTODateWk(model.getDueDate());
		}
		this.vendorRefId = model.getVendorRefId();
		this.billAmount = model.getBillAmount();
		this.inwardBy = model.getInwardBy();
		if (model.getInwardDate() != null) {
			this.inwardDate = dateUtil.getStringTODateWk(model.getInwardDate());
		}
		this.poQty = model.getPoQty();
		this.suppliedQty = model.getSuppliedQty();
		this.pendingQty = model.getPendingQty();
		this.totalQcScore = model.getTotalQcScore();
		this.numberOffItems = model.getNumberOffItems();
		this.deliveryScore = model.getDeliveryScore();
		this.loyaltyScore = model.getLoyaltyScore();
		this.qcScore = model.getQcScore();
		this.totalScore = model.getTotalScore();
		this.approveById = model.getApproveById();
		this.emailId = model.getEmailId();
		this.emailStatusFlag = model.getEmailStatusFlag();
		this.mobileNumber = model.getMobileNumber();
		this.activeFlag = model.getActiveFlag();
		this.createdById = model.getCreatedById();
		this.department = model.getDepartment();
		this.company = model.getCompany();
		this.branch = model.getBranch();
		if (model.getDate() != null) {
			this.date = dateUtil.getStringTODateWk(model.getDate());
		}
		this.boxCompression = model.getBoxCompression();
		ObjectMapper mapper = new ObjectMapper();
		try {
			this.box_compression_path_value = mapper.writeValueAsString(model.getBoxCompressionPathValue());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
