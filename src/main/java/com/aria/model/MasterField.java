package com.aria.model;

import com.aria.dto.MasterFieldDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "master_field")
public class MasterField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String fieldName;

    @Column
    private String fieldType;

    @Column
    private String possibleValues;

    @Column
    private String dataSelectionType;

    @Column
    private String maxMumLength;

    @Column
    private int active;

    @Column
    private Long masterComponentId;

    @Column(updatable = false)
    private Long createdBy;

    @Column
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;


    public MasterField(MasterFieldDto masterFieldDto) {
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
