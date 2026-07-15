package com.aria.model;

import com.aria.dto.BlendingAdviceStockDetailsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "blending_advice_stock_details")
public class BlendingAdviceStockDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blending_advice_details_id")
    private Long blendingAdviceDetailsId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "cas_group")
    private String casGroup;

    @Column(name = "no_of_bags")
    private Double noOfBags;

    
    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private String status;

    @OneToMany(mappedBy = "blendingAdviceStockId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BlendingAdviceBinDetails> binDetails;

    public BlendingAdviceStockDetails(BlendingAdviceStockDetailsDto blendingAdviceStockDetails) {
        this.id = blendingAdviceStockDetails.getId();
        this.blendingAdviceDetailsId = blendingAdviceStockDetails.getBlendingAdviceDetailsId();
        this.casGroup = blendingAdviceStockDetails.getCasGroup();
        this.noOfBags = blendingAdviceStockDetails.getNoOfBags();
        this.created_date = blendingAdviceStockDetails.getCreated_date();
        this.updated_date = blendingAdviceStockDetails.getUpdated_date();
        this.productId = blendingAdviceStockDetails.getProductId();
    }
}
