package com.aria.dto;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoDownDetailsForBankDto {
	
	private Long id;
	private String gName;
	private Double lot;
	private Long branch;
	private Double opening;
	private Double receipts;
	private Double internalPlantReceipt;
	private Double total;
	private Double delivery;
	private Double internalPlantIssue;	
	private Double Closing;
	

}
