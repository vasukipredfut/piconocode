package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "dwdaysum")
public class DWDayTrn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long trnid;
    @Column
    private String trnno;
    @Column
    private Long empcode;
    @Column
    private String name;
    @Column
    private Long desigid;
    @Column
    private String  designation;
    @Column
    private String sex;
    @Column
    private Long jdate;
    @Column
    private Long expyears;
    @Column
    private Long  basic;
    @Column
    private Long da;
    @Column
    private Long wa;
    @Column
    private Long ca;
    @Column
    private Long hra;
    @Column
    private Long gwa;
    @Column
    private Long allowance;
    @Column
    private Long total;
    @Column
    private Long shiftdate;
    @Column
    private Long shiftid;
    @Column
    private Long pshift;
    @Column
    private Long did;
    @Column
    private Long bid;
    @Column
    private Long cid;
    @Column
    private Long trnweekid;
    @Column
    private Long usecid;
    @Column
    private Long pstatus;
    @Column
    private Long intime;
    @Column
    private Long outtime;
    @Column
    private Long salwkid;
    @Column
    private Long ototal;
    @Column
    private Long oda;
    @Column
    private Long oallow;
}
