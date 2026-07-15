package com.aria.dto;

import java.util.Date;

import com.aria.model.Organisation;
import com.aria.model.ProductMaster;
import com.aria.model.Salesvra;
import com.aria.model.Salesvraitems;
import com.aria.model.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalesvraDto {
      
	public String gst;

	public String cst;

	public String phone;

	public String companyname;

	public String ccity;

	public String caddress;

	public String invoiceno;

	public Date billdate;

	public String employeename;

	public String eaddress;

	public Double amount;

	public String patriclur;

	public String hsncode;

	public Double rs;

	public Long qty;

	public Double cgst;

	public Double sgst;

	public Double igst;

	public Double roudoff;

	public SalesvraDto(Salesvra salesvra,Salesvraitems salesvraItems,Organisation org,User user) {
		this.gst = org.getGST();
		this.cst = org.getRegn_1();
		this.phone = org.getPhone();
		this.companyname = org.getName();
		this.ccity = org.getCity();
		this.caddress = org.getAddress();
		this.invoiceno = salesvra.getTrnno();
		this.billdate = salesvra.getBilldate();
		this.employeename = user.getEmployeename();
		this.eaddress = user.getAddress();
		this.amount = salesvra.getAmount();
		this.rs = salesvra.getAmount();
		this.qty = salesvraItems.getQty();
		this.cgst = salesvraItems.getCgst();
		this.sgst = salesvraItems.getSgst();
		this.igst = salesvraItems.getIgst();
		this.roudoff = salesvra.getRoundoff();
	}
}