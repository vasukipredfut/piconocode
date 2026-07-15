package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReasonDto {
	private Long id;
	private String reason;
	private String intime;
	private String outtime;
	private String totalworkingdays;

}
