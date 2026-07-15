package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalaryTransactionListDTO {

    private Long categoryId;
    private String category;
    private String connectTable;
    private Long trnId;
    private String trnNo;
    private String year;
    private Long month;
    private Long branchId;
    private String branchName;
    private Long companyId;
    private String companyName;

    private Long deptId;
    private String deptName;
    private String status;


    public SalaryTransactionListDTO(SalaryTransactionList salaryTransactionList) {
        this.categoryId = salaryTransactionList.getCategoryId();
        this.connectTable = salaryTransactionList.getConnectTable();
        if (salaryTransactionList.getConnectTable() != null && salaryTransactionList.getConnectTable().equalsIgnoreCase("emaster")) {

            if (salaryTransactionList.getCategoryId() != null && salaryTransactionList.getCategoryId() == 1) {
                category = "Staff";
            } else {
                category = "StaffApprentice";
            }
        } else {
            if (salaryTransactionList.getCategoryId() != null && salaryTransactionList.getCategoryId() == 1) {
                category = "Wages";
            } else {
                category = "WagesApprentice";
            }
        }
        this.trnId = salaryTransactionList.getTrnId();
        this.trnNo = salaryTransactionList.getTrnNo();
        this.year = salaryTransactionList.getYear();
        this.month = salaryTransactionList.getMonth();
        this.branchId = salaryTransactionList.getBranchId();
        this.branchName = salaryTransactionList.getBranchName();
        this.companyId = salaryTransactionList.getCompanyId();
        this.companyName = salaryTransactionList.getCompanyName();
        this.deptId = salaryTransactionList.getDept();
        this.status=salaryTransactionList.getStatus();
    }
}
