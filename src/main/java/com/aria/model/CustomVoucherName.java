
package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.CustomVoucherNameDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vouname")
public class CustomVoucherName {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	private String prefix;
	
	private Long vouFirmId;
	
	private Long dept;
	
	private String recordStatus;
	
	public CustomVoucherName(CustomVoucherNameDto customVouNameDto)
	{
		this.id=customVouNameDto.getId();
		this.name=customVouNameDto.getName();
		this.prefix=customVouNameDto.getPrefix();
		this.recordStatus=customVouNameDto.getRecordStatus();
		this.vouFirmId=customVouNameDto.getVouFirmId();
	}

}
