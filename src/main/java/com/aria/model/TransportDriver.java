package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vehicle_driver")
public class TransportDriver {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
	@JoinColumn(name = "vehicle_id", referencedColumnName = "id")
	private Machine vehicleId;
	
	
	//From employee_detail table
	@Column(name="driver_id")
	private Long driverId;
	
	//By default is one (1 -> active and 0 -> non active)
	@Column(name="status")
	private Integer status;
	
	public TransportDriver(Long vehicleId,Long driverId) {
		Machine machine=new Machine();
		machine.setId(vehicleId);
		this.vehicleId=machine;
		this.driverId=driverId;
		this.status=1;
	}
}
