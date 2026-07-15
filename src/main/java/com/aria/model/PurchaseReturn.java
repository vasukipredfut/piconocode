package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.aria.dto.PurchaseReturnDto;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "purchase_return")
public class PurchaseReturn {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "vendor_id", referencedColumnName = "id")
	private Vendor vendor;

	@OneToOne
	@JoinColumn(name = "firm_id", referencedColumnName = "id")
	private Organisation firmOrg;

	@Column(name = "bill_No")
	private String billNo;

	@Column(name = "bill_Date")
	private Date billDate;

	@Column(name = "bill_Inward_Date")
	private Date billInwardDate;

	@Column(name = "bill_Transaction_No")
	private String billTransactionNo;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;
	
	@Column
    private String status;

	@Column(name = "lorry_Serial_No")
	private String lorrySerialNo;
	
	@OneToMany(mappedBy = "purchaseReturnId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<PurchaseReturnProducts> purchaseReturnProducts;

	@Column(name = "branch_id", updatable = false)
	private Long branchId;

	@Column(name = "verified_by")
	private Long verifiedBy;

	@Column
	private LocalDateTime verified_date;

	@Column(name = "rejected_by")
	private Long rejectedBy;

	@Column
	private LocalDateTime rejected_date;

	@Column(name = "vehicle_mode")
	private String vehicleMode;

	@Column(name = "vehicle_no")
	private String vehicleNo;

	@Column(name = "vehicle_type")
	private String vehicleType;
	@Column(name = "gdc_no")
	private String GDCNo;
	@Column
	private LocalDateTime gdc_date;



	public PurchaseReturn(PurchaseReturnDto purchaseReturn) {
		this.id = purchaseReturn.getId();

		if (purchaseReturn.getFirmId() != null) {
			Organisation organisation = new Organisation();
			organisation.setId(purchaseReturn.getFirmId());
			this.firmOrg = organisation;
		}
		if (purchaseReturn.getVendorId() != null) {
			Vendor vendor = new Vendor();
			vendor.setId(purchaseReturn.getVendorId());
			this.vendor = vendor;
		}
		this.billNo = purchaseReturn.getBillNo();
		this.billTransactionNo = purchaseReturn.getBillTransactionNo();
		this.createdBy = purchaseReturn.getCreatedBy();
		this.updatedBy = purchaseReturn.getUpdatedBy();
		this.updated_date = purchaseReturn.getUpdated_date();
		this.status = purchaseReturn.getStatus();
		this.lorrySerialNo = purchaseReturn.getLorrySerialNo();
		this.branchId = purchaseReturn.getBranchId();

		this.vehicleMode = purchaseReturn.getVehicleMode();
		this.vehicleNo = purchaseReturn.getVehicleNo();
		this.vehicleType = purchaseReturn.getVehicleType();
	}
}
