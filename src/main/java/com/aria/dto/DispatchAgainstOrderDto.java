package com.aria.dto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.DispatchAgainstOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class DispatchAgainstOrderDto {

	private Long id;

	
	private Long orderNumber;

	
	private String orderDate;

	private Long branch;

    private String branchName;
	
	private String vendorName;

	private Long enteredBy;
	
	private String enteredByName;

	private Long createdBy;

	
	private Long updatedBy;

	
	private LocalDateTime created_date;

	
	private LocalDateTime updated_date;

	private Map<String,Object> dispatchAgainstOrderValue;

	
	

	public DispatchAgainstOrderDto(DispatchAgainstOrder dispatchAgainstOrder) {
		this.id = dispatchAgainstOrder.getId();
		this.orderNumber = dispatchAgainstOrder.getOrderNumber();
		if (dispatchAgainstOrder.getBranch() != null) {
			 this.branch = dispatchAgainstOrder.getBranch().getId();
	            this.branchName = dispatchAgainstOrder.getBranch().getBranchName();

		}

		if (dispatchAgainstOrder.getEnteredBy() != null) {
			 this.enteredBy = dispatchAgainstOrder.getEnteredBy().getId();
	            this.enteredByName = dispatchAgainstOrder.getEnteredBy().getEmployeename();
		}
		
		this.vendorName = dispatchAgainstOrder.getVendorName();
		this.createdBy = dispatchAgainstOrder.getCreatedBy();
		this.updatedBy = dispatchAgainstOrder.getUpdatedBy();
		this.created_date = dispatchAgainstOrder.getCreated_date();
		this.updated_date = dispatchAgainstOrder.getUpdated_date();
		ObjectMapper mapper = new ObjectMapper();
		 try {
	            this.dispatchAgainstOrderValue = mapper.readValue(dispatchAgainstOrder.getDispatchAgainstOrderValue(), HashMap.class);
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	}
}
