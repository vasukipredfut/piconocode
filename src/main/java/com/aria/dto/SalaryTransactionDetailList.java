package com.aria.dto;

import java.util.Date;

public interface SalaryTransactionDetailList {


    Long getCategoryId();


    String getCategory();

    String getConnectTable();

    Long getTrnId();

    String getTrnNo();

    String getYear();
    String getNoOfDays();

    Long getMonth();

    Long getBranchId();

    String getBranchName();

    Long getCompanyId();

    String getCompanyName();

    Long getEmpId();

    String getEmpName();

    double getWorkedDays();

    double getBasic();

    double getDa();

    double getGwa();

    double getHra();

    double getWa();

    double getCa();

    double getAllowance();

    Double getLeaveHolidayBasic();

    Double getLeaveHolidayDa();

    Double getLeaveWagesBasicDa();

    double getGrossSalary();

    Double getProvidentFund();

    Double getEmployeeWelfareFund();

    Double getEmployerWelfareFund();

    double getMessBill();

    Double getUniformDeduction();

    Double getOtherDeduction();

    Double getProfessionalTax();

    double getTds();

    double getTotalDeduction();

    double getNetSalary();
    
    Double getEpf1();

    Double getEpf2();

    String getAccountNo();

    String getIfsc();

    String getBankName();
    Date getFromDate();
    Date getToDate();
}
