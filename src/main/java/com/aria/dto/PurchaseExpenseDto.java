package com.aria.dto;

import java.io.File;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.ExpenseDto;
import com.aria.model.PurchaseExpense;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseExpenseDto {

	
	private Long amc_Id;

	
	private String amc_Expense_Name;

	
	private Integer amc_Flag;

	
	private Integer amc_Category_Id;
	
	
	private Long amc_Machine_Ref_Id;

	private String amc_Machine_Ref_Name;
	
	private LocalDateTime created_Timestamp;
	
	
	private LocalDateTime updated_Timestamp;

	
	private Integer department;
	
	
	private Integer company;
	
	
	private Integer branch;
	
	
	private Integer amc_Expense_lot;
	
	
	private Integer amc_Expense_unit;
	
	
	private Integer amc_Expense_Variable_type;
	
	
	private String hsn_Ref_Id;
	
	
	private Double amc_Expense_cost;

	private Long createdBy;

	private String createdByName;

	
	private Long updatedBy;

	

	public PurchaseExpenseDto(PurchaseExpense field) {
		
		this.amc_Id=field.getAmc_Id();
		this.amc_Category_Id=field.getAmc_Category_Id();
		this.amc_Expense_cost=field.getAmc_Expense_cost();
		this.amc_Expense_lot=field.getAmc_Expense_lot();
		this.amc_Expense_Name=field.getAmc_Expense_Name();
		this.amc_Expense_Variable_type=field.getAmc_Expense_Variable_type();
		this.amc_Expense_unit=field.getAmc_Expense_unit();
		this.amc_Flag=field.getAmc_Flag();
		this.amc_Machine_Ref_Id=field.getAmc_Machine_Ref_Id();
		this.branch=field.getBranch();
		this.company=field.getCompany();
		this.department=field.getDepartment();
		this.created_Timestamp=field.getCreated_Timestamp();
		this.updated_Timestamp=field.getUpdated_Timestamp();
		this.createdBy=field.getCreatedBy();
		this.updatedBy=field.getUpdatedBy();
		this.hsn_Ref_Id=field.getHsn_Ref_Id();

	}
}
