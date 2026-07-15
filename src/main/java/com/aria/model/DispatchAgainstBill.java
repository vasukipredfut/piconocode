package com.aria.model;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dto.DispatchAgainstBillDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@NoArgsConstructor
@Table(name = "dispatch_against_bill")
@Getter
@Setter
public class DispatchAgainstBill {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String billnumber;

	@Column(name = "bill_date")
	private Date billDate;

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

	@Column(name = "dispatch_against_bill_value")
	private String dispatchAgainstBillValue;

	public DispatchAgainstBill(DispatchAgainstBillDto dispatchAgainstBill) {
		this.id = dispatchAgainstBill.getId();
		this.billnumber = dispatchAgainstBill.getBillnumber();
		if (dispatchAgainstBill.getBranch() != null) {
			Branch branch = new Branch();
			branch.setId(dispatchAgainstBill.getBranch());
			this.branch = branch;
		}

		if (dispatchAgainstBill.getEnteredBy() != null) {
			User user = new User();
			user.setId(dispatchAgainstBill.getEnteredBy());
			this.enteredBy = user;
		}
		
		this.vendorName = dispatchAgainstBill.getVendorName();
		this.createdBy = dispatchAgainstBill.getCreatedBy();
		this.updatedBy = dispatchAgainstBill.getUpdatedBy();
		this.created_date = dispatchAgainstBill.getCreated_date();
		this.updated_date = dispatchAgainstBill.getUpdated_date();
		ObjectMapper mapper = new ObjectMapper();
		try {
			this.dispatchAgainstBillValue = mapper
					.writeValueAsString(dispatchAgainstBill.getDispatchAgainstBillValue());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}