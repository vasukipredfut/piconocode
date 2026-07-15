package com.aria.dto;


import com.aria.model.Broker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class BrokerDto {


	private Long id;

	private String name;

	private String contact;

	private String address;

	private String mobile;

	private Long parentid;


	public BrokerDto(Broker broker) {
		
		this.id=broker.getId();
		this.name=broker.getName();
		this.contact=broker.getContact();
		this.address=broker.getAddress();
		this.mobile=broker.getMobile();
		this.parentid=broker.getParentid();
	
	}
}
