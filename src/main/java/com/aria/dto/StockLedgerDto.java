package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class StockLedgerDto {

    private Long id;
    private String typeName;
    private String entryType;
    private String type;
    private String txnNo;
    private String txnDate;
    private String accountingDate;
    private String narration;
    private Double issue;
    private String lot;
    private Double receipt;
    private String balance;
    private Long binNo;


    public StockLedgerDto(StockLedger stockLedger) {
        DateUtil dateUtil = new DateUtil();
        this.id = stockLedger.getId();
        this.entryType = stockLedger.getType();
        if (stockLedger.getType() != null && stockLedger.getType().equalsIgnoreCase("Stock Inward")) {
            this.typeName = "Purchases";
        } else if (stockLedger.getType() != null && stockLedger.getType().equalsIgnoreCase("Excess")) {
            this.typeName = "Purchases-Excess";
        } else if (stockLedger.getType() != null && stockLedger.getType().equalsIgnoreCase("Shortage")) {
            this.typeName = "Purchases-Shortage";
        } else if (stockLedger.getType() != null && stockLedger.getType().equalsIgnoreCase("PurchaseReturn")) {
            this.typeName = "Purchase-Return";
        } else if (stockLedger.getType() != null && stockLedger.getType().equalsIgnoreCase("PRODN")) {
            this.typeName = "Production";
        } else if (stockLedger.getType() != null && stockLedger.getType().equalsIgnoreCase("Shortage")) {
            this.typeName = "Purchases-Shortage";
        }else {
            this.typeName = stockLedger.getType();
        }

        if (stockLedger.getAccountingDate() != null) {
            this.accountingDate = dateUtil.getDateTOStringWk(stockLedger.getAccountingDate());
        }
        
        if(stockLedger.getType().equalsIgnoreCase("OPENING")){
            this.narration = "By Opening Balance As on "+accountingDate;
        }else {
            this.narration = stockLedger.getNarration();
        }

        this.txnNo = stockLedger.getTxnNo();
        
        if(stockLedger.getLot() != null) {
            this.lot = stockLedger.getLot().toString();
        }
        
        if (stockLedger.getTxnDate() != null) {
            this.txnDate = dateUtil.getDateTOStringWk(stockLedger.getTxnDate());
        }

        if (stockLedger.getTrnType() != null && stockLedger.getTrnType().equalsIgnoreCase("I")) {
            this.issue = stockLedger.getQty();
            this.type = "ISSUES";
        } else if (stockLedger.getTrnType() != null && stockLedger.getTrnType().equalsIgnoreCase("R")) {
            this.receipt = stockLedger.getQty();
            this.type = "RECEIPT";
        }
        this.balance = stockLedger.getBalance();
        this.binNo = stockLedger.getBinNo();

    }

    public StockLedgerDto(StockLedger stockLedger, String type) {
        DateUtil dateUtil = new DateUtil();
        this.typeName = type;
        if (stockLedger.getAccountingDate() != null) {
            this.accountingDate = dateUtil.getDateTOStringWk(stockLedger.getAccountingDate());
        }
        if (stockLedger.getTxnDate() != null) {
            this.txnDate = dateUtil.getDateTOStringWk(stockLedger.getTxnDate());
        }
        this.narration = "By Opening Balance As on "+accountingDate;
        this.balance = stockLedger.getBalance();
    }
}
