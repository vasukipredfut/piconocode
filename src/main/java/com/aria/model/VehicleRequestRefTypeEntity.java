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
@Table(name = "vehicle_request_ref_type")
public class VehicleRequestRefTypeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="vehicle_req_allot_id")
	private Long allotmentId;

	@Column(name="action")
	private String action;

	@Column(name="ref_type")
	private String refType;

	@Column(name="ref_type_value")
	private String refTypeValue;

	@Column(name = "CREATED_BY")
	public Long createdBy;


	@CreatedDate
	@Column(name = "CREATED_DATE")
	public Date createdDate;

}
