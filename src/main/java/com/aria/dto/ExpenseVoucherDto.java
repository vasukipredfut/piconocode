package com.aria.dto;

import jakarta.persistence.Column;

import com.aria.model.ExpenseVoucher;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExpenseVoucherDto {
	
private Long id;
	
	
	private Long  vouId;
	

	private Long  expId;
	
	
	private Long  dept;
		
	
	private String  assosiatedWith;
	
	
	private String  filterDataBy;
	
	
	private String  purpose;
	
	
	private String  claim;
	

	private String  status;
	
	private String expName;
	
	
	public ExpenseVoucherDto(ExpenseVoucher expenseVoucher)
	{
		this.id=expenseVoucher.getId();	
		this.vouId=expenseVoucher.getVouId();	
		this.expId=expenseVoucher.getExpId();	
		this.dept=expenseVoucher.getDept();	
		this.assosiatedWith=expenseVoucher.getAssosiatedWith();	
		this.filterDataBy=expenseVoucher.getFilterDataBy();	
		this.purpose=expenseVoucher.getPurpose();	
		this.claim=expenseVoucher.getClaim();	
		this.status=expenseVoucher.getStatus();	
		
		}

}
