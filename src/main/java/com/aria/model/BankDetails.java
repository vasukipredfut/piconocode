package com.aria.model;

import com.aria.dto.BankDetailsDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "bankdetail")
public class BankDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String ifsc;

    @Column
    private String branch;

    @Column
    private String centre;

    @Column
    private String district;

    @Column
    private String state;

    @Column
    private String address;

    @Column
    private String date;

    @Column(name = "lastmodified")
    private LocalDateTime lastmodified;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;


    public BankDetails(BankDetailsDTO bankDetailsDTO) {
        this.id = bankDetailsDTO.getId();
        this.name = bankDetailsDTO.getName();
        this.ifsc = bankDetailsDTO.getIfsc();
        this.branch = bankDetailsDTO.getBranch();
        this.centre = bankDetailsDTO.getCentre();
        this.district = bankDetailsDTO.getDistrict();
        this.state = bankDetailsDTO.getState();
        this.address = bankDetailsDTO.getAddress();
        this.date = bankDetailsDTO.getDate();
        this.lastmodified = bankDetailsDTO.getLastmodified();
        this.created_date = bankDetailsDTO.getCreated_date();
        this.updated_date = bankDetailsDTO.getUpdated_date();
    }
}
