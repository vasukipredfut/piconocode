package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalesvraResList {
	
	public Long id;
	
     public String billno;
     
     public String trnno;
     
     public String employeename;
     
     public String emptype;
     
     public String deductiontype;
     
     public String branch;
     
     public String company;
     
     public String dept;
     
     public Long month;
     
     public Long year;

	public SalesvraResList(SalesvraList salesvra){
		this.id=salesvra.getId();
		this.billno = salesvra.getBillno();
		this.trnno = salesvra.getTrnno();
		this.emptype=salesvra.getEmptype();
		this.employeename = salesvra.getEmployeename();
		this.branch = salesvra.getBranch();
		this.company = salesvra.getCompany();
		this.dept = salesvra.getDept();
		this.month = salesvra.getMonth();
		this.year = salesvra.getYear();
	}
     
     
}
