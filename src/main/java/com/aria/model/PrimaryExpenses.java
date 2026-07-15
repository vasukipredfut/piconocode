package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.PrimaryExpensesDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//created by dhivya ,primaryexpenses for expenses, discussed by subhash sir and alaguraja sir
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "primary_expenses")
public class PrimaryExpenses {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String name;
    
    public PrimaryExpenses(PrimaryExpensesDto primaryExpensesDto)
    {
    	this.id=primaryExpensesDto.getId();
		this.name=primaryExpensesDto.getName();
    }

}
