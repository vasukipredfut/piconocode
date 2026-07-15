package com.aria.model;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.aria.dto.DispatchOrderWithPriceDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@NoArgsConstructor
@Table(name = "dispatch_order_withprice")
@Getter
@Setter
public class DispatchOrderWithPrice  {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_number")
	private Long orderNumber;

	@Column(name = "order_date")
	private Date orderDate;

	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch branch;

	@Column(name = "vendor_name")
	private String vendorName;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User enteredBy;

	@Column(name = "broker_name")
	private String brokerName;
	
	@Column
	private Long cess;
	
	
	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@Column(name = "dispatch_Order_With_Price_Value")
	private String dispatchOrderWithPriceValue;

	public DispatchOrderWithPrice(DispatchOrderWithPriceDto dispatchOrderWithPrice) {
		this.id = dispatchOrderWithPrice.getId();
		this.orderNumber = dispatchOrderWithPrice.getOrderNumber();
		if (dispatchOrderWithPrice.getBranch() != null) {
			Branch branch = new Branch();
			branch.setId(dispatchOrderWithPrice.getBranch());
			this.branch = branch;
		}

		if (dispatchOrderWithPrice.getEnteredBy() != null) {
			User user = new User();
			user.setId(dispatchOrderWithPrice.getEnteredBy());
			this.enteredBy = user;
		}
		
		this.vendorName = dispatchOrderWithPrice.getVendorName();
		this.brokerName = dispatchOrderWithPrice.getBrokerName();
		this.cess = dispatchOrderWithPrice.getCess();
		this.createdBy = dispatchOrderWithPrice.getCreatedBy();
		this.updatedBy = dispatchOrderWithPrice.getUpdatedBy();
		this.created_date = dispatchOrderWithPrice.getCreated_date();
		this.updated_date = dispatchOrderWithPrice.getUpdated_date();
		ObjectMapper mapper = new ObjectMapper();
		try {
			this.dispatchOrderWithPriceValue = mapper
					.writeValueAsString(dispatchOrderWithPrice.getDispatchOrderWithPriceValue());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
