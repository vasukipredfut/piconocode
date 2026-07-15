package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Company {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String GST;
    private String email;
    private String phoneNo;
    private String mobileNo;
    private String fax;


    public Company(CompanyList company) {
        this.id = company.getId();
        this.name = company.getName();
        this.address = company.getAddress();
        this.city = company.getCity();
        this.state = company.getState();
        this.pinCode = company.getPinCode();
        this.GST = company.getGST();
        this.email = company.getEmail();
        this.phoneNo = company.getPhoneNo();
        this.mobileNo = company.getMobileNo();
        this.fax = company.getFax();
    }
}
