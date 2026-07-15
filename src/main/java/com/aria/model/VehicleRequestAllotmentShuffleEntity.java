package com.aria.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vehicle_request_driver_shuffle")
public class VehicleRequestAllotmentShuffleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="vehicle_req_allot_id")
	private Long allotmentId;


	@Column(name="reason")
	private String reason;

	@Column(name="vehicle_info_id")
	private Long vehicleInfoId;


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
