package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PartyDetails {


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


    public PartyDetails(PartyList partyList) {
        this.id = partyList.getId();
        this.name = partyList.getName();
        this.address = partyList.getAddress();
        this.city = partyList.getCity();
        this.state = partyList.getState();
        this.pinCode = partyList.getPinCode();
        this.GST = partyList.getGST();
        this.email = partyList.getEmail();
        this.phoneNo = partyList.getPhoneNo();
        this.mobileNo = partyList.getMobileNo();
    }
}
