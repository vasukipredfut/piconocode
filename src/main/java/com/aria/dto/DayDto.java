package com.aria.dto;

import com.aria.model.Day;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DayDto {

	private Long id;

	private String dayName;

	public DayDto(Day day) {
		this.id = day.getId();
		this.dayName = day.getDayName();
	}
}
