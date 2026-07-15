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


import com.aria.dto.MachineWFMaintenanceReasonDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "machine_wf_maintenance_reason")
public class MachineWFMaintenanceReason {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "machine_work_flow_id")
	private Long machineWorkFlowId;

	@OneToOne
	@JoinColumn(name = "assign_attribute_id", referencedColumnName = "id")
	private AttributesAssign assignAttributes;

	@OneToOne
	@JoinColumn(name = "consumption_id", referencedColumnName = "id")
	private ProductMaster products;

	@Column(name = "value_type")
	private String valueType;

	@Column
	private String zone;

	@Column
	private Long quantity;

	@Column(name = "bin_name")
	private String binName;

	@Column(name = "batch_no")
	private String batchNo;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public MachineWFMaintenanceReason(MachineWFMaintenanceReasonDto field) {
		this.id = field.getId();
		if (field.getAssignAttributesId() != null) {
			/*AttributesAssign temp = new AttributesAssign();
			temp.setId(field.getAssignAttributesId());
			this.assignAttributes = temp;*/
		}
		if (field.getProductsId() != null) {
			ProductMaster temp = new ProductMaster();
			temp.setId(field.getProductsId());
			this.products = temp;
		}
		this.binName = field.getBinName();
		this.batchNo = field.getBatchNo();
		this.quantity = field.getQuantity();
		this.valueType = field.getValueType();
		this.zone = field.getZone();
		this.createdBy = field.getCreatedBy();
		this.created_date = field.getCreated_date();
		this.updatedBy = field.getUpdatedBy();
		this.updated_date = field.getUpdated_date();
	}
}
