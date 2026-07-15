package com.aria.dto;


import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BankReptDto {
	List<BankSummaryProductDTO> productDTOList;
	   private Company firmDetails;
	    private Object plantDetails;
	    private Long year;
	    private Long month;
	    
	    

}
