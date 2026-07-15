package com.aria.model;

import com.aria.dto.JournalProductMappingDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "journal_product_mapping")
public class JournalProductMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "journal_id")
    private Long journalId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "consider_as")
    private String considerAs;

    @Column(name = "bom_take_type")
    private String bomTakeType;

    @Column(name = "bptype")
    private String batchProductType;

    @Column
    private String quantity;

    @Column
    private String cost;

    @Column
    private Double cost_per;

    @Column
    private Double fqty;

    @Column
    private Double cost_amount;

    @Column
    private String pridunit;

    @Column
    private String activity;

    @Column
    private String trackno;

    @Column
    private Boolean status;

    @Column(name = "applicable_For_Jcal")
    private Boolean applicableForJcal;

    @Column(name = "bottleweight")
    private String bottleWeight;




    public JournalProductMapping(JournalProductMappingDto journalProductMapping) {
        this.id = journalProductMapping.getId();
        this.journalId = journalProductMapping.getJournalId();
        this.productId = journalProductMapping.getProductId();
        this.productName = journalProductMapping.getProductName();
        this.considerAs = journalProductMapping.getConsiderAs();
        this.batchProductType = journalProductMapping.getBatchProductType();
        this.quantity = journalProductMapping.getQuantity();
        this.cost = journalProductMapping.getCost();
        this.cost_per = journalProductMapping.getCostPer();
        this.fqty = journalProductMapping.getFqty();
        this.cost_amount = journalProductMapping.getCostAmount();
        this.pridunit = journalProductMapping.getPridunit();
        this.activity = journalProductMapping.getActivity();
        this.trackno = journalProductMapping.getTrackno();
        this.bomTakeType = journalProductMapping.getBomTakeType();
        this.applicableForJcal = journalProductMapping.getApplicableForJcal();
        this.bottleWeight = journalProductMapping.getBottleWeight();

    }
}
