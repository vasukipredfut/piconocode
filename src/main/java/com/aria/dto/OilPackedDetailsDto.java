package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OilPackedDetailsDto {
	List<ByeProductDto>byeProduct;
	 private Company firmDetails;
	    private Object plantDetails;
	

}
