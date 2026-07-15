package com.aria.dto;
import java.time.LocalDateTime;

import com.aria.model.WelfareAmount;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WelfareAmountDto {
	private Long id;

	private String welfareCategory;
	
	private String welfareName;
	
    private String fromDate;

    private String toDate;
    
	private Long productionEntry;
	
	private Long cost;
	
	private Long welfareContribution;
	
	private String foodPercentage;
	
	private String uniformPercentage;
	
    private String transportationPercentage; 

	//private Long payableAmount;

	//private Long employeePayableAmount;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;
	
    public WelfareAmountDto(WelfareAmount welfareAmount) {
		this.id = welfareAmount.getId();
		this.welfareCategory =welfareAmount.getWelfareCategory();
		this.welfareName =welfareAmount.getWelfareName();
		 if(welfareAmount.getProductionEntry() != null) {
	            this.productionEntry = welfareAmount.getProductionEntry().getId();
	            //this.productionEntry = amountBreakdown.getProductionEntry().getCost();
	        }
		 if(welfareAmount.getWelfareContribution() != null) {
	            this.welfareContribution = welfareAmount.getWelfareContribution().getId();
	            this.foodPercentage = welfareAmount.getWelfareContribution().getFoodPercentage();
	            this.uniformPercentage = welfareAmount.getWelfareContribution().getUniformPercentage();
	            this.transportationPercentage = welfareAmount.getWelfareContribution().getTransportationPercentage();
	        }

		//this.payableAmount = welfareAmount.getPayableAmount();
		//this.employeePayableAmount = welfareAmount.getEmployeePayableAmount();
		this.createdBy = welfareAmount.getCreatedBy();
		this.updatedBy = welfareAmount.getUpdatedBy();
		this.created_date = welfareAmount.getCreated_date();
		this.updated_date = welfareAmount.getUpdated_date();

	}
}
