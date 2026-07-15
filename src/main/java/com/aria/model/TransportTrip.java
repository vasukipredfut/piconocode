package com.aria.model;


import com.aria.dto.TransportTripDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vtrips")
public class TransportTrip {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@Column
	private String shortname;

	@Column
	private String coverage;

	@DateTimeFormat(pattern="HH:mm:ss" )
	@Temporal(TemporalType.TIME)
	@Column(name = "starttime")
	private Date startTime;

	@Column
	private String jhrs;

	@Column
	private String bhrs;

	@Column
	private String freq;

	@Column
	private String cate;

	@Column
	private String distance;

	@OneToMany(mappedBy = "trip", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<TransportTripExpenses> transportTripExpensesList;

	public TransportTrip(TransportTripDto transportTrip) throws ParseException {
		this.id = transportTrip.getId();
		this.name = transportTrip.getName();
		this.shortname = transportTrip.getShortname();
		this.coverage = transportTrip.getCoverage();
		this.startTime = new SimpleDateFormat("HH:mm:ss").parse(transportTrip.getStartTime());
		this.jhrs = transportTrip.getJhrs();
		this.bhrs = transportTrip.getBhrs();
		this.freq = transportTrip.getFreq();
		this.cate = transportTrip.getCate();
		this.distance = transportTrip.getDistance();
	}

}
