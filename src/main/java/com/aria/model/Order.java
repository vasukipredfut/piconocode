package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.OrderDto;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Table(name = "dpatch_billorder")
@Getter
@Setter
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ordernumber")
	private Long orderNumber;

	@Column(name = "order_billdate")
	private Date billDate;

	@Column(name = "order_billno")
	private String billNo;

	@OneToOne
	@JoinColumn(name = "vendor_id", referencedColumnName = "id")
	private Vendor vendorId;

	@Column(name = "dpatch_against_type_id")
	private Long dpatchAgainstTypeId;

	@OneToOne
	@JoinColumn(name = "created_by", referencedColumnName = "id")
	private User createdBy;

	@Column(name = "created_date")
	private LocalDateTime createdDate;

	@Column(name = "order_status")
	private String orderStatus;

	@Column
	private Integer active;

	@OneToMany(mappedBy = "orderId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<DpatchBillorderItems> dispatchItem;

	@Column(name = "firm_id")
	private Long firmId;

	@Column(name = "branch_id")
	private Long branchId;


	public Order(OrderDto orderDto) {
		DateUtil dateUtil = new DateUtil();
		if (orderDto.getOrderNumber() != null)
			this.orderNumber = orderDto.getOrderNumber();

		this.billDate = dateUtil.getStringTODateWk(orderDto.getBillDate());
		if (orderDto.getBillNo() != null && orderDto.getBillNo() != "")
			this.billNo = orderDto.getBillNo();

		if (orderDto.getVendorId() != null) {
			Vendor vendor = new Vendor();
			vendor.setId(orderDto.getVendorId());
			this.vendorId = vendor;
		}

		if (orderDto.getBillNo() != null && orderDto.getBillNo() != "")
			this.dpatchAgainstTypeId = 2L;
		else
			this.dpatchAgainstTypeId = 1L;

		this.orderStatus = orderDto.getOrderStatus();
		this.createdDate = orderDto.getCreatedDate();
		this.active = 0;
	}

}
