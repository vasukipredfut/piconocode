package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubProductDto {
	private Long  sub1ProuctId;
	private String sub1ProductName;
	 List<Double> lots ;
	List<Sub2ProductDto>sub2ProductList;
}
