package com.aria.dto;

import com.aria.model.PrimaryExpenses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//created by dhivya ,primaryexpenses for expenses, discussed by subhash sir and alaguraja sir
@Getter
@Setter
@NoArgsConstructor
public class PrimaryExpensesDto {
	
	private Long id;
	private String name;
	
	public PrimaryExpensesDto(PrimaryExpenses primaryExpenses)
	{
		this.id=primaryExpenses.getId();
		this.name=primaryExpenses.getName();
	}

}
