package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.apache.commons.text.StringEscapeUtils;

import com.aria.dto.ExpenseDto;
import com.aria.dto.ExpensesMappingResponse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "expenses")
public class Expense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String rname;

	@Column
	private String spec;

	@Column
	private String rtype;
	
	@Column
	private String itype;
	
	@Column
	private String unit;

	@Column
	private Integer lot;
	
	@Column
	private Double cost;
	
	@Column
	private Integer parent_id;

	@Column(name = "created_By",updatable = false)
	private Long createdBy;

	@Column(name = "updated_By")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public Expense(ExpenseDto field) {


		this.id = field.getId();
		this.rname = StringEscapeUtils.unescapeHtml4(field.getRname());
		this.spec = StringEscapeUtils.unescapeHtml4(field.getSpec());
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
	
	public Expense(ExpensesMappingResponse field) {
		this.id = field.getId();
		this.rname = StringEscapeUtils.unescapeHtml4(field.getRname());
		this.spec = StringEscapeUtils.unescapeHtml4(field.getSpec());
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
