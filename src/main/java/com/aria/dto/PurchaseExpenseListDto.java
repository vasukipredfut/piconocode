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
import com.aria.model.PurchaseExpenseList;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseExpenseListDto {

	
	private Long id;

	
	private String expenseName;

	
	private Long machineId;

	
	private String machineName;
	
	

	

	public PurchaseExpenseListDto(PurchaseExpenseList field) {
	
		this.id=field.getId();
		this.expenseName=field.getExpenseName();
		this.machineId=field.getMachineId();
		this.machineName=field.getMachineName();
	
	}
}
