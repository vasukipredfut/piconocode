package com.aria.model;


import com.aria.dto.HoSalesOrderDTO;
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
@Table(name = "Ho_Sales_Order")
public class HoSalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long hoId;

    @Column
    private String refType;

    @Column
    private Date billDate;

    @Column
    private String billNo;
    @Column
    private String salesInvNo;

    @Column
    private Long companyBranchId;

    @Column
    private Long hocustomerId;

    @Column
    private Long hodeliverAt;

    @Column
    private String totalAmount;

    @Column
    private String currency;

    @Column
    private String currencyName;

    @Column
    private Date createdDt;

    @Column
    private Long hoitemGroupId;

    @Column
    private String saleType;
    @Column
    private String totalCess;

    @Column
    private String cessRate;

    @Column
    private String netWeight;

    @Column
    private String grossWeight;

    @Column
    private String roundOff;

    @Column
    private String exchangeRate;

    @Column
    private String cumPackingNotes;

    @Column
    private String exportTo;

    @Column
    private String exporterRef;

    @Column
    private String buyerOrderDate;

    @Column
    private String buyerOrderNo;

    @Column
    private String otherReference;

    @Column
    private String buyerDetail;

    @Column
    private String orgineCountry;

    @Column
    private String finalDestincountry;

    @Column
    private String precarriageBy;

    @Column
    private String receiptPlace;

    @Column
    private String flightNo;

    @Column
    private String fobRemark;

    @Column
    private String advancePercentRemark;

    @Column
    private String balancePercentRemark;

    @Column
    private String displayKgs;

    @Column
    private Long hubId;

    @Column
    private String totalCgst;

    @Column
    private String totalSgst;

    @Column
    private String totalIgst;

    @Column
    private String siStatus;

    @Column
    private String totalValue;

    @Column
    private String totalTaxableValue;

    @Column
    private String totalDiscount;

    @Column
    private String grossTotal;

    @Column
    private String totalQtyPcs;

    @Column
    private String totalQtyBox;

    @Column
    private String stateCode;

    @Column
    private String totalVolume;

    @Column
    private String containerMark;

    @Column
    private Long createdBy;

    @Column
    private String netContent;

    @Column
    private String empName;

    @Column
    private String custName;

    @Column
    private String locationName;

    @Column
    private String gstNo;

    @Column
    private String scheme;

    @Column
    private String schemePercent;

    @Column
    private String qd;

    @Column
    private String splDisc;

    @Column
    private String targetDiscount;

    @Column
    private Long invType;

    @Column
    private String panNo;

    @Column
    private String transporterId;

    @Column
    private String DistKm;

    @Column
    private String hoVechicleNo;

    @Column
    private String totalTcs;

    @Column
    private String tcsPercent;

    @Column
    private String totalMcess;

    @Column
    private Long tripId;

    @Column
    private Long tripSort;

    @Column
    private String invOrderNo;

    @Column
    private Date invOrderDate;

    @Column
    private String ewayBill;

    @Column
    private Date ewayBilldate;

    @Column
    private Date irnAckdate;

    @Column
    private String irnAckno;
    @Column
    private String irnNo;


    @Column(updatable = false)
    private String raw_ho_sales_Order;


    public HoSalesOrder(HoSalesOrderDTO hoSalesInvoice) {
        DateUtil dateUtil = new DateUtil();
        this.id = hoSalesInvoice.getId();
        this.hoId = hoSalesInvoice.getHoId();
        this.refType = hoSalesInvoice.getRefType();
        this.billDate = dateUtil.getStringTODateYYYYMMDD(hoSalesInvoice.getBillDate());
        this.billNo = hoSalesInvoice.getBillNo();
        this.salesInvNo = hoSalesInvoice.getSalesInvNo();
        this.companyBranchId = hoSalesInvoice.getCompanyBranchId();
        this.hocustomerId = hoSalesInvoice.getHocustomerId();
        this.hodeliverAt = hoSalesInvoice.getHodeliverAt();
        this.totalAmount = hoSalesInvoice.getTotalAmount();
        this.currency = hoSalesInvoice.getCurrency();
        this.currencyName = hoSalesInvoice.getCurrencyName();
        this.createdDt = dateUtil.getStringTODateYYYYMMDD(hoSalesInvoice.getCreatedDt());
        this.hoitemGroupId = hoSalesInvoice.getHoitemGroupId();
        this.saleType = hoSalesInvoice.getSaleType();
        this.totalCess = hoSalesInvoice.getTotalCess();
        this.cessRate = hoSalesInvoice.getCessRate();
        this.netWeight = hoSalesInvoice.getNetWeight();
        this.grossWeight = hoSalesInvoice.getGrossWeight();
        this.roundOff = hoSalesInvoice.getRoundOff();
        this.exchangeRate = hoSalesInvoice.getExchangeRate();
        this.cumPackingNotes = hoSalesInvoice.getCumPackingNotes();
        this.exportTo = hoSalesInvoice.getExportTo();
        this.exporterRef = hoSalesInvoice.getExporterRef();
        this.buyerOrderDate = hoSalesInvoice.getBuyerOrderDate();
        this.buyerOrderNo = hoSalesInvoice.getBuyerOrderNo();
        this.otherReference = hoSalesInvoice.getOtherReference();
        this.buyerDetail = hoSalesInvoice.getBuyerDetail();
        this.orgineCountry = hoSalesInvoice.getOrgineCountry();
        this.finalDestincountry = hoSalesInvoice.getFinalDestincountry();
        this.precarriageBy = hoSalesInvoice.getPrecarriageBy();
        this.receiptPlace = hoSalesInvoice.getReceiptPlace();
        this.flightNo = hoSalesInvoice.getFlightNo();
        this.fobRemark = hoSalesInvoice.getFobRemark();
        this.advancePercentRemark = hoSalesInvoice.getAdvancePercentRemark();
        this.balancePercentRemark = hoSalesInvoice.getBalancePercentRemark();
        this.displayKgs = hoSalesInvoice.getDisplayKgs();
        this.hubId = hoSalesInvoice.getHubId();
        this.totalCgst = hoSalesInvoice.getTotalCgst();
        this.totalSgst = hoSalesInvoice.getTotalSgst();
        this.totalIgst = hoSalesInvoice.getTotalIgst();
        this.siStatus = hoSalesInvoice.getSiStatus();
        this.totalValue = hoSalesInvoice.getTotalValue();
        this.totalTaxableValue = hoSalesInvoice.getTotalTaxableValue();
        this.totalDiscount = hoSalesInvoice.getTotalDiscount();
        this.grossTotal = hoSalesInvoice.getGrossTotal();
        this.totalQtyPcs = hoSalesInvoice.getTotalQtyPcs();
        this.totalQtyBox = hoSalesInvoice.getTotalQtyBox();
        this.stateCode = hoSalesInvoice.getStateCode();
        this.totalVolume = hoSalesInvoice.getTotalVolume();
        this.containerMark = hoSalesInvoice.getContainerMark();
        this.createdBy = hoSalesInvoice.getCreatedBy();
        this.netContent = hoSalesInvoice.getNetContent();
        this.empName = hoSalesInvoice.getEmpName();
        this.custName = hoSalesInvoice.getCustName();
        this.locationName = hoSalesInvoice.getLocationName();
        this.gstNo = hoSalesInvoice.getGstNo();
        this.scheme = hoSalesInvoice.getScheme();
        this.schemePercent = hoSalesInvoice.getSchemePercent();
        this.qd = hoSalesInvoice.getQd();
        this.splDisc = hoSalesInvoice.getSplDisc();
        this.targetDiscount = hoSalesInvoice.getTargetDiscount();
        this.invType = hoSalesInvoice.getInvType();
        this.panNo = hoSalesInvoice.getPanNo();
        this.transporterId = hoSalesInvoice.getTransporterId();
        this.DistKm = hoSalesInvoice.getDistKm();
        this.hoVechicleNo = hoSalesInvoice.getHoVechicleNo();
        this.totalTcs = hoSalesInvoice.getTotalTcs();
        this.tcsPercent = hoSalesInvoice.getTcsPercent();
        this.totalMcess = hoSalesInvoice.getTotalMcess();
        this.tripId = hoSalesInvoice.getTripId();
        this.tripSort = hoSalesInvoice.getTripSort();
        this.invOrderNo = hoSalesInvoice.getInvOrderNo();
        this.invOrderDate = dateUtil.getStringTODateYYYYMMDD(hoSalesInvoice.getInvOrderDate());

        this.ewayBill = hoSalesInvoice.getInvOrderNo();
        this.ewayBilldate = dateUtil.getStringTODateYYYYMMDD(hoSalesInvoice.getInvOrderDate());

        this.irnAckno = hoSalesInvoice.getIrnAckno();
        this.irnNo = hoSalesInvoice.getIrnNo();
        this.irnAckdate = dateUtil.getStringTODateYYYYMMDD(hoSalesInvoice.getIrnAckdate());
    }
}
