package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DepayMasterDto {
	private String code;
	private String name;
	private double basic;
	private double esiceil;
	private double epfbase;
}
