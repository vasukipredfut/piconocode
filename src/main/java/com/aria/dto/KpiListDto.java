package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class KpiListDto {


    private Long stepId ;

    private String active;

    private int stepNumber;

    private String skip;

    private String taskName;

    private String taskCategory;

    private String taskGroup;

    private Long kpiNameId;

    private String kpiName;

    private Long productMasterId;

    private String productMasterName;

    private Long taskGroupMapId;

    private List<AttributesListDto> attributeFieldsProcessType;


    private String skipflag;
    private String stepStatus;

    public KpiListDto(KpiList kpiList) {
        this.stepId = kpiList.getStepId();
        this.active = kpiList.getActive();
        this.stepNumber = kpiList.getStepNumber();
        this.skip = kpiList.getSkip();
        this.taskName = kpiList.getTaskName();
        this.taskCategory = kpiList.getTaskCategory();
        this.taskGroup = kpiList.getTaskGroup();
        this.kpiNameId = kpiList.getKpiNameId();
        this.kpiName = kpiList.getKpiName();
        this.productMasterId = kpiList.getProductMasterId();
        this.productMasterName = kpiList.getProductMasterName();
        this.taskGroupMapId = kpiList.getTaskGroupMapId();
    }
}
