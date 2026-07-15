package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.aria.model.PurchaseReturn;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseReturnDto {

	private Long id;

	private Long vendorId;

	private String vendorName;

	private Long firmId;

	private String firmName;

	private String billTransactionNo;
	
	private String billDate;

	private String billInwardDate;

	private String billNo;

	private Long createdBy;

	private String createdByName;

	private Long updatedBy;

	private String updatedByName;

	private String createdDate;

	private String createdDateTime;

	private LocalDateTime updated_date;

	private List<PurchaseReturnProductsDto> purchaseReturnProducts;

	private Long billRefId;
	
    private String status;

	private String lorrySerialNo;

	private Long branchId;

	private Long verifiedBy;
	private String verifiedByName;
	private String verifiedDate;
	private String verifiedDateTime;
	private Long rejectedBy;
	private String rejectedByName;
	private String rejectedDate;
	private String rejectedDateTime;

	private String vehicleMode;
	private String vehicleNo;
	private String vehicleType;
	    
	public PurchaseReturnDto(PurchaseReturn purchaseReturn) {

		DateUtil dateUtil = new DateUtil();

		this.id = purchaseReturn.getId();

		if (purchaseReturn.getFirmOrg() != null) {
			this.firmId = purchaseReturn.getFirmOrg().getId();
			this.firmName = purchaseReturn.getFirmOrg().getName();
		}
		if (purchaseReturn.getVendor() != null) {
			this.vendorId = purchaseReturn.getVendor().getId();
			this.vendorName = purchaseReturn.getVendor().getName();
		}
		this.billNo = purchaseReturn.getBillNo();
		this.billTransactionNo = purchaseReturn.getBillTransactionNo();
		this.createdBy = purchaseReturn.getCreatedBy();
		this.updatedBy = purchaseReturn.getUpdatedBy();

		this.verifiedBy = purchaseReturn.getVerifiedBy();
		this.rejectedBy = purchaseReturn.getRejectedBy();

		this.updated_date = purchaseReturn.getUpdated_date();
		this.status = purchaseReturn.getStatus();
		this.lorrySerialNo = purchaseReturn.getLorrySerialNo();
		this.branchId = purchaseReturn.getBranchId();
		this.purchaseReturnProducts = purchaseReturn.getPurchaseReturnProducts() != null
				? purchaseReturn.getPurchaseReturnProducts().stream().map(field -> new PurchaseReturnProductsDto(field))
						.collect(Collectors.toList())
				: null;

		if (purchaseReturn.getCreated_date() != null) {
			this.createdDate = dateUtil.getLocalDateTimeToDateString(purchaseReturn.getCreated_date());
			this.createdDateTime = dateUtil.getLocalDateTimeToDateTimeString(purchaseReturn.getCreated_date());
		}

		if (purchaseReturn.getRejected_date() != null) {
			this.rejectedDate = dateUtil.getLocalDateTimeToDateString(purchaseReturn.getRejected_date());
			this.rejectedDateTime = dateUtil.getLocalDateTimeToDateTimeString(purchaseReturn.getRejected_date());
		}

		if (purchaseReturn.getVerified_date() != null) {
			this.verifiedDate = dateUtil.getLocalDateTimeToDateString(purchaseReturn.getVerified_date());
			this.verifiedDateTime = dateUtil.getLocalDateTimeToDateTimeString(purchaseReturn.getVerified_date());
		}

		this.vehicleMode = purchaseReturn.getVehicleMode();
		this.vehicleNo = purchaseReturn.getVehicleNo();
		this.vehicleType = purchaseReturn.getVehicleType();
	}
}
