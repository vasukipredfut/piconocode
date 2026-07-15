package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
public class InProcessListDto {

    private Long id;

    private Long proreportDbId;

    private Long tgId;

    private String name;

    private Long positionNumber;
    private String attributeType;
    private String inputType;

    private String valueType;
    private String direct_cummulative;
    private String formula;

    private String minValue;

    private String maxValue;

    private Long score;

    private String unit;

    private String attributesResValue;

    private Long attributesResId;

    private List<AttributeFieldsProcessTypeOptionBasedDto> options;

    public InProcessListDto(InProcessList attributesList) {
        this.id = attributesList.getId();
        this.proreportDbId = attributesList.getProreportDbId();
        this.tgId = attributesList.getTgId();
        this.name = attributesList.getName();
        this.positionNumber = attributesList.getPositionNumber();
        this.attributeType = attributesList.getAttributeType();
        this.inputType = attributesList.getInputType();
        this.valueType = attributesList.getValueType();
        this.direct_cummulative = attributesList.getDirect_cummulative();
        this.formula = attributesList.getFormula();
        this.minValue = attributesList.getMinValue();
        this.maxValue = attributesList.getMaxValue();
        this.score = attributesList.getScore();
        this.unit = attributesList.getUnit();
        this.attributesResValue = attributesList.getAttributesResValue();
        this.attributesResId = attributesList.getAttributesResId();
    }

}
