package com.aria.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vehicle_request_vanboy")
public class VehicleRequestVanBoyEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="vehicle_req_allot_id")
	private Long allotmentId;

	@Column(name="user_id")
	private Long userId;

	@Column(name = "CREATED_BY")
	public Long createdBy;

	@CreatedDate
	@Column(name = "CREATED_DATE")
	public Date createdDate;

}
