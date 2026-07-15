package com.aria.dto;


import com.aria.model.HoSalesOrder;
import com.aria.model.HoSalesOrderItems;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class HoSalesOrderDTO {


    private Long id;


    private Long hoId;


    private String refType;


    private String billDate;


    private String billNo;

    private String salesInvNo;


    private Long companyBranchId;


    private Long hocustomerId;


    private Long hodeliverAt;


    private String totalAmount;


    private String currency;


    private String currencyName;


    private String createdDt;


    private Long hoitemGroupId;


    private String saleType;

    private String totalCess;


    private String cessRate;


    private String netWeight;


    private String grossWeight;


    private String roundOff;


    private String exchangeRate;


    private String cumPackingNotes;


    private String exportTo;


    private String exporterRef;


    private String buyerOrderDate;


    private String buyerOrderNo;


    private String otherReference;


    private String buyerDetail;


    private String orgineCountry;


    private String finalDestincountry;


    private String precarriageBy;


    private String receiptPlace;


    private String flightNo;


    private String fobRemark;


    private String advancePercentRemark;


    private String balancePercentRemark;


    private String displayKgs;


    private Long hubId;


    private String totalCgst;


    private String totalSgst;


    private String totalIgst;


    private String siStatus;


    private String totalValue;


    private String totalTaxableValue;


    private String totalDiscount;


    private String grossTotal;


    private String totalQtyPcs;


    private String totalQtyBox;


    private String stateCode;


    private String totalVolume;


    private String containerMark;


    private Long createdBy;


    private String netContent;


    private String empName;


    private String custName;


    private String locationName;


    private String gstNo;


    private String scheme;


    private String schemePercent;


    private String qd;


    private String splDisc;


    private String targetDiscount;


    private Long invType;


    private String panNo;


    private String transporterId;


    private String DistKm;


    private String hoVechicleNo;


    private String totalTcs;


    private String tcsPercent;


    private String totalMcess;


    private Long tripId;


    private Long tripSort;


    private String invOrderNo;


    private String invOrderDate;

    private String ewayBill;


    private String ewayBilldate;


    private String irnAckdate;


    private String irnAckno;

    private String irnNo;

    List<HoSalesOrderItemsDTO> invoiceItem;

    List<HoSalesOrderHsnDTO> invoiceHsninfo;

    public HoSalesOrderDTO(HoSalesOrder hoSalesInvoice) {

        DateUtil dateUtil = new DateUtil();

        this.id = hoSalesInvoice.getId();
        this.hoId = hoSalesInvoice.getHoId();
        this.refType = hoSalesInvoice.getRefType();
        this.billDate = dateUtil.getDateTOStringYYYYMMDD(hoSalesInvoice.getBillDate());
        this.billNo = hoSalesInvoice.getBillNo();
        this.salesInvNo = hoSalesInvoice.getSalesInvNo();
        this.companyBranchId = hoSalesInvoice.getCompanyBranchId();
        this.hocustomerId = hoSalesInvoice.getHocustomerId();
        this.hodeliverAt = hoSalesInvoice.getHodeliverAt();
        this.totalAmount = hoSalesInvoice.getTotalAmount();
        this.currency = hoSalesInvoice.getCurrency();
        this.currencyName = hoSalesInvoice.getCurrencyName();
        this.createdDt = dateUtil.getDateTOStringYYYYMMDD(hoSalesInvoice.getCreatedDt());
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
        this.invOrderDate = dateUtil.getDateTOStringYYYYMMDD(hoSalesInvoice.getInvOrderDate());
        this.irnAckno = hoSalesInvoice.getIrnAckno();
        this.irnNo = hoSalesInvoice.getIrnNo();
        this.irnAckdate = dateUtil.getDateTOStringYYYYMMDD(hoSalesInvoice.getIrnAckdate());
    }
}
