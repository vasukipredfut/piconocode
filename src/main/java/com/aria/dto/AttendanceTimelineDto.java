package com.aria.dto;

import java.util.Date;

import com.aria.model.AttendanceTimeline;
import com.aria.utils.DateUtil;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class AttendanceTimelineDto {

	private Long id;

	private Long attendanceId;

	private Long userId;

	private Long instrulogId;

//	private Biomast biomast;

	private Long biomastId;

	private String location;

	private Date inTime;

	private Date outTime;

	private Date attendanceTime;

	private String attendanceDate;

	private String duration;

	private Boolean isReasonNeeded;

	private String reason;

	public AttendanceTimelineDto(AttendanceTimeline attendanceTimeline) {

		this.id = attendanceTimeline.getId();

		this.attendanceId = attendanceTimeline.getAttendanceId();

		this.userId = attendanceTimeline.getUserId();

		this.instrulogId = attendanceTimeline.getInstrulogId();

		if (attendanceTimeline.getBiomast() != null) {
			this.biomastId = attendanceTimeline.getBiomast().getSno();
			this.location = attendanceTimeline.getBiomast().getLocation();
		}

		this.inTime = attendanceTimeline.getInTime();

		this.outTime = attendanceTimeline.getOutTime();

////		DateUtil dateUtil = new DateUtil();
//		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
////		sdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
//		String attendanceTime = sdf1.format(attendanceTimeline.getAttendanceTime());
////		this.attendanceTime = attendanceTime;

		this.attendanceTime = attendanceTimeline.getAttendanceTime();

		DateUtil dateUtil = new DateUtil();
		this.attendanceDate = dateUtil.getDateToStringDDMMYYYY(attendanceTimeline.getAttendanceDate());

		this.duration = attendanceTimeline.getDuration();

		this.isReasonNeeded = attendanceTimeline.getIsReasonNeeded();

		this.reason = attendanceTimeline.getReason();
	}
}
