package com.aria.model;

import com.aria.dto.PhysicalStockDto;
import com.aria.dto.PhysicalStockItems;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;


@Entity
@NoArgsConstructor
@Table(name = "physical_stock")
@Getter
@Setter
public class PhysicalStock {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 
    
    @Column
    private Long refid; 
    
    @Column
    private String name;

    @Column
    private String binid;

    @Column
    private Long binname;
    
    @Column
    private Long gid; 
    
    @Column
    private Long bid;

    @Column
    private Long  did;

    @Column
    private Double qty;
    
    @Column
    private Double lot;

    @Column(updatable = false)
    @Temporal(TemporalType.DATE)
    private Date adate;

    @Column
    private Long aby; 

    @Column
    private String status;

    public PhysicalStock(PhysicalStockItems physicalStock) {
        this.refid = physicalStock.getRefid();
        this.name = physicalStock.getProductName();
        this.binid = physicalStock.getBinid();
        this.binname = physicalStock.getBinname();
        this.gid = physicalStock.getGid();
        this.bid = physicalStock.getBranch();
        this.did = physicalStock.getDept();
        this.qty = physicalStock.getQty();
        this.lot = physicalStock.getLot();
        this.status = "C";
    }


    public PhysicalStock(PhysicalStockDto physicalStockDto) {
        this.id = physicalStockDto.getId();
        this.refid = physicalStockDto.getRefid();
        this.name = physicalStockDto.getName();
        this.binid = physicalStockDto.getBinid();
        this.binname = physicalStockDto.getBinname();
        this.gid = physicalStockDto.getGid();
        this.bid = physicalStockDto.getBid();
        this.did = physicalStockDto.getDid();
        this.qty = physicalStockDto.getQty();
        this.lot = physicalStockDto.getLot();

    }
}
