package com.aria.model;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "inward_check")
public class InwardCheck {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "batch_no")
	private String batchNo;

	@Column(name = "recheck_log")
	private String recheckLog;

	@Column
	private String recheckdesc;

	@Column(name = "inward_date")
	private Date inwardDate;

	@Column(name = "product_id")
	private Long productId;

	@Column
	private String status;

	@Column
	private String completedstep;

	@Column
	private String bag;

	@Column
	private String migrated;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;
	



	public InwardCheck(InwardCheck inwardCheck) {
		this.id = inwardCheck.getId();
		this.status = inwardCheck.getStatus();
		this.completedstep = inwardCheck.getCompletedstep();
		this.batchNo = inwardCheck.getBatchNo();
		this.recheckLog = inwardCheck.getBatchNo();
		this.recheckdesc = inwardCheck.getBatchNo();
		this.bag = inwardCheck.getBatchNo();
		this.createdBy = inwardCheck.getCreatedBy();
		this.updatedBy = inwardCheck.getUpdatedBy();
		this.created_date = inwardCheck.getCreated_date();
		this.updated_date = inwardCheck.getUpdated_date();
		this.productId=inwardCheck.getProductId();
	}

}