package com.aria.model;

import java.time.LocalDate;
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

import com.aria.dto.GoDownExternalDto;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "goDownExternal")
public class GoDownExternal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "to_name")
	private String toName;

	@Column(name = "to_id")
	private Long toId;

	@Column
	private int active;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "created_date", updatable = false)
	private LocalDate createdDate;

	@Column(name = "updated_by")
	private Long updatedBy;

	@OneToOne
	@JoinColumn(name = "issue_by", referencedColumnName = "id")
	private User issueBy;

	@OneToOne
	@JoinColumn(name = "issue_dept_id", referencedColumnName = "id")
	private DepartmentDetails issueDepartment;

	@OneToOne
	@JoinColumn(name = "issue_branch_id", referencedColumnName = "id")
	private Branch issueBranch;

	@Column(name = "dispatch_on")
	private Date dispatchOn;

	@Column(name = "from_val")
	private String from;

	@Column(name = "from_name")
	private String fromName;

	@Column(name = "from_id")
	private Long fromId;

	@OneToOne
	@JoinColumn(name = "from_dept_id", referencedColumnName = "id", updatable = false)
	private DepartmentDetails fromDepartment;

	@OneToOne
	@JoinColumn(name = "from_branch_id", referencedColumnName = "id", updatable = false)
	private Branch fromBranch;

	@Column(name = "to_val")
	private String to;

	@Column(name = "trn_no")
	private String trnNo;

	@Column(name = "issue_reference")
	private String issueReference;

	@Column(name = "issue_mode")
	private String issueMode;

	@Column(name = "transport_mode")
	private String transmode;

	@Column(name = "vehicle_type")
	private String vehicleType;

	@Column(name = "vehicle_name")
	private String vehicleName;


	@Column(name = "eway_bill")
	private String ewayBill;

	@Column(name = "status")
	private String status;

	@OneToOne
	@JoinColumn(name = "delivered_By", referencedColumnName = "id")
	private User deliveredBy;

	@Column(name = "gdc_no")
	private String GDCNo;

	@OneToMany(mappedBy = "goDownExternalId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<RequestGodownItem> requestItem;


	@Column(updatable = false)
	private String type;


	@OneToMany(mappedBy = "goDownExternalId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	public List<ExternalGodownBinDetails> externalGodownBinDetails;


	@Column(name = "firm_Id", updatable = false)
	private Long firmId;


	public GoDownExternal(GoDownExternalDto goDownExternalDto) {
		DateUtil dateUtil = new DateUtil();
		this.id = goDownExternalDto.getId();
		this.toName = goDownExternalDto.getToName();
		this.fromName = goDownExternalDto.getFromName();
		this.active = goDownExternalDto.getActive();
		this.createdBy = goDownExternalDto.getCreatedBy();
		this.createdDate = goDownExternalDto.getCreatedDate();
		this.updatedBy = goDownExternalDto.getUpdatedBy();
		this.dispatchOn = dateUtil.getStringTODateWk(goDownExternalDto.getDispatchOn());
		this.from = goDownExternalDto.getFrom();
		this.to = goDownExternalDto.getTo();
		this.trnNo = goDownExternalDto.getTrnNo();
		this.issueReference = goDownExternalDto.getIssueReference();
		this.issueMode = goDownExternalDto.getIssueMode();
		this.transmode = goDownExternalDto.getTransmode();
		this.vehicleType = goDownExternalDto.getVehicleType();
		this.vehicleName = goDownExternalDto.getVehicleName();
		this.type = goDownExternalDto.getType();

		this.ewayBill = goDownExternalDto.getEwayBill();
		this.status = goDownExternalDto.getStatus();
		this.GDCNo = goDownExternalDto.getGDCNo();
		this.firmId = goDownExternalDto.getFirmId();

		if (goDownExternalDto.getIssueDeptId() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(goDownExternalDto.getIssueDeptId());
			this.issueDepartment = departmentDetails;
		}

		if (goDownExternalDto.getIssueBranchId() != null) {
			Branch branch = new Branch();
			branch.setId(goDownExternalDto.getIssueBranchId());
			this.issueBranch = branch;
		}
		if (goDownExternalDto.getIssueDeptId() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(goDownExternalDto.getIssueDeptId());
			this.issueDepartment = departmentDetails;
		}

		if (goDownExternalDto.getIssueBranchId() != null) {
			Branch branch = new Branch();
			branch.setId(goDownExternalDto.getIssueBranchId());
			this.issueBranch = branch;
		}
	}

}