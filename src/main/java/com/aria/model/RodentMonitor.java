package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.RodentMonitorDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "rodent_monitor")
public class RodentMonitor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Date rodentDate;

	@Column
	private String boxNo;

	@Column
	private String reason;

	@OneToOne
	@JoinColumn(name = "godown_id", referencedColumnName = "id")
	private GoDown godown;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;
	
	

	public RodentMonitor(RodentMonitorDto field) {
		
		if (field.getGodownId() != null) {
			GoDown godown= new GoDown();
			godown.setId(field.getGodownId());
			this.godown = godown;
		}
		
		this.id = field.getId();
		this.boxNo = field.getBoxNo();
		this.reason = field.getReason();

		this.createdBy = field.getCreatedBy();
		this.updatedBy = field.getUpdatedBy();
		this.created_date = field.getCreated_date();
		this.updated_date = field.getUpdated_date();
	}
}
