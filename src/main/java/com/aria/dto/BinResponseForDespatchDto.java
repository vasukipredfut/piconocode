package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class BinResponseForDespatchDto {
	private String binName;
	private Double quantity;
	private String batchNumber;
}
