package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.Where;

import com.aria.dto.CanteenAttendanceDto;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "canteen_attendances")
public class CanteenAttendance {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Date date;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;
	
	@OneToMany(mappedBy = "attendanceMenuId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	//@Where(clause = "type='breakfast'")
	private List<CanteenAttendanceMenu> all;

	@OneToMany(mappedBy = "attendanceMenuId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Where(clause = "type='breakfast'")
	private List<CanteenAttendanceMenu> breakfast;

	@OneToMany(mappedBy = "attendanceMenuId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Where(clause = "type='lunch'")
	private List<CanteenAttendanceMenu> lunch;

	@OneToMany(mappedBy = "attendanceMenuId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Where(clause = "type='dinner'")
	private List<CanteenAttendanceMenu> dinner;

	public CanteenAttendance(CanteenAttendanceDto canteenAttendance) {
		this.id = canteenAttendance.getId();
		this.createdBy = canteenAttendance.getCreatedBy();
		this.updatedBy = canteenAttendance.getUpdatedBy();
		this.created_date = canteenAttendance.getCreated_date();
		this.updated_date = canteenAttendance.getUpdated_date();

	}
}
