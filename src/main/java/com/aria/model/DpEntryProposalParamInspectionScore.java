package com.aria.model;

import com.aria.dto.DpEntryProposalParamInspectionScoreDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

@Entity
@NoArgsConstructor
@Table(name = "dp_entry_proposal_param_inspection_score")
@Getter
@Setter
@ToString
public class DpEntryProposalParamInspectionScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "po_number")
    private String poNumber;

    @Column
    private Long bill_ref_id;
    
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "param_id")
    private Long paramId;

    @Column(name = "param_score")
    private int paramScore;


    public DpEntryProposalParamInspectionScore(DpEntryProposalParamInspectionScoreDto paramInspectionScore) {
        this.id = paramInspectionScore.getId();
        this.poNumber = paramInspectionScore.getPoNumber();
        this.bill_ref_id = paramInspectionScore.getBillId();
        this.itemId = paramInspectionScore.getItemId();
        this.paramId = paramInspectionScore.getParamId();
        this.paramScore = paramInspectionScore.getParamScore();
    }
}
