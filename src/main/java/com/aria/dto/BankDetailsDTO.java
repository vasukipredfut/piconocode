package com.aria.dto;

import com.aria.model.BankDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class BankDetailsDTO {

    private Long id;

    private String name;

    private String ifsc;
    
    private String branch;
    
    private String centre;

    private String district;
    
    private String state;

    private String address;

    private String date;

    private LocalDateTime lastmodified;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    public BankDetailsDTO(BankDetails bankDetailsDto) {
        this.id = bankDetailsDto.getId();
        this.name = bankDetailsDto.getName();
        this.ifsc = bankDetailsDto.getIfsc();
        this.branch = bankDetailsDto.getBranch();
        this.centre = bankDetailsDto.getCentre();
        this.district = bankDetailsDto.getDistrict();
        this.state = bankDetailsDto.getState();
        this.address = bankDetailsDto.getAddress();
        this.date = bankDetailsDto.getDate();
        this.lastmodified = bankDetailsDto.getLastmodified();
        this.created_date = bankDetailsDto.getCreated_date();
        this.updated_date = bankDetailsDto.getUpdated_date();
    }
}
