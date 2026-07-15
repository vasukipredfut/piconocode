package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RatehistoryProductDto {

	private Long productId;

	private String productName;

	private Long lot;

	private List<RatehistoryGroupingDto> partyList;
}
