package com.aria.model;


import com.aria.dto.SalesReturnDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sales_return")
public class SalesReturn {




    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "firm_id", referencedColumnName = "id")
    private Organisation firmOrg;

    @OneToOne
    @JoinColumn(name = "vendor_id", referencedColumnName = "id")
    private Vendor vendor;


    @Column(name = "trnno")
    private String trnNo;

    @Column(name = "trndate")
    @Temporal(TemporalType.DATE)
    private Date trnDate;


    @Column(name = "refno") //bill No
    private String referenceNo;


    @Column(name = "bill_date")
    @Temporal(TemporalType.DATE)
    private Date billDate;


    @Column(name = "amount")
    private double amount;

    @Column(name = "vid")
    private Long vid;


    @Column(name = "von")
    private String von;


    @Column(name = "con")
    private String con;


    @Column(name = "vby")
    private Long verifyBy;


    @Column(name = "branch")
    private Long branch;

    @Column(name = "dept")
    private Long dept;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updatedBy")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private String status;

    @Column
    private String return_reason;

    @Column
    private String return_type;


    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column
    private LocalDateTime verified_date;

    @Column(name = "rejected_by")
    private Long rejectedBy;

    @Column
    private LocalDateTime rejected_date;


    @OneToMany(mappedBy = "returnId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SalesReturnProducts> salesReturnProducts;


    public SalesReturn(SalesReturnDto salesReturn) {
        DateUtil dateUtil = new DateUtil();
        this.id = salesReturn.getId();
        if (salesReturn.getFirmId() != null) {
            Organisation organisation = new Organisation();
            organisation.setId(salesReturn.getFirmId());
            this.firmOrg = organisation;
        }
        if (salesReturn.getVendorId() != null) {

            Vendor vendor = new Vendor();
            vendor.setId(salesReturn.getVendorId());
            this.vendor = vendor;
            this.referenceNo = salesReturn.getReferenceNo();
            this.billDate = dateUtil.getStringTODateWk(salesReturn.getBillDate());

            this.trnNo = salesReturn.getTrnNo();
            this.trnDate = dateUtil.getStringTODateWk(salesReturn.getTrnDate());

            this.amount = salesReturn.getAmount();

            this.vid = salesReturn.getVid();
            this.von = salesReturn.getVon();
            this.con = salesReturn.getCon();
            this.dept = salesReturn.getDept();
            this.branch = salesReturn.getBranch();

            this.createdBy = salesReturn.getCreatedBy();
            this.updatedBy = salesReturn.getUpdatedBy();

            this.return_reason = salesReturn.getReturnReason();
            this.return_type = salesReturn.getReturnType();

        }

    }
}