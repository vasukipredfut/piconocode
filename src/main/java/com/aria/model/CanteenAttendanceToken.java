package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import jakarta.persistence.*;

import com.aria.dto.CanteenAttendanceTokenDto;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "canteen_attendance_token")
public class CanteenAttendanceToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Date date;

	@Column
	private String shift1;

	@Column(name = "shift1no_Of_Tea_Tokens_Used")
	private String shift1noOfTeaTokensUsed;

	@Column(name = "shift1no_Of_Snacks_Tokens_Used")
	private String shift1noOfSnacksTokensUsed;

	@Column
	private String shift2;

	@Column(name = "shift2no_Of_Tea_Tokens_Used")
	private String shift2noOfTeaTokensUsed;

	@Column(name = "shift2no_Of_Snacks_Tokens_Used")
	private String shift2noOfSnacksTokensUsed;

	@Column
	private String shift3;

	@Column(name = "shift3no_Of_Tea_Tokens_Used")
	private String shift3noOfTeaTokensUsed;

	@Column(name = "shift3no_Of_Snacks_Tokens_Used")
	private String shift3noOfSnacksTokensUsed;

	@Column(name = "created_by",updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public CanteenAttendanceToken(CanteenAttendanceTokenDto canteenAttendanceToken) {
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
