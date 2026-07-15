package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "brmproids")
public class BRMProducts {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lno;

    @Column
    private Long id;

    @Column
    private String name;

    @Column
    private Long did;

    @Column
    private Long bid;

    @Column
    private String smanage;

    @Column
    private String dispunit1;

    @Column
    private String dispunit2;

    @Column
    private Long stkgrp;

    @Column
    private String status;

    @Column
    private String pcategory;

    @Column
    private Long createdby;

    @Column(name = "day_End_Closing_Flag")
    private Boolean dayEndClosingFlag;
}
