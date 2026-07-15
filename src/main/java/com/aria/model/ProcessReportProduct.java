package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "prgrppro")
public class ProcessReportProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Long  prid;
    @Column
    private Long prgrpid;
    @Column
    private Long proid;
    @Column
    private double lot;
    @Column
    private Long prprogrp;
    @Column
    private String lotname;
    @Column
    private Long createdby ;
}
