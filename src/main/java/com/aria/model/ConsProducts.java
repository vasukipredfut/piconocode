package com.aria.model;

import com.aria.dto.ConsProductsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "consproducts")
@Getter
@Setter
public class ConsProducts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String product;
    
    @Column
    private Long refid;
    
    @Column
    private Double lot;
    
    @Column
    private Long eid; 
    
    @Column
    private String etable;
    
    @Column
    private Long process;

    @Column(name = "process_table")
    private Long processTable;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_On")
    private LocalDateTime createdOn;


    public ConsProducts(ConsProductsDto consProducts) {
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
