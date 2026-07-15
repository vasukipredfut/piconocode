package com.aria.dto;

public class BranchAddressDto {

	private Long branchId;

	private String branchName;

	private String address;

	private String city;

	public Long getBranchId() {
		return branchId;
	}

	public String getBranchName() {
		return branchName;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public BranchAddressDto(bAddressList branchAddress) {
		this.branchId = branchAddress.getBranchId();
		this.branchName = branchAddress.getBranchName();
		this.address = branchAddress.getAddress();
		this.city = branchAddress.getCity();
	}

}
