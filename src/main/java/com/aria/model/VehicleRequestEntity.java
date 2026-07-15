package com.aria.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vehicle_request")
public class VehicleRequestEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="trip_id")
	private Long tripId;

	@Column(name="type_of_vehicle")
	private String typeofVehicle;


	@DateTimeFormat(pattern="HH:mm:ss" )
	@Temporal(TemporalType.TIME)
	@Column(name = "start_time")
	private Date startTime;

	@Column(name ="travel_distance")
	private String travelDistance;

	@Column(name="no_of_vehicle")
	private Long noofvehicle;

	@Column
	private String capacity;

	@Column(name = "req_department")
	private Long reqDepartment;

	@Column(name="req_plant")
	private Long reqPlant;

	@Column(name ="requested_by")
	private Long requestedBy;

	@Column(name="travel_date")
	private Date  travelDate;

	@CreatedDate
	@Column(name = "CREATED_DATE")
	public Date createdDate;

	@LastModifiedDate
	@Column(name = "UPDATED_DATE")
	public Date updatedDate;

	@PreUpdate
	public void updateDate() {
		this.setUpdatedDate(new Date());
	}

}
