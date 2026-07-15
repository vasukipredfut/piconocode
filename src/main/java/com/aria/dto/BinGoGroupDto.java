package com.aria.dto;

import java.util.ArrayList;
import java.util.List;

import com.aria.model.BinGoGroupList;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinGoGroupDto {

	private Long branchId;

	private String binType;

	private Long deptId;

	private String binCode;

	private Long cluster;

	private Long godownId;

	private String godownName;

	private String deptName;

	private String branchName;

	private String orgName;

	private Boolean locked;

	private String locktype;

	private List<BinProduct> productList = new ArrayList<>();

	public BinGoGroupDto(BinGoGroupList binDetails) {

		this.branchId = binDetails.getBranchId();
		this.binType = binDetails.getBinType();
		this.deptId = binDetails.getDeptId();
		this.godownId = binDetails.getGodownId();
		this.godownName = binDetails.getGodownName();
		this.deptName = binDetails.getDeptName();
		this.branchName = binDetails.getBranchName();
		this.cluster = binDetails.getCluster();
		this.binCode = binDetails.getBinCode();
		this.orgName = binDetails.getOrgName();
		this.locked = binDetails.getlocked();
		this.locktype = binDetails.getlocktype();
	}
}