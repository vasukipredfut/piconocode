package com.aria.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.TransportDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "vinfo")
public class Transport {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@OneToOne
	@JoinColumn(name = "vid", referencedColumnName = "id")
	private Machine vid;
	
	@Column
	private String capacity;
	
	@Column
	private String tcapacity;
	
	@Column
	private String cate;
	
	@Column
	private String ftype;
	
	@Column
	private Long refid;
	
	@Column
	private String status;
	
	@Column
	private String battery;
	
	@Column
	private String engine;
	
	@Column
	private String chassis;
	
	@Column
	private String tyre1;
	
	@Column
	private String tyre2;
	
	@Column
	private String tyre3;
	
	@Column
	private String tyre4;
	
	@Column
	private String tyre5;
	
	@Column
	private String tyre6;
	
	@Column
	private String tyre7;
	
	@Column
	private String tyre8;
	
	@Column
	private String tyre9;
	
	@Column
	private String tyre10;

	@Column
	private Timestamp upon;
	
	@Column
	private Integer mmx;

	@Column
	private Long upby;
	
	@Column
	private Integer target;

	@OneToOne
	@JoinColumn(name = "driver_id", referencedColumnName = "id")
	private User driver_id;
	
	public Transport(TransportDto transport) {
		Machine m=new Machine();
		m.setId(transport.getSelectedVehicle());
		this.vid=m;
		this.capacity=transport.getCapacity();
		this.cate=transport.getVehicleType();
		this.tcapacity=transport.getTankCapacity();
		this.ftype=transport.getFuelType();
		this.mmx=transport.getMaximumMilage();
		this.upby=0L;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date date=new java.util.Date();
		String dateString = dateFormat.format(date);
		Timestamp timestamp = Timestamp.valueOf(dateString);
		this.upon= timestamp;

		User user=new User();
		user.setId(transport.getDriverId());

		this.driver_id=user;
	}
}
