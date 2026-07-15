package com.aria.dto;

import org.apache.commons.text.StringEscapeUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExpenseVoucherListDto {
	private Long id;
    private String customVoucherName;
    private String secondaryExpenseName;
    private String departmentName;
    private String contype;
    private String category;
    
    public ExpenseVoucherListDto(ExpenseVoucherList expenseVoucherList)
    {
    	this.id=expenseVoucherList.getId();
    	this.customVoucherName=expenseVoucherList.getCustomVoucherName();
    	this.secondaryExpenseName=StringEscapeUtils.unescapeHtml4(expenseVoucherList.getSecondaryExpenseName());
    	this.departmentName=expenseVoucherList.getDepartmentName();
    	this.contype=expenseVoucherList.getContype();
    	this.category=expenseVoucherList.getCategory();
    }

}
