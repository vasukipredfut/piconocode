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

import com.aria.dto.MachineWFBreakdownReasonDetailsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "machine_wf_breakdown_reason_details")
public class MachineWFBreakdownReasonDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "machine_work_flow_id")
	private Long machineWorkFlowId;

	@Column(name = "breakdown_reason_id")
	private Long breakdownReasonId;

	@OneToOne
	@JoinColumn(name = "consumption_id", referencedColumnName = "id")
	private ProductMaster products;

	@Column
	private Long quantity;

	@Column(name = "bin_name")
	private String binName;

	@Column(name = "batch_no")
	private String batchNo;
	
	@Column
	private String zone;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public MachineWFBreakdownReasonDetails(MachineWFBreakdownReasonDetailsDto field) {
		this.id = field.getId();
		
		if (field.getProductsId() != null) {
			ProductMaster temp = new ProductMaster();
			temp.setId(field.getProductsId());
			this.products = temp;
		}
		this.binName = field.getBinName();
		this.batchNo = field.getBatchNo();
		this.quantity = field.getQuantity();
		this.zone = field.getZone();
		
		this.createdBy = field.getCreatedBy();
		this.created_date = field.getCreated_date();
		this.updatedBy = field.getUpdatedBy();
		this.updated_date = field.getUpdated_date();
	}
}
