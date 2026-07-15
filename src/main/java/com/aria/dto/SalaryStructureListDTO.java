package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalaryStructureListDTO {

    private Long desiId;
    private String designation;
    private String status;
    private Long year;
    private String applyTo;
    private String salaryType;
    private double minimumBasic;
    private double da;
    private double allowance;
    private double hra;
    private Long dacatId;
    private String category;
    private String basicDaPercentage;
    private String goodWorkPercentage;
    private String conveyancePercentage;
    private String maximumConveyance;
    private String minimumConveyance;
    private String minimumHRA;


    public SalaryStructureListDTO(SalaryStructureList salaryStructureList) {
        this.desiId = salaryStructureList.getDesiId();
        this.designation = salaryStructureList.getDesignation();
        this.status = salaryStructureList.getStatus();
        this.year = salaryStructureList.getYear();
        this.applyTo = salaryStructureList.getApplyTo();
        this.salaryType = salaryStructureList.getSalaryType();
        this.minimumBasic = salaryStructureList.getMinimumBasic();
        this.da = salaryStructureList.getDa();
        this.allowance = salaryStructureList.getAllowance();
        this.hra = salaryStructureList.getHra();
        this.dacatId = salaryStructureList.getDacatId();
        this.category = salaryStructureList.getCategory();
        this.basicDaPercentage = salaryStructureList.getBasicDaPercentage();
        this.goodWorkPercentage = salaryStructureList.getGoodWorkPercentage();
        this.conveyancePercentage = salaryStructureList.getConveyancePercentage();
        this.maximumConveyance = salaryStructureList.getMaximumConveyance();
        this.minimumConveyance = salaryStructureList.getMinimumConveyance();
        this.minimumHRA = salaryStructureList.getMinimumHRA();
    }
}
