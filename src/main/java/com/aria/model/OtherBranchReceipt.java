
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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "other_branch_receipt")
public class OtherBranchReceipt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "request_id")
	private Long requestId;

	@Column(name = "issue_id")
	private Long issueId;

	@Column(name = "hup_id")
	private String hupId;

	@Column(name = "docket_no")
	private String docketNo;

	@OneToOne
	@JoinColumn(name = "request_dept_id", referencedColumnName = "id")
	private DepartmentDetails requestDepartment;

	@OneToOne
	@JoinColumn(name = "request_branch_id", referencedColumnName = "id")
	private Branch requestBranch;

	@OneToOne
	@JoinColumn(name = "request_org_id", referencedColumnName = "id", updatable = false)
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
	@JoinColumn(name = "issue_org_id", referencedColumnName = "id", updatable = false)
	private Organisation issueOrg;

	@Column(name = "request_date")
	private Date requestDate;

	@Column(name = "issue_date")
	private Date issueDate;

	@Column(name = "entry_date")
	private Date entryDate;

	@Column(name = "received_date")
	private Date receivedDate;

	@OneToOne
	@JoinColumn(name = "received_user_id", referencedColumnName = "id")
	private User receivedBy;

	@Column
	private String status;

	@Column(name = "trn_No")
	private String trnNo;

	@Column
	private int active;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;


	@Column(name = "other_Branch_Receipt_Value")
	private String otherBranchReceiptValue;
}
