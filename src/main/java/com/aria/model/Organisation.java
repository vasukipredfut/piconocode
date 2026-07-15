package com.aria.model;

import com.aria.dto.OrganizationDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "organisation")
public class Organisation {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String search;

    @Column
    private String address;

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String phone;

    @Column
    private String fax;

    @Column
    private String email;

    @Column
    private String vattin;

    @Column
    private String regn_1;

    @Column
    private String regn_2;

    @Column
    private String purpose;

    @Column
    private String workinst;

    @Column
    private String safetyinst;

    @Column
    private String trnprefix;

    @Column
    private String lfname;

    @Column
    private String activeflag;

    @Column
    private String GST;

    @Column
    private String stock;


    public Organisation(OrganizationDto organisation) {
        this.id = organisation.getIdOrg();
        this.name = organisation.getName();
    }
}
