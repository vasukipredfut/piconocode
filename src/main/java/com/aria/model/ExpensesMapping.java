package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.ExpensesMappingDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="expgroup")
public class ExpensesMapping {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column
	private Long expId;
	@Column
	private Long secExpId;
	@Column
	private String secExpName;
	
	public ExpensesMapping(ExpensesMappingDto expensesMapping) {
		this.id=expensesMapping.getId();
		this.expId=expensesMapping.getExpId();
		this.secExpId=expensesMapping.getSecExpId();
	}
	

}
