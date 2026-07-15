package com.aria.dto;

import java.util.HashMap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserAttendanceReport {

	AttendanceDto attendanceDto;

	HashMap<String, Object> attendanceReport;
}
