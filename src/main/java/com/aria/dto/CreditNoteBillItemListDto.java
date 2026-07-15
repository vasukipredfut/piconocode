package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CreditNoteBillItemListDto {





    private String billNumber;


    private Long vendorId;


    private Long productId;

    private String productName;

    private Double quantity;

    private Double unitRate;


    private Double cgstPercentage;


    private Double cgstAmount;


    private Double sgstPercentage;


    private Double sgstAmount;


    private Double igstPercentage;


    private Double igstAmount;


    private Double lineTotal;


    private Long stockInwardBy;
    private String stockInwardDate;
    private String stockInwardDateTime;


    private String lot;

    public CreditNoteBillItemListDto(CreditNoteBillItemList billItemList ) {
        DateUtil dateUtil = new DateUtil();
        this.billNumber = billItemList.getBillNumber();
        this.vendorId = billItemList.getVendorId();
        this.productId = billItemList.getProductId();
        this.productName = billItemList.getProductName();
        this.unitRate = billItemList.getUnitRate();
        this.cgstPercentage = billItemList.getCgstPercentage();
        this.cgstAmount = billItemList.getCgstAmount();
        this.sgstPercentage = billItemList.getSgstPercentage();
        this.sgstAmount = billItemList.getSgstAmount();
        this.igstPercentage = billItemList.getIgstPercentage();
        this.igstAmount = billItemList.getIgstAmount();
        this.lineTotal = billItemList.getLineTotal();
        this.stockInwardBy = billItemList.getStockInwardBy();
        this.stockInwardDate = dateUtil.getLocalDateTimeToDateString(billItemList.getStockInwardDate());
        this.stockInwardDateTime = dateUtil.getLocalDateTimeToDateTimeString(billItemList.getStockInwardDate());
        this.lot = billItemList.getLot();
        this.quantity = billItemList.getQuantity();
    }
}
