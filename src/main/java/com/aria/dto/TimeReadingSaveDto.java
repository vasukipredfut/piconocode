package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeReadingSaveDto {
	private Long timingId;
	private String timingName;
	private Long attributeeeid;
	private String atname;
	private Double opening;
	private Double closing;
	private Double difference;
	private Double unitRate;

}
