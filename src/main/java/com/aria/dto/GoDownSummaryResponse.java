package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoDownSummaryResponse {

	List<GoDownSummaryResult>goDownDetails;
	   private Company firmDetails;
	    private Object plantDetails;
}
