
package com.aria.dto;

import java.util.Date;

import com.aria.model.WelfareEmployee;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WelfareEmployeeDto {

    private  Long id;
    private Long empId;
    private  String designation;
    private  String employeeName;
    private  Date registrationon;
    private  String item;
    private  Long cost;

    public WelfareEmployeeDto(WelfareEmployee welfareEmployee) {
    	 this.id =welfareEmployee.getId();
    	if(welfareEmployee.getUser() != null) {
            this.empId = welfareEmployee.getUser().getId();
            this.designation = welfareEmployee.getUser().getDesignation().getName();
            this.employeeName = welfareEmployee.getUser().getEmployeename();
        }
    	this.registrationon = welfareEmployee.getRegistrationon();
        this.item =welfareEmployee.getItem();
        this.cost = welfareEmployee.getCost();
    }
}
