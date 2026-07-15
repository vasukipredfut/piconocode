package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RatehistoryGroupingDto {

	private String partyName;

	private Long partyId;

	private List<RatehistoryPartyGroupingDto> partyGroupingDto;

}
