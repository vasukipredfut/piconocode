package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "process_report_group_title")
public class ProcessReportTitle {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String prgrptitle;

    @Column
    private Long  prid;

    @Column
    private Long prgrpid;

    @Column
    private Long prprgrp;

    @Column
    private String trntype;

    @Column
    private Long createdby;

    @Column
    private String disp_name;

    @Column
    private Long order;
}
