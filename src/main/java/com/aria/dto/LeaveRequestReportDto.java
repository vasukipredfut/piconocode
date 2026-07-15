package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaveRequestReportDto {

	private Integer earnedLeavesCount;
	
	private Integer leaveWithWagesCount;
	
	private Integer maternityCount;
	
	private Integer lossOfPayCount;
	
	private Integer accidentLeaveCount;
	
	private Integer remainingLeaveCount;
	private String weekOffDay;
}
