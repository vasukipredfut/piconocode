package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;
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

import com.aria.dto.MachineWFBreakdownReasonDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "machine_wf_breakdown_reason")
public class MachineWFBreakdownReason {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "machine_work_flow_id")
	private Long machineWorkFlowId;
	
	@Column
	private String problem;
	
	@Column
	private String action;
	
	@Column(name = "completed_date")
	private Date completedDate;

	@Column(name = "completed_time")
	private String completedTime;

	@OneToMany(mappedBy = "breakdownReasonId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<MachineWFBreakdownReasonDetails> breakdownDetails;


	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public MachineWFBreakdownReason(MachineWFBreakdownReasonDto field) {
		this.id = field.getId();
		
//		if (field.getProductsId() != null) {
//			Products temp = new Products();
//			temp.setId(field.getProductsId());
//			this.products = temp;
//		}
		
		this.problem = field.getProblem();
		this.action = field.getAction();
//		this.completedTime = field.getCompletedTime();
		this.createdBy = field.getCreatedBy();
		this.created_date = field.getCreated_date();
		this.updatedBy = field.getUpdatedBy();
		this.updated_date = field.getUpdated_date();
	}
}
