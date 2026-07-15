package com.aria.dto;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.DispatchAgainstBill;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class DispatchAgainstBillDto {

	private Long id;

	
	private String billnumber;

	
	private String billDate;

	private Long branch;

    private String branchName;
	
	private String vendorName;

	private Long enteredBy;
	
	private String enteredByName;

	private Long createdBy;

	
	private Long updatedBy;

	
	private LocalDateTime created_date;

	
	private LocalDateTime updated_date;

	private Map<String,Object> dispatchAgainstBillValue;

	
	

	public DispatchAgainstBillDto(DispatchAgainstBill dispatchAgainstBill) {
		this.id = dispatchAgainstBill.getId();
		this.billnumber = dispatchAgainstBill.getBillnumber();
		if (dispatchAgainstBill.getBranch() != null) {
			 this.branch = dispatchAgainstBill.getBranch().getId();
	            this.branchName = dispatchAgainstBill.getBranch().getBranchName();

		}

		if (dispatchAgainstBill.getEnteredBy() != null) {
			 this.enteredBy = dispatchAgainstBill.getEnteredBy().getId();
	            this.enteredByName = dispatchAgainstBill.getEnteredBy().getEmployeename();
		}
		
		this.vendorName = dispatchAgainstBill.getVendorName();
		this.createdBy = dispatchAgainstBill.getCreatedBy();
		this.updatedBy = dispatchAgainstBill.getUpdatedBy();
		this.created_date = dispatchAgainstBill.getCreated_date();
		this.updated_date = dispatchAgainstBill.getUpdated_date();
		ObjectMapper mapper = new ObjectMapper();
		 try {
	            this.dispatchAgainstBillValue = mapper.readValue(dispatchAgainstBill.getDispatchAgainstBillValue(), HashMap.class);
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	}
}