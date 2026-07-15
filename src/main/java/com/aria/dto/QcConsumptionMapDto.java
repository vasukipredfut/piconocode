package com.aria.dto;

import com.aria.model.QcConsumptionMap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QcConsumptionMapDto {
	private Long id;
    private Long configId;
    private Long conProduct;
    private Double requiredQty;
    private String conProductName;
    private Double lot;
	public QcConsumptionMapDto(QcConsumptionMap qcConsumptionMap) {
		
		this.id = qcConsumptionMap.getId();
		this.configId = qcConsumptionMap.getConfigId();
		this.conProduct = qcConsumptionMap.getConProduct();
		this.requiredQty = qcConsumptionMap.getRequiredQty();
		this.lot=qcConsumptionMap.getLot();
	}
    

}
