package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.aria.model.Order;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OrderDto {

	private Long id;

	private Long orderNumber;

	private String billDate;

	private String billNo;

	private Long vendorId;

	private Long dpatchAgainstTypeId;

	private Long createdBy;

	private String createdPerson;

	private String orderStatus;

	private LocalDateTime createdDate;

	private String vendorName;

	private List<DpatchBillorderItemsDto> dpatchBillorderItems;

	public OrderDto(Order order) {
		this.id = order.getId();
		DateUtil dateUtil = new DateUtil();
		if (order.getOrderNumber() != null)
			this.orderNumber = order.getOrderNumber();

		this.billDate = dateUtil.getDateTOStringWk(order.getBillDate());

		if (order.getBillNo() != null && order.getBillNo() != "")
			this.billNo = order.getBillNo();
		if (order.getVendorId() != null) {
			this.vendorId = order.getVendorId().getId();
			this.vendorName = order.getVendorId().getName();
		}
		if (order.getBillNo() != null && order.getBillNo() != "")
			this.dpatchAgainstTypeId = 2L;
		else
			this.dpatchAgainstTypeId = 1L;
		if (order.getCreatedBy() != null) {
			this.createdBy = order.getCreatedBy().getId();
			this.createdPerson = order.getCreatedBy().getEmployeename();
		}
		this.orderStatus = order.getOrderStatus();
		this.createdDate = order.getCreatedDate();
	}
}
