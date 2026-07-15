
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.Where;

import com.aria.dto.MaterialIssueDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor

@Table(name = "material_issue")
public class MaterialIssue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "request_id")
	private Long requestId;

	@OneToOne
	@JoinColumn(name = "request_dept_id", referencedColumnName = "id")
	private DepartmentDetails requestDepartment;

	@OneToOne
	@JoinColumn(name = "request_branch_id", referencedColumnName = "id")
	private Branch requestBranch;

	@OneToOne
	@JoinColumn(name = "org_id", referencedColumnName = "id" , updatable = false)
	private Organisation organisation;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User issueBy;

	@OneToOne
	@JoinColumn(name = "issue_dept_id", referencedColumnName = "id")
	private DepartmentDetails issueDepartment;

	@OneToOne
	@JoinColumn(name = "issue_branch_id", referencedColumnName = "id")
	private Branch issueBranch;

	@Column(name = "request_name")
	private String requestName;

	@Column(name = "request_description")
	private String requestDescription;

	@Column(name = "request_date", updatable = false)
	@Temporal(TemporalType.DATE)
	private Date requestDate;

	@Column(name = "issue_date")
	@Temporal(TemporalType.DATE)
	private Date issueDate;

	@Column
	private String status;

	@Column(name = "trn_no")
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

	@Column(name = "sal_status")
	private String salStatus;

	@OneToMany(mappedBy = "issueItemId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Where(clause = "active=0")
	private List<IssueItems> issueItems;


	@OneToMany(mappedBy = "issueItemId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<IssueItems> allIssueItems;


	@Column(name = "accounting_date")
	@Temporal(TemporalType.DATE)
	private Date accountingDate;

	public MaterialIssue(MaterialIssueDto materialIssue) {
		this.id = materialIssue.getId();
		this.requestId = materialIssue.getRequestId();
		this.requestName = materialIssue.getRequestName();
		this.requestDescription = materialIssue.getRequestDescription();
		this.status = materialIssue.getStatus();
		this.trnNo = materialIssue.getTrnNo();
		this.createdBy = materialIssue.getCreatedBy();
		this.updatedBy = materialIssue.getUpdatedBy();
		this.created_date = materialIssue.getCreated_date();
		this.updated_date = materialIssue.getUpdated_date();
		if (materialIssue.getRequestFromBranchId() != null) {
			Branch branch = new Branch();
			branch.setId(materialIssue.getRequestFromBranchId());
			this.requestBranch = branch;
		}

		if (materialIssue.getRequestFromDepartmentId() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(materialIssue.getRequestFromDepartmentId());
			this.requestDepartment = departmentDetails;
		}

		if (materialIssue.getRequestFromFirmId() != null) {
			Organisation organisation = new Organisation();
			organisation.setId(materialIssue.getRequestFromFirmId());
			this.organisation = organisation;
		}

	}

	public MaterialIssue(MaterialIssueDto materialIssue, Boolean status) {

		this.requestId = materialIssue.getRequestId();
		this.requestName = materialIssue.getRequestName();
		this.requestDescription = materialIssue.getRequestDescription();
		this.createdBy = materialIssue.getCreatedBy();
		this.updatedBy = materialIssue.getUpdatedBy();
		this.created_date = materialIssue.getCreated_date();
		this.updated_date = materialIssue.getUpdated_date();
		if (materialIssue.getRequestFromBranchId() != null) {
			Branch branch = new Branch();
			branch.setId(materialIssue.getRequestFromBranchId());
			this.requestBranch = branch;
		}
		if (materialIssue.getRequestFromDepartmentId() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(materialIssue.getRequestFromDepartmentId());
			this.requestDepartment = departmentDetails;
		}
		if (materialIssue.getRequestFromFirmId() != null) {
			Organisation organisation = new Organisation();
			organisation.setId(materialIssue.getRequestFromFirmId());
			this.organisation = organisation;
		}
	}

}