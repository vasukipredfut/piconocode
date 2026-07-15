package com.aria.model;

import com.aria.dto.SalaryStructureDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.csv.CSVRecord;

import jakarta.persistence.*;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "salary_structure")
public class SalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aid;

    @Column
    private Long id;

    @Column
    private Long trnyear;

    @Column
    private String name;

    @Column
    private Long dacategory;

    @Column
    private Double minbasic;

    @Column
    private Double da;

    @Column
    private Double allowance;

    @Column
    private Double bper;

    @Column
    private Double gwper;

    @Column
    private Double convper;

    @Column
    private Double maxconv;

    @Column
    private Double minconv;

    @Column
    private Double minhra;

    @Column
    private String status;

    @Column
    private String applyto;

    @Column
    private String fixed;

    @Column
    private String usecid;

    @Column
    private Double desiper;

    @Column
    private Double depfper;

    @Column
    private Double eesiper;

    @Column
    private Double eepf1per;

    @Column
    private Double eepf2per;

    @Column
    private Double epfbase;

    @Column
    private Double esiceil;
    
    @Column
    private String  wtype;
    
    
    

    public SalaryStructure(SalaryStructureDto salaryStructure) {
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

    public SalaryStructure(CSVRecord csvRecord) {

        if (csvRecord.get("ID") != null && !csvRecord.get("ID").trim().isEmpty()) {
            this.aid = Long.valueOf(csvRecord.get("ID"));
        }
        if (csvRecord.get("TRN_YEAR") != null && !csvRecord.get("TRN_YEAR").trim().isEmpty()) {
            this.trnyear = Long.valueOf(csvRecord.get("TRN_YEAR"));
        }

        if (csvRecord.get("DESIGNATION") != null && !csvRecord.get("DESIGNATION").trim().isEmpty()) {
            this.name = csvRecord.get("DESIGNATION");
        }
        
        
        if (csvRecord.get("DESIGNATIONID") != null && !csvRecord.get("DESIGNATIONID").trim().isEmpty() ) {
            
        	this.id = Long.valueOf(csvRecord.get("DESIGNATIONID"));
        }
        if (csvRecord.get("APPLICABLE_TO") != null && !csvRecord.get("APPLICABLE_TO").trim().isEmpty()) {
            this.applyto = csvRecord.get("APPLICABLE_TO");
        }

        if (csvRecord.get("BASIC") != null && !csvRecord.get("BASIC").trim().isEmpty()) {
            this.minbasic = Double.valueOf(csvRecord.get("BASIC"));
        }
        if (csvRecord.get("DA") != null && !csvRecord.get("DA").trim().isEmpty()) {
            this.da = Double.valueOf(csvRecord.get("DA"));
            // DA = 0.01 means CONTRACT
            if (this.da != null && this.da == 0.01) {
                this.wtype = "CONTRACT";
            }
        }
        if (csvRecord.get("BASIC_DA_PERCENTAGE") != null && !csvRecord.get("BASIC_DA_PERCENTAGE").trim().isEmpty()) {
            this.bper = Double.valueOf(csvRecord.get("BASIC_DA_PERCENTAGE"));
        }
        if (csvRecord.get("FIXED_ALLOWANCE") != null && !csvRecord.get("FIXED_ALLOWANCE").trim().isEmpty()) {
            this.allowance = Double.valueOf(csvRecord.get("FIXED_ALLOWANCE"));
        }
        if (csvRecord.get("GOOD_WORK") != null && !csvRecord.get("GOOD_WORK").trim().isEmpty()) {
            this.gwper = Double.valueOf(csvRecord.get("GOOD_WORK"));
        }
        if (csvRecord.get("CONVEYANCE") != null && !csvRecord.get("CONVEYANCE").trim().isEmpty()) {
            this.convper = Double.valueOf(csvRecord.get("CONVEYANCE"));
        }

        if (csvRecord.get("MINMUM_CONVEYANCE") != null && !csvRecord.get("MINMUM_CONVEYANCE").trim().isEmpty()) {
            this.minconv = Double.valueOf(csvRecord.get("MINMUM_CONVEYANCE"));
        }
        if (csvRecord.get("MAXIMUM_CONVEYANCE") != null && !csvRecord.get("MAXIMUM_CONVEYANCE").trim().isEmpty()) {
            this.maxconv = Double.valueOf(csvRecord.get("MAXIMUM_CONVEYANCE"));
        }
        if (csvRecord.get("TOTAL_SALARY_TYPE") != null && !csvRecord.get("TOTAL_SALARY_TYPE").trim().isEmpty()) {
            this.fixed = csvRecord.get("TOTAL_SALARY_TYPE");
        }
        if (csvRecord.get("COMPANY_ESI") != null && !csvRecord.get("COMPANY_ESI").trim().isEmpty()) {
            this.eesiper = Double.valueOf(csvRecord.get("COMPANY_ESI"));
        }
        if (csvRecord.get("DEDUCTION_ESI") != null && !csvRecord.get("DEDUCTION_ESI").trim().isEmpty()) {
            this.desiper = Double.valueOf(csvRecord.get("DEDUCTION_ESI"));
        }
        if (csvRecord.get("ESI_CEILING") != null && !csvRecord.get("ESI_CEILING").trim().isEmpty()) {
            this.esiceil = Double.valueOf(csvRecord.get("ESI_CEILING"));
        }
        if (csvRecord.get("COMPANY_EPF") != null && !csvRecord.get("COMPANY_EPF").trim().isEmpty()) {
            this.eepf1per = Double.valueOf(csvRecord.get("COMPANY_EPF"));
        }
        if (csvRecord.get("COMPANY_FPF") != null && !csvRecord.get("COMPANY_FPF").trim().isEmpty()) {
            this.eepf2per = Double.valueOf(csvRecord.get("COMPANY_FPF"));
        }
        if (csvRecord.get("DEDUCTION_FPF") != null && !csvRecord.get("DEDUCTION_FPF").trim().isEmpty()) {
            this.depfper = Double.valueOf(csvRecord.get("DEDUCTION_FPF"));
        }
        if (csvRecord.get("EPF_BASE") != null && !csvRecord.get("EPF_BASE").trim().isEmpty()) {
            this.epfbase = Double.valueOf(csvRecord.get("EPF_BASE"));
        }
        if (csvRecord.get("MINMUM_HRA") != null && !csvRecord.get("MINMUM_HRA").trim().isEmpty()) {
            this.minhra = Double.valueOf(csvRecord.get("MINMUM_HRA"));
        }

        if (csvRecord.get("STATUS") != null && !csvRecord.get("STATUS").trim().isEmpty()) {
            this.status = csvRecord.get("STATUS");
        } else {
            this.status = "U";
        }

    }

}
