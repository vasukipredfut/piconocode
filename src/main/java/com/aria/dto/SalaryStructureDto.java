package com.aria.dto;


import com.aria.model.SalaryStructure;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalaryStructureDto {


    private Long id;


    private Long aid;


    private Long trnyear;


    private String name;


    private Long dacategory;


    private Double minbasic;


    private Double da;


    private Double allowance;


    private Double bper;


    private Double gwper;


    private Double convper;


    private Double maxconv;


    private Double minconv;


    private Double minhra;


    private String status;


    private String applyto;


    private String fixed;


    private String usecid;


    private Double desiper;


    private Double depfper;


    private Double eesiper;


    private Double eepf1per;


    private Double eepf2per;


    private Double epfbase;


    private Double esiceil;

    public SalaryStructureDto(SalaryStructure salaryStructure) {
        this.id = salaryStructure.getId();
        this.aid = salaryStructure.getAid();
        this.trnyear = salaryStructure.getTrnyear();
        this.name = salaryStructure.getName();
        this.dacategory = salaryStructure.getDacategory();
        this.minbasic = salaryStructure.getMinbasic();
        this.da = salaryStructure.getDa();
        this.allowance = salaryStructure.getAllowance();
        this.bper = salaryStructure.getBper();
        this.gwper = salaryStructure.getGwper();
        this.convper = salaryStructure.getConvper();
        this.maxconv = salaryStructure.getMaxconv();
        this.minconv = salaryStructure.getMinconv();
        this.minhra = salaryStructure.getMinhra();
        this.status = salaryStructure.getStatus();
        this.applyto = salaryStructure.getApplyto();
        this.fixed = salaryStructure.getFixed();
        this.usecid = salaryStructure.getUsecid();
        this.desiper = salaryStructure.getDesiper();
        this.depfper = salaryStructure.getDepfper();
        this.eesiper = salaryStructure.getEesiper();
        this.eepf1per = salaryStructure.getEepf1per();
        this.eepf2per = salaryStructure.getEepf2per();
        this.epfbase = salaryStructure.getEpfbase();
        this.esiceil = salaryStructure.getEsiceil();
    }
}
