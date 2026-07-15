package com.aria.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.aria.dto.DispatchAgainstOrderDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@NoArgsConstructor
@Table(name = "dispatch_against_order")
@Getter
@Setter
public class DispatchAgainstOrder {

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

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@Column(name = "dispatch_Against_Order_Value")
	private String dispatchAgainstOrderValue;

	public DispatchAgainstOrder(DispatchAgainstOrderDto dispatchAgainstOrder) {
		this.id = dispatchAgainstOrder.getId();
		this.orderNumber = dispatchAgainstOrder.getOrderNumber();
		if (dispatchAgainstOrder.getBranch() != null) {
			Branch branch = new Branch();
			branch.setId(dispatchAgainstOrder.getBranch());
			this.branch = branch;
		}

		if (dispatchAgainstOrder.getEnteredBy() != null) {
			User user = new User();
			user.setId(dispatchAgainstOrder.getEnteredBy());
			this.enteredBy = user;
		}
		
		this.vendorName = dispatchAgainstOrder.getVendorName();
		this.createdBy = dispatchAgainstOrder.getCreatedBy();
		this.updatedBy = dispatchAgainstOrder.getUpdatedBy();
		this.created_date = dispatchAgainstOrder.getCreated_date();
		this.updated_date = dispatchAgainstOrder.getUpdated_date();
		ObjectMapper mapper = new ObjectMapper();
		try {
			this.dispatchAgainstOrderValue = mapper
					.writeValueAsString(dispatchAgainstOrder.getDispatchAgainstOrderValue());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
