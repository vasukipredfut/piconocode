package com.aria.model;


import com.aria.dto.BrokerDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "broker ")
public class Broker {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@Column
	private String contact;

	@Column
	private String address;

	@Column
	private String mobile;

	@Column
	private Long parentid;


	public Broker(BrokerDto broker) {
		this.id=broker.getId();
		this.name=broker.getName();
		this.contact=broker.getContact();
		this.address=broker.getAddress();
		this.mobile=broker.getMobile();
		this.parentid=broker.getParentid();
	}
}
