package com.aria.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "intra_branch_rec")
public class IntraBranchReceiptEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "docket_No")
	private String docketNo;

	@Column(name = "hup_Id")
	private String hupId;

	@OneToOne
	@JoinColumn(name = "issue_branch_id", referencedColumnName = "id")
	private Branch issueBranch;


	@Column(name = "request_branch_id")
	private Long requestBranchId;

	@Column(name = "dispatch_on")
	@Temporal(TemporalType.DATE)
	private Date dispatchOn;

	@Column(name = "dispatch_by")
	private Long dispatchBy;

	@Column
	private String status;

	@Column(name = "trn_no")
	private String trnNo;

	@Column(name = "created_By", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;


	@Column(name = "delivery_user_id")
	private Long deliveryByStaff;

	@Column(name = "org_id", updatable = false)
	private Long requestFirmId;

	@Column(name = "request_dept_id", updatable = false)
	private Long requestDeptId;

}
