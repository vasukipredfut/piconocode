package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ASRSAmendmentRequestDto {
	// 26-08-2025 created by dhivya (ASRSAmendment related field request ), discussed by alaguraja sir  
	private Long dominoCount;
	private Long amendmentCount; 
	private String reason;
	private Long asrsId;
	    

}
