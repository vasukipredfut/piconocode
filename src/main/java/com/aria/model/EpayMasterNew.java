package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "epaymasternew")
public class EpayMasterNew {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  id;


    @Column
    private String connect_table;
    
    @Column
    private Long empcode;

    @Column
    private String name;

    @Column
    private String sex;

    @Column
    @Temporal(TemporalType.DATE)
    private Date jdate;

    @Column
    private String status;

    @Column
    private String nat_work;

    @Column
    private Long dacatid;

    @Column
    private Long did;

    @Column
    private Long bid;

    @Column
    private Long bioid;

    @Column
    private double gwa;

    @Column
    private double ca;

    @Column
    private double hra;

    @Column
    private double basic;

    @Column
    private double da;

    @Column
    private double wa;

    @Column
    private double total;

    @Column
    private double netsalary;

    @Column
    private double tds;

    @Column
    private String trnyear;

    @Column
    private String oc1;

    @Column
    private String oc2;

    @Column
    private String aby;

    @Column
    private double tdeduction;

    @Column
    private double epf;

    @Column
    private double epf1;

    @Column
    private double epf2;

    @Column
    private double esi;

    @Column
    private double eesi;

    @Column
    private String esino;

    @Column
    private String epfno;

    @Column
    private String dispen;

    @Column
    private double eepf1per;

    @Column
    private double eepf2per;

    @Column
    private double desiper;

    @Column
    private double depfper;

    @Column
    private double eesiper;

    @Column
    private Long bankacno;

    @Column
    private String bankcode;

    @Column
    private String bank_ifsc;

    @Column
    private Long year;


    @Column
    private Long month;

    @Column
    private Long desigid;

    @Column
    private String designation;

    @Column
    private Long company;

    @Column
    private String firm;

    @Column
    private String fps;

    @Column
    private String fpmode;

    @Column
    private double mlsalary;

    @Column
    private double lincentive;

    @Column
    private double lsalary;

    @Column
    private String lpaid;

    @Column
    private String eexem;

    @Column
    private String epfexem;

    @Column
    private String esiexem;

    @Column
    private double mwdays;

    @Column
    private double mldays;

    @Column
    private double mlpay;

    @Column
    private double allowance;

    @Column
    private String contract;
}
