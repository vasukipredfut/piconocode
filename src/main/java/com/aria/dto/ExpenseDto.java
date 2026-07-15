package com.aria.dto;

import java.time.LocalDateTime;

import org.apache.commons.text.StringEscapeUtils;

import com.aria.model.Expense;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExpenseDto {

	private Long id;

	
	private String rname;

	
	private String spec;

	
	private String rtype;
	
	
	private String itype;
	
	
	private String unit;

	
	private Integer lot;
	
	
	private Double cost;
	
	
	private Integer parent_id;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private String primaryExpensesName;
	
	private Long primaryExpId;
	public ExpenseDto(Expense field) {
		this.id = field.getId();
		this.rname = StringEscapeUtils.unescapeHtml4(field.getRname());
		this.spec = field.getSpec();
		this.rtype = field.getRtype();
		this.itype = field.getItype();
		this.lot = field.getLot();
		this.unit = field.getUnit();
		this.cost = field.getCost();
		this.createdBy = field.getCreatedBy();
		this.updatedBy = field.getUpdatedBy();
		this.created_date = field.getCreated_date();
		this.updated_date = field.getUpdated_date();
		this.parent_id=field.getParent_id();

	}

}
