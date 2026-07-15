package com.aria.dto;


import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class ValidateSpecDto {

	
	private Long specType;
	
	private List<Long> specId;


}
