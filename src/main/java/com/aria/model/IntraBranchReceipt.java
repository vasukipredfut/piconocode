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
@Table(name = "intra_branch_receipt")
public class IntraBranchReceipt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "request_id")
	private Long requestId;

	@Column(name = "issue_id")
	private Long issueId;

	@Column(name = "hup_id")
	private String hupId;

	@Column(name = "docket_No")
	private String docketNo;

	@OneToOne
	@JoinColumn(name = "request_dept_id", referencedColumnName = "id")
	private DepartmentDetails requestDepartment;

	@OneToOne
	@JoinColumn(name = "request_branch_id", referencedColumnName = "id")
	private Branch requestBranch;

	@OneToOne
	@JoinColumn(name = "request_org_id", referencedColumnName = "id")
	private Organisation requestOrg;

	@OneToOne
	@JoinColumn(name = "issue_dept_id", referencedColumnName = "id")
	private DepartmentDetails issueDepartment;

	@OneToOne
	@JoinColumn(name = "issue_branch_id", referencedColumnName = "id")
	private Branch issueBranch;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User issueBy;

	@OneToOne
	@JoinColumn(name = "issue_org_id", referencedColumnName = "id")
	private Organisation issueOrg;


	@Column(name = "issue_Date")
	@Temporal(TemporalType.DATE)
	private Date issueDate;

	@Column(name = "entry_Date")
	@Temporal(TemporalType.DATE)
	private Date entryDate;

	@Column(name = "received_Date")
	@Temporal(TemporalType.DATE)
	private Date receivedDate;

	@OneToOne
	@JoinColumn(name = "received_user_id", referencedColumnName = "id")
	private User receivedBy;

	@Column(name = "received_vehicle_no")
	private String receivedVehicleNo;

	@Column
	private String status;

	@Column(name = "trn_No")
	private String trnNo;

	@Column
	private int active;

	@Column(name = "created_By", updatable = false)
	private Long createdBy;

	@Column(name = "updated_By")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@Column(name = "intra_Branch_Receipt_Value")
	private String intraBranchReceiptValue;

	@Column(name = "group_by")
	private String groupBy;
}
