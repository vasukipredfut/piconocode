package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.aria.model.Attendance;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttendanceDto {

	private Long id;

	private String secureKey;

	private Long userId;

	private String userName;

	private String date;

	private String PunchIn;

	private Object PunchInAt;

	private String PunchOut;

	private Object PunchOutAt;

	private String production;

	private String overTime;

	private String Break;

	private String BreakIn;

	private String BreakOut;

	private String overDueToBreakTimeReason;

	private String overTimeReason;

	private String status;

	private int active;
	
	private List<AttendanceTimelineDto> attendanceTimelineDtos;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public AttendanceDto(Attendance attendance) {
		this.id = attendance.getId();
		this.production = attendance.getProduction();
		this.Break = attendance.getBreak();

		if (attendance.getUser() != null) {
			this.userName = attendance.getUser().getEmployeename();
			this.userId = attendance.getUser().getId();
		}
//		this.date = attendance.getDate().toString();
		this.overDueToBreakTimeReason = attendance.getOverDueToBreakTimeReason();
		this.overTimeReason = attendance.getOverTimeReason();
		this.overTime = attendance.getOverTime();
		this.status = attendance.getStatus();
		this.active = attendance.getActive();
		
//		this.attendanceTimelineDtos = attendance.getAttendanceTimelines() != null ? attendance.getAttendanceTimelines().stream().map(data -> new AttendanceTimelineDto(data))
//				.collect(Collectors.toList()) : null;
		
		this.createdBy = attendance.getCreatedBy();
		this.updatedBy = attendance.getUpdatedBy();
		this.created_date = attendance.getCreated_date();
		if (attendance.getUpdated_date() != null) {
			this.updated_date = attendance.getUpdated_date();
		} else {
			this.updated_date = attendance.getCreated_date();
		}
	}


	private Map<String,Object> statistic;
}
