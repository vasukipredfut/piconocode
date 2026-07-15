package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BranchAddress {

    private Long branchId;

    private String branchName;

    private String address;

    private String city;
    private String pinCode;
    private String state;
    private String email;
    private String fax;
    private String phoneNo;


    public BranchAddress(BranchAddressList branchAddress) {
        this.branchId = branchAddress.getBranchId();
        this.branchName = branchAddress.getBranchName();
        this.address = branchAddress.getAddress();
        this.city = branchAddress.getCity();
        this.state = branchAddress.getState();
        this.pinCode = branchAddress.getPinCode();
        this.email = branchAddress.getEmail();
        this.fax = branchAddress.getFax();
        this.phoneNo = branchAddress.getPhoneNo();
    }
}
