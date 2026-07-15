package com.aria.model;

import com.aria.dto.HoSalesInvoiceHsnDTO;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Ho_Sales_Invoice_Hsn")
public class HoSalesInvoiceHsn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hoinvoiceid")
    private Long hoInvoiceId;

    @Column
    private Long hoInvoiceHsnInfoId;

    @Column
    private String hsnCode;

    @Column
    private Long hoitemGroupId;
    @Column
    private String proValue;
    @Column
    private String discountValue;
    @Column
    private String taxableValue;
    @Column
    private String totalCgst;
    @Column
    private String totalSgst;
    @Column
    private String totalIgst;
    @Column
    private String tcs;
    @Column
    private String totalTcs;
    @Column
    private String netAmount;
    @Column
    private Long createdBy;
    @Column
    private Date createdDate;
    @Column
    private Long updatedBy;
    @Column
    private Date updatedDate;
    @Column
    private String soStatus;
    @Column
    private Long branchId;
    @Column
    private String refType;
    @Column
    private String roundOff;
    @Column
    private String cessAmount;
    @Column
    private String cgst;
    @Column
    private String sgst;
    @Column
    private String igst;
    @Column
    private String mcess;
    @Column
    private String totalMcess;



    public HoSalesInvoiceHsn(HoSalesInvoiceHsnDTO invoiceHsn) {
        DateUtil dateUtil = new DateUtil();
        this.id = invoiceHsn.getId();
        this.hoInvoiceId = invoiceHsn.getHoInvoiceHsnInfoId();
        this.hoInvoiceHsnInfoId = invoiceHsn.getHoInvoiceHsnInfoId();
        this.hsnCode = invoiceHsn.getHsnCode();
        this.hoitemGroupId = invoiceHsn.getHoitemGroupId();
        this.proValue = invoiceHsn.getProValue();
        this.discountValue = invoiceHsn.getDiscountValue();
        this.taxableValue = invoiceHsn.getTaxableValue();
        this.totalCgst = invoiceHsn.getTotalCgst();
        this.totalSgst = invoiceHsn.getTotalSgst();
        this.totalIgst = invoiceHsn.getTotalIgst();
        this.tcs = invoiceHsn.getTcs();
        this.totalTcs = invoiceHsn.getTotalTcs();
        this.netAmount = invoiceHsn.getNetAmount();
        this.createdBy = invoiceHsn.getCreatedBy();
        this.createdDate = dateUtil.getStringTODateYYYYMMDD(invoiceHsn.getCreatedDate());
        this.updatedBy = invoiceHsn.getUpdatedBy();
        this.updatedDate = dateUtil.getStringTODateYYYYMMDD(invoiceHsn.getUpdatedDate());
        this.soStatus = invoiceHsn.getSoStatus();
        this.branchId = invoiceHsn.getBranchId();
        this.refType = invoiceHsn.getRefType();
        this.roundOff = invoiceHsn.getRoundOff();
        this.cessAmount = invoiceHsn.getCessAmount();
        this.cgst = invoiceHsn.getCgst();
        this.sgst = invoiceHsn.getSgst();
        this.igst = invoiceHsn.getIgst();
        this.mcess = invoiceHsn.getMcess();
        this.totalMcess = invoiceHsn.getTotalMcess();
    }
}
