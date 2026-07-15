package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.CanteenAttendance;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CanteenAttendanceDto {
	private Long id;

	private String date;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private List<CanteenAttendanceMenuDto> breakfast;
	private List<CanteenAttendanceMenuDto> lunch;
	private List<CanteenAttendanceMenuDto> dinner;

	public CanteenAttendanceDto(CanteenAttendance canteenAttendance) {
		this.id = canteenAttendance.getId();
		this.createdBy = canteenAttendance.getCreatedBy();
		this.updatedBy = canteenAttendance.getUpdatedBy();
		this.created_date = canteenAttendance.getCreated_date();
		if (canteenAttendance.getUpdated_date() != null) {
			this.updated_date = canteenAttendance.getUpdated_date();
		} else {
			this.updated_date = canteenAttendance.getCreated_date();
		}

		this.breakfast = canteenAttendance.getBreakfast() != null ? canteenAttendance.getBreakfast().stream()
				.map(field -> new CanteenAttendanceMenuDto(field)).collect(Collectors.toList()) : null;
		this.lunch = canteenAttendance.getLunch() != null ? canteenAttendance.getLunch().stream()
				.map(field -> new CanteenAttendanceMenuDto(field)).collect(Collectors.toList()) : null;
		this.dinner = canteenAttendance.getDinner() != null ? canteenAttendance.getDinner().stream()
				.map(field -> new CanteenAttendanceMenuDto(field)).collect(Collectors.toList()) : null;
	}
}