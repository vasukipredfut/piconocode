package com.aria.model;


import com.aria.dto.ConsumptionDetailDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "consumption_details")
public class ConsumptionDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dp_entry_stored_product_id")
    private Long dpEntryStoredProductId;

    @Column(name = "consumption_name")
    private String consumptionName;

    @Column(name = "consumption_datefrom")
    private String consumptionDatefrom;
    
    @Column(name = "consumption_dateto")
    private String consumptionDateto;
    
    @Column(name = "consumed_by")
    private Long consumedBy;
    
    @Column(name = "remarks")
    private String remarks;
    
    @Column(name = "consumption_qty")
    private Double consumptionQty;

    @Column(name = "consumption_cost")
    private Double consumptionCost;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Getters and Setters



    public ConsumptionDetails(ConsumptionDetailDto dto) {
        this.id = dto.getId();
        this.consumptionName = dto.getConsumptionName();
        this.consumptionDatefrom = dto.getConsumptionDatefrom();
        this.consumptionDateto = dto.getConsumptionDateto();
        this.consumedBy = dto.getConsumedBy();
        this.remarks = dto.getRemarks();
        this.consumptionQty = dto.getConsumptionQty();
        this.consumptionCost = dto.getConsumptionCost();
    }
}