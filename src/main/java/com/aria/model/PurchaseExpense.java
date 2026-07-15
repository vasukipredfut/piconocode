package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.ExpenseDto;
import com.aria.dto.PurchaseExpenseDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "purchase_amc_expense")
public class PurchaseExpense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long amc_Id;

	@Column
	private String amc_Expense_Name;

	@Column
	private Integer amc_Flag;

	@Column
	private Integer amc_Category_Id;
	
	@Column
	private Long amc_Machine_Ref_Id;
	
	@Column
	private LocalDateTime created_Timestamp;
	
	@Column
	private LocalDateTime updated_Timestamp;

	@Column
	private Integer department;
	
	@Column
	private Integer company;
	
	@Column
	private Integer branch;
	
	@Column
	private Integer amc_Expense_lot;
	
	@Column
	private Integer amc_Expense_unit;
	
	@Column
	private Integer amc_Expense_Variable_type;
	
	@Column
	private String hsn_Ref_Id;
	
	@Column
	private Double amc_Expense_cost;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;



	public PurchaseExpense(PurchaseExpenseDto field) {

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
