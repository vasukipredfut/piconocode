package com.aria.dto;

import com.aria.model.ConsumptionDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionDetailDto {

    public Long id;
    public Long dpEntryStoredProductId;
    public String consumptionName;
    public String consumptionDatefrom;
    public String consumptionDateto;
    public Long consumedBy;
    public String remarks;
    public Double consumptionQty;
    public Double consumptionCost;



    public ConsumptionDetailDto(ConsumptionDetails consumptionDetails) {
        this.id = consumptionDetails.getId();
        this.dpEntryStoredProductId = consumptionDetails.getDpEntryStoredProductId();
        this.consumptionName = consumptionDetails.getConsumptionName();
        this.consumptionDatefrom = consumptionDetails.getConsumptionDatefrom();
        this.consumptionDateto = consumptionDetails.getConsumptionDateto();
        this.consumedBy = consumptionDetails.getConsumedBy();
        this.remarks = consumptionDetails.getRemarks();
        this.consumptionQty = consumptionDetails.getConsumptionQty();
        this.consumptionCost = consumptionDetails.getConsumptionCost();
    }
}
