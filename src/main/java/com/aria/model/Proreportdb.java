package com.aria.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.aria.dto.ProreprtdbDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "proreportdb")
public class Proreportdb {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long kpi;

	@Column
	private Long refid;

	@Column
	private Long tgid;

	@Column
	private Long atid;

	@Column
	private String atname;

	@Column
	private String attype;

	@Column
	private String colid;

	@Column
	private Long lastupid;

	@Column
	private String stk;

	@Column
	private String cas;

	@Column
	private String cascolid;

	@Column
	private String rstatus;

	@Column
	private String scre;

	@Column
	private String cv;

	@Column
	private String iv;

	@Column
	private float cvamt;

	@Column
	private float ivamt;

	@Column
	private String cvtype;

	@Column
	private String ivtype;

	@Column
	private String stkprn;

	@Column
	private String attribute_min_value;

	@Column
	private String attribute_max_value;

	@Column
	private String attribute_range_value;

	@Column
	private String input_type;

	@Column
	private String formula;

	@Column
	private String sambanthi_formula;

	@Column
	private String units;

	@Column
	private String bgid;
	
	@OneToMany(mappedBy = "attributes_id", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AttributeFieldsProcessTypeOptionBased> options;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;



	public Proreportdb(ProreprtdbDto model){
		this.id = model.getId();
		this.kpi = model.getKpi();
		this.refid = model.getRefid();
		this.tgid = model.getTgid();
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

	}
}
