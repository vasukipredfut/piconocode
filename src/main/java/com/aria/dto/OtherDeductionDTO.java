package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OtherDeductionDTO {


    public String connectTable;
     
    public Long id;

    public Long empId;

    public String empName;

    public Long deptId;
    public String depName;

    public Long companyId;
    public String companyName;

    public Long branchId;

    public String branchName;

    public String name;

    public String status;

    public String description;

    public Long month;
    public Long year;
    
    public String employeetype;
    
    public double amount;

    public String generatebill;


    public OtherDeductionDTO(OtherDeductionList otherDeductionList ) {
        this.connectTable = otherDeductionList.getConnectTable();
        this.id=otherDeductionList.getId();
        this.empId = otherDeductionList.getEmpId();
        this.empName = otherDeductionList.getEmpName();
        this.deptId = otherDeductionList.getDeptId();
        this.depName = otherDeductionList.getDepName();
        this.companyId = otherDeductionList.getCompanyId();
        this.companyName = otherDeductionList.getCompanyName();
        this.branchId = otherDeductionList.getBranchId();
        this.branchName = otherDeductionList.getBranchName();
        this.name = otherDeductionList.getName();
        this.status = otherDeductionList.getStatus();
        this.description = otherDeductionList.getDescription();
        this.month = otherDeductionList.getMonth();
        this.year = otherDeductionList.getYear();
        this.employeetype=otherDeductionList.getEmployeetype();
        this.amount = otherDeductionList.getAmount();
        this.generatebill=otherDeductionList.getGeneratebill();
    }
}
