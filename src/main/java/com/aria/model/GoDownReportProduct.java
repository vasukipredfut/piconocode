package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "go_down_report_product")
public class GoDownReportProduct {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private String name;

    @Column
    private String iname;

    @Column
    private String rname;

    @Column
    private String lr;

    @Column
    private String le;

    @Column
    private String ls;

    @Column
    private String dp;

    @Column
    private String pr;

    @Column
    private Long createdby;

    @Column
    private String RMC;
}
