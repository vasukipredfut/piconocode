package com.aria.dto;

import java.util.Date;
import java.util.List;

import com.aria.model.DpatchDespatch;
import com.aria.model.Order;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter

public class DpatchDispatchDto {
	private Long id;

	private Long createdBy;

	private String dispatcherName;

	private Long approvedBy;

	private String approverName;

	private String billNo;

	private Long orderId;

	private String deliveryDate;

	private String trnNo;

	private String status;

	private Long orderNumber;

	private String vendorName;

	private String vendorCity;

	private Date billDate;

	private List<ProductsAndBindDto> products;

	public DpatchDispatchDto(DpatchDespatch dispatch) {
		DateUtil dateUtil = new DateUtil();
		this.id = dispatch.getId();
		if (dispatch.getCreatedBy() != null) {
			this.createdBy = dispatch.getCreatedBy().getId();
			this.dispatcherName = dispatch.getCreatedBy().getEmployeename();
		}

		if (dispatch.getApprovedBy() != null) {
			this.approvedBy = dispatch.getApprovedBy().getId();
			this.approverName = dispatch.getApprovedBy().getEmployeename();
		}
		Order billRef = dispatch.getDpatch_Bill_Ref_Id();
		this.orderId = billRef.getId();
		if (billRef.getBillNo() != null && !billRef.getBillNo().isEmpty()) {
			this.billNo = billRef.getBillNo();
		}
		if (billRef.getOrderNumber() != null) {
			this.orderNumber = billRef.getOrderNumber();
		}
		if (billRef.getVendorId() != null) {
			this.vendorName = billRef.getVendorId().getName();
			this.vendorCity = billRef.getVendorId().getCity();
		}
		this.deliveryDate = dateUtil.getDateTOStringWk(dispatch.getTrndate());
		this.trnNo = dispatch.getTrnNo();
		this.status = dispatch.getStatus();

	}
}
