package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.ExpenseVoucherDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "expvoucher")
public class ExpenseVoucher {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private Long  vouId;
	
	@Column
	private Long  expId;
	
	@Column
	private Long  dept;
		
	@Column(name="contype")
	private String  assosiatedWith;
	
	@Column(name="category")
	private String  filterDataBy;
	
	@Column(name="description")
	private String  purpose;
	
	@Column(name="workinst")
	private String  claim;
	
	@Column
	private String  status;
	
	public ExpenseVoucher(ExpenseVoucherDto expenseVoucher)
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
