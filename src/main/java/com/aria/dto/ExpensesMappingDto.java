package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.ExpensesMapping;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExpensesMappingDto {
	private Long id;
	private  Long expId;
	private Long secExpId;
	
	public ExpensesMappingDto(ExpensesMapingList expensesMapping) {
		this.id=expensesMapping.getId();
		this.expId=expensesMapping.getExpId();
		this.secExpId=expensesMapping.getSecExpId();
	}
	
	public ExpensesMappingDto(ExpensesMapping expensesMapping) {
		this.id=expensesMapping.getId();
		this.expId=expensesMapping.getExpId();
		this.secExpId=expensesMapping.getSecExpId();
	}
	
}
