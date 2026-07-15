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
@Table(name = "journal_entry")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String trntype;

    @Column
    private String trnno;

    @Column
    private Date trndate;

    @Column
    private Long pshiftid;

    @Column
    private Long sjid;

    @Column
    private Long wcid;

    @Column
    private String wctable;

    @Column
    private Long lineid;

    @Column
    private Long priid;

    @Column
    private Double priqty;

    @Column
    private Double prilot;

    @Column
    private Long proid;

    @Column
    private String binid;

    @Column
    private Long binname;

    @Column
    private Long batchid;

    @Column
    private String batchno;

    @Column
    private String reduce_batchno;

    @Column
    private String type;

    @Column
    private String ctype;

    @Column
    private Double cper;

    @Column
    private Double stkqty;

    @Column
    private Double sstkqty;

    @Column
    private Double lot;

    @Column
    private Double avgcost;

    @Column
    private Double cost;

    @Column
    private String atrnno;

    @Column
    private Date atrndate;

    @Column
    private Long atrnid;

    @Column
    private String usecid;

    @Column
    private String sflag;

    @Column
    private String astatus;

    @Column
    private Long aby;

    @Column
    private String aon;

    @Column
    private Long eby;

    @Column
    private String casgrp;

    @Column
    private Long casweekid;  // blending Advice details id

    @Column
    private Long godown;

    @Column
    private String srce;

    @Column
    private Long did;

    @Column
    private Long bid;

    @Column
    private Long vid;

    @Column
    private Long poid;

    @Column
    private Long billid;

    @Column
    private Date bdt;

    @Column
    private Long rowid;

    @Column
    private Long instype;

    @Column
    private String exid;

    @Column
    private String tasum;

    @Column
    private Double tastk;

    @Column
    private String bptype;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date adate;

    @Column(name = "total_input_qty")
    private Double totalInputQty;

    @Column(name = "total_output_qty")
    private Double totalOutputQty;

    @Column(name = "sal_status")
    private String salStatus;

    @Column(name = "accounting_date")
    @Temporal(TemporalType.DATE)
    private Date accounting_date;

    @Column(name = "firm_id")
    private Long firmId;

    @Column(name = "bom_Intake_Type")
    private String bomIntakeType;
}
