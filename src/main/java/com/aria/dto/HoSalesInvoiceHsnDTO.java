package com.aria.dto;


import com.aria.model.HoSalesInvoiceHsn;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class HoSalesInvoiceHsnDTO {

    private Long id;

    private Long hoInvoiceId;

    private Long hoInvoiceHsnInfoId;


    private String hsnCode;


    private Long hoitemGroupId;

    private String proValue;

    private String discountValue;

    private String taxableValue;

    private String totalCgst;

    private String totalSgst;

    private String totalIgst;

    private String tcs;

    private String totalTcs;

    private String netAmount;

    private Long createdBy;

    private String createdDate;

    private Long updatedBy;

    private String updatedDate;

    private String soStatus;

    private Long branchId;

    private String refType;

    private String roundOff;

    private String cessAmount;

    private String cgst;

    private String sgst;

    private String igst;

    private String mcess;

    private String totalMcess;




    public HoSalesInvoiceHsnDTO(HoSalesInvoiceHsn invoiceHsn) {
        DateUtil dateUtil = new DateUtil();
        this.id = invoiceHsn.getId();
        this.hoInvoiceId = invoiceHsn.getHoInvoiceId();
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
        this.createdDate = dateUtil.getDateTOStringYYYYMMDD(invoiceHsn.getCreatedDate());
        this.updatedBy = invoiceHsn.getUpdatedBy();
        this.updatedDate = dateUtil.getDateTOStringYYYYMMDD(invoiceHsn.getUpdatedDate());
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
