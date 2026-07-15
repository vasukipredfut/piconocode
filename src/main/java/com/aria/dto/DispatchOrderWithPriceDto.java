package com.aria.dto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.DispatchOrderWithPrice;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class DispatchOrderWithPriceDto {
	

	private Long id;

	
	private Long orderNumber;

	
	private String orderDate;

	private Long branch;

    private String branchName;
	
	

	private Long enteredBy;
	
	private String enteredByName;

	private String vendorName;

	
	private String brokerName;
	
	
	private Long cess;
	
	
	
	private Long createdBy;

	
	private Long updatedBy;

	
	private LocalDateTime created_date;


	private LocalDateTime updated_date;

	
	private Map<String,Object> dispatchOrderWithPriceValue;

	public DispatchOrderWithPriceDto(DispatchOrderWithPrice dispatchOrderWithPrice) {
		this.id = dispatchOrderWithPrice.getId();
		this.orderNumber = dispatchOrderWithPrice.getOrderNumber();
		if (dispatchOrderWithPrice.getBranch() != null) {
			 this.branch = dispatchOrderWithPrice.getBranch().getId();
	            this.branchName = dispatchOrderWithPrice.getBranch().getBranchName();

		}

		if (dispatchOrderWithPrice.getEnteredBy() != null) {
			this.enteredBy = dispatchOrderWithPrice.getEnteredBy().getId();
            this.enteredByName = dispatchOrderWithPrice.getEnteredBy().getEmployeename();

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
			 this.dispatchOrderWithPriceValue = mapper.readValue(dispatchOrderWithPrice.getDispatchOrderWithPriceValue(), HashMap.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}