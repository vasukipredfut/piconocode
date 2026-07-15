package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EsiSalaryListDTO {


    private int empCount;
    private String connectTable;
    private double esiTotalSal;
    private Long firmId;
    private String firmName;
    private Long branchId;
    private String branchName;
    private String year;
    private Long month;


    public EsiSalaryListDTO(EsiSalaryList esiSalaryList) {
        this.empCount = esiSalaryList.getEmpCount();
        this.connectTable = esiSalaryList.getConnectTable();
        this.esiTotalSal = esiSalaryList.getEsiTotalSal();
        this.firmId = esiSalaryList.getFirmId();
        this.firmName = esiSalaryList.getFirmName();
        this.branchId = esiSalaryList.getBranchId();
        this.branchName = esiSalaryList.getBranchName();
        this.year = esiSalaryList.getYear();
        this.month = esiSalaryList.getMonth();
    }
}
