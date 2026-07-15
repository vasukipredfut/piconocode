package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MaintenanceAttributesListDto {

    private Long id;

    private Long proreportDbId;

    private Long tgId;

    private String name;

    private Long positionNumber;

    private String attributeType;

    private String inputType;

    private String valueType;

    private String minValue;

    private String maxValue;

    private String purpose_benefit;

    private String workinst_saftmeasure;

    private List<AttributeFieldsProcessTypeOptionBasedDto> options;
    private List<AttributeFieldsProcessTypeValueBasedDto> values;

    public MaintenanceAttributesListDto(MaintenanceAttributesList maintenanceAttributesList) {
        this.id = maintenanceAttributesList.getId();
        this.proreportDbId = maintenanceAttributesList.getProreportDbId();
        this.tgId = maintenanceAttributesList.getTgId();
        this.name = maintenanceAttributesList.getName();
        this.positionNumber = maintenanceAttributesList.getPositionNumber();
        this.attributeType = maintenanceAttributesList.getAttributeType();
        this.inputType = maintenanceAttributesList.getInputType();
        this.valueType = maintenanceAttributesList.getValueType();
        this.minValue = maintenanceAttributesList.getMinValue();
        this.maxValue = maintenanceAttributesList.getMaxValue();
        this.purpose_benefit = maintenanceAttributesList.getPurposeBenefit();
        this.workinst_saftmeasure = maintenanceAttributesList.getWorkinstSaftmeasure();
    }
}
