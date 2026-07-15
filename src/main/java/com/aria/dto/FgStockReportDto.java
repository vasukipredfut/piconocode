package com.aria.dto;

import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FgStockReportDto {

	
    private Long itemId;
    private String itemName;
    private Double lot;
    private String unit;
    // -------- OPENING / CLOSING --------
    private Double openingQty;
    private Double closingQty;

    // -------- RECEIPT --------
    private Double purchaseQty;          // Stock Inward
    private Double productionReceiveQty; // PRODUCTION - R
    private Double excessQty;             // Excess
    private Double salesReturnQty;        // SALES_RETURN - I

    // -------- ISSUE --------
    private Double productionIssueQty;    // PRODUCTION - I
    private Double salesQty;              // DISPATCH - I
    private Double purchaseReturnQty;     // PurchaseReturn
    private Double shortageQty;
    private Double splitIssue;
    private Double splitReceipt;
    private Map<String, Double> interReceiptQty;
    private Map<String, Double> interIssueQty;
	    
}
