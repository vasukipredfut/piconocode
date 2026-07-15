package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.CustomVouDeptDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "customvou_deptmap")
public class CustomVouDeptMap {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)	
	private Long id;
	
	@Column
	private Long customVouNameId;
	
	@Column
	private Long dept;
	
	public CustomVouDeptMap(CustomVouDeptDto customVouNameDto)
	{
		this.dept=customVouNameDto.getDept();
	}
	
	
}
