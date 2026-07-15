package com.aria.dto;

import java.util.Optional;


import com.aria.dao.BinStockRepository;
import com.aria.model.BinStock;
import com.aria.model.PurchaseReturnStoredBinDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseReturnStoredBinDetailsReceiptDto {
	
	private Long id;

    private Long returnProductId;

    private Long itemId;

    private String itemName;

    private Long binNo;

    private String batchNo;

    private Double currentStock;

    private Double excessOrShortage;
    
    private double amount;
    
    public PurchaseReturnStoredBinDetailsReceiptDto(PurchaseReturnStoredBinDetails binDetails, BinStockRepository binStockRepository) {
        this.id = binDetails.getId();
        this.returnProductId = binDetails.getReturnProductId();
        this.itemId = binDetails.getItemId();
        this.itemName = binDetails.getItemName();
        this.binNo = binDetails.getBinNo();
        this.batchNo = binDetails.getBatchNo();
        this.currentStock = binDetails.getCurrentStock();
        this.excessOrShortage = binDetails.getExcessOrShortage();
        Optional<BinStock> binstock = Optional.ofNullable(binStockRepository.findByBatchNoAndBinName(binNo, batchNo));
        if(binstock.isPresent()) {
        	double amount = 0;
        	amount = binstock.get().getAvgCost() * binDetails.getExcessOrShortage();
        	this.amount = amount;
        }


    }

}
