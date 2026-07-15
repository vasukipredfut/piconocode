package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.Proreportdb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProreprtdbDto {

	private Long id;

	private Long kpi;

	private Long refid;

	private Long tgid;

	private Long tcat;

	private Long taskid;

	private Long atid;

	private String atname;

	private String attype;

	private String colid;

	private Long lastupid;

	private String stk;

	private String cas;

	private String cascolid;

	private String rstatus;

	private String scre;

	private String cv;

	private String iv;

	private float cvamt;

	private float ivamt;

	private String cvtype;

	private String ivtype;

	private String stkprn;

	private String attribute_min_value;

	private String attribute_max_value;

	private String attribute_range_value;

	private String input_type;

	private String formula;

	private String sambanthi_formula;

	private String units;

	private Long dispOrder;

	private String bgid;
	
	private List<AttributeFieldsProcessTypeOptionBasedDto> options;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private String purpose_benefit;
	private String workinst_saftmeasure;

	public ProreprtdbDto(Proreportdb model) {
		this.id = model.getId();
		this.kpi = model.getKpi();
		this.refid = model.getRefid();
		this.tgid = model.getTgid();
		//this.tcat = model.getTcat();
		//this.taskid = model.getTaskid();
		this.atid = model.getAtid();
		this.atname = model.getAtname();
		this.attype = model.getAttype();
		this.colid = model.getColid();
		this.lastupid = model.getLastupid();
		this.stk = model.getStk();
		this.cas = model.getCas();
		this.cascolid = model.getCascolid();
		this.rstatus = model.getRstatus();
		this.scre = model.getScre();
		this.cv = model.getCv();
		this.iv = model.getIv();
		this.cvamt = model.getCvamt();
		this.ivamt = model.getIvamt();
		this.cvtype = model.getCvtype();
		this.ivtype = model.getIvtype();
		this.stkprn = model.getStkprn();
		this.attribute_min_value = model.getAttribute_min_value();
		this.attribute_max_value = model.getAttribute_max_value();
		this.attribute_range_value = model.getAttribute_range_value();
		this.input_type = model.getInput_type();
		this.formula = model.getFormula();
		this.sambanthi_formula = model.getSambanthi_formula();
		this.units = model.getUnits();
		this.bgid = model.getBgid();
		 this.options = model.getOptions() != null ? model.getOptions().stream()
					.map(obj -> new AttributeFieldsProcessTypeOptionBasedDto(obj)).collect(Collectors.toList()) : null;

		this.createdBy = model.getCreatedBy();
		this.updatedBy = model.getUpdatedBy();
		this.created_date = model.getCreated_date();
		this.updated_date = model.getUpdated_date();
	}
}
