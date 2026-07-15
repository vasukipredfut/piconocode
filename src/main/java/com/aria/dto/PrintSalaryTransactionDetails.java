package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PrintSalaryTransactionDetails {


    private Long companyId;
    private String companyName;
    private String companyAddress;
    private String year;
    private Long month;
    private Long branchId;
    private String branchName;
    private String branchAddress;
    private String branchManager;
    private String regNo;

    private int totalEmployees;
    private int totalMens;
    private int totalWomens;


    List<SalaryTransactionDetailListDTO> salaryTransactionDetailList;
}
