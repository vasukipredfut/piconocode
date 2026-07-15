package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EpfSalaryListDTO {


    private int empCount;
    private String connectTable;
    private double epfTotalSal;
    private Long firmId;
    private String firmName;
    private Long branchId;
    private String branchName;
    private String year;
    private Long month;


    public EpfSalaryListDTO(EpfSalaryList epfSalaryList) {
        this.empCount = epfSalaryList.getEmpCount();
        this.connectTable = epfSalaryList.getConnectTable();
        this.epfTotalSal = epfSalaryList.getEpfTotalSal();
        this.firmId = epfSalaryList.getFirmId();
        this.firmName = epfSalaryList.getFirmName();
        this.branchId = epfSalaryList.getBranchId();
        this.branchName = epfSalaryList.getBranchName();
        this.year = epfSalaryList.getYear();
        this.month = epfSalaryList.getMonth();
    }
}
