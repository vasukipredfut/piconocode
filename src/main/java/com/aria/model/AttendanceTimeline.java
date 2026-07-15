package com.aria.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.aria.dto.AttendanceTimelineDto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "attendance_timeline")
public class AttendanceTimeline {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long attendanceId;

	@Column
	private Long userId;

	@Column
	private Long instrulogId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "biomast_id", referencedColumnName = "sno")
	private Biomast biomast;

	@Column
	private Date inTime;

	@Column
	private Date outTime;

	@Column
	private Date attendanceTime;

	@Column
	private Date attendanceDate;

	@Transient
	@Column
	private String duration;

	@Transient
	@Column
	private Boolean isReasonNeeded;

	@Column
	private String reason;
	
	@Column
	private String endposition;

	@Column(updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private Date created_date;

	@Column
	private Date updated_date;

	public AttendanceTimeline(AttendanceTimelineDto attendanceTimelineDto) {

		this.id = attendanceTimelineDto.getId();

		this.attendanceId = attendanceTimelineDto.getAttendanceId();

		this.userId = attendanceTimelineDto.getUserId();

		this.instrulogId = attendanceTimelineDto.getInstrulogId();

//		this.biomast = attendanceTimelineDto.getBiomast();

		if (attendanceTimelineDto.getBiomastId() != null) {
			Biomast biomast = new Biomast();
			biomast.setSno(attendanceTimelineDto.getBiomastId());
			this.biomast = biomast;
		}

		this.inTime = attendanceTimelineDto.getInTime();

		this.outTime = attendanceTimelineDto.getOutTime();

//		this.attendanceTime = attendanceTimelineDto.getAttendanceTime();

//		this.attendanceDate = attendanceTimelineDto.getAttendanceDate();

		this.duration = attendanceTimelineDto.getDuration();

		this.isReasonNeeded = attendanceTimelineDto.getIsReasonNeeded();

		this.reason = attendanceTimelineDto.getReason();
	}
}
