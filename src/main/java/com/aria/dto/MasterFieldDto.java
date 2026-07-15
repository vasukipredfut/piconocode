package com.aria.dto;

import com.aria.model.MasterField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MasterFieldDto {


    private Long id;

    private String fieldName;

    private String fieldType;

    private String possibleValues;

    private String dataSelectionType;

    private String maxMumLength;

    private int active;

    private Long masterComponentId;

    private Long createdBy;

    private Long updatedBy;

    private String createdByName;

    private String updatedByName;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    public MasterFieldDto(MasterField masterFieldDto) {
        this.id = masterFieldDto.getId();
        this.fieldName = masterFieldDto.getFieldName();
        this.fieldType = masterFieldDto.getFieldType();
        this.possibleValues = masterFieldDto.getPossibleValues();
        this.dataSelectionType = masterFieldDto.getDataSelectionType();
        this.maxMumLength = masterFieldDto.getMaxMumLength();
        this.active = masterFieldDto.getActive();
        this.masterComponentId = masterFieldDto.getMasterComponentId();
        this.createdBy = masterFieldDto.getCreatedBy();
        this.updatedBy = masterFieldDto.getUpdatedBy();
        this.created_date = masterFieldDto.getCreated_date();
        this.updated_date = masterFieldDto.getUpdated_date();
    }
}
