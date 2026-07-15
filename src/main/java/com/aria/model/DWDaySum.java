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
@Table(name = "dwdaysum")
public class DWDaySum {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String trnno;

    @Column
    private Date shiftdate;

    @Column
    private Long  shift;

    @Column
    private Long  pshift;

    @Column
    private Long  did;

    @Column
    private Long bid;

    @Column
    private Long trnweekid;

    @Column
    private String  usecid;

    @Column
    private double amt;

    @Column
    private Long cby;

    @Column
    private String con;

    @Column
    private Long vby;

    @Column
    private String von;
}
