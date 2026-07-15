package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.PanelDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "panel")
public class Panel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "panel_id")
	private Long panelId;

	@Column(name = "panel_name")
	private String panelName;

	@Column
	private String mf;

	@OneToOne
	@JoinColumn(name = "unit_id", referencedColumnName = "id")
	private Units units;

	@OneToOne
	@JoinColumn(name = "machine_id", referencedColumnName = "id")
	private Machine machine;

	@OneToOne
	@JoinColumn(name = "godown_id", referencedColumnName = "id")
	private GoDown godown;

	@OneToOne
	@JoinColumn(name = "eb_service_id", referencedColumnName = "id")
	private EB eb;

	@Column(name = "purpose_benefit")
	private String purposeBenefit;

	@Column(name = "work_Safety_Instructions")
	private String workSafetyInstructions;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;
	
	
	@Column
	private String isono;
	
	@Column
	private String unit;
	
	@Column
	private String pmachine;
	
	@Column
	private String pgodown;
	
	@Column
	private String ebservice;
	
	@Column
	private String cluster;
	
	@Column
	private Long branch;
	
	@Column
	private String benefit;
	
	@Column
	private String pfinal;

	@Column(name = "machine_on_off")
	private Boolean machineOnOff;

	@Column(name = "max_units")
	private double maxUnits;

	public Panel(PanelDto field) {
		if (field.getUnitId() != null) {
			Units units= new Units();
			units.setId(field.getUnitId());
			this.units = units;
		}
		if (field.getMachineId() != null) {
			Machine machine= new Machine();
			machine.setId(field.getMachineId());
			this.machine = machine;
		}
		if (field.getGodownId() != null) {
			GoDown godown= new GoDown();
			godown.setId(field.getGodownId());
			this.godown = godown;
		}
		if (field.getEbServiceId() != null) {
			EB eb= new EB();
			eb.setId(field.getEbServiceId());
			this.eb = eb;
		}
		this.id = field.getId();
		this.panelId = field.getPanelId();
		this.panelName = field.getPanelName();
		this.mf = field.getMf();
		this.purposeBenefit = field.getPurposeBenefit();
		this.workSafetyInstructions = field.getWorkSafetyInstructions();
		this.machineOnOff = field.getMachineOnOff();

		this.createdBy = field.getCreatedBy();
		this.updatedBy = field.getUpdatedBy();
		this.created_date = field.getCreated_date();
		this.updated_date = field.getUpdated_date();
		this.maxUnits = field.getMaxUnits();
	}
}
