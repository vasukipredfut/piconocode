package com.aria.dto;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@Getter
@Setter
public class OrderResponse {
	private Long id;
	private Long ordernumber;
	private String order_billno;
	private String order_status;
	private Date order_billdate;
	private String createdBy;
	private String vendorCity;
	private String vendorName;
	
	public OrderResponse(Long id,Long orderNumber, String billNo, Date billDate, String city,String name,String employeename, String orderStatus) {
		this.id=id;
		this.ordernumber=orderNumber;
		this.order_billno=billNo;
		this.order_billdate=billDate;
		this.createdBy=employeename;
		this.vendorCity=city;
		this.vendorName=name;
		this.order_status=orderStatus;
	}
}
