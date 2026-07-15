package com.aria.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vehicle_request_driver_additional")
public class VehicleRequestAllotmentDriverEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="vehicle_req_allot_id")
	private Long allotmentId;

	@Column(name="driver_status")
	private String driverStatus;

	@Column(name="reason")
	private String reason;

	@Column(name="driver_id")
	private Long driverId;


	@Column(name = "CREATED_BY")
	public Long createdBy;

	@Column(name = "updated_by")
	public Long updatedBy;


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
