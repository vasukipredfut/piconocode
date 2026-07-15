package com.aria.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExpensesListDto {
	private Long id;
    private String secName;
    private String secType;
    private Double cost;   
    private String unit;
    private String  primaryExpName;
    private Long  groupId;
   
    
    public ExpensesListDto(ExpensesList expensesList)
    {
    	this.id=expensesList.getId();
    	this.secName=StringEscapeUtils.unescapeHtml4(expensesList.getSecName());
    	this.secType=expensesList.getSecType();
    	this.cost=expensesList.getCost();
    	this.unit=expensesList.getUnit();
    	this.primaryExpName=expensesList.getPrimaryExpName();
    	this.groupId=expensesList.getGroupId();
    	
//    	String deptStr = expensesList.getPrimaryExpName();
//        this.primaryExpName = deptStr != null
//                ? Arrays.asList(deptStr.split("\\s*,\\s*"))
//                : new ArrayList<>();
    }


	

}
