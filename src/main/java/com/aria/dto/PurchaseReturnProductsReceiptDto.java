package com.aria.dto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.model.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseReturnProductsReceiptDto {

	private Long id;

	private Long purchaseReturnId;

	private Long productId;

	//private String productCode;

	private String productName;

	private String productType;

	private String batchNo;

	private String currentStock;

	private String lot;

	private String bin;

	private String returnQuantity;

	private String reason;

	private Double amount;

	private String hsbn;

	private String unit;
	private String unit_cost;
	private String unit_rate;

	private List<PurchaseReturnStoredBinDetailsReceiptDto> binDetails;
	
	public PurchaseReturnProductsReceiptDto(PurchaseReturnProducts purchaseReturnProducts, BinStockRepository binStockRepository, BillInwardItemReceivedRepository inwardItemReceivedRepository) {
		this.id = purchaseReturnProducts.getId();
		this.purchaseReturnId = purchaseReturnProducts.getPurchaseReturnId();
		this.productId = purchaseReturnProducts.getProduct().getId();
		this.productName = purchaseReturnProducts.getProduct().getProductName();
		this.hsbn=purchaseReturnProducts.getProduct().getHsnRefId();
		this.unit=purchaseReturnProducts.getProduct().getUnit();
		//this.productCode = purchaseReturnProducts.getProductCode();
		this.productType = purchaseReturnProducts.getProductType();
		this.batchNo = purchaseReturnProducts.getBatchNo();
		this.lot = purchaseReturnProducts.getLot();
		this.bin = purchaseReturnProducts.getBin();
		this.currentStock = purchaseReturnProducts.getCurrentStock();
		this.returnQuantity = purchaseReturnProducts.getReturnQuantity();
		this.reason = purchaseReturnProducts.getReason();
		Optional<BillInwardItemReceived> optional= inwardItemReceivedRepository.findByProductsIdANDBatchNo(productId, batchNo);
		if(optional.isPresent()){
			this.unit_cost = optional.get().getUnitCost();
			this.unit_rate = optional.get().getUnitRate();
		}

		this.binDetails = purchaseReturnProducts.getBinDetails() != null ? purchaseReturnProducts.getBinDetails().stream()
				.map(obj ->  new PurchaseReturnStoredBinDetailsReceiptDto(obj, binStockRepository))
				.collect(Collectors.toList()) :null;
		this.amount = binDetails.stream().mapToDouble(PurchaseReturnStoredBinDetailsReceiptDto::getAmount).sum();		
		
	}
}
