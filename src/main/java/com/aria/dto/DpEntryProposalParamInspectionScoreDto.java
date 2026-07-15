package com.aria.dto;

import com.aria.model.DpEntryProposalParamInspectionScore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class DpEntryProposalParamInspectionScoreDto {

    private Long id;
    private String poNumber;
    private Long billId;
    private Long itemId;
    private Long paramId;
    private Long poId;
    private int paramScore;

    public DpEntryProposalParamInspectionScoreDto(DpEntryProposalParamInspectionScore paramInspectionScore) {
        this.id = paramInspectionScore.getId();
        this.poNumber = paramInspectionScore.getPoNumber();
        this.billId = paramInspectionScore.getBill_ref_id();
        this.itemId = paramInspectionScore.getItemId();
        this.paramId = paramInspectionScore.getParamId();
        this.paramScore = paramInspectionScore.getParamScore();
    }
}
