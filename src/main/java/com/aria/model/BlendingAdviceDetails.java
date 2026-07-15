package com.aria.model;

import com.aria.dto.BlendingAdviceDetailsDto;
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
@Table(name = "blending_advice_details")
public class BlendingAdviceDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blending_advice_id")
    private Long blendingAdviceId;

    @Column(name = "journal_id")
    private Long journalId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "product_id")
    public Long productId;

    @Column(name = "total_no_of_kalasal")
    private Double totalNoOfKalasal;

    @Column(name = "total_no_of_kalasal_bag")
    private Double totalNoOfKalasalBag;

    @Column(name = "completed_no_of_kalasal")
    private Double completedNoOfKalasal;

    @Column(name = "cas_trn")
    private String casTransaction;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @OneToMany(mappedBy = "blendingAdviceDetailsId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BlendingAdviceStockDetails> stockDetails;

    @OneToMany(mappedBy = "blendingAdviceDetailsId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BlendingAdvicePreProcess> preProcesses;


    @Column
    private String status;

    public BlendingAdviceDetails(BlendingAdviceDetailsDto blendingAdviceDetails) {
        this.id = blendingAdviceDetails.getId();
        this.blendingAdviceId = blendingAdviceDetails.getBlendingAdviceId();
        this.journalId = blendingAdviceDetails.getJournalId();
        this.branchId = blendingAdviceDetails.getBranchId();
        this.totalNoOfKalasal = blendingAdviceDetails.getTotalNoofKalasal();
        this.totalNoOfKalasalBag = blendingAdviceDetails.getTotalNoofKalasalBag();
        this.productId = blendingAdviceDetails.getProductId();
        this.created_date = blendingAdviceDetails.getCreated_date();
        this.updated_date = blendingAdviceDetails.getUpdated_date();
        this.casTransaction = blendingAdviceDetails.getCasTransaction();
        this.status = blendingAdviceDetails.getStatus();
    }
}
