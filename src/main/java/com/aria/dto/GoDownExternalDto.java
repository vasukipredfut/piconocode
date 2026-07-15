package com.aria.dto;

import java.time.LocalDate;
import java.util.List;

import com.aria.model.ExternalGodownBinDetails;
import com.aria.model.GoDownExternal;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoDownExternalDto {

	private Long id;

	private String toName;
	private Long toId;

	private String fromName;
	private Long fromId;

	private LocalDate createdDate;

	private String issuedPerson;

	private String deliveredPerson;

	private String dispatchOn;
	private String dispatchOnDateTime;

	private String from;

	private String to;

	private String trnNo;

	private String oldTrnNo;  // only blending Request

	private String issueReference;

	private Long issueDeptId;

	private Long issueBranchId;

	private Long fromDeptId;

	private Long fromBranchId;

	private int active;

	private Long createdBy;

	private Long updatedBy;

	private String issueMode;

	private String transmode;

	private String vehicleType;

	private String vehicleName;

	private String materialReturn;

	private String ewayBill;

	private String status;

	private Long deliveredBy;

	private String GDCNo;
	private String type;

	private List<RequestGodownItemDto> requestItem;

	private Company firmDetails;
	private Object fromAddress;
	private Object toAddress;

	private Long firmId;
	public List<ExternalGodownBinDetails> externalGodownBinDetails;

	public GoDownExternalDto(GoDownExternal goDownExternal) {
		DateUtil dateUtil = new DateUtil();
		this.id = goDownExternal.getId();
		this.toName = goDownExternal.getToName();
		this.fromName = goDownExternal.getFromName();
		this.createdDate = goDownExternal.getCreatedDate();
		if(goDownExternal.getDispatchOn() != null) {
			this.dispatchOn = dateUtil.getDateTOStringWk(goDownExternal.getDispatchOn());
			this.dispatchOnDateTime = dateUtil.getDateTime(goDownExternal.getDispatchOn());
		}

		if (goDownExternal.getIssueBy() != null) {
			this.issuedPerson = goDownExternal.getIssueBy().getEmployeename();
		}
		if (goDownExternal.getDeliveredBy() != null) {
			this.deliveredPerson = goDownExternal.getDeliveredBy().getEmployeename();
		}
		if (goDownExternal.getDeliveredBy() != null) {
			this.deliveredBy = goDownExternal.getDeliveredBy().getId();
		}
		this.from = goDownExternal.getFrom();
		this.to = goDownExternal.getTo();
		this.trnNo = goDownExternal.getTrnNo();
		this.issueReference = goDownExternal.getIssueReference();
		if (goDownExternal.getIssueDepartment() != null) {
			this.issueDeptId = goDownExternal.getIssueDepartment().getId();
		}
		if (goDownExternal.getIssueBranch() != null) {
			this.issueBranchId = goDownExternal.getIssueBranch().getId();
		}
		if (goDownExternal.getFromDepartment() != null) {
			this.fromDeptId = goDownExternal.getFromDepartment().getId();
		}
		if (goDownExternal.getFromBranch() != null) {
			this.fromBranchId = goDownExternal.getFromBranch().getId();
		}
		this.active = goDownExternal.getActive();
		this.fromId = goDownExternal.getFromId();
		this.toId = goDownExternal.getToId();
		this.createdBy = goDownExternal.getCreatedBy();
		this.updatedBy = goDownExternal.getUpdatedBy();
		this.issueMode = goDownExternal.getIssueMode();
		this.transmode = goDownExternal.getTransmode();
		this.vehicleType = goDownExternal.getVehicleType();
		this.vehicleName = goDownExternal.getVehicleName();

		this.type = goDownExternal.getType();
		this.ewayBill = goDownExternal.getEwayBill();
		this.status = goDownExternal.getStatus();
		this.GDCNo = goDownExternal.getGDCNo();
		this.externalGodownBinDetails = goDownExternal.getExternalGodownBinDetails();
		this.firmId = goDownExternal.getFirmId();
	}

}
