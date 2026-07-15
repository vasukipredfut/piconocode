package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "linksj")
public class LinkSj {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long sjid;

    @Column
    private Long priid;

    @Column
    private Long proid;

    @Column
    private String cate;

    @Column
    private String qtype;

    @Column
    private String ctype;

    @Column
    private Double cper;

    @Column
    private Double fqty;

    @Column
    private String bptype;

    @Column
    private String cas;

    @Column
    private Double cost;

    @Column
    private String bproid;

    @Column
    private String trackno;

    @Column
    private Long batchproid;

    @Column
    private String pridunit;

    @Column
    private String activity;

    @Column
    private Long did;

    @Column
    private String proname;

    @Column
    private String sjname;

    @Column
    private String exid;

    @Column
    private String tasum;

    @Column
    private Double lmf;

    @Column
    private String exid1;

    @Column
    private String exop;

    @Column
    private String dry;

    @Column
    private String prm;

    @Column
    private String flm;

    @Column
    private String ldbag;

    @Column
    private String cbox;

    @Column(name = "bottle_Weight_From")
    private Double bottleWeightFrom;

    @Column(name = "bottle_Weight_To")
    private Double bottleWeightTo;

    @Column(name = "report_Activity")
    private String reportActivity;
}
