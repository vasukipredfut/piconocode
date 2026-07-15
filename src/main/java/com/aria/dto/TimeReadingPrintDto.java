package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeReadingPrintDto {
	private Long timingId;
	private String timingName;
	private Long attributeeeid;
	private String atname;
	private Double unitRate;
}
