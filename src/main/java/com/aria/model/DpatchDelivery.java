package com.aria.model;

import java.sql.Date;
import java.sql.Timestamp;

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
@Table(name = "dpatch_delivery")
public class DpatchDelivery {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
	@JoinColumn(name = " despatch_id", referencedColumnName = "id")
	private DpatchDespatch despatch_id;
	
	@Column
	private Date delivery_date;
	
	@OneToOne
	@JoinColumn(name = " vehicle_id", referencedColumnName = "id")
	private VehicleGroup vehicle_id;
	
	@OneToOne
	@JoinColumn(name = "created_By", referencedColumnName = "id")
	private User created_By;
	
	@OneToOne
	@JoinColumn(name = "party_id", referencedColumnName = "id")
	private Vendor party_id;
	
	@Column
	private Long active;
	
	@Column
	private Timestamp created_Timestamp;
	
	@Column
	private Long vehicle_Type_Id;
	
	@Column
	private String vehicle_no;
}
