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

import com.aria.dto.EBDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "eb_master")
public class EB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "service_name")
	private String serviceName;

	@Column(name = "service_category")
	private String serviceCategory;

	@Column(name = "service_no")
	private String serviceNo;
	
	@Column
	private String capacity;
	
	@Column
	private String status;
	
	@Column
	private Long cluster;
	
//	@Column
//	private Long branch;
	
	@Column
	private Long company;
	
	@Column
	private String address;
	
	@Column
	private Long expense;

	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch branch;

	@OneToOne
	@JoinColumn(name = "firm_id", referencedColumnName = "id")
	private Organisation firm;

//	@Column(updatable = false)
//	private Long createdBy;
//
//	@Column
//	private Long updatedBy;
	
	@OneToOne
//	@Column(updatable = false)
	@JoinColumn(name = "created_by", updatable = false, referencedColumnName = "id")
	private User createdBy;
	
	@OneToOne
	@JoinColumn(name = "updated_by", referencedColumnName = "id")
	private User updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public EB(EBDto eb) {
		if(eb.getBranchId() !=null){
			Branch branch = new Branch();
			branch.setId(Long.valueOf(eb.getBranchId()));
			this.branch = branch;
		}

		if(eb.getFirmId() !=null){
			Organisation firm = new Organisation();
			firm.setId(Long.valueOf(eb.getFirmId()));
			this.firm = firm;
		}
		this.id = eb.getId();

		this.serviceName = eb.getServiceName();

		this.serviceCategory = eb.getServiceCategory();
		this.serviceNo = eb.getServiceNo();
		this.status = eb.getStatus();
		if (eb.getCreatedBy() != null) {
			User ref= new User();
			ref.setId(eb.getCreatedBy());
			this.createdBy = ref;
		}
		if (eb.getUpdatedBy() != null) {
			User ref= new User();
			ref.setId(eb.getUpdatedBy());
			this.updatedBy = ref;
		}
//		this.createdBy = eb.getCreatedBy();
//		this.updatedBy = eb.getUpdatedBy();
		this.created_date = eb.getCreated_date();
		this.updated_date = eb.getUpdated_date();
	}
}
