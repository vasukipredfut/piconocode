package com.aria.dto;


import com.aria.model.ConsProducts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
@NoArgsConstructor
@Getter
@Setter
public class ConsProductsDto {

    private Long id;

   
    private String product;

   
    private Long refid;

   
    private Double lot;

   
    private Long eid;

   
    private String etable;

   
    private Long process;

   
    private Long processTable;

   
    private Long createdBy;

   
    private LocalDateTime createdOn;

    public ConsProductsDto(ConsProducts consProducts) {
        this.id = consProducts.getId();
        this.product = consProducts.getProduct();
        this.refid = consProducts.getRefid();
        this.lot = consProducts.getLot();
        this.eid = consProducts.getEid();
        this.etable = consProducts.getEtable();
        this.process = consProducts.getProcess();
        this.processTable = consProducts.getProcessTable();
        this.createdBy = consProducts.getCreatedBy();
        this.createdOn = consProducts.getCreatedOn();
    }
}
