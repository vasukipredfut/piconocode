package com.aria.dto;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WithInBranchReceiveListDto {

	private Long id;
	private Long requestId;
	private String trnNo;
	private String itemNames;
	private String requestFromBranch;
	private String requestFromDepartment;
	private String status;
	private String requestDate;
	private String issueFromBranch;
	private String issueFromDepartment;

	public WithInBranchReceiveListDto(WithInBranchReceiveList materialRequest) {
		DateUtil dateUtil = new DateUtil();
		this.id = materialRequest.getId();
		this.trnNo = materialRequest.getTrnNo();
		this.requestId = materialRequest.getRequestId();		
		this.itemNames = materialRequest.getItemNames() != null ? Arrays.stream(materialRequest.getItemNames().split(",")).distinct().collect(Collectors.joining(",")) : null;
		this.requestFromBranch = materialRequest.getRequestFromBranch();
		this.requestFromDepartment = materialRequest.getRequestFromDepartment();
		this.status = materialRequest.getStatus();
		if (materialRequest.getRequestDate() != null) {
			this.requestDate = dateUtil.getDateTOStringWk(materialRequest.getRequestDate());
		}
		this.issueFromBranch = materialRequest.getIssueFromBranch();
		this.issueFromDepartment = materialRequest.getIssueFromDepartment();
	}
}
