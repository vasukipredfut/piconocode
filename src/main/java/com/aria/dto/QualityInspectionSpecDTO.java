package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

import com.aria.model.QualityInspectionSpec;
import com.aria.model.SupplierEvaluationAttributes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QualityInspectionSpecDTO {

	private Long purchaseQualityInspectionScoreId;

    private Long billItemRefId;

    private Long specId;

    private Long itemRefId;

    private String description;

    private Integer parameterScore;

    private Integer activeFlag;


    public QualityInspectionSpecDTO(QualityInspectionSpec model) {
        this.purchaseQualityInspectionScoreId = model.getPurchase_quality_inspection_score_id();
        this.billItemRefId = model.getBillItemRefId();
        this.specId = model.getSpecId();
        this.itemRefId = model.getItemRefId();
        this.description = model.getDescription();
        this.parameterScore = model.getParameterScore();
        this.activeFlag = model.getActiveFlag();
    }

}
