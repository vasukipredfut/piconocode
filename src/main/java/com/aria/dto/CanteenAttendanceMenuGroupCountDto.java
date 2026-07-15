package com.aria.dto;

import com.aria.model.CanteenAttendanceMenu;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CanteenAttendanceMenuGroupCountDto {

	private String date;
	
	private Long count;

	private String type;

	public CanteenAttendanceMenuGroupCountDto(Object date, Object count, Object type) {
		super();
		this.date = date.toString();
		String a = String.valueOf(count);
		Long b = Long.parseLong(a);
		this.count = b;
		this.type = type.toString();
	}

	

	
	
}
