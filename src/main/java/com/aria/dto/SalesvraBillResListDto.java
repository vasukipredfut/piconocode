package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalesvraBillResListDto {
	
	private String invoiceno;
    private String billdate;
    private Double rate;
    private Double rs;
    private Double roundoff;
    private String gst;
    private String cst;
    private String phone;
    private String company;
    private String ccity;
    private String caddress;
    private String employeename;
    private String eaddress;
    private Integer qty;
    private Double cgst;
    private Double sgst;
    private Double igst;
    private Double cgstValue;
    private Double sgstValue;
    private Double igstValue;
    private String patriclur;
    private String hsncode;
	public SalesvraBillResListDto(SalevraBillListDto p) {
		super();
		 this.invoiceno = p.getInvoiceno();
	        this.billdate = p.getBilldate();
	        this.rate = p.getRate();
	        this.rs = p.getRs();
	        this.gst = p.getGst();
	        this.phone = p.getPhone();
	        this.company = p.getCompany();
	        this.ccity = p.getCcity();
	        this.caddress = p.getCaddress();
	        this.employeename = p.getEmployeename();
	        this.eaddress = p.getEaddress();
	        this.qty = p.getQty();
	        this.patriclur = p.getPatriclur();
	        this.hsncode = p.getHsncode();
	}
    
    

}
