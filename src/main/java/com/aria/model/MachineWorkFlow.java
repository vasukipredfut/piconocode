package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

import com.aria.dto.MachinePanelReadingMappingDto;
import com.aria.dto.MachineWorkFlowDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "machine_workflow")
public class MachineWorkFlow {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "log_id")
	private int logId;

	@OneToOne
	@JoinColumn(name = "machine_id", referencedColumnName = "id")
	private Machine machine;

	@Column(name = "power_source")
	private String powerSource;

	@Column
	private String reason;

	@Column(name = "time_slab")
	private String timeSlab;

	@Column(name = "production_shift")
	private String productionShift;

	@Column(name = "c_status")
	private String cStatus;

	@Column(name = "power_on_date")
	private Date powerOnDate;

	@Column(name = "power_off_date")
	private Date powerOffDate;

	@Column(name = "breakdown_date")
	private Date breakdownDate;

	@Column(name = "breakdown_time")
	private String breakDownTime;

	@Column(name = "power_on_time")
	private String powerOnTime;

	@Column(name = "power_off_time")
	private String powerOffTime;

	@Column(name = "meter_closing_reading")
	private String meterClosingReading;

	@Column(name = "power_off_reason")
	private String powerOffReason;

	@Column(name = "reason_id")
	private Long powerOffReasonId;

	@Column(name = "detailed_reason")
	private String detailedReason;

	@Column(name = "detailed_reason_id")
	private Long detailedReasonId;

	@Column(name = "operated_by")
	private Long operatedBy;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;


	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@OneToMany(mappedBy = "workFlowId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	List<MachinePanelReadingMapping> panelsReadingMapping;

}
