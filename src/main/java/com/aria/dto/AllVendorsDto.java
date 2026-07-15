package com.aria.dto;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.aria.model.Order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class AllVendorsDto {
	private Long id;
	private String vendorNAme;
	
	public AllVendorsDto(Long id,String name) {
		this.id=id;
		this.vendorNAme=name;
	}
}
