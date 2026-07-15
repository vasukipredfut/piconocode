package com.aria.model;


import com.aria.dto.SupplierEvaluationDTO;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@NoArgsConstructor
@Table(name = "supeval")  // client  table name
@Getter
@Setter
public class SupplierEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long branch;

    @Column
    private Long did;

    @Column
    private Long cid;

    @Column
    @Temporal(TemporalType.DATE)
    private Date fromdate;

    @Column
    @Temporal(TemporalType.DATE)
    private Date todate;

    @Column
    private Long vid;

    @Column
    private String trnno;
    @Column
    private String created;

    @Column
    //@Temporal(TemporalType.DATE)
    private Date created_date;

    @Column
    private Long created_by;

    @Column
    private String verify;

    @Column
    //@Temporal(TemporalType.DATE)
    private Date verify_date;
    @Column
    private Long verify_staff;

    @Column
    private String approval;

    @Column
    //@Temporal(TemporalType.DATE)
    private Date approved_date;

    @Column
    private Long approval_staff;

    @Column
    private String authenty;

    @Column
    //@Temporal(TemporalType.DATE)
    private Date authorised_date;
    @Column
    private Long authorise_staff;
    @Column
    private String blocklist;
    @Column
    private Date entry;
    @Column
    private String mailsent;

    @Column
    private String status;

    public SupplierEvaluation(SupplierEvaluationDTO supplierEvaluation) {
        DateUtil dateUtil = new DateUtil();
        this.id = supplierEvaluation.getId();
        this.branch = supplierEvaluation.getBranch();
        this.did = supplierEvaluation.getDid();
        this.cid = supplierEvaluation.getCompany();
        this.vid = supplierEvaluation.getVendorId();
        this.fromdate = dateUtil.getStringTODateWk(supplierEvaluation.getFromDate());
        this.todate = dateUtil.getStringTODateWk(supplierEvaluation.getToDate());
        this.mailsent = supplierEvaluation.getMailsent();
        this.status = supplierEvaluation.getStatus();
    }
}
