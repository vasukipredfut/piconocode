package com.aria.dto;

import com.aria.model.CanteenAttendanceMenu;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CanteenAttendanceMenuDto {

	private Long id;

	private Long attendanceMenuId;

	public Long userId;

	public String userName;

	private String inTime;

	private String outTime;

	private String type;

	public CanteenAttendanceMenuDto(CanteenAttendanceMenu canteenAttendanceMenu) {
		this.id = canteenAttendanceMenu.getId();
		this.attendanceMenuId = canteenAttendanceMenu.getAttendanceMenuId();
		if (canteenAttendanceMenu.getUser() != null) {
			this.userName = canteenAttendanceMenu.getUser().getEmployeename();
			this.userId = canteenAttendanceMenu.getUser().getId();
		}

		this.type = canteenAttendanceMenu.getType();
	}

}
