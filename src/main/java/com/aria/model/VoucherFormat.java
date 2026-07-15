package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.VoucherFormatDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vouformat")
public class VoucherFormat {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column
	private String name;
	@Column
	private String isono;

	public VoucherFormat(VoucherFormatDto voucherFormatDto)
	{
		this.id=voucherFormatDto.getId();
		this.name=voucherFormatDto.getName();
		this.isono=voucherFormatDto.getIsono();
		
	}
	
}
