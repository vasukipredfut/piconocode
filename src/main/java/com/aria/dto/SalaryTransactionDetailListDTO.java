package com.aria.dto;

import java.util.Date;

import com.aria.model.SalaryTransaction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalaryTransactionDetailListDTO {


    private Long categoryId;
    private String category;
    private String connectTable;
    private Long trnId;
    private String trnNo;
    private String year;
    private String noOfDays;
    private Long month;
    private Long branchId;
    private String branchName;
    private Long companyId;
    private String companyName;


    private Long empId;
    private String empName;
    private double workedDays;
    private double basic;
    private double da;
    private double gwa;
    private double hra;
    private double wa;
    private double ca;
    private double allowance;
    private double leaveHolidayBasic;
    private double leaveHolidayDa;
    private double leaveWagesBasicDa;
    private double grossSalary;
    private double providentFund;
    private double employeeWelfareFund;
    private double employerWelfareFund;
    private double messBill;
    private double uniformDeduction;
    private double otherDeduction;
    private double professionalTax;
    private double tds;
    private double epf1;
    private double epf2;
    private double totalDeduction;
    private double netSalary;
    private String accountNo;
    private String ifsc;
    private String bankName;
    
    private Date fromDate;
    private Date toDate;
    


    public SalaryTransactionDetailListDTO(SalaryTransactionDetailList salaryTransactionList) {

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
        this.noOfDays = salaryTransactionList.getNoOfDays();
        this.month = salaryTransactionList.getMonth();
        this.branchId = salaryTransactionList.getBranchId();
        this.branchName = salaryTransactionList.getBranchName();
        this.companyId = salaryTransactionList.getCompanyId();
        this.companyName = salaryTransactionList.getCompanyName();

        this.empId = salaryTransactionList.getEmpId();
        this.empName = salaryTransactionList.getEmpName();
        this.workedDays = salaryTransactionList.getWorkedDays();
        this.basic = salaryTransactionList.getBasic();
        this.da = salaryTransactionList.getDa();
        this.gwa = salaryTransactionList.getGwa();
        this.hra = salaryTransactionList.getHra();
        this.wa = salaryTransactionList.getWa();
        this.ca = salaryTransactionList.getCa();
        this.allowance = salaryTransactionList.getAllowance();
        if (salaryTransactionList.getLeaveHolidayBasic() != null) {
            this.leaveHolidayBasic = salaryTransactionList.getLeaveHolidayBasic();
        } else {
            this.leaveHolidayBasic = 0.0;
        }
        if (salaryTransactionList.getLeaveHolidayDa() != null) {
            this.leaveHolidayDa = salaryTransactionList.getLeaveHolidayDa();
        } else {
            this.leaveHolidayDa = 0.0;
        }
        if (salaryTransactionList.getLeaveWagesBasicDa() != null) {
            this.leaveWagesBasicDa = salaryTransactionList.getLeaveWagesBasicDa();
        } else {
            this.leaveWagesBasicDa = 0.0;
        }
        this.grossSalary = salaryTransactionList.getGrossSalary();
        if (salaryTransactionList.getProvidentFund() != null) {
            this.providentFund = salaryTransactionList.getProvidentFund();
        } else {
            this.providentFund = 0.0;
        }
        if (salaryTransactionList.getEmployeeWelfareFund() != null) {
            this.employeeWelfareFund = salaryTransactionList.getEmployeeWelfareFund();
        } else {
            this.employerWelfareFund = 0.0;
        }
        if (salaryTransactionList.getEmployerWelfareFund() != null) {
            this.employerWelfareFund = salaryTransactionList.getEmployerWelfareFund();
        } else {
            this.employerWelfareFund = 0.0;
        }
        this.messBill = salaryTransactionList.getMessBill();
        if (salaryTransactionList.getUniformDeduction() != null) {
            this.uniformDeduction = salaryTransactionList.getUniformDeduction();
        } else {
            this.uniformDeduction = 0.0;
        }
        if (salaryTransactionList.getOtherDeduction() != null) {
            this.otherDeduction = salaryTransactionList.getOtherDeduction();
        } else {
            this.otherDeduction = 0.0;
        }
        if (salaryTransactionList.getProfessionalTax() != null) {
            this.professionalTax = salaryTransactionList.getProfessionalTax();
        } else {
            this.professionalTax = 0.0;
        }
        this.tds = salaryTransactionList.getTds();
        if (salaryTransactionList.getEpf1() != null) {
            this.epf1 = salaryTransactionList.getEpf1();
        } else {
            this.epf1 = 0.0;
        }
        if (salaryTransactionList.getEpf2() != null) {
            this.epf2 = salaryTransactionList.getEpf2();
        } else {
            this.epf2 = 0.0;
        }
        this.totalDeduction = salaryTransactionList.getTotalDeduction();
        this.netSalary = salaryTransactionList.getNetSalary();
        this.accountNo = salaryTransactionList.getAccountNo();
        this.ifsc = salaryTransactionList.getIfsc();
        this.bankName = salaryTransactionList.getBankName();
        this.fromDate = salaryTransactionList.getFromDate();
        this.toDate = salaryTransactionList.getToDate();
    }


}
