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
public class DespatchPoductsResponseDto {
	private Long id;
	private String producName;
	private String lot;
	private Long total;
	private Double quantity;
	private List<BinResponseForDespatchDto> bin;
}
