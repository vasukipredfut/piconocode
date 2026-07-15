package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.CanteenAttendanceToken;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CanteenAttendanceTokenDto {
	private Long id;

	private String date;

	private String shift1;

	private String shift2;

	private String shift3;

	private String shift1noOfTeaTokensUsed;

	private String shift1noOfSnacksTokensUsed;

	private String shift2noOfTeaTokensUsed;

	private String shift2noOfSnacksTokensUsed;

	private String shift3noOfTeaTokensUsed;

	private String shift3noOfSnacksTokensUsed;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public CanteenAttendanceTokenDto(CanteenAttendanceToken canteenAttendanceToken) {
		this.id = canteenAttendanceToken.getId();
		this.shift1 = canteenAttendanceToken.getShift1();
		this.shift2 = canteenAttendanceToken.getShift2();
		this.shift3 = canteenAttendanceToken.getShift3();
		this.shift1noOfTeaTokensUsed = canteenAttendanceToken.getShift1noOfTeaTokensUsed();
		this.shift1noOfSnacksTokensUsed = canteenAttendanceToken.getShift1noOfSnacksTokensUsed();
		this.shift2noOfTeaTokensUsed = canteenAttendanceToken.getShift2noOfTeaTokensUsed();
		this.shift2noOfSnacksTokensUsed = canteenAttendanceToken.getShift2noOfSnacksTokensUsed();
		this.shift3noOfTeaTokensUsed = canteenAttendanceToken.getShift3noOfTeaTokensUsed();
		this.shift3noOfSnacksTokensUsed = canteenAttendanceToken.getShift3noOfSnacksTokensUsed();
		this.createdBy = canteenAttendanceToken.getCreatedBy();
		this.updatedBy = canteenAttendanceToken.getUpdatedBy();
		this.created_date = canteenAttendanceToken.getCreated_date();
		this.updated_date = canteenAttendanceToken.getUpdated_date();

	}
}
